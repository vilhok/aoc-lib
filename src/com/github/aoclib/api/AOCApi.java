package com.github.aoclib.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.aoclib.db.DBManager;
import com.github.aoclib.solver.Part;
import com.github.aoclib.solver.Solution;

public final class AOCApi extends Api {

	static {
		Thread httpWorker = new Thread(new HTTPWorker());
		httpWorker.setPriority(Thread.MIN_PRIORITY);
		httpWorker.setDaemon(true);
		httpWorker.start();
	}

	/*
	 * various URLs for interacting with a specific day.
	 */
	private static final String dayURL = "https://adventofcode.com/%d/day/%d";
	private static final String inputURL = "https://adventofcode.com/%d/day/%d/input";
	private static final String answerURL = "https://adventofcode.com/%d/day/%d/answer";
	// private static final String yearpage = "https://adventofcode.com/%d/";

	// private static final String eventpage =
	// "https://adventofcode.com/2015/events";

	private static final String personalStats = "https://adventofcode.com/%d/leaderboard/self";

	static final String USER_AGENT = "aoc-java-API; https://github.com/vilhok/aoc-api";

	/*
	 * 
	 */
	private static final String timeRegex = "Please wait (.*) before trying again.";
	private static final String alternateTimeRegex = "You have (.*)s left to wait.";
	private static final String oldAnswerRegex = "Your puzzle answer was .*\\.";

	private static final String longtime = " Because you have guessed incorrectly 4 times on this puzzle, please wait 5 minutes before trying again";
	private static final String waitTime = "You have 2m 34s left to wait.";

	/*
	 * Queue for outbound HTTP requests. Multiple requests can be submitted at same
	 * time, but the the client does the rate limiting.
	 */
	private static LinkedBlockingQueue<AOCRequest> requests = new LinkedBlockingQueue<AOCRequest>();

	/**
	 * 
	 */
	private static final HttpClient httpClient = HttpClient.newBuilder()//
			.version(HttpClient.Version.HTTP_2)//
			.connectTimeout(Duration.ofSeconds(10))//
			.build();

	/*
	 * Waits at least 1 second before making a request, even before the very first.
	 */
	private static long queryWaitTime(AOCRequestType type) throws SQLException {
		long nextPossibleTime = DBManager.getNextApiTime(type);
		long systemTime = System.currentTimeMillis();
		return systemTime > nextPossibleTime ? 1000 : nextPossibleTime - systemTime;
	}

	/**
	 * Offers a solution to the AOC website. Returns a {@code SubmitStatss} which
	 * represents the query result.
	 * 
	 * @param username
	 * @param solution
	 * @return
	 * @throws IOException
	 */

