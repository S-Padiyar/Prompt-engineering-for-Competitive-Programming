import java.io.*;
import java.util.*;

public class Main {
    // Maximum value in the universe
    static final int N = 2_000_000;
    // Enough nodes to handle up to ~4e5 updates * log2(N) ~ 8.4e6
    static final int MAXNODES = 8_500_000;

    // Arrays for the implicit segment tree
    static int[] lc = new int[MAXNODES];
    static int[] rc = new int[MAXNODES];
    static int[] pref = new int[MAXNODES];
    static int[] suf  = new int[MAXNODES];
    static int[] best = new int[MAXNODES];
    static int poolPtr = 0; // next free node index

    // Allocate a new node, initialize to "all-1" on a segment of given length
    static int newNode(int len) {
        int id = ++poolPtr;
        lc[id] = rc[id] = 0;
        pref[id] = suf[id] = best[id] = len;
        return id;
    }

    /**
     * Point-update: set M[pos] = val (0 or 1).
     * node==0 means "this segment is all-1" and a node must be allocated.
     * Returns the root index of the (sub)tree after update.
     */
    static int update(int node, int L, int R, int pos, int val) {
        if (node == 0) {
            node = newNode(R - L + 1);
        }
        if (L == R) {
            // leaf
            if (val == 1) {
                pref[node] = suf[node] = best[node] = 1;
            } else {
                pref[node] = suf[node] = best[node] = 0;
            }
            return node;
        }
        int mid = (L + R) >>> 1;
        if (pos <= mid) {
            lc[node] = update(lc[node], L, mid, pos, val);
        } else {
            rc[node] = update(rc[node], mid + 1, R, pos, val);
        }
        // Pull up
        int leftId  = lc[node],  rightId = rc[node];
        int lLen = mid - L + 1,   rLen = R - mid;
        // left child's data or default if missing
        int lp = (leftId == 0 ? lLen : pref[leftId]);
        int ls = (leftId == 0 ? lLen : suf[leftId]);
        int lb = (leftId == 0 ? lLen : best[leftId]);
        // right child's data or default if missing
        int rp = (rightId == 0 ? rLen : pref[rightId]);
        int rs = (rightId == 0 ? rLen : suf[rightId]);
        int rb = (rightId == 0 ? rLen : best[rightId]);

        pref[node] = (lp == lLen ? lLen + rp : lp);
        suf[node]  = (rs == rLen ? rLen + ls : rs);
        best[node] = Math.max(Math.max(lb, rb), ls + rp);

        return node;
    }

    /**
     * Find the leftmost position d in [L..R] where there is a
     * block of k consecutive 1's (missing) in M.  We assume best[node]>=k.
     * If node==0, that entire interval is all-1, so the answer is L.
     */
    static int query(int node, int L, int R, int k) {
        if (node == 0) {
            // entire [L..R] is all-1
            return L;
        }
        if (L == R) {
            // leaf and best[node]>=k implies k==1
            return L;
        }
        int mid = (L + R) >>> 1;
        int leftId  = lc[node];
        int rightId = rc[node];
        int lLen = mid - L + 1, rLen = R - mid;

        int lb = (leftId  == 0 ? lLen : best[leftId]);
        if (lb >= k) {
            // we can fit the block entirely in the left child
            return query(leftId, L, mid, k);
        }
        int ls = (leftId  == 0 ? lLen : suf[leftId]);
        int rp = (rightId == 0 ? rLen : pref[rightId]);
        if (ls + rp >= k) {
            // a block that crosses mid
            return mid - ls + 1;
        }
        // otherwise it must fit in the right child
        return query(rightId, mid + 1, R, k);
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));
        int t = Integer.parseInt(in.readLine().trim());
        
        while (t-- > 0) {
            // reset the root for this test
            int root = 0;
            // read initial set
            int n = Integer.parseInt(in.readLine().trim());
            StringTokenizer st = new StringTokenizer(in.readLine());
            for (int i = 0; i < n; i++) {
                int x = Integer.parseInt(st.nextToken());
                // mark M[x]=0 (present)
                root = update(root, 1, N, x, 0);
            }

            int m = Integer.parseInt(in.readLine().trim());
            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(in.readLine());
                char op = st.nextToken().charAt(0);
                int v = Integer.parseInt(st.nextToken());
                if (op == '+') {
                    // insert v => M[v]=0
                    root = update(root, 1, N, v, 0);
                } else if (op == '-') {
                    // remove v => M[v]=1
                    root = update(root, 1, N, v, 1);
                } else {
                    // query k-load
                    int k = v;
                    // if the root's best < k, answer is N+1
                    int b = (root == 0 ? N : best[root]);
                    if (b < k) {
                        out.println(N + 1);
                    } else {
                        int ans = query(root, 1, N, k);
                        out.println(ans);
                    }
                }
            }
        }

        out.flush();
        out.close();
    }
}