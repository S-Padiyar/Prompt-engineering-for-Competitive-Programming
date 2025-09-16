import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 1_000_000_007;

    // fast exponentiation mod
    static long modPow(long a, long e) {
        long res = 1 % MOD;
        a %= MOD;
        while (e > 0) {
            if ((e & 1) == 1) res = (res * a) % MOD;
            a = (a * a) % MOD;
            e >>= 1;
        }
        return res;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        // Precompute inverses and powers of 2
        long inv10000 = modPow(10000, MOD - 2);
        long inv2 = (MOD + 1) / 2;
        long inv4 = (inv2 * inv2) % MOD;
        long[] pow2 = new long[20];
        pow2[0] = 1;
        for (int i = 1; i < pow2.length; i++) {
            pow2[i] = (pow2[i - 1] * 2) % MOD;
        }

        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            int[] a = new int[n];
            int[] p = new int[n];

            StringTokenizer stA = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(stA.nextToken());
            }
            StringTokenizer stP = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                p[i] = Integer.parseInt(stP.nextToken());
            }

            // A[b] = product of (1-2p_i) over those i with b-th bit = 1
            // B[b][c] = product of (1-2p_i) over those i where exactly one of bits b,c is 1
            long[] A = new long[10];
            long[][] B = new long[10][10];
            Arrays.fill(A, 1L);
            for (int i = 0; i < 10; i++) {
                Arrays.fill(B[i], 1L);
            }

            // Build A and B
            for (int i = 0; i < n; i++) {
                long prob = (p[i] * inv10000) % MOD;        // p_i / 10000 mod
                long D = (1 - 2 * prob) % MOD;              // 1 - 2*p_i
                if (D < 0) D += MOD;

                int mask = a[i];
                // update A
                for (int b = 0; b < 10; b++) {
                    if (((mask >> b) & 1) == 1) {
                        A[b] = (A[b] * D) % MOD;
                    }
                }
                // update B for b<c
                for (int b = 0; b < 10; b++) {
                    int bitb = (mask >> b) & 1;
                    for (int c = b + 1; c < 10; c++) {
                        int bitc = (mask >> c) & 1;
                        if ((bitb ^ bitc) == 1) {
                            B[b][c] = (B[b][c] * D) % MOD;
                        }
                    }
                }
            }

            // Compute the expectation
            long ans = 0;

            // diagonal terms i=j
            for (int b = 0; b < 10; b++) {
                long term = ((1 - A[b]) % MOD + MOD) % MOD;
                term = (term * inv2) % MOD;
                term = (term * pow2[2 * b]) % MOD;
                ans = (ans + term) % MOD;
            }

            // off-diagonal terms i != j
            for (int b = 0; b < 10; b++) {
                for (int c = 0; c < 10; c++) {
                    if (b == c) continue;
                    long Ab = A[b], Ac = A[c];
                    long Bbc = (b < c ? B[b][c] : B[c][b]);
                    long num = (1 - Ab - Ac + Bbc) % MOD;
                    if (num < 0) num += MOD;
                    long term = (pow2[b + c] * num) % MOD;
                    term = (term * inv4) % MOD;
                    ans = (ans + term) % MOD;
                }
            }

            out.println(ans);
        }

        out.flush();
    }
}