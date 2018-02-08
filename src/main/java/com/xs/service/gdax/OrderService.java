package com.xs.service.gdax;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.sun.tools.javac.util.Pair;
import com.xs.model.gdax.Fill;
import com.xs.model.gdax.Order;
import com.xs.model.gdax.ProductId;
import com.xs.util.MathUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OrderService {
	private static final double MIN_BUY_UNIT = 0.001;
	private static final String ORDERS_ENDPOINT = "/orders";
	private static final String FILLS_ENDPOINT = "/fills";
	private static final String BUY = "buy";
	private static final String SELL = "sell";

	private static final Retryer<Order> GENERAL_RETRYER = RetryerBuilder.<Order>newBuilder()
		.retryIfRuntimeException()
		.withWaitStrategy(WaitStrategies.exponentialWait(2000, TimeUnit.MILLISECONDS))
		.withStopStrategy(StopStrategies.stopAfterAttempt(3))
		.build();
	private static final Retryer<List<Fill>> FILL_RETRYER = RetryerBuilder.<List<Fill>>newBuilder()
		.retryIfRuntimeException()
		.withWaitStrategy(WaitStrategies.exponentialWait(2000, TimeUnit.MILLISECONDS))
		.withStopStrategy(StopStrategies.stopAfterAttempt(3))
		.build();
	private static final Retryer<List<String>> CANCEL_RETRYER = RetryerBuilder.<List<String>>newBuilder()
		.retryIfRuntimeException()
		.withWaitStrategy(WaitStrategies.exponentialWait(3000, TimeUnit.MILLISECONDS))
		.withStopStrategy(StopStrategies.stopAfterAttempt(5))
		.build();

	private final GdaxTradingServiceTemplate gdaxTradingServiceTemplate;
	private final ProductService productService;

	@Autowired
	public OrderService(GdaxTradingServiceTemplate gdaxTradingServiceTemplate, ProductService productService) {
		this.gdaxTradingServiceTemplate = gdaxTradingServiceTemplate;
		this.productService = productService;
	}

	public Order getOrder(String orderId) throws ExecutionException, RetryException {
		return GENERAL_RETRYER.call(() -> gdaxTradingServiceTemplate.get(ORDERS_ENDPOINT + "/" + orderId,new ParameterizedTypeReference<Order>(){}));
	}

	// TODO: revisit using getWithParams
//	public Fill getFill(String orderId) throws ExecutionException, RetryException {
//		List<Fill> fillList = FILL_RETRYER.call(() -> gdaxTradingServiceTemplate.getWithParams(
//			FILLS_ENDPOINT + "?order_id={order_id}",
//			new ParameterizedTypeReference<List<Fill>>(){},
//			ImmutableMap.of("order_id", orderId)));
//		return CollectionUtils.isEmpty(fillList) ? null : fillList.get(0);
//	}

	// return null if order is not existed (i.e., cancelled)
	// one order id can corresponding to multiple settled fills
	public List<Fill> getFillsByOrderId(String orderId) throws ExecutionException, RetryException {
		return FILL_RETRYER.call(() -> gdaxTradingServiceTemplate.get(
			FILLS_ENDPOINT + "?order_id=" + orderId,
			new ParameterizedTypeReference<List<Fill>>(){}));
	}

	public List<Fill> getFills() throws ExecutionException, RetryException {
		return FILL_RETRYER.call(() -> gdaxTradingServiceTemplate.get(
			FILLS_ENDPOINT, new ParameterizedTypeReference<List<Fill>>(){}));
	}

	// this would be best effort, if the order is partially fulfilled, we will cancel orders after max retries
	// also note there is precision requirement. So only get 5 decimal maximum
	public Order placeLimitOrder(String productId, String side, double size, double price) throws ExecutionException, RetryException {
		double limitSize = Math.max(MIN_BUY_UNIT, size);
		return GENERAL_RETRYER.call(() -> gdaxTradingServiceTemplate.post(ORDERS_ENDPOINT, new ParameterizedTypeReference<Order>(){},
			new NewLimitOrder(productId, side, MathUtil.roundDoubleTo5Decimal(price), MathUtil.roundDoubleTo5Decimal(limitSize))
		));
	}

	public List<String> cancelAllOrders() throws ExecutionException, RetryException {
		return CANCEL_RETRYER.call(() -> Arrays.asList(gdaxTradingServiceTemplate.delete(ORDERS_ENDPOINT, new ParameterizedTypeReference<String[]>(){})));
	}

	/**
	 *
	 * @param productId example: BTC-USD
	 * @param unitPrice the placed unit price in the order
	 * @param targetCash the total cash you want to place for buying given product
	 * @param init
	 * @return
	 * @throws ExecutionException
	 * @throws RetryException
	 */
	public Order bestEffortBuyWithTotalCash(String productId, double unitPrice, double targetCash, Optional<Integer> maxtry, boolean init) {
		if (unitPrice == 0) throw new IllegalStateException(productId + " unitPrice is 0");
		try {
			return placeOrderUntilSettled(productId, BUY, targetCash / unitPrice, unitPrice, maxtry, init);
		} catch (ExecutionException | RetryException ex) {
			log.error("error occurred during bestEffortBuyWithTotalCash unit price: " + unitPrice + " target cash: " + targetCash, ex);
			return null;
		}
	}

	public Order bestEffortSellWithSize(String productId, double unitPrice, double size, Optional<Integer> maxtry, boolean init) {
		if (unitPrice == 0) throw new IllegalStateException(productId + " unitPrice is 0");
		try {
			return placeOrderUntilSettled(productId, SELL, size, unitPrice, maxtry, init);
		} catch (ExecutionException | RetryException ex) {
			log.error("error occurred during bestEffortSellWithSize unit price: " + unitPrice + " size: " + size, ex);
			return null;
		}
	}

	private Order placeOrderUntilSettled(String productId, String side, double size, double unitPrice, Optional<Integer> maxtry, boolean init)
		throws ExecutionException, RetryException {
		Order placedOrder = placeLimitOrder(productId, side, size, unitPrice);
		int max = maxtry.orElse(10);
		int hasTried = 0;
		while (hasTried < max) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.error("sleep error");
			}
			placedOrder = getOrder(placedOrder.getId());
			if (placedOrder.isSettled() && "done".equals(placedOrder.getStatus())) {
				return placedOrder;
			}
			if (!init) {
				Pair<Double, Double> buyAndSellPrice = productService.getBestLimitBuySellPricePair(ProductId.BTC_USD);
				if (buyAndSellPrice != null) {
					if (BUY.equals(side)) {
						if (buyAndSellPrice.fst > unitPrice) {
							log.info("buy price: " + unitPrice + " is not current optimal: " + buyAndSellPrice.fst);
							hasTried = max;
							break;
						}
					} else {
						if (buyAndSellPrice.snd < unitPrice) {
							log.info("sell price: " + unitPrice + " is not current optimal: " + buyAndSellPrice.snd);
							hasTried = max;
							break;
						}
					}
				}
			}
			hasTried ++;
		}
		if (hasTried == max) {
			log.info(placedOrder + " not settled, started cancel all the orders");
			List<String> canceledOrders = cancelAllOrders();
			log.info("finished cancel all the orders due to max try, cancelled orders: " + canceledOrders);
		}
		return placedOrder;
	}

	public class NewLimitOrder {
		private String product_id;
		private String side;
		private String type;
		private double price;
		private double size;
		private boolean post_only;

		public NewLimitOrder(String product_id, String side, double price, double size) {
			this.product_id = product_id;
			this.side = side;
			this.type = "limit";
			this.price = price;
			this.size = size;
			this.post_only = true; // true means not executed as market order
		}
	}
}
