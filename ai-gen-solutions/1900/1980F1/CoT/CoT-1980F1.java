import java.io.*;
import java.util.*;

public class Main {
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
        long nextLong() { return Long.parseLong(next()); }
    }

    public static void main(String[] args) {
        FastReader in = new FastReader(System.in);
        int t = in.nextInt();
        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            long n = in.nextLong();
            long m = in.nextLong();
            int k = in.nextInt();

            // Read fountains: we store (column x, reindexed row y, original index)
            // reindex row so that bottom row is y=1, top row is y=n:
            //    if original row is r (1=top, n=bottom), then y = n - r + 1.
            int[] X = new int[k];
            long[] Y = new long[k];
            for (int i = 0; i < k; i++) {
                long r = in.nextLong();
                long c = in.nextLong();
                X[i] = (int) c;
                Y[i] = n - r + 1;
            }

            // For each column, pick the fountain with minimal Y.
            // We'll sort (x, y, i) by x then by y, and scan.
            Integer[] idx = new Integer[k];
            for (int i = 0; i < k; i++) idx[i] = i;
            Arrays.sort(idx, (a, b) -> {
                if (X[a] != X[b]) return Integer.compare(X[a], X[b]);
                return Long.compare(Y[a], Y[b]);
            });

            // Build list of (x, yMin, id) for each column's minimal-y fountain:
            List<int[]> events = new ArrayList<>();
            int lastX = -1;
            for (int ii = 0; ii < k; ii++) {
                int i = idx[ii];
                if (X[i] != lastX) {
                    lastX = X[i];
                    // store (column x, yMin (fits in int?), originalIndex)
                    events.add(new int[]{X[i], (int)Y[i], i});
                }
            }

            // Now sweep these events in ascending x, keeping curY and accumulating area.
            long alpha = 0;
            long curY = n + 1;       // the running global minimum + 1
            long prevX = 1;          // next column to fill in from
            int[] ans = new int[k];  // default 0;  will set 1 for 'record' fountains

            for (int[] ev : events) {
                long x = ev[0];
                long yMin = ev[1];
                int id = ev[2];

                // First, columns [prevX .. x-1] have no new constraint:
                long w = x - prevX;
                if (w > 0) {
                    alpha += w * (curY - 1);
                }

                // Now at column x itself we see a fountain constraint yMin.
                // Update curY = min(curY, yMin).  If strictly smaller, mark this fountain.
                if (yMin < curY) {
                    curY = yMin;
                    ans[id] = 1;  // removing this fountain strictly increases alpha
                }
                // Alice can take (curY - 1) cells in column x:
                alpha += (curY - 1);

                prevX = x + 1;
            }
            // Finally, columns [prevX .. m]:
            if (prevX <= m) {
                alpha += (m - prevX + 1) * (curY - 1);
            }

            // Output
            sb.append(alpha).append('\n');
            for (int i = 0; i < k; i++) {
                sb.append(ans[i]).append(i + 1 < k ? ' ' : '\n');
            }
        }

        System.out.print(sb.toString());
    }
}