import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer tok = new StringTokenizer(in.readLine());
        int t = Integer.parseInt(tok.nextToken());
        StringBuilder sb = new StringBuilder();
        
        while (t-- > 0) {
            tok = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(tok.nextToken());
            int k = Integer.parseInt(tok.nextToken());
            int[] a = new int[n];
            tok = new StringTokenizer(in.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(tok.nextToken());
            }
            
            // Case 1: k = 1 -> we can in effect choose any single element
            if (k == 1) {
                int mx = Integer.MIN_VALUE;
                for (int v : a) if (v > mx) mx = v;
                sb.append(mx).append('\n');
                continue;
            }
            
            // Case 2: no deletions possible
            if (k > n) {
                // The final size is n, median is the floor((n+1)/2)-th after sorting
                int[] copy = Arrays.copyOf(a, n);
                Arrays.sort(copy);
                sb.append(copy[(n + 1) / 2 - 1]).append('\n');
                continue;
            }
            
            // Compute how many will remain: m = n mod k; if zero then m = k
            int m = n % k == 0 ? k : (n % k);
            
            // Case 3: final size = 1 but k>1 => m=1 => survivors must be i≡1 mod k
            if (m == 1) {
                int best = Integer.MIN_VALUE;
                // 1-based indices i with (i-1)%k==0 => i=1, 1+k, 1+2k, ...
                for (int i = 0; i < n; i += k) {
                    if (a[i] > best) best = a[i];
                }
                sb.append(best).append('\n');
                continue;
            }
            
            // Case 4: final size >=2.  Use the sliding window heuristic:
            int ans = Integer.MIN_VALUE;
            for (int i = 0; i + k - 1 < n; i++) {
                int cur = Math.min(a[i], a[i + k - 1]);
                if (cur > ans) ans = cur;
            }
            sb.append(ans).append('\n');
        }
        
        System.out.print(sb);
    }
}