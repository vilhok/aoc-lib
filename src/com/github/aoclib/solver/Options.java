package com.github.aoclib.solver;

public class Options {

	/*
	 * Skip solving the first, in case there is a solution
	 */
	public boolean skipFirstIfDone = false;

	/*
	 * Skip solving the second, in case there is a solution
	 */
	public boolean skipSecondIfDone = false;

	/*
	 * Disable submits completely
	 */
	public boolean noSubmit = false;

	/*
	 * If a test fails, skip rest of the tests.
	 */
	public boolean stopAtFirstFailedTest = false;

	/*
	 * Which
	 */
	public int[] testsToSkip = new int[0];

	public boolean runTests = true;

//	public boolean warnNoTests = true;

//	public boolean checkFrontPage = true;

	/*
	 * Skip solved 
	 */
	public boolean skipSolved = false;
	
	public boolean preLoadInput = true;

}
