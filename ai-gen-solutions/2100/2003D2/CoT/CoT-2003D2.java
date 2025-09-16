import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        StringTokenizer stk;

        int t = Integer.parseInt(in.readLine());
        while (t-- > 0) {
            stk = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(stk.nextToken());
            long m = Long.parseLong(stk.nextToken());

            // We will collect (mu -> p) edges
            ArrayList<int[]> edges = new ArrayList<>();
            int maxNode = 0;

            for (int i = 0; i < n; i++) {
                stk = new StringTokenizer(in.readLine());
                int li = Integer.parseInt(stk.nextToken());
                int[] a = new int[li];
                for (int j = 0; j < li; j++) {
                    a[j] = Integer.parseInt(stk.nextToken());
                }

                // sort + unique
                Arrays.sort(a);
                int write = 1;
                for (int j = 1; j < li; j++) {
                    if (a[j] != a[j - 1]) {
                        a[write++] = a[j];
                    }
                }
                // now unique sorted prefix is a[0..write-1)
                // find mex = mu
                int mu = 0;
                while (mu < write && a[mu] == mu) {
                    mu++;
                }

                // find next missing > mu
                long p = (long)mu + 1;
                int idx = mu; 
                // we know a[idx] > mu or idx == write
                while (idx < write && a[idx] == p) {
                    p++;
                    idx++;
                }

                // record edge mu -> p
                edges.add(new int[]{mu, (int)p});
                maxNode = Math.max(maxNode, Math.max(mu, (int)p));
            }

            // build adjacency
            List<List<Integer>> adj = new ArrayList<>();
            for (int i = 0; i <= maxNode; i++) {
                adj.add(new ArrayList<>());
            }
            for (int[] e : edges) {
                adj.get(e[0]).add(e[1]);
            }

            // dp[v] = max reachable from v
            int[] dp = new int[maxNode + 1];
            for (int v = maxNode; v >= 0; v--) {
                int best = v;
                for (int u : adj.get(v)) {
                    best = Math.max(best, dp[u]);
                }
                dp[v] = best;
            }

            // sum up f(k)
            long ans = 0;
            int upTo = (int)Math.min(m, maxNode);
            for (int k = 0; k <= upTo; k++) {
                ans += dp[k];
            }
            if (m > maxNode) {
                // add sum of k for k = maxNode+1 .. m
                long R = m, L = maxNode + 1;
                long sumR = R * (R + 1) / 2;
                long sumL = (L - 1) * L / 2;
                ans += (sumR - sumL);
            }

            sb.append(ans).append('\n');
        }
        System.out.print(sb.toString());
    }
}