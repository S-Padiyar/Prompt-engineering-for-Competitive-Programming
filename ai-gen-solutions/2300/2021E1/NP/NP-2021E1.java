import java.io.*;
import java.util.*;

public class Main {
    static final long INF = (long)1e18;
    static int n, m, p;
    static int[] special;
    
    // For the reconstruction tree we will have up to 2n-1 nodes:
    static int MAX = 800;
    static int[] leftSon = new int[MAX], rightSon = new int[MAX];
    static long[] label = new long[MAX];
    static int[] dsu = new int[MAX];

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter pw = new PrintWriter(System.out);
        int T = Integer.parseInt(br.readLine().trim());
        
        while (T-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            n = Integer.parseInt(st.nextToken());
            m = Integer.parseInt(st.nextToken());
            p = Integer.parseInt(st.nextToken());
            
            special = new int[n+1];
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < p; i++) {
                int s = Integer.parseInt(st.nextToken());
                special[s] = 1;
            }
            
            // Read edges
            Edge[] edges = new Edge[m];
            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                long w = Long.parseLong(st.nextToken());
                edges[i] = new Edge(u, v, w);
            }
            Arrays.sort(edges);
            
            // Initialize DSU
            int tot = n;  // We'll create internal nodes numbered n+1, n+2, ...
            for (int i = 1; i <= 2*n; i++) {
                dsu[i] = i;
                leftSon[i] = rightSon[i] = 0;
                label[i] = 0;
            }
            
            // Build reconstruction tree by Kruskal
            for (Edge e : edges) {
                int ru = find(e.u);
                int rv = find(e.v);
                if (ru != rv) {
                    tot++;
                    label[tot] = e.w; // internal node's label
                    leftSon[tot] = ru;
                    rightSon[tot] = rv;
                    dsu[ru] = tot;
                    dsu[rv] = tot;
                    dsu[tot] = tot;
                }
            }
            
            // The root is the dsu-find of any leaf, say 1:
            int root = find(1);
            
            // Now do the DP on that binary tree:
            DP res = dfs(root);
            
            // res.dp1[c] is min cost using exactly c centers, all specials covered.
            // Once c >= p, cost is 0 (we can place a center at each special).
            for (int c = 1; c <= n; c++) {
                long ans = INF;
                if (c < res.dp1.length) ans = res.dp1[c];
                if (c >= p) ans = 0;  // can always zero out if we have at least p centers
                pw.print(ans + (c == n ? "\n" : " "));
            }
            
            // clear special marks
            for (int i = 1; i <= n; i++) special[i] = 0;
        }
        
        pw.flush();
    }
    
    // DSU find with path‐compression
    static int find(int x) {
        if (dsu[x] != x) dsu[x] = find(dsu[x]);
        return dsu[x];
    }
    
    // The DP return: dp0[b] = cost if 0 centers in subtree, b specials left unserved
    //                dp1[c] = cost if c>0 centers in subtree and all specials served
    // plus leafCount, specialCount.
    static class DP {
        long[] dp0, dp1;
        int leafCount, specCount;
    }
    
    static DP dfs(int u) {
        DP cur = new DP();
        
        // If it's a leaf
        if (leftSon[u] == 0 && rightSon[u] == 0) {
            // leafCount = 1
            cur.leafCount = 1;
            cur.specCount = special[u];
            
            // dp0 size = specCount+1
            cur.dp0 = new long[cur.specCount + 1];
            Arrays.fill(cur.dp0, INF);
            
            // dp1 size = leafCount+1 = 2
            cur.dp1 = new long[cur.leafCount + 1];
            Arrays.fill(cur.dp1, INF);
            
            // case: no center, 0 specials unserved
            cur.dp0[0] = 0;
            // if leaf is special, we can leave it unserved:
            if (cur.specCount == 1) cur.dp0[1] = 0;
            // place 1 center => serve itself at cost 0
            cur.dp1[1] = 0;
            
            return cur;
        }
        
        // otherwise internal node
        DP L = dfs(leftSon[u]);
        DP R = dfs(rightSon[u]);
        
        cur.leafCount = L.leafCount + R.leafCount;
        cur.specCount = L.specCount + R.specCount;
        
        cur.dp0 = new long[cur.specCount + 1];
        cur.dp1 = new long[cur.leafCount + 1];
        Arrays.fill(cur.dp0, INF);
        Arrays.fill(cur.dp1, INF);
        
        long W = label[u];  // cost to serve at this merge
        
        // Case 4: c=0 centers overall in u => combine dp0 of children
        for (int bL = 0; bL <= L.specCount; bL++) {
            if (L.dp0[bL] >= INF) continue;
            for (int bR = 0; bR <= R.specCount; bR++) {
                if (R.dp0[bR] >= INF) continue;
                int b = bL + bR;
                long cost = L.dp0[bL] + R.dp0[bR];
                if (cost < cur.dp0[b]) cur.dp0[b] = cost;
            }
        }
        
        // Case 2: cv>0, cw=0 => we place cv centers in L, none in R
        // any unserved in R must now be served at u, paying W each
        for (int cL = 1; cL <= L.leafCount; cL++) {
            if (L.dp1[cL] >= INF) continue;
            for (int bR = 0; bR <= R.specCount; bR++) {
                if (R.dp0[bR] >= INF) continue;
                long cost = L.dp1[cL] + R.dp0[bR] + W * bR;
                if (cost < cur.dp1[cL]) cur.dp1[cL] = cost;
            }
        }
        
        // Case 3: cv=0, cw>0
        for (int cR = 1; cR <= R.leafCount; cR++) {
            if (R.dp1[cR] >= INF) continue;
            for (int bL = 0; bL <= L.specCount; bL++) {
                if (L.dp0[bL] >= INF) continue;
                long cost = R.dp1[cR] + L.dp0[bL] + W * bL;
                if (cost < cur.dp1[cR]) cur.dp1[cR] = cost;
            }
        }
        
        // Case 1: cv>0 && cw>0 => all specials in both subtrees are served below
        for (int cL = 1; cL <= L.leafCount; cL++) {
            if (L.dp1[cL] >= INF) continue;
            for (int cR = 1; cR <= R.leafCount; cR++) {
                if (R.dp1[cR] >= INF) continue;
                int c = cL + cR;
                long cost = L.dp1[cL] + R.dp1[cR];
                if (cost < cur.dp1[c]) cur.dp1[c] = cost;
            }
        }
        
        return cur;
    }
    
    // Edge structure + sort by weight
    static class Edge implements Comparable<Edge> {
        int u, v;
        long w;
        Edge(int u, int v, long w) { this.u = u; this.v = v; this.w = w; }
        public int compareTo(Edge o) {
            return Long.compare(this.w, o.w);
        }
    }
}