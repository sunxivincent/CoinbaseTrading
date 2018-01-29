package com.xs.service.strategy;

import com.github.rholder.retry.RetryException;
import com.google.common.annotations.VisibleForTesting;
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
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
// current this only supports btc trading
public class AverageMoverTask implements Runnable {
	// in order to make placing limit order easier, widen the range a little bit
	public static final double ADJUST_BID_ASK_RANGE = 0.02;
	public static final double REMAINING_UNIT_IGNORED = 0.005;
	public static final int TOTAL_BUCKET = 100;
	public static final double BTC_INIT_PERCENT_UPPRER = 0.5;
	public static final double BTC_INIT_PERCENT_LOWER = 0.2; // if below this threshold need to buy more
	public static final double BUCKET_PERCENT = 1.0 / TOTAL_BUCKET;
	public static final double MIN_CASH = 1;

	@NonNull private final AccountService accountService;
	@NonNull private final OrderService orderService;
	@NonNull private final ProductService productService;
	@NonNull private final TransactionLogger transactionLogger;
	// only place buy or sell when adjacent element's price larger than delta
	private final double priceDelta; // need to be larger than ADJUST_BID_ASK_RANGE

	private final TreeSet<PriceElement> priceElements;
	private final AtomicBoolean hasInit = new AtomicBoolean(false);

