import java.io.*;
import java.util.*;

public class Main {
    static final long INF = Long.MAX_VALUE / 4;

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer tok;

        int T = Integer.parseInt(in.readLine());
        StringBuilder out = new StringBuilder();

        while (T-- > 0) {
            tok = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(tok.nextToken());
            long k = Long.parseLong(tok.nextToken());
            int F = n / 2;           // we need F = floor(n/2) raises to get median

            // read a[], b[]
            long[] a = new long[n];
            int[] b = new int[n];
            tok = new StringTokenizer(in.readLine());
            for (int i = 0; i < n; i++) a[i] = Long.parseLong(tok.nextToken());
            tok = new StringTokenizer(in.readLine());
            for (int i = 0; i < n; i++) b[i] = Integer.parseInt(tok.nextToken());

            // Make a sorted list of (value,index) so we can remove each index
            // and know the "initial" median of the other n-1 elements.
            Pair[] P = new Pair[n];
            for (int i = 0; i < n; i++) {
                P[i] = new Pair(a[i], i);
            }
            Arrays.sort(P);

            // t = position of median in a sorted of size n
            // for c_i of size n-1, median is at pos_c = floor((n-1 + 2)/2).
            // That equals floor((n+1)/2).  We'll call t = floor((n+1)/2).
            int t = (n + 1) / 2;

            // precompute initial median[without i] = m0[i]
            long[] m0 = new long[n];
            // If the removed index is among the first t in sorted order,
            // the c_i median is P[t].val, else it's P[t-1].val.
            // careful with 0-based:
            for (int rank = 0; rank < n; rank++) {
                int idx = P[rank].idx;
                if (rank < t) {
                    // we removed one of the earliest t,
                    // the median of the rest is the (t)-th smallest in 0-based => P[t].val
                    m0[idx] = (t < n ? P[t].val : P[n - 1].val);
                } else {
                    // removed from >= t, median is P[t-1].val
                    m0[idx] = P[t - 1].val;
                }
            }

            // We'll binary search the final answer S.
            // Lower bound L=0, upper bound R = (max(a)+k) + (max(a)+k)
            long maxA = 0;
            for (long v : a) if (v > maxA) maxA = v;
            long L = 0, R = (maxA + k) * 2 + 5;

            while (L < R) {
                long mid = (L + R + 1) >>> 1;
                if (canAchieve(mid, n, k, F, a, b, m0)) {
                    L = mid;
                } else {
                    R = mid - 1;
                }
            }

            out.append(L).append('\n');
        }
        System.out.print(out);
    }

    static boolean canAchieve(long S, int n, long k, int F,
                              long[] a, int[] b, long[] m0) {
        // Scenario B: spend all k on the champion, 0 on median.
        // Then champion i must have b[i]=1 and
        //   a[i] + k + m0[i] >= S.
        // If any i does, we succeed immediately.
        for (int i = 0; i < n; i++) {
            if (b[i] == 1) {
                if (a[i] + k + m0[i] >= S) {
                    return true;
                }
            }
        }

        // Scenario A: spend 0 on champion, up to k on median.
        // We need for some i:
        //    a[i] + newMedian >= S
        //  => newMedian >= Y = S - a[i]
        // We'll build a cost-array of length n-1 for each i,
        // pick the F smallest costs, sum them, and see if <= k.
        //
        // We can optimize a bit by noticing that Y depends only on i,
        // but we must rebuild the costs without j=i.
        // Because n <= 2e5 in total, we can afford O(n log n) per check.

        // We prepare a sorted multiset (or an array to sort) of (cost, index)
        // for all j where j != i.
        long[] costs = new long[n];

        for (int i = 0; i < n; i++) {
            long Y = S - a[i];
            // Build costs[] over j!=i:
            int p = 0;
            for (int j = 0; j < n; j++) {
                if (j == i) continue;
                long c;
                if (a[j] >= Y) {
                    c = 0;
                } else if (b[j] == 1) {
                    c = Y - a[j];  // can raise
                } else {
                    c = INF;       // cannot raise this one => infinite cost
                }
                costs[p++] = c;
            }
            // if p != n-1 in theory, but we guaranteed just skipping i
            Arrays.sort(costs, 0, n-1);
            // sum the first F:
            long sum = 0;
            for (int x = 0; x < F; x++) {
                sum += costs[x];
                if (sum > k) break;
            }
            if (sum <= k) {
                // We can raise median ≥ Y with ≤ k ops.
                return true;
            }
        }

        // Neither scenario A nor B worked for any champion i.
        return false;
    }

    // small helper to sort (value,index)
    static class Pair implements Comparable<Pair> {
        long val;
        int idx;
        Pair(long v, int i) { val = v; idx = i; }
        public int compareTo(Pair o) {
            return Long.compare(this.val, o.val);
        }
    }
}