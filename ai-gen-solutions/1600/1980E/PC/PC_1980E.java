import java.io.*;
import java.util.StringTokenizer;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());
            int N = n * m;

            // Positions of each value in A
            int[] arow = new int[N+1];
            int[] acol = new int[N+1];
            // Read matrix A
            for (int i = 1; i <= n; i++) {
                st = new StringTokenizer(in.readLine());
                for (int j = 1; j <= m; j++) {
                    int v = Integer.parseInt(st.nextToken());
                    arow[v] = i;
                    acol[v] = j;
                }
            }

            // Positions of each value in B
            int[] brow = new int[N+1];
            int[] bcol = new int[N+1];
            // Read matrix B
            for (int i = 1; i <= n; i++) {
                st = new StringTokenizer(in.readLine());
                for (int j = 1; j <= m; j++) {
                    int v = Integer.parseInt(st.nextToken());
                    brow[v] = i;
                    bcol[v] = j;
                }
            }

            // Attempt to build row‐mapping Rmap: A‐row -> B‐row
            int[] Rmap = new int[n+1];
            boolean ok = true;
            for (int v = 1; v <= N && ok; v++) {
                int ar = arow[v], br = brow[v];
                if (Rmap[ar] == 0) {
                    Rmap[ar] = br;
                } else if (Rmap[ar] != br) {
                    ok = false;
                }
            }
            // Attempt to build column‐mapping Cmap: A‐col -> B‐col
            int[] Cmap = new int[m+1];
            for (int v = 1; v <= N && ok; v++) {
                int ac = acol[v], bc = bcol[v];
                if (Cmap[ac] == 0) {
                    Cmap[ac] = bc;
                } else if (Cmap[ac] != bc) {
                    ok = false;
                }
            }

            // Check both maps are bijections
            if (ok) {
                boolean[] usedRow = new boolean[n+1];
                for (int i = 1; i <= n; i++) {
                    int r = Rmap[i];
                    if (r < 1 || r > n || usedRow[r]) {
                        ok = false;
                        break;
                    }
                    usedRow[r] = true;
                }
            }
            if (ok) {
                boolean[] usedCol = new boolean[m+1];
                for (int j = 1; j <= m; j++) {
                    int c = Cmap[j];
                    if (c < 1 || c > m || usedCol[c]) {
                        ok = false;
                        break;
                    }
                    usedCol[c] = true;
                }
            }

            out.println(ok ? "YES" : "NO");
        }

        out.flush();
    }
}