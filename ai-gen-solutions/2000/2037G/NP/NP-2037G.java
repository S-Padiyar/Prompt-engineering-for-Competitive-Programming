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

        // 1) Precompute smallest-prime-factor and Mobius up to maxA
        int[] spf = new int[maxA + 1];
        int[] mu  = new int[maxA + 1];
        List<Integer> primes = new ArrayList<>();
        mu[1] = 1;
        for (int i = 2; i <= maxA; i++) {
            if (spf[i] == 0) {
                spf[i] = i;
                mu[i] = -1;
                primes.add(i);
            }
            for (int p : primes) {
                long prod = 1L * p * i;
                if (prod > maxA) break;
                spf[p * i] = p;
                if (i % p == 0) {
                    mu[p * i] = 0;  // square factor
                    break;
                } else {
                    mu[p * i] = -mu[i];
                }
            }
        }

        // 2) DP
        // S[d] = sum of dp[j] (mod) for all j<i with d | a[j]
        int[] S = new int[maxA + 1];
        int tot_dp = 0;   // sum of all previous dp[j]
        int dp_n = 0;     // we'll extract dp[n-1] at the end

        for (int i = 0; i < n; i++) {
            // 2a) factor a[i] into distinct primes
            int x = a[i];
            ArrayList<Integer> fac = new ArrayList<>();
            while (x > 1) {
                int p = spf[x];
                fac.add(p);
                while (x % p == 0) {
                    x /= p;
                }
            }
            int k = fac.size();

            int dpi;
            if (i == 0) {
                // city 1
                dpi = 1;
            } else {
                // 2b) Enumerate square-free divisors d of a[i], sum mu[d] * S[d]
                long coprimeSum = 0;
                int subsets = 1 << k;
                for (int mask = 0; mask < subsets; mask++) {
                    long d = 1;
                    for (int b = 0; b < k; b++) {
                        if ((mask & (1 << b)) != 0) {
                            d *= fac.get(b);
                        }
                    }
                    int md = mu[(int)d];
                    if (md == 0) continue;
                    coprimeSum += md * (long)S[(int)d];
                }
                // mod it
                coprimeSum %= MOD;
                if (coprimeSum < 0) coprimeSum += MOD;

                // dp[i] = tot_dp - coprimeSum (mod)
                dpi = tot_dp - (int)coprimeSum;
                dpi %= MOD;
                if (dpi < 0) dpi += MOD;
            }

            // 2c) update tot_dp and S[d]
            tot_dp += dpi;
            if (tot_dp >= MOD) tot_dp -= MOD;

            // add dpi to each square-free divisor d
            int subsets = 1 << k;
            for (int mask = 0; mask < subsets; mask++) {
                long d = 1;
                for (int b = 0; b < k; b++) {
                    if ((mask & (1 << b)) != 0) {
                        d *= fac.get(b);
                    }
                }
                S[(int)d] = (S[(int)d] + dpi) % MOD;
            }

            if (i == n - 1) {
                dp_n = dpi;
            }
        }

        // 3) output dp[n]
        System.out.println(dp_n);
    }
}