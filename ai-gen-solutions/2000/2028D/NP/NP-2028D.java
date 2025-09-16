import java.io.*;
import java.util.*;

public class Main {
    static final int INF = Integer.MAX_VALUE;

    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader();
        PrintWriter out = new PrintWriter(System.out);

        int t = in.nextInt();
        while (t-- > 0) {
            int n = in.nextInt();
            // Read the three permutations p[0]=Queen, p[1]=King, p[2]=Jack
            int[][] p = new int[3][n+1];
            for (int c = 0; c < 3; c++) {
                for (int i = 1; i <= n; i++) {
                    p[c][i] = in.nextInt();
                }
            }

            // Build three segment-trees, each over [1..n], storing (minPrefValue, position).
            SegTree[] seg = new SegTree[3];
            for (int c = 0; c < 3; c++) {
                seg[c] = new SegTree(n);
                seg[c].build(1, 1, n, p[c]);
            }

            // BFS structures
            boolean[] visited = new boolean[n+1];
            int[] parent = new int[n+1];    // predecessor in the BFS tree
            int[] how = new int[n+1];       // which opponent (0,1,2) we traded with to get here

            // Start BFS from card 1
            Deque<Integer> queue = new ArrayDeque<>();
            visited[1] = true;
            // remove card 1 from all segment-trees
            for (int c = 0; c < 3; c++) {
                seg[c].update(1, 1, n, 1, INF);
            }
            queue.add(1);

            while (!queue.isEmpty()) {
                int a = queue.poll();
                if (a == n) break; // already reached n
                // For each opponent c, try to find all x > a with p[c][x] < p[c][a].
                for (int c = 0; c < 3; c++) {
                    while (true) {
                        // query the minimum p[c][x] in the range [a+1..n]
                        if (a+1 > n) break;
                        Pair best = seg[c].query(1, 1, n, a+1, n);
                        if (best.pref >= p[c][a]) break; // no more valid trades with this c
                        int x = best.pos;
                        // visit x
                        visited[x] = true;
                        parent[x] = a;
                        how[x] = c; // we got to x by trading with opponent c
                        queue.add(x);
                        // remove x from all three segment-trees so we never revisit
                        for (int d = 0; d < 3; d++) {
                            seg[d].update(1, 1, n, x, INF);
                        }
                    }
                }
            }

            if (!visited[n]) {
                out.println("NO");
            } else {
                // Reconstruct path from 1 to n
                List<Pair> path = new ArrayList<>();
                int cur = n;
                while (cur != 1) {
                    path.add(new Pair(how[cur], cur));
                    cur = parent[cur];
                }
                Collections.reverse(path);

                out.println("YES");
                out.println(path.size());
                for (Pair step : path) {
                    char who = (step.pref==0 ? 'q' : step.pref==1 ? 'k' : 'j');
                    out.println(who + " " + step.pos);
                }
            }
        }

        out.flush();
    }

    // Utility pair to hold (minPrefValue, position) or (opponent, position)
    static class Pair {
        int pref, pos;
        Pair(int p, int x) { pref = p; pos = x; }
    }

    // A segment tree that on each node stores the minimum preference value in that segment
    // and the position where that minimum occurs.
    static class SegTree {
        int n;
        int[] minv;  // minimum preference in this node's interval
        int[] idx;   // position of that minimum

        SegTree(int size) {
            n = size;
            minv = new int[4*size + 5];
            idx  = new int[4*size + 5];
        }

        // Build from the array pref[1..n]
        void build(int node, int L, int R, int[] pref) {
            if (L == R) {
                minv[node] = pref[L];
                idx[node]  = L;
            } else {
                int mid = (L+R)>>1;
                build(node<<1,    L, mid, pref);
                build(node<<1|1, mid+1, R, pref);
                pull(node);
            }
        }

        // Pull up info after children updates
        void pull(int node) {
            if (minv[node<<1] <= minv[node<<1|1]) {
                minv[node] = minv[node<<1];
                idx[node]  = idx[node<<1];
            } else {
                minv[node] = minv[node<<1|1];
                idx[node]  = idx[node<<1|1];
            }
        }

        // Point-update: set pref[pos] = value
        void update(int node, int L, int R, int pos, int value) {
            if (L == R) {
                minv[node] = value;
                // idx[node] stays L
            } else {
                int mid = (L+R)>>1;
                if (pos <= mid) update(node<<1, L, mid, pos, value);
                else          update(node<<1|1, mid+1, R, pos, value);
                pull(node);
            }
        }

        // Range-min query on [i..j], return (minValue, position)
        Pair query(int node, int L, int R, int i, int j) {
            if (i > R || j < L) {
                return new Pair(INF, -1);
            }
            if (i <= L && R <= j) {
                return new Pair(minv[node], idx[node]);
            }
            int mid = (L+R)>>1;
            Pair left  = query(node<<1,    L, mid, i, j);
            Pair right = query(node<<1|1, mid+1, R, i, j);
            if (left.pref <= right.pref) return left;
            else                         return right;
        }
    }

    // Fast input reader
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
        FastReader() { br = new BufferedReader(new InputStreamReader(System.in)); }
        String next() throws IOException {
            while (st == null || !st.hasMoreTokens())
                st = new StringTokenizer(br.readLine());
            return st.nextToken();
        }
        int nextInt() throws IOException { return Integer.parseInt(next()); }
    }
}