package com.xs.service.gdax;

import com.sun.tools.javac.util.Pair;
import com.xs.model.gdax.Account;
import com.xs.model.gdax.Currency;
import com.xs.model.gdax.Order;
import com.xs.model.gdax.PriceElement;
import com.xs.model.gdax.ProductId;
import com.xs.util.TransactionLogger;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

@Slf4j
public class AverageMoverTask implements Runnable {
	private final AccountService accountService;
	private final OrderService orderService;
	private final ProductService productService;
	private final TransactionLogger transactionLogger;

	private TreeSet<PriceElement> priceElements;
	private AtomicBoolean hasInit;

	public AverageMoverTask(AccountService accountService,
													OrderService orderService,
													ProductService productService,
													TreeSet<PriceElement> priceElements,
													TransactionLogger transactionLogger) {

		this.accountService = accountService;
		this.orderService = orderService;
		this.productService = productService;
		this.priceElements = priceElements;
		this.transactionLogger = transactionLogger;

//		if (hasInit.compareAndSet(false, true)) {
//			initialize();
//		}
//
	}

	public TreeSet<PriceElement> initializePriceElements() {
		TreeSet<PriceElement> priceElements = new TreeSet<>((p1, p2) -> Double.compare(p1.getUnitPrice(), p2.getCurrencyUnit()));
		try {
			Map<String, Account> currencyToAccount = accountService.getAccounts();
			Pair<Double, Double> bestBidAskPair = productService.getBestLimitBuySellPricePair(ProductId.BTC_USD);

			// use available since there might be some transaction on hold
			double availableBtc = currencyToAccount.get(Currency.BTC).getAvailable();
			double btcPrice = bestBidAskPair.snd; // treat current best ask price is btc price

			double availableUsd = currencyToAccount.get(Currency.USD).getAvailable();
			double totalAccountAvailableBalance = availableBtc * btcPrice + availableUsd;

			double percent = availableBtc / totalAccountAvailableBalance;
			if (percent > 0.8) {
				List<String> orderList = orderService.cancelAllOrders();
				log.error("failed to init priceElements, order cancelled due to too much btc allocated: " + orderList);
				throw new IllegalStateException("availableBtc: " + availableBtc + " btcPrice: " + btcPrice
					+ " availableUsd: " + availableUsd + " percent: " + percent + " > 80%");
			} else if (percent > 0.4) {
				int nbuckets = (int) ((float) percent / (float) 0.1);
				IntStream.range(0, nbuckets)
					.forEach(i -> priceElements.add(new PriceElement("INIT_BTC_" + i, availableBtc / nbuckets, btcPrice, System.currentTimeMillis())));
			} else {
				// TODO : solve the problem that price will drop since bestBidAskPair is computed, here just re-calculate to relect latest price
				double lastedBuyPrice = productService.getBestLimitBuySellPricePair(ProductId.BTC_USD).fst;
				double totalCash = totalAccountAvailableBalance * 0.4 - availableBtc * btcPrice;
				Order placedOrder = orderService.placeBuyWithTotalCash(ProductId.BTC_USD, lastedBuyPrice, totalCash);
				transactionLogger.writeLog("placeBuyWithTotalCash: " + totalCash + "\n" + "Detail: " + placedOrder + "\n");
//				IntStream.range(0, 4)
//					.forEach(i -> priceElements.add(new PriceElement("INIT_BTC_" + i, placedOrder., placedOrder., System.currentTimeMillis())));
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return priceElements;

	}



	@Override
	public void run() {


	}

}
