package com.github.aoclib.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Instruction{

	private List<String> values;
	public String line;

	public Instruction(String line, Delimiter d){
		this.line = line;
		values = Arrays.stream(line.split(d.delimiter)).collect(Collectors.toList());
	}

	public String name(){
		return values.get(0);
	}

	public String arg(int i){
		if(values.size() < i){
			return "";
		}
		return values.get(i + 1);
	}
}
