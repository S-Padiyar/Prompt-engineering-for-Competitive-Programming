import java.io.*;
import java.util.*;

public class Main {
    static final long INF = (long)4e18;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());
            long v = Long.parseLong(st.nextToken());

            long[] a = new long[n+1];
            st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }

            // 1) Compute pref[i] = max # of disjoint segments with sum >= v in a[1..i]
            int[] pref = new int[n+1];
            {
                long sum = 0;
                int cnt = 0;
                for (int i = 1; i <= n; i++) {
                    sum += a[i];
                    if (sum >= v) {
                        cnt++;
                        sum = 0;
                    }
                    // clamp to m (we never need more than m)
                    pref[i] = cnt > m ? m : cnt;
                }
                pref[0] = 0;
            }

            // 2) Compute suff[i] = max # of disjoint segments with sum >= v in a[i..n]
            int[] suff = new int[n+2];
            {
                long sum = 0;
                int cnt = 0;
                for (int i = n; i >= 1; i--) {
                    sum += a[i];
                    if (sum >= v) {
                        cnt++;
                        sum = 0;
                    }
                    suff[i] = cnt > m ? m : cnt;
                }
                suff[n+1] = 0;
            }

            // 3) Prefix sums P
            long[] P = new long[n+1];
            for (int i = 1; i <= n; i++) {
                P[i] = P[i-1] + a[i];
            }

            // 4) Segment tree (or BIT) to maintain min P[L] by pref[L] index
            //    We have indices 0..m
            SegmentTree seg = new SegmentTree(m+1);
            // initialize all to INF
            seg.init(INF);
            // insert L=0: pref[0]=0, P[0]=0
            seg.update(0, 0L);

            long answer = -1;  // if stays -1, we print -1

            // Sweep R = 1..n+1
            for (int R = 1; R <= n+1; R++) {
                int need = m - suff[R];
                if (need <= m && need >= 0) {
                    long bestL = seg.query(need, m);
                    if (bestL < INF) {
                        long candidate = P[R-1] - bestL;
                        if (candidate > answer) {
                            answer = candidate;
                        }
                    }
                }
                // now insert L = R (for the next rounds), as long as R <= n
                if (R <= n) {
                    int p = pref[R];
                    long val = P[R];
                    seg.update(p, val);
                }
            }

            sb.append(answer).append('\n');
        }
        System.out.print(sb);
    }

    // A simple iterative segment‐tree for range minimum,
    // size = next power of two ≥ n, tree in array [1..2*size)
    static class SegmentTree {
        int n, size;
        long[] st;

        SegmentTree(int n) {
            this.n = n;
            // find power-of-two
            size = 1;
            while (size < n) size <<= 1;
            st = new long[2*size];
        }

        void init(long v) {
            Arrays.fill(st, v);
        }

        // point update: st[pos] = min(st[pos], val)
        void update(int pos, long val) {
            pos += size;
            if (val < st[pos]) {
                st[pos] = val;
                for (pos >>= 1; pos > 0; pos >>= 1) {
                    st[pos] = Math.min(st[2*pos], st[2*pos+1]);
                }
            }
        }

        // range query [L..R], inclusive, 0-based
        long query(int L, int R) {
            long res = INF;
            L += size;  R += size;
            while (L <= R) {
                if ((L & 1) == 1) res = Math.min(res, st[L++]);
                if ((R & 1) == 0) res = Math.min(res, st[R--]);
                L >>= 1; R >>= 1;
            }
            return res;
        }
    }
}