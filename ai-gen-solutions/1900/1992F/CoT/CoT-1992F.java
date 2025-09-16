import java.io.*;
import java.util.*;

public class Main {
    static final int MAXX = 100000;
    // divIdx[v] = (1 + index of v in the divisor list), or 0 if v is not a divisor
    static int[] divIdx = new int[MAXX + 1];

    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader();
        StringBuilder sb = new StringBuilder();

        int t = in.nextInt();
        while (t-- > 0) {
            int n = in.nextInt();
            int x = in.nextInt();
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = in.nextInt();
            }

            // 1) Compute and sort all divisors of x
            List<Integer> divsList = new ArrayList<>();
            for (int d = 1; d * d <= x; d++) {
                if (x % d == 0) {
                    divsList.add(d);
                    if (d * d != x) {
                        divsList.add(x / d);
                    }
                }
            }
            Collections.sort(divsList);
            int D = divsList.size();
            int[] divs = new int[D];
            for (int i = 0; i < D; i++) {
                divs[i] = divsList.get(i);
                // mark in divIdx so we can map a divisor back to its index
                divIdx[divs[i]] = i + 1;
            }
            int xIdx = divIdx[x] - 1;  // index of x among divisors

            // 2) We'll maintain a DP array dp[j] = can we form divisor divs[j] in current segment
            boolean[] dp = new boolean[D];
            dp[0] = true;  // we can always form product 1 (divs[0] == 1)

            int segments = 1;  // at least one segment
            for (int v : a) {
                // If v doesn't divide x, it can't help form x, so skip it
                if (x % v != 0) continue;

                int vmax = x / v;  // to avoid overflow, only divs[j] <= x/v can multiply by v
                boolean cut = false;

                // Try to add v to current segment's DP
                for (int j = D - 1; j >= 0; j--) {
                    if (!dp[j]) continue;
                    int dj = divs[j];
                    if (dj > vmax) continue;
                    int prod = dj * v;      // guaranteed ≤ x
                    int k = divIdx[prod] - 1; 
                    if (!dp[k]) {
                        dp[k] = true;
                    }
                    // If we just made dp[x] = true, we must cut
                    if (k == xIdx) {
                        cut = true;
                    }
                }

                if (cut) {
                    // start a new segment at v
                    segments++;
                    Arrays.fill(dp, false);
                    dp[0] = true;
                    // replay v into the fresh dp
                    for (int j = D - 1; j >= 0; j--) {
                        if (!dp[j]) continue;
                        int dj = divs[j];
                        if (dj > vmax) continue;
                        int prod = dj * v;
                        int k = divIdx[prod] - 1;
                        dp[k] = true;
                    }
                }
            }

            // Clear divIdx entries for the next test
            for (int d : divs) {
                divIdx[d] = 0;
            }

            sb.append(segments).append('\n');
        }

        System.out.print(sb);
    }

    // Fast IO
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
        FastReader() {
            br = new BufferedReader(new InputStreamReader(System.in));
        }
        String next() throws IOException {
            while (st == null || !st.hasMoreTokens())
                st = new StringTokenizer(br.readLine());
            return st.nextToken();
        }
        int nextInt() throws IOException {
            return Integer.parseInt(next());
        }
    }
}