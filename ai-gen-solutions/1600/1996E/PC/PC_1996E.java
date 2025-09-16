import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 1_000_000_007;

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(in.readLine());
        while (t-- > 0) {
            String s = in.readLine().trim();
            int n = s.length();

            // Build prefix sums P[0..n]
            // P[0] = 0; P[i] = P[i-1] + (1 if s.charAt(i-1)=='1' else -1)
            int[] P = new int[n+1];
            for (int i = 1; i <= n; i++) {
                P[i] = P[i-1] + (s.charAt(i-1) == '1' ? +1 : -1);
            }

            // Pair (prefixSum, index) for all i in [0..n]
            // We'll sort by prefixSum, then by index
            Pair[] arr = new Pair[n+1];
            for (int i = 0; i <= n; i++) {
                arr[i] = new Pair(P[i], i);
            }
            Arrays.sort(arr);

            // Now scan sorted array, grouping by equal prefix sums
            long answer = 0;
            int start = 0;
            while (start <= n) {
                int end = start + 1;
                while (end <= n && arr[end].sum == arr[start].sum) {
                    end++;
                }
                // We have a block arr[start..end-1] all with the same prefixSum
                // Process them in one sweep
                long sumA = 0;  // will accumulate A_i = (pos_i + 1)
                for (int k = start; k < end; k++) {
                    int idx = arr[k].index;
                    long A = idx + 1L;         // x = pos + 1
                    long B = (n - idx) + 1L;   // (n - y + 1), here y = idx
                    // Add all contributions A_i * B_j for i<j
                    answer = (answer + sumA * B) % MOD;
                    sumA = (sumA + A) % MOD;
                }
                start = end;
            }

            out.println(answer);
        }

        out.flush();
    }

    // A small helper class to store (prefix sum, index) and sort by them
    static class Pair implements Comparable<Pair> {
        int sum, index;
        Pair(int s, int i) { sum = s; index = i; }
        public int compareTo(Pair other) {
            if (this.sum != other.sum) {
                return Integer.compare(this.sum, other.sum);
            }
            return Integer.compare(this.index, other.index);
        }
    }
}