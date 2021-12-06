package com.github.aoclib.solver;

import java.lang.reflect.InvocationTargetException;

public class ReflectiveDayProvider implements DayProvider {

	private static String root = "solutions.year%d";

	public static void setRoot(String s) {
		root = s;
	}

	@Override
	public DayX get(int year, int day) {
		String cp = String.format(root + "." + "Year%dDay%02d", year, year, day);
		try {

			@SuppressWarnings("unchecked")
			Class<? extends DayX> c = (Class<? extends DayX>) Class.forName(cp);

			return c.getConstructor().newInstance();

		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			System.err.println("To solve year " + year + " day " + day + ", following class is needed:");
			System.err.println("\t"+cp);
			System.err.println("Make sure to extend class DayX");
			if (day < 10) {
				System.err.println("Single digit days must contain the zero as well: 0" + day);
			}
			System.err.println("\nAutogenerate all days for current year with args: tools --generate-year [year]");

		}
		return DayProvider.NOP;

	}

}
