import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        int t = Integer.parseInt(br.readLine().trim());

        // We'll reuse these across test cases to avoid reallocation overhead.
        // But since sum(n) <= 2e5, re-creating per test is also acceptable.
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            long c = Long.parseLong(st.nextToken());

            long[] a = new long[n + 1];
            st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }

            // Build adjacency list
            List<Integer>[] adj = new ArrayList[n + 1];
            for (int i = 1; i <= n; i++) {
                adj[i] = new ArrayList<>();
            }
            for (int i = 0; i < n - 1; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                adj[u].add(v);
                adj[v].add(u);
            }

            // BFS to orient the tree, record parent[] and bfsOrder[]
            int[] parent = new int[n + 1];
            int[] bfsOrder = new int[n];
            int head = 0, tail = 0;
            bfsOrder[tail++] = 1;
            parent[1] = 0;  // root has no parent

            while (head < tail) {
                int u = bfsOrder[head++];
                for (int v : adj[u]) {
                    if (v == parent[u]) continue;
                    parent[v] = u;
                    bfsOrder[tail++] = v;
                }
            }

            // dp0[u] = best if u is NOT strengthened
            // dp1[u] = best if u IS strengthened
            long[] dp0 = new long[n + 1];
            long[] dp1 = new long[n + 1];

            // Process nodes in reverse BFS order => children before parents
            for (int idx = n - 1; idx >= 0; idx--) {
                int u = bfsOrder[idx];
                long best0 = 0;       // sum for dp0[u]
                long best1 = a[u];    // start with a[u] for dp1[u]

                for (int v : adj[u]) {
                    if (v == parent[u]) continue;
                    // If u is NOT picked, child v can be picked or not, no penalty
                    best0 += Math.max(dp0[v], dp1[v]);
                    // If u IS picked, picking v costs 2c on edge (u,v)
                    best1 += Math.max(dp0[v], dp1[v] - 2L * c);
                }
                dp0[u] = best0;
                dp1[u] = best1;
            }

            // The answer is the best of picking root(1) or not.
            long answer = Math.max(dp0[1], dp1[1]);
            // If it's negative, choosing nobody yields 0
            if (answer < 0) answer = 0L;
            sb.append(answer).append('\n');
        }

        System.out.print(sb);
    }
}