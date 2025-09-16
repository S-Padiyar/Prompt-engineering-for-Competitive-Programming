import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            StringTokenizer tok = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(tok.nextToken());
            int m = Integer.parseInt(tok.nextToken());
            int k = Integer.parseInt(tok.nextToken());

            // Read the heights
            long[][] A = new long[n][m];
            for (int i = 0; i < n; i++) {
                tok = new StringTokenizer(in.readLine());
                for (int j = 0; j < m; j++) {
                    A[i][j] = Long.parseLong(tok.nextToken());
                }
            }

            // Read the types and build B, and accumulate initial D = sum A[i][j]*B[i][j]
            int[][] B = new int[n][m];
            long D = 0;
            for (int i = 0; i < n; i++) {
                String s = in.readLine();
                for (int j = 0; j < m; j++) {
                    // T=0-->B=+1, T=1-->B=-1
                    B[i][j] = (s.charAt(j) == '0' ? +1 : -1);
                    D += A[i][j] * B[i][j];
                }
            }

            // Build 2D prefix sums of B for O(1) window sums
            int[][] pref = new int[n+1][m+1];
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= m; j++) {
                    pref[i][j] = pref[i-1][j] + pref[i][j-1]
                               - pref[i-1][j-1] + B[i-1][j-1];
                }
            }

            // Compute G = gcd of all |Delta_{r,c}|
            long G = 0;
            for (int r = 0; r + k <= n; r++) {
                for (int c = 0; c + k <= m; c++) {
                    int sumB = pref[r+k][c+k] 
                             - pref[r][c+k] 
                             - pref[r+k][c] 
                             + pref[r][c];
                    G = gcd(G, Math.abs((long)sumB));
                }
            }

            // Check feasibility
            boolean ok;
            if (G == 0) {
                // No operation can change D => must already be zero
                ok = (D == 0);
            } else {
                // We need D ≡ 0 (mod G)
                ok = (D % G == 0);
            }

            out.println(ok ? "YES" : "NO");
        }

        out.flush();
    }

    // Euclidean gcd for nonnegative a,b
    static long gcd(long a, long b) {
        while (b != 0) {
            long t = a % b;
            a = b;
            b = t;
        }
        return a;
    }
}