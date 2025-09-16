import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        int t = Integer.parseInt(in.readLine().trim());

        // We'll use a single Random for all weights.
        Random rnd = new Random();

        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());

            // Difference array on positions 1..n+1
            long[] diff = new long[n + 2];

            // For each friendship, pick a random 64-bit weight
            long[] w = new long[m];
            for (int i = 0; i < m; i++) {
                w[i] = rnd.nextLong();
            }

            // Read and register each friendship's "clockwise" interval
            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(in.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                
                // The edges of the clockwise arc are [u, u+1, …, v-1] mod n
                // Compute r = v-1 in 1..n
                int r = v - 1;
                if (r == 0) r = n;

                if (u <= r) {
                    // Non-wrapping case
                    diff[u] += w[i];
                    if (r + 1 <= n) diff[r + 1] -= w[i];
                } else {
                    // Wrapping: interval [u..n] and [1..r]
                    diff[u] += w[i];
                    diff[n + 1] -= w[i];
                    diff[1] += w[i];
                    if (r + 1 <= n) diff[r + 1] -= w[i];
                }
            }

            // Sweep from edge=1..n, build the signature hash and count occurrences
            Map<Long, Integer> freq = new HashMap<>();
            long hash = 0;
            int best = 0;

            for (int edge = 1; edge <= n; edge++) {
                hash += diff[edge];
                // Count this hash
                int f = freq.getOrDefault(hash, 0) + 1;
                freq.put(hash, f);
                if (f > best) {
                    best = f;
                }
            }

            // The answer is n - (maximum number of edges sharing one signature)
            sb.append((n - best)).append('\n');
        }

        System.out.print(sb);
    }
}