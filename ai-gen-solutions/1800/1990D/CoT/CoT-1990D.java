import java.io.*;
import java.util.*;

public class Main {
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
        FastReader(InputStream in){ br=new BufferedReader(new InputStreamReader(in)); }
        String next() {
            while (st==null || !st.hasMoreTokens()) {
                try { st=new StringTokenizer(br.readLine()); }
                catch(IOException e){ throw new RuntimeException(e); }
            }
            return st.nextToken();
        }
        int nextInt(){ return Integer.parseInt(next()); }
    }

    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader(System.in);
        PrintWriter out = new PrintWriter(System.out);

        int t = in.nextInt();
        while(t-->0) {
            int n = in.nextInt();
            int[] a = new int[n];
            for(int i=0;i<n;i++) {
                a[i] = in.nextInt();
            }
            // b[i] = ceil(a[i]/2)
            int[] b = new int[n];
            for(int i=0;i<n;i++){
                b[i] = (a[i]+1)/2;
            }

            // dp holds pairs (carry -> best cost).
            // Start at i=0 with carry=0, cost=0.
            TreeMap<Integer,Integer> dp = new TreeMap<>();
            dp.put(0, 0);

            for(int i=0;i<n;i++){
                int need = b[i];
                // If need==0, we can collapse all carries to (0 -> minCost).
                if(need==0){
                    int best = Integer.MAX_VALUE;
                    for(int c: dp.values()){
                        best = Math.min(best, c);
                    }
                    dp.clear();
                    dp.put(0, best);
                    continue;
                }
                // Otherwise, do the two transitions per old state:
                //  1) pay row-paint => carry'=0, cost+1
                //  2) no paint: carry' = max(0, need - carry), cost += carry'
                TreeMap<Integer,Integer> next = new TreeMap<>();

                for(Map.Entry<Integer,Integer> e: dp.entrySet()){
                    int carryOld = e.getKey();
                    int costOld  = e.getValue();
                    // (1) row-paint:
                    int c1 = 0;
                    int cost1 = costOld + 1;
                    next.merge(c1, cost1, Math::min);

                    // (2) no row-paint:
                    int x = need - carryOld;
                    if(x < 0) x = 0;  // no deficit
                    int c2 = x;
                    int cost2 = costOld + x;
                    next.merge(c2, cost2, Math::min);
                }

                // Prune dominated states: if carry2>carry1 and cost2<=cost1 then (carry1) is never better.
                TreeMap<Integer,Integer> pruned = new TreeMap<>();
                int bestSoFar = Integer.MAX_VALUE;
                // Iterate in descending carry
                for(int c : next.descendingKeySet()){
                    int cst = next.get(c);
                    if(cst < bestSoFar) {
                        // keep
                        pruned.put(c, cst);
                        bestSoFar = cst;
                    }
                }
                dp = pruned;
            }

            // Final answer = min cost among all carries
            int ans = Integer.MAX_VALUE;
            for(int c: dp.values()){
                ans = Math.min(ans, c);
            }
            out.println(ans);
        }
        out.flush();
    }
}