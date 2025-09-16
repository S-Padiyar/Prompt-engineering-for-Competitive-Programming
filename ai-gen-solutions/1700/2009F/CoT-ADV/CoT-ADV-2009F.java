import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // Fast input
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();

        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int q = Integer.parseInt(st.nextToken());

            // Read the array a[1..n]
            long[] a = new long[n];
            st = new StringTokenizer(in.readLine());
            long sumA = 0;
            for (int i = 0; i < n; i++) {
                a[i] = Long.parseLong(st.nextToken());
                sumA += a[i];
            }

            // Build ext[1..2n] as two copies of a, and its prefix sums P[0..2n]
            long[] P = new long[2 * n + 1];  // P[0] = 0
            for (int i = 1; i <= 2 * n; i++) {
                P[i] = P[i - 1] + a[(i - 1) % n];
            }

            // Function to compute B(k) = sum_{i=1..k} b[i]
            // Uses 0-based arithmetic internally
            // If k==0, sum is 0
            class Prefix {
                long get(long k) {
                    if (k == 0) return 0;
                    long block = (k - 1) / n;           // number of complete blocks before
                    long shift = block + 1;             // which cyclic shift
                    long pos = (k - 1) % n + 1;         // how many elements in the partial block
                    long partialSum = P[(int)(shift + pos - 1)] - P[(int)(shift - 1)];
                    return block * sumA + partialSum;
                }
            }
            Prefix pref = new Prefix();

            // Answer queries
            for (int i = 0; i < q; i++) {
                st = new StringTokenizer(in.readLine());
                long l = Long.parseLong(st.nextToken());
                long r = Long.parseLong(st.nextToken());
                long ans = pref.get(r) - pref.get(l - 1);
                sb.append(ans).append('\n');
            }
        }

        // Print all answers at once
        System.out.print(sb.toString());
    }
}