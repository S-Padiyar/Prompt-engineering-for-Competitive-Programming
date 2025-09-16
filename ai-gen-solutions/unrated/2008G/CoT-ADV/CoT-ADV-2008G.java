import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // Fast input
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();
        
        while (t-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            long k = Long.parseLong(st.nextToken());
            
            st = new StringTokenizer(br.readLine());
            long[] a = new long[n];
            for (int i = 0; i < n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }
            
            // Case n = 1: no operations allowed
            if (n == 1) {
                long x = a[0];
                // The first x missing are 0..x-1
                if (k <= x) {
                    sb.append(k - 1).append('\n');
                } else {
                    // Then missing continue from x+0, x+1, ...
                    sb.append(k).append('\n');
                }
                continue;
            }
            
            // Compute gcd of all a[i]
            long g = a[0];
            for (int i = 1; i < n; i++) {
                g = gcd(g, a[i]);
            }
            
            // If gcd = 1, we can cover 0..n-1. Missing start at n, n+1, ...
            if (g == 1) {
                // k-th missing = (n) + (k-1)
                sb.append((n + k - 1)).append('\n');
                continue;
            }
            
            // Otherwise g > 1
            long delta = g - 1;            // missing per block
            long M = delta * n;            // missing count in first n blocks
            
            if (M < k) {
                // Case B: we exhaust the first n blocks
                // result = n*g + (k - M - 1)
                long ans = n * g + (k - M - 1);
                sb.append(ans).append('\n');
            } else {
                // Case A: the k-th missing lies in the first few blocks
                // b = (k-1)/delta, off = (k-1)%delta
                long b = (k - 1) / delta;
                long off = (k - 1) % delta;
                long ans = b * g + 1 + off;
                sb.append(ans).append('\n');
            }
        }
        
        // Output all at once
        System.out.print(sb.toString());
    }
    
    // Helper: compute gcd of two longs
    private static long gcd(long x, long y) {
        while (y != 0) {
            long r = x % y;
            x = y;
            y = r;
        }
        return x;
    }
}