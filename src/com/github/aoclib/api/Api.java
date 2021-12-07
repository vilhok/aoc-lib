package com.github.aoclib.api;

import java.io.IOException;
import java.sql.SQLException;

import com.github.aoclib.solver.Solution;

public abstract class Api {

	public static Api API;

	/**
	 * Offers a solution to the AOC website.
	 * 
	 * @param username
	 * @param sol
	 * @return
	 * @throws IOException
	 */
	public abstract SubmitStatus offerSolution(String username, Solution sol) throws IOException;

	/**
	 * Downloads the whole task page. Can be used to parse existing solutions.
	 * 
	 * @param cookie
	 * @param year
	 * @param day
	 * @return
	 */
	public abstract AOCResponse checkTaskPage(String username, int year, int day) throws IOException;

	/**
	 * Checks the events page to validate existing solutions
	 * 
	 * @param cookie
	 * @param year
	 * @param day
	 * @return
	 */
	public abstract AOCResponse checkEventPage(String username) throws IOException;

	/**
	 * Checks the page of a year to validate existing solutions
	 * 
	 * @param cookie
	 * @param year
	 * @param day
	 * @return
	 */
	public abstract AOCResponse checkYearPage(String username, int year) throws IOException;

	/**
	 * Attempts to download AOC input for a given day.
	 * 
	 * @param username
	 * @param year
	 * @param day
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public abstract String downloadInput(String username, int year, int day) throws IOException, SQLException;

	public abstract void fetchSolution(String username, int year, int day) throws IOException, SQLException;

}
