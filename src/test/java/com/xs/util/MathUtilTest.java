package com.xs.util;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MathUtilTest {
	@DataProvider
	public Object[][] getNumber() {
		return new Object[][]{
			{0.012346, 0.01235},
			{0.012345, 0.01235},
			{0.012344, 0.01234},
			{0.01234, 0.01234},
			{0.01237, 0.01237},
			{0.0123, 0.01230},
			{1234560.0123, 1234560.01230},
		};
	}

	@Test(dataProvider = "getNumber")
	public void testRoundDoubleTo5Decimal(double num, double expected) {
		Assert.assertEquals(MathUtil.roundDoubleTo5Decimal(num), expected);
	}
}