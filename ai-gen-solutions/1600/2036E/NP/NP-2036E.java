import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer stk = new StringTokenizer(in.readLine());
        int n = Integer.parseInt(stk.nextToken());
        int k = Integer.parseInt(stk.nextToken());
        int q = Integer.parseInt(stk.nextToken());

        // Read the original a[i][j]
        int[][] a = new int[n][k];
        for (int i = 0; i < n; i++) {
            stk = new StringTokenizer(in.readLine());
            for (int j = 0; j < k; j++) {
                a[i][j] = Integer.parseInt(stk.nextToken());
            }
        }

        // Build b[j][i] = b_{i+1, j+1}, i.e. prefix-OR down each column j
        int[][] b = new int[k][n];
        for (int j = 0; j < k; j++) {
            b[j][0] = a[0][j];
            for (int i = 1; i < n; i++) {
                b[j][i] = b[j][i - 1] | a[i][j];
            }
        }

        // Process each query
        StringBuilder sb = new StringBuilder();
        for (int _q = 0; _q < q; _q++) {
            int m = Integer.parseInt(in.readLine().trim());
            // Our candidate interval of country-indices is [low, high], 1-based
            int low = 1;
            int high = n;

            for (int t = 0; t < m; t++) {
                if (low > high) {
                    // Already impossible, just read and discard
                    in.readLine();
                    continue;
                }

                // Read requirement: r, op, c
                stk = new StringTokenizer(in.readLine());
                int r = Integer.parseInt(stk.nextToken()) - 1; // zero-based column
                String op = stk.nextToken();
                int c = Integer.parseInt(stk.nextToken());

                int[] col = b[r];

                if (op.equals(">")) {
                    // Find first index i where col[i] > c
                    int pos = upperBound(col, c);
                    // pos is in [0..n]; if pos==n, no such index
                    int iMin = (pos == n ? n + 1 : pos + 1);
                    low = Math.max(low, iMin);
                } else {
                    // op is "<"
                    // Find first index i where col[i] >= c
                    int pos = lowerBound(col, c);
                    // all indices < pos satisfy col[i] < c
                    // the largest 1-based index is pos
                    int iMax = pos;
                    high = Math.min(high, iMax);
                }
            }

            if (low <= high) {
                sb.append(low).append('\n');
            } else {
                sb.append(-1).append('\n');
            }
        }

        // Output all answers
        System.out.print(sb);
    }

    // lowerBound(a, x): first index i such that a[i] >= x, or a.length if none.
    static int lowerBound(int[] a, int x) {
        int l = 0, r = a.length;
        while (l < r) {
            int m = (l + r) >>> 1;
            if (a[m] >= x) {
                r = m;
            } else {
                l = m + 1;
            }
        }
        return l;
    }

    // upperBound(a, x): first index i such that a[i] > x, or a.length if none.
    static int upperBound(int[] a, int x) {
        int l = 0, r = a.length;
        while (l < r) {
            int m = (l + r) >>> 1;
            if (a[m] > x) {
                r = m;
            } else {
                l = m + 1;
            }
        }
        return l;
    }
}