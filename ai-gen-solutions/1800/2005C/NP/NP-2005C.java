import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int T = Integer.parseInt(in.readLine().trim());
        // The pattern we seek as a subsequence:
        char[] P = { 'n', 'a', 'r', 'e', 'k' };
        // Quick lookup: is this character one of {n,a,r,e,k} ?
        boolean[] isPatChar = new boolean[26];
        for (char c : P) {
            isPatChar[c - 'a'] = true;
        }

        StringBuilder sb = new StringBuilder();
        final int NEG_INF = -1_000_000_000;

        while (T-- > 0) {
            // Read n and m
            st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());

            // dp[s] = best 10*f - T so far, ending in automaton state s
            int[] dp = new int[5];
            dp[0] = 0;
            for (int i = 1; i < 5; i++) dp[i] = NEG_INF;

            for (int i = 0; i < n; i++) {
                String s = in.readLine().trim();

                // Count T_i = how many chars in s are in {n,a,r,e,k}
                int T_i = 0;
                for (char c : s.toCharArray()) {
                    if (isPatChar[c - 'a']) {
                        T_i++;
                    }
                }

                // Precompute for each start state:
                //   endState[start], fCount[start]
                int[] endState = new int[5];
                int[] fCount   = new int[5];

                for (int start = 0; start < 5; start++) {
                    int curState = start;
                    int fullPasses = 0;
                    for (char c : s.toCharArray()) {
                        if (c == P[curState]) {
                            curState++;
                            if (curState == 5) {
                                // completed "narek"
                                fullPasses++;
                                curState = 0;
                            }
                        }
                    }
                    endState[start] = curState;
                    fCount[start]   = fullPasses;
                }

                // Now do the DP update: either skip or take the string
                int[] dp2 = dp.clone();
                for (int stt = 0; stt < 5; stt++) {
                    if (dp[stt] == NEG_INF) continue;
                    int ns = endState[stt];
                    int gain = dp[stt] + 10 * fCount[stt] - T_i;
                    dp2[ns] = Math.max(dp2[ns], gain);
                }
                dp = dp2;
            }

            // Our answer is the maximum over all end‐states (we could leave a partial match)
            int ans = dp[0];
            for (int i = 1; i < 5; i++) {
                ans = Math.max(ans, dp[i]);
            }
            sb.append(ans).append('\n');
        }

        System.out.print(sb);
    }
}