import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int T = Integer.parseInt(in.readLine().trim());
        StringBuilder sb = new StringBuilder();
        while (T-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int k = Integer.parseInt(st.nextToken());
            int p = Integer.parseInt(st.nextToken());
            int H = n - 1;

            // dpNext corresponds to dp[j+1], dpCur to dp[j].
            long[] dpNext = new long[k+1], dpCur = new long[k+1];

            // Base case for j = H
            dpNext[0] = 0;
            for (int t = 1; t <= k; t++) {
                dpNext[t] = 1;   // Exactly 1 way if t_H >= 1
            }

            // Work backwards j = H-1 ... 0
            for (int j = H - 1; j >= 0; j--) {
                // Build prefix sums of dpNext
                long[] S0 = new long[k+1], S1 = new long[k+1];
                for (int u = 1; u <= k; u++) {
                    S0[u] = (S0[u-1] + dpNext[u]) % p;
                    S1[u] = (S1[u-1] + dpNext[u] * u) % p;
                }
                // Fill dpCur[t]
                dpCur[0] = 0;
                for (int t = 1; t <= k; t++) {
                    // m = floor((t+1)/2)
                    int m = (t + 1) >> 1;
                    long s0t = S0[t], s0m = S0[m];
                    long s1t = S1[t], s1m = S1[m];
                    // sum1 = sum_{u=1..m} u*dpNext[u]
                    long sum1 = s1m;
                    // sum2 = (t+1)*sum_{u=m+1..t} dpNext[u]
                    long part = (s0t - s0m + p) % p;
                    long sum2 = part * (t + 1) % p;
                    // sum3 = sum_{u=m+1..t} u*dpNext[u]
                    long sum3 = (s1t - s1m + p) % p;
                    dpCur[t] = (sum1 + sum2 - sum3) % p;
                    if (dpCur[t] < 0) dpCur[t] += p;
                }
                // swap dpCur, dpNext
                long[] tmp = dpNext;
                dpNext = dpCur;
                dpCur = tmp;
            }

            long waysForOneLeaf = dpNext[k];          // dp[0][k]
            long pow2 = modPow(2, H, p);              // 2^{n-1} mod p
            long ans = (waysForOneLeaf * pow2) % p;
            sb.append(ans).append('\n');
        }

        System.out.print(sb);
    }

    // fast exponentiation mod m
    static long modPow(long base, int exp, int m) {
        long res = 1 % m, cur = base % m;
        while (exp > 0) {
            if ((exp & 1) != 0) res = (res * cur) % m;
            cur = (cur * cur) % m;
            exp >>= 1;
        }
        return res;
    }
}