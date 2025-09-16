import java.io.*;
import java.util.*;

public class Main {
    static class Fountain {
        int r, c, idx;
        Fountain(int r, int c, int idx) {
            this.r = r; this.c = c; this.idx = idx;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(in.readLine());
        int t = Integer.parseInt(st.nextToken());

        StringBuilder out = new StringBuilder();
        while (t-- > 0) {
            st = new StringTokenizer(in.readLine());
            long n = Long.parseLong(st.nextToken());
            long m = Long.parseLong(st.nextToken());
            int k = Integer.parseInt(st.nextToken());

            // Read the fountains
            Fountain[] F = new Fountain[k];
            for (int i = 0; i < k; i++) {
                st = new StringTokenizer(in.readLine());
                int r = Integer.parseInt(st.nextToken());
                int c = Integer.parseInt(st.nextToken());
                F[i] = new Fountain(r, c, i);
            }

            // Sort by column asc, then row desc
            Arrays.sort(F, (a,b) -> {
                if (a.c != b.c) return Integer.compare(a.c, b.c);
                return -Integer.compare(a.r, b.r);
            });

            // Group by column: pick the single top fountain in each column
            ArrayList<int[]> cols = new ArrayList<>();
            // each entry is { col, r_max, idx_of_that_max }
            for (int i = 0; i < k; ) {
                int c = F[i].c;
                int rMax = F[i].r;
                int idxMax = F[i].idx;
                int j = i + 1;
                // skip any others in same column
                while (j < k && F[j].c == c) j++;
                cols.add(new int[]{ c, rMax, idxMax });
                i = j;
            }

            int u = cols.size();
            long[] prevF = new long[u], fVal = new long[u];
            long cur = 1L;

            // Compute the non‐decreasing f(c_j) = max so far of L(c_j)=r_max+1
            for (int j = 0; j < u; j++) {
                prevF[j] = cur;
                long L = (long)cols.get(j)[1] + 1L;
                if (L > cur) cur = L;
                fVal[j] = cur;
            }

            // Compute Bob's area B = sum_{columns} (f(c)-1).
            // We only need to do it in O(u) by noticing f(c) is constant
            // between c_j..(c_{j+1}-1).
            long B = 0;
            for (int j = 0; j < u; j++) {
                long c_j = cols.get(j)[0];
                long nextC = (j+1 < u ? cols.get(j+1)[0] : m+1L);
                long len = nextC - c_j;
                B += (fVal[j] - 1L) * len;
            }

            // Alice's max area:
            long alpha = n*m - B;
            out.append(alpha).append('\n');

            // Which fountains are "critical"?  Only the unique max‐of‐column
            // for which L(c_j)=r_max+1 > prevF[j].
            int[] ans = new int[k];
            for (int j = 0; j < u; j++) {
                long L = (long)cols.get(j)[1] + 1L;
                if (L > prevF[j]) {
                    int idxMax = cols.get(j)[2];
                    ans[idxMax] = 1;
                }
            }

            // Print the k answers in the original order
            for (int i = 0; i < k; i++) {
                out.append(ans[i]).append(i+1<k ? ' ' : '\n');
            }
        }
        System.out.print(out);
    }
}