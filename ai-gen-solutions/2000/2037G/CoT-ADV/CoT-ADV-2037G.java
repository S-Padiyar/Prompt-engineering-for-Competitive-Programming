import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 998244353;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int n = Integer.parseInt(br.readLine().trim());
        StringTokenizer st = new StringTokenizer(br.readLine());
        int[] a = new int[n];
        int maxA = 0;
        for (int i = 0; i < n; i++) {
            a[i] = Integer.parseInt(st.nextToken());
            if (a[i] > maxA) maxA = a[i];
        }

        // 1) Build smallest-prime-factor sieve up to maxA
        int[] spf = new int[maxA + 1];
        for (int i = 2; i <= maxA; i++) {
            if (spf[i] == 0) {
                for (int j = i; j <= maxA; j += i) {
                    if (spf[j] == 0) spf[j] = i;
                }
            }
        }

        // 2) G[d] will store sum of dp[j] so far for all j whose a[j] % d == 0
        int[] G = new int[maxA + 1];

        // dp_i will be computed on the fly; only dp[n-1] is needed at the end.
        int dp_i = 0;
        // Temporary arrays for divisor enumeration
        int[] primes = new int[7];
        int[] d    = new int[1 << 7];
        int[] sz   = new int[1 << 7];

        for (int i = 0; i < n; i++) {
            // 3) Factor a[i] into distinct primes
            int x = a[i], k = 0;
            while (x > 1) {
                int p = spf[x];
                primes[k++] = p;
                while (x % p == 0) x /= p;
            }

            // 4) Enumerate all nonempty subsets of these k primes
            int subsets = 1 << k;
            d[0] = 1; 
            sz[0] = 0;
            for (int mask = 1; mask < subsets; mask++) {
                int lsb = mask & -mask;
                int prev = mask ^ lsb;
                int bitIdx = Integer.numberOfTrailingZeros(lsb);
                d[mask] = d[prev] * primes[bitIdx];
                sz[mask] = sz[prev] + 1;
            }

            // 5) Compute dp[i] by inclusion–exclusion:
            //    sum_{nonempty subset S} (-1)^{|S|+1} * G[product(S)]
            dp_i = 0;
            if (i == 0) {
                // by definition there's exactly one path starting at city 1
                dp_i = 1;
            } else {
                for (int mask = 1; mask < subsets; mask++) {
                    int div = d[mask];
                    int sign = (sz[mask] % 2 == 1) ? 1 : -1;
                    int val = G[div];
                    dp_i = (dp_i + sign * val) % MOD;
                }
                if (dp_i < 0) dp_i += MOD;
            }

            // 6) Update G[div] += dp_i for all those square-free divisors div
            for (int mask = 1; mask < subsets; mask++) {
                int div = d[mask];
                int nv = G[div] + dp_i;
                if (nv >= MOD) nv -= MOD;
                G[div] = nv;
            }
        }

        // dp[n-1] is our answer
        System.out.println(dp_i);
    }
}