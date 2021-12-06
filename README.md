# A helper library for Advent of code

A helper library for minimizing the amount of work needed to participate in [https://adventofcode.com](ttps://adventofcode.com)

### Features

 * Automatic input download
 * Rate limited queries to AoC
 * Local input caching
 * Local correct/wrong solution caching
 * Boilerplate generator
 * Solution speed benchmarks 
 
 
 
### Workflow

1. Pre-generate boilerplate for current year and setup the cookies
2. Open the solver template for current day in your text editor
3. Open web browser to read the problem at [https://adventofcode.com](ttps://adventofcode.com)
4. Write the solution and run it to automatically submit the result

## Getting started

For quick start, just use this code and run it and follow the top section of the printed help.

```java
package mypackage;

import com.github.aoclib.solver.Solver;

public class MyClass {
	public static void main(String[] args){
		DBManager.setPath("aoc.db");
		new Solver(args).run();
	}
}
```

Then, run the program again and it tells you what class file you need to create in order to solve a task.

## Full instructions
You may just run the file multiple times and follow instructions after each run. Alternatively, do the following:
### Adding a user

Have the same or similar code as in the example above. 

Then add a user:

`java MyClass user --add myName --cookie session=0123456789...adcdef`

The `myName` is just a local username and is not used to interact with advent of code. While technically multiple usernames and different cookies could be added, there are very few reasons to do so. It's not considered appropriate to solve puzzles for multiple users at the same time. Such feature does not exist in this program, nor it should be ever created.


### Creating a solver

To get all default solver boilerplate for a specific year, run: 

`java MyClass tools --generate-year 2021`


#### Alternative: the manual way
Extend class `DayX` with a specific name and implement the methods.

The file should be name in the following way. The dot-notation means the java package path:
`solution.yearYYYY.YearYYYYDayDD.java`

For example:
`solution.year2021.Year2021Day06.java`

If you want to store the solvers in some other path, you may setup it like this (before calling `run()` from the solver):

```java
ReflectiveDayProvider.setRoot("my.path.to.files.year%d");
```

This path must have a single `%d` which will be replaced by the `--year` argument that is given when solving a specific day.

### Solving a puzzle

Run `java MyClass solve -u myName -y 2021 -d 6`

If the AoC is in progress, note that the year defaults to current year (in December) and previous year otherwise. The day defaults to current day (UTC-5). If you are solving the puzzles on the day they unlock, you can just run:

Run `java MyClass solve -u myName`


### Change the database location

The cache is a single sqlite database file. It will be stored in your working directoy as `aoc.db`

Give a custom db name and location by:

```java
DBManager.setFile("path/to/my.db")`
```
Non-existing directories will be generated.


### Overriding the DayProvider

In case you are not happy about initializing the daily solvers by reflection or if you dont like the default naming scheme, feel free to implement `DayProvider` and set that to the solver by using `mySolver.daysFrom(dayprovider);`


At this point, your file might look like something like this:

```java
package mypackage;

import com.github.aoclib.solver.Solver;

public class MyClass {
	public static void main(String[] args){
		DBManager.setPath("my/custom/aoc.db");
		new Solver(args).solversFrom(new MyBetterSolverGenerator()).run();
	}
}
```

On top of this, this file does not really have to be modified. Everything else is done with command line arguments.

Pass `-h` to find all the subcommands, and `subcommand -h` to get a specific help.

## Parsing input and solving a puzzle

To actually solve the puzzle, complete the following methods:

```java
public Object firstPart(InputParser input){
return NOT_SOLVED;
}
public Object secondPart(InputParser input){
return NOT_SOLVED;
}
```

It's often good idea to just print your result and return NOT_SOLVED when you are working on the problem. Each returned value that is not considered *invalid* will be submitted to Advent of code, which might get you banned in the long run. The library respects the submission delays imposed by the page, and refuses to submit a same wrong solution twice. After you are sure you want to submit the answer, return the whatever answer you have. Primitives will will be autoboxed and any return value will be automatically converted to string with `toString()`

Some of the aforementioned invalid values include: ` true, false, 0,-1, null, ""`


### Input parser examples

`InputParser` contains the input as a `List<String>` and has several methods to get that in a suitable format, such as spliting the lines by a `Delimiter` or by a custom (regex) and converting each value to any format.

Some examples:

```java
// a single line/word input:
String line =  input.string();

// a single line input that is an integer:
int number = input.integer();

// multiple integers across multiple lines to a single array
// whitespace as default delimiter
int[] myInts = input.asSingleIntArray(); 
int[] myInts2 = input.asSingleIntArray(Delimiter.COMMA); 

// each line of delimited integers as a separate list:
List<List<Integer>> rows = input.linesAsLists(Delimiter.COMMA,Integer::parseInt);

// each group of lines as separate lists.
// Groups of lines are separated by two newlines. 
List<List<String> groups = input.getGroups();

//the input as a single matrix or characters
char[][] myTable = input.charMatrix();
```
See the `InputParser` documentation for the full list of functions.


