import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(in.readLine());
        int t = Integer.parseInt(st.nextToken());
        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            st = new StringTokenizer(in.readLine());
            int l = Integer.parseInt(st.nextToken());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());

            // read the target array a[1..l]
            int[] a = new int[l+1];
            st = new StringTokenizer(in.readLine());
            for (int i = 1; i <= l; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            // read the grid and build position‐lists for values 1..7
            @SuppressWarnings("unchecked")
            ArrayList<int[]>[] pos = new ArrayList[8];
            for (int v = 1; v <= 7; v++) {
                pos[v] = new ArrayList<>();
            }
            for (int i = 1; i <= n; i++) {
                st = new StringTokenizer(in.readLine());
                for (int j = 1; j <= m; j++) {
                    int v = Integer.parseInt(st.nextToken());
                    // record that grid‐cell (i,j) has value v
                    pos[v].add(new int[]{i, j});
                }
            }

            // We'll do DP downwards from k=l..1
            // winNext[r][c] = win[k+1][r][c], winCurr[r][c] = win[k][r][c]
            boolean[][] winNext = new boolean[n+2][m+2];
            boolean[][] winCurr = new boolean[n+2][m+2];
            // valGen trick to avoid clearing the val[][] every time
            int[][] valGen = new int[n+2][m+2];
            int gen = 1;

            // Initialize winNext = false (this is win[l+1][r][c] = false).
            // Now proceed k = l down to 1:
            for (int k = l; k >= 1; k--, gen++) {
                int v = a[k];
                // Mark val[r][c] = true exactly where b[r][c]==v and winNext[r+1][c+1]==false
                for (int[] xy : pos[v]) {
                    int r = xy[0], c = xy[1];
                    if (!winNext[r+1][c+1]) {
                        valGen[r][c] = gen;
                    }
                }
                // Build winCurr as the 2D suffix‐OR of val
                // Borders are false:
                for (int c = 1; c <= m+1; c++) {
                    winCurr[n+1][c] = false;
                }
                for (int r = 1; r <= n; r++) {
                    winCurr[r][m+1] = false;
                }
                // fill the interior from bottom‐right upwards
                for (int r = n; r >= 1; r--) {
                    for (int c = m; c >= 1; c--) {
                        boolean canWinHere = (valGen[r][c] == gen)
                            || winCurr[r+1][c]
                            || winCurr[r][c+1];
                        winCurr[r][c] = canWinHere;
                    }
                }
                // swap winNext and winCurr for the next iteration
                boolean[][] tmp = winNext;
                winNext = winCurr;
                winCurr = tmp;
            }

            // At the end winNext[1][1] = win[1][1][1].
            sb.append(winNext[1][1] ? 'T' : 'N').append('\n');
        }

        System.out.print(sb);
    }
}