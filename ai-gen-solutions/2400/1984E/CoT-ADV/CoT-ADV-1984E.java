import java.io.*;
import java.util.*;

public class Main {
    static int N;
    static List<Integer>[] adj;
    static int[] parent, deg;
    static int[] order;       // BFS order
    static int[] dp0, dpBest; // dp0[u] = sum dpBest[children], dpBest[u] = best matching in subtree u
    // For each u we keep the two largest "diff" values over its children:
    static int[] diff1, diff2, diff1Id;
    // For reroot:
    static int[] upDp0, upBest, diffParent;
    static long[] sumMmAdj;

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer tk = new StringTokenizer(in.readLine());
        int t = Integer.parseInt(tk.nextToken());
        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            tk = new StringTokenizer(in.readLine());
            N = Integer.parseInt(tk.nextToken());
            // Build adjacency
            adj = new ArrayList[N+1];
            for (int i = 1; i <= N; i++) {
                adj[i] = new ArrayList<>();
            }
            for (int i = 1; i < N; i++) {
                tk = new StringTokenizer(in.readLine());
                int u = Integer.parseInt(tk.nextToken());
                int v = Integer.parseInt(tk.nextToken());
                adj[u].add(v);
                adj[v].add(u);
            }

            // Prepare arrays
            parent = new int[N+1];
            order  = new int[N];
            deg    = new int[N+1];
            for (int i = 1; i <= N; i++) {
                deg[i] = adj[i].size();
            }

            // 1) BFS to get a parent[] and an order[] by increasing depth
            Queue<Integer> q = new ArrayDeque<>();
            q.add(1);
            parent[1] = 0;
            int idx = 0;
            while (!q.isEmpty()) {
                int u = q.poll();
                order[idx++] = u;
                for (int w: adj[u]) {
                    if (w == parent[u]) continue;
                    parent[w] = u;
                    q.add(w);
                }
            }

            // 2) Post‐order DP to compute, for each u, the matching on the subtree rooted at u
            dp0 = new int[N+1];
            dpBest = new int[N+1];
            diff1 = new int[N+1];
            diff2 = new int[N+1];
            diff1Id = new int[N+1];

            // Process in reverse BFS‐order so children come before parent
            for (int i = N-1; i >= 0; i--) {
                int u = order[i];
                // Sum up children's dpBest
                int sum0 = 0;
                int best1 = 0, best2 = 0, best1id = -1;
                for (int w: adj[u]) {
                    if (w == parent[u]) continue;
                    sum0 += dpBest[w];
                }
                // compute diffs for each child
                for (int w: adj[u]) {
                    if (w == parent[u]) continue;
                    int d = dp0[w] + 1 - dpBest[w];
                    // track top two
                    if (d > best1) {
                        best2 = best1;
                        best1 = d;
                        best1id = w;
                    } else if (d > best2) {
                        best2 = d;
                    }
                }
                dp0[u] = sum0;
                diff1[u] = best1;
                diff2[u] = best2;
                diff1Id[u] = best1id >= 0 ? best1id : 0;
                // dpBest[u] = either we match u->one child or not
                int gain = Math.max(0, best1);
                dpBest[u] = Math.max(sum0, sum0 + gain);
            }

            // 3) Reroot (BFS‐style) to compute the matching of the "outside" component at each node
            upDp0 = new int[N+1];
            upBest = new int[N+1];
            diffParent = new int[N+1];

            // Root's outside‐component has no nodes => dp0=0, best=0, diffParent=0
            upDp0[1] = 0;
            upBest[1] = 0;
            diffParent[1] = 0;

            // Process in BFS‐order so parent is processed before children
            for (int ii = 0; ii < N; ii++) {
                int u = order[ii];
                // Sum of all neighbor‐contributions
                int sumAll = dp0[u] + upBest[u];
                int bigDiff = Math.max(diff1[u], diffParent[u]);  // best diff among all neighbors

                for (int w: adj[u]) {
                    if (w == parent[u]) {
                        // we will never BFS back up
                        continue;
                    }
                    // Exclude w's contribution
                    int contribW = dpBest[w];
                    int dp0Excl = sumAll - contribW;
                    // which diff do we drop?
                    int dd;
                    if (diff1Id[u] == w) {
                        // we used w's diff1, drop it
                        dd = Math.max(diff2[u], diffParent[u]);
                    } else {
                        dd = Math.max(diff1[u], diffParent[u]);
                    }
                    if (dd < 0) dd = 0;
                    int bestExcl = Math.max(dp0Excl, dp0Excl + dd);

                    upDp0[w]     = dp0Excl;
                    upBest[w]    = bestExcl;
                    diffParent[w] = dp0Excl + 1 - bestExcl;

                    // continue BFS
                }
            }

            // 4) compute for each v the sum of its adjacent‐component matchings
            sumMmAdj = new long[N+1];
            for (int u = 1; u <= N; u++) {
                long s = upBest[u];
                for (int w: adj[u]) {
                    if (w == parent[u]) continue;
                    s += dpBest[w];
                }
                sumMmAdj[u] = s;
            }

            // 5) finally compute leaves(v) = 
            //       if deg[v]>=2:  (N − 1 − sumMmAdj[v])
            //       else (N − sumMmAdj[v])
            int answer = 0;
            for (int v = 1; v <= N; v++) {
                int leaves;
                if (deg[v] >= 2) {
                    leaves = (int)(N - 1 - sumMmAdj[v]);
                } else {
                    leaves = (int)(N - sumMmAdj[v]);
                }
                if (leaves > answer) answer = leaves;
            }

            sb.append(answer).append('\n');
        }

        System.out.print(sb);
    }
}