	@Autowired
	public AverageMoverTask(AccountService accountService, OrderService orderService,
													ProductService productService, TransactionLogger transactionLogger,
													@Value("${gdax.strategy.movingAverage.priceDelta}") double priceDelta) {
		if (priceDelta < ADJUST_BID_ASK_RANGE) {
			throw new IllegalArgumentException("priceDelta: " + priceDelta + " less than " + ADJUST_BID_ASK_RANGE);
		}
		this.priceDelta = priceDelta;
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
			if (accountBTCStat.btcPercent > BTC_INIT_PERCENT_UPPRER) {
				List<String> orderList = orderService.cancelAllOrders();
				log.error("failed to init priceElements, order cancelled due to too much btc allocated: " + orderList);
				throw new IllegalStateException("availableBtc: " + accountBTCStat.availableBtc + " btcPrice: " + accountBTCStat.btcPrice
					+ " availableUsd: " + accountBTCStat.availableUsd + " btcPercent: " + accountBTCStat.btcPercent + " >" + BTC_INIT_PERCENT_UPPRER);
			} else if (accountBTCStat.btcPercent > BTC_INIT_PERCENT_LOWER) {
				int nbuckets = (int) ((float) accountBTCStat.btcPercent / (float) BUCKET_PERCENT);
				initEnqueue(priceElements, nbuckets, accountBTCStat.availableBtc, accountBTCStat.btcPrice);
			} else {
				// TODO : solve the problem that price will drop since bestBidAskPair is computed, here just re-calculate to reflect latest price
				accountBTCStat = getAccountBTCStat();
				double targetCash = accountBTCStat.totalAccountAvailableBalance * (BTC_INIT_PERCENT_LOWER - accountBTCStat.btcPercent);
				log.info("start init task best effort buy order with targetCash: " + targetCash + " current " + accountBTCStat);
				if (targetCash > MIN_CASH) {
					Order placedOrder = orderService.bestEffortBuyWithTotalCash(ProductId.BTC_USD, accountBTCStat.btcBuyPrice - ADJUST_BID_ASK_RANGE, targetCash,
						Optional.of(600)); // wait at most 10 min until buy order finish
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
			if (priceElements.size() > (int)(TOTAL_BUCKET * BTC_INIT_PERCENT_UPPRER) || priceElements.size() < (int)(TOTAL_BUCKET * BTC_INIT_PERCENT_LOWER)) {
				throw new IllegalStateException("PriceElements size: " + priceElements.size() + " is not expected");
			}
		} catch (Exception e) {
			log.error("error occurred during PriceElement", e);
			throw new IllegalStateException(e);
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
			log.info("starting running task");
			if(hasInit.compareAndSet(false, true)) {
				initializePriceElements();
			}
			if (CollectionUtils.isEmpty(priceElements)) {
				log.error("failed to start task since price elements is empty");
				throw new IllegalStateException();
			}
			AccountBTCStat accountBTCStat = getAccountBTCStat();
			Pair<PriceElement, PriceElement> pair = PriceElement.getLowerAndHigher(accountBTCStat.btcPrice, priceElements, priceDelta);
			Pair<PriceElement, PriceElement> currentPriceLocation = PriceElement.getLowerAndHigher(accountBTCStat.btcPrice, priceElements, 0);
			if (pair.fst == null) {
				// place order when current price is lowest and the adjacent order price difference less than threshold
				// also make sure there is no two elements with same price were enqueued initially that are counted in the
				// lower higher calculation
				if (priceElements.size() < TOTAL_BUCKET
					&& currentPriceLocation.snd != null
					&& Math.abs(currentPriceLocation.snd.getUnitCostBasis() - accountBTCStat.btcPrice) > priceDelta) {
					buyOneBucket(priceElements, accountBTCStat);
				}
			} else if (priceElements.size() < (int)(TOTAL_BUCKET * BTC_INIT_PERCENT_LOWER)) { // keep some btc orders
				buyOneBucket(priceElements, accountBTCStat);
			} else { // time to make some profits
				sellOneBucket(priceElements, pair.fst, accountBTCStat);
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
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

	private AccountBTCStat getAccountBTCStat() throws ExecutionException, RetryException {
		Map<String, Account> currencyToAccount = accountService.getAccounts();
		Pair<Double, Double> bestBidAskPair = productService.getBestLimitBuySellPricePair(ProductId.BTC_USD);
		double availableBtc = currencyToAccount.get(Currency.BTC).getAvailable();
		double btcPrice = bestBidAskPair.snd; // treat current best ask price is btc price
		double availableUsd = currencyToAccount.get(Currency.USD).getAvailable();
		double totalAccountAvailableBalance = availableBtc * btcPrice + availableUsd;
		double btcPercent = 1 - availableUsd / totalAccountAvailableBalance;

		return new AccountBTCStat(totalAccountAvailableBalance, availableUsd, availableBtc,
			btcPrice, bestBidAskPair.fst, btcPercent);
	}

	private void buyOneBucket(TreeSet<PriceElement> priceElements, AccountBTCStat accountBTCStat) throws ExecutionException, RetryException, IOException {
		log.info("starting buy one bucket" + accountBTCStat);
		double targetCash = Math.min(accountBTCStat.availableUsd, accountBTCStat.totalAccountAvailableBalance * BUCKET_PERCENT);
		if (targetCash < MIN_CASH) {
			log.info("targetCash: " + targetCash + " is not sufficient, wait for next sell opportunity " + accountBTCStat);
			return;
		}
		Order placedOrder = orderService.bestEffortBuyWithTotalCash(ProductId.BTC_USD, accountBTCStat.btcBuyPrice - ADJUST_BID_ASK_RANGE,
			targetCash, Optional.empty());
		// make sure we have at least partial order settled
		if (placedOrder == null || placedOrder.getFilled_size() == 0 || placedOrder.getExecuted_value() == 0) {
			log.info("possible due to unexpected limit price or price is not good, look for next opportunities");
			return;
		}
		transactionLogger.writeLog("Bought with targetCash: " + targetCash + " " + placedOrder + " " + accountBTCStat);
		priceElements.add(new PriceElement(placedOrder.getId(),
			placedOrder.getFilled_size(), placedOrder.getPrice(), System.currentTimeMillis()));

	}

	private void sellOneBucket(TreeSet<PriceElement> priceElements, PriceElement priceElement, AccountBTCStat accountBTCStat) throws ExecutionException, RetryException, IOException {
		log.info("starting sell one bucket " + priceElement + " " + accountBTCStat);
		if (!priceElements.contains(priceElement)) {
			log.error("priceElement is not included in set " + priceElements);
			throw new IllegalArgumentException();
		}
		Order placedOrder = orderService.bestEffortSellWithSize(ProductId.BTC_USD, accountBTCStat.btcPrice + ADJUST_BID_ASK_RANGE,
			priceElement.getUnit(), Optional.empty());
		if (placedOrder == null || placedOrder.getFilled_size() == 0 || placedOrder.getExecuted_value() == 0) {
			log.info("possible due to unexpected limit price or price is not good, look for next opportunities");
			return;
		}
		// there is some chance that only partial order settled, if the remaining part is negligible then we still remove element
		// from priceElements set, it will eventually catch up, if not we need to modify the unit field to reflect the actual order we sold
		double profit = placedOrder.getFilled_size() * (placedOrder.getPrice() - priceElement.getUnitCostBasis());
		transactionLogger.writeLog("Sold with profit: " + profit + " cost basis: " + priceElement.getUnitCostBasis() + " sell price: " + placedOrder.getPrice() + placedOrder + " " + accountBTCStat);
		if (profit < 0) {
			log.error("order placed: " + placedOrder + " has negative profit: " + profit);
			if (profit < -50.0) {
				throw new IllegalStateException("unexpected profit: " + profit);
			}
		}
		double remainUnit = priceElement.getUnit() - placedOrder.getFilled_size();
		if (remainUnit < REMAINING_UNIT_IGNORED) {
			log.info("delete " + priceElement + " from set since remain unit: " + remainUnit + " is negligible " + placedOrder);
			priceElements.remove(priceElement);
		} else {
			priceElement.setUnit(remainUnit);
		}
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
