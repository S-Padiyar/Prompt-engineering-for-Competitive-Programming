import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int T = Integer.parseInt(br.readLine().trim());
        while (T-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            long[] a = new long[n];
            int[] b = new int[n];
            for (int i = 0; i < n; i++) {
                StringTokenizer st = new StringTokenizer(br.readLine());
                a[i] = Long.parseLong(st.nextToken());
                b[i] = Integer.parseInt(st.nextToken());
            }

            // Build a segment tree for range‐maximum queries over a[]
            SegmentTree seg = new SegmentTree(a);

            long[] dp = new long[n];
            long[] ans = new long[n];

            // last[v] = last index where value v appeared, or -1 if none
            // b[i] <= n by problem statement, so we size n+1.
            int[] last = new int[n+1];
            Arrays.fill(last, -1);

            long currentMax = 0;
            for (int i = 0; i < n; i++) {
                // start dp[i] = a[i]
                long best = a[i];
                int v = b[i];
                int j = last[v];
                if (j != -1) {
                    // query the maximum a[k] for k in [j+1..i-1]
                    long gapMax = 0L;
                    if (j + 1 <= i - 1) {
                        gapMax = seg.rangeMax(j + 1, i - 1);
                    }
                    // can the two runs merge?  They both must survive until the gap is gone
                    if (a[j] > gapMax && a[i] > gapMax) {
                        long candidate = dp[j] + (a[i] - gapMax);
                        if (candidate > best) {
                            best = candidate;
                        }
                    }
                }
                dp[i] = best;
                // answer so far
                currentMax = Math.max(currentMax, dp[i]);
                ans[i] = currentMax;
                // remember this run of value v
                last[v] = i;
            }

            // output answers for prefixes 1..n
            for (int i = 0; i < n; i++) {
                out.print(ans[i]);
                out.print(i+1 < n ? ' ' : '\n');
            }
        }

        out.flush();
    }

    // A simple iterative segment tree for range‐maximum on a static array.
    static class SegmentTree {
        int n;
        long[] tree;

        // Build from array arr[0..n-1]
        SegmentTree(long[] arr) {
            int sz = arr.length;
            n = 1;
            while (n < sz) n <<= 1;
            tree = new long[2 * n];
            // initialize leaves
            for (int i = 0; i < sz; i++) {
                tree[n + i] = arr[i];
            }
            for (int i = sz; i < n; i++) {
                tree[n + i] = 0L;  // neutral for max
            }
            // build internal nodes
            for (int i = n - 1; i > 0; i--) {
                tree[i] = Math.max(tree[2 * i], tree[2 * i + 1]);
            }
        }

        // range‐max on [l..r], 0‐based inclusive
        long rangeMax(int l, int r) {
            long res = 0L; // neutral for max (all a[i] ≥ 1)
            l += n;
            r += n;
            while (l <= r) {
                if ((l & 1) == 1) {
                    res = Math.max(res, tree[l++]);
                }
                if ((r & 1) == 0) {
                    res = Math.max(res, tree[r--]);
                }
                l >>= 1;
                r >>= 1;
            }
            return res;
        }
    }
}