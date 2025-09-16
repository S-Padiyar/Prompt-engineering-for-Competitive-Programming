import java.io.*;
import java.util.*;

public class Main {
    static final int MAXN = 200000 + 5;
    static final int LOG  = 18;  // since 2^18 > 200000

    static int n;
    static long w;
    static int[] p = new int[MAXN];
    static int[] depth = new int[MAXN];
    static int[] R     = new int[MAXN];
    static int[] U     = new int[MAXN];  // unknown‐edge count on path i->i+1
    static int[][] up  = new int[LOG][MAXN];

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int T = Integer.parseInt(br.readLine().trim());
        StringBuilder out = new StringBuilder();

        while (T-- > 0) {
            st = new StringTokenizer(br.readLine());
            n = Integer.parseInt(st.nextToken());
            w = Long.parseLong(st.nextToken());

            // read parents
            p[1] = 1;  // root's parent dummy
            st = new StringTokenizer(br.readLine());
            for (int i = 2; i <= n; i++) {
                p[i] = Integer.parseInt(st.nextToken());
            }

            // compute depth (since p[i] < i always, we can do in one pass)
            depth[1] = 0;
            for (int i = 2; i <= n; i++) {
                depth[i] = depth[ p[i] ] + 1;
            }

            // build binary‐lifting table up[0][i]=parent
            for (int i = 1; i <= n; i++) {
                up[0][i] = p[i];
            }
            for (int k = 1; k < LOG; k++) {
                for (int i = 1; i <= n; i++) {
                    up[k][i] = up[k-1][ up[k-1][i] ];
                }
            }

            // compute R[u] = max label in subtree of u
            for (int i = 1; i <= n; i++) {
                R[i] = i;
            }
            for (int i = n; i >= 2; i--) {
                int par = p[i];
                if (R[i] > R[par]) {
                    R[par] = R[i];
                }
            }

            // initialize U[i] = number of edges on path i->i+1, for i=1..n-1;
            // and U[n] = number of edges on path n->1 = depth[n].
            for (int i = 1; i < n; i++) {
                int j = i+1;
                int l = lca(i, j);
                U[i] = depth[i] + depth[j] - 2*depth[l];
            }
            U[n] = depth[n];

            // how many pairs are already fully known?
            int good = 0;
            for (int i = 1; i <= n; i++) {
                if (U[i] == 0) good++;
            }

            long S   = 0;      // sum of known‐on‐path weights
            long Rem = w;      // remaining total weight

            // process the n-1 events
            for (int e = 1; e < n; e++) {
                st = new StringTokenizer(br.readLine());
                int x   = Integer.parseInt(st.nextToken());
                long y  = Long.parseLong(st.nextToken());

                // this edge appears in exactly two of the cyclic‐adjacent paths:
                //   between (x-1, x)  if x>1   ⇒ index (x-1)
                //   and between (R[x], R[x]+1 mod n)
                // which in our 1..n cycle is "R[x]" if R[x]<n else "n" (i.e. the pair n->1).
                int i1 = x - 1;        // 1 <= i1 <= n-1
                int i2 = (R[x] < n ? R[x] : n);

                // reveal t_x = y
                Rem -= y;
                S   += 2 * y;  // each edge shows up in 2 cyclic‐adjacent distances

                // decrement the unknown‐edge count on those two paths
                if (i1 >= 1) {
                    U[i1]--;
                    if (U[i1] == 0) good++;
                }
                U[i2]--;
                if (U[i2] == 0) good++;

                // answer = S + Rem*(number of still‐unknown paths)
                long ans = S + Rem * ( (long)n - good );
                out.append(ans).append(' ');
            }
            out.append('\n');
        }

        System.out.print(out);
    }

    // classic binary‐lifting LCA
    static int lca(int a, int b) {
        if (depth[a] < depth[b]) {
            int t = a; a = b; b = t;
        }
        int diff = depth[a] - depth[b];
        for (int k = 0; k < LOG; k++) {
            if ((diff & (1<<k)) != 0) {
                a = up[k][a];
            }
        }
        if (a == b) return a;
        for (int k = LOG-1; k >= 0; k--) {
            if (up[k][a] != up[k][b]) {
                a = up[k][a];
                b = up[k][b];
            }
        }
        return up[0][a];
    }
}