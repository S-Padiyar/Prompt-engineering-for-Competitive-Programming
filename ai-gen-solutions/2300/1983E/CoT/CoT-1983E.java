import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 1_000_000_007;
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer tok;

        int t = Integer.parseInt(in.readLine());
        // We may have sum of all n up to ~5e5, so we precompute inverses up to that.
        int MAXN = 500_000 + 5;
        int[] inv = new int[MAXN];
        inv[1] = 1;
        for (int i = 2; i < MAXN; i++) {
            // inv[i] = MOD - (MOD/i)*inv[MOD%i] mod MOD
            inv[i] = (int) ( (long)(MOD - MOD/i) * inv[MOD % i] % MOD );
        }

        while (t-- > 0) {
            tok = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(tok.nextToken());
            int k = Integer.parseInt(tok.nextToken());
            int M = n - k; // number of non-special

            long sumSp = 0, sumNs = 0;
            tok = new StringTokenizer(in.readLine());
            for (int i = 0; i < k; i++) {
                sumSp = (sumSp + Integer.parseInt(tok.nextToken())) % MOD;
            }
            for (int i = k; i < n; i++) {
                sumNs = (sumNs + Integer.parseInt(tok.nextToken())) % MOD;
            }

            long ansA;
            if (M == 0) {
                // No non-specials: Alice gets all special balls
                ansA = sumSp;
            } else {
                // P_sp = (floor(M/2)+1)/(M+1)
                long cntEven = (M/2) + 1; 
                long Psp = cntEven * inv[M+1] % MOD;

                // P_ns = floor((M+1)/2)/M
                long cntHalf = ((M + 1L) / 2L);
                long Pns = cntHalf * inv[M] % MOD;

                ansA = (Psp * sumSp + Pns * sumNs) % MOD;
            }

            long total = (sumSp + sumNs) % MOD;
            long ansB = (total - ansA + MOD) % MOD;

            System.out.println(ansA + " " + ansB);
        }
    }
}