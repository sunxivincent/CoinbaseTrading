package com.xs.Util;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.management.RuntimeErrorException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class SignatureFactory {

	public static Mac SHARED_MAC;

	static {
		try {
			SHARED_MAC = Mac.getInstance("HmacSHA256");
		} catch (NoSuchAlgorithmException nsaEx) {
			nsaEx.printStackTrace();
		}
	}

	public static String createGdaxSignature(String secretKey, String requestPath, String method, String body, String timestamp) {
		try {
			String prehash = timestamp + method.toUpperCase() + requestPath + body;
			byte[] secretDecoded = Base64.getDecoder().decode(secretKey);
			SecretKeySpec keyspec = new SecretKeySpec(secretDecoded, "HmacSHA256");
			Mac sha256 = (Mac) SHARED_MAC.clone();
			sha256.init(keyspec);
			return Base64.getEncoder().encodeToString(sha256.doFinal(prehash.getBytes()));
		} catch (CloneNotSupportedException | InvalidKeyException e) {
			e.printStackTrace();
			throw new RuntimeErrorException(new Error("Cannot set up authentication headers."));
		}
	}

}
