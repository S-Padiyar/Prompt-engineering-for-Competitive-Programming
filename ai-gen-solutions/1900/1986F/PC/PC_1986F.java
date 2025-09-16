import java.io.*;
import java.util.*;

public class Main {
    static class StackEntry {
        int type;     // 0 = DFS state; 1 = "post-child" state
        int v, parent, iter, u;
        // For type=0 (DFS): v=vertex, parent=parent, iter=next neighbor index
        // For type=1 (post): v=parent, u=child
        StackEntry(int type, int v, int parent, int iter, int u) {
            this.type = type;
            this.v = v;
            this.parent = parent;
            this.iter = iter;
            this.u = u;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());

            // Build adjacency list
            List<Integer>[] adj = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) {
                adj[i] = new ArrayList<>();
            }
            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(in.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                adj[u].add(v);
                adj[v].add(u);
            }

            // Arrays for Tarjan‐bridge + subtree‐sizes
            int[] tin = new int[n+1], low = new int[n+1], sz = new int[n+1];
            boolean[] visited = new boolean[n+1];
            int timer = 0;
            long bestProduct = 0;

            // We'll do one DFS (the graph is connected)
            ArrayDeque<StackEntry> stack = new ArrayDeque<>();
            // Initial DFS state at vertex 1, parent = -1, next-iter = 0
            stack.push(new StackEntry(0, 1, -1, 0, 0));

            while (!stack.isEmpty()) {
                StackEntry e = stack.pop();
                if (e.type == 1) {
                    // "Post-child" state: we've finished child u of parent v
                    int v = e.v, u = e.u;
                    // Update low-link and subtree size
                    low[v] = Math.min(low[v], low[u]);
                    sz[v] += sz[u];
                    // Bridge check
                    if (low[u] > tin[v]) {
                        long s = sz[u];
                        long prod = s * (n - s);
                        if (prod > bestProduct) {
                            bestProduct = prod;
                        }
                    }
                } else {
                    // DFS state
                    int v = e.v, p = e.parent;
                    if (e.iter == 0) {
                        // first time we see v
                        visited[v] = true;
                        tin[v] = low[v] = ++timer;
                        sz[v] = 1;
                    }
                    if (e.iter < adj[v].size()) {
                        // Process next neighbor
                        int u = adj[v].get(e.iter);
                        e.iter++;  // increment for next time
                        // Push the current state back (to resume later)
                        stack.push(e);

                        if (u == p) {
                            // the edge back to parent; ignore
                            continue;
                        }
                        if (visited[u]) {
                            // back-edge
                            low[v] = Math.min(low[v], tin[u]);
                        } else {
                            // tree-edge, will recurse into u
                            // After returning from u, we must do the "post-child" update
                            stack.push(new StackEntry(1, v, 0, 0, u));  // post-child
                            stack.push(new StackEntry(0, u, v, 0, 0));  // DFS into u
                        }
                    }
                    // else e.iter >= adj[v].size() => all neighbors done, no re-push
                }
            }

            // Total pairs minus max unreachable = min reachable
            long totalPairs = ((long)n * (n - 1)) / 2;
            long answer = totalPairs - bestProduct;
            out.println(answer);
        }

        out.flush();
    }
}