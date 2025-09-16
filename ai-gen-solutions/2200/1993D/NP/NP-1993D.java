import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader();
        PrintWriter out = new PrintWriter(System.out);

        int t = in.nextInt();
        while (t-- > 0) {
            int n = in.nextInt();
            int k = in.nextInt();
            int[] a = new int[n];
            int maxv = 0;
            for (int i = 0; i < n; i++) {
                a[i] = in.nextInt();
                if (a[i] > maxv) maxv = a[i];
            }
            // Compute final length r:
            int r = n % k;
            if (r == 0) r = k;
            // Number of survivors we must have >= X to force median>=X:
            // median index = floor((r+1)/2), 
            // so need (r - medianIndex + 1) survivors >= X.
            int medianIndex = (r + 1) / 2;           // floor((r+1)/2)
            int need = r - medianIndex + 1;

            // Binary search on X in [1..maxv].
            int lo = 1, hi = maxv;
            while (lo < hi) {
                int mid = lo + (hi - lo + 1) / 2;
                if (canHaveAtLeast(a, n, k, r, need, mid)) {
                    lo = mid;
                } else {
                    hi = mid - 1;
                }
            }

            out.println(lo);
        }

        out.flush();
    }

    /**
     * Check whether we can achieve median >= X by seeing
     * if at least 'need' of the r residue-classes
     * each contain some a[i] >= X.
     */
    static boolean canHaveAtLeast(int[] a, int n, int k, int r, int need, int X) {
        int countGood = 0;
        // We only care about the first r residue-classes: j=0..r-1.
        // In each, look at positions j, j+k, j+2k, ...
        for (int j = 0; j < r; j++) {
            boolean good = false;
            for (int pos = j; pos < n; pos += k) {
                if (a[pos] >= X) {
                    good = true;
                    break;
                }
            }
            if (good) {
                if (++countGood >= need) {
                    return true;
                }
            }
        }
        return false;
    }

    /** fast I/O **/
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
        FastReader() {
            br = new BufferedReader(new InputStreamReader(System.in));
        }
        String next() throws IOException {
            while (st == null || !st.hasMoreTokens()) {
                String s = br.readLine();
                if (s == null) return null;
                st = new StringTokenizer(s);
            }
            return st.nextToken();
        }
        int nextInt() throws IOException {
            return Integer.parseInt(next());
        }
    }
}