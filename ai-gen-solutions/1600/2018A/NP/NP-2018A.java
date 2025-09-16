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
            long k = Long.parseLong(st.nextToken());
            
            long[] a = new long[n];
            st = new StringTokenizer(in.readLine());
            long sumA = 0, maxA = 0;
            for (int i = 0; i < n; i++) {
                a[i] = Long.parseLong(st.nextToken());
                sumA += a[i];
                if (a[i] > maxA) {
                    maxA = a[i];
                }
            }
            
            // Binary search for the largest deck-size x in [1..n]
            int lo = 1, hi = n;
            while (lo < hi) {
                int mid = (lo + hi + 1) >>> 1;  // upper middle
                if (can(mid, sumA, maxA, k)) {
                    lo = mid;
                } else {
                    hi = mid - 1;
                }
            }
            
            sb.append(lo).append('\n');
        }
        
        System.out.print(sb);
    }
    
    /**
     * Check if deck-size x is feasible.
     * We need an integer t such that:
     *   t >= maxA
     *   t >= ceil(sumA / x)
     *   t <= floor((sumA + k) / x)
     */
    static boolean can(int x, long sumA, long maxA, long k) {
        // minimal t we require
        long need1 = maxA;
        long need2 = (sumA + x - 1) / x;   // ceil(sumA / x)
        long lowT = Math.max(need1, need2);
        // maximal t allowed by our budget
        long highT = (sumA + k) / x;
        return lowT <= highT;
    }
}