package com.xs.model.gdax;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
	String id;

	double size;

	double price; // the final unit price of product when deal is settled

	String product_id;

	String side;

	String stp;

	String type;

	String time_in_force;

	boolean post_only;

	String created_at;

	double fill_fees;

	double filled_size;

	double executed_value; // the total amount money used to buy the placed order

	String status;

	boolean settled;
}
