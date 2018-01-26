package com.xs.model.gdax;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PriceElement {
	String id;
	double currencyUnit;
	double unitPrice;
	long timestamp;
}
