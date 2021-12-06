package com.github.aoclib.solver;

public class PartSolveData {
	@Override
	public String toString() {
		return "PartSolveData [solution=" + solution + ", correct=" + correct + ", statusMsg=" + statusMsg + "]";
	}
	Solution solution;
	boolean correct;
	String statusMsg;

}
