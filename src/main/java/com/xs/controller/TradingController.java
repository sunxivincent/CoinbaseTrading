package com.xs.controller;

import com.sun.tools.javac.util.Pair;
import com.xs.model.gdax.Order;
import com.xs.service.gdax.AccountService;
import com.xs.service.gdax.OrderService;
import com.xs.service.gdax.ProductService;
import com.xs.service.gdax.TradingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
public class TradingController {

	@Autowired
	TradingService gdaxTradingService;

	@Autowired
	AccountService accountService;

	@Autowired
	OrderService orderService;

	@Autowired
	ProductService productService;

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

	@RequestMapping(value = "/account")
	public String account() {
		StringBuilder sb = new StringBuilder();
		try {
			accountService.getAccounts().forEach((key, value) -> sb.append(key).append(value).append("\n"));
		} catch (Exception e) {
			return "failed";
		}
		return sb.toString();
	}

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

	@RequestMapping(value = "/order")
	public String order() {
		StringBuilder sb = new StringBuilder();
		try {
			Order order = orderService.placeBuyWithTotalCash("BTC-USD", 8000, 100);
			sb.append(order).append("\n").append("order placed").append("\n");
		} catch (Exception e) {
			return "failed";
		}
		return sb.toString();
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

	private List<TradingService> getServices() {
		return Arrays.asList(gdaxTradingService);
	}
}
