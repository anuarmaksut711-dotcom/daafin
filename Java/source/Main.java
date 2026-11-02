import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class Main {
    static Object handle(Map<String, Object> input) {
        switch ((String) input.get("task")) {
            case "ping": return Map.of("status", "ready");
            case "lcs": return Algorithms.lcs(input);
            case "merge": return Algorithms.merge(input);
            case "tree": return Algorithms.tree(input);
            case "naive": return Algorithms.naive(input);
            case "kmp": return Algorithms.kmp(input);
            case "rk": return Algorithms.rk(input);
            case "suffix": return Algorithms.suffix(input);
            case "aho": return Algorithms.aho(input);
            default: throw new IllegalArgumentException("Unknown task");
        }
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));
        PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true);
        String line;
        while ((line = reader.readLine()) != null) {
            Map<String, Object> input = (Map<String, Object>) Json.parse(line);
            writer.println(Json.write(handle(input)));
        }
        writer.flush();
    }
}

