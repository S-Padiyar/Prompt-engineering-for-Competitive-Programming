import java.io.*;
import java.util.*;

public class Main {
    // Fast I/O
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
        FastReader(InputStream in) { br = new BufferedReader(new InputStreamReader(in)); }
        String next() {
            while (st == null || !st.hasMoreTokens()) {
                try { st = new StringTokenizer(br.readLine()); }
                catch (IOException e) { throw new RuntimeException(e); }
            }
            return st.nextToken();
        }
        int nextInt() { return Integer.parseInt(next()); }
    }

    // gcd helper
    static int gcd(int a, int b) {
        while (b != 0) {
            int t = a % b;
            a = b;
            b = t;
        }
        return a;
    }

    // Check if x is 0 or a power of two
    static boolean isPowerOfTwoOrZero(int x) {
        return x == 0 || ((x & (x - 1)) == 0);
    }

    public static void main(String[] args) {
        FastReader in = new FastReader(System.in);
        PrintWriter out = new PrintWriter(System.out);

        int t = in.nextInt();
        while (t-- > 0) {
            int n = in.nextInt();
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = in.nextInt();
            }

            // If n == 1, there's exactly 1 subarray and it's brilliant.
            if (n == 1) {
                out.println(1);
                continue;
            }

            // Build the difference array d[i] = |a[i+1] - a[i]|, length = n-1
            int m = n - 1;
            int[] d = new int[m];
            for (int i = 0; i < m; i++) {
                d[i] = Math.abs(a[i + 1] - a[i]);
            }

            // We'll do the classic "sliding GCD" DP.
            // prevG[k], prevCnt[k] will store the distinct GCD-values
            // and their counts for subarrays ending at index i-1.
            int[] prevG = new int[32], prevCnt = new int[32];
            int prevSize = 0;

            long ans = 0;  // counts all brilliant subarrays of length >= 2

            for (int i = 0; i < m; i++) {
                int val = d[i];
                int[] curG = new int[32], curCnt = new int[32];
                int curSize = 0;

                // Extend every old subarray by d[i]
                for (int j = 0; j < prevSize; j++) {
                    int g = gcd(prevG[j], val);
                    if (curSize > 0 && curG[curSize - 1] == g) {
                        curCnt[curSize - 1] += prevCnt[j];
                    } else {
                        curG[curSize] = g;
                        curCnt[curSize] = prevCnt[j];
                        curSize++;
                    }
                }

                // Also start a brand-new subarray [i, i]
                if (curSize > 0 && curG[curSize - 1] == val) {
                    curCnt[curSize - 1]++;
                } else {
                    curG[curSize] = val;
                    curCnt[curSize] = 1;
                    curSize++;
                }

                // Count how many of these ending-at-i have GCD in {0,1,2,4,8,...}
                for (int j = 0; j < curSize; j++) {
                    if (isPowerOfTwoOrZero(curG[j])) {
                        ans += curCnt[j];
                    }
                }

                // Move cur -> prev for next iteration
                prevSize = curSize;
                System.arraycopy(curG, 0, prevG, 0, curSize);
                System.arraycopy(curCnt,0, prevCnt,0, curSize);
            }

            // Add the n single-element subarrays (all are brilliant).
            ans += n;

            out.println(ans);
        }

        out.flush();
    }
}