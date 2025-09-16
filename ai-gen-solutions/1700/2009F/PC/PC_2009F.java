import java.io.*;
import java.util.StringTokenizer;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader  br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter     out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
        StringTokenizer st;

        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int q = Integer.parseInt(st.nextToken());

            // Read the array a[0..n-1]
            int[] a = new int[n];
            st = new StringTokenizer(br.readLine());
            long sumA = 0;
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
                sumA += a[i];
            }

            // Build prefix sums of the "doubled" array to handle wrap-around
            // pre2[i] = sum of a[0..i] in the infinite repetition sense, for i in [0..2n-1].
            long[] pre2 = new long[2 * n];
            for (int i = 0; i < 2 * n; i++) {
                pre2[i] = (i == 0 ? 0 : pre2[i - 1]) + a[i % n];
            }

            // A helper to get sum of b[0..x] (0-based).  If x<0, returns 0.
            // Otherwise, number of full blocks = t = x / n, remainder = p = x % n.
            // Sum = t * sumA + sum of a[t..t+p] in the circular manner, 
            // fetched via pre2.
            java.util.function.LongUnaryOperator prefixB = (long x) -> {
                if (x < 0) return 0L;
                long tBlocks = x / n;
                int p = (int)(x % n);
                int start = (int)tBlocks;       // block start in the doubled array
                int end   = start + p;          // end index in the doubled array
                long partial = pre2[end] - (start > 0 ? pre2[start - 1] : 0L);
                return tBlocks * sumA + partial;
            };

            // Answer queries
            for (int i = 0; i < q; i++) {
                st = new StringTokenizer(br.readLine());
                long L = Long.parseLong(st.nextToken()) - 1;  // to 0-based
                long R = Long.parseLong(st.nextToken()) - 1;
                long answer = prefixB.applyAsLong(R)
                                 - prefixB.applyAsLong(L - 1);
                out.println(answer);
            }
        }
        out.flush();
    }
}