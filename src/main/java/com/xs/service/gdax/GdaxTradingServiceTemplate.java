package com.xs.service.gdax;

import com.google.gson.Gson;
import com.xs.util.SignatureFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GdaxTradingServiceTemplate {
	private final String apikey;
	private final String passphrase;
	private final String secret;
	private final String baseUrl;
	private final SignatureFactory signatureFactory;
	private final RestTemplate restTemplate;
	private final Gson gson;
	private final boolean enable;

	@Autowired
	public GdaxTradingServiceTemplate(@Value("${gdax.key}") String apikey,
																		@Value("${gdax.passphrase}") String passphrase,
																		@Value("${gdax.secret}") String secret,
																		@Value("${gdax.api.baseUrl}") String baseUrl,
																		SignatureFactory signatureFactory,
																		RestTemplate restTemplate,
																		Gson gson,
																		@Value("${gdax.enable}") String enable) {
		this.apikey = apikey;
		this.passphrase = passphrase;
		this.secret = secret;
		this.baseUrl = baseUrl;
		this.signatureFactory = signatureFactory;
		this.restTemplate = restTemplate;
		this.gson = gson;
		this.enable = "true".equals(enable);
	}

	public <T> T get(String requestPath, ParameterizedTypeReference<T> responseType) {
		try {
			ResponseEntity<T> responseEntity = restTemplate.exchange(
				baseUrl + requestPath,
				HttpMethod.GET,
				createHttpEntity(requestPath, HttpMethod.GET.toString(), ""),
				responseType);

			return responseEntity.getBody();
		} catch (Exception ex) {
			log.error("GET request Failed for " + requestPath, ex);
			throw ex;
		}
	}

	// example "http://my-rest-url.org/rest/account/{account}?name={name}"
	// TODO: might be issue if using requestPathPattern to create createHttpEntity
	// not sure why it works originally in getBestBidAsk but not in getFill
	public <T, R> T getWithParams(String requestPathPattern, ParameterizedTypeReference<T> responseType, Map<String, R> params) {
		try {
				ResponseEntity<T> responseEntity = restTemplate.exchange(
				baseUrl + requestPathPattern,
				HttpMethod.GET,
				createHttpEntity(requestPathPattern, HttpMethod.GET.toString(), ""),
				responseType,
				params);
			return responseEntity.getBody();
		} catch (Exception ex) {
			log.error("GET request Failed for " + requestPathPattern, ex);
			throw ex;
		}
	}


	public <T> List<T> getAsList(String requestPath, ParameterizedTypeReference<T[]> responseType) {
		T[] result = get(requestPath, responseType);
		return result == null ? null : Arrays.asList(result);
	}

	public <T, R> T post(String requestPath, ParameterizedTypeReference<T> responseType, R jsonObj) {
		String jsonBody = gson.toJson(jsonObj);
		try {
			ResponseEntity<T> response = restTemplate.exchange(baseUrl + requestPath,
				HttpMethod.POST,
				createHttpEntity(requestPath, HttpMethod.POST.toString(), jsonBody),
				responseType);
			return response.getBody();
		} catch (Exception ex) {
			log.error("POST request Failed for " + requestPath, ex);
			throw ex;
		}
	}

	public <T> T delete(String requestPath, ParameterizedTypeReference<T> responseType) {
		try {
			ResponseEntity<T> response = restTemplate.exchange(baseUrl + requestPath,
				HttpMethod.DELETE,
				createHttpEntity(requestPath, HttpMethod.DELETE.toString(), ""),
				responseType);
			return response.getBody();
		} catch (Exception ex) {
			log.error("DELETE request Failed for " + requestPath, ex);
			throw ex;
		}
	}

	private HttpEntity createHttpEntity(String requestPath, String method, String jsonBody) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("accept", "application/json");
		headers.add("content-type", "application/json");
		if (enable) {
			String timestamp = Instant.now().getEpochSecond() + "";
			headers.add("CB-ACCESS-TIMESTAMP", timestamp);
			headers.add("CB-ACCESS-KEY", apikey);
			headers.add("CB-ACCESS-PASSPHRASE", passphrase);
			headers.add("CB-ACCESS-SIGN", signatureFactory.createGdaxSignature(secret, requestPath, method, jsonBody, timestamp));
		}
		return new HttpEntity<>(jsonBody, headers);
	}
}
