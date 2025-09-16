import java.io.*;
import java.util.*;

public class Main {
    // Maximum total length across all testcases
    static final int MAXN = 200_000 + 5;

    // Hash parameters
    static final long MOD1 = 1_000_000_007;
    static final long MOD2 = 1_000_000_009;
    static final long BASE = 9113823;

    // Precomputed powers of BASE mod MOD1 and MOD2
    static long[] pow1 = new long[MAXN], pow2 = new long[MAXN];

    public static void main(String[] args) throws IOException {
        // Precompute BASE^i mod MOD1 and MOD2
        pow1[0] = pow2[0] = 1;
        for (int i = 1; i < MAXN; i++) {
            pow1[i] = (pow1[i - 1] * BASE) % MOD1;
            pow2[i] = (pow2[i - 1] * BASE) % MOD2;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        int t = Integer.parseInt(st.nextToken());

        StringBuilder ansOut = new StringBuilder();

        // Reusable arrays (no per-test allocations)
        int[] s    = new int[MAXN];
        int[] srev = new int[MAXN];
        long[] hs1 = new long[MAXN], hs2 = new long[MAXN];
        long[] hr1 = new long[MAXN], hr2 = new long[MAXN];
        long[] ht1 = new long[MAXN], ht2 = new long[MAXN];
        char[]  tempT = new char[MAXN];

        for (int _case = 0; _case < t; _case++) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int k = Integer.parseInt(st.nextToken());
            String str = br.readLine().trim();

            // Map '0'->1, '1'->2 to avoid zero in polynomial hash
            for (int i = 0; i < n; i++) {
                s[i] = (str.charAt(i) == '0' ? 1 : 2);
                srev[n - 1 - i] = s[i];
            }

            // Build prefix-hash of s and srev
            buildHash(s,    hs1, hs2, n);
            buildHash(srev, hr1, hr2, n);

            int answer = -1;

            // Try both possible k-proper “phases”: start bit 0 or start bit 1
            for (int startBit = 0; startBit <= 1 && answer < 0; startBit++) {
                // Build the target T of length n
                // Blocks of k: even block idx => startBit, odd => 1-startBit
                for (int i = 0; i < n; i++) {
                    int block = i / k;
                    int bit   = ((block & 1) == 0 ? startBit : 1 - startBit);
                    tempT[i] = (bit == 0 ? '0' : '1');
                }
                // Map T into ints 1/2
                for (int i = 0; i < n; i++) {
                    int v = (tempT[i] == '0' ? 1 : 2);
                    ht1[i] = (i == 0 ? v : (ht1[i - 1] * BASE + v) % MOD1);
                    ht2[i] = (i == 0 ? v : (ht2[i - 1] * BASE + v) % MOD2);
                }

                // Now test all p = 1..n
                for (int p = 1; p <= n; p++) {
                    int lenA = p, lenB = n - p;

                    // 1) reverse(A) ?= T[n-p .. n-1]
                    int  l1 = n - p, r1 = n - 1;
                    long h_srev_1 = substringHash(hr1, pow1,  l1, r1, MOD1);
                    long h_srev_2 = substringHash(hr2, pow2,  l1, r1, MOD2);
                    long h_t_1    = substringHash(ht1, pow1,  l1, r1, MOD1);
                    long h_t_2    = substringHash(ht2, pow2,  l1, r1, MOD2);
                    if (h_srev_1 != h_t_1 || h_srev_2 != h_t_2) {
                        continue;
                    }

                    // 2) B = s[p..n-1] ?= T[0..n-p-1]
                    if (lenB > 0) {
                        long h_suf_s_1 = substringHash(hs1, pow1,  p, n - 1, MOD1);
                        long h_suf_s_2 = substringHash(hs2, pow2,  p, n - 1, MOD2);
                        long h_pre_t_1 = substringHash(ht1, pow1,  0, lenB - 1, MOD1);
                        long h_pre_t_2 = substringHash(ht2, pow2,  0, lenB - 1, MOD2);
                        if (h_suf_s_1 != h_pre_t_1 || h_suf_s_2 != h_pre_t_2) {
                            continue;
                        }
                    }
                    // both conditions passed
                    answer = p;
                    break;
                }
            }

            ansOut.append(answer).append('\n');
        }

        System.out.print(ansOut);
    }

    /** Build rolling hash h1[], h2[] for integer array a[0..n-1]. */
    static void buildHash(int[] a, long[] h1, long[] h2, int n) {
        long cur1 = 0, cur2 = 0;
        for (int i = 0; i < n; i++) {
            cur1 = (cur1 * BASE + a[i]) % MOD1;
            cur2 = (cur2 * BASE + a[i]) % MOD2;
            h1[i] = cur1;
            h2[i] = cur2;
        }
    }

    /**
     * Return the double‐hash of substring h[l..r] in O(1).
     * Precondition: h[i] is prefix ‑ hash of length i+1.
     */
    static long substringHash(long[] h, long[] pw,
                              int l, int r, long mod) {
        if (l == 0) {
            return h[r];
        } else {
            long res = (h[r] - (h[l - 1] * pw[r - l + 1]) % mod) % mod;
            if (res < 0) res += mod;
            return res;
        }
    }
}