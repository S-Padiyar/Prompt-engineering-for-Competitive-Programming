import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static StringTokenizer st;

    // Fast read of next token
    static String next() throws IOException {
        while (st == null || !st.hasMoreTokens()) {
            String line = br.readLine();
            if (line == null) return null;
            st = new StringTokenizer(line);
        }
        return st.nextToken();
    }

    static int nextInt() throws IOException {
        return Integer.parseInt(next());
    }

    // The "possible" check: can we kill >= k enemies in T attacks?
    // We build intervals [L_i, R_i] = [x_i - w_i, x_i + w_i], w_i = m - ceil(h_i/T),
    // keep only those with w_i >= 0, and see if some integer point is
    // covered by at least k intervals via a heap-based sweep.
    static boolean possible(int T, int n, int m, int k,
                            int[] h, int[] x,
                            long[] packed, int[] heap) {
        int tsize = 0;
        // Build intervals
        for (int i = 0; i < n; i++) {
            // ceil(h[i]/T)
            int need = (h[i] + T - 1) / T;
            int w = m - need;
            if (w < 0) continue;          // can't damage this enemy enough
            int L = x[i] - w;
            int R = x[i] + w;
            // pack L in high 32 bits (signed), R in low 32 bits
            packed[tsize++] = ((long) L << 32) | (R & 0xffffffffL);
        }
        if (tsize < k) return false;

        // Sort intervals by L ascending
        Arrays.sort(packed, 0, tsize);

        // We'll do a min-heap of R's in a plain array
        int heapSize = 0;

        for (int i = 0; i < tsize; i++) {
            long p = packed[i];
            int L = (int) (p >> 32);  // top 32 bits, sign-extended
            int R = (int) p;          // low 32 bits

            // Remove from heap all intervals whose R < L
            while (heapSize > 0 && heap[0] < L) {
                // pop min
                heapSize--;
                heap[0] = heap[heapSize];
                // heapify down
                int idx = 0;
                while (true) {
                    int left = 2 * idx + 1;
                    if (left >= heapSize) break;
                    int right = left + 1;
                    int smallChild = left;
                    if (right < heapSize && heap[right] < heap[left])
                        smallChild = right;
                    if (heap[smallChild] < heap[idx]) {
                        int tmp = heap[idx];
                        heap[idx] = heap[smallChild];
                        heap[smallChild] = tmp;
                        idx = smallChild;
                    } else {
                        break;
                    }
                }
            }

            // Insert R into heap
            int pos = heapSize++;
            heap[pos] = R;
            // heapify up
            while (pos > 0) {
                int par = (pos - 1) >>> 1;
                if (heap[par] <= heap[pos]) break;
                int tmp = heap[par];
                heap[par] = heap[pos];
                heap[pos] = tmp;
                pos = par;
            }

            // If we now have at least k intervals covering L, success
            if (heapSize >= k) return true;
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        int Tcases = nextInt();
        StringBuilder out = new StringBuilder();

        while (Tcases-- > 0) {
            int n = nextInt();
            int m = nextInt();
            int k = nextInt();

            int[] h = new int[n];
            int[] x = new int[n];
            int maxH = 0;

            for (int i = 0; i < n; i++) {
                h[i] = nextInt();
                if (h[i] > maxH) maxH = h[i];
            }
            for (int i = 0; i < n; i++) {
                x[i] = nextInt();
            }

            // Pre-allocate helper arrays
            long[] packed = new long[n];
            int[] heap = new int[n];

            // Quick check: if even with T = maxH we can't get k intervals covering some point,
            // then it's impossible in <anything>.
            if (!possible(maxH, n, m, k, h, x, packed, heap)) {
                out.append(-1).append('\n');
                continue;
            }

            // Binary search for the minimal T in [1..maxH] that is possible.
            int lo = 1, hi = maxH;
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                if (possible(mid, n, m, k, h, x, packed, heap)) {
                    hi = mid;
                } else {
                    lo = mid + 1;
                }
            }
            out.append(lo).append('\n');
        }

        System.out.print(out);
    }
}