import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            long m = Long.parseLong(st.nextToken());

            long[] a = new long[n], c = new long[n];
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                c[i] = Long.parseLong(st.nextToken());
            }

            // Sort indices by petal-count a[i]
            Integer[] idx = new Integer[n];
            for (int i = 0; i < n; i++) idx[i] = i;
            Arrays.sort(idx, (i, j) -> Long.compare(a[i], a[j]));

            long best = 0;

            // 1) Single-type bouquets
            for (int _i = 0; _i < n; _i++) {
                int i = idx[_i];
                long price = a[i], stock = c[i];
                long maxCount = m / price;
                if (maxCount > stock) maxCount = stock;
                long total = maxCount * price;
                if (total > best) best = total;
            }

            // 2) Two-type bouquets with adjacent petal-counts differing by 1
            for (int _i = 0; _i + 1 < n; _i++) {
                int i = idx[_i], j = idx[_i + 1];
                // require a[j] = a[i] + 1
                if (a[j] - a[i] != 1) continue;

                long x  = a[i];
                long c1 = c[i];
                long c2 = c[j];
                // cost if we take all of both
                long totalAll = c1 * x + c2 * (x + 1);
                if (totalAll <= m) {
                    // we can afford everything
                    if (totalAll > best) best = totalAll;
                } else {
                    // otherwise try k = floor(m/x) and k-1
                    long k0 = m / x;
                    for (int d = 0; d < 2; d++) {
                        long k = k0 - d;
                        if (k < 0) continue;
                        // coins left after buying k flowers of cost x
                        long rem = m - k * x;
                        // we must buy at least this many (x+1)-flowers (if k > c1)
                        long bmin = k - c1;
                        if (bmin < 0) bmin = 0;
                        // we can buy at most c2 of them, at most k total,
                        // and we cannot exceed rem coins: each costs (x+1)
                        long bmax = k;
                        if (bmax > c2) bmax = c2;
                        long canPay = rem / (x + 1);
                        if (bmax > canPay) bmax = canPay;

                        if (bmax >= bmin) {
                            long sum = k * x + bmax * (x + 1);
                            if (sum > best) best = sum;
                        }
                    }
                }
            }

            sb.append(best).append('\n');
        }

        System.out.print(sb);
    }
}