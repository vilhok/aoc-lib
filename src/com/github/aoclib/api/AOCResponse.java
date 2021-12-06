package com.github.aoclib.api;

/**
 * Represents a response given by https://adventofcode.com
 */
public class AOCResponse {
	private final String contents;

	public AOCResponse(String contents) {
		this.contents = contents;
	}

	/**
	 * The returned body as a string
	 * @return
	 */
	public String raw() {
		return contents;
	}

	/**
	 * Parses and returns the meaningful bit from the result.
	 * @return
	 */
	public SubmitStatus parseStatus() {
		return SubmitStatus.forResult(contents);
	}
	
}
