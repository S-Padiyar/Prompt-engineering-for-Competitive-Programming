import java.io.*;
import java.util.*;

public class Main {
    static class Rect implements Comparable<Rect> {
        int c, r, id;
        Rect(int _c, int _r, int _id) {
            c = _c; r = _r; id = _id;
        }
        public int compareTo(Rect o) {
            return Integer.compare(this.c, o.c);
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter pw = new PrintWriter(new BufferedOutputStream(System.out));
        int t = Integer.parseInt(br.readLine().trim());

        StringTokenizer tok;
        while (t-- > 0) {
            tok = new StringTokenizer(br.readLine());
            long n = Long.parseLong(tok.nextToken());
            long m = Long.parseLong(tok.nextToken());
            int k = Integer.parseInt(tok.nextToken());

            // Read fountains
            Rect[] R = new Rect[k + 1];
            for (int i = 0; i < k; i++) {
                tok = new StringTokenizer(br.readLine());
                int r = Integer.parseInt(tok.nextToken());
                int c = Integer.parseInt(tok.nextToken());
                R[i] = new Rect(c, r, i);
            }
            // Add the "fake" rectangle for (1,m) -> rows [1..1], cols [m..m]
            R[k] = new Rect((int)m, 1, k);

            // Sort by left-edge c
            Arrays.sort(R);

            // We'll sweep the distinct c-values, maintain top two r's
            long S = 0;             // total union area
            long[] a = new long[k+1];  // exclusive contributions
            long top1 = 0, top2 = 0;   // top1 >= top2
            int cntTop1 = 0;          // how many rectangles have r == top1
            int idxTop1 = -1;         // if cntTop1==1, the ID of that rect

            int p = 0, N = k+1;
            while (p < N) {
                int cval = R[p].c;
                // absorb all rectangles starting at c = cval
                while (p < N && R[p].c == cval) {
                    long rr = R[p].r;
                    int id = R[p].id;
                    if (rr > top1) {
                        // new maximum
                        top2 = top1;
                        top1 = rr;
                        cntTop1 = 1;
                        idxTop1 = id;
                    } else if (rr == top1) {
                        cntTop1++;
                    } else if (rr > top2) {
                        top2 = rr;
                    }
                    p++;
                }
                // the next distinct c is at R[p].c (if p<N), otherwise infinite
                long len = (p < N
                    ? (long)R[p].c - cval
                    : 1L);  // last segment is width 1

                // area contribution for this segment
                S += len * top1;

                // if exactly one rect attains top1, it has exclusive height segment
                if (cntTop1 == 1) {
                    // after removing that rectangle, height would fall to top2
                    a[idxTop1] += len * (top1 - top2);
                }
            }

            long alpha = n * m - S;
            pw.println(alpha);
            // Output a[0..k-1]
            for (int i = 0; i < k; i++) {
                pw.print(a[i]);
                if (i+1 < k) pw.print(' ');
            }
            pw.println();
        }

        pw.flush();
    }
}