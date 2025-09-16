import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder out = new StringBuilder();
        int t = Integer.parseInt(in.readLine().trim());
        
        // We'll reuse arrays up to the maximum n across all test cases.
        int maxN = 200_000;
        long[] a = new long[maxN + 5];
        int[] nxt = new int[maxN + 5];
        int[] f   = new int[maxN + 5];
        long[] depth = new long[maxN + 5];
        
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            long x = Long.parseLong(st.nextToken());
            st = new StringTokenizer(in.readLine());
            for (int i = 1; i <= n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }
            
            // 1) Build nxt[i]: first index j >= i such that sum(a[i..j]) > x.
            int r = 1;
            long sum = 0;
            for (int l = 1; l <= n; l++) {
                // Extend r while we can eat without overflow
                while (r <= n && sum + a[r] <= x) {
                    sum += a[r];
                    r++;
                }
                // Now either r>n or sum + a[r] > x => r is our overflow point
                nxt[l] = r;  // r in [l..n+1]
                
                // Slide window start forward
                if (r > l) {
                    // we had included a[l] in sum
                    sum -= a[l];
                } else {
                    // r == l => window is empty, force r to l+1
                    r = l + 1;
                }
            }
            
            // 2) Build f[i] = nxt[i+1], and set f[n] = n+1
            for (int i = 0; i < n; i++) {
                f[i] = nxt[i + 1];
            }
            f[n] = n + 1;
            
            // 3) DP from right to left to count how many steps land inside [1..n]
            //    depth[i] = 0 if f[i] > n, else 1 + depth[f[i]].
            depth[n] = 0;
            for (int i = n - 1; i >= 0; i--) {
                if (f[i] > n) {
                    depth[i] = 0;
                } else {
                    depth[i] = 1 + depth[f[i]];
                }
            }
            
            // 4) Sum depth[0..n-1]: that is the number of subarrays whose final g = 0
            long zeroCount = 0;
            for (int i = 0; i < n; i++) {
                zeroCount += depth[i];
            }
            
            // Total subarrays = n*(n+1)/2
            long total = (long)n * (n + 1) / 2;
            long answer = total - zeroCount;
            out.append(answer).append('\n');
        }
        
        System.out.print(out);
    }
}