import java.io.*;
import java.util.*;

public class Main {
    static final int THRESHOLD = 50;  // if subarray length > THRESHOLD, auto-YES

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer tk = new StringTokenizer(br.readLine());
        int n = Integer.parseInt(tk.nextToken());
        int q = Integer.parseInt(tk.nextToken());

        int[] a = new int[n];
        tk = new StringTokenizer(br.readLine());
        for (int i = 0; i < n; i++) {
            a[i] = Integer.parseInt(tk.nextToken());
        }

        StringBuilder sb = new StringBuilder();
        for (int _q = 0; _q < q; _q++) {
            tk = new StringTokenizer(br.readLine());
            int l = Integer.parseInt(tk.nextToken()) - 1;
            int r = Integer.parseInt(tk.nextToken()) - 1;
            int len = r - l + 1;

            if (len > THRESHOLD) {
                // By the Fibonacci / pigeonhole argument,
                // in any >50 sticks you can always find two disjoint triangles.
                sb.append("YES\n");
                continue;
            }

            // Copy and sort the small subarray
            int m = len;
            int[] b = new int[m];
            for (int i = 0; i < m; i++) {
                b[i] = a[l + i];
            }
            Arrays.sort(b);

            // 1) Find *one* triangle with smallest possible "k"
            boolean foundFirst = false;
            int f_i = -1, f_j = -1, f_k = -1;

            // standard two-pointer for each k
            for (int k = 2; k < m; k++) {
                int i = 0, j = k - 1;
                while (i < j) {
                    if ((long)b[i] + b[j] > b[k]) {
                        // found a valid triangle (i,j,k)
                        f_i = i;
                        f_j = j;
                        f_k = k;
                        foundFirst = true;
                        break;
                    }
                    // too small, raise i
                    i++;
                }
                if (foundFirst) break;
            }

            if (!foundFirst) {
                // not even one triangle
                sb.append("NO\n");
                continue;
            }

            // 2) Remove the three chosen indices, produce c (still sorted)
            int[] c = new int[m - 3];
            int idx = 0;
            for (int x = 0; x < m; x++) {
                if (x == f_i || x == f_j || x == f_k) continue;
                c[idx++] = b[x];
            }

            // 3) On c, check if *any* triangle exists
            boolean foundSecond = false;
            int cm = c.length;
            for (int k = 2; k < cm && !foundSecond; k++) {
                int i = 0, j = k - 1;
                while (i < j) {
                    if ((long)c[i] + c[j] > c[k]) {
                        foundSecond = true;
                        break;
                    }
                    i++;
                }
            }

            sb.append(foundSecond ? "YES\n" : "NO\n");
        }

        System.out.print(sb.toString());
    }
}