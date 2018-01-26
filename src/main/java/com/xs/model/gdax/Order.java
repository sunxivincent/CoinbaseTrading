package com.xs.model.gdax;

public class Order {
	String id;

	double size;

	double price;

	String product_id;

	String side;

	String stp;

	String type;

	String time_in_force;

	boolean post_only;

	String created_at;

	String fill_fees;

	String filled_size;

	String executed_value;

	String status;

	boolean settled;

	public Order() {
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}

	public void setSide(String side) {
		this.side = side;
	}

	public void setStp(String stp) {
		this.stp = stp;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setTime_in_force(String time_in_force) {
		this.time_in_force = time_in_force;
	}

	public void setPost_only(boolean post_only) {
		this.post_only = post_only;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public void setFill_fees(String fill_fees) {
		this.fill_fees = fill_fees;
	}

	public void setFilled_size(String filled_size) {
		this.filled_size = filled_size;
	}

	public void setExecuted_value(String executed_value) {
		this.executed_value = executed_value;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setSettled(boolean settled) {
		this.settled = settled;
	}

	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Order)) return false;
		final Order other = (Order) o;
		if (!other.canEqual((Object) this)) return false;
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

	protected boolean canEqual(Object other) {
		return other instanceof Order;
	}

	public String toString() {
		return "Order(id=" + this.getId() + ", size=" + this.getSize() + ", price=" + this.getPrice() + ", product_id=" + this.getProduct_id() + ", side=" + this.getSide() + ", stp=" + this.getStp() + ", type=" + this.getType() + ", time_in_force=" + this.getTime_in_force() + ", post_only=" + this.isPost_only() + ", created_at=" + this.getCreated_at() + ", fill_fees=" + this.getFill_fees() + ", filled_size=" + this.getFilled_size() + ", executed_value=" + this.getExecuted_value() + ", status=" + this.getStatus() + ", settled=" + this.isSettled() + ")";
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
}
