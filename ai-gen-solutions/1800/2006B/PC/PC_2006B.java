import java.io.*;
import java.util.*;

public class Main {
    static int N;
    static long W;
    static int[] parent, depth, sz, heavy, head, pos, rightEnd;
    static List<Integer>[] children;
    static Fenwick bit;
    static int curt;
    static long sumKnown;
    static int fullyKnownCnt;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int T = Integer.parseInt(br.readLine().trim());
        StringBuilder out = new StringBuilder();

        while (T-- > 0) {
            st = new StringTokenizer(br.readLine());
            N = Integer.parseInt(st.nextToken());
            W = Long.parseLong(st.nextToken());

            parent = new int[N+1];
            depth  = new int[N+1];
            sz      = new int[N+1];
            heavy   = new int[N+1];
            head    = new int[N+1];
            pos     = new int[N+1];
            rightEnd= new int[N+1];
            children= new List[N+1];

            for (int i = 1; i <= N; i++) {
                children[i] = new ArrayList<>();
                sz[i] = 1;
            }

            // read parents p[2..N]
            st = new StringTokenizer(br.readLine());
            for (int i = 2; i <= N; i++) {
                int p = Integer.parseInt(st.nextToken());
                parent[i] = p;
                children[p].add(i);
            }
            parent[1] = 0;
            depth[1] = 0;
            // depths come easily since p[i] < i
            for (int i = 2; i <= N; i++) {
                depth[i] = depth[parent[i]] + 1;
            }

            // compute subtree sizes
            for (int i = N; i >= 2; i--) {
                sz[parent[i]] += sz[i];
            }
            // compute rightEnd = i + subtree_size[i] - 1
            for (int i = 1; i <= N; i++) {
                rightEnd[i] = i + sz[i] - 1;
            }

            // find heavy child
            for (int u = 1; u <= N; u++) {
                int maxSize = 0, hv = 0;
                for (int v : children[u]) {
                    if (sz[v] > maxSize) {
                        maxSize = sz[v];
                        hv = v;
                    }
                }
                heavy[u] = hv;
            }

            // HLD: assign head[] and pos[]
            curt = 0;
            bit  = new Fenwick(N);
            dfsDecompose();

            // initially all edges unknown => BIT[x]=1 for x>1
            for (int x = 2; x <= N; x++) {
                bit.update(pos[x], 1);
            }

            sumKnown = 0L;
            fullyKnownCnt = 0;

            // process the n-1 events
            int events = N - 1;
            for (int e = 0; e < events; e++) {
                st = new StringTokenizer(br.readLine());
                int x = Integer.parseInt(st.nextToken());
                long y = Long.parseLong(st.nextToken());

                // the two pairs that use edge x:
                //   (x-1,x)   and   (r_x, r_x+1 mod n)
                int u1 = x - 1, v1 = x;
                if (pathSum(u1, v1) == 1) {
                    fullyKnownCnt++;
                }
                int r = rightEnd[x];
                int u2 = r;
                int v2 = (r == N ? 1 : r + 1);
                if (pathSum(u2, v2) == 1) {
                    fullyKnownCnt++;
                }

                // now mark edge x as known
                bit.update(pos[x], -1);
                sumKnown += y;

                // compute answer
                // 2*sumKnown + (W - sumKnown)*(N - fullyKnownCnt)
                long rem = W - sumKnown;
                long ans = 2L*sumKnown + rem*(N - fullyKnownCnt);
                out.append(ans).append(' ');
            }
            out.append('\n');
        }

        System.out.print(out);
    }

    // Heavy-Light Decomposition: assign head[u], pos[u] by a manual stack
    static void dfsDecompose() {
        Stack<Frame> st = new Stack<>();
        st.push(new Frame(1, 1, 0, 0));

        while (!st.isEmpty()) {
            Frame f = st.pop();
            int u = f.u;
            if (f.state == 0) {
                // first time at u
                head[u] = f.h;
                pos[u]  = ++curt;
                // process heavy child first
                if (heavy[u] != 0) {
                    // after heavy, return to light‐child stage
                    st.push(new Frame(u, f.h, 1, 0));
                    st.push(new Frame(heavy[u], f.h, 0, 0));
                } else {
                    // no heavy, proceed to light
                    st.push(new Frame(u, f.h, 1, 0));
                }
            } else {
                // state==1 => do light children in order
                int szc = children[u].size();
                boolean pushed = false;
                for (int i = f.nextChild; i < szc; i++) {
                    int v = children[u].get(i);
                    if (v == heavy[u]) continue;
                    // resume at nextChild = i+1
                    st.push(new Frame(u, f.h, 1, i+1));
                    st.push(new Frame(v, v, 0, 0));
                    pushed = true;
                    break;
                }
                // if none left, we pop off f
            }
        }
    }

    // sum of the "unknown" bits on the path u->v
    static int pathSum(int u, int v) {
        int res = 0;
        while (head[u] != head[v]) {
            if (depth[head[u]] >= depth[head[v]]) {
                res += bit.rangeSum(pos[head[u]], pos[u]);
                u = parent[head[u]];
            } else {
                res += bit.rangeSum(pos[head[v]], pos[v]);
                v = parent[head[v]];
            }
        }
        // now on same chain
        if (u != v) {
            if (depth[u] > depth[v]) {
                // sum from pos[v]+1 .. pos[u]
                res += bit.rangeSum(pos[v]+1, pos[u]);
            } else {
                // sum from pos[u]+1 .. pos[v]
                res += bit.rangeSum(pos[u]+1, pos[v]);
            }
        }
        return res;
    }

    // Fenwick (BIT) for point‐update, prefix‐sum
    static class Fenwick {
        int n;
        int[] f;
        Fenwick(int n) { this.n = n; f = new int[n+1]; }
        void update(int i, int d) {
            for (; i <= n; i += i&-i) f[i] += d;
        }
        int query(int i) {
            int s = 0;
            for (; i > 0; i -= i&-i) s += f[i];
            return s;
        }
        int rangeSum(int l, int r) {
            if (l>r) return 0;
            return query(r) - query(l-1);
        }
    }

    // a little helper for the manual‐stack HLD
    static class Frame {
        int u, h, state, nextChild;
        Frame(int _u, int _h, int _st, int _nc) {
            u = _u; h = _h; state = _st; nextChild = _nc;
        }
    }
}