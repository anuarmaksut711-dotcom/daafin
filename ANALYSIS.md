# Analysis

## Environment and commands

Windows 11 Pro, AMD Ryzen 7 250, 16.44 GB RAM. Microsoft OpenJDK 17.0.19,
Python 3.14.7. No extra libraries are needed. `java` and `javac` must be on PATH.
The commands in README.md were run with `py -3.14` in place of `python` on Windows.
Java uses `-Xmx384m`. All costs and tree totals use `long`.

## Methods

**LCS.** `dp[i][j]` is the best length for the first `i` characters of `a`
and first `j` characters of `b`. Row 0 and column 0 are zero. Equal final
characters give `dp[i-1][j-1]+1`; otherwise take the larger of the top and
left cells. Rows are processed from top to bottom, so each needed cell is
ready. Any common subsequence either uses the equal final character or skips
one final character. These choices cover an optimum. Reconstruction follows
equal characters diagonally and otherwise follows the larger cell. A tie
skips a character from `a`. Reverse the collected characters at the end.

**Merge.** `dp[l][r]` is the minimum cost for the closed interval `[l,r]`.
One block costs zero. For each last split `k`, try
`dp[l][k]+dp[k+1][r]+prefix[r+1]-prefix[l]`. Process increasing interval
lengths. Every valid last merge joins two adjacent child intervals, so trying
all splits finds the optimum. Keep the first split on a tie. Reconstruction
outputs both child plans before `[l,k,r]`, which makes the plan chronological.
No blocks or one block returns an empty plan and cost zero.

**Tree.** Root at vertex 0 and build a parent-first order without recursion.
In reverse order, `take[v]` is the best subtree weight with `v` selected;
`skip[v]` is the best weight with `v` excluded. Start with its own weight and
zero, respectively. Add `skip[child]` to `take[v]`, and
`max(take[child],skip[child])` to `skip[v]`. Children are ready first.
Subtrees have no edges between them, so their independent optima can be added.
Reconstruction uses the parent-first order: a selected parent forces a skip;
otherwise choose `take` only when it is larger. Ties skip. This also allows
the empty set for negative weights. An empty tree returns zero.

**Naive.** At each start from 0 through `n-m`, compare pattern characters
from left to right. Before comparison `j`, the first `j` characters match.
Report the start exactly when all `m` characters match. Every valid start is
visited, including overlapping ones and the last start. With `m=0`, the loop
reports all `n+1` boundaries. With `m>n`, it reports none.

**KMP.** `pi[i]` is the longest proper prefix of the pattern that is also a
suffix ending at `i`. Build it left to right, following earlier prefix links
on a mismatch. During search, `j` is the matched pattern prefix at the end
of the scanned text. Fallback keeps the longest remaining possible prefix,
so it cannot miss a match. After a full match, use `pi[m-1]` to keep overlaps.
Each successful extension advances the scan; each fallback reduces `j`.
Handle an empty pattern before indexing it. A longer pattern produces no match.

**Rabin-Karp.** The hash is the ASCII polynomial with base 31 and modulus
101, with the highest power on the left. Build the first window and pattern
hashes left to right. For each next window, subtract the old character times
`31^(m-1)`, normalize the remainder, multiply by 31, and add the new character.
This preserves the window hash. Equal hashes trigger actual character
comparisons; only exact matches are reported. Thus collisions cannot add false
matches. Empty and longer patterns are handled before creating a window.

**Suffix array.** Initially ranks are ASCII codes. At width `w`, sort suffix
indices by `(rank[i],rank[i+w])`, using -1 past the end. Equal pairs get the
same new rank. A pair describes twice as many characters as the old rank,
so doubling eventually gives full lexicographic order. Stop early when all
ranks differ. `Arrays.sort` uses comparison sorting on `Integer[]`; suffix
strings are not copied. Only indices 0 through `n-1` are included.

