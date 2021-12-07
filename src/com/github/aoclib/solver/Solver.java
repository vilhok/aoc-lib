package com.github.aoclib.solver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.github.aoclib.api.AOCApi;
import com.github.aoclib.api.Api;
import com.github.aoclib.api.Input;
import com.github.aoclib.api.SubmitStatus;
import com.github.aoclib.db.DBManager;
import com.github.aoclib.db.SolutionData;
import com.github.aoclib.utils.DayGenerator;
import com.github.aoclib.utils.TimeUtils;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.impl.choice.RangeArgumentChoice;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

public class Solver {

	int maxYear;

	static {
		Api.API = new AOCApi();
	}

	private static final String SOLVE = "solve";
	private static final String BENCHMARK = "benchmark";
	private static final String TOOLS = "tools";

	private DayProvider dayprovider;

	Namespace parsedArgs;

	public Solver(String[] args) {
		DBManager.nonNullCheck();
		this.dayprovider = new ReflectiveDayProvider();
		if (args.length == 0) {
			parseArgs(new String[] { "-h" });
		} else {
			parseArgs(args);
		}
	}

	private void parseArgs(String[] args) {
		ArgumentParser parser = ArgumentParsers.newFor("").build()//
				.description("Java library for Advent of code https://adventofcode.com/" + "\n\n" + "*".repeat(50)
						+ "\nIf this is the first time, add a user: \n\nuser -a [username] -c [cookie]\n\n"
						+ " The username is LOCAL username and has nothing to do with AoC login. You must first log in with a browser and pick the cookie. "
						+ "\n\nAfter you have stored a user, run:\n\n" + "solve -u [user] -d [day] -y [year]\n\n"
						+ "*".repeat(50) + "\n\nFor further info, run any of subcommand with argument -h");

		Subparsers subs = parser.addSubparsers().dest("mode");
		Subparser solver = subs.addParser(SOLVE)//
				.description("Solver mode is used to solve a puzzle for specific day.");
		solver.defaultHelp(true);
		subs.help("run [mode] -h for more information")
				.description("Pick one of the following modes to run for different functionalities").title("modes");

		ArgumentGroup puzzleSolve = solver.addArgumentGroup("puzzle selectors");
		puzzleSolve.addArgument("-u", "--user")
				.help("Local username. Database must have a cookie with this name. For help, run: user -h");

		LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC-5"));
		Argument dayArg = puzzleSolve.addArgument("-d", "--day")//
				.type(Integer.class)
				.help("Defaults to current day (UTC-5), if AoC is currently in progress. No default and required argument during other times.");

		RangeArgumentChoice<Integer> range = Arguments.range(1, 25);
		dayArg.choices(range);
		// if running current advent of code, defaults to current day.
		if (now.getMonth() == Month.DECEMBER && range.contains(now.getDayOfMonth())) {
			dayArg.setDefault(now.getDayOfMonth());
		} else {
			dayArg.required(true);
		}

		int defaultYear;
		if (now.getMonth() == Month.DECEMBER) {
			defaultYear = now.getYear();
		} else {
			defaultYear = now.getYear() - 1;
		}
		maxYear = defaultYear;

		puzzleSolve.addArgument("-y", "--year").setDefault(defaultYear)
				.help("Defaults to current year on December, and previous year before December.").type(Integer.class);

		puzzleSolve.addArgument("-p", "--part").choices(List.of("1", "2", "both")).setDefault("both").required(false)
				.help("Which puzzle part to solve");

		Subparser benchmark = subs.addParser(BENCHMARK);

		benchmark.addArgument("-u", "--user").required(true)
				.help("Local username. Database must have a cookie with this name. For help, run: user -h");

		int benchYear;
		if (now.getMonth() == Month.DECEMBER) {
			benchYear = now.getYear();
		} else {
			benchYear = now.getYear() - 1;
		}

		benchmark.addArgument("-y", "--year").setDefault(benchYear)
				.help("Defaults to current year on December, and previous year before December.").type(Integer.class)
				.setDefault(0);

		Subparser cache = subs.addParser("tools");
		MutuallyExclusiveGroup mg = cache.addMutuallyExclusiveGroup("Cache tools and code generation");

		mg.addArgument("-g", "--generate-year").type(Integer.class).metavar("YEAR")
				.help("Automatically generate the solver files for a specific year.");
		mg.addArgument("--fix-solution").nargs(5).metavar("[user]", "[day]", "[year]", "[part]", "[solution]").help(
				"Insert a correct solution to the database manually, for example if you have solved it on another computer. ");

		mg.addArgument("--delete-solution").nargs(4).metavar("[user]", "[day]", "[year]", "[part]").help(
				"If, for whatever reason, your database contains an invalid correct solution that should not be there, you may use this to delete it. This results in an error later, as your code tries to re-submit the solution. This is automatically fixed by fetching the correct solution from the AoC. You may alternatively use --fix-solution.");

//		mg.addArgument("--rebuild-cache").action(Arguments.storeTrue()).help(
//				"rebuilds the whole solution and answer cache. Please never do this, keep your database with you even if you switch computers. This takes a long time, it sends a requests to AoC website every 5 seconds not to put enormous load on the servers. As stated by AoC devs, you might get banned by misusing the page. I don't take any responsibility.");

		mg.required(true);

		Subparser user = subs.addParser("user");

		user.addArgument("-a", "--add").help(
				"Adds a local username. This can be anything you want. Under normal circumstances, you only ever add a single user.");
		user.addArgument("-c", "--cookie").help("The cookie to be stored in the local database");

		try {
			parsedArgs = parser.parseArgs(args);

		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(-1);
		}

	}

