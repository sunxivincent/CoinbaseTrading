package com.xs.service.gdax;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.xs.model.gdax.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AccountService {
	private static final Retryer<List<Account>> RETRYER = RetryerBuilder.<List<Account>>newBuilder()
		.retryIfRuntimeException()
		.withWaitStrategy(WaitStrategies.fixedWait(300,TimeUnit.MILLISECONDS))
		.withStopStrategy(StopStrategies.stopAfterAttempt(5))
		.build();

	private final GdaxTradingService gdaxTradingService;

	public static final String ACCOUNTS_ENDPOINT = "/accounts";

	@Autowired
	public AccountService(GdaxTradingService gdaxTradingService) throws ExecutionException, RetryException {
		this.gdaxTradingService = gdaxTradingService;
	}

	public Map<String, Account> getAccounts() throws ExecutionException, RetryException {
		List<Account> accounts = RETRYER.call(() -> gdaxTradingService.getAsList(ACCOUNTS_ENDPOINT, new ParameterizedTypeReference<Account[]>(){}));
		if (CollectionUtils.isEmpty(accounts)) {
			throw new IllegalArgumentException("currencyToAccount is null");
		}
		return accounts.stream().collect(Collectors.toMap(Account::getCurrency, a -> a));
	}
}
