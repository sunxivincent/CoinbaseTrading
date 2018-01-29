package com.xs.controller;

import com.sun.tools.javac.util.Pair;
import com.xs.model.gdax.Account;
import com.xs.model.gdax.Fill;
import com.xs.model.gdax.Order;
import com.xs.service.gdax.AccountService;
import com.xs.service.gdax.OrderService;
import com.xs.service.gdax.ProductService;
import com.xs.service.gdax.TradingService;
import com.xs.service.strategy.MMStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class TradingController {

	private final TradingService gdaxTradingService;
	private final AccountService accountService;
	private final OrderService orderService;
	private final ProductService productService;

	@Autowired
	public TradingController(TradingService gdaxTradingService,
													 AccountService accountService,
													 OrderService orderService,
													 ProductService productService) {
		this.gdaxTradingService = gdaxTradingService;
		this.accountService = accountService;
		this.orderService = orderService;
		this.productService = productService;
	}

	@RequestMapping(value = "/test")
	public String sanityTest() {
		StringBuilder sb = new StringBuilder();
		try {
			getServices().forEach(s -> sb.append(s.getClass().getCanonicalName()).append(s.sanityTest()).append("\n"));
		} catch (Exception e) {
			return "failed";
		}
		return sb.toString();
	}

	@RequestMapping(value = "/account/{currency}")
	@ResponseBody
	public Account account(@PathVariable String currency) {
		try {
			return accountService.getAccount(currency);
		} catch (Exception e) {
			log.error("failed to fetch account information", e);
		}
		return null;
	}

	@RequestMapping(value = "/accounts")
	@ResponseBody
	public List<Account> accounts() {
		try {
			return accountService.getAccounts().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
		} catch (Exception e) {
			log.error("failed to fetch account information", e);
		}
		return null;
	}

	// used when stop is not doing well....
	@RequestMapping(value = "/stop")
	public String stop() {
		StringBuilder sb = new StringBuilder();
		try {
			List<String> orders = orderService.cancelAllOrders();
			sb.append("order cancelled").append(orders);
		} catch (Exception e) {
			return "failed";
		}
		return sb.toString();
	}

	@RequestMapping(value = "/fills")
	@ResponseBody
	public List<Fill> fills() {
		try {
			return orderService.getFills();
		} catch (Exception e) {
			log.error("failed to fetch fills information", e);
		}
		return null;
	}

	@RequestMapping(value = "/fills/{orderId}")
	@ResponseBody
	public List<Fill> getFillByOrderId(@PathVariable String orderId) {
		try {
			return orderService.getFillsByOrderId(orderId);
		} catch (Exception e) {
			log.error("failed to fetch fills information", e);
		}
		return null;
	}

	@RequestMapping(value = "/order/{orderId}")
	@ResponseBody
	public Order order(@PathVariable String orderId) {
		try {
			return orderService.getOrder(orderId);
		} catch (Exception e) {
			log.error("failed to fetch order: " + orderId, e);
		}
		return null;
	}

	@RequestMapping(value = "/best")
	public String best() {
		StringBuilder sb = new StringBuilder();
		try {
			Pair<Double, Double> pair = productService.getBestLimitBuySellPricePair("BTC-USD");
			sb.append(pair.fst).append(" ").append(pair.snd);
		} catch (Exception e) {
			return "failed";
		}
		return sb.toString();
	}

	@RequestMapping(value = "/mm/{strategy}")
	public void makeMoney(@PathVariable String strategy) {
		if ("average_mover".equals(strategy)) {
			gdaxTradingService.makeMoney(MMStrategy.AVERAGE_MOVER);
		} else {
			throw new IllegalArgumentException("strategy: " + strategy + " not supported");
		}
	}

	@RequestMapping(value = "/mm/stop")
	public void makeMoneyStop() {
		gdaxTradingService.stop();
	}

	private List<TradingService> getServices() {
		return Arrays.asList(gdaxTradingService);
	}
}
