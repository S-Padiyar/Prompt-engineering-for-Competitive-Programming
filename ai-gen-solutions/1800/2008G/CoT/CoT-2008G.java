import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer tk = new StringTokenizer(in.readLine());
        int t = Integer.parseInt(tk.nextToken());
        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            tk = new StringTokenizer(in.readLine());
            long n = Long.parseLong(tk.nextToken());
            long k = Long.parseLong(tk.nextToken());

            tk = new StringTokenizer(in.readLine());
            long[] a = new long[(int)n];
            for (int i = 0; i < n; i++) {
                a[i] = Long.parseLong(tk.nextToken());
            }

            // Case n=1: no operations possible.
            if (n == 1) {
                long v = a[0];
                if (k <= v) {
                    sb.append(k - 1).append('\n');
                } else {
                    sb.append(k).append('\n');
                }
                continue;
            }

            // Compute gcd of all a[i].
            long g = a[0];
            for (int i = 1; i < n; i++) {
                g = gcd(g, a[i]);
            }

            // If gcd = 1, it's the special case g-1 = 0 => S = 0.
            // Then we occupy 0,1,...,n-1 => first missing is n.
            if (g == 1) {
                // S = n*(1-1) = 0
                // answer = n + (k - 1)
                sb.append(n + (k - 1)).append('\n');
                continue;
            }

            // For g > 1:
            long S = n * (g - 1);  // total missing below n*g
            if (k <= S) {
                // The k-th missing is in [0, n*g), in the non-multiples of g
                long blockSize = g - 1;
                long fullBlocks = (k - 1) / blockSize;
                long rem = (k - 1) % blockSize + 1;
                long ans = fullBlocks * g + rem;
                sb.append(ans).append('\n');
            } else {
                // The k-th missing is >= n*g, all integers missing there,
                // so they come one by one after n*g.
                long ans = n * g + (k - S - 1);
                sb.append(ans).append('\n');
            }
        }

        System.out.print(sb);
    }

    // Standard Euclidean gcd
    static long gcd(long x, long y) {
        while (y != 0) {
            long t = x % y;
            x = y;
            y = t;
        }
        return x;
    }
}