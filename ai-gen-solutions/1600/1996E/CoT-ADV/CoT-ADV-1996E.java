import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 1_000_000_007;

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(reader.readLine().trim());
        StringBuilder output = new StringBuilder();

        while (t-- > 0) {
            String s = reader.readLine().trim();
            int n = s.length();

            // Compute prefix sums: +1 for '1', -1 for '0'
            int[] P = new int[n + 1];
            for (int i = 1; i <= n; i++) {
                P[i] = P[i - 1] + (s.charAt(i - 1) == '1' ? 1 : -1);
            }

            // Group indices by prefix sum value
            // Map: sumValue -> list of positions i where P[i] == sumValue
            Map<Integer, ArrayList<Integer>> map = new HashMap<>();
            for (int i = 0; i <= n; i++) {
                map.computeIfAbsent(P[i], k -> new ArrayList<>()).add(i);
            }

            long answer = 0L;

            // Process each group
            for (ArrayList<Integer> positions : map.values()) {
                int m = positions.size();
                if (m < 2) continue;  // need at least 2 to form a pair

                // Build suffix sums of (n - pos + 1)
                long[] suffix = new long[m + 1];
                suffix[m] = 0;
                for (int j = m - 1; j >= 0; j--) {
                    long val = (n - positions.get(j) + 1L) % MOD;
                    suffix[j] = (suffix[j + 1] + val) % MOD;
                }

                // Sum contributions
                for (int i = 0; i < m - 1; i++) {
                    long leftChoices = (positions.get(i) + 1L) % MOD;
                    long waysRight = suffix[i + 1];  // all suffix sums from i+1
                    answer = (answer + leftChoices * waysRight) % MOD;
                }
            }

            output.append(answer).append("\n");
        }

        // Print all answers
        System.out.print(output);
    }
}