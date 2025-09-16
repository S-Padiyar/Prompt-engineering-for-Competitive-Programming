import java.io.*;
import java.util.*;

public class Main {
    static class SegTree {
        int n;
        long[] st;
        SegTree(int _n) {
            n = 1;
            while (n < _n) n <<= 1;
            st = new long[2*n];
        }
        // point update: set position p (1-based) to value v
        void update(int p, long v) {
            p += n - 1;
            st[p] = v;
            for (p >>= 1; p >= 1; p >>= 1) {
                st[p] = Math.max(st[2*p], st[2*p+1]);
            }
        }
        // range max on [L..R], 1-based
        long query(int L, int R) {
            if (L > R) return 0;
            L += n - 1;
            R += n - 1;
            long ans = 0;
            while (L <= R) {
                if ((L & 1) == 1) ans = Math.max(ans, st[L++]);
                if ((R & 1) == 0) ans = Math.max(ans, st[R--]);
                L >>= 1;
                R >>= 1;
            }
            return ans;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder out = new StringBuilder();

        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            int[] a = new int[n+1], b = new int[n+1];
            for (int i = 1; i <= n; i++) {
                StringTokenizer st = new StringTokenizer(br.readLine());
                a[i] = Integer.parseInt(st.nextToken());
                b[i] = Integer.parseInt(st.nextToken());
            }

            // lastPos[c] = position of the 'alive' run of color c
            // best[c]    = its death-time
            int[] lastPos = new int[n+1];
            long[] best   = new long[n+1];
            long[] dp     = new long[n+1];
            long[] death  = new long[n+1];

            SegTree seg = new SegTree(n);

            for (int i = 1; i <= n; i++) {
                int col = b[i];
                long length = a[i];

                if (lastPos[col] == 0) {
                    // first run of this color
                    death[i]    = length;
                    best[col]   = length;
                    lastPos[col]= i;
                } else {
                    int j = lastPos[col];
                    long L = best[col];
                    long midMax = seg.query(j+1, i-1);

                    // case (a): possible merge?
                    if (midMax < L && midMax < length) {
                        long merged = L + length - midMax;
                        death[i]    = merged;
                        best[col]   = merged;
                        lastPos[col]= i;
                    } else {
                        // no merge
                        if (L > length) {
                            // old run outlives new
                            death[i] = length;
                            // best[col], lastPos[col] unchanged
                        } else {
                            // new run outlives (or ties) old
                            death[i]    = length;
                            best[col]   = length;
                            lastPos[col]= i;
                        }
                    }
                }

                // record it in the segment tree
                seg.update(i, death[i]);
                // strength of prefix i
                dp[i] = Math.max(dp[i-1], death[i]);
            }

            // output
            for (int i = 1; i <= n; i++) {
                out.append(dp[i]).append(i==n?'\n':' ');
            }
        }

        System.out.print(out);
    }
}