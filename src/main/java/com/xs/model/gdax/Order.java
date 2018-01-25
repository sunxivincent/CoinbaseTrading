package com.xs.model.gdax;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
	String id;

	@JsonProperty("size")
	double size;

	@JsonProperty("price")
	double price;

	@JsonProperty("product_id")
	String product_id;

	@JsonProperty("side")
	String side;

	@JsonProperty("stp")
	String stp;

	@JsonProperty("type")
	String type;

	@JsonProperty("time_in_force")
	String time_in_force;

	@JsonProperty("post_only")
	boolean post_only;

	@JsonProperty("created_at")
	String created_at;

	@JsonProperty("fill_fees")
	String fill_fees;

	@JsonProperty("filled_size")
	String filled_size;

	@JsonProperty("executed_value")
	String executed_value;

	@JsonProperty("status")
	String status;

	@JsonProperty("settled")
	boolean settled;

	@java.beans.ConstructorProperties({"id", "size", "price", "product_id", "side", "stp", "type", "time_in_force", "post_only", "created_at", "fill_fees", "filled_size", "executed_value", "status", "settled"})
	public Order(String id, double size, double price, String product_id, String side, String stp, String type, String time_in_force, boolean post_only, String created_at, String fill_fees, String filled_size, String executed_value, String status, boolean settled) {
		this.id = id;
		this.size = size;
		this.price = price;
		this.product_id = product_id;
		this.side = side;
		this.stp = stp;
		this.type = type;
		this.time_in_force = time_in_force;
		this.post_only = post_only;
		this.created_at = created_at;
		this.fill_fees = fill_fees;
		this.filled_size = filled_size;
		this.executed_value = executed_value;
		this.status = status;
		this.settled = settled;
	}

	public String getId() {
		return this.id;
	}

	public double getSize() {
		return this.size;
	}

	public double getPrice() {
		return this.price;
	}

	public String getProduct_id() {
		return this.product_id;
	}

	public String getSide() {
		return this.side;
	}

	public String getStp() {
		return this.stp;
	}

	public String getType() {
		return this.type;
	}

	public String getTime_in_force() {
		return this.time_in_force;
	}

	public boolean isPost_only() {
		return this.post_only;
	}

	public String getCreated_at() {
		return this.created_at;
	}

	public String getFill_fees() {
		return this.fill_fees;
	}

	public String getFilled_size() {
		return this.filled_size;
	}

	public String getExecuted_value() {
		return this.executed_value;
	}

	public String getStatus() {
		return this.status;
	}

	public boolean isSettled() {
		return this.settled;
	}

	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Order)) return false;
		final Order other = (Order) o;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		if (Double.compare(this.getSize(), other.getSize()) != 0) return false;
		if (Double.compare(this.getPrice(), other.getPrice()) != 0) return false;
		final Object this$product_id = this.getProduct_id();
		final Object other$product_id = other.getProduct_id();
		if (this$product_id == null ? other$product_id != null : !this$product_id.equals(other$product_id)) return false;
		final Object this$side = this.getSide();
		final Object other$side = other.getSide();
		if (this$side == null ? other$side != null : !this$side.equals(other$side)) return false;
		final Object this$stp = this.getStp();
		final Object other$stp = other.getStp();
		if (this$stp == null ? other$stp != null : !this$stp.equals(other$stp)) return false;
		final Object this$type = this.getType();
		final Object other$type = other.getType();
		if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
		final Object this$time_in_force = this.getTime_in_force();
		final Object other$time_in_force = other.getTime_in_force();
		if (this$time_in_force == null ? other$time_in_force != null : !this$time_in_force.equals(other$time_in_force))
			return false;
		if (this.isPost_only() != other.isPost_only()) return false;
		final Object this$created_at = this.getCreated_at();
		final Object other$created_at = other.getCreated_at();
		if (this$created_at == null ? other$created_at != null : !this$created_at.equals(other$created_at)) return false;
		final Object this$fill_fees = this.getFill_fees();
		final Object other$fill_fees = other.getFill_fees();
		if (this$fill_fees == null ? other$fill_fees != null : !this$fill_fees.equals(other$fill_fees)) return false;
		final Object this$filled_size = this.getFilled_size();
		final Object other$filled_size = other.getFilled_size();
		if (this$filled_size == null ? other$filled_size != null : !this$filled_size.equals(other$filled_size))
			return false;
		final Object this$executed_value = this.getExecuted_value();
		final Object other$executed_value = other.getExecuted_value();
		if (this$executed_value == null ? other$executed_value != null : !this$executed_value.equals(other$executed_value))
			return false;
		final Object this$status = this.getStatus();
		final Object other$status = other.getStatus();
		if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
		if (this.isSettled() != other.isSettled()) return false;
		return true;
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final long $size = Double.doubleToLongBits(this.getSize());
		result = result * PRIME + (int) ($size >>> 32 ^ $size);
		final long $price = Double.doubleToLongBits(this.getPrice());
		result = result * PRIME + (int) ($price >>> 32 ^ $price);
		final Object $product_id = this.getProduct_id();
		result = result * PRIME + ($product_id == null ? 43 : $product_id.hashCode());
		final Object $side = this.getSide();
		result = result * PRIME + ($side == null ? 43 : $side.hashCode());
		final Object $stp = this.getStp();
		result = result * PRIME + ($stp == null ? 43 : $stp.hashCode());
		final Object $type = this.getType();
		result = result * PRIME + ($type == null ? 43 : $type.hashCode());
		final Object $time_in_force = this.getTime_in_force();
		result = result * PRIME + ($time_in_force == null ? 43 : $time_in_force.hashCode());
		result = result * PRIME + (this.isPost_only() ? 79 : 97);
		final Object $created_at = this.getCreated_at();
		result = result * PRIME + ($created_at == null ? 43 : $created_at.hashCode());
		final Object $fill_fees = this.getFill_fees();
		result = result * PRIME + ($fill_fees == null ? 43 : $fill_fees.hashCode());
		final Object $filled_size = this.getFilled_size();
		result = result * PRIME + ($filled_size == null ? 43 : $filled_size.hashCode());
		final Object $executed_value = this.getExecuted_value();
		result = result * PRIME + ($executed_value == null ? 43 : $executed_value.hashCode());
		final Object $status = this.getStatus();
		result = result * PRIME + ($status == null ? 43 : $status.hashCode());
		result = result * PRIME + (this.isSettled() ? 79 : 97);
		return result;
	}

	public String toString() {
		return "Order(id=" + this.getId() + ", size=" + this.getSize() + ", price=" + this.getPrice() + ", product_id=" + this.getProduct_id() + ", side=" + this.getSide() + ", stp=" + this.getStp() + ", type=" + this.getType() + ", time_in_force=" + this.getTime_in_force() + ", post_only=" + this.isPost_only() + ", created_at=" + this.getCreated_at() + ", fill_fees=" + this.getFill_fees() + ", filled_size=" + this.getFilled_size() + ", executed_value=" + this.getExecuted_value() + ", status=" + this.getStatus() + ", settled=" + this.isSettled() + ")";
	}
}
