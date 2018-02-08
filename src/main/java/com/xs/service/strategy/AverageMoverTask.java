package com.xs.service.strategy;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.AtomicDouble;
import com.sun.tools.javac.util.Pair;
import com.xs.model.gdax.Account;
import com.xs.model.gdax.Currency;
import com.xs.model.gdax.Order;
import com.xs.model.gdax.PriceElement;
import com.xs.model.gdax.ProductId;
import com.xs.service.gdax.AccountService;
import com.xs.service.gdax.OrderService;
import com.xs.service.gdax.ProductService;
import com.xs.util.TransactionLogger;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
// current this only supports btc trading
public class AverageMoverTask implements Runnable {
	// in order to make placing limit order easier, widen the range a little bit
	public static final double ADJUST_BID_ASK_RANGE = 0;
	public static final double REMAINING_UNIT_IGNORED = 0.002;
	public static final int TOTAL_BUCKET = 20;
	public static final double BTC_INIT_PERCENT_LOWER = 0.1; // if below this threshold need to buy more
	public static final double BUCKET_PERCENT = 1.0 / TOTAL_BUCKET;
	public static final double MIN_CASH = 1;

	@NonNull private final AccountService accountService;
	@NonNull private final OrderService orderService;
	@NonNull private final ProductService productService;
	@NonNull private final TransactionLogger transactionLogger;
	// only place buy or sell when adjacent element's price larger than delta
	private final double initDelta; // need to be larger than ADJUST_BID_ASK_RANGE

	private final TreeSet<PriceElement> priceElements;
	private final AtomicBoolean hasInit = new AtomicBoolean(false);
	private final AtomicDouble totalProfit = new AtomicDouble(0);
	private boolean prevOp = false;  // buy
	private double buyDelta;
	private double sellDelta;
	private int extraWaitSec = 5;

	@Autowired
	public AverageMoverTask(AccountService accountService, OrderService orderService,
													ProductService productService, TransactionLogger transactionLogger,
													@Value("${gdax.strategy.movingAverage.initDelta}") double initDelta) {
		if (initDelta < ADJUST_BID_ASK_RANGE) {
			throw new IllegalArgumentException("initDelta: " + initDelta + " less than " + ADJUST_BID_ASK_RANGE);
		}
		this.initDelta = initDelta;
		this.buyDelta = initDelta;
		this.sellDelta = initDelta;

		this.priceElements = new TreeSet<>(new PriceElement.PriceElementComparator());
		this.accountService = accountService;
		this.orderService = orderService;
		this.productService = productService;
		this.transactionLogger = transactionLogger;
	}

	private void initializePriceElements() {
		log.info("init priceElements....");
		try {
			AccountBTCStat accountBTCStat = getAccountBTCStat();
			transactionLogger.writeLog("[account start]: " + accountBTCStat);
			if (accountBTCStat.btcPercent > BTC_INIT_PERCENT_LOWER) {
				int nbuckets = (int) ((float) accountBTCStat.btcPercent / (float) BUCKET_PERCENT);
				initEnqueue(priceElements, nbuckets, accountBTCStat.availableBtc, accountBTCStat.btcPrice);
			} else {
				double targetCash = accountBTCStat.totalAccountAvailableBalance * (BTC_INIT_PERCENT_LOWER - accountBTCStat.btcPercent);
				log.info("start init task best effort buy order with targetCash: " + targetCash + " current " + accountBTCStat);
				if (targetCash > MIN_CASH) {
					Order placedOrder = orderService.bestEffortBuyWithTotalCash(ProductId.BTC_USD, accountBTCStat.btcBuyPrice - ADJUST_BID_ASK_RANGE, targetCash,
						Optional.of(600), true); // wait at most 10 min until buy order finish
					if (placedOrder == null) {
						log.error("failed to init priceElements, too many time retried");
						throw new IllegalStateException();
					}
					transactionLogger.writeLog("Bought with targetCash: " + targetCash + " " + placedOrder + " " + accountBTCStat);
					double enqueuedBtcSize = placedOrder.getFilled_size() + accountBTCStat.availableBtc;
					double enqueuedBtcAverageCostBasis =
						(accountBTCStat.totalAccountAvailableBalance * accountBTCStat.btcPercent + placedOrder.getExecuted_value()) / enqueuedBtcSize;
					initEnqueue(priceElements, (int)(TOTAL_BUCKET * BTC_INIT_PERCENT_LOWER), enqueuedBtcSize, enqueuedBtcAverageCostBasis);
				} else {
					log.error("failed to start init task since targetCash: " + targetCash);
					throw new IllegalStateException();
				}
			}
			if (priceElements.size() > TOTAL_BUCKET || priceElements.size() < (int)(TOTAL_BUCKET * BTC_INIT_PERCENT_LOWER)) {
				throw new IllegalStateException("PriceElements size: " + priceElements.size() + " is not expected");
			}
		} catch (Exception e) {
			throw new IllegalStateException("error occurred during PriceElement", e);
		}
	}

