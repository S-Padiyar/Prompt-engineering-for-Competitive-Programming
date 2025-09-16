import java.io.*;
import java.util.*;

public class Main {
    static final int MAXN = 1000000 + 5;
    static int[] head = new int[MAXN];
    static int[] to   = new int[MAXN];
    static int[] nxt  = new int[MAXN];
    static int   edgeCnt;
    static int[] F    = new int[MAXN];

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out  = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
        int t = Integer.parseInt(in.readLine().trim());
        
        while (t-- > 0) {
            int n = Integer.parseInt(in.readLine().trim());
            // Clear adjacency
            for (int i = 1; i <= n; i++) head[i] = 0;
            edgeCnt = 0;

            StringTokenizer st = new StringTokenizer(in.readLine());
            for (int i = 2; i <= n; i++) {
                int p = Integer.parseInt(st.nextToken());
                // add edge p -> i
                edgeCnt++;
                to[edgeCnt]  = i;
                nxt[edgeCnt] = head[p];
                head[p]      = edgeCnt;
            }

            // Process nodes in descending order (post-order)
            for (int v = n; v >= 1; v--) {
                int e = head[v];
                if (e == 0) {
                    // no children
                    F[v] = 0;
                    continue;
                }
                // gather children D_i:
                int M = 0;
                // first pass: find max
                for (int ee = e; ee != 0; ee = nxt[ee]) {
                    int c = to[ee];
                    if (F[c] > M) M = F[c];
                }
                // second pass: sum 2^(D_i - M)
                double sum = 0.0;
                for (int ee = e; ee != 0; ee = nxt[ee]) {
                    int c = to[ee];
                    // exactly 2^(F[c] - M):
                    sum += Math.scalb(1.0, F[c] - M);
                }
                // now log2(sum), careful with precision
                double log2sum = Math.log(sum) / Math.log(2.0);
                // we subtract a tiny epsilon to protect against  rounding upward
                int  tceil   = (int)Math.ceil(log2sum - 1e-12);
                int  h1      = M + tceil;    // from Kraft‐sum
                int  h2      = M + 1;         // from child‐depth
                F[v]         = Math.max(h1, h2);
            }

            // answer is F[1]
            out.println(F[1]);
        }

        out.flush();
    }
}