import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 998244353;
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        // Precompute powers of two up to 300,000
        int MAX = 300000;
        long[] pow2 = new long[MAX+1];
        pow2[0] = 1;
        for (int i = 1; i <= MAX; i++) {
            pow2[i] = (pow2[i-1] << 1) % MOD;
        }

        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            int n = Integer.parseInt(in.readLine().trim());
            st = new StringTokenizer(in.readLine());
            long[] a = new long[n+1];
            for (int i = 1; i <= n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }

            // 1) Build prefix-sums S and track minimum
            long[] S = new long[n+1];
            long minS = Long.MAX_VALUE;
            for (int i = 1; i <= n; i++) {
                S[i] = S[i-1] + a[i];
                if (S[i] < minS) minS = S[i];
            }

            // If no prefix ever went negative, all choices yield the same final sum S[n]
            if (minS >= 0) {
                // 2^n mod
                System.out.println(pow2[n]);
                continue;
            }

            // Otherwise we must flip at one of the positions j where S[j] == minS
            // Build negBefore[i] = count of k<i with S[k]<0
            int[] negBefore = new int[n+1];
            int cntN = 0;
            for (int i = 1; i <= n; i++) {
                negBefore[i] = cntN;
                if (S[i] < 0) cntN++;
            }

            // Sum over those j with S[j] == minS
            long ans = 0;
            for (int j = 1; j <= n; j++) {
                if (S[j] == minS) {
                    // exponent = n - 1 - negBefore[j]
                    int exp = n - 1 - negBefore[j];
                    ans = (ans + pow2[exp]) % MOD;
                }
            }

            System.out.println(ans);
        }
    }
}