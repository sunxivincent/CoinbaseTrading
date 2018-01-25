package com.xs.model.gdax;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PriceElement {
	String id;
	double currency_unit;
	double unit_price;
	long timestamp;
}
