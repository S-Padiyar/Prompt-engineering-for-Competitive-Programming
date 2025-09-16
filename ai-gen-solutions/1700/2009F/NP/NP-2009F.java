import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();

        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int q = Integer.parseInt(st.nextToken());

            // Read array a[0..n-1]
            st = new StringTokenizer(br.readLine());
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            // Build prefix sums: pre[i] = sum of a[0..i-1], pre[0]=0
            long[] pre = new long[n+1];
            for (int i = 0; i < n; i++) {
                pre[i+1] = pre[i] + a[i];
            }
            long sumA = pre[n];  // total sum of one block (one copy of a)

            // Process queries
            while (q-- > 0) {
                // Read 1-based l,r ; convert to 0-based
                st = new StringTokenizer(br.readLine());
                long l = Long.parseLong(st.nextToken()) - 1;
                long r = Long.parseLong(st.nextToken()) - 1;

                // Which block and which offset inside that block?
                int lb = (int)(l / n), lo = (int)(l % n);
                int rb = (int)(r / n), ro = (int)(r % n);

                long answer = 0;
                
                // Case 1: both endpoints in the same block
                if (lb == rb) {
                    int len = ro - lo + 1;
                    answer = partialSumInBlock(pre, n, lb, lo, len);
                } else {
                    // Left partial block
                    int lenL = n - lo;
                    answer += partialSumInBlock(pre, n, lb, lo, lenL);

                    // Full blocks in between
                    long full = rb - lb - 1;
                    if (full > 0) {
                        answer += full * sumA;
                    }

                    // Right partial block
                    int lenR = ro + 1;
                    answer += partialSumInBlock(pre, n, rb, 0, lenR);
                }

                sb.append(answer).append('\n');
            }
        }

        System.out.print(sb);
    }

    /**
     * Compute the sum of 'len' elements inside the block whose
     * cyclic‐shift start index is 'shift'
     * (0-based), beginning at offset 'off' (0-based) within that block.
     *
     * pre is the prefix‐sum array of the original a[0..n-1]:
     *   pre[k] = a[0] + a[1] + ... + a[k-1].
     *
     * We map the block's positions into the cyclic a[] and sum,
     * handling wrap neatly.
     */
    private static long partialSumInBlock(long[] pre, int n,
                                          int shift, int off, int len) {
        // Index in a[] where we start extracting
        int start = shift + off;
        if (start >= n) {
            start -= n;  // equivalent to (start % n) but start < 2n
        }

        // If we don't wrap around the end of a[]
        if (start + len <= n) {
            return pre[start + len] - pre[start];
        } else {
            // We wrap around: first chunk to end of a[], then from a[0]
            long part1 = pre[n] - pre[start];
            int rem = (start + len) - n;
            long part2 = pre[rem];  // pre[rem] = sum of a[0..rem-1]
            return part1 + part2;
        }
    }
}