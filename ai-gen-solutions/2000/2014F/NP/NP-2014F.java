import java.io.*;
import java.util.*;

public class Main {
    static final int MAXN = 200_000;
    static int[] head = new int[MAXN + 5];
    static int[] to   = new int[2 * MAXN + 5];
    static int[] nxt  = new int[2 * MAXN + 5];
    static long[] dp0 = new long[MAXN + 5];
    static long[] dp1 = new long[MAXN + 5];
    static int[] parent = new int[MAXN + 5];
    static int[] order  = new int[MAXN + 5];
    static int[] queue  = new int[MAXN + 5];
    static long[] A     = new long[MAXN + 5];
    static int edgeCnt;

    public static void main(String[] args) throws IOException {
        FastReader fr = new FastReader();
        PrintWriter pw = new PrintWriter(System.out);

        int t = fr.nextInt();
        while (t-- > 0) {
            int n = fr.nextInt();
            long c = fr.nextLong();

            // initialize adjacency
            for (int i = 1; i <= n; i++) {
                head[i] = -1;
            }
            edgeCnt = 0;

            // read gold amounts
            for (int i = 1; i <= n; i++) {
                A[i] = fr.nextLong();
            }

            // read edges
            for (int i = 0; i < n - 1; i++) {
                int u = fr.nextInt();
                int v = fr.nextInt();
                addEdge(u, v);
                addEdge(v, u);
            }

            // BFS to root at 1, record parent and BFS order
            int front = 0, back = 0;
            queue[back++] = 1;
            parent[1] = 0;
            order[0] = 1;
            int ordSize = 1;

            while (front < back) {
                int v = queue[front++];
                for (int e = head[v]; e != -1; e = nxt[e]) {
                    int u = to[e];
                    if (u == parent[v]) continue;
                    parent[u] = v;
                    queue[back++] = u;
                    order[ordSize++] = u;
                }
            }

            // DP in reverse BFS order (children before parent)
            for (int idx = ordSize - 1; idx >= 0; idx--) {
                int v = order[idx];
                long take0 = 0;         // v not chosen
                long take1 = A[v];      // v chosen

                for (int e = head[v]; e != -1; e = nxt[e]) {
                    int u = to[e];
                    if (u == parent[v]) continue;
                    // if v not chosen, child u may be chosen or not
                    take0 += Math.max(dp0[u], dp1[u]);
                    // if v chosen, edge (v,u) penalizes 2c if u is also chosen
                    long option0 = dp0[u];
                    long option1 = dp1[u] - 2 * c;
                    take1 += Math.max(option0, option1);
                }

                dp0[v] = take0;
                dp1[v] = take1;
            }

            long ans = Math.max(dp0[1], dp1[1]);
            pw.println(ans);
        }

        pw.close();
    }

    static void addEdge(int u, int v) {
        to[edgeCnt] = v;
        nxt[edgeCnt] = head[u];
        head[u] = edgeCnt++;
    }

    // Fast I/O
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;

        public FastReader() throws IOException {
            br = new BufferedReader(
                   new InputStreamReader(System.in));
        }

        String next() throws IOException {
            while (st == null || !st.hasMoreTokens()) {
                String s = br.readLine();
                if (s == null) return null;
                st = new StringTokenizer(s);
            }
            return st.nextToken();
        }

        int nextInt() throws IOException {
            return Integer.parseInt(next());
        }

        long nextLong() throws IOException {
            return Long.parseLong(next());
        }
    }
}