	@VisibleForTesting
	public static void initEnqueue(TreeSet<PriceElement> priceElements, int nBucket, double availableBtc, double unitCostBasis) {
		log.info("enqueue nBucket: " + nBucket + " size: " + availableBtc + " cost basis: " + unitCostBasis);
		for (int i = 0; i < nBucket; i++) {
			priceElements.add(new PriceElement("INIT_BTC_" + i + "_" + UUID.randomUUID().toString(), availableBtc / nBucket, unitCostBasis, System.currentTimeMillis()));
		}
	}

	@Override
	public void run() {
		try {
			if(hasInit.compareAndSet(false, true)) {
				log.info("starting running task");
				initializePriceElements();
			}

			AccountBTCStat accountBTCStat = getAccountBTCStat();
			if (accountBTCStat == null) {
				log.error("failed to get accountBTCStat, wait 30s for next run");
				Thread.sleep(30*1000);
				return;
			}

			Pair<PriceElement, PriceElement> zeroDeltaPair = PriceElement.getLowerAndHigher(accountBTCStat.btcPrice, priceElements, 0);
			if (zeroDeltaPair != null &&
				  zeroDeltaPair.fst != null && accountBTCStat.btcPrice - zeroDeltaPair.fst.getUnitCostBasis() > sellDelta) {
				boolean success = sellOneBucket(priceElements, zeroDeltaPair.fst, accountBTCStat);
				if (success) {
					if (prevOp) {
						extraWaitSec = Math.min(60, extraWaitSec+10);;
						sellDelta += 20;
					} else {
						extraWaitSec = 10;
					}
					buyDelta = Math.max(initDelta, buyDelta - 20);
					if (priceElements.size() < TOTAL_BUCKET * 0.5) {
						buyDelta = initDelta;
					}
					prevOp = true;
					log.info("sleep: " + extraWaitSec + " s, buy delta: " + buyDelta + " sell delta: " + sellDelta);
					Thread.sleep(extraWaitSec * 1000);
				}
			} else if (zeroDeltaPair == null ||
					zeroDeltaPair.snd != null && zeroDeltaPair.snd.getUnitCostBasis() - accountBTCStat.btcPrice > buyDelta &&
					(zeroDeltaPair.fst == null || accountBTCStat.btcPrice - zeroDeltaPair.fst.getUnitCostBasis() > buyDelta)) {
				if (priceElements.size() < TOTAL_BUCKET) {
					boolean success = buyOneBucket(priceElements, accountBTCStat);
					if (success) {
						if (!prevOp) {
							extraWaitSec = Math.min(60, extraWaitSec+10);;
							buyDelta += 20;
						} else {
							extraWaitSec = 10;
						}
						sellDelta = Math.max(initDelta, sellDelta - 20);
						if (priceElements.size() >= TOTAL_BUCKET * 0.5) {
							sellDelta = initDelta;
						}
						prevOp = false;
						log.info("sleep: " + extraWaitSec + " s, buy delta: " + buyDelta + " sell delta: " + sellDelta);
						Thread.sleep(extraWaitSec * 1000);
					}
				}
			}
		} catch (Exception e) {
			throw new IllegalStateException("unexpected error occurred during task run", e);
		}
	}

	public void stop() {
		try {
			List<String> cancelledOrders = orderService.cancelAllOrders();
			hasInit.compareAndSet(true, false);
			log.info("orders cancelled: " + cancelledOrders);
		} catch (Exception e) {
			log.error("stop failed for cancelling orders");
		}
	}

	private AccountBTCStat getAccountBTCStat() {
		Map<String, Account> currencyToAccount = accountService.getAccounts();
		Pair<Double, Double> bestBidAskPair = productService.getBestLimitBuySellPricePair(ProductId.BTC_USD);
		if (bestBidAskPair == null) return null;
		double availableBtc = currencyToAccount.get(Currency.BTC).getAvailable();
		double btcPrice = bestBidAskPair.snd; // treat current best ask price is btc price
		double availableUsd = currencyToAccount.get(Currency.USD).getAvailable();
		double totalAccountAvailableBalance = availableBtc * btcPrice + availableUsd;
		double btcPercent = 1 - availableUsd / totalAccountAvailableBalance;
		return new AccountBTCStat(totalAccountAvailableBalance, availableUsd, availableBtc,
			btcPrice, bestBidAskPair.fst, btcPercent);
	}

