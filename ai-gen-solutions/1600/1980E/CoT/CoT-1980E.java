import java.io.*;
import java.util.*;

public class Main {
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
        FastReader(InputStream in) { br = new BufferedReader(new InputStreamReader(in)); }
        String next() throws IOException {
            while (st == null || !st.hasMoreTokens())
                st = new StringTokenizer(br.readLine());
            return st.nextToken();
        }
        int nextInt() throws IOException { return Integer.parseInt(next()); }
    }

    public static void main(String[] args) throws IOException {
        FastReader fr = new FastReader(System.in);
        StringBuilder sb = new StringBuilder();
        int T = fr.nextInt();
        // Maximum total n*m over all tests is <= 2e5
        // We'll allocate these once, of size a bit above 2e5.
        final int MAXNM = 200_000 + 5;
        int[] posArow = new int[MAXNM], posAcol = new int[MAXNM];
        int[] posBrow = new int[MAXNM], posBcol = new int[MAXNM];

        while (T-- > 0) {
            int n = fr.nextInt();
            int m = fr.nextInt();
            int N = n * m;

            // Read matrix a and record positions
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= m; j++) {
                    int x = fr.nextInt();
                    posArow[x] = i;
                    posAcol[x] = j;
                }
            }
            // Read matrix b and record positions
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= m; j++) {
                    int x = fr.nextInt();
                    posBrow[x] = i;
                    posBcol[x] = j;
                }
            }

            // Try to build a consistent row‐mapping p: a‐row -> b‐row
            int[] p = new int[n+1];
            boolean[] seenBrow = new boolean[n+1];
            Arrays.fill(p, -1);
            boolean ok = true;
            for (int x = 1; x <= N; x++) {
                int ra = posArow[x];
                int rb = posBrow[x];
                if (p[ra] == -1) {
                    p[ra] = rb;
                } else if (p[ra] != rb) {
                    ok = false;
                    break;
                }
            }
            // Check that p is injective (hence bijective, since domain & codomain size n)
            if (ok) {
                for (int i = 1; i <= n; i++) {
                    if (p[i] < 1 || p[i] > n) {
                        ok = false;
                        break;
                    }
                    if (seenBrow[p[i]]) {
                        ok = false;
                        break;
                    }
                    seenBrow[p[i]] = true;
                }
            }

            // Now do the same for columns
            if (ok) {
                int[] q = new int[m+1];
                boolean[] seenBcol = new boolean[m+1];
                Arrays.fill(q, -1);
                for (int x = 1; x <= N; x++) {
                    int ca = posAcol[x];
                    int cb = posBcol[x];
                    if (q[ca] == -1) {
                        q[ca] = cb;
                    } else if (q[ca] != cb) {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    for (int j = 1; j <= m; j++) {
                        if (q[j] < 1 || q[j] > m) {
                            ok = false;
                            break;
                        }
                        if (seenBcol[q[j]]) {
                            ok = false;
                            break;
                        }
                        seenBcol[q[j]] = true;
                    }
                }
            }

            sb.append(ok ? "YES\n" : "NO\n");
        }

        System.out.print(sb);
    }
}