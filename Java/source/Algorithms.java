import java.util.*;

public final class Algorithms {
    static Map<String, Object> lcs(Map<String, Object> input) {
        String a = (String) input.get("a");
        String b = (String) input.get("b");
        int n = a.length();
        int m = b.length();
        int[][] dp = new int[n + 1][m + 1];

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        StringBuilder sequence = new StringBuilder();
        int i = n;
        int j = m;
        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                sequence.append(a.charAt(i - 1));
                i--;
                j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }
        return Map.of("length", dp[n][m], "sequence", sequence.reverse().toString());
    }

    static Map<String, Object> merge(Map<String, Object> input) {
        List<?> sizes = (List<?>) input.get("sizes");
        int n = sizes.size();
        List<int[]> plan = new ArrayList<>();
        if (n < 2) {
            return Map.of("cost", 0L, "plan", plan);
        }

        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + ((Number) sizes.get(i)).longValue();
        }
        long[][] dp = new long[n][n];
        int[][] split = new int[n][n];
        for (int length = 2; length <= n; length++) {
            for (int left = 0; left + length <= n; left++) {
                int right = left + length - 1;
                dp[left][right] = Long.MAX_VALUE;
                long sum = prefix[right + 1] - prefix[left];
                for (int k = left; k < right; k++) {
                    long cost = dp[left][k] + dp[k + 1][right] + sum;
                    if (cost < dp[left][right]) {
                        dp[left][right] = cost;
                        split[left][right] = k;
                    }
                }
            }
        }
        mergePlan(0, n - 1, split, plan);
        return Map.of("cost", dp[0][n - 1], "plan", plan);
    }

    private static void mergePlan(int left, int right, int[][] split, List<int[]> plan) {
        if (left >= right) {
            return;
        }
        int k = split[left][right];
        mergePlan(left, k, split, plan);
        mergePlan(k + 1, right, split, plan);
        plan.add(new int[]{left, k, right});
    }

    static Map<String, Object> tree(Map<String, Object> input) {
        int n = ((Number) input.get("n")).intValue();
        List<?> weights = (List<?>) input.get("weights");
        List<?> edges = (List<?>) input.get("edges");
        List<Integer> selected = new ArrayList<>();
        if (n == 0) {
            return Map.of("weight", 0L, "selected", selected);
        }

        List<List<Integer>> graph = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }
        for (Object item : edges) {
            List<?> edge = (List<?>) item;
            int u = ((Number) edge.get(0)).intValue();
            int v = ((Number) edge.get(1)).intValue();
            graph.get(u).add(v);
            graph.get(v).add(u);
        }

        int[] parent = new int[n];
        int[] order = new int[n];
        parent[0] = -1;
        int count = 1;
        for (int i = 0; i < count; i++) {
            int v = order[i];
            for (int child : graph.get(v)) {
                if (child != parent[v]) {
                    parent[child] = v;
                    order[count++] = child;
                }
            }
        }

        long[] take = new long[n];
        long[] skip = new long[n];
        for (int i = n - 1; i >= 0; i--) {
            int v = order[i];
            take[v] = ((Number) weights.get(v)).longValue();
            for (int child : graph.get(v)) {
                if (child != parent[v]) {
                    take[v] += skip[child];
                    skip[v] += Math.max(take[child], skip[child]);
                }
            }
        }

        boolean[] chosen = new boolean[n];
        for (int v : order) {
            if ((parent[v] == -1 || !chosen[parent[v]]) && take[v] > skip[v]) {
                chosen[v] = true;
                selected.add(v);
            }
        }
        return Map.of("weight", Math.max(take[0], skip[0]), "selected", selected);
    }

    static Map<String, Object> naive(Map<String, Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");
        List<Integer> positions = new ArrayList<>();
        for (int start = 0; start <= text.length() - pattern.length(); start++) {
            int j = 0;
            while (j < pattern.length() && text.charAt(start + j) == pattern.charAt(j)) {
                j++;
            }
            if (j == pattern.length()) {
                positions.add(start);
            }
        }
        return Map.of("positions", positions);
    }

    static Map<String, Object> kmp(Map<String, Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");
        int m = pattern.length();
        List<Integer> positions = new ArrayList<>();
        if (m == 0) {
            for (int i = 0; i <= text.length(); i++) {
                positions.add(i);
            }
            return Map.of("positions", positions);
        }

        int[] pi = new int[m];
        for (int i = 1; i < m; i++) {
            int j = pi[i - 1];
            while (j > 0 && pattern.charAt(i) != pattern.charAt(j)) {
                j = pi[j - 1];
            }
            if (pattern.charAt(i) == pattern.charAt(j)) {
                j++;
            }
            pi[i] = j;
        }

        int j = 0;
        for (int i = 0; i < text.length(); i++) {
            while (j > 0 && text.charAt(i) != pattern.charAt(j)) {
                j = pi[j - 1];
            }
            if (text.charAt(i) == pattern.charAt(j)) {
                j++;
            }
            if (j == m) {
                positions.add(i - m + 1);
                j = pi[j - 1];
            }
        }
        return Map.of("positions", positions);
    }

    static Map<String, Object> rk(Map<String, Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");
        int n = text.length();
        int m = pattern.length();
        List<Integer> positions = new ArrayList<>();
        if (m == 0) {
            for (int i = 0; i <= n; i++) {
                positions.add(i);
            }
            return Map.of("positions", positions);
        }
        if (m > n) {
            return Map.of("positions", positions);
        }

        int base = 31;
        int mod = 101;
        long power = 1;
        long patternHash = 0;
        long windowHash = 0;
        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * base + pattern.charAt(i)) % mod;
            windowHash = (windowHash * base + text.charAt(i)) % mod;
            if (i < m - 1) {
                power = power * base % mod;
            }
        }

        for (int start = 0; start <= n - m; start++) {
            if (windowHash == patternHash) {
                int j = 0;
                while (j < m && text.charAt(start + j) == pattern.charAt(j)) {
                    j++;
                }
                if (j == m) {
                    positions.add(start);
                }
            }
            if (start < n - m) {
                windowHash = (windowHash - text.charAt(start) * power) % mod;
                if (windowHash < 0) {
                    windowHash += mod;
                }
                windowHash = (windowHash * base + text.charAt(start + m)) % mod;
            }
        }
        return Map.of("positions", positions);
    }

    static Map<String, Object> suffix(Map<String, Object> input) {
        String text = (String) input.get("text");
        int n = text.length();
        Integer[] sa = new Integer[n];
        int[] rank = new int[n];
        int[] lcp = new int[n];
        for (int i = 0; i < n; i++) {
            sa[i] = i;
            rank[i] = text.charAt(i);
        }

        for (int width = 1; width < n; width *= 2) {
            final int step = width;
            final int[] oldRank = rank;
            Arrays.sort(sa, (a, b) -> {
                int first = Integer.compare(oldRank[a], oldRank[b]);
                if (first != 0) {
                    return first;
                }
                int secondA = a + step < n ? oldRank[a + step] : -1;
                int secondB = b + step < n ? oldRank[b + step] : -1;
                return Integer.compare(secondA, secondB);
            });

            int[] nextRank = new int[n];
            for (int i = 1; i < n; i++) {
                int a = sa[i - 1];
                int b = sa[i];
                int secondA = a + width < n ? rank[a + width] : -1;
                int secondB = b + width < n ? rank[b + width] : -1;
                boolean different = rank[a] != rank[b] || secondA != secondB;
                nextRank[b] = nextRank[a] + (different ? 1 : 0);
            }
            rank = nextRank;
            if (rank[sa[n - 1]] == n - 1) {
                break;
            }
        }

        int[] position = new int[n];
        for (int i = 0; i < n; i++) {
            position[sa[i]] = i;
        }
        int common = 0;
        int bestLength = 0;
        int bestStart = 0;
        for (int i = 0; i < n; i++) {
            int index = position[i];
            if (index == 0) {
                common = 0;
                continue;
            }
            int j = sa[index - 1];
            while (i + common < n && j + common < n
                    && text.charAt(i + common) == text.charAt(j + common)) {
                common++;
            }
            lcp[index] = common;
            if (common > bestLength) {
                bestLength = common;
                bestStart = i;
            }
            if (common > 0) {
                common--;
            }
        }
        return Map.of("sa", sa, "lcp", lcp,
                "repeated", text.substring(bestStart, bestStart + bestLength));
    }

    private static class Node {
        int[] next = new int[95];
        int fail;
    }

    static Map<String, Object> aho(Map<String, Object> input) {
        List<?> patterns = (List<?>) input.get("patterns");
        List<?> texts = (List<?>) input.get("texts");
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node());
        int[] terminal = new int[patterns.size()];
        for (int i = 0; i < patterns.size(); i++) {
            String pattern = (String) patterns.get(i);
            int state = 0;
            for (int j = 0; j < pattern.length(); j++) {
                int c = pattern.charAt(j) - 32;
                if (nodes.get(state).next[c] == 0) {
                    nodes.get(state).next[c] = nodes.size();
                    nodes.add(new Node());
                }
                state = nodes.get(state).next[c];
            }
            terminal[i] = state;
        }

        int[] order = new int[nodes.size()];
        int head = 0;
        int tail = 0;
        for (int c = 0; c < 95; c++) {
            int child = nodes.get(0).next[c];
            if (child != 0) {
                order[tail++] = child;
            }
        }
        while (head < tail) {
            int v = order[head++];
            Node node = nodes.get(v);
            Node fallback = nodes.get(node.fail);
            for (int c = 0; c < 95; c++) {
                int child = node.next[c];
                if (child != 0) {
                    nodes.get(child).fail = fallback.next[c];
                    order[tail++] = child;
                } else {
                    node.next[c] = fallback.next[c];
                }
            }
        }

        List<long[]> counts = new ArrayList<>();
        for (Object item : texts) {
            String text = (String) item;
            long[] visits = new long[nodes.size()];
            int state = 0;
            for (int i = 0; i < text.length(); i++) {
                state = nodes.get(state).next[text.charAt(i) - 32];
                visits[state]++;
            }
            for (int i = tail - 1; i >= 0; i--) {
                int v = order[i];
                visits[nodes.get(v).fail] += visits[v];
            }
            long[] row = new long[patterns.size()];
            for (int i = 0; i < patterns.size(); i++) {
                row[i] = visits[terminal[i]];
            }
            counts.add(row);
        }
        return Map.of("counts", counts);
    }
}
