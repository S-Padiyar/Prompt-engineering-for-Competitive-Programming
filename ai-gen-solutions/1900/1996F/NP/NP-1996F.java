import java.io.*;
import java.util.StringTokenizer;

public class Main {
    public static void main(String[] args) throws IOException {
        // Fast I/O
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        int t = Integer.parseInt(br.readLine().trim());

        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            long k = Long.parseLong(st.nextToken());

            long[] a = new long[n];
            long[] b = new long[n];
            long maxA = 0;

            // Read array a
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Long.parseLong(st.nextToken());
                if (a[i] > maxA) {
                    maxA = a[i];
                }
            }
            // Read array b
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                b[i] = Long.parseLong(st.nextToken());
            }

            // First compute how many strictly positive terms there are in all sequences
            long totalPositive = 0;
            for (int i = 0; i < n; i++) {
                // number of j >= 0 with a[i] - j*b[i] >= 1
                // => j <= (a[i]-1)/b[i]
                long count = (a[i] >= 1 ? ( (a[i]-1) / b[i] ) + 1 : 0);
                totalPositive += count;
                if (totalPositive > k) {
                    // no need to exceed k
                    totalPositive = k + 1;
                    break;
                }
            }

            // If there are fewer than k positive terms, just sum them all
            if (totalPositive <= k) {
                // Sum every positive term
                long answer = 0;
                for (int i = 0; i < n; i++) {
                    long tCount = (a[i] >= 1 ? ((a[i]-1)/b[i]) + 1 : 0);
                    // sum of arithmetic sequence: a[i] + (a[i]-b[i]) + ... up to tCount terms
                    // = tCount*a[i] - b[i]*(tCount*(tCount-1)/2)
                    if (tCount > 0) {
                        answer += tCount * a[i]
                                - b[i] * (tCount * (tCount - 1) / 2);
                    }
                }
                sb.append(answer).append('\n');
                continue;
            }

            // Otherwise we do a binary search for the threshold X in [1..maxA]
            long low = 1, high = maxA, X = 1;
            while (low <= high) {
                long mid = (low + high) >>> 1;
                if (countAtLeast(a, b, mid, k) >= k) {
                    // mid is feasible, try a larger threshold
                    X = mid;
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            }

            // We found the largest X such that C(X) >= k.
            // Next compute M = C(X+1), the number of terms strictly greater than X,
            // and sumGt = sum of all those terms > X.
            long M = 0;
            long sumGt = 0;
            for (int i = 0; i < n; i++) {
                if (a[i] > X) {
                    long cnt = (a[i] - (X + 1)) / b[i] + 1;  // #terms > X
                    M += cnt;
                    // sum of the first cnt terms:  a[i] + (a[i]-b[i]) + ...  
                    // = cnt*a[i] - b[i]*(cnt*(cnt-1)/2)
                    sumGt += cnt * a[i] - b[i] * (cnt * (cnt - 1) / 2);
                }
            }

            // We need (k - M) more picks, each of value exactly X
            long answer = sumGt + (k - M) * X;
            sb.append(answer).append('\n');
        }

        // Output
        System.out.print(sb.toString());
    }

    /**
     * Count how many terms across all sequences are >= x,
     * stopping early if we exceed the limit 'cap'.
     */
    private static long countAtLeast(long[] a, long[] b, long x, long cap) {
        long total = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] >= x) {
                long cnt = (a[i] - x) / b[i] + 1;
                total += cnt;
                if (total > cap) {
                    return cap + 1;
                }
            }
        }
        return total;
    }
}