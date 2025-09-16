import java.io.*;
import java.util.*;

public class Main {
    static class FastInput {
        BufferedReader br;
        StringTokenizer st;
        FastInput(InputStream in) {
            br = new BufferedReader(new InputStreamReader(in));
        }
        String nextToken() throws IOException {
            while (st == null || !st.hasMoreTokens()) {
                String line = br.readLine();
                if (line == null) return null;
                st = new StringTokenizer(line);
            }
            return st.nextToken();
        }
        int nextInt() throws IOException {
            return Integer.parseInt(nextToken());
        }
    }

    static int n, m;
    static List<Edge>[] adj;
    static int[] tin, low, subSize;
    static boolean[] visited;
    static int timer;
    static long bestProduct;

    static class Edge {
        int to, id;
        Edge(int _to, int _id) {
            to = _to; id = _id;
        }
    }

    static void dfs(int u, int parentEdgeId) {
        visited[u] = true;
        tin[u] = low[u] = ++timer;
        subSize[u] = 1;

        for (Edge e : adj[u]) {
            int v = e.to, eid = e.id;
            if (eid == parentEdgeId) continue;
            if (visited[v]) {
                // back-edge
                low[u] = Math.min(low[u], tin[v]);
            } else {
                // tree-edge
                dfs(v, eid);
                subSize[u] += subSize[v];
                low[u] = Math.min(low[u], low[v]);
                // check bridge condition
                if (low[v] > tin[u]) {
                    long A = subSize[v];
                    long B = n - A;
                    bestProduct = Math.max(bestProduct, A * B);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        // Increase stack size by running solve() in a dedicated thread
        Thread thread = new Thread(null, () -> {
            try {
                solve();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "solver", 1 << 26);
        thread.start();
        thread.join();
    }

    static void solve() throws IOException {
        FastInput in = new FastInput(System.in);
        PrintWriter out = new PrintWriter(System.out);

        int t = in.nextInt();
        while (t-- > 0) {
            n = in.nextInt();
            m = in.nextInt();

            adj = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) {
                adj[i] = new ArrayList<>();
            }
            for (int i = 0; i < m; i++) {
                int u = in.nextInt();
                int v = in.nextInt();
                adj[u].add(new Edge(v, i));
                adj[v].add(new Edge(u, i));
            }

            tin = new int[n+1];
            low = new int[n+1];
            subSize = new int[n+1];
            visited = new boolean[n+1];
            timer = 0;
            bestProduct = 0;

            // Graph is guaranteed connected, so one DFS from node 1 suffices:
            dfs(1, -1);

            long totalPairs = (long) n * (n - 1) / 2;
            long answer = totalPairs - bestProduct;
            out.println(answer);
        }

        out.flush();
    }
}