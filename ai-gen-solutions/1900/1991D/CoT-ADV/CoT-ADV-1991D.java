import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // Fast I/O
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder out = new StringBuilder();

        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            int n = Integer.parseInt(in.readLine().trim());
            int k;
            int[] color = new int[n+1];  // 1-based

            if (n == 1) {
                // Only one vertex needs color 1
                k = 1;
                color[1] = 1;
            }
            else if (n == 2) {
                // Two vertices, edge(1,2) since 1^2=3 prime => 2 colors
                k = 2;
                color[1] = 1;
                color[2] = 2;
            }
            else if (n == 3) {
                // Graph on {1,2,3} has edges 1-2 (3),1-3(2), but not 2-3(1)
                // Needs 2 colors
                k = 2;
                // One valid coloring: 1->1, 2->2, 3->2
                color[1] = 1;
                color[2] = 2;
                color[3] = 2;
            }
            else if (n == 4) {
                // Known chi=3
                k = 3;
                // Valid: [1,2,2,3]
                color[1] = 1;
                color[2] = 2;
                color[3] = 2;
                color[4] = 3;
            }
            else if (n == 5) {
                // Known chi=3
                k = 3;
                // Valid: [1,2,2,3,3]
                color[1] = 1;
                color[2] = 2;
                color[3] = 2;
                color[4] = 3;
                color[5] = 3;
            }
            else {
                // For n >= 6, we need 4 colors
                k = 4;
                for (int i = 1; i <= n; i++) {
                    int bit0 = i & 1;
                    int bit1 = (i >> 1) & 1;
                    color[i] = bit1 * 2 + bit0 + 1;
                }
            }

            // Output
            out.append(k).append('\n');
            for (int i = 1; i <= n; i++) {
                out.append(color[i]).append(i == n ? '\n' : ' ');
            }
        }
        System.out.print(out);
    }
}