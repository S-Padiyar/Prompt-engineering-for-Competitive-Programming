import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // Fast input
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        int n = Integer.parseInt(st.nextToken());  // number of countries
        int k = Integer.parseInt(st.nextToken());  // number of regions
        int q = Integer.parseInt(st.nextToken());  // number of queries

        // We'll build b[col][row], 0-based.
        // b[j][i] = OR of a[0..i][j]
        int[][] b = new int[k][n];

        // Read in the matrix row by row, build prefix-OR down columns
        for (int i = 0; i < n; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < k; j++) {
                int aij = Integer.parseInt(st.nextToken());
                if (i == 0) {
                    b[j][i] = aij;
                } else {
                    b[j][i] = b[j][i - 1] | aij;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        // Process each query
        for (int qi = 0; qi < q; qi++) {
            int m = Integer.parseInt(br.readLine().trim());
            int lo = 1, hi = n;  // candidate range [lo..hi], 1-based

            for (int req = 0; req < m; req++) {
                st = new StringTokenizer(br.readLine());
                int r = Integer.parseInt(st.nextToken()) - 1;  // 0-based column
                char op = st.nextToken().charAt(0);            // '<' or '>'
                int c = Integer.parseInt(st.nextToken());

                int[] col = b[r];
                if (op == '<') {
                    // Find last index i with col[i] < c
                    // lower_bound of c gives first idx >= c
                    int lb = lowerBound(col, c);
                    int last = lb - 1; 
                    if (last < 0) {
                        // no row satisfies b[row][r] < c
                        hi = 0;
                    } else {
                        hi = Math.min(hi, last + 1); // convert to 1-based
                    }
                } else {
                    // op == '>' 
                    // Find first index i with col[i] > c
                    int ub = upperBound(col, c);
                    if (ub >= n) {
                        // no row has b[row][r] > c
                        lo = n + 1;
                    } else {
                        lo = Math.max(lo, ub + 1);
                    }
                }
            }

            // After all m constraints, check if there's a valid i
            if (lo <= hi) {
                sb.append(lo).append('\n');
            } else {
                sb.append(-1).append('\n');
            }
        }

        // Output all answers
        System.out.print(sb.toString());
    }

    // Returns the first index in 'arr' where arr[idx] >= x,
    // or arr.length if all are < x.
    static int lowerBound(int[] arr, int x) {
        int l = 0, r = arr.length;
        while (l < r) {
            int mid = (l + r) >>> 1;
            if (arr[mid] < x) {
                l = mid + 1;
            } else {
                r = mid;
            }
        }
        return l;
    }

    // Returns the first index in 'arr' where arr[idx] > x,
    // or arr.length if none are > x.
    static int upperBound(int[] arr, int x) {
        int l = 0, r = arr.length;
        while (l < r) {
            int mid = (l + r) >>> 1;
            if (arr[mid] <= x) {
                l = mid + 1;
            } else {
                r = mid;
            }
        }
        return l;
    }
}