package com.xs.util;

import java.text.DecimalFormat;

public class MathUtil {
	private static final DecimalFormat DF = new DecimalFormat("#.#####");
	public static double roundDoubleTo5Decimal (double number) {
		return Double.parseDouble(DF.format(number));
	}
}
