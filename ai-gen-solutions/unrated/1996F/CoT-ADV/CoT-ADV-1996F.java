import java.io.*;
import java.util.StringTokenizer;

public class Main {
    public static void main(String[] args) throws IOException {
        // Fast input/output
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        int t = Integer.parseInt(br.readLine().trim());

        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            long k = Long.parseLong(st.nextToken());

            long[] a = new long[n], b = new long[n];
            st = new StringTokenizer(br.readLine());
            long maxA = 0;
            for (int i = 0; i < n; i++) {
                a[i] = Long.parseLong(st.nextToken());
                if (a[i] > maxA) maxA = a[i];
            }
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                b[i] = Long.parseLong(st.nextToken());
            }

            // 1) Compute total strictly positive picks: L_i = ceil(a_i/b_i).
            long totalPositivePicks = 0;
            long sumAllPositive = 0;
            for (int i = 0; i < n; i++) {
                long Li = (a[i] + b[i] - 1) / b[i]; // ceil(a_i / b_i)
                totalPositivePicks += Li;
                // Sum of that arithmetic sequence:
                // first = a[i], last = a[i] - (Li-1)*b[i], count = Li
                long lastVal = a[i] - (Li - 1) * b[i];
                sumAllPositive += Li * (a[i] + lastVal) / 2;
            }

            // If k is larger or equal, we take all positive picks + zeros
            if (totalPositivePicks <= k) {
                out.println(sumAllPositive);
                continue;
            }

            // 2) Binary search for threshold v such that C(v) >= k > C(v+1)
            long lo = 1, hi = maxA;
            while (lo < hi) {
                long mid = lo + (hi - lo + 1) / 2;
                if (countAtLeast(mid, a, b) >= k) {
                    lo = mid;
                } else {
                    hi = mid - 1;
                }
            }
            long v = lo;

            // 3) Count how many picks strictly greater than v
            long cntGreater = countAtLeast(v + 1, a, b);

            // 4) Compute sum of all picks > v
            //    For each i, we take c_i = countAtLeast(v+1) from pile i.
            //    Their sum is c_i*a[i] - b[i]*c_i*(c_i-1)/2
            long sumGreater = 0;
            for (int i = 0; i < n; i++) {
                if (a[i] > v) {
                    long ci = (a[i] - (v + 1)) / b[i] + 1;
                    // sum of ci terms: a[i], a[i]-b[i], ..., a[i]-(ci-1)*b[i]
                    sumGreater += ci * a[i] - b[i] * (ci * (ci - 1) / 2);
                }
            }

            // 5) The remaining (k - cntGreater) picks each contribute exactly v
            long remaining = k - cntGreater;
            long answer = sumGreater + remaining * v;

            out.println(answer);
        }
        out.flush();
        out.close();
    }

    /**
     * Count how many terms across all piles are >= x.
     * That is, for each i, if a[i] < x => 0. Else floor((a[i]-x)/b[i]) + 1.
     */
    private static long countAtLeast(long x, long[] a, long[] b) {
        long cnt = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] >= x) {
                cnt += (a[i] - x) / b[i] + 1;
            }
        }
        return cnt;
    }
}