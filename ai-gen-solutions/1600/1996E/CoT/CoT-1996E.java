import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 1_000_000_007;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            String s = br.readLine().trim();
            int n = s.length();

            // Compute prefix sums: +1 for '1', -1 for '0'
            int[] pref = new int[n+1];
            pref[0] = 0;
            for (int i = 0; i < n; i++) {
                pref[i+1] = pref[i] + (s.charAt(i)=='1' ? +1 : -1);
            }

            // Group positions by prefix sum value
            // key = prefix sum, value = list of positions where that sum occurs
            Map<Integer, ArrayList<Integer>> groups = new HashMap<>();
            for (int i = 0; i <= n; i++) {
                groups
                  .computeIfAbsent(pref[i], k -> new ArrayList<>())
                  .add(i);
            }

            // For each group of positions p0 < p1 < ... < p_{m-1},
            // sum over all a < b of (p[a]+1)*(n - p[b] + 1)
            long answer = 0L;
            for (ArrayList<Integer> list : groups.values()) {
                int m = list.size();
                if (m < 2) continue;  // no pairs to form

                // We'll do one backward pass keeping a suffix sum of (n - p[b] + 1)
                long suffixSum = 0;
                for (int idx = m - 1; idx >= 0; idx--) {
                    int p = list.get(idx);
                    if (idx < m - 1) {
                        // All b > idx have contributed to suffixSum
                        long leftFactor = (p + 1L) % MOD;
                        answer = (answer + leftFactor * suffixSum) % MOD;
                    }
                    long rightTerm = (n - p + 1L) % MOD;
                    suffixSum = (suffixSum + rightTerm) % MOD;
                }
            }

            out.println(answer);
        }

        out.flush();
    }
}