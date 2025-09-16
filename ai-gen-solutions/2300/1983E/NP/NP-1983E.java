import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 1_000_000_007;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(br.readLine().trim());

        int[] n = new int[t], k = new int[t];
        long[] sum = new long[t];
        int maxN = 0;

        // Read all test cases, record sums and track max n
        for (int i = 0; i < t; i++) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            n[i] = Integer.parseInt(st.nextToken());
            k[i] = Integer.parseInt(st.nextToken());
            maxN = Math.max(maxN, n[i]);
            st = new StringTokenizer(br.readLine());
            long s = 0;
            for (int j = 0; j < n[i]; j++) {
                s = (s + Integer.parseInt(st.nextToken())) % MOD;
            }
            sum[i] = s;
        }

        // Precompute modular inverses up to maxN+1
        int maxInv = maxN + 1;
        long[] inv = new long[maxInv + 1];
        inv[1] = 1;
        for (int i = 2; i <= maxInv; i++) {
            inv[i] = MOD - (MOD / i) * inv[MOD % i] % MOD;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < t; i++) {
            int ni = n[i], ki = k[i];
            long Si = sum[i];

            int N = ni - ki;  // # non-special
            // c1 = ceil(N/2) = floor((N+1)/2)
            long c1 = (N + 1L) / 2;
            // s = ceil((N+1)/2) = floor((N+2)/2)
            long s  = (N + 2L) / 2;
            long den = (N + 1L) % MOD; 

            // A_num = c1*(N+1) + k*s  (mod MOD)
            long A_num = (c1 * den + ki * s) % MOD;

            // Alice's expected sum:
            //   = Si * A_num * inv[N+1] * inv[n]  (all mod MOD)
            long alice = Si % MOD;
            alice = alice * A_num % MOD;
            alice = alice * inv[(int)den] % MOD;
            alice = alice * inv[ni] % MOD;

            // Bob gets the rest
            long bob = (Si - alice + MOD) % MOD;

            sb.append(alice).append(" ").append(bob).append("\n");
        }

        System.out.print(sb);
    }
}