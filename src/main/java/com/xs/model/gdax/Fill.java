package com.xs.model.gdax;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class Fill {
	String trade_id;
	String product_id;
	double price;
	double size;
	String order_id;
	String created_at;
	String liquidity;
	double fee;
	boolean settled;
	String side;
}
