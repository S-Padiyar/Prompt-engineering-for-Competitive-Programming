import java.io.*;
import java.util.StringTokenizer;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        int t = Integer.parseInt(br.readLine().trim());

        while (t-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            long k = Long.parseLong(st.nextToken());

            long[] a = new long[n], b = new long[n];
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                b[i] = Long.parseLong(st.nextToken());
            }

            // Helper to count how many terms >= x across all sequences.
            // We cap the count at k, since we only care if it's >= k.
            java.util.function.LongUnaryOperator countAtLeast = (long x) -> {
                long total = 0;
                for (int i = 0; i < n; i++) {
                    if (a[i] >= x) {
                        long cnt = (a[i] - x) / b[i] + 1;
                        total += cnt;
                        if (total >= k) {
                            // no need to count more precisely
                            return k;
                        }
                    }
                }
                return total;
            };

            // If even the positive terms are fewer than k, sum all positives.
            long posCount = countAtLeast.applyAsLong(1L);
            if (posCount < k) {
                // Sum all positive terms:
                // for each i, number of positive terms = c = (a[i]-1)/b[i] + 1
                // sum of that AP = c*a[i] - b[i]*c*(c-1)/2
                long ans = 0;
                for (int i = 0; i < n; i++) {
                    long c = (a[i] >= 1) ? ((a[i] - 1) / b[i] + 1) : 0;
                    if (c > 0) {
                        ans += c * a[i] - b[i] * (c * (c - 1) / 2);
                    }
                }
                System.out.println(ans);
                continue;
            }

            // Binary search for the largest T with C(T) >= k
            long low = 1, high = 0;
            for (long v : a) {
                if (v > high) high = v;
            }

            while (low < high) {
                long mid = (low + high + 1) >>> 1;
                if (countAtLeast.applyAsLong(mid) >= k) {
                    low = mid;
                } else {
                    high = mid - 1;
                }
            }
            long T = low;

            // Now compute how many are strictly greater than T (i.e. >= T+1)
            // and sum those terms.
            long totalGreater = 0;
            long sumGreater = 0;
            for (int i = 0; i < n; i++) {
                if (a[i] >= T + 1) {
                    long c = (a[i] - (T + 1)) / b[i] + 1;
                    totalGreater += c;
                    // sum of c terms: a + (a - b) + ... + (a - (c-1)b)
                    // = c*a - b*(c*(c-1)/2)
                    sumGreater += c * a[i] - b[i] * (c * (c - 1) / 2);
                }
            }

            // We still need (k - totalGreater) terms that are exactly == T
            long needT = k - totalGreater;
            long answer = sumGreater + needT * T;

            System.out.println(answer);
        }
    }
}