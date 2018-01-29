package com.xs.service.gdax;

import com.xs.service.strategy.MMStrategy;
import com.xs.service.strategy.StrategyEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class GdaxTradingService implements TradingService {
	private final String testUrl;
	private final RestTemplate restTemplate;
	private final StrategyEngine strategyEngine;

	@Autowired
	public GdaxTradingService (@Value("${gdax.api.testUrl}") String testUrl,
																		RestTemplate restTemplate,
																		StrategyEngine strategyEngine) {
		this.testUrl = testUrl;
		this.restTemplate = restTemplate;
		this.strategyEngine = strategyEngine;
	}
	@Override
	public String sanityTest() {
		ResponseEntity<String> response
			= restTemplate.getForEntity(testUrl, String.class);
		if (!response.getStatusCode().is2xxSuccessful()) {
			throw new IllegalArgumentException("response is not 2xx" + " response code: " + response.getStatusCode());
		}
		return response.getBody();
	}

	@Override
	public void makeMoney(MMStrategy strategy) {
		strategyEngine.start(strategy);
	}

	@Override
	public void stop() {
		strategyEngine.stop();
	}
}
