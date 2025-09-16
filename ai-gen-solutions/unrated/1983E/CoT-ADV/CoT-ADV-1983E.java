import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 1_000_000_007;

    // fast exponentiation a^e mod
    static long modPow(long a, long e) {
        long res = 1 % MOD;
        a %= MOD;
        while (e > 0) {
            if ((e & 1) != 0) res = (res * a) % MOD;
            a = (a * a) % MOD;
            e >>= 1;
        }
        return res;
    }

    // modular inverse via Fermat: a^(MOD-2) mod
    static long inv(long a) {
        return modPow(a, MOD - 2);
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            // read n,k
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int k = Integer.parseInt(st.nextToken());
            int m = n - k;  // number of ordinary balls

            // read values
            long sumSpecial = 0, sumOrdinary = 0;
            st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= n; i++) {
                long v = Long.parseLong(st.nextToken());
                if (i <= k) sumSpecial = (sumSpecial + v) % MOD;
                else          sumOrdinary = (sumOrdinary + v) % MOD;
            }

            // build fraction pS = p0_num/p0_den, pN = p1_num/p1_den
            long p0_num, p0_den;
            long p1_num = 0, p1_den = 1;  // for ordinary

            if ((m & 1) == 0) {
                // m even
                // p_S = (m+2) / (2*(m+1)) ,  p_N = 1/2
                p0_num = m + 2L;
                p0_den = 2L * (m + 1L) % MOD;
                if (m > 0) {
                    p1_num = 1;
                    p1_den = 2;
                }
            } else {
                // m odd
                // p_S = 1/2 , p_N = (m+1)/(2*m)
                p0_num = 1;
                p0_den = 2;
                if (m > 0) {
                    p1_num = m + 1L;
                    p1_den = 2L * m % MOD;
                }
            }

            // compute Alice = sumSpecial * pS + sumOrdinary * pN  (all mod)
            long inv_p0 = inv(p0_den);
            long aliceSpecial = (sumSpecial % MOD) * (p0_num % MOD) % MOD * inv_p0 % MOD;

            long aliceOrd = 0;
            if (m > 0) {
                long inv_p1 = inv(p1_den);
                aliceOrd = (sumOrdinary % MOD) * (p1_num % MOD) % MOD * inv_p1 % MOD;
            }
            long alice = (aliceSpecial + aliceOrd) % MOD;

            // Bob = totalSum - Alice
            long total = (sumSpecial + sumOrdinary) % MOD;
            long bob = (total - alice + MOD) % MOD;

            System.out.println(alice + " " + bob);
        }
    }
}