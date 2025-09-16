import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int t = Integer.parseInt(in.readLine().trim());
        final int INF = 1_000_000_000;

        while (t-- > 0) {
            st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int k = Integer.parseInt(st.nextToken());

            // dp[j] = min cost to earn exactly j points so far
            int[] dp = new int[k + 1];
            Arrays.fill(dp, INF);
            dp[0] = 0;

            // Keep track of maximum possible total points
            int sumPoints = 0;

            for (int i = 0; i < n; i++) {
                st = new StringTokenizer(in.readLine());
                int a = Integer.parseInt(st.nextToken());
                int b = Integer.parseInt(st.nextToken());
                sumPoints += a + b;

                // We only need g[t] up to t = min(k, a+b)
                int U = Math.min(k, a + b);
                int[] g = new int[U + 1];
                Arrays.fill(g, INF);
                g[0] = 0;

                // Precompute the best cost g[t] for exactly t points from this rectangle
                for (int tPts = 1; tPts <= U; tPts++) {
                    // We choose r rows and c cols with r + c = tPts
                    // 0 <= r <= b, 0 <= c <= a
                    int best = INF;
                    // r = number of rows
                    int rMin = Math.max(0, tPts - a);
                    int rMax = Math.min(tPts, b);
                    for (int r = rMin; r <= rMax; r++) {
                        int c = tPts - r;
                        int cost = r * a + c * b - r * c; 
                        if (cost < best) best = cost;
                    }
                    g[tPts] = best;
                }

                // Merge g[] into the global dp[] with a group-knapsack step
                for (int j = k; j >= 0; j--) {
                    if (dp[j] == INF) continue;
                    for (int tPts = 1; tPts <= U && j + tPts <= k; tPts++) {
                        int cst = g[tPts];
                        if (cst == INF) continue;
                        dp[j + tPts] = Math.min(dp[j + tPts], dp[j] + cst);
                    }
                }
            }

            // If total possible points < k, impossible
            if (sumPoints < k || dp[k] >= INF) {
                System.out.println(-1);
            } else {
                System.out.println(dp[k]);
            }
        }
    }
}