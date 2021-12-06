package com.github.aoclib.utils;

/**
 * Why type "," when you can type "Delimiter.COMMA"? <br>
 * <br>
 * All the basic delimiters and some more regex-ey ones. Most method should also
 * take bare strings for delimiters, if you insist.
 *
 */
public enum Delimiter {
	NONE(""), SPACE(" "), COMMA(","), COLON(":"), SEMICOLON(";"), TAB("\n"), NEWLINE("\n"), WHITESPACE("\\s+");

	public final String delimiter;

	private Delimiter(String delimiter) {
		this.delimiter = delimiter;
	}
}
