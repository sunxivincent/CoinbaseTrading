package com.xs.service.gdax;

import com.xs.service.strategy.MMStrategy;
import org.springframework.context.Lifecycle;
import org.springframework.stereotype.Component;

@Component
public interface TradingService extends Lifecycle {
	String sanityTest();

	double makeMoney(MMStrategy strategy);
}
