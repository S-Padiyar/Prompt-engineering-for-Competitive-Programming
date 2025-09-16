import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader in;
    static PrintWriter out;

    // compute gcd(a,b)
    static int gcd(int a, int b) {
        while (b != 0) {
            int t = a % b;
            a = b;
            b = t;
        }
        return a;
    }

    public static void main(String[] args) throws IOException {
        in = new BufferedReader(new InputStreamReader(System.in));
        out = new PrintWriter(System.out);

        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            int n = Integer.parseInt(in.readLine().trim());
            StringTokenizer st = new StringTokenizer(in.readLine());
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            boolean[] used = new boolean[n];
            int usedCount = 0;
            long ans = 0;

            int cur = 0;  // the gcd of the prefix so far

            // We will pick elements until we either
            //  1) get cur=1, or
            //  2) find that gcd(cur, bestNext) == cur again,
            //     meaning no further reduction is possible.
            mainLoop:
            while (usedCount < n) {
                int bestG = Integer.MAX_VALUE;
                int bestIdx = -1;
                // scan for the element that minimizes gcd(cur, a[j])
                for (int j = 0; j < n; j++) {
                    if (used[j]) continue;
                    int g = gcd(cur, a[j]);
                    if (g < bestG) {
                        bestG = g;
                        bestIdx = j;
                        if (bestG == 1) break;  // can't do better
                    }
                }

                int rem = n - usedCount;  // how many are left

                // if the best we can do is keep cur the same, we break
                // adding cur*rem to the answer
                if (bestG == cur) {
                    ans += (long) cur * rem;
                    break;
                }

                // otherwise we pick that element
                ans += bestG;
                cur = bestG;
                used[bestIdx] = true;
                usedCount++;

                // if we have achieved cur=1, all remaining will contribute 1
                if (cur == 1) {
                    ans += (long) 1 * (n - usedCount);
                    break;
                }
            }

            out.println(ans);
        }

        out.flush();
    }
}