import java.io.*;
import java.util.StringTokenizer;

public class Main {
    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader();
        PrintWriter out = new PrintWriter(System.out);

        int t = in.nextInt();
        while (t-- > 0) {
            int n = in.nextInt();
            long k = in.nextLong();
            long[] a = new long[n];
            long[] b = new long[n];
            long maxA = 0;
            for (int i = 0; i < n; i++) {
                a[i] = in.nextLong();
                maxA = Math.max(maxA, a[i]);
            }
            for (int i = 0; i < n; i++) {
                b[i] = in.nextLong();
            }

            // 1) Compute total number of positive terms sum L_i
            long totalTerms = 0;
            for (int i = 0; i < n; i++) {
                // L_i = ceil(a[i]/b[i])
                long Li = (a[i] + b[i] - 1) / b[i];
                totalTerms += Li;
                if (totalTerms > k) {
                    // We only care if it exceeds k or not
                    break;
                }
            }

            // Case A: sum L_i <= k  --> Just take all positive terms
            if (totalTerms <= k) {
                long ans = 0;
                for (int i = 0; i < n; i++) {
                    long Li = (a[i] + b[i] - 1) / b[i];
                    // Sum of arithmetic sequence:
                    // Li*a[i] - b[i]*(Li*(Li-1)/2)
                    long sumSeq = Li * a[i] - b[i] * (Li * (Li - 1) / 2);
                    ans += sumSeq;
                }
                out.println(ans);
                continue;
            }

            // Case B: sum L_i > k  --> We must pick exactly k of the top values
            // Binary search threshold X
            long lo = 1, hi = maxA, best = 1;
            while (lo <= hi) {
                long mid = (lo + hi) >>> 1;
                // Count how many terms >= mid
                long cnt = 0;
                for (int i = 0; i < n; i++) {
                    if (a[i] >= mid) {
                        cnt += ( (a[i] - mid) / b[i] ) + 1;
                        if (cnt >= k) break;  // no need to count more
                    }
                }
                if (cnt >= k) {
                    best = mid;    // we can afford to raise the threshold
                    lo = mid + 1;
                } else {
                    hi = mid - 1;
                }
            }

            long X = best;  // final threshold

            // Count how many are strictly > X, and sum them
            long countGT = 0;    // how many terms > X
            long sumGT   = 0;    // sum of those terms
            for (int i = 0; i < n; i++) {
                if (a[i] >= X + 1) {
                    long t = (a[i] - (X + 1)) / b[i] + 1;  // number of terms >= X+1
                    countGT += t;
                    // sum of the first t terms: a[i] + (a[i]-b[i]) + ... + (a[i]-(t-1)*b[i])
                    // = t*a[i] - b[i] * (t*(t-1)/2)
                    long sumSeq = t * a[i] - b[i] * ( t * (t - 1) / 2 );
                    sumGT += sumSeq;
                }
            }

            // We still need (k - countGT) more picks, all worth exactly X
            long rem = k - countGT;
            long answer = sumGT + rem * X;

            out.println(answer);
        }
        out.flush();
    }

    // Fast IO template
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
        FastReader() {
            br = new BufferedReader(new InputStreamReader(System.in));
        }
        String next() throws IOException {
            while (st == null || !st.hasMoreTokens()) {
                st = new StringTokenizer(br.readLine());
            }
            return st.nextToken();
        }
        int nextInt() throws IOException { return Integer.parseInt(next()); }
        long nextLong() throws IOException { return Long.parseLong(next()); }
    }
}