package com.github.aoclib.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.aoclib.utils.Delimiter;
import com.github.aoclib.utils.Instruction;

public class InputParser {

	private final List<String> input;

	public InputParser(List<String> input) {
		this.input = input;
	}

	public int firstLineLength() {
		return input.get(0).length();
	}

	/**
	 *
	 * Returns true if all the input lines have the same length. Lines lengths are
	 * compared by {@link String#length()}.
	 * 
	 * @return Are all the input lines equally long
	 */
	public boolean allSameLength() {
		int len = input.size();
		for (String s : input) {
			if (s.length() != len)
				return false;
		}
		return true;
	}

	public InputParser(String inputString) {
		this.input = new ArrayList<>(Arrays.asList(inputString.split("\n")));

	}

	/**
	 * Join all the lines to a single string, without doing any splitting.
	 * 
	 * @return A single string containing all the rows
	 */
	public String string() {
		return joinLinesToString(Delimiter.NONE);

	}

	/**
	 * Returns the single and supposedly only line as an int.
	 * 
	 * @return the first line of the input as an int.
	 */
	public int integer() {
		return Integer.parseInt(input.get(0));

	}

	/**
	 * Combines all lines and returns then as char array
	 * 
	 * @return
	 */
	public char[] chars() {
		return joinLinesToString(Delimiter.NONE).toCharArray();
	}

	/**
	 * Return lines as unprocessed strings
	 * 
	 * @return All rows of the input as a List
	 */
	public List<String> getLines() {
		return new ArrayList<>(input);
	}

	public <T> List<T> getLines(Function<String, T> mapper) {

		return input.stream().map(mapper).collect(Collectors.toList());
	}

	/**
	 * Return groups of lines as separate lists.
	 * 
	 * A group is defined by multiple lines, separated by newlines.
	 * 
	 * @return Lists of Strings for each group, contained in a List
	 */
	public List<List<String>> getGroups() {
		List<List<String>> list = new ArrayList<>();
		ArrayList<String> current = new ArrayList<>();
		for (String line : input) {
			if (line.isBlank()) {
				if (current.size() > 0) {
					list.add(current);
				}
				current = new ArrayList<>();
			} else {
				current.add(line);
			}
		}
		// adds the final group if there was no blank line as the last line
		if (list.get(list.size() - 1) != current) {
			list.add(current);
		}
		return list;
	}

	/**
	 * Split each row by Delimiter and return then in a string array of strings.
	 * 
	 * @param valueSeparator
	 * @return
	 */
	public String[] joinLineValuesToArray(Delimiter valueSeparator) {

		ArrayList<String> str = new ArrayList<>();
		for (String s : input) {
			String[] a = s.split(valueSeparator.delimiter);
			for (String ss : a) {
				str.add(ss.trim());
			}
		}
		return str.toArray(new String[0]);
	}

	/**
	 * Returns the data as a single string. All lines are combined by using a
	 * specific delimiter.
	 * 
	 * @param delimiter
	 * @return
	 */
	public String joinLinesToString(Delimiter delimiter) {
		return input.stream().collect(Collectors.joining(delimiter.delimiter));
	}

	/**
	 * Returns whitespace separated integers
	 * 
	 * @return
	 */
	public int[] asSingleIntArray() {
		return asSingleIntArray(Delimiter.WHITESPACE);
	}

	/**
	 * Returns a single integer array from single line of input by using a custom
	 * delimiter
	 * 
	 * @param rowValueDelimiter
	 * @return
	 */
	public int[] asSingleIntArray(Delimiter rowValueDelimiter) {
		List<String> lines = input;
		return lines.stream() //
				.filter(s -> !s.matches("\\s*")) //
				.flatMap(s -> Arrays.stream(s.split(rowValueDelimiter.delimiter))) //
				.mapToInt(e -> Integer.parseInt(e)) //
				.toArray();
	}

	public long[] asSingleLongArray(Delimiter splitRegex) {
		List<String> lines = input;
		return lines.stream() //
				.filter(s -> !s.matches("\\s*")) //
				.flatMap(s -> Arrays.stream(s.split(splitRegex.delimiter))) //
				.mapToLong(e -> Long.parseLong(e)) //
				.toArray();
	}

	/**
	 * Returns each split line as a list of items of type T. Requires a custom
	 * mapper from String to T
	 * 
	 * @param <T>
	 * @param sep
	 * @param valueMapper
	 * @return
	 */
	public <T> List<List<T>> linesAsLists(String sep, Function<String, T> valueMapper) {

		return input //
				.stream()//
				.map(line -> line.trim())//
				.filter(line -> !line.isBlank())//
				.map(i -> Arrays.stream(i.split(sep))//
						.map(valueMapper)//
						.collect(Collectors.toList()))
				.collect(Collectors.toList());
	}

	public <T> List<List<T>> linesAsLists(Delimiter sep, Function<String, T> valueMapper) {
		return linesAsLists(sep.delimiter, valueMapper);
	}

	/**
	 * Returns a matrix with each line split to char array.
	 * 
	 * @return char[][] where that contains each row as char[]
	 */
	public char[][] charMatrix() {
		return input.stream()//
				.map(String::toCharArray)//
				.collect(Collectors.toList())//
				.toArray(char[][]::new);

	}

	/**
	 * Returns a matrix as an integer array. Assume the input consists of strings
	 * that have only ascii numbers
	 * 
	 * 
	 */
	public int[][] intMatrix() {
		int[][] in = new int[input.size()][];
		int index = 0;
		for (String line : input) {
			in[index++] = line.chars().map(i -> i - 48).toArray();
		}

		return in;
	}

	/**
	 * Returns each line as separated values.
	 * 
	 * @param rowValueDelimiter the delimiter for values on each row.
	 * @return A list containing a list for eachs rows values.
	 */
	public List<List<String>> linesAsLists(Delimiter rowValueDelimiter) {
		return linesAsLists(rowValueDelimiter.delimiter, Object::toString);
	}

	public List<List<String>> linesAsLists(String rowValueDelimiter) {
		return linesAsLists(rowValueDelimiter, Object::toString);
	}

	/**
	 * Returns the input as 2019 intcode program
	 * 
	 * @return a long[] array that represent the intcode program
	 */
	public long[] intCodeProgram() {
		return asSingleLongArray(Delimiter.COMMA);
	}

	/**
	 * Get as year 2017 instructions
	 * 
	 * @param instructionDelimiter
	 * @return
	 */
	public List<Instruction> as2017Instruction(Delimiter instructionDelimiter) {
		return input.stream().map(e -> new Instruction(e, instructionDelimiter)).collect(Collectors.toList());
	}

}
