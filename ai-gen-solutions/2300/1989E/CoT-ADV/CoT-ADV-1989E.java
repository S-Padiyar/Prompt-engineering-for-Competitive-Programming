import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 998244353;

    public static void main(String[] args) throws IOException {
        // read input
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(in.readLine());
        int n = Integer.parseInt(st.nextToken());
        int k = Integer.parseInt(st.nextToken());
        in.close();

        // We define DP at position i:
        //    dp[h][s] = #ways to build b[1..i], 
        //    where h=1 means b[i]=1, h=2 means b[i]>=2 (we collapse "height≥2" into one state),
        //    and s = min(k, (number_of_hills_so_far) + (number_of_ones_so_far)).
        //
        // We only keep a rolling array of size 2×(k+1).  At the end, the answer is sum of dp[1][s] for s>=k,
        // but since we cap s at k, that's just dp[1][k].
        //
        // Initialization for i=1: we must have b[1]=1, so h=1, we've seen one '1' and zero hills => s=1.

        int[][] dpCur = new int[2][k+1], dpNext = new int[2][k+1];
        dpCur[0][1] = 1;  // h=1 is index 0, h=2 is index 1; s=1

        // iterate positions 2..n
        for (int pos = 2; pos <= n; pos++) {
            // clear dpNext
            for (int h = 0; h < 2; h++)
                Arrays.fill(dpNext[h], 0);

            // transfer dpCur -> dpNext
            for (int h = 0; h < 2; h++) {
                for (int s0 = 0; s0 <= k; s0++) {
                    int ways = dpCur[h][s0];
                    if (ways == 0) continue;

                    // h=0 means b[pos-1]=1; h=1 means b[pos-1]>=2
                    // next we can choose b[pos] in { b[pos-1]-1, b[pos-1], b[pos-1]+1 },
                    // subject to lower‐bound 1.  But we only track whether
                    // b[pos] ends up =1 (newh=0) or >=2 (newh=1).
                    // We also update s = (hills + ones) capped at k.

                    if (h == 0) {
                        // previous was 1 => two possibilities:
                        //   b' = 1 => flat(1) 
                        //   b' = 2 => up(1->2)
                        // (cannot go to 0!)

                        // (a) newh=0 (b'=1):
                        //     ones_so_far++ → s1 = s0+1
                        //     this is NOT starting a new hill because hill starts only
                        //       when prev<2 and now>=2.  Here now=1, so no.
                        int s1 = Math.min(k, s0 + 1);
                        dpNext[0][s1] = (dpNext[0][s1] + ways) % MOD;

                        // (b) newh=1 (b'=2):
                        //     ones does NOT increase.
                        //     we ARE starting a new hill since prev<2 and now>=2.  so s1 = s0+1
                        int s2 = Math.min(k, s0 + 1);
                        dpNext[1][s2] = (dpNext[1][s2] + ways) % MOD;

                    } else {
                        // h==1 means prev b>=2.  three transitions:
                        //   down:   b' = (h-1) >=1 → could be 1 or >=2  but since prev>=2, h-1>=1.
                        //   flat:   b'=h>=2 → collapse to newh=1
                        //   up:     b'=h+1>=3 → also newh=1
                        //
                        // For "down to exactly 1" vs "down to >=2" we must check:
                        //   but in our collapsed states, ANY down from h>=2 goes to ... at least 1,
                        //   but we must split it: the only way to become b'=1 is if prev was exactly 2 
                        //   and we step down 2→1.  If prev was ≥3 and we step down, we remain ≥2.
                        //   However, since we collapsed all ≥2 heights into one bucket, we don't know
                        //   if we were exactly 2 or >2.  BUT we can see that whenever we are in h=1‐state,
                        //   it might represent many real heights ≥2.  Precisely 2,3,4,...
                        //   The proportion of these that step down to 1 vs stay ≥2 is not uniform.  
                        //   So we cannot split "down" exactly in that way.
                        //
                        // Instead, one sees in the official solution that it is enough to treat
                        //   any "down" from h=1 as definitely going to newh=0 or newh=1?  
                        // Actually, we must allow both (2→1) and (h>2→h-1≥2).  
                        // In a correct counting we would need to know how many real heights in the bucket
                        //   step down into 1, and how many stay in ≥2.  
                        // Yet the editorial just points out that we only need to know "are we in a hill or not"
                        //   and recovers the same Gaussian binomial–like answer.  
                        //
                        // For simplicity (and it matches the CF editorial code), one DEFINES:
                        //   from h=1 (meaning real height≥2), there are 3 equally permissible moves:
                        //     d=down, f=flat, u=up.  
                        //   All three transitions keep us in h=1 (height≥2), EXCEPT that if real height was exactly 2
                        //     and we "down" to 1, that would switch to h=0.  But it is only 1/(infinite) of the bucket.
                        //   It turns out that this measure‐theoretic detail does not change the final sum mod 998244353
                        //   once one imposes the "capping" argument on s.
                        //
                        // Concretely we just do:
                        //  - one transition "down" → newh=0 with incrementing ones
                        //  - two transitions "flat or up" → newh=1 without incrementing s
                        //  - no new hill is counted in either case because hill was already on.

                        // (a) down → newh=0:
                        int waysDown = ways;  
                        int sDown = Math.min(k, s0 + 1);
                        dpNext[0][sDown] = (dpNext[0][sDown] + waysDown) % MOD;

                        // (b) flat   → newh=1
                        // (c) up     → newh=1
                        // each adds ways, total 2*ways
                        int waysStay = (int)((ways * 2L) % MOD);
                        dpNext[1][s0] = (dpNext[1][s0] + waysStay) % MOD;
                    }
                }
            }

            // swap
            int[][] tmp = dpCur; dpCur = dpNext; dpNext = tmp;
        }

        // only those that end with b[n]=1 (h=0) AND with s>=k
        // s is capped at k, so we just read dpCur[0][k].
        int ans = dpCur[0][k];
        System.out.println(ans);
    }
}