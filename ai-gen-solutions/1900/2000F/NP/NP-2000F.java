import java.io.*;
import java.util.*;

public class Main {
    static final int INF = 1_000_000_000;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int k = Integer.parseInt(st.nextToken());

            int[] A = new int[n], B = new int[n];
            long sumCap = 0;
            for (int i = 0; i < n; i++) {
                st = new StringTokenizer(br.readLine());
                A[i] = Integer.parseInt(st.nextToken());
                B[i] = Integer.parseInt(st.nextToken());
                // maximum points from rect i is A[i] + B[i], cap at k
                sumCap += Math.min(k, A[i] + B[i]);
            }

            // Quick infeasibility check
            if (sumCap < k) {
                sb.append(-1).append('\n');
                continue;
            }

            // F[j] = min cost to get exactly j points (j=0..k), we will fold ≥k into index k
            int[] F = new int[k + 1];
            Arrays.fill(F, INF);
            F[0] = 0;

            // Process each rectangle
            for (int i = 0; i < n; i++) {
                int a = A[i], b = B[i];
                int cap = Math.min(k, a + b);

                // Build dp_i[0..cap], dp_i[p] = min cost to get p points from this rectangle
                int[] dp = new int[cap + 1];
                Arrays.fill(dp, INF);
                dp[0] = 0;
                // For each p = x+y, 0 ≤ x ≤ b, 0 ≤ y ≤ a
                for (int p = 1; p <= cap; p++) {
                    int lo = Math.max(0, p - a);
                    int hi = Math.min(p, b);
                    int best = INF;
                    for (int x = lo; x <= hi; x++) {
                        int y = p - x;
                        // cost = x*a + y*b - x*y
                        int cost = x * a + y * b - x * y;
                        if (cost < best) best = cost;
                    }
                    dp[p] = best;
                }

                // Merge into the global DP F -> G
                int[] G = new int[k + 1];
                Arrays.fill(G, INF);

                for (int p = 0; p <= cap; p++) {
                    if (dp[p] == INF) continue;
                    for (int j = 0; j <= k; j++) {
                        if (F[j] == INF) continue;
                        int nj = j + p >= k ? k : j + p;
                        int cost = F[j] + dp[p];
                        if (cost < G[nj]) {
                            G[nj] = cost;
                        }
                    }
                }

                // Move G back into F
                F = G;
            }

            int ans = F[k];
            if (ans >= INF) ans = -1;
            sb.append(ans).append('\n');
        }

        System.out.print(sb);
    }
}