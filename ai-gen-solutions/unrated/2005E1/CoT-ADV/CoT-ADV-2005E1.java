import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // Fast I/O
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder output = new StringBuilder();
        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int l = Integer.parseInt(st.nextToken());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());

            // Read array a
            int[] a = new int[l + 1];
            st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= l; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            // Read matrix b (1-based indexing)
            int[][] b = new int[n + 2][m + 2];
            for (int i = 1; i <= n; i++) {
                st = new StringTokenizer(br.readLine());
                for (int j = 1; j <= m; j++) {
                    b[i][j] = Integer.parseInt(st.nextToken());
                }
            }

            // dpNext[r][c] holds dp[pos+1][r][c]; initialize for pos = l+1 as all false.
            boolean[][] dpNext = new boolean[n + 2][m + 2];

            // Work backwards from pos = l down to 1
            for (int pos = l; pos >= 1; pos--) {
                int want = a[pos];
                boolean[][] dpCurr = new boolean[n + 2][m + 2];
                // Build dpCurr[r][c] = suffix-OR over (x,y) >= (r,c) of
                // (b[x][y]==want && !dpNext[x+1][y+1]).
                for (int r = n; r >= 1; r--) {
                    for (int c = m; c >= 1; c--) {
                        // If we can pick (r,c) to match want and leave the opponent
                        // in a losing state, it's a winning move
                        if (b[r][c] == want && !dpNext[r + 1][c + 1]) {
                            dpCurr[r][c] = true;
                        } else {
                            // Otherwise inherit any winning choice in the submatrix
                            dpCurr[r][c] = dpCurr[r + 1][c] || dpCurr[r][c + 1];
                        }
                    }
                }
                // Move dpCurr → dpNext for the next iteration
                dpNext = dpCurr;
            }

            // At the start pos=1 with submatrix (1,1), if dpNext[1][1] is true, Tsovak wins.
            output.append(dpNext[1][1] ? 'T' : 'N').append('\n');
        }
        System.out.print(output);
    }
}