	@Override
	public SubmitStatus offerSolution(String username, Solution solution) throws IOException {
		String cookie = getCookie(username);
		if (solution.day == 0 || solution.year == 0) {
			throw new IOException("Invalid date:" + username + "/" + solution);
		}

		String ur = String.format(answerURL, solution.year, solution.day);
		URI u = URI.create(ur);
		System.out.println(u);
		HttpRequest request = HttpRequest.newBuilder()//
				.POST(getBody(solution.part, solution.solution))//
				.uri(u)//
				.setHeader("Cookie", cookie)//
				.header("User-Agent", USER_AGENT)//
				.header("Content-Type", "application/x-www-form-urlencoded")//
				.build();

		AOCRequest requestFuture = new AOCRequest(request, AOCRequestType.SUBMIT);

		System.out.println("Submitting solution attempt..");
		requests.add(requestFuture);
		while (true) {
			try {
				System.out.println("Waiting for a reply..");
				String response = requestFuture.get();
				SubmitStatus ss = SubmitStatus.forResult(response);
				if (ss == SubmitStatus.UNDEFINED) {
					System.out.println(response);
				}
				return ss;
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	private String getCookie(String username) throws IOException {
		try {
			return DBManager.getCookie(username);
		} catch (SQLException e1) {
			throw new IOException(e1);
		} catch (NoSuchElementException e2) {
			throw new IOException("User " + username + " not found from database.");
		}
	}

	/**
	 * Helper function to get the BodyPublisher object for POST request.
	 * 
	 * @param level
	 * @param answer
	 * @return
	 */
	private static BodyPublisher getBody(Part level, Object answer) {
		return BodyPublishers.ofString(String.format("level=%d&answer=%s", level.intvalue, answer.toString()));
	}

	/**
	 * Downloads the whole task page. Can be used to parse existing solutions.
	 * 
	 * @param cookie
	 * @param year
	 * @param day
	 * @return
	 */
	@Override
	public AOCResponse checkTaskPage(String username, int year, int day) throws IOException {

		String cookie = getCookie(username);
		AOCRequestType aocfpage = AOCRequestType.TASKPAGE_FETCH;
		HttpRequest request = HttpRequest.newBuilder()//
				.GET()//
				.uri(URI.create(String.format(dayURL, year, day)))//
				.setHeader("Cookie", cookie)//
				.header("User-Agent", USER_AGENT)//
				.build();

		AOCRequest item = new AOCRequest(request, aocfpage);
		requests.add(item);
		return null;
//		while (true) {
//			try {
//				return null;
//				return parseResponse(aocfpage, item.get());
//			} catch (InterruptedException | ExecutionException e) {
//				e.printStackTrace();
//			}
//		}
	}

	private HttpRequest getRequest(String request, String cookie) {
		return HttpRequest.newBuilder()//
				.GET()//
				.uri(URI.create(request))//
				.setHeader("Cookie", cookie)//
				.header("User-Agent", USER_AGENT)//
				.build();
	}

	/**
	 * Attempts to download AOC input for a given day.
	 * 
	 * @param username
	 * @param year
	 * @param day
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	@Override
	public String downloadInput(String username, int year, int day) throws IOException, SQLException {

		String cookie;
		try {
			cookie = DBManager.getCookie(username);
		} catch (SQLException e1) {
			throw new IOException(e1);
		} catch (NoSuchElementException e2) {
			throw new IOException("User " + username + " not found from database.");
		}
		System.out.println(String.format(inputURL, year, day));
		HttpRequest request = getRequest(String.format(inputURL, year, day), cookie);

		AOCRequest item = new AOCRequest(request, AOCRequestType.INPUT_FETCH);
		requests.add(item);

		while (true) {
			try {

				String input = item.get();

				int status = item.getStatus();
				System.out.println(status);
				if (status != 200) {
					System.err.println("status code: " + status);
					System.err.println("Input:");
					System.err.println(input);
					throw new IOException("Input file was not available!");
				}

				DBManager.storeInput(username, year, day, input);

				return input;
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

//	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException, SQLException {
//		AOCApi a = new AOCApi();
//		DBManager.setPath("aoc.db");
//		a.fetchPersonalStats("koodivelho-yt");
////		a.downloadInput("koodivelho-yt", 2015, 1);
//	}

	public void fetchPersonalStats(String username) throws IOException, InterruptedException, ExecutionException {
		String cookie;
		try {
			cookie = DBManager.getCookie(username);
		} catch (SQLException e1) {
			throw new IOException(e1);
		} catch (NoSuchElementException e2) {
			System.err.println("User " + username + " not found from database.");
			return;
		}

		for (int i = 2015; i <= 2015; i++) {
			// if(DBManager.hasMissingSolutions()){
			HttpRequest req = getRequest(String.format(personalStats, 2020), cookie);
			AOCRequest item = new AOCRequest(req, AOCRequestType.INPUT_FETCH);
			requests.add(item);
			System.out.println("request added");
			Thread.sleep(5000);
			System.out.println("getting item");
			String s = item.get();
			System.out.println(s);
			// }

		}

	}

	/**
	 * Parse response from the HTTP
	 * 
	 * @param rawHTTP
	 * @return //
	 */
//	private static SubmitStatus parseResponse(AOCRequestType request, String rawHTTPresponse) {
//		AOCResponse r = new AOCResponse(rawHTTPresponse);
//
//		if (request == AOCRequestType.FRONTPAGE_FETCH) {
//			Matcher m = Pattern.compile("Your puzzle answer was <code>(.*)</code>\\.").matcher(rawHTTPresponse);
//
//			while (m.find()) {
//				for (int j = 1; j <= m.groupCount(); j++) {
//					System.out.println(m.group(j));
//				}
//				System.out.println("Done");
//			}
//			// TODO: push FP fetch to DB
//
//		} else if (request == AOCRequestType.SUBMIT) {
//
//			return SubmitStatus.forResult(rawHTTPresponse);
//		}
//		// TODO: push input fetch to do
//
//		// TODO: submit:
//		/*
//		 * parse result -> push time or parse long time ->
//		 */
//		return null;
//	}

	// ------------ Class AOCRequestType ------------

	public enum AOCRequestType {
		INPUT_FETCH("input fetch", 10000), //
		TASKPAGE_FETCH("front page fetch", 1000), //
		EVENTPAGE_FETCH("event page fetch", 1000), //
		SUBMIT("solution submit", 5000), //
		PERSONAL_STATS("personal leaderboard", 5000);

		private String requestExplanation;
		private int defaultWaitMS;

		AOCRequestType(String string, int ms) {
			this.requestExplanation = string;
			this.defaultWaitMS = ms;
		}
	}

	// ------------ End AOCRequestType ------------

	// ------------ Class AOCRequest ------------

	/**
	 * Due to AOC rate-limiting, each outbound HTTP-request is wrapped into a
	 * Future. The submitter of this request can immediately start to wait the
	 * result, which eventually completes after the HTTP client is able to process
	 * the actual request. The result of the request will be put in this object,
	 * which decrements the countdownlatch that is used as wait mechanism.
	 *
	 */
	private static class AOCRequest implements Future<String> {
		private final HttpRequest request;

		private CountDownLatch wait = new CountDownLatch(1);

		private int statusCode;
		private String result;
		private AOCRequestType type;

		public AOCRequest(HttpRequest request, AOCRequestType type) {
			super();
			this.request = request;
			this.type = type;
		}

		public void putBody(String result) {
			this.result = result;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return false;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public boolean isDone() {
			return result != null;
		}

		@Override
		public String get() throws InterruptedException, ExecutionException {
			boolean waiting = true;
			while (waiting) {
				System.out.println("started waiting");
				wait.await(); //
				System.out.println("wait ended");
				waiting = false;
			}
			return result;
		}

		@Override
		public String get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException, TimeoutException {
			return get();
		}

		public void ready() {
			wait.countDown();
		}

		public void putCode(int statusCode) {
			this.statusCode = statusCode;
		}

		public int getStatus() {
			return statusCode;
		}
	}

	// ------------ Class HTTPWorker ------------

	private static class HTTPWorker implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					AOCRequest request = requests.poll(1, TimeUnit.DAYS);
					System.out.println("Next API request: " + request.type.requestExplanation);

					long waitms = queryWaitTime(request.type);
					long lastPrintSeconds = waitms / 1000;
					while (waitms > 0) {

						long nextSeconToPrint = waitms / 1000;
						if (nextSeconToPrint != lastPrintSeconds) {
							System.out.println("Waiting ~" + nextSeconToPrint + " s before request..");
							lastPrintSeconds = nextSeconToPrint;
						}
						long waitTime = waitms > 250 ? 250 : waitms;
						Thread.sleep(waitTime);
						waitms -= waitTime;
					}

					HttpResponse<String> response = httpClient.send(request.request,
							HttpResponse.BodyHandlers.ofString());

					System.out.println("Response received");
					request.putBody(response.body());
					request.putCode(response.statusCode());
//					System.out.println(response.body());
//					System.out.println(response.statusCode());
//					System.out.println(response.headers());
					long time;
					if (request.type == AOCRequestType.SUBMIT) {
						time = System.currentTimeMillis() + parseTime(request.type, response.body());
					} else {
						time = System.currentTimeMillis() + request.type.defaultWaitMS;
					}

					DBManager.insertNextTime(time, request.type);
					request.ready();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();

				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		private long parseTime(AOCRequestType type, String body) {
			Matcher m = Pattern.compile(timeRegex).matcher(body);
			if (m.find()) {
				String response = m.group(1);
				if (response.contains("one minute")) {
					return 60 * 1000;
				} else if (response.contains("five")) {
					return 5 * 60 * 1000;
				} else {
					System.err.println("Error: unknown wait time requested by the server:" + response);
				}
				// TODO: parse other kinds of responses
				return type.defaultWaitMS;
			} else {
				return type.defaultWaitMS;
			}

		}
	}

	@Override
	public AOCResponse checkEventPage(String username) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AOCResponse checkYearPage(String username, int year) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
