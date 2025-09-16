import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // Fast input
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(br.readLine().trim());
        // We will reuse two boolean arrays up to size 2^18.
        final int MAXC = 18;
        final int MAXMASK = 1 << MAXC;
        boolean[] bad = new boolean[MAXMASK];
        boolean[] invalid = new boolean[MAXMASK];

        while (t-- > 0) {
            String[] parts = br.readLine().split(" ");
            int n = Integer.parseInt(parts[0]);
            int c = Integer.parseInt(parts[1]);
            int k = Integer.parseInt(parts[2]);
            String s = br.readLine().trim();

            int fullMask = (1 << c) - 1;
            int Msize = 1 << c;  // we'll only use [0..Msize-1]
            
            // Clear bad[] and invalid[] up to Msize
            for (int m = 0; m < Msize; m++) {
                bad[m] = false;
                invalid[m] = false;
            }

            // Build the windows of length k (or if k > n, just the whole string once).
            int[] freq = new int[c];
            int windowMask = 0;

            if (k >= n) {
                // Only one window: the entire string [0..n-1]
                for (int i = 0; i < n; i++) {
                    int letter = s.charAt(i) - 'A';
                    if (++freq[letter] == 1) {
                        windowMask |= (1 << letter);
                    }
                }
                int A = fullMask ^ windowMask;
                bad[A] = true;
            } else {
                // Build first window [0..k-1]
                for (int i = 0; i < k; i++) {
                    int letter = s.charAt(i) - 'A';
                    if (++freq[letter] == 1) {
                        windowMask |= (1 << letter);
                    }
                }
                // Slide from i=0..n-k
                bad[fullMask ^ windowMask] = true;
                for (int i = 1; i <= n - k; i++) {
                    // remove s[i-1], add s[i+k-1]
                    int oldLet = s.charAt(i - 1) - 'A';
                    if (--freq[oldLet] == 0) {
                        windowMask &= ~(1 << oldLet);
                    }
                    int newLet = s.charAt(i + k - 1) - 'A';
                    if (++freq[newLet] == 1) {
                        windowMask |= (1 << newLet);
                    }
                    bad[fullMask ^ windowMask] = true;
                }
            }

            // Also enforce final‐letter constraint: s[n-1] must be in E
            int lastLetter = s.charAt(n - 1) - 'A';
            int Aend = fullMask ^ (1 << lastLetter);
            bad[Aend] = true;

            // Mark all submasks of a bad mask as invalid
            for (int A = 0; A < Msize; A++) {
                if (bad[A]) {
                    // iterate all submasks of A
                    int sub = A;
                    do {
                        invalid[sub] = true;
                        sub = (sub - 1) & A;
                    } while (sub != A);
                }
            }

            // Scan for the valid mask with minimal popcount
            int answer = c;  // at worst, we need all c letters
            for (int m = 0; m < Msize; m++) {
                if (!invalid[m]) {
                    int pc = Integer.bitCount(m);
                    if (pc < answer) {
                        answer = pc;
                        if (answer == 0) break; // can't do better than 0
                    }
                }
            }

            out.println(answer);
        }

        out.flush();
    }
}