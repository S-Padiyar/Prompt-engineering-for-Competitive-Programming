import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // Fast I/O
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        int t = Integer.parseInt(in.readLine().trim());
        // The pattern we are matching
        char[] pat = new char[]{'n','a','r','e','k'};
        final long NEG_INF = (long)-1e18;
        
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());
            
            // Read all n blocks
            String[] blocks = new String[n];
            for (int i = 0; i < n; i++) {
                blocks[i] = in.readLine();
            }
            
            // dp[k] = best value 10*x - t so far, with automaton in state k (0..4)
            long[] dp = new long[5];
            Arrays.fill(dp, NEG_INF);
            dp[0] = 0;  // start with no partial match, zero value
            
            // Process each block
            for (int i = 0; i < n; i++) {
                String s = blocks[i];
                // Count how many letters in s are among {n,a,r,e,k}
                int t_add = 0;
                for (char c : s.toCharArray()) {
                    if (c=='n'||c=='a'||c=='r'||c=='e'||c=='k') t_add++;
                }
                
                // Precompute transitions for each possible incoming state
                int[] outState = new int[5];
                int[] xAdd     = new int[5];
                
                for (int kin = 0; kin < 5; kin++) {
                    int k = kin, xCount = 0;
                    for (char c : s.toCharArray()) {
                        if (c == pat[k]) {
                            k++;
                            if (k == 5) {
                                // found a full "narek"
                                xCount++;
                                k = 0;
                            }
                        }
                    }
                    outState[kin] = k;
                    xAdd[kin]     = xCount;
                }
                
                // Do the take/skip DP update
                long[] newDp = Arrays.copyOf(dp, 5);
                for (int kin = 0; kin < 5; kin++) {
                    if (dp[kin] == NEG_INF) continue;
                    long val = dp[kin] + 10L * xAdd[kin] - t_add;
                    int kout = outState[kin];
                    newDp[kout] = Math.max(newDp[kout], val);
                }
                
                dp = newDp;
            }
            
            // Answer is the best among all final states
            long ans = NEG_INF;
            for (int k = 0; k < 5; k++) {
                ans = Math.max(ans, dp[k]);
            }
            // If all negative, we can always pick nothing => score = 0
            ans = Math.max(ans, 0L);
            
            out.println(ans);
        }
        out.flush();
    }
}