**Kasai LCP.** Build inverse suffix positions. For text start `i`, compare
with the previous suffix in sorted order. After moving to `i+1`, at least
`common-1` characters can be reused; reset at sorted position zero. The total
number of character extensions is linear. `lcp[0]=0`. Suffixes with a common
prefix form a contiguous sorted group, so some adjacent pair achieves the
longest repeated substring. Return the first larger LCP found during the
text-order scan. Overlaps are allowed. Empty input returns empty arrays and string.

**Aho-Corasick.** Each node has 95 transitions for ASCII 32 through 126.
Insert the dictionary and save one terminal node per original pattern position.
BFS builds failure links to the longest proper suffix present in the trie.
Missing transitions use the already built failure state's transitions; root
misses stay at root. During each text scan, count visits to reached states.
Reverse BFS adds each state's visits to its failure state. Every suffix-pattern
match is then counted at its terminal. Duplicate patterns share a terminal
and get equal counts. This aggregates dense matches without listing them.
State and visits start fresh for each text. Empty dictionaries give empty
rows; empty texts give zero rows; no texts gives no rows.

## Complexity

Input and output storage are separate from auxiliary storage.
Here `z` is returned match count, `c` is equal-hash window count, `S` is trie
state count, `L` is total dictionary length, `p` is pattern count, and `q` is text count.

|Command|Time|Auxiliary space|Output space|
|---|---|---|---|
|lcs|O((n+1)(m+1))|O((n+1)(m+1))|O(min(n,m)+1)|
|merge|O(n^3+1)|O(n^2+1), including reconstruction stack|O(n+1)|
|tree|O(n+1)|O(n+1), including adjacency lists and reconstruction|O(n+1)|
|naive|O(n*m+n+1)|O(1)|O(z+1)|
|kmp|O(n+m+z+1)|O(m+1)|O(z+1)|
|rk|O(n+m+c*m) for 1<=m<=n; O(n*m+n+m+1) overall|O(1)|O(z+1)|
|suffix doubling|O(n log^2(n+1)+1)|O(n+1), including sorting space|O(n+1)|
|Kasai and repeat|O(n+1)|O(n+1)|O(n+1)|
|aho|O(L+95*S+p + sum of (text length+S+p) over texts)|O(95*S+p)|O(q*(p+1)+1)|

Output array/list creation and copying the returned strings are included.
The empty cases take O(1), except when required output has nonzero size.

## Tests

`python tools/run.py --check`: all 44 public cases passed, plus the two
complete batches in one process. `python tests/student/test_algorithms.py`:
1,548 small/mixed checks and 14 scale checks passed. Small tests use seed 417,
full enumeration for LCS and tree optima, all adjacent merge orders, direct
suffix sorting, and independent exact matching. Witnesses are validated by
the supplied public validator. Each small case is run again in reverse order.

|Case in tests/student/test_algorithms.py|Expected|Actual|Defect checked|
|---|---|---|---|
|lcs: `ab`, `ba`|length 1, valid witness|PASS|Tie reconstruction|
|lcs: empty strings|0 and empty sequence|PASS|Empty prefixes|
|merge: `[1,100,1]`|203, valid two-step plan|PASS|Nonadjacent greedy merges|
|merge: 200 blocks of 10^9|1544000000000, valid plan|PASS|Overflow and maximum size|
|tree: negative singleton|0, empty set|PASS|Forcing a vertex|
|tree: 100000-vertex chain, weights 10^9|50000000000000|PASS|Deep recursion and overflow|
|tree: 100000-vertex star, weights 10^9|99999000000000|PASS|High degree and reconstruction|
|all searches: `aaaaa`, `aaa`|[0,1,2]|PASS|Overlaps|
|all searches: `abc`, empty pattern|[0,1,2,3]|PASS|Final empty boundary|
|rk: `abdj`, `ab`|[0]|PASS|Hash collision: ab and dj both hash to 75|
|kmp: 200000 a's, 100000 a's|Starts 0..100000|PASS|Full search limits|
|rk: 10000 a's, 2000 a's|Starts 0..8000|PASS|Dense verified candidates|
|suffix: all binary strings through length 6|Direct suffix/LCP reference|PASS|Rank and Kasai errors|
|suffix: 50000 a's|SA 49999..0, LCP 0..49999|PASS|Long overlaps and scale|
|suffix: 50000 periodic printable characters|Rank-by-character reference|PASS|Full alphabet and suffix ordering|
|aho: he, she, he; sheshe twice and empty|[2,2,2], [2,2,2], [0,0,0]|PASS|Suffix counts, duplicates, reset|
|aho: 10000 duplicate patterns, 100 texts|100 rows of 10000 counts equal to 1991|PASS|Dense output without per-match expansion|
|aho: one pattern of length 100000, text length 200000|100001|PASS|Maximum trie depth|

