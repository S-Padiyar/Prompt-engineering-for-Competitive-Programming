import java.io.*;
import java.util.*;

public class Main {
    static int N;
    static List<Integer>[] g;
    static int[] in, out, sz, parent, depth;
    static int timeDfs;
    // Euler‐tour gives us for each v, [in[v], out[v]] covering its subtree.
    // We also need heavy‐light decomposition data:
    static int[] heavy, head, pos, par;
    static int curPos;
    // Segment‐tree 1 over B[1..N] for range‐min / range‐max, point‐update.
    static class Seg1 {
        int n;
        int[] mn, mx;
        Seg1(int _n) {
            n = 1;
            while (n < _n) n <<= 1;
            mn = new int[2*n];
            mx = new int[2*n];
            Arrays.fill(mn, Integer.MAX_VALUE);
            Arrays.fill(mx, Integer.MIN_VALUE);
        }
        void update(int i, int v) {
            i += n-1;
            mn[i] = mx[i] = v;
            for (i >>=1; i>0; i>>=1) {
                mn[i] = Math.min(mn[2*i], mn[2*i+1]);
                mx[i] = Math.max(mx[2*i], mx[2*i+1]);
            }
        }
        // query range [l..r]
        int rangeMin(int l, int r) {  return rangeMin(1,1,n,l,r);  }
        int rangeMax(int l, int r) {  return rangeMax(1,1,n,l,r);  }
        private int rangeMin(int idx, int L, int R, int ql, int qr) {
            if (qr<L || R<ql) return Integer.MAX_VALUE;
            if (ql<=L && R<=qr) return mn[idx];
            int mid=(L+R)>>1;
            return Math.min(rangeMin(2*idx,L,mid,ql,qr),
                            rangeMin(2*idx+1,mid+1,R,ql,qr));
        }
        private int rangeMax(int idx, int L, int R, int ql, int qr) {
            if (qr<L || R<ql) return Integer.MIN_VALUE;
            if (ql<=L && R<=qr) return mx[idx];
            int mid=(L+R)>>1;
            return Math.max(rangeMax(2*idx,L,mid,ql,qr),
                            rangeMax(2*idx+1,mid+1,R,ql,qr));
        }
    }
    // Segment‐tree 2 over all v in Euler‐in‐order (1..N) storing F[v]; want global max F[v].
    static class Seg2 {
        int n;
        int[] tree;
        Seg2(int _n) {
            n = 1;
            while (n < _n) n <<= 1;
            tree = new int[2*n];
            Arrays.fill(tree, Integer.MIN_VALUE);
        }
        // set F at vIndex (1..N) to val
        void update(int vIndex, int val) {
            int i = vIndex + n - 1;
            tree[i] = val;
            for (i>>=1; i>0; i>>=1) {
                tree[i] = Math.max(tree[2*i], tree[2*i+1]);
            }
        }
        int queryMax() {
            return tree[1];
        }
    }
    // The two segment‐trees:
    static Seg1 seg1;
    static Seg2 seg2;
    // Current permutation p[1..N], and inv[] = where is each value
    static int[] p, invp;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        int T = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();
        while (T-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken()),
                q = Integer.parseInt(st.nextToken());
            N = n;
            // read tree
            g = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) g[i] = new ArrayList<>();
            parent = new int[n+1];
            st = new StringTokenizer(br.readLine());
            for (int i = 2; i <= n; i++) {
                int par = Integer.parseInt(st.nextToken());
                parent[i] = par;
                g[par].add(i);
            }
            parent[1] = 0;
            // allocate arrays
            in = new int[n+1];
            out = new int[n+1];
            sz = new int[n+1];
            depth = new int[n+1];
            heavy = new int[n+1];
            head = new int[n+1];
            pos = new int[n+1];
            par = new int[n+1];

            // 1) dfs to compute in[], out[], sz[], depth[], heavy-child
            timeDfs = 0;
            dfs1(1, 0, 0);
            // 2) HLD decompose
            curPos = 1;
            dfs2(1, 1);

            // read initial p
            p = new int[n+1];
            invp = new int[n+1];
            st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= n; i++) {
                p[i] = Integer.parseInt(st.nextToken());
                invp[p[i]] = i;
            }
            // build segment‐trees
            seg1 = new Seg1(n);
            seg2 = new Seg2(n);

            // Fill seg1 with B[in[v]] = invp[v]
            for (int v = 1; v <= n; v++) {
                seg1.update(in[v], invp[v]);
            }
            // Fill seg2 by computing F[v] for each v:
            for (int v = 1; v <= n; v++) {
                int mn = seg1.rangeMin(in[v], out[v]);
                int mx = seg1.rangeMax(in[v], out[v]);
                int f = mx - mn - (out[v] - in[v]);
                seg2.update(in[v], f);
            }

            // answer queries
            while (q-- > 0) {
                st = new StringTokenizer(br.readLine());
                int x = Integer.parseInt(st.nextToken());
                int y = Integer.parseInt(st.nextToken());
                // swap p[x], p[y]
                int vx = p[x], vy = p[y];
                p[x] = vy;  p[y] = vx;
                invp[vx] = y; invp[vy] = x;
                // Update seg1 at the two points in‐order:
                seg1.update(in[vx], invp[vx]);
                seg1.update(in[vy], invp[vy]);
                // Now we must re‐compute F[v] for exactly those v whose [in[v],out[v]] contains in[vx] or in[vy].
                // Those v are ancestors of vx in HLD plus ancestors of vy.
                updateAncestors(vx);
                updateAncestors(vy);

                // Finally check if global max F[*] == 0 and p[1]==1
                if (seg2.queryMax() == 0 && p[1] == 1) {
                    sb.append("YES\n");
                } else {
                    sb.append("NO\n");
                }
            }
        }
        System.out.print(sb);
    }

    // dfs1: compute size, parent, depth, in/out and heavy-child
    static void dfs1(int u, int parU, int d) {
        par[u] = parU;
        depth[u] = d;
        sz[u] = 1;
        in[u] = ++timeDfs;
        int maxSub = 0;
        for (int w : g[u]) {
            dfs1(w, u, d+1);
            if (sz[w] > maxSub) {
                maxSub = sz[w];
                heavy[u] = w;
            }
            sz[u] += sz[w];
        }
        out[u] = timeDfs;
    }

    // dfs2: decompose into heavy‐light chains
    static void dfs2(int u, int headU) {
        head[u] = headU;
        pos[u] = curPos++;
        if (heavy[u] != 0) {
            dfs2(heavy[u], headU);
        }
        for (int w : g[u]) {
            if (w != heavy[u]) {
                dfs2(w, w);
            }
        }
    }

    // Recompute F[v] = (max-min) - (subtreeSizeRange) for every v on path u→root
    // but we do it in O(log n) HLD segments; each v itself costs O(log n) to recompute.
    static void updateAncestors(int u) {
        while (u != 0) {
            int h = head[u];
            // we need to recompute F[v] for v in the chain [h..u] (along the chain)
            // but that is (u-h+1) nodes individually.  We can afford it in O(log n) 
            // per node times O(log n) segments = O(log²n).
            for (int v = u; v != par[h]; v = par[v]) {
                int mn = seg1.rangeMin(in[v], out[v]);
                int mx = seg1.rangeMax(in[v], out[v]);
                int f = mx - mn - (out[v] - in[v]);
                seg2.update(in[v], f);
            }
            u = par[h];
        }
    }
}