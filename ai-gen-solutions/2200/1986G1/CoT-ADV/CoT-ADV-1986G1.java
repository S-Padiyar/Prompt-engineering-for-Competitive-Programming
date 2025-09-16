import java.io.*;
import java.util.*;

public class Main {
    // Fast gcd
    private static int gcd(int a, int b) {
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
            int n = Integer.parseInt(in.readLine().trim());
            StringTokenizer st = new StringTokenizer(in.readLine());
            int[] p = new int[n+1];
            for (int i = 1; i <= n; i++) {
                p[i] = Integer.parseInt(st.nextToken());
            }

            long ans = 0;
            // For each i, we only try j that are multiples of step = p[i]/gcd(i,p[i])
            for (int i = 1; i <= n; i++) {
                int g = gcd(i, p[i]);
                int step = p[i] / g;
                // Step j through multiples of 'step'
                // Only pairs with j > i can possibly work
                for (int j = step; j <= n; j += step) {
                    if (j <= i) continue;
                    // Check divisibility
                    long num = (long)p[i] * p[j];
                    long den = (long)i * j;
                    if (num % den == 0) {
                        ans++;
                    }
                }
            }

            out.println(ans);
        }
        out.flush();
    }
}