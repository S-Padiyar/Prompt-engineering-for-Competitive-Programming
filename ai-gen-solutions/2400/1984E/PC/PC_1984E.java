import java.io.*;
import java.util.*;

public class Main {
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
        FastReader() { br = new BufferedReader(new InputStreamReader(System.in)); }
        String next() {
            while (st == null || !st.hasMoreTokens()) {
                try { st = new StringTokenizer(br.readLine()); }
                catch (IOException e) { throw new RuntimeException(e); }
            }
            return st.nextToken();
        }
        int nextInt() { return Integer.parseInt(next()); }
    }

    public static void main(String[] args) {
        FastReader in = new FastReader();
        PrintWriter out = new PrintWriter(System.out);

        int t = in.nextInt();
        while (t-- > 0) {
            int n = in.nextInt();
            // Read tree
            List<Integer>[] g = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) {
                g[i] = new ArrayList<>();
            }
            for (int i = 0; i < n-1; i++) {
                int u = in.nextInt(), v = in.nextInt();
                g[u].add(v);
                g[v].add(u);
            }

            // 1) Root the tree at 1, build a postorder
            int[] parent = new int[n+1];
            parent[1] = 0;
            List<Integer> post = new ArrayList<>(n);

            // Iterative DFS to get parent[] and postorder
            Deque<int[]> stack = new ArrayDeque<>();
            stack.push(new int[]{1, 0, 0}); // {node, parent, state=0:pre,1:post}
            while (!stack.isEmpty()) {
                int[] cur = stack.pop();
                int u = cur[0], p = cur[1], st = cur[2];
                if (st == 0) {
                    parent[u] = p;
                    // push post‐state
                    stack.push(new int[]{u, p, 1});
                    for (int w : g[u]) {
                        if (w == p) continue;
                        stack.push(new int[]{w, u, 0});
                    }
                } else {
                    post.add(u);
                }
            }

            // Build child lists
            List<Integer>[] children = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) {
                children[i] = new ArrayList<>();
            }
            for (int v = 2; v <= n; v++) {
                children[parent[v]].add(v);
            }

            // 2) DP for maximum independent set
            int[] dp0 = new int[n+1], dp1 = new int[n+1];
            for (int u : post) {
                // dp1[u] = 1 + sum dp0[child]
                dp1[u] = 1;
                for (int w : children[u]) {
                    dp1[u] += dp0[w];
                }
                // dp0[u] = sum of max(dp0[child], dp1[child])
                int s = 0;
                for (int w : children[u]) {
                    s += Math.max(dp0[w], dp1[w]);
                }
                dp0[u] = s;
            }
            // Best MIS size on the whole tree:
            int alpha = Math.max(dp0[1], dp1[1]);

            // 3) We now do a "possible states" DP to see which nodes can be 0/excluded
            //    or 1/included in *some* optimum solution.
            boolean[] poss0 = new boolean[n+1], poss1 = new boolean[n+1];

            // We'll push (node,state) into a queue
            Deque<int[]> q = new ArrayDeque<>();
            // Initialize at root = 1
            if (dp0[1] == alpha) {
                poss0[1] = true;
                q.add(new int[]{1,0});
            }
            if (dp1[1] == alpha) {
                poss1[1] = true;
                q.add(new int[]{1,1});
            }

            // Propagate down
            while (!q.isEmpty()) {
                int[] cur = q.poll();
                int u = cur[0], st = cur[1];
                if (st == 1) {
                    // u is included => all children must be 0
                    for (int w : children[u]) {
                        if (!poss0[w]) {
                            poss0[w] = true;
                            q.add(new int[]{w, 0});
                        }
                    }
                } else {
                    // u is excluded => for each child we must match
                    // whichever of dp0[w], dp1[w] was used
                    for (int w : children[u]) {
                        if (dp0[w] > dp1[w]) {
                            // dp0[w] was used
                            if (!poss0[w]) {
                                poss0[w] = true;
                                q.add(new int[]{w,0});
                            }
                        } else if (dp1[w] > dp0[w]) {
                            // dp1[w] was used
                            if (!poss1[w]) {
                                poss1[w] = true;
                                q.add(new int[]{w,1});
                            }
                        } else {
                            // equal => child can be either 0 or 1
                            if (!poss0[w]) {
                                poss0[w] = true;
                                q.add(new int[]{w,0});
                            }
                            if (!poss1[w]) {
                                poss1[w] = true;
                                q.add(new int[]{w,1});
                            }
                        }
                    }
                }
            }

            // 4) Now check if there is any *leaf* v (degree == 1) with poss0[v] == true.
            //    If so, we can do alpha+1; otherwise just alpha.
            int ans = alpha;
            for (int v = 1; v <= n; v++) {
                if (g[v].size() == 1) {
                    if (poss0[v]) {
                        ans = alpha + 1;
                        break;
                    }
                }
            }

            out.println(ans);
        }
        out.flush();
    }
}