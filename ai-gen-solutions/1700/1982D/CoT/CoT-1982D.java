import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    static StringTokenizer tok = new StringTokenizer("");

    static String next() throws IOException {
        while (!tok.hasMoreTokens()) {
            String line = in.readLine();
            if (line == null) return null;
            tok = new StringTokenizer(line);
        }
        return tok.nextToken();
    }
    static int nextInt() throws IOException {
        return Integer.parseInt(next());
    }
    static long nextLong() throws IOException {
        return Long.parseLong(next());
    }
    
    // gcd for nonnegative inputs
    static long gcd(long a, long b) {
        if (a < 0) a = -a;
        if (b < 0) b = -b;
        while (b != 0) {
            long t = a % b;
            a = b;
            b = t;
        }
        return a;
    }

    public static void main(String[] args) throws IOException {
        int t = nextInt();
        StringBuilder sb = new StringBuilder();
        for (int _case = 0; _case < t; _case++) {
            int n = nextInt(), m = nextInt(), k = nextInt();
            long[][] a = new long[n+1][m+1];
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= m; j++) {
                    a[i][j] = nextLong();
                }
            }
            String[] types = new String[n];
            for (int i = 0; i < n; i++) {
                types[i] = next();
            }
            // 1) Compute initial D = sum_caps - sum_noCaps
            long sumCaps = 0, sumNoCaps = 0;
            for (int i = 1; i <= n; i++) {
                String row = types[i-1];
                for (int j = 1; j <= m; j++) {
                    if (row.charAt(j-1) == '0') {
                        sumCaps += a[i][j];
                    } else {
                        sumNoCaps += a[i][j];
                    }
                }
            }
            long D = sumCaps - sumNoCaps;

            // 2) Build b[i][j] = +1 (cap), -1 (noCap)
            int[][] b = new int[n+1][m+1];
            for (int i = 1; i <= n; i++) {
                String row = types[i-1];
                for (int j = 1; j <= m; j++) {
                    b[i][j] = (row.charAt(j-1) == '0' ? 1 : -1);
                }
            }

            // 3) Prefix sums of b
            long[][] ps = new long[n+1][m+1];
            for (int i = 1; i <= n; i++) {
                long running = 0;
                for (int j = 1; j <= m; j++) {
                    running += b[i][j];
                    ps[i][j] = ps[i-1][j] + running;
                }
            }

            // 4) Sweep all k×k blocks, collect gcd of their deltas
            long G = 0;
            for (int i = 1; i + k - 1 <= n; i++) {
                for (int j = 1; j + k - 1 <= m; j++) {
                    int i2 = i + k - 1;
                    int j2 = j + k - 1;
                    long blockSum = ps[i2][j2]
                                  - ps[i-1][j2]
                                  - ps[i2][j-1]
                                  + ps[i-1][j-1];
                    G = gcd(G, blockSum);
                }
            }

            // 5) Decision
            boolean ok;
            if (G == 0) {
                // All block deltas are 0 ⇒ we cannot change D at all
                ok = (D == 0);
            } else {
                // We can shift D by any multiple of G
                ok = (Math.floorMod(D, G) == 0);
            }

            sb.append(ok ? "YES\n" : "NO\n");
        }

        System.out.print(sb);
    }
}