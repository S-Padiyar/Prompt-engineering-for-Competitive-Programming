import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 998244353;
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int n = Integer.parseInt(br.readLine().trim());
        StringTokenizer st = new StringTokenizer(br.readLine());
        int[] a = new int[n];
        int maxA = 0;
        for (int i = 0; i < n; i++) {
            a[i] = Integer.parseInt(st.nextToken());
            if (a[i] > maxA) maxA = a[i];
        }

        // 1) Build smallest-prime-factor (spf) table up to maxA
        int[] spf = new int[maxA + 1];
        for (int i = 2; i <= maxA; i++) {
            if (spf[i] == 0) {
                for (int j = i; j <= maxA; j += i) {
                    if (spf[j] == 0) spf[j] = i;
                }
            }
        }

        // 2) sumdp[d] = sum of dp[u] for all u<v with d | a[u]
        int[] sumdp = new int[maxA + 1];

        // Precompute bit‐counts for masks up to 1<<7 = 128
        int MAXMASK = 1 << 7;
        int[] bitCount = new int[MAXMASK];
        for (int m = 1; m < MAXMASK; m++) {
            bitCount[m] = bitCount[m >>> 1] + (m & 1);
        }

        // Map single-bit masks to their index: bitIdx[1<<i]=i
        int[] bitIdx = new int[MAXMASK];
        for (int i = 0; i < 7; i++) {
            bitIdx[1 << i] = i;
        }

        // dp[v] will be computed on the fly; we only need dp[n-1] at the end.
        int dpAtN = 0;

        // A small buffer for subset-products
        int[] prodBuf = new int[MAXMASK];

        for (int i = 0; i < n; i++) {
            // 1) Factor a[i] to get distinct primes
            int x = a[i];
            ArrayList<Integer> primes = new ArrayList<>();
            while (x > 1) {
                int p = spf[x];
                primes.add(p);
                while (x % p == 0) {
                    x /= p;
                }
            }
            int k = primes.size();
            int limit = 1 << k;

            int dpVal;
            if (i == 0) {
                // dp[1] = 1
                dpVal = 1;
            } else {
                // 2) Compute dp[i] by inclusion–exclusion
                long s = 0;
                prodBuf[0] = 1;
                for (int mask = 1; mask < limit; mask++) {
                    int low = mask & -mask;        // lowest set bit
                    int idx = bitIdx[low];        // which prime
                    int prev = mask ^ low;
                    prodBuf[mask] = prodBuf[prev] * primes.get(idx);
                    int bc = bitCount[mask];
                    int d = prodBuf[mask];
                    int val = sumdp[d];
                    if ((bc & 1) == 1) {
                        s += val;
                    } else {
                        s -= val;
                    }
                }
                // normalize modulo
                s %= MOD;
                if (s < 0) s += MOD;
                dpVal = (int)s;
            }

            // 3) Update sumdp for every nonempty subset of primes
            for (int mask = 1; mask < limit; mask++) {
                int d = prodBuf[mask];
                int nv = sumdp[d] + dpVal;
                if (nv >= MOD) nv -= MOD;
                sumdp[d] = nv;
            }

            if (i == n - 1) dpAtN = dpVal;
        }

        // Answer is dp[n]
        System.out.println(dpAtN);
    }
}