import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(in.readLine().trim());
        StringBuilder out = new StringBuilder();

        while (t-- > 0) {
            String[] line = in.readLine().split(" ");
            int n = Integer.parseInt(line[0]);
            int c = Integer.parseInt(line[1]);
            int k = Integer.parseInt(line[2]);
            String s = in.readLine().trim();

            // Convert letters to 0-based indices
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = s.charAt(i) - 'A';
            }

            // If k >= n, there are no length-k windows.
            // We only need S to contain the last letter.
            if (k >= n) {
                out.append(1).append('\n');
                continue;
            }

            // Build g[mask] = number of windows of length k whose letter-set = mask.
            int maxMask = 1 << c;
            int[] g = new int[maxMask];

            int[] cnt = new int[c];
            int wmask = 0;
            // Initialize first window [0..k-1]
            for (int i = 0; i < k; i++) {
                if (cnt[a[i]]++ == 0) {
                    wmask |= (1 << a[i]);
                }
            }
            g[wmask]++;

            // Slide window from i=1..n-k
            for (int i = 1; i + k - 1 < n; i++) {
                int outLetter = a[i - 1];
                if (--cnt[outLetter] == 0) {
                    wmask &= ~(1 << outLetter);
                }
                int inLetter = a[i + k - 1];
                if (cnt[inLetter]++ == 0) {
                    wmask |= (1 << inLetter);
                }
                g[wmask]++;
            }

            // SOS DP: h[mask] = sum_{sub ⊆ mask} g[sub]
            int[] h = Arrays.copyOf(g, maxMask);
            for (int b = 0; b < c; b++) {
                int bit = 1 << b;
                for (int m = 0; m < maxMask; m++) {
                    if ((m & bit) != 0) {
                        h[m] += h[m ^ bit];
                    }
                }
            }

            int allMask = maxMask - 1;
            int lastLetterMask = 1 << a[n - 1];
            int ans = c;  // worst‐case we might need all c letters

            // Try all S that contain the last letter
            for (int S = 0; S < maxMask; S++) {
                if ((S & lastLetterMask) == 0) continue;  // must include final letter
                int comp = allMask ^ S;
                // h[comp] > 0 means there is some window U with U ⊆ comp
                // i.e. U ∩ S = ∅, so S fails.  We need h[comp]==0.
                if (h[comp] == 0) {
                    int pc = Integer.bitCount(S);
                    if (pc < ans) {
                        ans = pc;
                    }
                }
            }

            out.append(ans).append('\n');
        }

        System.out.print(out);
    }
}