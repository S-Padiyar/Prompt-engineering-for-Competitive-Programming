import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter    out = new PrintWriter(System.out);
        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            long n = Long.parseLong(st.nextToken());
            long m = Long.parseLong(st.nextToken());
            int  k = Integer.parseInt(st.nextToken());

            // Read fountains
            int[] R = new int[k], C = new int[k];
            for (int i = 0; i < k; i++) {
                st = new StringTokenizer(br.readLine());
                R[i] = Integer.parseInt(st.nextToken());
                C[i] = Integer.parseInt(st.nextToken());
            }

            // 1) Group by column: track max-r and count of that max
            HashMap<Integer,int[]> colData = new HashMap<>();
            // colData.get(c) = { maxR, countOfMaxR }
            for (int i = 0; i < k; i++) {
                int c = C[i], r = R[i];
                int[] dat = colData.get(c);
                if (dat == null) {
                    dat = new int[]{r, 1};
                    colData.put(c, dat);
                } else {
                    if (r > dat[0]) {
                        dat[0] = r;
                        dat[1] = 1;
                    } else if (r == dat[0]) {
                        dat[1]++;
                    }
                }
            }

            // 2) Extract and sort distinct columns
            int   tcols = colData.size();
            int[] cols  = new int[tcols];
            {
                int idx = 0;
                for (int c : colData.keySet()) cols[idx++] = c;
                Arrays.sort(cols);
            }

            // 3) Build z[i] = maxR at cols[i], and prefix maxima M[i]
            long[] z = new long[tcols];
            long[] M = new long[tcols];
            for (int i = 0; i < tcols; i++) {
                z[i] = colData.get(cols[i])[0];
                if (i == 0) M[i] = z[i];
                else      M[i] = Math.max(M[i-1], z[i]);
            }

            // 4) Compute sum_y = sum_j y_j; length of each segment times M[i]
            //    segments are [c[i]..c[i+1]-1] all have y=M[i], and
            //    from c[tcols-1]..m are M[tcols-1].
            long sumY = 0;
            for (int i = 0; i < tcols; i++) {
                long len;
                if (i < tcols - 1) {
                    len = (long)cols[i+1] - cols[i];
                } else {
                    len = m - cols[i] + 1;
                }
                sumY += len * M[i];
            }

            // 5) Alpha = total cells - sumY
            long total = n * m;
            long alpha = total - sumY;
            out.println(alpha);

            // 6) Precompute a map from column to its index i
            HashMap<Integer,Integer> colIndex = new HashMap<>();
            for (int i = 0; i < tcols; i++) {
                colIndex.put(cols[i], i);
            }

            // 7) For each fountain, decide if removing it would lower sumY
            //    => raises alpha => print 1.
            //    Conditions:
            //    (a) its row R[i] == z[j]
            //    (b) that z[j] was unique in that column
            //    (c) z[j] > M[j-1]  (with M[-1]=0)
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < k; i++) {
                int c     = C[i];
                int r     = R[i];
                int j     = colIndex.get(c);
                int[] dat = colData.get(c);
                long zj   = dat[0];    // the max-row in that column
                int  cnt  = dat[1];    // how many fountains share that max

                boolean inc = false;
                if (r == zj && cnt == 1) {
                    long prevMax = (j == 0 ? 0 : M[j-1]);
                    if (zj > prevMax) {
                        inc = true;
                    }
                }
                sb.append(inc ? '1' : '0');
                if (i+1 < k) sb.append(' ');
            }
            out.println(sb.toString());
        }
        out.flush();
    }
}