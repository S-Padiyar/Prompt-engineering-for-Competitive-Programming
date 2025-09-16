import java.io.*;
import java.util.*;

public class Main {
    static final int MAXN = 400;
    static final int MAXNODES = 2*MAXN;        // up to 2n-1
    static final long INF = (long)1e18;

    // The reconstruction tree
    static int leftCh[] = new int[MAXNODES+1], rightCh[] = new int[MAXNODES+1];
    static long  nodeWeight[] = new long[MAXNODES+1];
    static int   dsuParent[] = new int[MAXNODES+1];
    static int   leafCount[] = new int[MAXNODES+1];
    static boolean isSpecial[] = new boolean[MAXNODES+1];

    // DP arrays
    // dp[u][j] = minimal cost to serve all special nodes within u's subtree
    //            that are actually served at or below u, using exactly j servers in u's subtree.
    // t[u][j]  = how many special nodes in u's subtree remain unserved if we used j servers.
    static long  dp[][] = new long[MAXNODES+1][MAXN+1];
    static int   t [][] = new int [MAXNODES+1][MAXN+1];

    // Edge for Kruskal
    static class Edge implements Comparable<Edge> {
        int u, v;
        long w;
        Edge(int _u, int _v, long _w) { u=_u; v=_v; w=_w; }
        public int compareTo(Edge o) {
            return Long.compare(this.w, o.w);
        }
    }

    // DSU find
    static int findp(int x) {
        if (dsuParent[x] != x) 
            dsuParent[x] = findp(dsuParent[x]);
        return dsuParent[x];
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        int T = Integer.parseInt(br.readLine().trim());
        StringBuilder output = new StringBuilder();

        while (T-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());
            int p = Integer.parseInt(st.nextToken());

            // read specials
            st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= n; i++) {
                isSpecial[i] = false;
            }
            for (int i = 0; i < p; i++) {
                int s = Integer.parseInt(st.nextToken());
                isSpecial[s] = true;
            }

            // read edges
            ArrayList<Edge> edges = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                long w = Long.parseLong(st.nextToken());
                edges.add(new Edge(u, v, w));
            }
            Collections.sort(edges);

            // initialize DSU
            int maxNode = 2*n - 1;
            for (int i = 1; i <= maxNode; i++) {
                dsuParent[i] = i;
                leftCh[i] = rightCh[i] = 0;
                nodeWeight[i] = 0;
            }

            // build Kruskal‐reconstruction‐tree
            int nxt = n;
            for (Edge e : edges) {
                int ru = findp(e.u), rv = findp(e.v);
                if (ru != rv) {
                    nxt++;
                    nodeWeight[nxt] = e.w;
                    leftCh [nxt] = ru;
                    rightCh[nxt] = rv;
                    dsuParent[ru] = nxt;
                    dsuParent[rv] = nxt;
                    dsuParent[nxt] = nxt;
                    if (nxt == maxNode) break;
                }
            }
            int root = nxt; // should be 2n-1

            // compute leafCount[] = number of original‐node leaves under u
            for (int u = 1; u <= root; u++) {
                if (u <= n) {
                    leafCount[u] = 1;
                } else {
                    leafCount[u] = leafCount[leftCh[u]] + leafCount[rightCh[u]];
                }
            }

            // DP bottom‐up
            for (int u = 1; u <= root; u++) {
                int sz = leafCount[u];
                if (u <= n) {
                    // a leaf
                    // dp[u][0], t[u][0]
                    dp[u][0] = 0;
                    t [u][0] = isSpecial[u] ? 1 : 0;
                    // dp[u][1], t[u][1]
                    dp[u][1] = 0;
                    t [u][1] = 0;
                } else {
                    // an internal node
                    int L = leftCh[u], R = rightCh[u];
                    int lsz = leafCount[L], rsz = leafCount[R];

                    // initialize dp[u][*]
                    for (int j = 0; j <= sz; j++) {
                        dp[u][j] = INF;
                    }

                    // knapsack‐style combine
                    for (int j1 = 0; j1 <= lsz; j1++) {
                        for (int j2 = 0; j2 <= rsz; j2++) {
                            int j = j1 + j2;
                            long baseCost = dp[L][j1] + dp[R][j2];
                            int  unserved = t[L][j1] + t[R][j2];
                            long newCost;
                            int  newUnserved;
                            if (j > 0) {
                                // we do have at least one server in this subtree
                                newCost = baseCost + unserved * nodeWeight[u];
                                newUnserved = 0;
                            } else {
                                // still zero servers
                                newCost = baseCost;
                                newUnserved = unserved;
                            }
                            if (newCost < dp[u][j]) {
                                dp[u][j] = newCost;
                                t [u][j] = newUnserved;
                            }
                        }
                    }
                }
            }

            // dp[root][k] = cost with exactly k servers.
            // problem wants "up to k" servers, so we take prefix‐min.
            long best = INF;
            for (int k = 1; k <= n; k++) {
                best = Math.min(best, dp[root][k]);
                if (k > 1) output.append(' ');
                output.append(best);
            }
            output.append('\n');

            // clear specials for next test
            for (int i = 1; i <= n; i++) {
                isSpecial[i] = false;
            }
        }

        System.out.print(output.toString());
    }
}