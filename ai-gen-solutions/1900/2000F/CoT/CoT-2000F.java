import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer tok = new StringTokenizer(in.readLine());
        int t = Integer.parseInt(tok.nextToken());

        final int INF = 1_000_000_000;
        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            tok = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(tok.nextToken());
            int k = Integer.parseInt(tok.nextToken());

            int[] a = new int[n], b = new int[n];
            for (int i = 0; i < n; i++) {
                tok = new StringTokenizer(in.readLine());
                a[i] = Integer.parseInt(tok.nextToken());
                b[i] = Integer.parseInt(tok.nextToken());
            }

            // dp[j] = minimal cost to get exactly j points so far
            int[] dp = new int[k+1];
            Arrays.fill(dp, INF);
            dp[0] = 0;

            // Process each rectangle
            for (int i = 0; i < n; i++) {
                // Precompute cost_i[p] = minimal operations to get exactly p points
                // from rectangle i (width = a[i], height = b[i]).
                int maxP = Math.min(k, a[i] + b[i]);
                int[] cost = new int[maxP+1];
                Arrays.fill(cost, INF);
                cost[0] = 0;  // zero rows + zero columns = 0 cost

                // Try all ways x columns + y rows = p
                for (int x = 0; x <= a[i]; x++) {
                    for (int y = 0; y <= b[i]; y++) {
                        int p = x + y;
                        if (p > maxP) continue;
                        // cost to paint x columns + y rows, minus double‐count
                        int c = x * b[i] + y * a[i] - x * y;
                        if (c < cost[p]) {
                            cost[p] = c;
                        }
                    }
                }

                // knapsack update: produce newdp
                int[] newdp = new int[k+1];
                Arrays.fill(newdp, INF);

                for (int got = 0; got <= k; got++) {
                    if (dp[got] == INF) continue;
                    // either take 0..maxP points from this rectangle
                    for (int p = 0; p <= maxP; p++) {
                        int c = cost[p];
                        if (c == INF) continue;
                        int nxt = got + p;
                        if (nxt > k) nxt = k;  // cap at k
                        int v = dp[got] + c;
                        if (v < newdp[nxt]) {
                            newdp[nxt] = v;
                        }
                    }
                }

                dp = newdp;
            }

            int ans = dp[k];
            sb.append(ans >= INF ? -1 : ans).append('\n');
        }

        System.out.print(sb);
    }
}