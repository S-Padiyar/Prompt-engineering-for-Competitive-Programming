import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader in;
    static PrintWriter out;
    static StringTokenizer tok;

    // Compute GCD of a and b
    static int gcd(int a, int b) {
        while (b != 0) {
            int t = b;
            b = a % b;
            a = t;
        }
        return a;
    }

    public static void main(String[] args) throws IOException {
        in  = new BufferedReader(new InputStreamReader(System.in));
        out = new PrintWriter(new OutputStreamWriter(System.out));

        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            int n = Integer.parseInt(in.readLine().trim());
            int[] a = new int[n];
            tok = new StringTokenizer(in.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(tok.nextToken());
            }

            // Sort so that the smallest element is first
            Arrays.sort(a);

            boolean[] used = new boolean[n];
            long sum = 0;

            // Pick the smallest element as b1
            int g = a[0];
            sum += g;
            used[0] = true;

            // Greedily pick the next element that minimizes the running GCD
            for (int k = 1; k < n; k++) {
                // If we've already reached gcd = 1, all future terms are 1
                if (g == 1) {
                    sum += (n - k); 
                    break;
                }
                int bestG = Integer.MAX_VALUE;
                int bestIdx = -1;
                for (int i = 1; i < n; i++) {
                    if (used[i]) continue;
                    int g2 = gcd(g, a[i]);
                    if (g2 < bestG) {
                        bestG = g2;
                        bestIdx = i;
                        if (bestG == 1) break;  // perfect, can't do better
                    }
                }
                // If the gcd does not decrease, it will remain 'g' for all remaining picks.
                if (bestG == g) {
                    sum += (long) g * (n - k);
                    break;
                }
                // Otherwise, use that element, accumulate its gcd, and continue
                sum += bestG;
                g = bestG;
                used[bestIdx] = true;
            }

            out.println(sum);
        }
        out.flush();
    }
}