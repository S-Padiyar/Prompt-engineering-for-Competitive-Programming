import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 1_000_000_007;
    static int MAXN = 5000;
    static long[] fact, invFact;

    public static void main(String[] args) throws IOException {
        // Fast input
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        int t = Integer.parseInt(st.nextToken());

        // Precompute factorials and inverses up to MAXN
        fact = new long[MAXN + 1];
        invFact = new long[MAXN + 1];
        fact[0] = 1;
        for (int i = 1; i <= MAXN; i++) {
            fact[i] = fact[i - 1] * i % MOD;
        }
        invFact[MAXN] = modInverse(fact[MAXN]);
        for (int i = MAXN; i > 0; i--) {
            invFact[i - 1] = invFact[i] * i % MOD;
        }

        StringBuilder sb = new StringBuilder();
        // Process each test
        for (int _case = 0; _case < t; _case++) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            long answer = 0;

            // Split at m0 = floor((n-1)/2)
            int m0 = (n - 1) / 2;

            // Regime A: m <= m0, do the sum over t
            for (int m = 0; m <= m0; m++) {
                long sumM = 0;
                // t goes from 0..m
                // x = m+1+t
                for (int tVar = 0; tVar <= m; tVar++) {
                    int x = m + 1 + tVar;
                    // C(m+t, t)
                    long c1 = binom(m + tVar, tVar);
                    // C(n - 1 - m - t, m - t)
                    long c2 = binom(n - 1 - m - tVar, m - tVar);
                    long term = ( (long)x * c1 % MOD ) * c2 % MOD;
                    sumM = (sumM + term) % MOD;
                }
                answer = (answer + sumM) % MOD;
            }

            // Regime B: m > m0, MEX = 2m+1
            for (int m = m0 + 1; m <= n; m++) {
                long c = binom(n, m);
                long mexVal = 2L * m + 1;
                answer = (answer + c * mexVal) % MOD;
            }

            sb.append(answer).append('\n');
        }

        // Output all answers
        System.out.print(sb.toString());
    }

    // Compute nCk mod using precomputed factorials
    static long binom(int n, int k) {
        if (k < 0 || k > n || n < 0) return 0;
        return fact[n] * invFact[k] % MOD * invFact[n - k] % MOD;
    }

    // Fast exponentiation for modular inverse
    static long modInverse(long x) {
        long pow = MOD - 2;
        long res = 1, base = x % MOD;
        while (pow > 0) {
            if ((pow & 1) == 1) {
                res = (res * base) % MOD;
            }
            base = (base * base) % MOD;
            pow >>= 1;
        }
        return res;
    }
}