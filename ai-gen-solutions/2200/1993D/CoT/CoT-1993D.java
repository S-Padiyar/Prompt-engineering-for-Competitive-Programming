import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader in;
    static PrintWriter out;
    static StringTokenizer tok;

    public static void main(String[] args) throws IOException {
        in = new BufferedReader(new InputStreamReader(System.in));
        out = new PrintWriter(System.out);

        int t = nextInt();
        while (t-- > 0) {
            int n = nextInt();
            int k = nextInt();
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = nextInt();
            }

            // Compute L = final length after removing in blocks of size k
            int r = n % k;
            int L = (r == 0 ? k : r);

            // For i = 1..L, we will pick the maximum among a[j] with j ≡ i (mod k).
            // We do 0-based internally, so residue class i=1 means indices j=0,0+k,0+2k,... 
            // i=2 means j=1,1+k,1+2k,... up to i=L => j=i-1, (i-1)+k, ...
            int[] v = new int[L];
            Arrays.fill(v, Integer.MIN_VALUE);

            for (int j = 0; j < n; j++) {
                int cls = j % k;  // this is i-1 when i = cls+1
                if (cls < L) {
                    v[cls] = Math.max(v[cls], a[j]);
                }
            }

            // Sort these L class‐maxima and take the median
            Arrays.sort(v);
            int medianIndex = (L - 1) / 2;  // 0‐based index of floor((L+1)/2)
            out.println(v[medianIndex]);
        }

        out.flush();
    }

    // fast input boilerplate
    static String next() throws IOException {
        while (tok == null || !tok.hasMoreTokens()) {
            tok = new StringTokenizer(in.readLine());
        }
        return tok.nextToken();
    }
    static int nextInt() throws IOException {
        return Integer.parseInt(next());
    }
}