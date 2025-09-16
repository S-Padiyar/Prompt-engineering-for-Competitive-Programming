import java.io.*;
import java.util.*;

public class Main {
    // Compute gcd of two non-negative longs
    static long gcd(long a, long b) {
        while (b != 0) {
            long t = a % b;
            a = b;
            b = t;
        }
        return a;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());
            int k = Integer.parseInt(st.nextToken());

            // Read heights
            long[][] a = new long[n+1][m+1];
            for (int i = 1; i <= n; i++) {
                st = new StringTokenizer(br.readLine());
                for (int j = 1; j <= m; j++) {
                    a[i][j] = Long.parseLong(st.nextToken());
                }
            }

            // Read types, build w = +1 for '0', -1 for '1'
            int[][] w = new int[n+1][m+1];
            for (int i = 1; i <= n; i++) {
                String row = br.readLine();
                for (int j = 1; j <= m; j++) {
                    w[i][j] = (row.charAt(j-1) == '0') ? +1 : -1;
                }
            }

            // Compute initial weighted difference D = sum w[i][j]*a[i][j]
            long D = 0;
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= m; j++) {
                    D += w[i][j] * a[i][j];
                }
            }

            // Build 2D prefix-sum of w
            long[][] PS = new long[n+1][m+1];
            for (int i = 1; i <= n; i++) {
                long rowSum = 0;
                for (int j = 1; j <= m; j++) {
                    rowSum += w[i][j];
                    PS[i][j] = PS[i-1][j] + rowSum;
                }
            }

            // Compute gcd of all |B_ij| for k x k windows
            long g = 0;
            for (int i = 1; i + k - 1 <= n; i++) {
                for (int j = 1; j + k - 1 <= m; j++) {
                    int r2 = i + k - 1;
                    int c2 = j + k - 1;
                    long sumW =
                        PS[r2][c2]
                      - PS[i-1][c2]
                      - PS[r2][j-1]
                      + PS[i-1][j-1];
                    long absW = Math.abs(sumW);
                    if (absW > 0) {
                        if (g == 0) g = absW;
                        else g = gcd(g, absW);
                    }
                }
            }

            // If g == 0, no window can change D, so D must already be 0
            boolean ok;
            if (g == 0) {
                ok = (D == 0);
            } else {
                ok = (D % g == 0);
            }

            sb.append(ok ? "YES\n" : "NO\n");
        }

        System.out.print(sb);
    }
}