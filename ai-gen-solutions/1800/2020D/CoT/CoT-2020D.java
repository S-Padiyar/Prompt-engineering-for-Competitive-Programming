import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // Fast input
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
 
        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());
            
            // Prepare to bucket operations by their d-value (1..10)
            @SuppressWarnings("unchecked")
            ArrayList<Op>[] opsByD = new ArrayList[11];
            for (int d = 1; d <= 10; d++) {
                opsByD[d] = new ArrayList<>();
            }
            
            // Read operations
            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(br.readLine());
                int a = Integer.parseInt(st.nextToken());
                int d = Integer.parseInt(st.nextToken());
                int k = Integer.parseInt(st.nextToken());
                opsByD[d].add(new Op(a, d, k));
            }
 
            // Initialize DSU for 0..n-1
            DSU dsu = new DSU(n);
 
            // Process each d = 1..10 separately
            for (int d = 1; d <= 10; d++) {
                if (opsByD[d].isEmpty()) continue;
                
                // intervals[r] will collect (t_start, t_end) for residue r mod d
                @SuppressWarnings("unchecked")
                ArrayList<Interval>[] intervals = new ArrayList[d];
                for (int r = 0; r < d; r++) {
                    intervals[r] = new ArrayList<>();
                }
                
                // Bucket each operation into its residue class
                for (Op op : opsByD[d]) {
                    int a0 = op.a - 1;        // zero-based
                    int r = a0 % d;           // residue
                    int t0 = a0 / d;          // starting index in that residue-class
                    intervals[r].add(new Interval(t0, t0 + op.k));
                }
                
                // For each residue, merge intervals and do the needed unions
                for (int r = 0; r < d; r++) {
                    ArrayList<Interval> lst = intervals[r];
                    if (lst.isEmpty()) continue;
                    
                    lst.sort(Comparator.comparingInt(x -> x.l));
                    
                    int cl = -1, cr = -1;   // current merged interval
                    for (Interval iv : lst) {
                        if (cl < 0) {
                            cl = iv.l; 
                            cr = iv.r;
                        } else if (iv.l <= cr + 1) {
                            // overlap or touching
                            cr = Math.max(cr, iv.r);
                        } else {
                            // push the previous block [cl,cr]
                            applyUnions(dsu, r, d, cl, cr);
                            cl = iv.l;
                            cr = iv.r;
                        }
                    }
                    // final flush
                    if (cl >= 0) {
                        applyUnions(dsu, r, d, cl, cr);
                    }
                }
            }
 
            // Count connected components
            int comps = 0;
            for (int i = 0; i < n; i++) {
                if (dsu.find(i) == i) comps++;
            }
            sb.append(comps).append('\n');
        }
        
        // Output all answers
        System.out.print(sb);
    }
    
    /** For a merged interval [L,R] in one residue-class,
        union (r + t*d) with (r + (t+1)*d) for t=L..R-1. */
    static void applyUnions(DSU dsu, int r, int d, int L, int R) {
        for (int t = L; t < R; t++) {
            int v1 = r + t * d;
            int v2 = r + (t + 1) * d;
            dsu.union(v1, v2);
        }
    }
    
    // A single operation (a, d, k)
    static class Op {
        int a, d, k;
        Op(int a, int d, int k) { this.a = a; this.d = d; this.k = k; }
    }
    
    // Interval on the t-axis
    static class Interval {
        int l, r;
        Interval(int l, int r) { this.l = l; this.r = r; }
    }
    
    // Standard DSU with path‐compression and union by rank
    static class DSU {
        int[] parent, rank;
        DSU(int n) {
            parent = new int[n];
            rank = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
                rank[i] = 0;
            }
        }
        int find(int x) {
            // iterative path‐compression
            int root = x;
            while (parent[root] != root) {
                root = parent[root];
            }
            while (x != root) {
                int p = parent[x];
                parent[x] = root;
                x = p;
            }
            return root;
        }
        void union(int x, int y) {
            int rx = find(x), ry = find(y);
            if (rx == ry) return;
            if (rank[rx] < rank[ry]) {
                parent[rx] = ry;
            } else if (rank[rx] > rank[ry]) {
                parent[ry] = rx;
            } else {
                parent[ry] = rx;
                rank[rx]++;
            }
        }
    }
}