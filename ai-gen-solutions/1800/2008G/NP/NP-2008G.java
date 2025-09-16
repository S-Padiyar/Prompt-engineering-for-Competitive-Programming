import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader in;
    static PrintWriter out;
    static StringTokenizer stok;

    static String nextToken() throws IOException {
        while (stok == null || !stok.hasMoreTokens()) {
            String line = in.readLine();
            if (line == null) return null;
            stok = new StringTokenizer(line);
        }
        return stok.nextToken();
    }

    static long nextLong() throws IOException {
        return Long.parseLong(nextToken());
    }

    static int nextInt() throws IOException {
        return Integer.parseInt(nextToken());
    }

    // Standard gcd
    static long gcd(long a, long b) {
        while (b != 0) {
            long t = a % b;
            a = b;
            b = t;
        }
        return a;
    }

    public static void main(String[] args) throws IOException {
        in = new BufferedReader(new InputStreamReader(System.in));
        out = new PrintWriter(System.out);

        int t = nextInt();
        while (t-- > 0) {
            int n = nextInt();
            long k = nextLong();
            long[] a = new long[n];
            for (int i = 0; i < n; i++) {
                a[i] = nextLong();
            }

            // Special case n = 1: no operations possible
            if (n == 1) {
                long v = a[0];
                // missing are all x != v
                // mex_k = (k <= v ? k-1 : k)
                long ans = (k <= v ? k - 1 : k);
                out.println(ans);
                continue;
            }

            // n >= 2: we can generate 0, d, sums, etc.
            long d = a[0];
            for (int i = 1; i < n; i++) {
                d = gcd(d, a[i]);
            }

            // total missing in the first n blocks, each of size (d-1)
            //   block 0..n-1 each miss exactly d-1 (the non-multiples)
            long totalFirst = (long)n * (d - 1);

            if (k <= totalFirst) {
                // the k-th missing is in [0..(n-1)*d + (d-1)]
                long block = (k - 1) / (d - 1);  // which full block among 0..n-1
                long r = (k - 1) % (d - 1);
                // block*d is present, so the r-th missing in that block is + (r+1)
                long ans = block * d + (r + 1);
                out.println(ans);
            } else {
                // it lies in the tail blocks t >= n, each of size d
                long rem = k - totalFirst; 
                long blockTail = (rem - 1) / d;  // how many full tail-blocks
                long r = (rem - 1) % d;
                long ans = (n + blockTail) * d + r;
                out.println(ans);
            }
        }

        out.flush();
    }
}