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
	protected record Test(String testInput, String expectedSolution) {
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
		String solution = firstPart(input).toString();
		long runtime = System.nanoTime() - time;
		return new Solution(p.getYear(), p.getDay(), Part.FIRST, solution, runtime);

	}

	public Solution solveSecondPart() {
		long time = System.nanoTime();
		String solution = secondPart(input).toString();
		long runtime = System.nanoTime() - time;
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
		return performTests(tests, this::secondPart);
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
		return performTests(tests, this::secondPart);
	}

	private boolean performTests(List<Test> tests, Function<InputParser, Object> test) {
		int notSolvedCnt = 0;
		int index = 0;
		boolean allPassed=true;
		for (Test tc : tests) {
			InputParser ip = new InputParser(tc.testInput);
			TestSolution result = new TestSolution(test.apply(ip).toString());
			if (result.solution.equals(NOT_SOLVED)) {
				notSolvedCnt++;
			} else if (!result.solution.equals(tc.expectedSolution)) {
				System.out.println("Test in index " + index + "failed. Result: " + result.solution + "Expected:"
						+ tc.expectedSolution);
				allPassed=false;
			}
			index++;
		}
		if(!allPassed) {
			return allPassed;
		}
		
		if (notSolvedCnt > 0) {
			System.out.println("Warning: " + notSolvedCnt + "/" + tests.size() + " tests returned NOT_SOLVED");
			System.out.println("As the main input likely returns NOT_SOLVED, this counts as tests begin passed.");
			System.out.println("Proceeding to attempt with main input...");
		}

		return true;
	}
}
