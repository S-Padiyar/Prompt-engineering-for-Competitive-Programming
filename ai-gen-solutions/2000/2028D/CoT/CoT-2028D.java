import java.io.*;
import java.util.*;

public class Main {
    static final int INF = Integer.MAX_VALUE / 2;

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder out = new StringBuilder();
        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            int n = Integer.parseInt(in.readLine().trim());
            // Read the three permutations p[0]=queen, p[1]=king, p[2]=jack.
            int[][] p = new int[3][n + 1];
            for (int who = 0; who < 3; who++) {
                StringTokenizer st = new StringTokenizer(in.readLine());
                for (int i = 1; i <= n; i++) {
                    p[who][i] = Integer.parseInt(st.nextToken());
                }
            }

            // Build 3 segment-trees, one per player, storing at leaf i the value p[who][i].
            SegTree[] seg = new SegTree[3];
            for (int who = 0; who < 3; who++) {
                seg[who] = new SegTree(n);
                for (int i = 1; i <= n; i++) {
                    seg[who].update(i, p[who][i]);
                }
            }

            // BFS structures
            boolean[] vis = new boolean[n + 1];
            int[] parent = new int[n + 1];
            int[] fromWhom = new int[n + 1];

            // Start from card 1
            vis[1] = true;
            for (int who = 0; who < 3; who++) {
                seg[who].update(1, -INF);  // remove it
            }

            int[] queue = new int[n];
            int qh = 0, qt = 0;
            queue[qt++] = 1;

            // BFS
            while (qh < qt) {
                int cur = queue[qh++];
                for (int who = 0; who < 3; who++) {
                    // we look for y > cur with p[who][y] > p[who][cur]
                    while (true) {
                        int y = seg[who].firstGreater(1, n, cur + 1, n, p[who][cur]);
                        if (y == -1) break;
                        // visit y
                        vis[y] = true;
                        parent[y] = cur;
                        fromWhom[y] = who;
                        queue[qt++] = y;
                        // remove y from all three trees so we never revisit
                        for (int w2 = 0; w2 < 3; w2++) {
                            seg[w2].update(y, -INF);
                        }
                    }
                }
            }

            if (!vis[n]) {
                out.append("NO\n");
            } else {
                out.append("YES\n");
                // Reconstruct the path of trades
                List<Integer> whoList = new ArrayList<>();
                List<Integer> cardList = new ArrayList<>();
                int cur = n;
                while (cur != 1) {
                    whoList.add(fromWhom[cur]);
                    cardList.add(cur);
                    cur = parent[cur];
                }
                int k = whoList.size();
                out.append(k).append("\n");
                for (int i = k - 1; i >= 0; i--) {
                    char c = (whoList.get(i) == 0 ? 'q' :
                              whoList.get(i) == 1 ? 'k' : 'j');
                    out.append(c).append(' ').append(cardList.get(i)).append("\n");
                }
            }
        }
        System.out.print(out);
    }

    // A simple segment-tree for range-maximum and point‐update.
    static class SegTree {
        int n;
        int[] st;

        SegTree(int _n) {
            n = _n;
            st = new int[4 * n + 4];
        }

        // point update: set position pos to value v
        void update(int pos, int v) {
            update(1, 1, n, pos, v);
        }

        private void update(int node, int L, int R, int pos, int v) {
            if (L == R) {
                st[node] = v;
                return;
            }
            int mid = (L + R) >>> 1;
            if (pos <= mid) update(node << 1, L, mid, pos, v);
            else update(node << 1 | 1, mid + 1, R, pos, v);
            st[node] = Math.max(st[node << 1], st[node << 1 | 1]);
        }

        // Find any index in [ql..qr] whose value exceeds 'thr', or -1 if none.
        int firstGreater(int node, int L, int R, int ql, int qr, int thr) {
            if (qr < L || R < ql || st[node] <= thr) return -1;
            if (L == R) return L;
            int mid = (L + R) >>> 1;
            int res = firstGreater(node << 1, L, mid, ql, qr, thr);
            if (res != -1) return res;
            return firstGreater(node << 1 | 1, mid + 1, R, ql, qr, thr);
        }
    }
}