import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int T = Integer.parseInt(in.readLine().trim());
        // We will read each test, do an O(n k^2) DP, and print.
        for (int tc = 0; tc < T; tc++) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int k = Integer.parseInt(st.nextToken());
            int p = Integer.parseInt(st.nextToken());
            
            // dp arrays for A[d][t] and B[d][t], we only keep two rows at once.
            int[] A_prev = new int[k+1], A_curr = new int[k+1];
            int[] B_prev = new int[k+1], B_curr = new int[k+1];
            
            // Base depth = 1
            for (int t = 0; t <= k; t++) {
                A_prev[t] = 1;
                B_prev[t] = 1;
            }
            
            // Repeatedly build up from depth d-1 to d
            for (int depth = 2; depth <= n; depth++) {
                // Build prefix sums of A_prev for fast queries
                int[] prefixA = new int[k+1];
                long running = 0;
                for (int i = 0; i <= k; i++) {
                    running += A_prev[i];
                    if (running >= p) running %= p;
                    prefixA[i] = (int)running;
                }
                
                // Compute A_curr[t] = sum_{L+R <= t} A_prev[L] * A_prev[R]
                for (int t = 0; t <= k; t++) {
                    long ways = 0;
                    // naive sum_{L=0..t} A_prev[L] * (sum_{R=0..t-L} A_prev[R])
                    for (int L = 0; L <= t; L++) {
                        int sumR = prefixA[t - L]; // = sum of A_prev[0..t-L]
                        ways += (long)A_prev[L] * sumR;
                        if (ways >= (1L<<62)) ways %= p; 
                    }
                    A_curr[t] = (int)(ways % p);
                }
                
                // Compute B_curr[t] = sum_{L>R, L+R <= t} B_prev[L] * A_prev[R]
                for (int t = 0; t <= k; t++) {
                    long ways = 0;
                    // sum L from 1..t, R from 0..min(L-1, t-L)
                    for (int L = 1; L <= t; L++) {
                        int maxR = t - L;       // from sum constraint
                        int capR = L - 1;       // from L>R
                        int Rlim = (maxR < capR) ? maxR : capR;
                        if (Rlim < 0) continue;
                        int sumR = prefixA[Rlim];
                        ways += (long)B_prev[L] * sumR;
                        if (ways >= (1L<<62)) ways %= p;
                    }
                    B_curr[t] = (int)(ways % p);
                }
                
                // swap current<->prev
                int[] tmpA = A_prev; A_prev = A_curr; A_curr = tmpA;
                int[] tmpB = B_prev; B_prev = B_curr; B_curr = tmpB;
            }
            
            // B_prev[k] = B[n][k].  Multiply by 2^(n-1) mod p.
            long ans = B_prev[k];
            long pow2 = 1, base = 2;
            int exp = n - 1;
            while (exp > 0) {
                if ((exp & 1) != 0) pow2 = (pow2 * base) % p;
                base = (base * base) % p;
                exp >>= 1;
            }
            ans = (ans * pow2) % p;
            System.out.println(ans);
        }
    }
}