import java.io.*;
import java.util.*;

public class Main {
    static final int INF = Integer.MAX_VALUE;
    static class SegTree {
        int n;
        int[] st;  // segment tree array for minimums

        public SegTree(int n) {
            this.n = n;
            // Safe size: 4*n
            st = new int[4 * n + 4];
        }

        // Build the tree with initial values a[1..n].
        void build(int[] a) {
            build(1, 1, n, a);
        }
        private void build(int node, int l, int r, int[] a) {
            if (l == r) {
                st[node] = a[l];
            } else {
                int mid = (l + r) >>> 1;
                build(node<<1,     l,   mid, a);
                build(node<<1 | 1, mid+1, r,   a);
                st[node] = Math.min(st[node<<1], st[node<<1|1]);
            }
        }

        // Point update: set position pos to value v
        void update(int pos, int v) {
            update(1, 1, n, pos, v);
        }
        private void update(int node, int l, int r, int pos, int v) {
            if (l == r) {
                st[node] = v;
            } else {
                int mid = (l + r) >>> 1;
                if (pos <= mid) update(node<<1, l, mid, pos, v);
                else            update(node<<1|1, mid+1, r, pos, v);
                st[node] = Math.min(st[node<<1], st[node<<1|1]);
            }
        }

        // Query min on interval [ql..qr]
        int queryMin(int ql, int qr) {
            return queryMin(1, 1, n, ql, qr);
        }
        private int queryMin(int node, int l, int r, int ql, int qr) {
            if (qr < l || r < ql) return INF;
            if (ql <= l && r <= qr) return st[node];
            int mid = (l + r) >>> 1;
            return Math.min(
                queryMin(node<<1,     l,   mid, ql, qr),
                queryMin(node<<1 | 1, mid+1, r,   ql, qr)
            );
        }

        // Find *any* index in [ql..qr] whose value < target.
        // If none, return -1.
        int findFirstLess(int ql, int qr, int target) {
            return findFirstLess(1, 1, n, ql, qr, target);
        }
        private int findFirstLess(int node, int l, int r, int ql, int qr, int target) {
            if (qr < l || r < ql || st[node] >= target) {
                return -1;
            }
            if (l == r) {
                // Leaf and st[node] < target
                return l;
            }
            int mid = (l + r) >>> 1;
            int res = findFirstLess(node<<1, l, mid, ql, qr, target);
            if (res != -1) return res;
            return findFirstLess(node<<1|1, mid+1, r, ql, qr, target);
        }
    }

    public static void main(String[] args) throws IOException {
        FastReader fr = new FastReader();
        PrintWriter out = new PrintWriter(System.out);

        int T = fr.nextInt();
        while (T-- > 0) {
            int n = fr.nextInt();
            // Read the three preference arrays for Queen, King, Jack
            int[][] pref = new int[3][n+1];
            for (int p = 0; p < 3; p++) {
                for (int i = 1; i <= n; i++) {
                    pref[p][i] = fr.nextInt();
                }
            }

            // Build three segment trees, one per player
            SegTree[] seg = new SegTree[3];
            for (int p = 0; p < 3; p++) {
                seg[p] = new SegTree(n);
                seg[p].build(pref[p]);
            }

            // BFS state
            boolean[] visited = new boolean[n+1];
            // parent[v] = (previous node, which player used)
            int[][] parent = new int[n+1][2];

            // Start BFS from node 1
            Deque<Integer> dq = new ArrayDeque<>();
            visited[1] = true;
            dq.addLast(1);

            // We never need node '1' in any segment tree, so we can remove it right away
            // (Optional since we only search b>c).
            for (int p = 0; p < 3; p++) {
                seg[p].update(1, INF);
            }

            while (!dq.isEmpty()) {
                int c = dq.pollFirst();
                // For each player p = 0(Q),1(K),2(J)
                for (int p = 0; p < 3; p++) {
                    // We can only move to b>c with pref[p][b] < pref[p][c]
                    while (true) {
                        // What's the minimum preference value in [c+1..n]?
                        if (c+1 > n) break;
                        int mn = seg[p].queryMin(c+1, n);
                        if (mn >= pref[p][c]) {
                            // no more b satisfy pref[b] < pref[c]
                            break;
                        }
                        // find some b
                        int b = seg[p].findFirstLess(c+1, n, pref[p][c]);
                        // mark b visited and remove from all three trees
                        visited[b] = true;
                        parent[b][0] = c;
                        parent[b][1] = p;
                        for (int pp = 0; pp < 3; pp++) {
                            seg[pp].update(b, INF);
                        }
                        dq.addLast(b);
                    }
                }
            }

            if (!visited[n]) {
                out.println("NO");
            } else {
                out.println("YES");
                // Reconstruct path by tracing parents from n back to 1
                List<String> moves = new ArrayList<>();
                int cur = n;
                char[] who = {'q','k','j'};
                while (cur != 1) {
                    int par = parent[cur][0];
                    int p   = parent[cur][1];
                    moves.add(who[p] + " " + cur);
                    cur = par;
                }
                Collections.reverse(moves);
                out.println(moves.size());
                for (String mv : moves) {
                    out.println(mv);
                }
            }
        }
        out.flush();
    }

    // Fast IO
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
        FastReader() {
            br = new BufferedReader(new InputStreamReader(System.in));
        }
        String next() {
            while (st == null || !st.hasMoreElements()) {
                try {
                    String line = br.readLine();
                    if (line == null) return null;
                    st = new StringTokenizer(line);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }
        int nextInt() {
            return Integer.parseInt(next());
        }
    }
}