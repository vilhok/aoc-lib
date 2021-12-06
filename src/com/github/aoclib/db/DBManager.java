package com.github.aoclib.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.github.aoclib.api.AOCApi.AOCRequestType;
import com.github.aoclib.solver.Part;
import com.github.aoclib.solver.Solution;

public class DBManager {
	static Connection c;

	public static void setFile(String filename) {
		String url = "jdbc:sqlite:" + filename;
		try {
			c = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.err.println("DB connection error:" + e.getMessage());
			System.err.println("");
		}
	}

	/**
	 * Insert the solution to the correct database table depending on the
	 * correctness of the solution
	 * 
	 * @param user
	 * @param s
	 * @param correct
	 * @throws SQLException
	 */
	public static synchronized void insertSolution(String user, Solution s, boolean correct) throws SQLException {
		String solutionsql = "INSERT INTO solutions(user,year,day,part,solution) VALUES(?,?,?,?,?)";
		String wrongsolution = "INSERT INTO wrongsolutions(user,year,day,part,solution) VALUES(?,?,?,?,?)";
		String sql = correct ? solutionsql : wrongsolution;

		try (PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, user);
			ps.setInt(2, s.year);
			ps.setInt(3, s.day);
			ps.setInt(4, s.part.intvalue);
			ps.setString(5, s.solution);
			ps.execute();
		}
	}

	public static synchronized void storeInput(String user, int year, int day, String data) throws SQLException {

		String solutionsql = "INSERT into userinput(user,year,day,input) VALUES(?,?,?,?)";

		try (PreparedStatement ps = c.prepareStatement(solutionsql)) {
			ps.setString(1, user);
			ps.setInt(2, year);
			ps.setInt(3, day);
			ps.setString(4, data);
			ps.execute();
		}
	}

	/**
	 * Read the input from the database
	 * 
	 * @param user
	 * @param year
	 * @param day
	 * @return
	 * @throws SQLException
	 */
	public static synchronized Optional<String> getInput(String user, int year, int day) throws SQLException {
		String datasql = "SELECT input FROM userinput WHERE user=? AND year=? AND day=?";
		try (PreparedStatement ps = c.prepareStatement(datasql)) {

			ps.setString(1, user);
			ps.setInt(2, year);
			ps.setInt(3, day);
			ResultSet rs = ps.executeQuery();

			rs.next();
			if (!rs.isAfterLast()) {
				return Optional.of(rs.getString(1));
			}
			return Optional.empty();

		}
	}

	/**
	 * Query the database whether or not the solution for given year/day exists.
	 * 
	 * @param user
	 * @param year
	 * @param day
	 * @param p
	 * @return
	 * @throws SQLException
	 */
	public static synchronized Solution getSolution(String user, int year, int day, Part p) throws SQLException {
		String sq = "SELECT solutions.solution FROM solutions WHERE user=? AND year=? AND day =? AND part=?";

		try (PreparedStatement ps = c.prepareStatement(sq)) {
			ps.setString(1, user);
			ps.setInt(2, year);
			ps.setInt(3, day);
			ps.setInt(4, p.intvalue);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return new Solution(year, day, p, rs.getString(1), -1);
			}
			return Solution.NULL_SOLUTION;
		}

	}

	/**
	 * Check if this solution exists in the database
	 * 
	 * @param user the username that is associated to the input
	 * @param s    the solution that will be searched from db
	 * @return true if the value is found in database.
	 * @throws SQLException
	 */
	public static synchronized boolean wrongSolution(String user, Solution s) throws SQLException {
		String sq = "SELECT wrongsolutions.solution FROM wrongsolutions WHERE user=? AND year=? AND day=? AND part=? ";
		try (PreparedStatement ps = c.prepareStatement(sq)) {
			ps.setString(1, user);
			ps.setInt(2, s.year);
			ps.setInt(3, s.day);
			ps.setInt(4, s.part.intvalue);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {

				if (s.solution.equals(rs.getString(1))) {
					return true;
				}
			}
			return false;
		}

	}

	/**
	 * Read a specific cookie for a user from the db
	 * 
	 * @param username
	 * @return
	 * @throws SQLException
	 */
	public static synchronized String getCookie(String username) throws SQLException {
		String sq = "SELECT cookies.cookie FROM cookies WHERE username=?";

		try (PreparedStatement ps = c.prepareStatement(sq)) {
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			rs.next();
			if (!rs.isAfterLast()) {
				return rs.getString(1);
			}
			throw new NoSuchElementException("User " + username + " does not exist in the database.");
		}
	}


	public static synchronized void insertNextTime(long earliestCallClockMs, AOCRequestType type) throws SQLException {
		String sq = "INSERT OR REPLACE INTO apidelay(next_time,name) VALUES(?,?);";
		try (PreparedStatement ps = c.prepareStatement(sq)) {
			ps.setLong(1, earliestCallClockMs);
			ps.setString(2, type.name());
			ps.execute();
		}
	}

	public static synchronized long getNextApiTime(AOCRequestType type) throws SQLException {
		String sq = "SELECT next_time FROM apidelay WHERE name=?;";
		try (PreparedStatement ps = c.prepareStatement(sq)) {
			ps.setString(1, type.name());
			ResultSet rs = ps.executeQuery();
			rs.next();
			if (!rs.isAfterLast()) {
				return rs.getLong(1);
			} else {
				return 0;
			}
		}

	}

	public static void addUser(String username, String cookie) {

	}

	public static void updateUser(String username, String cookie) {

	}

	/**
	 * Returns true if the username is stored in the db.
	 * 
	 * @param username
	 * @return
	 */
	public static boolean hasUser(String username) {
		return false;
	}

	public static SolutionData getSolution(int year, int day, String username) throws SQLException {
		Solution first = getSolution(username, year, day, Part.FIRST);
		Solution second = getSolution(username, year, day, Part.SECOND);
		return new SolutionData(first, second);
	}
}
