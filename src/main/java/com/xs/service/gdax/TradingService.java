package com.xs.service.gdax;

import com.xs.service.strategy.MMStrategy;
import org.springframework.stereotype.Component;

@Component
public interface TradingService {
	String sanityTest();

	void makeMoney(MMStrategy strategy);

	void stop();
}
