import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            // Read n, q
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int q = Integer.parseInt(st.nextToken());

            // Read array a and build frequency F[1..n]
            int[] F = new int[n + 1];
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                int v = Integer.parseInt(st.nextToken());
                F[v]++;
            }

            // Build prefix-sum P[0..n]
            int[] P = new int[n + 1];
            for (int i = 1; i <= n; i++) {
                P[i] = P[i - 1] + F[i];
            }

            // Read queries and record unique x's
            int[] queries = new int[q];
            boolean[] seenX = new boolean[n + 1];
            List<Integer> uniqueX = new ArrayList<>();

            for (int i = 0; i < q; i++) {
                int x = Integer.parseInt(br.readLine().trim());
                queries[i] = x;
                if (!seenX[x]) {
                    seenX[x] = true;
                    uniqueX.add(x);
                }
            }

            // Prepare answer array
            int[] ans = new int[n + 1];  // only ans[x] for seen x used

            // The median position we want
            int medianPos = (n % 2 == 1) ? (n + 1) / 2 : (n / 2 + 1);

            // Process each distinct x
            for (int x : uniqueX) {
                if (x == 1) {
                    // Everything mod 1 is 0 ⇒ median is 0
                    ans[x] = 0;
                    continue;
                }

                // Binary search for smallest r in [0..x-1] with Sx(r) >= medianPos
                int lo = 0, hi = x - 1, best = x - 1;
                while (lo <= hi) {
                    int mid = (lo + hi) >>> 1;

                    // Compute Sx(mid) = # of v with (v mod x) <= mid
                    int cnt = 0;
                    // We sum for k=0.. while k*x <= n
                    for (int k = 0; ; k++) {
                        int base = k * x;
                        if (base > n) break;
                        int upper = base + mid;
                        if (upper > n) upper = n;
                        // Sum F[base+1..upper] = P[upper] - P[base]
                        cnt += P[upper] - (base == 0 ? 0 : P[base]);
                        if (cnt >= medianPos) break;  // early exit
                    }

                    if (cnt >= medianPos) {
                        best = mid;
                        hi = mid - 1;
                    } else {
                        lo = mid + 1;
                    }
                }

                ans[x] = best;
            }

            // Output answers in original query order
            for (int x : queries) {
                sb.append(ans[x]).append(' ');
            }
            sb.append('\n');

            // Clear the seen[] marks for the next test
            for (int x : uniqueX) {
                seenX[x] = false;
            }
        }

        System.out.print(sb);
    }
}