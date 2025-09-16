import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(in.readLine().trim());
        final int INF = 1_000_000_000;
        
        while (t-- > 0) {
            int n = Integer.parseInt(in.readLine().trim());
            int[] a = new int[n+2];
            StringTokenizer st = new StringTokenizer(in.readLine());
            for (int i = 1; i <= n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }
            // dp[i][c] = min cost after finishing rows < i,
            //            with c (0..2) blacks of row i already covered by a square from above.
            int[] dpPrev = {0, INF, INF};
            
            for (int i = 1; i <= n; i++) {
                int[] dpCur = {INF, INF, INF};
                
                // Build the small list of possible (d_i, d_{i+1}) coverages
                // from placing exactly one 2x2 or none:
                //   (0,0) always allowed (no square),
                //   (1,1) if a[i]>=1 && a[i+1]>=1,
                //   (2,1) if a[i]>=2 && a[i+1]>=1,
                //   (1,2) if a[i]>=1 && a[i+1]>=2,
                //   (2,2) if a[i]>=2 && a[i+1]>=2.
                List<int[]> choices = new ArrayList<>();
                choices.add(new int[]{0, 0});
                if (i < n) {
                    if (a[i] >= 1 && a[i+1] >= 1) choices.add(new int[]{1, 1});
                    if (a[i] >= 2 && a[i+1] >= 1) choices.add(new int[]{2, 1});
                    if (a[i] >= 1 && a[i+1] >= 2) choices.add(new int[]{1, 2});
                    if (a[i] >= 2 && a[i+1] >= 2) choices.add(new int[]{2, 2});
                }
                
                for (int coveredAbove = 0; coveredAbove <= 2; coveredAbove++) {
                    int baseCost = dpPrev[coveredAbove];
                    if (baseCost >= INF) continue;
                    
                    for (int[] ch : choices) {
                        int d_i     = ch[0]; // how many blacks in row i covered by this square
                        int d_next  = ch[1]; // how many blacks in row i+1 covered
                        
                        // Do we need to pay a row‐operation in row i?
                        int coveredNow = coveredAbove + d_i;
                        int rowOp = (coveredNow >= a[i] ? 0 : 1);
                        
                        // Did we place a 2x2 at all?
                        int sqOp = (d_i!=0 || d_next!=0) ? 1 : 0;
                        
                        int cost = baseCost + rowOp + sqOp;
                        
                        // Next state's "coveredAbove" for row i+1 is d_next (capped at 2)
                        int cnext = Math.min(d_next, 2);
                        dpCur[cnext] = Math.min(dpCur[cnext], cost);
                    }
                }
                
                dpPrev = dpCur;
            }
            
            // After finishing row n, we must have carried in coverage=0 to row n+1
            int answer = dpPrev[0];
            System.out.println(answer);
        }
    }
}