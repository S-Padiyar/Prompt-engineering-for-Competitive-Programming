import java.util.*;
import java.io.*;

public class Main {
    static final long INF = Long.MAX_VALUE / 4;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            // Read n, m, p
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());
            int p = Integer.parseInt(st.nextToken());

            // Which leaves are special?
            boolean[] isSpecial = new boolean[n+1];
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < p; i++) {
                int s = Integer.parseInt(st.nextToken());
                isSpecial[s] = true;
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

            // Build MST by Kruskal
            Arrays.sort(edges, Comparator.comparingLong(e -> e.w));
            DSU dsu1 = new DSU(n+1);
            List<Edge> mstEdges = new ArrayList<>(n-1);
            for (Edge e : edges) {
                int ru = dsu1.find(e.u);
                int rv = dsu1.find(e.v);
                if (ru != rv) {
                    dsu1.union(ru, rv);
                    mstEdges.add(e);
                    if (mstEdges.size() == n-1) break;
                }
            }

            // Build the Kruskal‐reconstruction tree
            // We'll index new internal nodes from n+1 upward.
            int maxNodes = 2*n; // safe upper bound
            int[] left  = new int[maxNodes];
            int[] right = new int[maxNodes];
            long[] wgt  = new long[maxNodes];
            for (int i = 1; i <= n; i++) {
                wgt[i] = 0; // leaves have weight 0
            }

            // another DSU for merging into the reconstruction
            DSU dsu2 = new DSU(2*n);
            for (Edge e : mstEdges) {
                int ru = dsu2.find(e.u);
                int rv = dsu2.find(e.v);
                int newNode = ++n;      // create a fresh internal node
                wgt[newNode] = e.w;     // this node's weight = edge's weight
                left[newNode]  = ru;
                right[newNode] = rv;
                dsu2.parent[ru] = newNode;
                dsu2.parent[rv] = newNode;
                dsu2.parent[newNode] = newNode;
            }
            int root = dsu2.find(1);  // the final root

            // Prepare DP structures
            long[][] f = new long[2*n+1][];  // f[u][j]
            int[] S = new int[2*n+1];        // S[u] = # special leaves

            // Post‐order DFS
            dfs(root, left, right, wgt, isSpecial, f, S);

            // f[root][j] is minimal cost with j centers.
            // For k>p, cost=0; for k=1..p, cost = f[root][k].
            StringBuilder sb = new StringBuilder();
            for (int k = 1; k <= root; k++) {
                long ans = (k <= S[root] ? f[root][k] : 0);
                sb.append(ans).append(' ');
            }
            System.out.println(sb);
        }
    }

    // DFS that computes f[u] and S[u] bottom‐up
    static void dfs(int u, int[] left, int[] right, long[] wgt,
                    boolean[] isSpecial, long[][] f, int[] S) {
        if (left[u] == 0 && right[u] == 0) {
            // leaf
            S[u] = isSpecial[u] ? 1 : 0;
            f[u] = new long[S[u] + 1];
            f[u][0] = 0;
            if (S[u] == 1) {
                f[u][1] = 0;
            }
            return;
        }

        // Internal node
        int a = left[u], b = right[u];
        dfs(a, left, right, wgt, isSpecial, f, S);
        dfs(b, left, right, wgt, isSpecial, f, S);

        S[u] = S[a] + S[b];
        f[u] = new long[S[u] + 1];
        Arrays.fill(f[u], INF);
        f[u][0] = 0;

        long w = wgt[u];
        for (int j1 = 0; j1 <= S[a]; j1++) {
            long ca = f[a][j1];
            if (ca >= INF) continue;
            for (int j2 = 0; j2 <= S[b]; j2++) {
                if (j1 + j2 == 0) continue; // zero centers covers nothing
                long cb = f[b][j2];
                if (cb >= INF) continue;
                int j = j1 + j2;
                long cost;
                if (j1 > 0 && j2 > 0) {
                    // both sides have centers => no cross‐term
                    cost = ca + cb;
                } else if (j1 == 0) {
                    // no center in a => all S[a] specials use centers in b at cost w each
                    cost = ca + cb + (long)S[a] * w;
                } else {
                    // j2 == 0
                    cost = ca + cb + (long)S[b] * w;
                }
                if (cost < f[u][j]) {
                    f[u][j] = cost;
                }
            }
        }
    }

    // DSU for Kruskal and reconstruction‐union
    static class DSU {
        int[] parent;
        DSU(int n) { parent = new int[n]; for(int i=0;i<n;i++) parent[i]=i; }
        int find(int x){ return parent[x]==x?x: (parent[x]=find(parent[x])); }
        void union(int a,int b){ parent[find(a)] = find(b); }
    }

    static class Edge {
        int u,v;
        long w;
        Edge(int u,int v,long w){ this.u=u; this.v=v; this.w=w; }
    }
}