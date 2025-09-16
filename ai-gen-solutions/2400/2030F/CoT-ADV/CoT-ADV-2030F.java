import java.io.*;
import java.util.*;

public class Main {
    static final int INF = Integer.MAX_VALUE / 2;

    // A simple segment tree for point updates and range-min queries
    static class SegTree {
        int n;
        int[] st;
        SegTree(int _n) {
            n = _n;
            st = new int[4*n];
            Arrays.fill(st, INF);
        }
        // point update: set position pos to value val
        void update(int node, int L, int R, int pos, int val) {
            if (L == R) {
                st[node] = val;
                return;
            }
            int mid = (L+R) >>> 1;
            if (pos <= mid) update(node<<1, L, mid, pos, val);
            else            update(node<<1|1, mid+1, R, pos, val);
            st[node] = Math.min(st[node<<1], st[node<<1|1]);
        }
        void update(int pos, int val) {
            update(1, 1, n, pos, val);
        }
        // query min on [i..j]
        int query(int node, int L, int R, int i, int j) {
            if (j < L || R < i) return INF;
            if (i <= L && R <= j) return st[node];
            int mid = (L+R) >>> 1;
            return Math.min(
                query(node<<1, L, mid, i, j),
                query(node<<1|1, mid+1, R, i, j));
        }
        int query(int i, int j) {
            if (i > j) return INF;
            return query(1, 1, n, i, j);
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder output = new StringBuilder();
        int T = Integer.parseInt(br.readLine().trim());

        while (T-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int q = Integer.parseInt(st.nextToken());
            st = new StringTokenizer(br.readLine());
            int[] a = new int[n+1];
            for (int i = 1; i <= n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            // 1) Compute last occurrence of each value
            int[] lastOcc = new int[n+1];
            for (int i = 1; i <= n; i++) {
                lastOcc[i] = 0;
            }
            for (int i = 1; i <= n; i++) {
                lastOcc[a[i]] = i;
            }
            // lastpos[i] = last occurrence of value a[i]
            int[] lastpos = new int[n+1];
            for (int i = 1; i <= n; i++) {
                lastpos[i] = lastOcc[a[i]];
            }

            // 2) Compute next occurrence of each a[i]
            int[] nextpos = new int[n+1];
            for (int v = 1; v <= n; v++) nextpos[v] = n+1;
            for (int i = n; i >= 1; i--) {
                nextpos[i] = nextpos[a[i]];
                nextpos[a[i]] = i;
            }

            // 3) Prepare queries for each i -> we want badR[i]
            class Q { int i, L, R, t; }
            ArrayList<Q> qlist = new ArrayList<>();

            for (int i = 1; i <= n; i++) {
                int nxt = nextpos[i];
                if (nxt <= n) {
                    int L = i+1, R = nxt-1;
                    if (L <= R) {
                        Q qq = new Q();
                        qq.i = i; qq.L = L; qq.R = R; qq.t = nxt;
                        qlist.add(qq);
                    }
                }
            }
            // sort queries by descending t
            qlist.sort((x,y) -> Integer.compare(y.t, x.t));

            // 4) Prepare positions j sorted by lastpos[j] descending
            Integer[] orderJ = new Integer[n];
            for (int i = 0; i < n; i++) orderJ[i] = i+1;
            Arrays.sort(orderJ, (x,y) -> Integer.compare(lastpos[y], lastpos[x]));

            // 5) Sweep to fill badR[i]
            SegTree st1 = new SegTree(n);
            int ptr = 0;
            int[] badR = new int[n+1];
            Arrays.fill(badR, n+1);

            for (Q qq : qlist) {
                // insert all j with lastpos[j] > qq.t
                while (ptr < n && lastpos[ orderJ[ptr] ] > qq.t) {
                    int j = orderJ[ptr];
                    st1.update(j, lastpos[j]);
                    ptr++;
                }
                // now query range L..R for minimal lastpos
                int best = st1.query(qq.L, qq.R);
                if (best <= n) {
                    // we found an interleaving reaching 'best'
                    badR[qq.i] = Math.min(badR[qq.i], best);
                }
            }

            // 6) Build a second segment tree over badR[]
            SegTree st2 = new SegTree(n);
            for (int i = 1; i <= n; i++) {
                st2.update(i, badR[i]);
            }

            // 7) Answer the input queries
            for (int _q = 0; _q < q; _q++) {
                st = new StringTokenizer(br.readLine());
                int L = Integer.parseInt(st.nextToken());
                int R = Integer.parseInt(st.nextToken());
                // if the minimum badR in [L..R] > R, it's clean
                int mn = st2.query(L, R);
                output.append( (mn > R) ? "YES\n" : "NO\n" );
            }
        }

        System.out.print(output);
    }
}