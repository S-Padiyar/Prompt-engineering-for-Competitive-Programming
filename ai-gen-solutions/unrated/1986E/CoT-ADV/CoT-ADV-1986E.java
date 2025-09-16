import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        int T = Integer.parseInt(br.readLine().trim());
        final long INF = Long.MAX_VALUE / 4;

        for (int tc = 0; tc < T; tc++) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int k = Integer.parseInt(st.nextToken());
            int[] a = new int[n];
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            // Group elements by residue mod k
            HashMap<Integer, ArrayList<Integer>> groups = new HashMap<>();
            for (int x : a) {
                int r = x % k;
                groups.computeIfAbsent(r, __ -> new ArrayList<>()).add(x);
            }

            // Count how many groups have odd size
            int oddCount = 0;
            for (ArrayList<Integer> g : groups.values()) {
                if ((g.size() & 1) == 1) {
                    oddCount++;
                }
            }
            // Feasibility check
            boolean ok;
            if (n % 2 == 0) {
                ok = (oddCount == 0);
            } else {
                ok = (oddCount == 1);
            }
            if (!ok) {
                out.println(-1);
                continue;
            }

            long totalCost = 0;

            // Process each residue-group
            for (ArrayList<Integer> g : groups.values()) {
                int m = g.size();
                Collections.sort(g);
                // Even-sized group -> pair adjacent
                if ((m & 1) == 0) {
                    for (int i = 1; i < m; i += 2) {
                        long diff = g.get(i) - g.get(i - 1);
                        totalCost += diff / k;
                    }
                } else {
                    // Odd-sized: do the DP to leave exactly one unmatched
                    long[] dp0 = new long[m];
                    long[] dp1 = new long[m];
                    // Base cases
                    dp0[0] = INF;    // can't perfectly pair just one element
                    dp1[0] = 0;      // leave the first as the unmatched

                    for (int i = 1; i < m; i++) {
                        long diffOps = (g.get(i) - g.get(i - 1)) / k;
                        // dp0[i]
                        long best0 = INF;
                        if (i >= 2) {
                            best0 = dp0[i - 2];
                        } else {
                            // i==1 => pairing v0,v1 on empty prefix => cost = dp0[-1] + diffOps
                            best0 = 0;
                        }
                        dp0[i] = best0 + diffOps;

                        // dp1[i]
                        long best1 = INF;
                        // Option 1: pair (i-1,i), one unmatched was already used in prefix
                        if (i >= 2) {
                            best1 = dp1[i - 2] + diffOps;
                        }
                        // Option 2: leave v[i] itself as the unmatched (use up the one)
                        best1 = Math.min(best1, dp0[i - 1]);
                        dp1[i] = best1;
                    }
                    // final cost = dp1[m-1]
                    totalCost += dp1[m - 1];
                }
            }

            out.println(totalCost);
        }

        out.flush();
    }
}