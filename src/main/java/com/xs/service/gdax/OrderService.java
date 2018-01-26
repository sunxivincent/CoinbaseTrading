package com.xs.service.gdax;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.collect.ImmutableMap;
import com.xs.model.gdax.Fill;
import com.xs.model.gdax.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OrderService {
	private static final double MIN_BUY_UNIT = 0.001;
	private static final String ORDERS_ENDPOINT = "/orders";
	private static final String FILLS_ENDPOINT = "/fills";

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

	@Autowired
	private final GdaxTradingService gdaxTradingService;

	public OrderService(GdaxTradingService gdaxTradingService) {
		this.gdaxTradingService = gdaxTradingService;
	}

	public Order getOrder(String orderId) throws ExecutionException, RetryException {
		return GENERAL_RETRYER.call(() -> gdaxTradingService.get(ORDERS_ENDPOINT + "/" + orderId,new ParameterizedTypeReference<Order>(){}));
	}

	public List<Fill> getFill(String orderId) throws ExecutionException, RetryException {
		return FILL_RETRYER.call(() -> gdaxTradingService.getWithParams(
			FILLS_ENDPOINT + "/" + orderId + "?order_id={order_id}", new ParameterizedTypeReference<List<Fill>>(){}, ImmutableMap.of("order_id", orderId)));
	}

	public Order placeLimitOrder(String productId, String side, double size, double price) throws ExecutionException, RetryException {
		double limitSize = Math.max(MIN_BUY_UNIT, size);
		return GENERAL_RETRYER.call(() -> gdaxTradingService.post(ORDERS_ENDPOINT, new ParameterizedTypeReference<Order>(){},
			new NewLimitOrder(productId, side, price, limitSize)
		));
	}

	public List<String> cancelAllOrders() throws ExecutionException, RetryException {
		return CANCEL_RETRYER.call(() -> Arrays.asList(gdaxTradingService.delete(ORDERS_ENDPOINT, new ParameterizedTypeReference<String[]>(){})));
	}

	// productId example: BTC-USD
	public Order placeBuyWithTotalCash(String productId, double unitPrice, double targetCash) throws ExecutionException, RetryException {
		if (unitPrice == 0) throw new IllegalStateException(productId + " unitPrice is 0");
		Order placedOrder = placeLimitOrder(productId, "buy", targetCash / unitPrice, unitPrice);
		int max = 60;
		int hasTried = 0;

		while (hasTried < max) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.error("sleep error");
			}
			placedOrder = getOrder(placedOrder.getId());
			if (placedOrder.isSettled() && "done".equals(placedOrder.getStatus())) break;
			hasTried ++;
		}
		if (hasTried == max) {
			log.error("has tried: " + max + " but order: " + placedOrder.getId() + " not fulfilled");
			cancelAllOrders();
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
			this.post_only = true;
		}
	}
}
