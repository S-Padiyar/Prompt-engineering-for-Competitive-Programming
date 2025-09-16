import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int T = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();
        while (T-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            int[] a = new int[n];
            StringTokenizer st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }
            sb.append(countBrilliant(a)).append('\n');
        }
        System.out.print(sb);
    }

    static long countBrilliant(int[] a) {
        int n = a.length;

        // freq map of distinct values in current window
        HashMap<Integer,Integer> freq = new HashMap<>();
        long badCount = 0;     // number of subarrays with NO power-of-two difference
        int pairCount = 0;     // number of unordered pairs in window whose diff = 2^k

        // sliding window [L..R], we will move R from 0..n-1
        int L = 0;

        for (int R = 0; R < n; R++) {
            // --- ADD a[R] into the window ---
            int x = a[R];
            int oldFreq = freq.getOrDefault(x, 0);
            if (oldFreq == 0) {
                // we are inserting x freshly, check all 2^k neighbors
                for (int k = 0; k < 31; k++) {
                    int d = 1 << k;
                    // check x-d
                    Integer f1 = freq.get(x - d);
                    if (f1 != null && f1 > 0) pairCount++;
                    // check x+d
                    Integer f2 = freq.get(x + d);
                    if (f2 != null && f2 > 0) pairCount++;
                }
            }
            freq.put(x, oldFreq + 1);

            // --- SHRINK from left until the window has NO good pair ---
            // as soon as pairCount>0, we have at least one power-of-two difference => brilliant
            // we must remove from left until pairCount==0 again
            while (pairCount > 0) {
                int y = a[L];
                int fy = freq.get(y);
                // decrement freq
                if (fy == 1) {
                    // we are removing y entirely => subtract out all pairs it formed
                    for (int k = 0; k < 31; k++) {
                        int d = 1 << k;
                        Integer f1 = freq.get(y - d);
                        if (f1 != null && f1 > 0) pairCount--;
                        Integer f2 = freq.get(y + d);
                        if (f2 != null && f2 > 0) pairCount--;
                    }
                    freq.remove(y);
                } else {
                    freq.put(y, fy - 1);
                }
                L++;
            }

            // now [L..R] is maximal window ending at R with NO power-of-two pair
            // any subarray ending at R that starts anywhere in [L..R] is also bad
            badCount += (R - L + 1);
        }

        // total subarrays = n*(n+1)/2
        long total = (long)n * (n + 1) / 2;
        // brilliant = total - bad
        return total - badCount;
    }
}