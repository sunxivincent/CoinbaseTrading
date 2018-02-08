package com.xs.model.gdax;

import com.sun.tools.javac.util.Pair;
import com.xs.service.strategy.AverageMoverTask;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import static org.testng.Assert.assertEquals;

public class PriceElementTest {
	public static final List<PriceElement> ELEMENTS = Arrays.asList(
		new PriceElement("1", 123, 80, 0L),
		new PriceElement("2", 123, 80, 0L),
		new PriceElement("3", 123, 90, 0L),
		new PriceElement("4", 123, 90, 0L),
		new PriceElement("5", 123, 100, 0L),
		new PriceElement("6", 123, 100, 0L)
	);
	private static final double NOT_FOUND = -1;

	@DataProvider
	public Object[][] priceAndElements() {
		return new Object[][] {
			{1, 70, NOT_FOUND, 80},
			{1, 80, NOT_FOUND, 90},
			{1, 81, 80, 90},
			{1, 89, 80, 90},
			{1, 100, 90, NOT_FOUND},
			{5, 95, 90, 100},
			{55, 85, NOT_FOUND, NOT_FOUND},
		};
	}

	@Test(dataProvider = "priceAndElements")
	public void testFindLowerHigherPriceElement(double delta,
																							double price,
																							double expectedLower,
																							double expectedHigher) {
		TreeSet<PriceElement> priceElements = new TreeSet<>(new PriceElement.PriceElementComparator());
		priceElements.addAll(ELEMENTS);
		Pair<PriceElement, PriceElement> pair = PriceElement.getLowerAndHigher(price, priceElements, delta);
		assertEquals(pair.fst == null ? NOT_FOUND : pair.fst.unitCostBasis , expectedLower);
		assertEquals(pair.snd == null ? NOT_FOUND : pair.snd.unitCostBasis , expectedHigher);
	}

	@Test
	public void testInitEnqueue() {
		TreeSet<PriceElement> priceElements = new TreeSet<>(new PriceElement.PriceElementComparator());
		AverageMoverTask.initEnqueue(priceElements, 6, 0.01, 10000);
		assertEquals(6, priceElements.size());
	}

	@Test
	public void testModify() {
		TreeSet<PriceElement> priceElements = new TreeSet<>(new PriceElement.PriceElementComparator());
		priceElements.add(new PriceElement("1", 1, 1000, 12345));
		priceElements.add(new PriceElement("2", 2, 1001, 12346));
		priceElements.add(new PriceElement("3", 3, 1002, 12347));
		PriceElement ceiling = priceElements.ceiling(new PriceElement("", 1, 1000, 12345));
		System.out.println(priceElements);
		if (ceiling != null) {
			ceiling.setUnitCostBasis(ceiling.getUnitCostBasis() - 100);
		}
		System.out.println(priceElements);
		priceElements.remove(ceiling);
		assertEquals(priceElements.size(), 2);
	}
}