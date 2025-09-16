import java.io.*;
import java.util.*;

public class Main {
    static final long INF = (long)1e18;

    // DSU arrays for building the Kruskal‐tree
    static int[] dsu;
    static int[] leftChild, rightChild;
    static long[] nodeWeight;
    static int[] demandCount, leafCount;
    static int curNode;  // next free index in the DSU‐tree

    // find with path compression
    static int find(int x) {
        if (dsu[x] != x) dsu[x] = find(dsu[x]);
        return dsu[x];
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int T = Integer.parseInt(br.readLine().trim());
        while (T-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());
            int p = Integer.parseInt(st.nextToken());

            // demands
            int[] demands = new int[p];
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < p; i++) {
                demands[i] = Integer.parseInt(st.nextToken());
            }

            // read edges
            class Edge { int u, v; long w; }
            Edge[] edges = new Edge[m];
            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken()) - 1;
                int v = Integer.parseInt(st.nextToken()) - 1;
                long w = Long.parseLong(st.nextToken());
                edges[i] = new Edge();
                edges[i].u = u; 
                edges[i].v = v; 
                edges[i].w = w;
            }

            // sort edges by weight for Kruskal
            Arrays.sort(edges, Comparator.comparingLong(e -> e.w));

            // we will build a tree of size up to 2n-1
            int maxNodes = 2*n;
            dsu        = new int[maxNodes];
            leftChild  = new int[maxNodes];
            rightChild = new int[maxNodes];
            nodeWeight = new long[maxNodes];
            demandCount= new int[maxNodes];
            leafCount  = new int[maxNodes];

            // initialize leaves 0..n-1
            for (int i = 0; i < 2*n; i++) {
                dsu[i]        = i;
                leftChild[i]  = -1;
                rightChild[i] = -1;
                nodeWeight[i] = 0;
                demandCount[i]= 0;
                leafCount[i]  = 0;
            }

            // mark which leaves are demands
            for (int s : demands) {
                demandCount[s-1] = 1;  // zero-based
            }
            for (int i = 0; i < n; i++) {
                leafCount[i] = 1; // each leaf is exactly 1 leaf
            }

            curNode = n;  // next new internal node index

            // Kruskal, building DSU-tree
            for (Edge e : edges) {
                int ru = find(e.u);
                int rv = find(e.v);
                if (ru != rv) {
                    int newNode = curNode++;
                    leftChild[newNode]  = ru;
                    rightChild[newNode] = rv;
                    nodeWeight[newNode] = e.w;
                    // union in DSU
                    dsu[ru] = newNode;
                    dsu[rv] = newNode;
                    dsu[newNode] = newNode;
                    // accumulate leafCount and demandCount
                    leafCount[newNode]   = leafCount[ru]   + leafCount[rv];
                    demandCount[newNode] = demandCount[ru] + demandCount[rv];
                }
            }

            // the root is curNode-1
            int root = curNode - 1;

            // DP array: for each node u we store dp[u][], size leafCount[u]+1
            @SuppressWarnings("unchecked")
            ArrayList<long[]> dp = new ArrayList<>(2*n);
            for (int i = 0; i < 2*n; i++) {
                dp.add(null);
            }

            // Build DP in increasing node index: children always < parent
            for (int u = 0; u < curNode; u++) {
                if (u < n) {
                    // leaf
                    long[] f = new long[2]; 
                    // open 0 servers in that leaf
                    f[0] = 0;
                    // open 1 server at that leaf
                    f[1] = 0;
                    dp.set(u, f);
                } else {
                    // internal node
                    int L = leftChild[u], R = rightChild[u];
                    long w = nodeWeight[u];
                    long[] dpl = dp.get(L), dpr = dp.get(R);
                    int szl = leafCount[L], szr = leafCount[R];
                    long[] f = new long[ szl + szr + 1 ];
                    Arrays.fill(f, INF);

                    // combine
                    for (int tl = 0; tl <= szl; tl++) {
                        long cl = dpl[tl];
                        boolean leftHas = (tl > 0);
                        int remL = leftHas ? 0 : demandCount[L];
                        for (int tr = 0; tr <= szr; tr++) {
                            long cr = dpr[tr];
                            boolean rightHas = (tr > 0);
                            int remR = rightHas ? 0 : demandCount[R];
                            int t = tl + tr;
                            long pen = 0;
                            if (leftHas && !rightHas) {
                                pen = (long)remR * w;
                            } else if (rightHas && !leftHas) {
                                pen = (long)remL * w;
                            }
                            long cand = cl + cr + pen;
                            if (cand < f[t]) {
                                f[t] = cand;
                            }
                        }
                    }
                    dp.set(u, f);
                }
            }

            // Extract answer from dp[root]
            long[] ans = dp.get(root);
            // We want ans[1..n]
            StringBuilder sb = new StringBuilder();
            for (int k = 1; k <= n; k++) {
                long v = ans[k];
                // by logic, v cannot be INF for k<=n
                sb.append(v).append(k==n ? "\n" : " ");
            }
            System.out.print(sb);
        }
    }
}