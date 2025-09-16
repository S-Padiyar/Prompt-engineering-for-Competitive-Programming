import java.io.*;
import java.util.*;

public class Main {
    // Maximum total nodes across all tests
    static final int MAXN = 200_000 + 5;
    
    // Adjacency via edge‐lists
    static int[] head = new int[MAXN];
    static int[] to   = new int[2 * MAXN];
    static int[] nxt  = new int[2 * MAXN];
    static int edgeCnt;

    // Tree DP arrays
    static long[] a   = new long[MAXN];
    static long[] dp0 = new long[MAXN];
    static long[] dp1 = new long[MAXN];
    static int[] parent = new int[MAXN];
    static int[] order  = new int[MAXN];
    static int[] stack  = new int[MAXN];

    public static void main(String[] args) throws IOException {
        BufferedReader br  = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter    out = new PrintWriter(System.out);
        StringTokenizer st = new StringTokenizer(br.readLine());
        int t = Integer.parseInt(st.nextToken());

        while (t-- > 0) {
            // Read n and c
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            long c = Long.parseLong(st.nextToken());

            // Read the gold at each node
            st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }

            // Build the adjacency lists
            // Clear old lists
            for (int i = 1; i <= n; i++) {
                head[i] = -1;
            }
            edgeCnt = 0;

            // Read edges
            for (int i = 0; i < n - 1; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                addEdge(u, v);
                addEdge(v, u);
            }

            // 1) Build a DFS order (parent pointers + an array 'order' in preorder)
            int sp = 0;
            stack[sp++] = 1;
            parent[1] = 0;
            int ordSz = 0;
            while (sp > 0) {
                int u = stack[--sp];
                order[ordSz++] = u;
                for (int e = head[u]; e != -1; e = nxt[e]) {
                    int v = to[e];
                    if (v != parent[u]) {
                        parent[v] = u;
                        stack[sp++] = v;
                    }
                }
            }

            // 2) Process in reverse order: children before parents
            for (int i = ordSz - 1; i >= 0; i--) {
                int u = order[i];

                // If we do NOT select u
                long best0 = 0;
                // If we DO select u
                long best1 = a[u];

                for (int e = head[u]; e != -1; e = nxt[e]) {
                    int v = to[e];
                    if (v == parent[u]) continue;

                    // for dp0[u], we can pick v or not pick v without penalty
                    best0 += Math.max(dp0[v], dp1[v]);

                    // for dp1[u], if we pick v we pay 2c for the edge (u,v)
                    long pickV = dp1[v] - 2 * c;
                    best1 += Math.max(dp0[v], pickV);
                }

                dp0[u] = best0;
                dp1[u] = best1;
            }

            // The answer is max(dp0[1], dp1[1]), clamped >= 0
            long ans = Math.max(dp0[1], dp1[1]);
            if (ans < 0) ans = 0;
            out.println(ans);
        }

        out.flush();
    }

    static void addEdge(int u, int v) {
        to[edgeCnt] = v;
        nxt[edgeCnt] = head[u];
        head[u]     = edgeCnt++;
    }
}