The tests also cover empty trees, zero weights, zero block sizes, longer
patterns, spaces, punctuation, empty dictionaries, and no texts.

## Search comparison

Run the two benchmark commands in README.md after building. The generator is
Java `Random(417+n)`, alphabet a-z. The random pattern is the text slice
`[n/2,n/2+m)`. Repeated cases use only a's in both strings. `m=n/4`.
All methods are checked against independent `String.startsWith` results
before any timings. This built-in is used only in the benchmark reference.

Each method and input has 50 unmeasured warm-up calls followed by three
measured calls in one JVM. Normal tiered JIT is enabled. `System.nanoTime`
measures the whole algorithm call, including result allocation and a volatile
result store. It excludes compilation, input generation, checking, JSON I/O,
and printing. All three methods use the same timing scope. Values are milliseconds.

|Case|n|m|Method|Run 1 (ms)|Run 2 (ms)|Run 3 (ms)|Median (ms)|Correct|
|---|---:|---:|---|---:|---:|---:|---:|---|
|random|600|150|naive|0.072600|0.074500|0.075200|0.074500|yes|
|random|600|150|kmp|0.048200|0.047700|0.048200|0.048200|yes|
|random|600|150|rk|0.051000|0.069900|0.063800|0.063800|yes|
|random|1500|375|naive|0.047400|0.045800|0.046700|0.046700|yes|
|random|1500|375|kmp|0.089300|0.116600|0.119600|0.116600|yes|
|random|1500|375|rk|0.073700|0.084400|0.096200|0.084400|yes|
|random|3000|750|naive|0.104000|0.051900|0.051700|0.051900|yes|
|random|3000|750|kmp|0.061800|0.058300|0.058100|0.058300|yes|
|random|3000|750|rk|0.101800|0.093700|0.092600|0.093700|yes|
|repeated|600|150|naive|0.223100|0.297700|0.230000|0.230000|yes|
|repeated|600|150|kmp|0.022800|0.022400|0.033300|0.022800|yes|
|repeated|600|150|rk|0.294200|0.269500|0.263100|0.269500|yes|
|repeated|1500|375|naive|0.540600|0.504700|0.503800|0.504700|yes|
|repeated|1500|375|kmp|0.017400|0.012500|0.015600|0.015600|yes|
|repeated|1500|375|rk|1.010300|0.999800|0.988600|0.999800|yes|
|repeated|3000|750|naive|1.992000|1.976800|2.040300|1.992000|yes|
|repeated|3000|750|kmp|0.030400|0.019800|0.045200|0.030400|yes|
|repeated|3000|750|rk|5.321800|5.355600|5.431400|5.355600|yes|

Random inputs usually fail naive comparisons early, so its low overhead helps.
On repeated inputs, naive and RK compare many full windows; KMP reuses prefixes.
With modulus 101, unequal strings can share a hash, adding verification work.
Even true equal windows require verification. This is a small-modulus exercise,
so the results do not describe all production hash choices. Small timings vary
with JIT and system load; they do not prove complexity bounds.

