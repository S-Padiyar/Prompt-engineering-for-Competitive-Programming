import java.io.*;
import java.util.*;

public class Main {
    static final long INF = (long)1e18;
    static int N;
    static long X;
    static long[] A;
    // segment‐tree arrays
    static long[] tMin, tMax, tAdd;
    static boolean[] tZero;
    
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        
        int t = Integer.parseInt(br.readLine());
        // We'll allocate once for the max possible N = 2e5
        int MAXN = 200_000;
        tMin  = new long[4 * MAXN + 10];
        tMax  = new long[4 * MAXN + 10];
        tAdd  = new long[4 * MAXN + 10];
        tZero = new boolean[4 * MAXN + 10];
        
        while (t-- > 0) {
            st = new StringTokenizer(br.readLine());
            N = Integer.parseInt(st.nextToken());
            X = Long.parseLong(st.nextToken());
            A = new long[N+1];
            st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= N; i++) {
                A[i] = Long.parseLong(st.nextToken());
            }
            
            // build segment‐tree over [1..N], dp initial = 0
            build(1, 1, N);
            
            long ans = 0;
            for (int r = 1; r <= N; r++) {
                long T = X - A[r];
                long bad;
                if (T < 0) {
                    // if A[r] > X, then dp_r[l] >= 0 always, so dp_r[l] > T for all l
                    bad = r;
                } else {
                    bad = query(1, 1, N, 1, r, T);
                }
                ans += (r - bad);
                
                // now update dp -> dp + A[r], and reset > X to 0, on [1..r]
                update(1, 1, N, 1, r, A[r]);
            }
            
            System.out.println(ans);
        }
    }
    
    // Build tree so that dp[..] = 0 initially
    static void build(int o, int L, int R) {
        tAdd[o] = 0;
        tZero[o] = false;
        tMin[o] = 0;
        tMax[o] = 0;
        if (L == R) {
            return;
        }
        int mid = (L + R) >>> 1;
        build(o<<1,     L, mid);
        build(o<<1 | 1, mid+1, R);
    }
    
    // Push down two kinds of lazy tags
    static void pushDown(int o, int L, int R) {
        if (tZero[o]) {
            // set children to zero
            int lc = o<<1, rc = o<<1|1;
            tZero[lc] = tZero[rc] = true;
            tAdd[lc] = tAdd[rc] = 0;
            tMin[lc] = tMax[lc] = 0;
            tMin[rc] = tMax[rc] = 0;
            tZero[o] = false;
        }
        if (tAdd[o] != 0) {
            long v = tAdd[o];
            int lc = o<<1, rc = o<<1|1;
            // add to children
            tAdd[lc] += v;
            tMin[lc] += v;
            tMax[lc] += v;
            tAdd[rc] += v;
            tMin[rc] += v;
            tMax[rc] += v;
            tAdd[o] = 0;
        }
    }
    
    // Pull up after children change
    static void pullUp(int o) {
        tMin[o] = Math.min(tMin[o<<1], tMin[o<<1|1]);
        tMax[o] = Math.max(tMax[o<<1], tMax[o<<1|1]);
    }
    
    // Query how many dp[l]>T in [ql..qr]
    static long query(int o, int L, int R, int ql, int qr, long T) {
        if (qr < L || R < ql) return 0;
        if (ql <= L && R <= qr) {
            if (tMax[o] <= T) return 0;
            if (tMin[o] >  T) return (R - L + 1);
            // else we must go deeper
        }
        pushDown(o, L, R);
        int mid = (L + R) >>> 1;
        long cnt = query(o<<1,   L, mid, ql, qr, T)
                 + query(o<<1|1, mid+1, R, ql, qr, T);
        return cnt;
    }
    
    // Update [ql..qr]: do “add = v; then any >X set to zero”
    static void update(int o, int L, int R, int ql, int qr, long v) {
        if (qr < L || R < ql) return;
        if (ql <= L && R <= qr) {
            // If even after adding v everything ≤X, just add
            if (tMax[o] + v <= X) {
                tAdd[o] += v;
                tMin[o] += v;
                tMax[o] += v;
                return;
            }
            // If after adding v everything >X, zero them all
            if (tMin[o] + v > X) {
                tZero[o] = true;
                tAdd[o] = 0;
                tMin[o] = tMax[o] = 0;
                return;
            }
            // Otherwise we need to recurse
        }
        pushDown(o, L, R);
        int mid = (L + R) >>> 1;
        update(o<<1,   L, mid, ql, qr, v);
        update(o<<1|1, mid+1, R, ql, qr, v);
        pullUp(o);
    }
}