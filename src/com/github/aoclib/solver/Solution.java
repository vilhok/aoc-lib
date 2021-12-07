package com.github.aoclib.solver;

import java.util.Set;

/**
 * 
 * Contains a solution for given year/day/part
 *
 */
public sealed class Solution permits TestSolution {
	/**
	 * These are not considered valid solutions are are never submitted, nor saved
	 * in the database.
	 */
	private static final Set<String> badSolutions = Set.of("0", "-1", "false", "true", "");

	/**
	 * A solution that does not exist.
	 */
	public static final Solution NULL_SOLUTION = new Solution(-1, -1, Part.FIRST, null, -1);

	public final int year;
	public final int day;
	public final Part part;
	public final String solution;
	public final long solutiontimeNS;

	public Solution(int year, int day, Part part, String solution, long solutiontimeNS) {
		super();
		this.year = year;
		this.day = day;
		this.part = part;
		this.solution = solution;
		this.solutiontimeNS = solutiontimeNS;
		if (solution == null) {
			solution = "null";
		}
	}

	public boolean isNull() {
		return solution == NULL_SOLUTION.solution || badSolutions.contains(solution);

	}

}
