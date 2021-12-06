package com.github.aoclib.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class DayGenerator {
	/**
	 * The default solver for a specific day looks like this.
	 */
	static String dayTemplate = """
			package solutions.year%s;

			import api.InputParser;
			import solver.DayX;

			public class Year%dDay%s extends DayX {

				@Override
				public Object firstPart(InputParser input) {
					return NOT_SOLVED;
				}

				@Override
				public Object secondPart(InputParser input) {
					return NOT_SOLVED;
				}


			}
			""";

	static String tests = """
			
			
			@Override
			protected void insertTestsPart1(List<TestCase> tests) {

			}
			
			@Override
			protected void insertTestsPart2(List<TestCase> tests) {

			}
			""";

	/**
	 * Generates a single boilerplate source code file.
	 * 
	 * @param year
	 * @param day
	 * @throws IOException
	 */
	public static void generate(int year, int day) throws IOException {
		String dir = "year" + year;
		File f = Paths.get("src/solutions", dir).toFile();
		if (!f.exists()) {
			f.mkdirs();
		}
		String dayStr = "" + day;
		if (dayStr.length() == 1) {
			dayStr = "0" + dayStr;
		}
		String cname = "Year" + year + "Day" + dayStr;
		dayTemplate = String.format(dayTemplate, year, year, dayStr);
		File target = Paths.get("src", "solutions", dir, cname + ".java").toFile();
		if (target.exists()) {
			System.err.println("Did not generate file" + target.getAbsolutePath());
			System.err.println("Target file already exists");
			return;
		}
		FileWriter fw = new FileWriter(target);

		fw.write(dayTemplate);
		fw.close();

	}

}
