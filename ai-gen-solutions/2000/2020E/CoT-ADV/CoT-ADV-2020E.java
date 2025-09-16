import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 1_000_000_007;
    static long modInv(long x) {
        // Fermat little theorem, since MOD is prime
        long res = 1, pow = MOD-2;
        x %= MOD;
        while (pow > 0) {
            if ((pow & 1) == 1) res = (res * x) % MOD;
            x = (x * x) % MOD;
            pow >>= 1;
        }
        return res;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        int t = Integer.parseInt(in.readLine());
        
        long inv10000 = modInv(10000);
        long inv2 = modInv(2);
        long inv4 = modInv(4);
        
        while (t-- > 0) {
            int n = Integer.parseInt(in.readLine());
            int[] a = new int[n];
            int[] p = new int[n];
            
            st = new StringTokenizer(in.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }
            st = new StringTokenizer(in.readLine());
            for (int i = 0; i < n; i++) {
                p[i] = Integer.parseInt(st.nextToken());
            }
            
            // Compute Q[i] = 1 - 2*P[i], where P[i] = p[i]/10000
            long[] Q = new long[n];
            for (int i = 0; i < n; i++) {
                long Pi = (p[i] * inv10000) % MOD;
                Q[i] = (1 - 2*Pi % MOD + MOD) % MOD;
            }
            
            // T[k] = product of Q[i] over i with bit k = 1
            long[] T = new long[10];
            Arrays.fill(T, 1L);
            
            // Tpair[k][ℓ] = product of Q[i] over i with (bit_k XOR bit_ℓ)=1
            long[][] Tpair = new long[10][10];
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    Tpair[i][j] = 1L;
                }
            }
            
            // Accumulate products
            for (int i = 0; i < n; i++) {
                int mask = a[i];
                // For each bit k set
                for (int k = 0; k < 10; k++) {
                    if (((mask >> k) & 1) == 1) {
                        T[k] = (T[k] * Q[i]) % MOD;
                    }
                }
                // For each pair k<ℓ or even k>ℓ, we just do all k,ℓ
                for (int k = 0; k < 10; k++) {
                    for (int ℓ = k + 1; ℓ < 10; ℓ++) {
                        int bitK = (mask >> k) & 1;
                        int bitL = (mask >> ℓ) & 1;
                        if ((bitK ^ bitL) == 1) {
                            Tpair[k][ℓ] = (Tpair[k][ℓ] * Q[i]) % MOD;
                        }
                    }
                }
            }
            
            // Now compute the final sum
            long ans = 0;
            
            // Single-bit contributions
            for (int k = 0; k < 10; k++) {
                // Pₖ = (1 - T[k])/2
                long pk = (1 - T[k] + MOD) % MOD * inv2 % MOD;
                long term = pk * modPow(2, 2*k, MOD) % MOD;
                ans = (ans + term) % MOD;
            }
            
            // Two-bit contributions
            for (int k = 0; k < 10; k++) {
                for (int ℓ = k + 1; ℓ < 10; ℓ++) {
                    // Pₖℓ = (1 - T[k] - T[ℓ] + Tpair[k][ℓ])/4
                    long joint = (1 - T[k] - T[ℓ] + Tpair[k][ℓ]) % MOD;
                    if (joint < 0) joint += MOD;
                    joint = joint * inv4 % MOD;
                    long term = (2 * modPow(2, k+ℓ, MOD)) % MOD * joint % MOD;
                    ans = (ans + term) % MOD;
                }
            }
            
            System.out.println(ans);
        }
    }
    
    // Fast exponentiation
    static long modPow(long base, int exp, int mod) {
        long res = 1, b = base % mod;
        while (exp > 0) {
            if ((exp & 1) == 1) res = (res * b) % mod;
            b = (b * b) % mod;
            exp >>= 1;
        }
        return res;
    }
}