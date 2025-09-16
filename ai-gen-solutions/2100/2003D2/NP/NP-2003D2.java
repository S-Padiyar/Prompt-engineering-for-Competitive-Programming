import java.io.*;
import java.util.*;

public class Main {
    static final int MAXV = 200005;  // upper bound for all base-mex values

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer stk;

        int t = Integer.parseInt(br.readLine());
        // Adjacency lists for edges v -> [u1, u2, ...]
        ArrayList<Integer>[] edges = new ArrayList[MAXV];
        for (int i = 0; i < MAXV; i++) {
            edges[i] = new ArrayList<>();
        }

        // To know which lists we need to clear after each test
        ArrayList<Integer> usedV = new ArrayList<>();

        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            stk = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(stk.nextToken());
            long m = Long.parseLong(stk.nextToken());

            // Track the maximum base-mex (t0) seen
            int maxBaseMex = 0;

            // Build the graph edges
            for (int i = 0; i < n; i++) {
                stk = new StringTokenizer(br.readLine());
                int len = Integer.parseInt(stk.nextToken());
                int[] arr = new int[len];
                for (int j = 0; j < len; j++) {
                    arr[j] = Integer.parseInt(stk.nextToken());
                }

                // Sort & dedupe
                Arrays.sort(arr);
                int write = 0;
                for (int j = 1; j < len; j++) {
                    if (arr[j] != arr[write]) {
                        arr[++write] = arr[j];
                    }
                }
                len = write + 1;

                // 1) find t0 = mex(A_i)
                int t0 = 0, idx = 0;
                while (idx < len && arr[idx] == t0) {
                    idx++;
                    t0++;
                }

                // 2) find t1 = next missing > t0
                int t1 = t0 + 1;
                int p = 0;
                while (p < len && arr[p] < t1) {
                    if (arr[p] == t1) {
                        t1++;
                    }
                    p++;
                }

                // update global maximum base-mex
                maxBaseMex = Math.max(maxBaseMex, t0);

                // 3) edges v->t0 for v in A_i with v<t0
                idx = 0;
                while (idx < len && arr[idx] < t0) {
                    int v = arr[idx++];
                    // add edge v->t0
                    if (edges[v].isEmpty()) {
                        usedV.add(v);
                    }
                    edges[v].add(t0);
                }
                // 4) edge t0->t1
                if (t0 < MAXV) {
                    if (edges[t0].isEmpty()) {
                        usedV.add(t0);
                    }
                    edges[t0].add(t1);
                }
            }

            // Prepare DP array
            long[] dp = new long[maxBaseMex + 1];

            // 5) Compute dp[v] in descending order
            for (int v = maxBaseMex; v >= 0; v--) {
                long best = v;
                for (int u : edges[v]) {
                    long candidate = (u <= maxBaseMex ? dp[u] : u);
                    if (candidate > best) {
                        best = candidate;
                    }
                }
                dp[v] = best;
            }

            // 6) Sum up f(0)+...+f(m)
            long up = Math.min(m, maxBaseMex);
            long ans = 0;
            for (int x = 0; x <= up; x++) {
                ans += dp[x];
            }
            if (m > maxBaseMex) {
                // sum of k = maxBaseMex+1 .. m
                long M = maxBaseMex;
                long total = (m * (m + 1) / 2) - (M * (M + 1) / 2);
                ans += total;
            }

            sb.append(ans).append('\n');

            // Clear adjacency for next test
            for (int v : usedV) {
                edges[v].clear();
            }
            usedV.clear();
        }

        System.out.print(sb);
    }
}