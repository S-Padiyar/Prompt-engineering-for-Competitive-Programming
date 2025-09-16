import java.io.*;
import java.util.*;

public class Main {
    static int gcd(int a, int b) {
        while (b != 0) {
            int t = a % b;
            a = b;
            b = t;
        }
        return a;
    }
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());
            int k = Integer.parseInt(st.nextToken());
            long[][] a = new long[n+1][m+1];
            for (int i = 1; i <= n; i++) {
                st = new StringTokenizer(in.readLine());
                for (int j = 1; j <= m; j++) {
                    a[i][j] = Long.parseLong(st.nextToken());
                }
            }
            int[][] w = new int[n+1][m+1];
            for (int i = 1; i <= n; i++) {
                String s = in.readLine();
                for (int j = 1; j <= m; j++) {
                    w[i][j] = (s.charAt(j-1) == '0') ? +1 : -1;
                }
            }
            long D = 0;
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= m; j++) {
                    D += w[i][j] * a[i][j];
                }
            }
            int[][] pref = new int[n+1][m+1];
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= m; j++) {
                    pref[i][j] = w[i][j] + pref[i-1][j] + pref[i][j-1] - pref[i-1][j-1];
                }
            }
            int g = 0;
            for (int u = 1; u + k - 1 <= n; u++) {
                for (int v = 1; v + k - 1 <= m; v++) {
                    int i2 = u + k - 1;
                    int j2 = v + k - 1;
                    int blockSum = pref[i2][j2] - pref[u-1][j2] - pref[i2][v-1] + pref[u-1][v-1];
                    g = gcd(g, Math.abs(blockSum));
                }
            }
            if (g == 0) {
                out.println(D == 0 ? "YES" : "NO");
            } else {
                out.println((D % g) == 0 ? "YES" : "NO");
            }
        }
        out.flush();
    }
}