import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // Fast input
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder out = new StringBuilder();

        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int c = Integer.parseInt(st.nextToken());
            int k = Integer.parseInt(st.nextToken());

            String s = in.readLine().trim();
            // If the whole text length <= k, we can make it one word, need only the last letter's case.
            if (n <= k) {
                out.append(1).append('\n');
                continue;
            }

            // Convert characters to 0..c-1
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = s.charAt(i) - 'A';
            }
            int endIdx = a[n - 1];  // index of the final letter

            int limit = 1 << c;
            // forbidden[M] = true if there's a k-length window whose set of letters = M
            boolean[] forbidden = new boolean[limit];

            // Build the first window's letter counts
            int[] cnt = new int[c];
            int mask = 0;
            // first k letters
            for (int i = 0; i < k; i++) {
                int x = a[i];
                if (cnt[x] == 0) {
                    mask |= (1 << x);
                }
                cnt[x]++;
            }
            // record the mask of window [0..k-1]
            forbidden[mask] = true;
            // slide the window from i=1..n-k
            for (int i = k; i < n; i++) {
                // remove a[i-k], add a[i]
                int outChar = a[i - k];
                cnt[outChar]--;
                if (cnt[outChar] == 0) {
                    mask &= ~(1 << outChar);
                }
                int inChar = a[i];
                if (cnt[inChar] == 0) {
                    mask |= (1 << inChar);
                }
                cnt[inChar]++;
                forbidden[mask] = true;
            }

            // bad[U] = true if U contains any forbidden mask as a subset
            boolean[] bad = Arrays.copyOf(forbidden, limit);
            // superset DP: any superset of a bad set is also bad
            for (int bit = 0; bit < c; bit++) {
                for (int m = 0; m < limit; m++) {
                    if ((m & (1 << bit)) != 0 && bad[m ^ (1 << bit)]) {
                        bad[m] = true;
                    }
                }
            }

            // Among complements U that are not bad and exclude the final letter,
            // pick the one with the largest popcount.
            int bestU = 0;
            for (int U = 0; U < limit; U++) {
                if (!bad[U] && ((U & (1 << endIdx)) == 0)) {
                    int pc = Integer.bitCount(U);
                    if (pc > bestU) {
                        bestU = pc;
                    }
                }
            }

            // The answer is c - bestU (we choose S = complement of U)
            out.append((c - bestU)).append('\n');
        }

        // Print all answers
        System.out.print(out);
    }
}