	/**
	 * Sets the {@link DatProvider} that provides puzzle executors.
	 * 
	 * @param dp
	 * @return
	 */
	public Solver daysFrom(DayProvider dp) {
		this.dayprovider = dp;
		return this;
	}

	/**
	 * 
	 * This method works in following way:<br>
	 * <ol>
	 * <li>Checks if the task is already solved, or if {@code Options.skipSolved} is
	 * false</li>
	 * <li>If task is to be solved, it will check if first part must be skipped if
	 * that is done already<br>
	 * <li>Proceed to solving. Attempt to solve the first task first and then the
	 * second.</li>
	 * <li>The second will be omitted if the first is incorrect.</li>
	 * 
	 * <li>Incorrect or correct solutions will be stored in database and submitted
	 * to adventofcode. Same incorrect solution will never be submitted twice.</li>
	 * </ol>
	 * NOTE: This method calls the api, which is severely rate-limited. If you are
	 * going to use this library, respect the built in timers. The AOC server is not
	 * to be misused. The user of this library accepts the consequences of misusing
	 * the server.
	 * 
	 * @param username
	 * @param year
	 * @param day
	 * @throws SQLException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private ResultData solve(String username, int year, int day, Options options)
			throws SQLException, IOException, InterruptedException {

		ResultData s = new ResultData();
		SolutionData oldData = DBManager.getSolution(year, day, username);

		System.out.println(oldData.firstSolved());
		if (options.skipSolved && oldData.bothSolved()) {
			System.out.println(day + "/" + year + " solved already. skipping..");
			return s;
		}

		DayX daySolver = dayprovider.get(year, day);
		if (daySolver == DayProvider.NOP) {
			System.out.println("Day implementation was not provided. skipping..");
			return s;
		}
		Input input = Input.get(year, day, username);

		// I would assume the day would know its own day/year though. Ugly.
		daySolver.setup(new Parameters(year, day), input.parser());

		boolean runSecondPart = false;
		// if solved are not skipped or first is not done
		if (oldData.firstSolved() && options.skipFirstIfDone) {
			s.firstSkipped = true;
			runSecondPart = true;
		} else {
			s.firstPart = solvePart(username, //
					options, //
					daySolver, //
					daySolver::testFirstPart, //
					daySolver::solveFirstPart, oldData.firstSolution.solution);
			if (s.firstPart.correct) {
				runSecondPart = true;
			}
		}

		if (runSecondPart) {
			if (oldData.secondSolved() && options.skipSecondIfDone) {
				s.secondSkipped = true;
			} else {
				s.secondPart = solvePart(username, //
						options, //
						daySolver, //
						daySolver::testSecondPart, //
						daySolver::solveSecondPart, oldData.secondSolution.solution);

			}
		}
		return s;
	}

	private PartSolveData solvePart(String username, Options options, DayX ctx, Function<int[], Boolean> tests,
			Supplier<Solution> solver, String knownCorrect) throws SQLException, IOException {
		PartSolveData psd = new PartSolveData();

		// perform tests
		if (options.runTests) {
			boolean testsPassed = tests.apply(options.testsToSkip);
			if (!testsPassed) {
				psd.statusMsg = "Tests failed.";
				return psd;
			} else if (ctx.onlyTest()) {
				psd.statusMsg = "Tests passed, but ONLY_TEST was set true. Wont run with the main input.";
				return psd;
			}
		}

		Solution solution = solver.get();
		if (solution.solution.equals(DayX.NOT_SOLVED)) {
			psd.statusMsg = "Solution not implemented!";
			return psd;
		}

		psd.solution = solution;

		if (knownWrongSolution(username, solution)) {
			psd.statusMsg = "This solution is known to be wrong.";
			return psd;
		}
		if (knownCorrect != null) {
			if (knownCorrect.equals(solution.solution)) {

				psd.correct = true;
				psd.statusMsg = "Solution (" + solution.solution + ") known to be correct";
			} else {
				psd.statusMsg = "There is a correct solution(" + knownCorrect + "), but this time solution was "
						+ solution.solution;
			}
		} else {
			if (solution.isNull()) {
				psd.statusMsg = "Solution is null or null-like: " + solution.solution + "\nRefusing to submit.";
			} else if (!options.noSubmit) {
				AOCVerify ao = verifyResultAtAOC(username, solution);
				psd.correct = ao.result == Result.CORRECT;
				psd.statusMsg = ao.msg;

			} else {
				psd.statusMsg = "No submits allowed (remove nosubmit-option from cli)";

			}

		}
		return psd;
	}

	private boolean knownWrongSolution(String username, Solution attempt) throws SQLException {
		return DBManager.wrongSolution(username, attempt);
	}

	static class AOCVerify {
		Result result;
		String msg;

		public AOCVerify(Result result, String msg) {
			super();
			this.result = result;
			this.msg = msg;
		}

		static AOCVerify correct(String msg) {
			return new AOCVerify(Result.CORRECT, msg);
		}

		static AOCVerify incorrect(String msg) {
			return new AOCVerify(Result.INCORRECT, msg);
		}

	}

	enum Result {
		CORRECT, INCORRECT,
	}

	/**
	 * 
	 * @param username     the username to submit to.
	 * @param usersolution The actual solution for the puzzle
	 * @return true if the result was correct.
	 * @throws SQLException
	 * @throws IOException
	 */
	private AOCVerify verifyResultAtAOC(String username, Solution usersolution) throws SQLException, IOException {
		SubmitStatus s = Api.API.offerSolution(username, usersolution);

		switch (s) {

		case CORRECT:
			DBManager.insertSolution(username, usersolution, true);
			if (usersolution.part == Part.FIRST)
				return AOCVerify.correct(usersolution.day + "/" + usersolution.year + " Correct! Earned a star: *");
			else
				return AOCVerify
						.correct(usersolution.day + "/" + usersolution.year + " Part 2 Correct! You have 2 stars: **");

		case ALREADY_SOLVED:
			// TODO: fecth the solution from the task page and isert to database.
			Api.API.fetchSolution(username, usersolution.year, usersolution.day);

			return AOCVerify.correct("This was already submitted. Solution was acquired from the AOC page.");
		case TOO_RECENT:
			// TODO: attempt to parse the delay from the page, though it should already be
			return AOCVerify.incorrect("Too recent answer!");
		case INCORRECT:
			DBManager.insertSolution(username, usersolution, false);

			return AOCVerify.incorrect("The solution is incorrect:" + usersolution.solution);

		case UNDEFINED:
			return AOCVerify.incorrect("Check the full response string");

		default:

			throw new RuntimeException("Result handling failed");
		}
	}

