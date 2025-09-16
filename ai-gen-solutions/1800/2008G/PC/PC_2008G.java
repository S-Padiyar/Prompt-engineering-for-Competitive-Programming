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
            long k = Long.parseLong(tok.nextToken());
            
            tok = new StringTokenizer(in.readLine());
            long[] a = new long[n];
            for (int i = 0; i < n; i++) {
                a[i] = Long.parseLong(tok.nextToken());
            }
            
            // Case 1: n == 1, cannot change the single element
            if (n == 1) {
                long x = a[0];
                if (k <= x) {
                    sb.append(k - 1).append('\n');
                } else {
                    sb.append(k).append('\n');
                }
                continue;
            }
            
            // Case 2: n >= 2
            // Compute d = gcd(a[0], a[1], ..., a[n-1])
            long d = a[0];
            for (int i = 1; i < n; i++) {
                d = gcd(d, a[i]);
            }
            
            // Let N1 = n*(d-1) be the number of missing in the first n blocks
            long N1 = (long)n * (d - 1);
            
            long answer;
            if (k <= N1) {
                // The k-th missing is in one of the first n "picked" blocks
                // Each such block contributes (d-1) missing,
                // and the missing in block b are exactly
                // b*d+1, b*d+2, ..., b*d+(d-1).
                long zeroBased = k - 1;                   // zero-based missing index
                long block = zeroBased / (d - 1);        // which block
                long offset = zeroBased % (d - 1);       // offset in that block
                answer = block * d + 1 + offset;
            } else {
                // The k-th missing is in one of the unpicked blocks,
                // each contributes d missing including its multiple.
                long rem = k - N1 - 1;  // zero-based among the "unpicked" blocks
                long block = n + (rem / d);
                long offset = rem % d;
                answer = block * d + offset;
            }
            
            sb.append(answer).append('\n');
        }
        
        System.out.print(sb);
    }
    
    // gcd helper
    private static long gcd(long x, long y) {
        while (y != 0) {
            long r = x % y;
            x = y;
            y = r;
        }
        return x;
    }
}