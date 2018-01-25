package com.xs.model.gdax;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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

@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {
	@JsonProperty("id")
	String id;

	@JsonProperty("currency")
	String currency;

	@JsonProperty("balance")
	double balance;

	@JsonProperty("available")
	double available;

	@JsonProperty("hold")
	double hold;

	@JsonProperty("profile_id")
	String profile_id;

	@java.beans.ConstructorProperties({"id", "currency", "balance", "available", "hold", "profile_id"})
	public Account(String id, String currency, double balance, double available, double hold, String profile_id) {
		this.id = id;
		this.currency = currency;
		this.balance = balance;
		this.available = available;
		this.hold = hold;
		this.profile_id = profile_id;
	}

	public String getId() {
		return this.id;
	}

	public String getCurrency() {
		return this.currency;
	}

	public double getBalance() {
		return this.balance;
	}

	public double getAvailable() {
		return this.available;
	}

	public double getHold() {
		return this.hold;
	}

	public String getProfile_id() {
		return this.profile_id;
	}

	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Account)) return false;
		final Account other = (Account) o;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$currency = this.getCurrency();
		final Object other$currency = other.getCurrency();
		if (this$currency == null ? other$currency != null : !this$currency.equals(other$currency)) return false;
		final Object this$balance = this.getBalance();
		final Object other$balance = other.getBalance();
		if (this$balance == null ? other$balance != null : !this$balance.equals(other$balance)) return false;
		final Object this$available = this.getAvailable();
		final Object other$available = other.getAvailable();
		if (this$available == null ? other$available != null : !this$available.equals(other$available)) return false;
		final Object this$hold = this.getHold();
		final Object other$hold = other.getHold();
		if (this$hold == null ? other$hold != null : !this$hold.equals(other$hold)) return false;
		final Object this$profile_id = this.getProfile_id();
		final Object other$profile_id = other.getProfile_id();
		if (this$profile_id == null ? other$profile_id != null : !this$profile_id.equals(other$profile_id)) return false;
		return true;
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $currency = this.getCurrency();
		result = result * PRIME + ($currency == null ? 43 : $currency.hashCode());
		final Object $balance = this.getBalance();
		result = result * PRIME + ($balance == null ? 43 : $balance.hashCode());
		final Object $available = this.getAvailable();
		result = result * PRIME + ($available == null ? 43 : $available.hashCode());
		final Object $hold = this.getHold();
		result = result * PRIME + ($hold == null ? 43 : $hold.hashCode());
		final Object $profile_id = this.getProfile_id();
		result = result * PRIME + ($profile_id == null ? 43 : $profile_id.hashCode());
		return result;
	}

	public String toString() {
		return "Account(id=" + this.getId() + ", currency=" + this.getCurrency() + ", balance=" + this.getBalance() + ", available=" + this.getAvailable() + ", hold=" + this.getHold() + ", profile_id=" + this.getProfile_id() + ")";
	}
}
