package com.github.aoclib.solver;

public class ResultData {

	boolean firstSkipped;
	boolean secondSkipped;
	PartSolveData firstPart;
	PartSolveData secondPart;
	@Override
	public String toString() {
		return "ResultData [firstSkipped=" + firstSkipped + ", secondSkipped=" + secondSkipped + ", firstPart="
				+ firstPart + ", secondPart=" + secondPart + "]";
	}
	
	
}
