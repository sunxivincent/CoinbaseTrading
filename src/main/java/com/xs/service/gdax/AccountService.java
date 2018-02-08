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

	private final GdaxTradingServiceTemplate gdaxTradingServiceTemplate;

	public static final String ACCOUNTS_ENDPOINT = "/accounts";

	@Autowired
	public AccountService(GdaxTradingServiceTemplate gdaxTradingServiceTemplate) throws ExecutionException, RetryException {
		this.gdaxTradingServiceTemplate = gdaxTradingServiceTemplate;
	}

	public Map<String, Account> getAccounts() {
		try {
			List<Account> accounts = RETRYER.call(() -> gdaxTradingServiceTemplate.getAsList(ACCOUNTS_ENDPOINT, new ParameterizedTypeReference<Account[]>(){}));
			return accounts.stream().collect(Collectors.toMap(Account::getCurrency, a -> a));
		} catch (ExecutionException | RetryException ex) {
			log.error("failed to get accounts", ex);
			return null;
		}
	}

	public Account getAccount(String currency) {
		Map<String, Account> accounts = getAccounts();
		return accounts == null ? null : accounts.get(currency);
	}
}
