import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();

        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            long k = Long.parseLong(st.nextToken());

            st = new StringTokenizer(in.readLine());
            long[] a = new long[n];
            for (int i = 0; i < n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }

            // Step 1.  Group values by residue class mod k.
            // We'll sort by (r = a[i] mod k, then by a[i]) in one pass.
            Pair[] pairs = new Pair[n];
            for (int i = 0; i < n; i++) {
                long r = a[i] % k;
                pairs[i] = new Pair(r, a[i]);
            }
            Arrays.sort(pairs, Comparator.comparingLong((Pair p) -> p.r)
                                        .thenComparingLong(p -> p.v));

            // Build each residue-group in turn.
            long totalCost = 0;
            int oddGroups = 0;
            List<Long> current = new ArrayList<>();

            int idx = 0;
            while (idx < n) {
                int start = idx;
                long r = pairs[idx].r;
                current.clear();
                // Collect all of this residue
                while (idx < n && pairs[idx].r == r) {
                    current.add(pairs[idx].v);
                    idx++;
                }
                // Now current holds one residue-group:
                int m = current.size();
                if ((m & 1) == 1) {
                    oddGroups++;
                }
            }

            // Feasibility check:
            // If n even => no odd-size group allowed.
            // If n odd  => exactly 1 odd-size group allowed.
            if ((n % 2 == 0 && oddGroups > 0) ||
                (n % 2 == 1 && oddGroups != 1)) {
                sb.append(-1).append('\n');
                continue;
            }

            // Second pass: actually compute the cost.
            idx = 0;
            while (idx < n) {
                long r = pairs[idx].r;
                current.clear();
                while (idx < n && pairs[idx].r == r) {
                    current.add(pairs[idx].v);
                    idx++;
                }
                int m = current.size();
                // Convert to a simple array and sort (already sorted by v).
                long[] v = new long[m + 1];  // 1-based for ease
                for (int i = 1; i <= m; i++) {
                    v[i] = current.get(i - 1);
                }

                if ((m & 1) == 0) {
                    // Even-size group: just pair neighbors (1,2),(3,4),...
                    long cost = 0;
                    for (int i = 1; i <= m; i += 2) {
                        cost += (v[i + 1] - v[i]) / k;
                    }
                    totalCost += cost;
                } else {
                    // Odd-size group: we must remove one element as center,
                    // then pair the rest optimally.
                    // We'll do an O(m) sweep with prefix/suffix sums.

                    // 1) Build the "difference in k-steps" array dp[j] = (v[j] - v[j-1]) / k.
                    long[] dp = new long[m + 2];
                    for (int j = 2; j <= m; j++) {
                        dp[j] = (v[j] - v[j - 1]) / k;
                    }

                    // 2) prefix-sum of the "standard pairing" (1,2),(3,4),...
                    long[] pref = new long[m + 2];
                    pref[0] = 0;
                    pref[1] = 0;
                    for (int j = 2; j <= m; j++) {
                        if ((j & 1) == 0) {
                            // j even => that completes a pair (j-1, j)
                            pref[j] = pref[j - 2] + dp[j];
                        } else {
                            // j odd => same as pref[j-1]
                            pref[j] = pref[j - 1];
                        }
                    }

                    // 3) suffix-sum if we pair starting at index i: (i,i+1),(i+2,i+3),...
                    long[] suf = new long[m + 3];
                    suf[m + 1] = 0;
                    suf[m + 2] = 0;
                    for (int i2 = m; i2 >= 1; i2--) {
                        if (i2 + 1 <= m) {
                            suf[i2] = dp[i2 + 1] + ((i2 + 2 <= m) ? suf[i2 + 2] : 0);
                        } else {
                            suf[i2] = 0;
                        }
                    }

                    // 4) Try removing each position t = 1..m as the center.
                    long best = Long.MAX_VALUE;
                    for (int tpos = 1; tpos <= m; tpos++) {
                        long cost = 0;
                        if ((tpos & 1) == 1) {
                            // If tpos odd, the leftover pairs are exactly
                            // {1,2},{3,4},..., skipping the block containing tpos.
                            long left = pref[tpos - 1];
                            long right = (tpos + 1 <= m ? suf[tpos + 1] : 0);
                            cost = left + right;
                        } else {
                            // If tpos even, there's a crossing pair (tpos-1,tpos+1)
                            // + standard prefix before tpos-1 and suffix after tpos+1.
                            long left = (tpos - 2 >= 1 ? pref[tpos - 2] : 0);
                            long middle = dp[tpos] +
                                          ((tpos + 1 <= m) ? dp[tpos + 1] : 0);
                            long right = (tpos + 2 <= m ? suf[tpos + 2] : 0);
                            cost = left + middle + right;
                        }
                        if (cost < best) best = cost;
                    }
                    totalCost += best;
                }
            }

            sb.append(totalCost).append('\n');
        }

        System.out.print(sb);
    }

    // Simple helper to store (residue, value) pairs.
    static class Pair {
        long r, v;
        Pair(long r, long v) {
            this.r = r;
            this.v = v;
        }
    }
}