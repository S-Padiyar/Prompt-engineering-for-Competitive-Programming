import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 1_000_000_007;
    static final int MAXMASK = 1<<10;  // 1024
    static int inv10000, inv4;
    static int[] pow2 = new int[10];
    // bitsOfMask[m] = array of bit‐positions that are 1 in m
    static int[][] bitsOfMask = new int[MAXMASK][];
    
    public static void main(String[] args) throws IOException {
        precomputeStatics();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        int t = Integer.parseInt(st.nextToken());
        
        // We'll reuse these arrays across test‐cases to avoid repeated allocation:
        int[] G = new int[MAXMASK];
        boolean[] active = new boolean[MAXMASK];
        ArrayList<Integer> activeList = new ArrayList<>();
        
        StringBuilder sb = new StringBuilder();
        for (int _case = 0; _case < t; _case++) {
            int n = Integer.parseInt(br.readLine().trim());
            
            // read a_i
            st = new StringTokenizer(br.readLine());
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }
            
            // read p_i, form w_i = (1 - 2 p_i) mod
            st = new StringTokenizer(br.readLine());
            activeList.clear();
            for (int i = 0; i < n; i++) {
                int pi = Integer.parseInt(st.nextToken());
                // p_i mod M = pi * inv10000 mod M
                long pmod = (long)pi * inv10000 % MOD;
                // w_i = 1 - 2 p_i
                int w = (int)((1 - 2*pmod + 2L*MOD) % MOD);
                int m = a[i];
                if (!active[m]) {
                    active[m] = true;
                    G[m] = w;
                    activeList.add(m);
                } else {
                    G[m] = (int)((long)G[m] * w % MOD);
                }
            }
            
            // Compute S_b for b=0..9
            int[] S_b = new int[10];
            Arrays.fill(S_b, 1);
            for (int m : activeList) {
                int g = G[m];
                for (int b : bitsOfMask[m]) {
                    S_b[b] = (int)((long)S_b[b] * g % MOD);
                }
            }
            
            // Compute S_{b⊕c} in a 10×10 array
            int[][] S_xor = new int[10][10];
            for (int b = 0; b < 10; b++) {
                for (int c = 0; c < 10; c++) {
                    S_xor[b][c] = 1;
                }
            }
            // For each active mask m, multiply G[m] into those pairs (b,c)
            // for which bit_b(m)⊕bit_c(m)=1
            for (int m : activeList) {
                int g = G[m];
                // we'll use bitsOfMask[m] = positions of ones
                // and for each b in that set we do c's that are zeros
                for (int b : bitsOfMask[m]) {
                    for (int c = 0; c < 10; c++) {
                        if (((m >> c) & 1) == 0) {
                            // (b,c) and (c,b) both need multiplying
                            S_xor[b][c] = (int)((long)S_xor[b][c] * g % MOD);
                            S_xor[c][b] = (int)((long)S_xor[c][b] * g % MOD);
                        }
                    }
                }
            }
            
            // Now form the double sum
            long answer = 0;
            for (int b = 0; b < 10; b++) {
                for (int c = 0; c < 10; c++) {
                    // numerator = 1 - S_b - S_c + S_{b⊕c}
                    long num = 1 - S_b[b] - S_b[c] + S_xor[b][c];
                    num %= MOD;
                    if (num < 0) num += MOD;
                    // divide by 4
                    long term = num * inv4 % MOD;
                    // multiply by 2^b * 2^c
                    term = term * pow2[b] % MOD * pow2[c] % MOD;
                    answer += term;
                }
            }
            
            answer %= MOD;
            sb.append(answer).append('\n');
            
            // clear active[] for next test
            for (int m : activeList) {
                active[m] = false;
            }
        }
        
        System.out.print(sb);
    }
    
    /** Precompute factorials, inverses, powers of two, and bit‐lists. */
    static void precomputeStatics() {
        inv10000 = modInv(10000, MOD);
        inv4 = modInv(4, MOD);
        pow2[0] = 1;
        for (int i = 1; i < 10; i++) {
            pow2[i] = (int)((pow2[i-1] * 2L) % MOD);
        }
        // build bit lists for masks 0..1023
        for (int m = 0; m < MAXMASK; m++) {
            int cnt = Integer.bitCount(m);
            bitsOfMask[m] = new int[cnt];
            int idx = 0;
            for (int b = 0; b < 10; b++) {
                if (((m >> b) & 1) != 0) {
                    bitsOfMask[m][idx++] = b;
                }
            }
        }
    }
    
    /** Fast exponentiation a^p mod m. */
    static long modPow(long a, long p, int m) {
        long r = 1 % m;
        a %= m;
        while (p > 0) {
            if ((p & 1) != 0) r = (r * a) % m;
            a = (a * a) % m;
            p >>= 1;
        }
        return r;
    }
    
    /** Modular inverse of a mod m (m prime) via Fermat's little theorem. */
    static int modInv(int a, int m) {
        return (int)modPow(a, m - 2, m);
    }
}