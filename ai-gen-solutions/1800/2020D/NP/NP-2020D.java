import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder   sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(br.readLine());
        int t = Integer.parseInt(st.nextToken());

        // Process each test case
        while (t-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());

            // --- 1) Prepare the Node‐DSU for 0..n-1
            int[] parent = new int[n], rank = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
                rank[i] = 0;
            }
            int components = n;

            // --- 2) Prepare the Edge‐DSU for each (d, r)
            //     parentEdge[d][r] is an int[] of length L(d,r),
            //     which merges edges in the chain of points {r, r+d, r+2d, ...}.
            //     L(d,r) = floor((n-1 - r)/d) + 1.
            int[][][] parentEdge = new int[11][][]; // d = 1..10
            for (int d = 1; d <= 10; d++) {
                parentEdge[d] = new int[d][];
                for (int r = 0; r < d; r++) {
                    if (r >= n) {
                        parentEdge[d][r] = new int[0];
                    } else {
                        int len = ((n - 1 - r) / d) + 1;  // number of points in chain
                        parentEdge[d][r] = new int[len];
                        for (int i = 0; i < len; i++) {
                            parentEdge[d][r][i] = i;
                        }
                    }
                }
            }

            // DSU-find for nodes (with path‐compression)
            final class NodeDSU {
                int find(int x) {
                    while (parent[x] != x) {
                        parent[x] = parent[parent[x]];
                        x = parent[x];
                    }
                    return x;
                }
                boolean union(int x, int y) {
                    int rx = find(x), ry = find(y);
                    if (rx == ry) return false;
                    if (rank[rx] < rank[ry]) {
                        parent[rx] = ry;
                    } else if (rank[ry] < rank[rx]) {
                        parent[ry] = rx;
                    } else {
                        parent[ry] = rx;
                        rank[rx]++;
                    }
                    return true;
                }
            }
            NodeDSU nodeDSU = new NodeDSU();

            // DSU-find for edges in a single chain (iterative + path‐compression)
            // We will write a helper that closes over parentEdge[d][r].
            // findEdge returns the smallest "edge index" >= x that is still its own parent.
            class EdgeDSU {
                int[] p;
                EdgeDSU(int[] arr) { p = arr; }
                int find(int x) {
                    int root = x;
                    while (p[root] != root) {
                        root = p[root];
                    }
                    // path-compress
                    int cur = x;
                    while (p[cur] != cur) {
                        int nxt = p[cur];
                        p[cur] = root;
                        cur = nxt;
                    }
                    return root;
                }
                void unite(int x, int y) {
                    int rx = find(x), ry = find(y);
                    p[rx] = ry;
                }
            }

            // --- 3) Process the m operations
            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(br.readLine());
                int a = Integer.parseInt(st.nextToken()) - 1;  // 0-based
                int d = Integer.parseInt(st.nextToken());
                int k = Integer.parseInt(st.nextToken());

                int r   = a % d;        // which chain
                int t0  = (a - r) / d;  // starting index in that chain
                int tEnd = t0 + k;      // we need edges t0..tEnd-1

                int[]    edgeArr = parentEdge[d][r];
                EdgeDSU  edgeDSU = new EdgeDSU(edgeArr);

                while (true) {
                    int u = edgeDSU.find(t0);
                    if (u >= tEnd) break;
                    // edge u in the chain joins pointAt(u) with pointAt(u+1)
                    int x = r + u*d;
                    int y = x + d;
                    if (nodeDSU.union(x, y)) {
                        components--;
                    }
                    // mark edge u as processed by unioning it with u+1
                    edgeDSU.unite(u, u+1);
                }
            }

            sb.append(components).append('\n');
        }

        // print all results
        System.out.print(sb);
    }
}