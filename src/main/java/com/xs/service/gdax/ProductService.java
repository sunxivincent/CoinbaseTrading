package com.xs.service.gdax;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.sun.tools.javac.util.Pair;
import com.xs.model.gdax.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ProductService {
	private static final String PRODUCT_ENDPOINT = "/products";
	private static final Retryer<Product> GENERAL_RETRYER = RetryerBuilder.<Product>newBuilder()
		.retryIfRuntimeException()
		.withWaitStrategy(WaitStrategies.exponentialWait(2000, TimeUnit.MILLISECONDS))
		.withStopStrategy(StopStrategies.stopAfterAttempt(3))
		.build();

	private final GdaxTradingServiceTemplate gdaxTradingServiceTemplate;

	@Autowired
	public ProductService(GdaxTradingServiceTemplate gdaxTradingServiceTemplate) {
		this.gdaxTradingServiceTemplate = gdaxTradingServiceTemplate;
	}

	public Product getBestBidAsk(String productId) throws ExecutionException, RetryException {
		return GENERAL_RETRYER.call(() -> gdaxTradingServiceTemplate.get(
			PRODUCT_ENDPOINT + "/" + productId + "/book?level=1",
			new ParameterizedTypeReference<Product>(){}));
	}

	public Pair<Double, Double> getBestLimitBuySellPricePair(String productId) throws ExecutionException, RetryException {
		Product product = getBestBidAsk(productId);
		if (product == null || CollectionUtils.isEmpty(product.getAsks()) || CollectionUtils.isEmpty(product.getBids())) {
			log.error("failed to fetch product " + productId + " best bid and ask");
		}
		return Pair.of(product.getBids().get(0).get(0), product.getAsks().get(0).get(0));
	}
}
