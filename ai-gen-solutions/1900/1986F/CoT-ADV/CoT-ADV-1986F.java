import java.io.*;
import java.util.*;

public class Main {
    static int n, m;
    static ArrayList<Edge>[] adj;
    static int[] tin, low, subSize;
    static boolean[] visited;
    static int timer;
    static long maxCut;  // maximum a*(n-a) over all bridges

    static class Edge {
        int to, id;
        Edge(int _to, int _id) { to = _to; id = _id; }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder out = new StringBuilder();
        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            n = Integer.parseInt(st.nextToken());
            m = Integer.parseInt(st.nextToken());

            // Build adjacency list
            adj = new ArrayList[n + 1];
            for (int i = 1; i <= n; i++) {
                adj[i] = new ArrayList<>();
            }
            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(in.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                adj[u].add(new Edge(v, i));
                adj[v].add(new Edge(u, i));
            }

            // Initialize arrays for Tarjan DFS
            tin = new int[n + 1];
            low = new int[n + 1];
            subSize = new int[n + 1];
            visited = new boolean[n + 1];
            timer = 0;
            maxCut = 0L;

            // Run DFS from node 1 (graph is connected)
            dfs(1, -1);

            // Total possible pairs
            long totalPairs = (1L * n * (n - 1)) / 2;
            long answer = totalPairs - maxCut;
            out.append(answer).append('\n');
        }

        System.out.print(out);
    }

    // DFS to compute tin[], low[], subtree sizes, and detect bridges
    static void dfs(int u, int parentEdgeId) {
        visited[u] = true;
        tin[u] = low[u] = timer++;
        subSize[u] = 1;

        for (Edge e : adj[u]) {
            int v = e.to;
            if (e.id == parentEdgeId) continue;
            if (visited[v]) {
                // back-edge
                low[u] = Math.min(low[u], tin[v]);
            } else {
                // tree-edge
                dfs(v, e.id);
                subSize[u] += subSize[v];
                low[u] = Math.min(low[u], low[v]);
                // Check for bridge
                if (low[v] > tin[u]) {
                    long a = subSize[v];
                    long b = (long) n - a;
                    maxCut = Math.max(maxCut, a * b);
                }
            }
        }
    }
}