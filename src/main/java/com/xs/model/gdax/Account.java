package com.xs.model.gdax;

import lombok.Data;

/**
 *     {
 "id": "e316cb9a-0808-4fd7-8914-97829c1925de",
 "currency": "USD",
 "balance": "80.2301373066930000",
 "available": "79.2266348066930000",
 "hold": "1.0035025000000000",
 "profile_id": "75da88c5-05bf-4f54-bc85-5c775bd68254"
 }
 */

@Data
public class Account {
	String id;

	String currency;

	double balance;

	double available;

	double hold;

	String profile_id;
}
