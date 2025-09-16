import java.io.*;
import java.util.*;

public class Main {
    static final int MAXN = 300_000 + 5;
    static final int LOG  = 19; // enough for up to ~5e5 nodes

    static int n, q;
    static int[] parent = new int[MAXN];
    static int[] depth  = new int[MAXN];
    static int[][] up   = new int[LOG][MAXN];
    // adjacency lists for building the tree
    static ArrayList<Integer>[] adj = new ArrayList[MAXN];
    // The current permutation p[1..n]
    static int[] p = new int[MAXN];
    // good[i]==true iff LCA(p[i-1],p[i]) == parent[p[i]]
    static boolean[] good = new boolean[MAXN];
    // temporary marker array when updating neighborhoods on swap
    static boolean[] seen = new boolean[MAXN];

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder out = new StringBuilder();
        StringTokenizer st;

        int T = Integer.parseInt(in.readLine().trim());
        while (T-- > 0) {
            st = new StringTokenizer(in.readLine());
            n = Integer.parseInt(st.nextToken());
            q = Integer.parseInt(st.nextToken());

            // initialize adjacency
            for (int i = 1; i <= n; i++) {
                if (adj[i] == null) adj[i] = new ArrayList<>();
                else adj[i].clear();
            }

            // read parents a[2..n]
            parent[1] = 0;  // root has no parent
            st = new StringTokenizer(in.readLine());
            for (int i = 2; i <= n; i++) {
                int a = Integer.parseInt(st.nextToken());
                parent[i] = a;
                adj[a].add(i);
            }

            // Read initial permutation p[1..n]
            st = new StringTokenizer(in.readLine());
            for (int i = 1; i <= n; i++) {
                p[i] = Integer.parseInt(st.nextToken());
            }

            // 1) DFS to compute depth[] and up[0][v] = parent[v]
            dfsCompute(1, 0);

            // 2) Build the binary-lifting table
            for (int k = 1; k < LOG; k++) {
                for (int v = 1; v <= n; v++) {
                    up[k][v] = up[k-1][ up[k-1][v] ];
                }
            }

            // 3) Initialize good[2..n] and count bad ones
            int badCt = 0;
            for (int i = 2; i <= n; i++) {
                good[i] = checkPair(i);
                if (!good[i]) badCt++;
            }
            // also require p[1]==1
            boolean badRoot = (p[1] != 1);

            // Process queries
            for (int qi = 0; qi < q; qi++) {
                st = new StringTokenizer(in.readLine());
                int x = Integer.parseInt(st.nextToken());
                int y = Integer.parseInt(st.nextToken());
                // We will swap p[x] <-> p[y].
                // Only positions i in { x-1,x, x+1,  y-1,y, y+1 } (2..n) can change good[i].
                int[] cand = { x-1, x, x+1, y-1, y, y+1 };
                List<Integer> toRecalc = new ArrayList<>();

                // remove old contributions
                for (int c : cand) {
                    if (c >= 2 && c <= n && !seen[c]) {
                        seen[c] = true;
                        if (!good[c]) badCt--;
                        toRecalc.add(c);
                    }
                }

                // do the swap
                int tmp = p[x]; p[x] = p[y]; p[y] = tmp;

                // recompute good[c] and add back
                for (int c : toRecalc) {
                    good[c] = checkPair(c);
                    if (!good[c]) badCt++;
                }

                // clear seen[]
                for (int c : toRecalc) {
                    seen[c] = false;
                }

                // update root check
                badRoot = (p[1] != 1);

                // final answer
                out.append((!badRoot && badCt == 0) ? "YES\n" : "NO\n");
            }
        }

        System.out.print(out);
    }

    // DFS from root to fill depth[v] and up[0][v]
    static void dfsCompute(int v, int par) {
        up[0][v] = par;
        depth[v] = (par == 0 ? 0 : depth[par] + 1);
        for (int w : adj[v]) {
            dfsCompute(w, v);
        }
    }

    // LCA by binary-lifting
    static int lca(int a, int b) {
        if (a == 0 || b == 0) return a|b; // handle degenerate
        if (depth[a] < depth[b]) {
            int t = a; a = b; b = t;
        }
        // lift a up to depth of b
        int diff = depth[a] - depth[b];
        for (int k = 0; k < LOG; k++) {
            if ((diff & (1<<k)) != 0) {
                a = up[k][a];
            }
        }
        if (a == b) return a;
        for (int k = LOG - 1; k >= 0; k--) {
            if (up[k][a] != up[k][b]) {
                a = up[k][a];
                b = up[k][b];
            }
        }
        return up[0][a]; // the parent
    }

    // Test the adjacency condition at index i: we need
    //    LCA( p[i-1], p[i] )  ==  parent[ p[i] ]
    static boolean checkPair(int i) {
        int u = p[i], v = p[i-1];
        int w = lca(u, v);
        return w == parent[u];
    }
}