import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 1_000_000_007;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(br.readLine().trim());
        int[] ns = new int[t];
        int maxn = 0;
        for (int i = 0; i < t; i++) {
            ns[i] = Integer.parseInt(br.readLine().trim());
            if (ns[i] > maxn) maxn = ns[i];
        }

        // Precompute factorials and inverse factorials up to maxn
        int N = maxn;
        int[] fac = new int[N+1];
        int[] ifac = new int[N+1];
        fac[0] = 1;
        for (int i = 1; i <= N; i++) {
            fac[i] = (int)((long)fac[i-1] * i % MOD);
        }
        ifac[N] = modPow(fac[N], MOD-2);
        for (int i = N; i > 0; i--) {
            ifac[i-1] = (int)((long)ifac[i] * i % MOD);
        }

        StringBuilder sb = new StringBuilder();
        // Process each test case
        for (int n : ns) {
            long ans = 0;

            // Case 1: m <= floor((n-1)/2), use the double sum
            int half = (n - 1) / 2;
            for (int m = 0; m <= half; m++) {
                int r = n - 2*m - 1;  // how many ones remain after the (m+1)-th
                long sumM = 0;
                // x runs from m+1 to 2m+1
                int L = m + 1, R = 2*m + 1;
                for (int x = L; x <= R; x++) {
                    // term = x * C(x-1, m) * C(n-x, r)
                    long t1 = fac[x-1];
                    t1 = t1 * ifac[m] % MOD;
                    t1 = t1 * ifac[(x-1)-m] % MOD;
                    long t2 = fac[n-x];
                    t2 = t2 * ifac[r] % MOD;
                    t2 = t2 * ifac[(n-x)-r] % MOD;
                    long term = ( (long)x * t1 % MOD ) * t2 % MOD;
                    sumM = (sumM + term) % MOD;
                }
                ans = (ans + sumM) % MOD;
            }

            // Case 2: m >= ceil(n/2), MEX = 2m+1 always
            int start = (n + 1) / 2; // ceil(n/2)
            for (int m = start; m <= n; m++) {
                // (2m+1)*C(n,m)
                long comb = (long)fac[n] * ifac[m] % MOD * ifac[n-m] % MOD;
                long term = comb * (2L*m + 1) % MOD;
                ans = (ans + term) % MOD;
            }

            sb.append(ans).append('\n');
        }

        System.out.print(sb.toString());
    }

    // Fast exponentiation mod
    static int modPow(long a, int p) {
        long res = 1, x = a % MOD;
        while (p > 0) {
            if ((p & 1) != 0) res = (res * x) % MOD;
            x = (x * x) % MOD;
            p >>= 1;
        }
        return (int)res;
    }
}