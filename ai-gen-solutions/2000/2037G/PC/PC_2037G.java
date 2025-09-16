import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 998244353;

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        // Read n
        int n = Integer.parseInt(in.readLine());
        // Read a[1..n]
        int[] a = new int[n+1];
        int maxA = 0;
        st = new StringTokenizer(in.readLine());
        for (int i = 1; i <= n; i++) {
            a[i] = Integer.parseInt(st.nextToken());
            if (a[i] > maxA) maxA = a[i];
        }

        // Sieve for least prime factor up to maxA
        int[] lpf = new int[maxA+1];
        for (int i = 2; i <= maxA; i++) {
            if (lpf[i] == 0) {
                for (int j = i; j <= maxA; j += i) {
                    if (lpf[j] == 0) lpf[j] = i;
                }
            }
        }

        // dp[i] = number of paths from 1 to i
        int[] dp = new int[n+1];
        // F[d] = sum of dp[i] for all i<current that are divisible by d
        int[] F = new int[maxA+1];

        // Temporary arrays for building the inclusion-exclusion subsets
        // max number of distinct primes of any a[i] <= 7,
        // so 1<<7 = 128 is enough.
        int[] ds = new int[128];
        int[] popc = new int[128];

        // Reusable array to hold the distinct prime divisors
        int[] primes = new int[8];

        // Main DP loop
        for (int i = 1; i <= n; i++) {
            // Factor a[i] into distinct primes
            int x = a[i];
            int k = 0;
            while (x > 1) {
                int p = lpf[x];
                primes[k++] = p;
                while (x % p == 0) {
                    x /= p;
                }
            }

            // Build all subset‐products ds[mask] and popcount
            int limit = 1 << k;
            ds[0] = 1;
            popc[0] = 0;
            for (int mask = 1; mask < limit; mask++) {
                int low = mask & -mask;               // least significant bit
                int j = Integer.numberOfTrailingZeros(low);
                int prev = mask ^ low;
                ds[mask] = ds[prev] * primes[j];
                popc[mask] = popc[prev] + 1;
            }

            // Compute dp[i] by inclusion‐exclusion, except for i=1
            int dpi = 0;
            if (i == 1) {
                dpi = 1;
            } else {
                for (int mask = 1; mask < limit; mask++) {
                    int d = ds[mask];
                    if ((popc[mask] & 1) == 1) {
                        // odd number of primes => add F[d]
                        dpi += F[d];
                        if (dpi >= MOD) dpi -= MOD;
                    } else {
                        // even => subtract F[d]
                        dpi -= F[d];
                        if (dpi < 0) dpi += MOD;
                    }
                }
            }
            dp[i] = dpi;

            // Update F[d] += dp[i] for all nonempty subsets d
            for (int mask = 1; mask < limit; mask++) {
                int d = ds[mask];
                F[d] += dpi;
                if (F[d] >= MOD) F[d] -= MOD;
            }
        }

        // Answer = dp[n]
        System.out.println(dp[n]);
    }
}