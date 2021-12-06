package com.github.aoclib.utils;

public class TimeUtils {

	/**
	 * Formats the nanosecond count in a more human readable format.
	 * 
	 * @param runtime in nanoseconds
	 * @return time represented as string.
	 */
	public static String getTimeString(long runtime) {
		String result = (runtime / 1_000_000) + " ms";
		if (runtime < 1000) {
			result += " (" + runtime + " ns)";
		} else if (runtime < 1_000_000) {
			result += " (" + runtime / 1000 + "µs)";
		}
		return result;
	}
}
