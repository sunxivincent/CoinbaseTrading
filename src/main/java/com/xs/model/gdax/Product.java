package com.xs.model.gdax;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
public class Product {
	String sequence;

	List<List<Double>> bids; // [ price, size, num-orders ],

	List<List<Double>> asks;
}
