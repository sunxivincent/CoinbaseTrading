package com.xs.service.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class StrategyEngine {
	private final static ScheduledExecutorService MM_POOL = Executors.newSingleThreadScheduledExecutor();
	private final AtomicBoolean running = new AtomicBoolean(false);

	@Autowired
	private AverageMoverTask averageMoverTask;

	public void start(MMStrategy strategy) {
		if (running.compareAndSet(false, true)) {
			if (strategy == MMStrategy.AVERAGE_MOVER) {
				log.info("starting AVERAGE_MOVER strategy");
				MM_POOL.scheduleWithFixedDelay(averageMoverTask, 0, 1, TimeUnit.SECONDS);
			}
		}
	}

	public void stop() {
		if (isRunning()) {
			log.info("stopping AVERAGE_MOVER strategy");
			averageMoverTask.stop();
		}
		MM_POOL.shutdownNow();
	}

	public boolean isRunning() {
		return running.get();
	}
}
