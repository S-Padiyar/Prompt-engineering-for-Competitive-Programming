import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class Main {
    public static void main(String[] args) throws IOException {
        // Fast input
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        
        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            // Read n, k
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            long k = Long.parseLong(st.nextToken());
            
            // Read array a[]
            long S = 0;       // sum of a[i]
            long M = 0;       // max of a[i]
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                long ai = Long.parseLong(st.nextToken());
                S += ai;
                if (ai > M) {
                    M = ai;
                }
            }
            
            int best = 1;  // deck size at least 1 is always possible
            // Try all s = 1..n
            for (int s = 1; s <= n; s++) {
                // Minimal number of decks needed so that m*s >= S
                long L = (S + s - 1) / s;            
                // Maximal number of decks we can afford buying at most k cards
                long R = (S + k) / s;               
                long need = Math.max(M, L);
                // Feasible if there's some integer m in [need..R]
                if (need <= R) {
                    best = s;
                }
            }
            
            System.out.println(best);
        }
    }
}