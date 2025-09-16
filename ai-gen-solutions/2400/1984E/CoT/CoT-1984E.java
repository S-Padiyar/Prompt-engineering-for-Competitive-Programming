import java.io.*;
import java.util.*;

public class Main {
    static final int MAXN = 300_000;
    static ArrayList<Integer>[] adj;
    static int[] parent, order;
    static int[] down0, down1, up0, up1;
    static int[] dp0tot, dp1tot, deg;
    static int N;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(br.readLine());
        // We'll reallocate per test but total sum of n <= 300k so it's ok.
        while (t-- > 0) {
            N = Integer.parseInt(br.readLine());
            adj = new ArrayList[N+1];
            for (int i = 1; i <= N; i++) {
                adj[i] = new ArrayList<>();
            }
            deg = new int[N+1];
            for (int i = 0; i < N-1; i++) {
                StringTokenizer st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                adj[u].add(v);
                adj[v].add(u);
                deg[u]++;
                deg[v]++;
            }

            // Special small case:
            if (N == 2) {
                // Two nodes always end up both leaves
                out.println(2);
                continue;
            }

            // 1) Build a DFS order and parent[] (iterative)
            parent = new int[N+1];
            order = new int[N]; 
            int osz = 0;
            int[] stack = new int[N];
            int sp = 0;
            stack[sp++] = 1;
            parent[1] = 0;

            while (sp > 0) {
                int u = stack[--sp];
                order[osz++] = u;
                for (int v : adj[u]) {
                    if (v == parent[u]) continue;
                    parent[v] = u;
                    stack[sp++] = v;
                }
            }

            // 2) Post‐order DP down[]
            down0 = new int[N+1];
            down1 = new int[N+1];
            for (int i = osz - 1; i >= 0; i--) {
                int u = order[i];
                int takeU = 1;      // u in the independent set
                int skipU = 0;      // u not in the independent set
                for (int v : adj[u]) {
                    if (v == parent[u]) continue;
                    // if u not taken, child may or may not be:
                    skipU += Math.max(down0[v], down1[v]);
                    // if u is taken, child cannot:
                    takeU += down0[v];
                }
                down0[u] = skipU;
                down1[u] = takeU;
            }

            // 3) Reroot DP up[]
            up0 = new int[N+1];
            up1 = new int[N+1];
            dp0tot = new int[N+1];
            dp1tot = new int[N+1];

            // Pre‐order (use the same 'order' which is a valid topological so parent is
            // visited before children).
            for (int i = 0; i < osz; i++) {
                int u = order[i];
                // sum of "child contributions" to skipU/ takeU
                int sumSkipChildren = 0, sumTakeChildren = 0;
                for (int v : adj[u]) {
                    if (v == parent[u]) continue;
                    sumSkipChildren += Math.max(down0[v], down1[v]);
                    sumTakeChildren += down0[v];
                }
                // parent‐side
                int skipParent  = Math.max(up0[u], up1[u]);
                int takeParent  = up0[u];

                // total for the entire tree at u
                int totSkip = skipParent + sumSkipChildren;
                int totTake = 1 + takeParent + sumTakeChildren;

                dp0tot[u] = totSkip;
                dp1tot[u] = totTake;

                // now compute up[] for each child
                for (int v : adj[u]) {
                    if (v == parent[u]) continue;
                    int cSkip = Math.max(down0[v], down1[v]);
                    int cTake = down0[v];
                    // exclude v's branch from u's total:
                    up0[v] = totSkip - cSkip;
                    up1[v] = totTake - cTake;
                }
            }

            // 4) Finally scan all leaves v1
            int ans = 0;
            for (int v = 1; v <= N; v++) {
                if (deg[v] == 1) {
                    // neighbor u
                    int u = (parent[v] == 0 ? adj[v].get(0) : parent[v]);
                    // if we root‐remove v, MIS(T\v) = max(dp0tot[u], dp1tot[u] - 0)  minus 1 if dp0tot[u] used
                    // but it simplifies to max(dp0tot[u], dp1tot[u]+1)
                    int candidate = Math.max(dp0tot[u], dp1tot[u] + 1);
                    ans = Math.max(ans, candidate);
                }
            }

            out.println(ans);
        }
        out.flush();
    }
}