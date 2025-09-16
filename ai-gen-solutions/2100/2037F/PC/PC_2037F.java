import java.io.*;
import java.util.*;

public class Main {
    // Fast input reader
    static class FastInput {
        BufferedInputStream in = new BufferedInputStream(System.in);
        byte[] buf = new byte[1 << 15];
        int len, pos;

        int read() throws IOException {
            if (len == pos) {
                len = in.read(buf);
                pos = 0;
                if (len == -1) return -1;
            }
            return buf[pos++];
        }

        int nextInt() throws IOException {
            int c, x = 0;
            do {
                c = read();
                if (c == -1) return -1;
            } while (c <= ' ');
            boolean neg = (c == '-');
            if (neg) c = read();
            for (; c >= '0' && c <= '9'; c = read()) {
                x = x * 10 + (c - '0');
            }
            return neg ? -x : x;
        }

        long nextLong() throws IOException {
            int c;
            long x = 0;
            do {
                c = read();
                if (c == -1) return -1;
            } while (c <= ' ');
            boolean neg = (c == '-');
            if (neg) c = read();
            for (; c >= '0' && c <= '9'; c = read()) {
                x = x * 10 + (c - '0');
            }
            return neg ? -x : x;
        }
    }

    static int n, k;
    static long m;
    static long[] h, x;

    // Check if with t attacks we can kill at least k enemies
    static boolean feasible(long t) {
        int cnt = 0;
        int[] start = new int[n], end = new int[n];

        for (int i = 0; i < n; i++) {
            long need = (h[i] + t - 1) / t;  // ceil(h[i]/t)
            if (need > m) continue;          // can't kill this one
            long w = m - need;
            int L = (int)(x[i] - w);
            int R = (int)(x[i] + w);
            start[cnt] = L;
            end[cnt]   = R + 1;  // end event at R+1
            cnt++;
        }

        if (cnt < k) return false;

        Arrays.sort(start, 0, cnt);
        Arrays.sort(end,   0, cnt);

        int iStart = 0, iEnd = 0, covered = 0;
        // Sweep through events
        while (iStart < cnt) {
            if (iEnd == cnt || start[iStart] < end[iEnd]) {
                // a new interval starts
                covered++;
                if (covered >= k) return true;
                iStart++;
            } else {
                // an interval ends
                covered--;
                iEnd++;
            }
        }

        return false;
    }

    public static void main(String[] args) throws IOException {
        FastInput in = new FastInput();
        PrintWriter out = new PrintWriter(System.out);

        int tcs = in.nextInt();
        while (tcs-- > 0) {
            n = in.nextInt();
            m = in.nextLong();
            k = in.nextInt();

            h = new long[n];
            x = new long[n];
            long maxH = 0;
            for (int i = 0; i < n; i++) {
                h[i] = in.nextLong();
                maxH = Math.max(maxH, h[i]);
            }
            for (int i = 0; i < n; i++) {
                x[i] = in.nextLong();
            }

            // Binary search on the number of attacks
            long left = 1, right = maxH, ans = -1;
            while (left <= right) {
                long mid = (left + right) >>> 1;
                if (feasible(mid)) {
                    ans = mid;
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            }

            out.println(ans);
        }

        out.flush();
    }
}