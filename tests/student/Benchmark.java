import java.util.*;

public class Benchmark {
    private static volatile Object result;

    private static Map<String, Object> run(String name, Map<String, Object> input) {
        switch (name) {
            case "naive": return Algorithms.naive(input);
            case "kmp": return Algorithms.kmp(input);
            case "rk": return Algorithms.rk(input);
            default: throw new IllegalArgumentException(name);
        }
    }

    public static void main(String[] args) {
        System.out.println("|Case|n|m|Method|Run 1 (ms)|Run 2 (ms)|Run 3 (ms)|Median (ms)|Correct|");
        System.out.println("|---|---:|---:|---|---:|---:|---:|---:|---|");
        for (String type : List.of("random", "repeated")) {
            for (int n : new int[]{600, 1500, 3000}) {
                int m = n / 4;
                String text;
                String pattern;
                if (type.equals("random")) {
                    Random random = new Random(417 + n);
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < n; i++) {
                        builder.append((char) ('a' + random.nextInt(26)));
                    }
                    text = builder.toString();
                    pattern = text.substring(n / 2, n / 2 + m);
                } else {
                    text = "a".repeat(n);
                    pattern = "a".repeat(m);
                }
                Map<String, Object> input = Map.of("text", text, "pattern", pattern);
                List<Integer> expected = new ArrayList<>();
                for (int i = 0; i <= n - m; i++) {
                    if (text.startsWith(pattern, i)) {
                        expected.add(i);
                    }
                }
                for (String name : List.of("naive", "kmp", "rk")) {
                    if (!run(name, input).get("positions").equals(expected)) {
                        throw new AssertionError(name + " returned a wrong result");
                    }
                }
                for (String name : List.of("naive", "kmp", "rk")) {
                    for (int i = 0; i < 50; i++) {
                        result = run(name, input);
                    }
                    double[] times = new double[3];
                    for (int i = 0; i < times.length; i++) {
                        long start = System.nanoTime();
                        result = run(name, input);
                        times[i] = (System.nanoTime() - start) / 1000000.0;
                    }
                    double[] sorted = times.clone();
                    Arrays.sort(sorted);
                    System.out.printf(Locale.ROOT,
                            "|%s|%d|%d|%s|%.6f|%.6f|%.6f|%.6f|yes|%n",
                            type, n, m, name, times[0], times[1], times[2], sorted[1]);
                }
            }
        }
    }
}
