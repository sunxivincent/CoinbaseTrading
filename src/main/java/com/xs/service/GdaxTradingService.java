package com.xs.service;

import com.google.gson.Gson;
import com.xs.Util.SignatureFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Slf4j
@Component
public class GdaxTradingService implements TradingService {
	String publicKey;
	String passphrase;
	String baseUrl;
	String testUrl;
	SignatureFactory signatureFactory;
	RestTemplate restTemplate;
	Gson gson;
	boolean enable;

	@Autowired
	public GdaxTradingService(@Value("${gdax.key}") String publicKey,
														@Value("${gdax.passphrase}") String passphrase,
														@Value("${gdax.api.baseUrl}") String baseUrl,
														@Value("${gdax.api.testUrl}") String testUrl,
														SignatureFactory signatureFactory,
														RestTemplate restTemplate,
														Gson gson,
														@Value("${gdax.enable}") String enable) {
		this.publicKey = publicKey;
		this.passphrase = passphrase;
		this.baseUrl = baseUrl;
		this.testUrl = testUrl;
		this.signatureFactory = signatureFactory;
		this.restTemplate = restTemplate;
		this.enable = "true".equals(enable);
	}

	@Override
	public String sanityTest() {
		ResponseEntity<String> response
			= restTemplate.getForEntity(testUrl, String.class);
		if (!response.getStatusCode().is2xxSuccessful()) {
			throw new IllegalArgumentException("response is not 2xx");
		}
		return response.getBody();
	}

	private HttpHeaders createHttpHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("accept", "application/json");
		headers.add("content-type", "application/json");
		if (enable) {
			String timestamp = Instant.now().getEpochSecond() + "";
			headers.add("CB-ACCESS-KEY", publicKey);
//			headers.add("CB-ACCESS-SIGN", signatureFactory.createGdaxSignature(resource, method, jsonBody, timestamp));
			headers.add("CB-ACCESS-TIMESTAMP", timestamp);
			headers.add("CB-ACCESS-PASSPHRASE", passphrase);
		}
		return headers;
	}
}