	private static void adduser(String string) throws IOException {
		List<String> file = Files.readAllLines(Paths.get(string))//
				.stream() //
				.map(line -> line.strip()) //
				.filter(line -> line.matches("\\s")) //
				.filter(line -> line.startsWith("#"))//
				.collect(Collectors.toList());//

		String username = file.get(0);
		String cookie = file.get(1);
		if (!cookie.matches("session=[a-f0-9]*")) {
			System.err.println("error: malformed cookie:");
			System.err.println(cookie);
			return;
		}
		if (DBManager.hasUser(username)) {
			DBManager.updateUser(username, cookie);
		} else {
			DBManager.addUser(username, cookie);
		}
	}

	private void doSolve() {
		String uname = parsedArgs.getString("user");
		int year = parsedArgs.getInt("year");
		int day = parsedArgs.getInt("day");
		System.out.println("Solving " + day + "/" + year + " for user " + uname);
		Options o = new Options();
//		o.skipFirstIfDone = true;
		o.preLoadInput = true;
		try {
			ResultData stats = solve(uname, year, day, o);
			if (stats.firstPart != null) {
				System.out.println("Part1: " + stats.firstPart.statusMsg);
				if (stats.firstPart.solution != null) {
					System.out.println("Time:" + TimeUtils.getTimeString(stats.firstPart.solution.solutiontimeNS));
				}
			}
			if (stats.secondPart != null) {
				System.out.println("Part2: " + stats.secondPart.statusMsg);
				if (stats.secondPart.solution != null) {
					System.out.println("Time:" + TimeUtils.getTimeString(stats.secondPart.solution.solutiontimeNS));
				}
			}
		} catch (SQLException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void doBenchmark() {
		String uname = parsedArgs.getString("user");
		int year = parsedArgs.getInt("year");
		if (year == 0) {
			for (int i = 2015; i < maxYear; i++) {

				benchmark(i, uname);
			}
		} else {
			benchmark(year, uname);
		}
	}

	private void benchmark(int year, String uname) {

		long start = System.nanoTime();
		long totalTime = 0;
		int unsolved = 0;
		Options o = new Options();
		o.noSubmit = true; // TODO: this is not honored!

		for (int i = 1; i <= 25; i++) {
			try {
				SolutionData sd = DBManager.getSolution(year, i, uname);
				if (sd.bothSolved()) {
					ResultData stats = solve(uname, year, i, new Options());
					if (stats.firstPart.solution != null) {
						long firstTime = stats.firstPart.solution.solutiontimeNS;
						System.out.print(i + "/" + year + ": Part1 " + TimeUtils.getTimeString(firstTime));
						totalTime += firstTime;
						if (stats.secondPart.solution != null) {
							long secondTime = stats.secondPart.solution.solutiontimeNS;
							System.out.println(", Part2 " + TimeUtils.getTimeString(secondTime));
							totalTime += secondTime;
						}
					} else {
						System.err.println("first part failed.");
					}
				} else {
					unsolved++;
				}
			} catch (SQLException | IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		int size = 45;
		System.out.println("*".repeat(size));
		long wallTime = System.nanoTime() - start;
		System.out.println("Year stats:");
		System.out.println("-".repeat(size));
		System.out.println("Task algorithm time:" + TimeUtils.getTimeString(totalTime));
		System.out.println("Bencmark wall time:" + TimeUtils.getTimeString(wallTime));
		System.out.print((unsolved > 0 ? "Unsolved tasks:" + unsolved : "") + "\n");
		System.out.println("-".repeat(size));
		System.out.println("*".repeat(size));
	}

	private void doTools() {
		System.out.println(parsedArgs);
		String arg = parsedArgs.getString("generate_year");
		if (arg != null) {
			for (int i = 1; i <= 25; i++) {
				try {
					DayGenerator.generate(Integer.parseInt(arg), i);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void run() {
		String mode = parsedArgs.get("mode");
		System.out.println(mode);
		switch (mode) {
		case SOLVE -> doSolve();
		case BENCHMARK -> doBenchmark();
		case TOOLS -> doTools();
		}
	}

}
