import java.io.*;
import java.util.*;

public class Main {
    static class DSU {
        int[] parent, rank;
        int count;
        DSU(int n) {
            parent = new int[n+1];
            rank   = new int[n+1];
            for (int i = 1; i <= n; i++) {
                parent[i] = i;
                rank[i] = 0;
            }
            count = n;
        }
        int find(int x) {
            // iterative find with path‐compression
            int root = x;
            while (parent[root] != root) {
                root = parent[root];
            }
            while (parent[x] != x) {
                int p = parent[x];
                parent[x] = root;
                x = p;
            }
            return root;
        }
        void union(int a, int b) {
            int ra = find(a), rb = find(b);
            if (ra == rb) return;
            // union by rank
            if (rank[ra] < rank[rb]) {
                parent[ra] = rb;
            } else if (rank[ra] > rank[rb]) {
                parent[rb] = ra;
            } else {
                parent[rb] = ra;
                rank[ra]++;
            }
            count--;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());

            // Initialize DSU for the n points:
            DSU dsu = new DSU(n);

            // nextEdge[d][i] = the smallest j >= i such that edge (j, j+d) is still unprocessed.
            // We'll keep it of size (n+2), 1-based.
            int[][] nextEdge = new int[11][n+2];
            for (int d = 1; d <= 10; d++) {
                for (int i = 1; i <= n+1; i++) {
                    nextEdge[d][i] = i;
                }
            }

            // Helper to find next unprocessed start >= x for difference d
            // with path compression:
            class Finder {
                int findNext(int d, int x) {
                    int r = x;
                    while (r <= n && nextEdge[d][r] != r) {
                        r = nextEdge[d][r];
                    }
                    // path-compress
                    int cur = x;
                    while (cur <= n && nextEdge[d][cur] != cur) {
                        int tmp = nextEdge[d][cur];
                        nextEdge[d][cur] = r;
                        cur = tmp;
                    }
                    return r;
                }
            }
            Finder finder = new Finder();

            // Process m operations
            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(br.readLine());
                int a = Integer.parseInt(st.nextToken());
                int d = Integer.parseInt(st.nextToken());
                int k = Integer.parseInt(st.nextToken());
                int last = a + k*d;

                // We want to add edges (x, x+d) for x = a, a+d, ... , a+(k-1)*d
                // but skip any already done.
                int x = finder.findNext(d, a);
                while (x + d <= last) {
                    dsu.union(x, x + d);
                    // mark this x as done, by pointing it forward
                    int nx = finder.findNext(d, x + d);
                    nextEdge[d][x] = nx;
                    x = nx;
                }
            }

            // The DSU.count field is the number of connected components.
            sb.append(dsu.count).append('\n');
        }
        System.out.print(sb);
    }
}