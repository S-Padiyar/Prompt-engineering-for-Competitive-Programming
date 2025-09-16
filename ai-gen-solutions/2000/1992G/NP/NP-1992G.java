import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 1_000_000_007;
    static int MAXN = 5000;    // problem constraint on n
    static int[] fact, invf;

    // fast exponentiation mod
    static int modPow(long a, long e) {
        long res = 1 % MOD;
        a %= MOD;
        while (e > 0) {
            if ((e & 1) != 0) res = (res * a) % MOD;
            a = (a * a) % MOD;
            e >>= 1;
        }
        return (int)res;
    }

    // binomial coefficient nCk mod
    static int comb(int n, int k) {
        if (k < 0 || k > n || n < 0) return 0;
        return (int) ((long)fact[n] * invf[k] % MOD * invf[n-k] % MOD);
    }

    public static void main(String[] args) throws IOException {
        // Precompute factorials and inverse factorials up to MAXN
        fact = new int[MAXN+1];
        invf = new int[MAXN+1];
        fact[0] = 1;
        for (int i = 1; i <= MAXN; i++) {
            fact[i] = (int)((long)fact[i-1] * i % MOD);
        }
        // inverse factorial via Fermat's little theorem
        invf[MAXN] = modPow(fact[MAXN], MOD-2);
        for (int i = MAXN; i >= 1; i--) {
            invf[i-1] = (int)((long)invf[i] * i % MOD);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());

            long answer = 0;
            // loop over subset sizes k
            for (int k = 0; k <= n; k++) {
                // sum over m = k+1 .. min(n, 2k+1)
                int mMax = Math.min(n, 2*k + 1);
                for (int m = k+1; m <= mMax; m++) {
                    int c1 = comb(m-1, k);
                    int x = 2*k - m + 1;
                    int c2 = comb(n-m, x);
                    long term = (long)m * c1 % MOD * c2 % MOD;
                    answer = (answer + term) % MOD;
                }
                // if 2k+1 > n, then we have the extra "overflow" term
                int mOver = 2*k + 1;
                if (mOver > n) {
                    long term = (long)mOver * comb(n, k) % MOD;
                    answer = (answer + term) % MOD;
                }
            }
            sb.append(answer).append('\n');
        }
        System.out.print(sb);
    }
}