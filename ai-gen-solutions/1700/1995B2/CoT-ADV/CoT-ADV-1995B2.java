import java.io.*;
import java.util.*;

public class Main {
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
        FastReader(InputStream in) {
            br = new BufferedReader(new InputStreamReader(in));
        }
        String next() throws IOException {
            while (st==null || !st.hasMoreTokens()) {
                String line = br.readLine();
                if (line==null) return null;
                st = new StringTokenizer(line);
            }
            return st.nextToken();
        }
        int nextInt()    throws IOException { return Integer.parseInt(next()); }
        long nextLong()  throws IOException { return Long.parseLong(next()); }
    }

    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader(System.in);
        int t = in.nextInt();
        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            int n = in.nextInt();
            long m = in.nextLong();

            // Read petals and counts
            long[] a = new long[n], c = new long[n];
            for (int i = 0; i < n; i++) a[i] = in.nextLong();
            for (int i = 0; i < n; i++) c[i] = in.nextLong();

            // Sort by petal count
            Integer[] idx = new Integer[n];
            for (int i = 0; i < n; i++) idx[i] = i;
            Arrays.sort(idx, Comparator.comparingLong(i -> a[i]));

            long best = 0;

            // 1) Best using only a single type
            for (int ii = 0; ii < n; ii++) {
                int i = idx[ii];
                long maxCount = m / a[i];
                if (maxCount > c[i]) maxCount = c[i];
                long petals = maxCount * a[i];
                if (petals > best) best = petals;
            }

            // 2) Best using exactly two consecutive petal‐counts
            for (int ii = 0; ii + 1 < n; ii++) {
                int i = idx[ii];
                int j = idx[ii+1];
                if (a[j] == a[i] + 1) {
                    long candidate = solvePair(a[i], c[i], c[j], m);
                    if (candidate > best) best = candidate;
                }
            }

            sb.append(best).append('\n');
        }

        System.out.print(sb);
    }

    /**
     * Compute the maximum total petals one can get from two types:
     *   - smaller-petal type: petal=a, quantity=n1
     *   - larger-petal type:  petal=a+1, quantity=n2
     * with budget M, under the rule 0 <= x <= n1, 0 <= y <= n2,
     *   maximize x*a + y*(a+1) <= M.
     */
    static long solvePair(long a, long n1, long n2, long M) {
        long b = a + 1;
        long best = 0;

        // Case I: take all n1 of the 'a' flowers if budget allows, then fill with as many 'b' as possible
        if (M >= n1 * a) {
            long rem = M - n1 * a;
            long y1  = rem / b;
            if (y1 > n2) y1 = n2;
            long s1 = n1 * a + y1 * b;
            if (s1 > best) best = s1;
        }

        // Case II: Do not necessarily exhaust n1; we search y in [L..R]
        // where L = first y that forces us NOT to use all n1, R = max possible y
        long L;
        if (M < n1 * a) {
            L = 0;  // we can never use all n1
        } else {
            // floor((M - n1*a)/b) + 1
            long tmp = (M - n1*a) / b;
            L = tmp + 1;
        }
        if (L < 0) L = 0;
        long R = M / b;
        if (R > n2) R = n2;

        if (L <= R) {
            long rem0 = M % a;   // target for (M - y*b) mod a
            // check if there's a y ≡ rem0 (mod a) in [L..R]
            long delta = L - rem0;
            long k = (delta <= 0 ? 0 : (delta + a - 1) / a);
            long y0 = rem0 + k * a;
            if (y0 <= R) {
                // perfect: we spend entire budget M
                if (M > best) best = M;
            } else {
                // no perfect solution; find y that makes the modulo as small as possible
                // First try largest y whose remainder ≤ rem0
                long Rrem = R % a;
                long y2;
                if (Rrem <= rem0) {
                    y2 = R;
                } else {
                    y2 = R - (Rrem - rem0);
                }
                if (y2 >= L) {
                    long r = (rem0 - (y2 % a));
                    long val = M - r;
                    if (val > best) best = val;
                } else {
                    // all remainders in [L..R] exceed rem0; pick the smallest remainder > rem0
                    long Lrem = L % a;
                    long r2 = a - (Lrem - rem0);
                    long val = M - r2;
                    if (val > best) best = val;
                }
            }
        }

        return best;
    }
}