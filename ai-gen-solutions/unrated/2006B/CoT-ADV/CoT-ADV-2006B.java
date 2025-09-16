import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        int t = Integer.parseInt(in.readLine().trim());
        
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            long w = Long.parseLong(st.nextToken());
            
            // Read parents p[2..n]
            int[] parent = new int[n+1];
            st = new StringTokenizer(in.readLine());
            for (int i = 2; i <= n; i++) {
                parent[i] = Integer.parseInt(st.nextToken());
            }
            
            // Compute R[i] = end of DFS-interval of subtree rooted at i
            int[] R = new int[n+1];
            for (int i = 1; i <= n; i++) {
                R[i] = i;
            }
            for (int i = n; i >= 2; i--) {
                int p = parent[i];
                if (R[i] > R[p]) R[p] = R[i];
            }
            
            // cnt[i] = how many *unknown* edges currently cover the pair-index i
            // i runs 1..n, where i = "pair (i,i+1)" mod n
            int[] cnt = new int[n+1];
            for (int j = 2; j <= n; j++) {
                // each edge j covers two boundaries
                int pos1 = j - 1;      // covers pair (j-1, j)
                int pos2 = R[j];       // covers pair (R[j], R[j]+1) mod n
                cnt[pos1]++;
                cnt[pos2]++;
            }
            
            // Initial C = # of pairs still having unknown edges = all n
            int C = n;  
            // but actually we should count how many i in [1..n] have cnt[i]>=1
            // with all edges unknown it's surely all n, but let's do it once
            // in case some cnt[i] = 0 (rare if n small).
            // (Every tree edge gives 2 covers, so sum cnt = 2(n-1), so no zero
            // slots only if some slot never covered; bottom line: it is n.)
            // We'll assume C=n to save O(n), but even O(n) pass is fine.
            
            long S_fixed = 0L;         // sum of fixed weights so far
            
            // Process the n-1 events
            for (int e = 1; e < n; e++) {
                st = new StringTokenizer(in.readLine());
                int x = Integer.parseInt(st.nextToken());
                long y = Long.parseLong(st.nextToken());
                
                // Reveal edge x's weight = y
                S_fixed += y;
                
                // Remove that edge's coverage
                int p1 = x - 1;
                cnt[p1]--;
                if (cnt[p1] == 0) C--;
                
                int p2 = R[x];
                cnt[p2]--;
                if (cnt[p2] == 0) C--;
                
                long W_remain = w - S_fixed;
                // Answer = 2*S_fixed + C * W_remain
                long ans = 2 * S_fixed + C * W_remain;
                sb.append(ans).append(e+1 < n ? ' ' : '\n');
            }
        }
        
        // Output all at once
        System.out.print(sb.toString());
    }
}