package com.github.aoclib.solver;

import com.github.aoclib.api.InputParser;

public interface DayProvider {

	public static final NopDay NOP = new NopDay();

	public DayX get(int year, int day);

	
	public static class NopDay extends DayX {

		private NopDay() {

		}
		@Override
		protected Object firstPart(InputParser ip) {
			return NOT_SOLVED;
		}

		@Override
		protected Object secondPart(InputParser ip) {
			return NOT_SOLVED;
		}

	}
}
