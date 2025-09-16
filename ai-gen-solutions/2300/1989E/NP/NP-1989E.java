import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 998244353;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        int n = Integer.parseInt(st.nextToken());
        int k = Integer.parseInt(st.nextToken());
        // dp[state][c] where state = last two bits in {00,01,10,11} = 0..3,
        // and c = how many ones so far in b[1..i-1], clipped at (k-1).
        int maxC = k - 1;
        int[][] dp = new int[4][k];
        int[][] nxt = new int[4][k];

        // initial condition: we imagine b[0] = 1, and no bits yet from 1..0
        // so last-two bits = (b[-1],b[0]) = (0,1) => state = 0*2+1 = 1
        dp[1][0] = 1;

        // build b[i] for i = 1..n-1 (free bits)
        for (int i = 1; i <= n - 1; i++) {
            for (int s = 0; s < 4; s++) {
                Arrays.fill(nxt[s], 0);
            }
            for (int state = 0; state < 4; state++) {
                int b2 = (state >> 1) & 1; // b[i-2]
                int b1 = state & 1;        // b[i-1]
                for (int c = 0; c < k; c++) {
                    int ways = dp[state][c];
                    if (ways == 0) continue;
                    // try bit = 0 or 1
                    for (int bit = 0; bit < 2; bit++) {
                        // forbid pattern 1,0,1 at interior i=3..n-1
                        if (i >= 3 && i <= n - 1) {
                            if (b2 == 1 && b1 == 0 && bit == 1) {
                                // would make "101" in the middle => forbid
                                continue;
                            }
                        }
                        int nc = c + bit; 
                        if (nc > maxC) nc = maxC;
                        int nstate = ((b1 << 1) | bit);
                        nxt[nstate][nc] = (nxt[nstate][nc] + ways) % MOD;
                    }
                }
            }
            // swap dp & nxt
            for (int s = 0; s < 4; s++) {
                System.arraycopy(nxt[s], 0, dp[s], 0, k);
            }
        }

        // finally we must set b[n] = 1, but it does not change c (we do NOT count that one),
        // nor do we forbid "101" at i=n.
        // Thus the final count is just sum of dp over c >= k-1
        long ans = 0;
        for (int s = 0; s < 4; s++) {
            ans = (ans + dp[s][maxC]) % MOD;
        }

        System.out.println(ans);
    }
}