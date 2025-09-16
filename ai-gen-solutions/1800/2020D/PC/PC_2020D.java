import java.io.*;
import java.util.*;

public class Main {
    static class DSU {
        int[] p, sz;
        DSU(int n) {
            p = new int[n+1];
            sz = new int[n+1];
            for(int i = 1; i <= n; i++){
                p[i] = i;
                sz[i] = 1;
            }
        }
        int find(int x) {
            return p[x] == x ? x : (p[x] = find(p[x]));
        }
        void unite(int a, int b) {
            a = find(a);
            b = find(b);
            if (a == b) return;
            // union by size
            if (sz[a] < sz[b]) {
                p[a] = b;
                sz[b] += sz[a];
            } else {
                p[b] = a;
                sz[a] += sz[b];
            }
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

            // Prepare DSU
            DSU dsu = new DSU(n);

            // We'll only need d up to min(10, n).
            int maxD = Math.min(10, n);

            // diff[d][r] will be the difference-array for residue-class r mod d.
            // We first allocate each chain's array according to its length.
            int[][][] diff = new int[maxD+1][][];
            int[][] chainLen = new int[maxD+1][ ];
            for(int d = 1; d <= maxD; d++){
                diff[d] = new int[d][ ];
                chainLen[d] = new int[d];
                for(int r = 0; r < d; r++){
                    // The smallest number >=1 which is congruent to r mod d:
                    //   if r==0, that number is d; else it is r.
                    int base = (r==0 ? d : r);
                    if (base > n) {
                        chainLen[d][r] = 0;
                        diff[d][r] = new int[0];
                    } else {
                        int len = (n - base) / d + 1;
                        chainLen[d][r] = len;
                        diff[d][r] = new int[len];
                        // initially all zero
                    }
                }
            }

            // Read operations, populate difference arrays
            for(int i = 0; i < m; i++){
                st = new StringTokenizer(br.readLine());
                int a = Integer.parseInt(st.nextToken());
                int d = Integer.parseInt(st.nextToken());
                int k = Integer.parseInt(st.nextToken());

                if (d > maxD) {
                    // d>n or >10 cannot connect anything meaningful
                    continue;
                }

                int r = a % d;
                int base = (r == 0 ? d : r);
                int startIdx = (a - base) / d;
                int endIdx = startIdx + k; // inclusive for vertex indices

                // We only want to connect consecutive pairs in [startIdx .. endIdx].
                // The edges are between positions j and j+1 for j in [startIdx..endIdx-1].
                // So we mark diff[startIdx] +=1, diff[endIdx] -=1.
                // Later prefix-sum>0 at position j means there is an edge (j, j+1).
                diff[d][r][startIdx]++;
                if (endIdx < chainLen[d][r]) {
                    diff[d][r][endIdx]--;
                }
            }

            // Now sweep each chain, build prefix sums, and perform DSU-union on covered edges
            for(int d = 1; d <= maxD; d++){
                for(int r = 0; r < d; r++){
                    int len = chainLen[d][r];
                    if (len <= 1) continue;  // no edges in a chain of length 0 or 1
                    int cur = 0;
                    int base = (r == 0 ? d : r);
                    for(int j = 0; j < len - 1; j++){
                        cur += diff[d][r][j];
                        if (cur > 0) {
                            int u = base + j*d;
                            int v = u + d;
                            dsu.unite(u, v);
                        }
                    }
                }
            }

            // Finally count how many distinct DSU-roots among 1..n
            int comps = 0;
            for(int i = 1; i <= n; i++){
                if (dsu.find(i) == i) comps++;
            }
            sb.append(comps).append('\n');
        }

        System.out.print(sb);
    }
}