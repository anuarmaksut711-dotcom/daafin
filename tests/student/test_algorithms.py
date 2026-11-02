import itertools
import pathlib
import random
import sys
from functools import lru_cache

ROOT = pathlib.Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))
from run import build, execute
from public_validate import check


def search(text, pattern):
    return [i for i in range(len(text) - len(pattern) + 1)
            if text.startswith(pattern, i)]


def lcs_length(a, b):
    best = 0
    for mask in range(1 << len(a)):
        s = "".join(a[i] for i in range(len(a)) if mask & (1 << i))
        it = iter(b)
        if all(any(c == d for d in it) for c in s):
            best = max(best, len(s))
    return best


@lru_cache(None)
def merge_cost(blocks):
    if len(blocks) < 2:
        return 0
    return min(blocks[i] + blocks[i + 1] + merge_cost(
        blocks[:i] + (blocks[i] + blocks[i + 1],) + blocks[i + 2:])
        for i in range(len(blocks) - 1))


def tree_weight(weights, edges):
    best = 0
    for mask in range(1 << len(weights)):
        if any(mask & (1 << u) and mask & (1 << v) for u, v in edges):
            continue
        best = max(best, sum(w for i, w in enumerate(weights) if mask & (1 << i)))
    return best


def suffix_result(text):
    sa = sorted(range(len(text)), key=lambda i: text[i:])
    lcp = [0] * len(text)
    best = ""
    for k in range(1, len(text)):
        a, b = sa[k - 1], sa[k]
        length = 0
        while a + length < len(text) and b + length < len(text):
            if text[a + length] != text[b + length]:
                break
            length += 1
        lcp[k] = length
        if length > len(best):
            best = text[a:a + length]
    return {"sa": sa, "lcp": lcp, "repeated": best}


def make_cases():
    rng = random.Random(417)
    cases = []

    def add(request, output):
        cases.append((request, output))

    def word(limit):
        return "".join(rng.choice("ab !~") for _ in range(rng.randrange(limit + 1)))

    for a, b in [("", ""), ("ab", "ba"), ("tea_time", "team")]:
        add({"task": "lcs", "a": a, "b": b}, {"length": lcs_length(a, b)})
    for _ in range(60):
        a, b = word(8), word(8)
        add({"task": "lcs", "a": a, "b": b}, {"length": lcs_length(a, b)})

    for sizes in [[], [0], [0, 0, 0], [1, 100, 1], [10**9] * 4]:
        add({"task": "merge", "sizes": sizes}, {"cost": merge_cost(tuple(sizes))})
    for _ in range(60):
        sizes = [rng.randrange(10) for _ in range(rng.randrange(8))]
        add({"task": "merge", "sizes": sizes}, {"cost": merge_cost(tuple(sizes))})

    for weights, edges in [([], []), ([-5], []), ([0, 0], [[0, 1]]),
                           ([10**9, -1, 10**9, 10**9], [[1, 0], [2, 1], [1, 3]])]:
        add({"task": "tree", "n": len(weights), "weights": weights, "edges": edges},
            {"weight": tree_weight(weights, edges)})
    for _ in range(60):
        n = rng.randrange(12)
        weights = [rng.randrange(-9, 10) for _ in range(n)]
        labels = list(range(n))
        rng.shuffle(labels)
        edges = [[labels[v], labels[rng.randrange(v)]] for v in range(1, n)]
        rng.shuffle(edges)
        add({"task": "tree", "n": n, "weights": weights, "edges": edges},
            {"weight": tree_weight(weights, edges)})

    pairs = [("", ""), ("abc", ""), ("", "a"), ("a", "ab"),
             ("aaaaa", "aaa"), ("abababa", "aba"), ("abdj", "ab"),
             ("A !~A !~", " !~"), ("abacababacaba", "abacaba")]
    pairs += [(word(35), word(10)) for _ in range(100)]
    for text, pattern in pairs:
        for task in ["naive", "kmp", "rk"]:
            add({"task": task, "text": text, "pattern": pattern},
                {"positions": search(text, pattern)})

    for length in range(7):
        for letters in itertools.product("ab", repeat=length):
            text = "".join(letters)
            add({"task": "suffix", "text": text}, suffix_result(text))
    for text in ["banana", "mississippi", " !~ !~", "zyx"] + [word(40) for _ in range(60)]:
        add({"task": "suffix", "text": text}, suffix_result(text))

    dictionaries = [([], ["", "abc"]), (["he", "she", "he"], ["sheshe", "sheshe", ""]),
                    (["a", "aa", "aaa", "aa"], ["aaaaa", "a", ""]), (["a"], [])]
    for _ in range(60):
        patterns = [word(6) or "a" for _ in range(rng.randrange(12))]
        texts = [word(30) for _ in range(rng.randrange(6))]
        dictionaries.append((patterns, texts))
    for patterns, texts in dictionaries:
        add({"task": "aho", "patterns": patterns, "texts": texts},
            {"counts": [[len(search(text, p)) for p in patterns] for text in texts]})
    return cases


