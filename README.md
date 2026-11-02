# Assignment 4

Java 17 solutions for LCS, block merging, tree selection, naive search, KMP,
Rabin-Karp, suffix array with Kasai LCP, and Aho-Corasick.

Requires JDK 17+ and Python 3.12+. Run from the project folder:

```sh
python tools/run.py --setup
python tools/run.py --check
python tests/student/test_algorithms.py
```

Build and run directly:

```sh
javac --release 17 -encoding UTF-8 -d Java/build Java/source/*.java
java -Xmx384m -cp Java/build Main
```

Enter one JSON request per line. Each output line is one JSON result.

```json
{"task":"kmp","text":"abababa","pattern":"aba"}
{"positions":[0,2,4]}
```

Run the search comparison:

```sh
javac --release 17 -encoding UTF-8 -cp Java/build -d Java/build tests/student/Benchmark.java
java -Xmx384m -cp Java/build Benchmark
```

See ANALYSIS.md for the method details, test results, and measured times.