	private boolean buyOneBucket(TreeSet<PriceElement> priceElements, AccountBTCStat accountBTCStat) {
		log.info("starting buy one bucket, btc price: " + accountBTCStat.btcPrice);
		double targetCash = Math.min(accountBTCStat.availableUsd, accountBTCStat.totalAccountAvailableBalance * BUCKET_PERCENT);
		if (targetCash < MIN_CASH) {
			log.info("targetCash: " + targetCash + " is not sufficient, wait for next sell opportunity " + accountBTCStat);
			return false;
		}
		Order placedOrder = orderService.bestEffortBuyWithTotalCash(
			ProductId.BTC_USD,
			accountBTCStat.btcBuyPrice - ADJUST_BID_ASK_RANGE,
			targetCash, Optional.empty(), false);
		// make sure we have at least partial order settled
		if (placedOrder == null || placedOrder.getFilled_size() == 0 || placedOrder.getExecuted_value() == 0) {
			log.info("possible due to unexpected limit price or price is not good, look for next opportunities");
			return false;
		}
		transactionLogger.writeLog("Bought with targetCash: " + targetCash + " " + placedOrder + " " + accountBTCStat);
		priceElements.add(
			new PriceElement(
				placedOrder.getId(),
				placedOrder.getFilled_size(),
				placedOrder.getPrice(),
				System.currentTimeMillis()));
		transactionLogger.writeLog(getQueueInfo());
		return true;
	}

	private boolean sellOneBucket(TreeSet<PriceElement> priceElements, PriceElement priceElement, AccountBTCStat accountBTCStat) {
		log.info("starting sell one bucket, unit price: " + priceElement.getUnitCostBasis() + " unit: " + priceElement.getUnit()
			+ accountBTCStat);
		if (!priceElements.contains(priceElement)) {
			log.error("priceElement" + priceElement + " is not included in set " + priceElements);
			throw new IllegalArgumentException();
		}
		Order placedOrder = orderService.bestEffortSellWithSize(ProductId.BTC_USD, accountBTCStat.btcPrice + ADJUST_BID_ASK_RANGE,
			priceElement.getUnit(), Optional.empty(), false);
		if (placedOrder == null || placedOrder.getFilled_size() == 0 || placedOrder.getExecuted_value() == 0) {
			log.info("possible due to unexpected limit price or price is not good, look for next opportunities");
			return false;
		}
		// there is some chance that only partial order settled, if the remaining part is negligible then we still remove element
		// from priceElements set, it will eventually catch up, if not we need to modify the unit field to reflect the actual order we sold
		double profit = placedOrder.getFilled_size() * (placedOrder.getPrice() - priceElement.getUnitCostBasis());
		transactionLogger.writeLog(
			"Current total profit: " + totalProfit.addAndGet(profit) +
			" Sold with profit: " + profit +
			" cost basis: " + priceElement.getUnitCostBasis() +
			" settled price: " + placedOrder.getPrice() + placedOrder + " " + accountBTCStat);
		if (profit < 0) {
			log.error("order placed: " + placedOrder + " has negative profit: " + profit);
			if (profit < -50.0) {
				throw new IllegalStateException("unexpected profit: " + profit);
			}
		}
		double remainUnit = priceElement.getUnit() - placedOrder.getFilled_size();
		if (remainUnit < REMAINING_UNIT_IGNORED) {
			priceElements.remove(priceElement);
			log.info("delete " + priceElement + " from set since remain unit: " + remainUnit + " is negligible " + placedOrder);
		} else {
			priceElement.setUnit(remainUnit);
		}
		transactionLogger.writeLog(getQueueInfo());
		return true;
	}

	private String getQueueInfo() {
		StringBuilder priceElementsInfo = new StringBuilder();
		priceElementsInfo.append("queue info: \n");
		priceElements.forEach(e -> priceElementsInfo.append(e.getUnitCostBasis() + " | " + e.getUnit() + "\n"));
		return priceElementsInfo.toString();
	}

	private class AccountBTCStat {
		double totalAccountAvailableBalance;
		double availableBtc;
		double availableUsd;
		double btcPrice;
		double btcBuyPrice;
		double btcPercent;

		public AccountBTCStat(double totalAccountAvailableBalance, double availableUsd, double availableBtc, double btcPrice,
													double btcBuyPrice, double btcPercent) {
			this.totalAccountAvailableBalance = totalAccountAvailableBalance;
			this.availableBtc = availableBtc;
			this.availableUsd = availableUsd;
			this.btcPrice = btcPrice;
			this.btcBuyPrice = btcBuyPrice;
			this.btcPercent = btcPercent;
		}

		public String toString() {
			return "[AccountBTCStat]: " + " totalAccountAvailableBalance: " + totalAccountAvailableBalance +
				" availableBtc: " + availableBtc + " availableUsd: " + availableUsd + " btcPrice: " + btcPrice +
				" btcBuyPrice: " + btcBuyPrice + " btcPercent: " + btcPercent;
		}
	}
}
