import java.io.*;
import java.util.*;

public class Main {
    static final int MAXN = 100000;          // maximum n over all test cases
    static long[] events = new long[2 * MAXN]; // scratch space for sweep events

    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader();
        PrintWriter out = new PrintWriter(System.out);

        int t = in.nextInt();
        while (t-- > 0) {
            int n = in.nextInt();
            long m = in.nextLong();
            int k = in.nextInt();

            long[] h = new long[n];
            long maxH = 0;
            for (int i = 0; i < n; i++) {
                h[i] = in.nextLong();
                if (h[i] > maxH) maxH = h[i];
            }
            long[] x = new long[n];
            for (int i = 0; i < n; i++) {
                x[i] = in.nextLong();
            }

            // Binary search for the minimum T
            long left = 1, right = maxH;
            long answer = -1;
            while (left <= right) {
                long mid = (left + right) >>> 1;
                if (canKillAtLeastK(n, m, k, h, x, mid)) {
                    answer = mid;
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            }
            out.println(answer);
        }

        out.flush();
    }

    /**
     * Check if there is a position p (integer) where at least k enemies
     * can be killed in exactly T hits.
     */
    static boolean canKillAtLeastK(int n, long m, int k,
                                   long[] h, long[] x, long T) {
        int ecount = 0; // will count 2 * (#intervals)

        // Build intervals [Li, Ri] for enemies killable in T hits
        for (int i = 0; i < n; i++) {
            // c = ceil(h[i] / T)
            long c = (h[i] + T - 1) / T;
            long r = m - c;   // must have r >= 0 to be killable
            if (r >= 0) {
                long L = x[i] - r;
                long R = x[i] + r;
                // encode events: at L, +1; at R+1, -1
                // pack into one long: high bits = position<<1, low bit = delta
                // delta bit: 1 for +1, 0 for -1.
                events[ecount++] = (L << 1) | 1;       // +1 event
                events[ecount++] = ((R + 1) << 1) | 0; // -1 event
            }
        }

        // If fewer than k intervals at all, impossible
        if (ecount / 2 < k) return false;

        // Sort all events by position, then by delta (we get -1 before +1 if same pos)
        Arrays.sort(events, 0, ecount);

        // Sweep-line to find max overlap
        int curr = 0;
        for (int i = 0; i < ecount; i++) {
            // decode delta
            // low bit == 1 => +1, else -1
            curr += ((events[i] & 1) == 1 ? +1 : -1);
            if (curr >= k) {
                return true;
            }
        }
        return false;
    }

    // Fast IO
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
        FastReader() {
            br = new BufferedReader(new InputStreamReader(System.in));
        }
        String next() {
            while (st == null || !st.hasMoreTokens()) {
                try { st = new StringTokenizer(br.readLine()); }
                catch(IOException e) { throw new RuntimeException(e); }
            }
            return st.nextToken();
        }
        int nextInt()    { return Integer.parseInt(next()); }
        long nextLong()  { return Long.parseLong(next()); }
    }
}