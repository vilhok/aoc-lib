package com.github.aoclib.api;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.aoclib.db.DBManager;

/*
 * Class for reading input for puzzle
 */
public class Input {

	private int year = 0;
	private int day = 0;
	private String username;

	/**
	 * During the solving of a specific puzzle, the input is loaded only once and
	 * stored into this variable. The part2 does not reload the input, but takes a
	 * copy from this variable.
	 * 
	 * 
	 */
	private List<String> cache;

	private Input(int year, int day, String username) {
		this.year = year;
		this.day = day;
		this.username = username;
	}

	/**
	 * Attempts to load the input from 3 locations, in the following order:<br>
	 * 0. Copy of the cached object <br>
	 * 1. Database <br>
	 * 2. AOC website
	 */
	private List<String> loadLines() {
		if (cache != null) {
			return new ArrayList<String>(cache);
		}

		try {
			Optional<String> in = DBManager.getInput(username, year, day);
			String s;
			if (!in.isPresent()) {
				System.out.println("Querying input from AOC-website:" + username + " " + year + " " + day);
				s = AOCApi.API.downloadInput(username, year, day);
			} else {
				s = in.get();
			}
			return List.of(s.split("\n"));
		} catch (SQLException | IOException e) {
			System.err.println("Input file was not available:");
			System.err.println("Make sure user"+username+" exists and has cookie set.");
			System.err.println("rung with args: user -h for help");
			e.printStackTrace();
			return List.of("Input was not available.", e.getMessage());

		}
	}

	/**
	 * Initialize input reader for a given day
	 * 
	 * @param day
	 * @param year
	 * @param username
	 * @return
	 */
	public static Input get(int year, int day, String username) {
		return new Input(year, day, username);
	}

	/**
	 * Get this input as a parser that can be us
	 * 
	 * @return an {@code InputParser} for parsing this input.
	 */
	public InputParser parser() {
		if (cache == null) {
			cache = loadLines();
		}
		return new InputParser(cache);
	}

	/**
	 * Preload this input to the cache, so that it is not done when the parser is
	 * eventually requested.
	 */
//	public void preload() {
//		if (cache == null)
//			cache = loadLines();
//	}
}
