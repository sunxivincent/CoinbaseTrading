package com.xs.util;

import lombok.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Component
public class SignatureFactory {

	public static Mac SHARED_MAC;
	private static List<String> ALLOWED_METHOD = Arrays.asList(
		HttpMethod.DELETE.toString(),
		HttpMethod.GET.toString(),
		HttpMethod.PUT.toString(),
		HttpMethod.POST.toString()
	);


	static {
		try {
			SHARED_MAC = Mac.getInstance("HmacSHA256");
		} catch (NoSuchAlgorithmException nsaEx) {
			nsaEx.printStackTrace();
		}
	}

	// https://api.gdax.com/accounts, requestPath => accounts
	public static String createGdaxSignature(@NonNull String secretKey,
																					 @NonNull String requestPath,
																					 @NonNull String method,
																					 @NonNull String body, // post body if any
																					 @NonNull String timestamp) {
		method = method.toUpperCase();
		if (!ALLOWED_METHOD.contains(method)) {
			throw new IllegalStateException("method: " + method +  "not valid");
		}

		try {
			byte[] secretDecoded = Base64.getDecoder().decode(secretKey);
			SecretKeySpec keyspec = new SecretKeySpec(secretDecoded, "HmacSHA256");
			Mac sha256 = (Mac) SHARED_MAC.clone();
			sha256.init(keyspec);
			String message = timestamp + method.toUpperCase() + requestPath + body;
			return Base64.getEncoder().encodeToString(sha256.doFinal(message.getBytes()));
		} catch (CloneNotSupportedException | InvalidKeyException e) {
			e.printStackTrace();
			throw new IllegalStateException("Cannot set up authentication headers.");
		}
	}

}
