import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader();
        PrintWriter out = new PrintWriter(System.out);

        int t = in.nextInt();
        while (t-- > 0) {
            int n = in.nextInt();
            int q = in.nextInt();
            long[] a = new long[n];
            for (int i = 0; i < n; i++) {
                a[i] = in.nextLong();
            }
            // Build prefix sums of a: psa[i] = sum of a[0..i-1]
            long[] psa = new long[n + 1];
            for (int i = 1; i <= n; i++) {
                psa[i] = psa[i - 1] + a[i - 1];
            }
            long S = psa[n];  // total sum of a

            // Helper to get sum of a segment of length len in the k-th cyclic shift, 
            // from block-position lpos..rpos (1-based).
            // We convert that to at most two contiguous segments of a[].
            // k is 1-based (which cyclic shift).
            class SumBlock {
                long get(int k, int lpos, int rpos) {
                    int len = rpos - lpos + 1;
                    // 0-based index in a where this segment starts:
                    int startIdx = ( (k - 1) + (lpos - 1) ) % n;
                    if (startIdx + len <= n) {
                        // no wrap
                        return psa[startIdx + len] - psa[startIdx];
                    } else {
                        // wraps around the end of a[]
                        int firstPart = n - startIdx;
                        long sum1 = psa[n] - psa[startIdx];
                        int rem = len - firstPart;
                        long sum2 = psa[rem]; 
                        return sum1 + sum2;
                    }
                }
            }
            SumBlock sb = new SumBlock();

            while (q-- > 0) {
                long l = in.nextLong();
                long r = in.nextLong();
                // which block contains position l?
                long blockL = (l - 1) / n + 1;
                int posL = (int)((l - 1) % n) + 1;
                long blockR = (r - 1) / n + 1;
                int posR = (int)((r - 1) % n) + 1;

                long ans;
                if (blockL == blockR) {
                    // Entire query inside one cyclic shift
                    ans = sb.get((int)blockL, posL, posR);
                } else {
                    // suffix of blockL
                    long part1 = sb.get((int)blockL, posL, n);
                    // prefix of blockR
                    long part2 = sb.get((int)blockR, 1, posR);
                    // fully covered blocks in between
                    long fullBlocks = blockR - blockL - 1;
                    long partMid = fullBlocks > 0 ? fullBlocks * S : 0;
                    ans = part1 + partMid + part2;
                }
                out.println(ans);
            }
        }

        out.flush();
        out.close();
    }

    // Fast I/O
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
        int nextInt() throws IOException { return Integer.parseInt(next()); }
        long nextLong() throws IOException { return Long.parseLong(next()); }
    }
}