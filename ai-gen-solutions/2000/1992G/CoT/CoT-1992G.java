import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 1_000_000_007;
    static int[] fact, invFact;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(br.readLine().trim());
        int[] ns = new int[t];
        int maxN = 0;
        for (int i = 0; i < t; i++) {
            ns[i] = Integer.parseInt(br.readLine().trim());
            if (ns[i] > maxN) maxN = ns[i];
        }

        // Precompute factorials and inverse factorials up to maxN
        fact = new int[maxN + 1];
        invFact = new int[maxN + 1];
        fact[0] = 1;
        for (int i = 1; i <= maxN; i++) {
            fact[i] = (int)((long)fact[i - 1] * i % MOD);
        }
        invFact[maxN] = modPow(fact[maxN], MOD - 2);
        for (int i = maxN; i >= 1; i--) {
            invFact[i - 1] = (int)((long)invFact[i] * i % MOD);
        }

        StringBuilder sb = new StringBuilder();
        for (int n : ns) {
            sb.append(computeMEOW(n)).append('\n');
        }
        System.out.print(sb);
    }

    /**
     * Compute MEOW for a permutation of size n.
     */
    static int computeMEOW(int n) {
        long ans = 0;
        int t = (n - 1) / 2;  // floor((n-1)/2)

        // 1) small m part
        for (int m = 0; m <= t; m++) {
            // v runs from m+1 to 2m+1
            for (int v = m + 1; v <= 2 * m + 1; v++) {
                // term = v * C(v-1, m) * C(n-v, 2m+1-v)
                long c1 = binom(v - 1, m);
                long c2 = binom(n - v, 2 * m + 1 - v);
                long term = v;
                term = (term * c1) % MOD;
                term = (term * c2) % MOD;
                ans = (ans + term) % MOD;
            }
        }

        // 2) large m part
        for (int m = t + 1; m <= n; m++) {
            long c = binom(n, m);
            long term = c * (2L * m + 1) % MOD;
            ans = (ans + term) % MOD;
        }

        return (int) ans;
    }

    /**
     * Fast binomial coefficient nCk modulo MOD.
     * Returns 0 if k<0 or k>n.
     */
    static long binom(int n, int k) {
        if (k < 0 || k > n) return 0;
        return ((long)fact[n] * invFact[k] % MOD) * invFact[n - k] % MOD;
    }

    /**
     * Fast exponentiation: a^e % MOD.
     */
    static int modPow(long a, long e) {
        long res = 1, base = a % MOD;
        while (e > 0) {
            if ((e & 1) != 0) res = (res * base) % MOD;
            base = (base * base) % MOD;
            e >>= 1;
        }
        return (int) res;
    }
}