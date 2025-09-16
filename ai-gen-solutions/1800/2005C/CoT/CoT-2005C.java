import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder out = new StringBuilder();
        
        // The target pattern "narek"
        char[] pattern = {'n','a','r','e','k'};
        // Map each letter 'a'..'z' to its index in pattern or -1
        int[] patIndex = new int[26];
        Arrays.fill(patIndex, -1);
        for (int i = 0; i < 5; i++) {
            patIndex[pattern[i] - 'a'] = i;
        }

        final int NEG_INF = -(int)1e9;
        
        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());
            
            // dp[j] = best value ending in partial-state j
            int[] dp = new int[5];
            for (int j = 1; j < 5; j++) dp[j] = NEG_INF;
            dp[0] = 0;
            
            // Process each string
            for (int i = 0; i < n; i++) {
                String s = in.readLine();
                
                // Count T_s = how many of 'n','a','r','e','k' in s
                int T_s = 0;
                for (char c : s.toCharArray()) {
                    if (patIndex[c - 'a'] != -1) {
                        T_s++;
                    }
                }
                
                // Precompute transitions for each starting state j = 0..4
                int[] endState = new int[5];
                int[] fullCount = new int[5];
                
                for (int start = 0; start < 5; start++) {
                    int idx = start;
                    int completed = 0;
                    for (char c : s.toCharArray()) {
                        if (c == pattern[idx]) {
                            idx++;
                            if (idx == 5) {
                                completed++;
                                idx = 0;
                            }
                        }
                    }
                    endState[start] = idx;
                    fullCount[start] = completed;
                }
                
                // New DP array (copy for the "skip" case)
                int[] newDp = dp.clone();
                
                // Try taking this string from each old state
                for (int oldSt = 0; oldSt < 5; oldSt++) {
                    if (dp[oldSt] == NEG_INF) continue;
                    int ns = endState[oldSt];
                    int gain = dp[oldSt] + 10 * fullCount[oldSt] - T_s;
                    newDp[ns] = Math.max(newDp[ns], gain);
                }
                
                // Commit
                dp = newDp;
            }
            
            // The answer is the best over all end-states
            int ans = NEG_INF;
            for (int j = 0; j < 5; j++) {
                ans = Math.max(ans, dp[j]);
            }
            out.append(ans).append('\n');
        }
        
        System.out.print(out.toString());
    }
}