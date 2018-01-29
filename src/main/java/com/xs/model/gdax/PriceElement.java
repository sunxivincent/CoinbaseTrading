package com.xs.model.gdax;

import com.sun.tools.javac.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.TreeSet;

@AllArgsConstructor
@Getter
@Setter
public class PriceElement {
	@NonNull String id;
	double unit;
	double unitCostBasis;
	long timestamp;

	// find the element with strictly lower and higher price in the set. Also note after scanning the price for
	// all elements, if not finding significant change (this is to avoid adding excessive same price element) we treate
	// it as a negligible case
	public static Pair<PriceElement, PriceElement> getLowerAndHigher(double targetPrice,
																																	 TreeSet<PriceElement> priceElements, double delta) {
		if (CollectionUtils.isEmpty(priceElements)) return null;
		PriceElement lower = priceElements.floor(new PriceElement("", 0, targetPrice-delta, System.currentTimeMillis()));
		PriceElement higher = priceElements.ceiling(new PriceElement("", 0, targetPrice+delta, System.currentTimeMillis()));
		return new Pair<>(lower, higher);
	}

	public static class PriceElementComparator implements Comparator<PriceElement> {
		@Override
		public int compare(PriceElement p1, PriceElement p2) {
			return
				p1.getUnitCostBasis() != p2.getUnitCostBasis() ?
					Double.compare(p1.getUnitCostBasis(), p2.getUnitCostBasis()) :
					p1.getUnit() != p2.getUnit() ?
						Double.compare(p1.getUnit(), p2.getUnit()) :
						p1.getTimestamp() != p2.getTimestamp() ?
							Double.compare(p1.getTimestamp(), p2.getTimestamp()) :
							p1.getId().compareTo(p2.getId());
		}
	}
}
