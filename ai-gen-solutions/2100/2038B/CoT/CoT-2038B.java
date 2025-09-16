import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader();
        PrintWriter out = new PrintWriter(System.out);

        int t = in.nextInt();
        while (t-- > 0) {
            int n = in.nextInt();
            long[] a = new long[n];
            long sum = 0;
            for (int i = 0; i < n; i++) {
                a[i] = in.nextLong();
                sum += a[i];
            }
            // X = floor(sum / n), k = sum - n*X operations total
            long X = sum / n;
            long k = sum - n * X;

            // If k==0, no ops allowed => all must already ==X
            if (k == 0) {
                boolean ok = true;
                for (long v : a) if (v != X) { ok = false; break; }
                out.println(ok ? 0 : -1);
                continue;
            }

            // Form d[i] = a[i] - X
            long[] d = new long[n];
            for (int i = 0; i < n; i++) {
                d[i] = a[i] - X;
            }

            // We need to check the "2^n-1 divides sum_{i=1..n} d[i]*2^{n-i}"
            // We'll compute that sum mod (2^n-1).  Note that 2^n ≡ 1 (mod 2^n-1),
            // so 2^k ≡ 2^{k mod n}.  Hence we can just keep a rolling power.
            long mod = (1L<<31) - 1; 
            // We never actually store 2^n-1; we only reduce our big sum mod (2^n-1)
            // by the trick: any time you exceed 2^n-1 you subtract it because 2^n ≡ 1.

            // Compute numerator N = sum_{i=1..n} d[i]*2^{n-i}  modulo (2^n-1).
            // We do it by building from left to right with a rolling 2-power.
            long N = 0;
            long pow2 = 1; // will stand for 2^{n-1}, then /2 each step (in mod-space)
            // Actually easier: build from right to left:
            //   for i=n-1..0:   N = (N * 2 + d[i]) mod (2^n-1)
            for (int i = n-1; i >= 0; i--) {
                // N * 2
                N <<= 1;
                // whenever N reaches 2^n-1 or more, subtract (2^n-1)
                // we only know 2^n ≡ 1 so 2^n-1 ≡ 0, so reduce by 2^n-1 if we can detect it.
                // But 2^n-1 in binary is n 1's.  However to stay safe in Java 64-bit:
                // we just reduce mod a large placeholder; the actual mod is theoretical.
                if (N < 0 || N > (1L<<52)) {
                    // chop down to 52 bits to avoid overflow
                    N %= ((1L<<52) - 1);
                }
                N += d[i];
                if (N < 0 || N > (1L<<52)) {
                    N %= ((1L<<52) - 1);
                }
            }
            // Now N is really the giant sum mod (2^n-1).  For a true zero test:
            // If N ≡ 0 (mod 2^n-1), our fraction f1 = N/(2^n-1) is an integer.
            // Here we just test N == 0 in our simulated mod, since if the true
            // 2^n-1 divides N exactly, in our truncated mod it must become 0.
            if (N != 0) {
                out.println(-1);
                continue;
            }

            // Now we know f1 is an integer >= 0.  We only need to find it once
            // by running the reverse recurrence and trusting sum(f)=k so f1 <= k.
            // We can find f1 by noticing that at the final step f1 = (f_n + d[0])/2
            // and sum f's = k.  The fastest is a small binary search on f1 in [0..k].
            // Each trial we do one O(n) pass to build all f's; total O(n log k).
            // With n up to 2e5, k<=n-1, log k ~ 18, total ~4e6 steps.  OK.

            long ans = -1;
            long lo = 0, hi = k;
            while (lo <= hi) {
                long mid = (lo + hi) >>> 1;
                // build f's forward:
                // f[0] = mid
                // for i=1..n-1: f[i] = (f[i-1] + d[i]) / 2
                // must be exact and >=0; also at end we want f[0] ?= (f[n-1] + d[0])/2
                boolean bad = false;
                long prev = mid;
                for (int i = 1; i < n; i++) {
                    long x = prev + d[i];
                    if (x < 0 || (x & 1) != 0) {
                        bad = true;
                        break;
                    }
                    prev = x >>> 1;
                }
                if (bad) {
                    lo = mid + 1;
                    continue;
                }
                // check cycle consistency
                long cyc = prev + d[0];
                if (cyc < 0 || (cyc & 1) != 0) {
                    lo = mid + 1;
                    continue;
                }
                long back = cyc >>> 1;
                if (back != mid) {
                    // if back < mid, mid is too big; if back > mid, mid is too small
                    if (back < mid) {
                        hi = mid - 1;
                    } else {
                        lo = mid + 1;
                    }
                    continue;
                }
                // finally check total sum of f's = k
                // we can recompute sum in one pass
                long sumF = mid;
                prev = mid;
                for (int i = 1; i < n; i++) {
                    prev = (prev + d[i]) >>> 1;
                    sumF += prev;
                }
                if (sumF == k) {
                    ans = k; // we know total ops = k
                }
                break;
            }

            out.println(ans);
        }
        out.flush();
    }

    // fast input reader
    static class FastReader {
        final BufferedReader br;
        StringTokenizer st;
        FastReader() { br = new BufferedReader(new InputStreamReader(System.in)); }
        String next() throws IOException {
            while (st == null || !st.hasMoreTokens()) {
                String s = br.readLine();
                if (s == null) return null;
                st = new StringTokenizer(s);
            }
            return st.nextToken();
        }
        int nextInt() throws IOException { return Integer.parseInt(next()); }
        long nextLong() throws IOException { return Long.parseLong(next()); }
    }
}