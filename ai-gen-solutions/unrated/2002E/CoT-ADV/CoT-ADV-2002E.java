import java.io.*;
import java.util.*;

public class Main {
    static final int MAXB = 300_000; // maximum possible b_i
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer tok;

        int t = Integer.parseInt(in.readLine().trim());
        // last[v] = last index where value v appeared; 0 if never
        int[] last = new int[MAXB+1];
        StringBuilder output = new StringBuilder();

        while (t-- > 0) {
            int n = Integer.parseInt(in.readLine().trim());
            int[] a = new int[n+1], b = new int[n+1];
            for (int i = 1; i <= n; i++) {
                tok = new StringTokenizer(in.readLine());
                a[i] = Integer.parseInt(tok.nextToken());
                b[i] = Integer.parseInt(tok.nextToken());
            }

            // Segment tree for range max over dp[1..n]
            SegTree st = new SegTree(n);

            long best = 0;  // current maximum dp over [1..i]
            for (int i = 1; i <= n; i++) {
                long dpi = a[i];           // base dp[i] = a[i]
                int v = b[i];
                int j = last[v];           // previous occurrence
                if (j != 0) {
                    // G = max dp in (j, i), i.e. j+1..i-1
                    long G = 0;
                    if (j+1 <= i-1) {
                        G = st.rangeMax(j+1, i-1);
                    }
                    // add merging bonus
                    long dpj = st.rangeMax(j, j); // or store dp[j] separately
                    dpi += Math.max(0L, dpj - G);
                }
                // store dp[i]
                st.update(i, dpi);
                // update global best
                if (dpi > best) best = dpi;
                output.append(best).append(' ');
                last[v] = i;
            }
            output.append('\n');

            // Clear last[] for this test
            for (int i = 1; i <= n; i++) {
                last[b[i]] = 0;
            }
        }

        System.out.print(output);
    }

    // A simple segment tree for range-maximum, 1-based indices
    static class SegTree {
        int size;
        long[] tree;

        SegTree(int n) {
            // find power-of-two >= n
            size = 1;
            while (size < n) size <<= 1;
            tree = new long[2 * size];
        }
        // point update: set position pos (1..n) to value val
        void update(int pos, long val) {
            pos += size - 1;
            tree[pos] = val;
            while (pos > 1) {
                pos >>= 1;
                tree[pos] = Math.max(tree[2*pos], tree[2*pos+1]);
            }
        }
        // range maximum in [l..r], 1-based inclusive
        long rangeMax(int l, int r) {
            long res = 0;
            l += size - 1;
            r += size - 1;
            while (l <= r) {
                if ((l & 1) == 1) res = Math.max(res, tree[l++]);
                if ((r & 1) == 0) res = Math.max(res, tree[r--]);
                l >>= 1; r >>= 1;
            }
            return res;
        }
    }
}