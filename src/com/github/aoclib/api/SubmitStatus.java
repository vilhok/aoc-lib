package com.github.aoclib.api;

public enum SubmitStatus {

	CORRECT, INCORRECT, ALREADY_SOLVED, UNDEFINED, TOO_RECENT;

	private static final String alreadySolved = "You don't seem to be solving the right level";
	private static final String correct = "That's the right answer!";
	private static final String incorrect = "That's not the right answer";
	private static final String tooRecent = "You gave an answer too recently";

	public static SubmitStatus forResult(String response) {

		if (response.contains(alreadySolved)) {
			return ALREADY_SOLVED;
		} else if (response.contains(correct)) {
			return CORRECT;
		} else if (response.contains(incorrect)) {
			return INCORRECT;
		} else if (response.contains(tooRecent)) {
			return TOO_RECENT;
		}
		return UNDEFINED;

	}
}
