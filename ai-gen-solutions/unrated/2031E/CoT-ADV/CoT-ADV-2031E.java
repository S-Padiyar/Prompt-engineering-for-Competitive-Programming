import java.io.*;
import java.util.*;

public class Main {
    static final int MAX_SMALL = 20; 
    // We only need to check deadlines up to 20, since 2^(20+1)-2 > 1e6.

    public static void main(String[] args) throws IOException {
        // Fast I/O
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        String line = br.readLine();
        int t = Integer.parseInt(line.trim());
        
        // Precompute small capacities capSum[d] = sum_{i=1..d} 2^i = 2^{d+1}-2
        long[] capSum = new long[MAX_SMALL+1];
        for (int d = 0; d <= MAX_SMALL; d++) {
            capSum[d] = ((1L << (d+1)) - 2L);
            if (capSum[d] < 0) capSum[d] = Long.MAX_VALUE; 
            // just in case shifting beyond 63 bits
        }

        // Process each test case
        while (t-- > 0) {
            line = br.readLine();
            int n = Integer.parseInt(line.trim());
            // Read parents
            int[] parent = new int[n+1];
            String[] parts = br.readLine().trim().split("\\s+");
            for (int i = 2; i <= n; i++) {
                parent[i] = Integer.parseInt(parts[i-2]);
            }
            // Build children adjacency
            ArrayList<Integer>[] children = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) {
                children[i] = new ArrayList<>();
            }
            for (int i = 2; i <= n; i++) {
                children[parent[i]].add(i);
            }

            // dp[v] = minimal height h of a perfect subtree needed to host v's subtree.
            int[] dp = new int[n+1];

            // Process nodes in reverse order  n, n-1, ..., 1
            // Since parent[i]<i, this guarantees all children of i are processed before i.
            for (int v = n; v >= 1; v--) {
                if (children[v].isEmpty()) {
                    // Leaf => no height needed
                    dp[v] = 0;
                } else {
                    int k = children[v].size();
                    // Gather children dps
                    int[] w = new int[k];
                    for (int i = 0; i < k; i++) {
                        w[i] = dp[ children[v].get(i) ];
                    }
                    // Sort descending
                    Arrays.sort(w);
                    // We'll read from the back
                    // w[k-1] highest, w[k-2] next, ... w[0] smallest

                    // Lower bound on h is max w_i + 1
                    int wmax = w[k-1];
                    int lo = wmax + 1;
                    // Upper bound we only need + MAX_SMALL
                    int hi = lo + MAX_SMALL;

                    // Binary‐search the minimal h in [lo..hi]
                    while (lo < hi) {
                        int mid = (lo + hi) >>> 1;
                        // EDF‐type check
                        boolean ok = true;
                        // We'll assign the "i-th child in deadline order" to check i <= capSum(limit_i)
                        // Our deadline array is limit_i = mid - w_i, and sorted so tightest (smallest limit) first => largest w first
                        // So i=1 => w[k-1], i=2 => w[k-2], ...
                        for (int i = 1; i <= k; i++) {
                            int wi = w[k - i]; 
                            int lim = mid - wi;
                            if (lim < 1) {
                                ok = false;
                                break;
                            }
                            // Cap the limit to MAX_SMALL to index our array
                            if (lim > MAX_SMALL) lim = MAX_SMALL;
                            if ((long)i > capSum[lim]) {
                                ok = false;
                                break;
                            }
                        }
                        if (ok) {
                            hi = mid;
                        } else {
                            lo = mid + 1;
                        }
                    }
                    dp[v] = lo;
                }
            }

            // The root's dp[1] is the answer
            out.println(dp[1]);
        }

        out.flush();
    }
}