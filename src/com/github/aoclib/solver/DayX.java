package com.github.aoclib.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.github.aoclib.api.InputParser;

/*
 * interface for all days
 */
public abstract class DayX {

	public static final String NOT_SOLVED = "NOT_SOLVED";

	private InputParser input;
	private Parameters p;

	/**
	 * Represents a single test case with an input and expected solution
	 *
	 */
	public record Test(String testInput, Object expectedSolution) {

	}

	/**
	 * Tests for part 1
	 * 
	 * Add tests to your solution as following:
	 * 
	 * {@code new Test(multiLineStringInput,solutionString)}
	 * 
	 * These tests will run before your actual input. If any test fails, the actual
	 * data is not used.
	 * 
	 * @param tests a list where tests should be inserted..
	 */
	protected void insertTestsPart1(List<Test> tests) {

	}

	/**
	 * Tests for part 2
	 * 
	 * Add tests to your solution as following:
	 * 
	 * {@code new Test(multiLineStringInput,solutionString)}
	 * 
	 * These tests will run before your actual input. If any test fails, the actual
	 * data is not used.
	 * 
	 * @param tests a list where tests should be inserted..
	 */
	protected void insertTestsPart2(List<Test> tests) {

	}

	/**
	 * Implement these to submit your solutions
	 */
	protected abstract Object firstPart(InputParser ip);

	protected abstract Object secondPart(InputParser ip);

	public Solution solveFirstPart() {
		long time = System.nanoTime();
		Object s = firstPart(input);
		long runtime = System.nanoTime() - time;
		String solution = s == null ? "null" : s.toString();
		return new Solution(p.getYear(), p.getDay(), Part.FIRST, solution, runtime);
	}

	public Solution solveSecondPart() {
		long time = System.nanoTime();
		Object s = secondPart(input);
		long runtime = System.nanoTime() - time;
		String solution = s == null ? "null" : s.toString();
		return new Solution(p.getYear(), p.getDay(), Part.SECOND, solution, runtime);
	}

	public void setup(Parameters p, InputParser input) {
		this.p = p;
		this.input = input;
	}

	/**
	 * Return true in either of following cases: <br>
	 * -No tests are given. <br>
	 * -All tests pass
	 * 
	 * @return true if tests pass
	 */
	public boolean testFirstPart(int[] skiptests) {
		ArrayList<Test> tests = new ArrayList<>();
		insertTestsPart1(tests);
		if (tests.size() > 0)
			System.out.println("Testing part 1:");
		return performTests(tests, this::firstPart);
	}

	/**
	 * Return true in either of following cases: <br>
	 * -No tests are given. <br>
	 * -All tests pass
	 * 
	 * @return true if tests pass
	 */
	public boolean testSecondPart(int[] skiptests) {
		ArrayList<Test> tests = new ArrayList<>();
		insertTestsPart2(tests);
		if (tests.size() > 0)
			System.out.println("Testing part 2:");
		return performTests(tests, this::secondPart);
	}

	private boolean performTests(List<Test> tests, Function<InputParser, Object> test) {
		int notSolvedCnt = 0;
		int index = 0;
		boolean allPassed = true;
		for (Test tc : tests) {
			InputParser ip = new InputParser(tc.testInput);
			TestSolution result = new TestSolution(test.apply(ip).toString());
			if (result.solution.equals(NOT_SOLVED)) {
				notSolvedCnt++;
			} else if (!result.solution.equals(tc.expectedSolution.toString())) {
				System.out.println("\tTest " + (index + 1) + " failed. Result: " + result.solution + " Expected: "
						+ tc.expectedSolution);
				allPassed = false;
			} else {
				System.out.println("\tTest " + (index + 1) + " passed.");
			}
			index++;
		}
		if (!allPassed) {
			return allPassed;
		}

		if (notSolvedCnt > 0) {
			System.err.println(">>Warning: " + notSolvedCnt + "/" + tests.size() + " tests returned NOT_SOLVED");
			System.err.println(">>As the main input likely returns NOT_SOLVED, this counts as tests being passed.");
			System.err.println(">>Proceeding to attempt with main input...\n");
		}

		return true;
	}
}
