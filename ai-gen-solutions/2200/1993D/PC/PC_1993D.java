import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader();
        PrintWriter out = new PrintWriter(System.out);

        int t = in.nextInt();
        while (t-- > 0) {
            int n = in.nextInt();
            int k = in.nextInt();
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = in.nextInt();
            }

            // Compute final size m
            int m;
            if (n <= k) {
                m = n;
            } else {
                int r = n % k;
                m = (r == 0 ? k : r);
            }

            // For j = 1..m, find the maximum in positions i ≡ j (mod k)
            // We'll store them in an array of length m, 0-based indexed.
            int[] M = new int[m];
            Arrays.fill(M, Integer.MIN_VALUE);
            for (int j = 0; j < m; j++) {
                // class (j+1) mod k => positions j, j+k, j+2k, ...
                for (int pos = j; pos < n; pos += k) {
                    M[j] = Math.max(M[j], a[pos]);
                }
            }

            // We need at least `need = floor(m/2) + 1` classes whose max >= X
            // Sorting M descending and taking the need-th largest does the job.
            Arrays.sort(M);        // ascending
            int need = (m / 2) + 1;
            // the need-th largest in an ascending array is at index m - need
            int answer = M[m - need];

            out.println(answer);
        }

        out.flush();
    }

    // Fast input reader
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
        FastReader() {
            br = new BufferedReader(new InputStreamReader(System.in));
        }
        String next() throws IOException {
            while (st == null || !st.hasMoreTokens()) {
                String s = br.readLine();
                if (s == null) return null;
                st = new StringTokenizer(s);
            }
            return st.nextToken();
        }
        int nextInt() throws IOException {
            return Integer.parseInt(next());
        }
    }
}