import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter  pw = new PrintWriter(new OutputStreamWriter(System.out));

        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int c = Integer.parseInt(st.nextToken());
            int k = Integer.parseInt(st.nextToken());
            String s = br.readLine().trim();

            // If the entire string is length <= k, we only need the last letter
            if (n <= k) {
                pw.println(1);
                continue;
            }

            // Map letters 'A'.. to 0..c-1
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = s.charAt(i) - 'A';
            }
            int lastLetter = a[n-1];

            int FULL = (1 << c) - 1;
            int LIM  = 1 << c;

            // have[mask] = 1 if some window has exactly this set of distinct letters
            int[] have = new int[LIM];

            // Sliding window of size k, track counts + bitmask
            int[] cnt = new int[c];
            int winMask = 0;

            // Initialize first window [0..k-1]
            for (int i = 0; i < k; i++) {
                int x = a[i];
                if (cnt[x]++ == 0) {
                    winMask |= (1 << x);
                }
            }
            have[winMask] = 1;

            // Slide window over positions 1..(n-k)
            for (int i = 1; i <= n - k; i++) {
                int out = a[i-1];
                if (--cnt[out] == 0) {
                    winMask ^= (1 << out);
                }
                int in = a[i + k - 1];
                if (cnt[in]++ == 0) {
                    winMask |= (1 << in);
                }
                have[winMask] = 1;
            }

            // SOS DP: G[mask] = sum of have[submask] over all submasks of mask
            // so G[mask] > 0 means there is some window L_i with L_i ⊆ mask.
            int[] G = new int[LIM];
            System.arraycopy(have, 0, G, 0, LIM);

            for (int bit = 0; bit < c; bit++) {
                for (int mask = 0; mask < LIM; mask++) {
                    if ((mask & (1 << bit)) != 0) {
                        G[mask] += G[mask ^ (1 << bit)];
                    }
                }
            }

            // Among masks M that contain the last letter, we need G[FULL^M] == 0
            // i.e. no "bad" window.  We pick the minimum popcount.
            int answer = c;
            int neededBit = 1 << lastLetter;
            for (int M = 0; M < LIM; M++) {
                if ((M & neededBit) == 0) continue;    // must contain lastLetter
                int comp = FULL ^ M;
                if (G[comp] == 0) {
                    // M is valid: it hits every window and includes S[n]
                    int pc = Integer.bitCount(M);
                    if (pc < answer) answer = pc;
                }
            }

            pw.println(answer);
        }

        pw.flush();
    }
}