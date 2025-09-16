import java.io.*;
import java.util.*;

public class Main {
    static final int MAX = 200000;  // sum of n, m ≤ 2⋅10^5
    // segment tree arrays
    static int[] mn, cnt, lazy;

    // Friendship data
    static int[] a, b;
    static int[] cwL, cwR;            // clockwise interval [cwL..cwR]
    // counterclockwise arcs may wrap -> up to two pieces:
    static int[] ccw1L, ccw1R, ccw2L, ccw2R;
    static boolean[] hasCcw2;

    // Events for sweeping k = 1..n:
    // at k we have a list of (friendshipIndex, delta=±1) 
    static List<int[]>[] events;

    // Build or rebuild a segment tree on [1..n]
    static void buildTree(int idx, int left, int right) {
        mn[idx] = 0;
        lazy[idx] = 0;
        cnt[idx] = right - left + 1;  // initially all zero => all are "min"
        if (left == right) return;
        int mid = (left + right) >> 1;
        buildTree(idx << 1, left, mid);
        buildTree(idx << 1 | 1, mid + 1, right);
    }

    // push down lazy
    static void pushDown(int idx) {
        if (lazy[idx] != 0) {
            int v = lazy[idx];
            for (int c = idx<<1; c <= (idx<<1|1); c++) {
                mn[c] += v;
                lazy[c] += v;
            }
            lazy[idx] = 0;
        }
    }

    // pull up
    static void pullUp(int idx) {
        int L = idx<<1, R = L|1;
        if (mn[L] < mn[R]) {
            mn[idx] = mn[L];
            cnt[idx] = cnt[L];
        } else if (mn[R] < mn[L]) {
            mn[idx] = mn[R];
            cnt[idx] = cnt[R];
        } else {
            mn[idx] = mn[L];
            cnt[idx] = cnt[L] + cnt[R];
        }
    }

    // range add v to [ql..qr]
    static void update(int idx, int left, int right, int ql, int qr, int v) {
        if (ql > qr || ql > right || qr < left) return;
        if (ql <= left && right <= qr) {
            mn[idx] += v;
            lazy[idx] += v;
            return;
        }
        pushDown(idx);
        int mid = (left + right) >> 1;
        update(idx<<1, left, mid, ql, qr, v);
        update(idx<<1|1, mid+1, right, ql, qr, v);
        pullUp(idx);
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer tok = new StringTokenizer(in.readLine());
        int T = Integer.parseInt(tok.nextToken());

        // Pre–allocate big arrays
        mn   = new int[4*MAX+4];
        cnt  = new int[4*MAX+4];
        lazy = new int[4*MAX+4];

        a = new int[MAX+1];
        b = new int[MAX+1];
        cwL = new int[MAX+1];
        cwR = new int[MAX+1];
        ccw1L = new int[MAX+1];
        ccw1R = new int[MAX+1];
        ccw2L = new int[MAX+1];
        ccw2R = new int[MAX+1];
        hasCcw2 = new boolean[MAX+1];

        events = new ArrayList[MAX+2];
        for(int i=0;i<events.length;i++){
            events[i] = new ArrayList<>();
        }

        while (T-- > 0) {
            tok = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(tok.nextToken());
            int m = Integer.parseInt(tok.nextToken());

            // clear events[1..n]
            for (int i = 1; i <= n+1; i++) {
                events[i].clear();
            }

            // read friendships
            for (int i = 1; i <= m; i++) {
                tok = new StringTokenizer(in.readLine());
                a[i] = Integer.parseInt(tok.nextToken());
                b[i] = Integer.parseInt(tok.nextToken());
            }

            // prepare intervals and events
            for (int i = 1; i <= m; i++) {
                int A = a[i], B = b[i];
                // clockwise arc = [A..B-1]
                cwL[i] = A;
                cwR[i] = B - 1;

                // counterclockwise is the complement: edges [B..n] plus [1..A-1]
                if (B <= n) {
                    ccw1L[i] = B;
                    ccw1R[i] = n;
                } else {
                    ccw1L[i] = 1; ccw1R[i] = 0;  // empty
                }
                if (A > 1) {
                    ccw2L[i] = 1;
                    ccw2R[i] = A - 1;
                    hasCcw2[i] = true;
                } else {
                    hasCcw2[i] = false;
                }

                // sweeping events for "cut position" k:
                // when k = A we enter the cw-interval => must switch to ccw => delta=+1
                // when k = B we leave cw => switch back => delta=-1
                events[A].add(new int[]{ i, +1 });
                events[B].add(new int[]{ i, -1 });
            }

            // build segment tree on [1..n]
            buildTree(1,1,n);

            // initially add *all* cw-intervals
            for (int i = 1; i <= m; i++) {
                int L = cwL[i], R = cwR[i];
                // note R >= L always since a<b
                update(1,1,n, L, R, +1);
            }

            // sweep k=1..n
            int answer = n;  // upper bound
            for (int k = 1; k <= n; k++) {
                // process all events at k
                for (var ev : events[k]) {
                    int idx = ev[0], d = ev[1];
                    if (d == +1) {
                        // friendship idx becomes active => remove cw, add ccw
                        update(1,1,n, cwL[idx], cwR[idx], -1);
                        // add the ccw pieces
                        update(1,1,n, ccw1L[idx], ccw1R[idx], +1);
                        if (hasCcw2[idx]) {
                            update(1,1,n, ccw2L[idx], ccw2R[idx], +1);
                        }
                    } else {
                        // d == -1 => goes inactive => remove ccw, add cw back
                        update(1,1,n, ccw1L[idx], ccw1R[idx], -1);
                        if (hasCcw2[idx]) {
                            update(1,1,n, ccw2L[idx], ccw2R[idx], -1);
                        }
                        update(1,1,n, cwL[idx], cwR[idx], +1);
                    }
                }
                // now the segment tree's mn=0 count = number of edges *not* covered.
                // covered = n - (#not covered).
                int notCov = cnt[1];     // at root, mn[1] must be 0, and cnt[1] how many zeros
                int covered = n - notCov;
                answer = Math.min(answer, covered);
            }

            // print
            System.out.println(answer);
        }
    }
}