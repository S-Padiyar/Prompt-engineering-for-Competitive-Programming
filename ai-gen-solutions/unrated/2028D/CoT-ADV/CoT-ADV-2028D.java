import java.io.*;
import java.util.*;

public class Main {
    static final int INF = Integer.MAX_VALUE;

    // Segment Tree supporting:
    //  - point updates: set position i to value v
    //  - range-min queries + "find first index in [ql..qr] with value <= thresh"
    static class SegmentTree {
        int n;
        int[] st;

        SegmentTree(int size) {
            n = size;
            st = new int[4 * n];
        }

        // Build from array arr[1..n]
        void build(int node, int l, int r, int[] arr) {
            if (l == r) {
                st[node] = arr[l];
            } else {
                int mid = (l + r) >>> 1;
                build(node << 1, l, mid, arr);
                build(node << 1 | 1, mid + 1, r, arr);
                st[node] = Math.min(st[node << 1], st[node << 1 | 1]);
            }
        }

        // Point update: set position pos to value val
        void update(int node, int l, int r, int pos, int val) {
            if (l == r) {
                st[node] = val;
            } else {
                int mid = (l + r) >>> 1;
                if (pos <= mid) update(node << 1, l, mid, pos, val);
                else           update(node << 1 | 1, mid + 1, r, pos, val);
                st[node] = Math.min(st[node << 1], st[node << 1 | 1]);
            }
        }

        // Find first index in [ql..qr] whose stored value <= thresh.
        // Returns -1 if none.
        int findFirst(int node, int l, int r, int ql, int qr, int thresh) {
            if (qr < l || r < ql || st[node] > thresh) {
                return -1;
            }
            if (l == r) {
                return l;
            }
            int mid = (l + r) >>> 1;
            int res = findFirst(node << 1, l, mid, ql, qr, thresh);
            if (res != -1) return res;
            return findFirst(node << 1 | 1, mid + 1, r, ql, qr, thresh);
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());

            // Read preferences for Q, K, J
            int[][] pref = new int[3][n + 1];
            for (int p = 0; p < 3; p++) {
                StringTokenizer st = new StringTokenizer(br.readLine());
                for (int i = 1; i <= n; i++) {
                    pref[p][i] = Integer.parseInt(st.nextToken());
                }
            }

            // Build segment trees for each player
            SegmentTree[] seg = new SegmentTree[3];
            for (int p = 0; p < 3; p++) {
                seg[p] = new SegmentTree(n);
                seg[p].build(1, 1, n, pref[p]);
            }

            // BFS structures
            boolean[] visited = new boolean[n + 1];
            int[] parent = new int[n + 1];
            int[] parentPlayer = new int[n + 1];  // 0->Q,1->K,2->J

            Deque<Integer> dq = new ArrayDeque<>();
            visited[1] = true;
            dq.addLast(1);

            // BFS: from card 'a', try to take all b>a s.t. pref[p][a] > pref[p][b]
            while (!dq.isEmpty()) {
                int a = dq.removeFirst();
                for (int p = 0; p < 3; p++) {
                    // We want pref[p][b] <= pref[p][a] - 1
                    int threshold = pref[p][a] - 1;
                    while (true) {
                        int b = seg[p].findFirst(1, 1, n, a + 1, n, threshold);
                        if (b == -1) break;
                        // Mark visited: we can now reach card b
                        visited[b] = true;
                        parent[b] = a;
                        parentPlayer[b] = p;
                        dq.addLast(b);
                        // Remove b from further consideration in player p's tree
                        seg[p].update(1, 1, n, b, INF);
                    }
                }
            }

            if (!visited[n]) {
                out.println("NO");
            } else {
                out.println("YES");
                // Reconstruct path from n back to 1
                List<Integer> moves = new ArrayList<>();
                List<Integer> who = new ArrayList<>();
                int cur = n;
                while (cur != 1) {
                    moves.add(cur);
                    who.add(parentPlayer[cur]);
                    cur = parent[cur];
                }
                Collections.reverse(moves);
                Collections.reverse(who);

                out.println(moves.size());
                char[] playerChar = {'q','k','j'};
                for (int i = 0; i < moves.size(); i++) {
                    out.printf("%c %d\n", playerChar[who.get(i)], moves.get(i));
                }
            }
        }

        out.flush();
        out.close();
    }
}