package com.xs.model.gdax;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
	String sequence;

	List<List<Double>> bids; // [ price, size, num-orders ],

	List<List<Double>> asks;
}
