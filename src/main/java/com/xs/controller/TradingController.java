package com.xs.controller;

import com.xs.service.TradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class TradingController {

	private final TradingService gdaxTradingService;

	@Value("${gdax.api.baseUrl}")
	private String prop1;

	@Autowired
	public TradingController(TradingService gdaxTradingService) {
		this.gdaxTradingService = gdaxTradingService;
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

	private List<TradingService> getServices() {
		return Arrays.asList(gdaxTradingService);
	}
}
