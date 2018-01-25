package com.xs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Bean
	public Gson gson() {
		return new GsonBuilder().registerTypeAdapter(Double.class,
			(JsonSerializer<Double>) (originalValue, typeOf, context) -> {
			BigDecimal bigValue = BigDecimal.valueOf(originalValue);
			return new JsonPrimitive(bigValue.toPlainString());
		}).create();
	}

}
