import java.io.*;
import java.util.*;

public class Main {
    static int[][] all;      // all[h][s] = #ways unconstrained in subtree height=h, sum=s
    static int[] fact, invFact;
    static int mod;

    // fast exponentiation mod
    static long modPow(long a, long e) {
        long r = 1 % mod;
        a %= mod;
        while (e > 0) {
            if ((e & 1) != 0) r = (r * a) % mod;
            a = (a * a) % mod;
            e >>= 1;
        }
        return r;
    }

    // modular inverse via Fermat (mod is prime)
    static int inv(int x) {
        return (int)modPow(x, mod - 2);
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter pw = new PrintWriter(System.out);

        int t = Integer.parseInt(br.readLine());
        for (int tc = 0; tc < t; tc++) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int k = Integer.parseInt(st.nextToken());
            mod = Integer.parseInt(st.nextToken());

            // precompute factorials and inverse factorials up to k
            fact = new int[k + 1];
            invFact = new int[k + 1];
            fact[0] = 1;
            for (int i = 1; i <= k; i++) {
                fact[i] = (int)((long)fact[i-1] * i % mod);
            }
            invFact[k] = inv(fact[k]);
            for (int i = k - 1; i >= 0; i--) {
                invFact[i] = (int)((long)invFact[i+1] * (i+1) % mod);
            }

            // precompute 2^h mod p up to h=n
            long[] pow2 = new long[n+1];
            pow2[0] = 1;
            for (int i = 1; i <= n; i++) {
                pow2[i] = (pow2[i-1] * 2) % mod;
            }

            // Build all[h][s] = C(s + 2^h -2, s)  for h=1..n, s=0..k
            all = new int[n+1][k+1];
            for (int h = 1; h <= n; h++) {
                // subtree size = 2^h -1  => we need C(s + (2^h-1)-1, s) = C(s+2^h-2, s)
                long m = (pow2[h] - 1 + mod) % mod; 
                // numerator product P[s] = m * (m+1) * ... * (m+s-1)
                long P = 1;
                all[h][0] = 1;
                for (int s = 1; s <= k; s++) {
                    // multiply by (m + (s-1)) mod p
                    P = (P * ((m + (s - 1)) % mod)) % mod;
                    // now divide by s!  => multiply by invFact[s]
                    all[h][s] = (int)(P % mod * invFact[s] % mod);
                }
            }

            // dp_path: rolling arrays of length k+1
            int[] prevPath = new int[k+1], curPath = new int[k+1];
            // height =1
            for (int s = 0; s <= k; s++) {
                prevPath[s] = 1;  // a single node with sum s
            }

            // Build up to height n
            for (int h = 2; h <= n; h++) {
                // prefix sums of prevPath
                long[] prefix = new long[k+1];
                prefix[0] = prevPath[0];
                for (int s = 1; s <= k; s++) {
                    prefix[s] = prefix[s-1] + prevPath[s];
                    if (prefix[s] >= mod) prefix[s] -= mod;
                }
                // for each total sum S at this height
                for (int S = 0; S <= k; S++) {
                    long ways = 0;
                    // off‐path child gets sum x in [0..(S-1)/2]
                    int maxOff = (S - 1) >> 1;
                    for (int x = 0; x <= maxOff; x++) {
                        // strictly path‐child sum y in [x+1 .. S-x]
                        long cntOff = all[h-1][x];
                        long cntOn  = prefix[S - x] - prefix[x];
                        if (cntOn < 0) cntOn += mod;
                        ways += cntOff * cntOn;
                        if (ways >= (long)mod*mod) ways %= mod;
                    }
                    curPath[S] = (int)(ways % mod);
                }
                // swap prevPath & curPath
                int[] tmp = prevPath;
                prevPath = curPath;
                curPath  = tmp;
            }

            // prevPath[k] = #heaps following one fixed path
            long ans = prevPath[k];
            // multiply by 2^{n-1} for choice of which leaf‐path is taken
            ans = ans * pow2[n-1] % mod;
            pw.println(ans);
        }

        pw.flush();
    }
}