def verify(command, cases, label):
    answers = execute(command, [request for request, _ in cases])
    for index, ((request, expected), answer) in enumerate(zip(cases, answers)):
        assert check(request, answer, expected), (label, index, request, expected, answer)
    print(f"PASS {label}: {len(cases)} cases")


def scale_cases():
    n = 100000
    yield ({"task": "lcs", "a": "a" * 1000, "b": "a" * 1000}, {"length": 1000})
    yield ({"task": "merge", "sizes": [10**9] * 200}, {"cost": 1544 * 10**9})
    yield ({"task": "tree", "n": n, "weights": [10**9] * n,
            "edges": [[i - 1, i] for i in range(1, n)]}, {"weight": 50000 * 10**9})
    yield ({"task": "tree", "n": n, "weights": [10**9] * n,
            "edges": [[0, i] for i in range(1, n)]}, {"weight": (n - 1) * 10**9})
    yield ({"task": "naive", "text": "a" * 3000, "pattern": "a" * 1500},
           {"positions": list(range(1501))})
    yield ({"task": "kmp", "text": "a" * 200000, "pattern": "a" * 100000},
           {"positions": list(range(100001))})
    yield ({"task": "kmp", "text": "a" * 200000, "pattern": ""},
           {"positions": list(range(200001))})
    yield ({"task": "rk", "text": "a" * 10000, "pattern": "a" * 2000},
           {"positions": list(range(8001))})
    yield ({"task": "rk", "text": "a", "pattern": "b" * 10000}, {"positions": []})
    n = 50000
    yield ({"task": "suffix", "text": "a" * n},
           {"sa": list(range(n - 1, -1, -1)), "lcp": list(range(n)), "repeated": "a" * (n - 1)})
    text = ("".join(chr(i) for i in range(32, 127)) * 527)[:50000]
    sa = sorted(range(len(text)), key=lambda i: (i % 95, -i))
    lcp = [0] * len(text)
    for i in range(1, len(text)):
        if sa[i] % 95 == sa[i - 1] % 95:
            lcp[i] = len(text) - sa[i - 1]
    yield ({"task": "suffix", "text": text},
           {"sa": sa, "lcp": lcp, "repeated": text[:-95]})
    patterns = ["a" * i for i in range(1, 447)]
    yield ({"task": "aho", "patterns": patterns, "texts": ["a" * 200000]},
           {"counts": [[200001 - i for i in range(1, 447)]]})
    yield ({"task": "aho", "patterns": ["a" * 10] * 10000,
            "texts": ["a" * 2000] * 100}, {"counts": [[1991] * 10000 for _ in range(100)]})
    yield ({"task": "aho", "patterns": ["a" * 100000], "texts": ["a" * 200000]},
           {"counts": [[100001]]})


if __name__ == "__main__":
    command = build("Java")
    cases = make_cases()
    verify(command, cases + list(reversed(cases)), "seed 417 and state reset")
    for i, case in enumerate(scale_cases(), 1):
        verify(command, [case], f"scale {i} ({case[0]['task']})")
