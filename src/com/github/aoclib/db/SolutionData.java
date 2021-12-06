package com.github.aoclib.db;

import com.github.aoclib.solver.Solution;

public class SolutionData {
	public final Solution firstSolution;
	public final Solution secondSolution;

	public SolutionData(Solution first, Solution second) {
		super();
		this.firstSolution = first;
		this.secondSolution = second;
	}

	public boolean bothSolved() {
		return firstSolved() && secondSolved();
	}

	public boolean firstSolved() {
		return firstSolution != Solution.NULL_SOLUTION;
	}

	public boolean secondSolved() {
		return secondSolution != Solution.NULL_SOLUTION;
	}
}
