import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Main {
    // Compute Z-array in O(N)
    // z[i] = length of longest substring starting i which matches prefix of s
    static int[] buildZ(char[] s) {
        int n = s.length;
        int[] z = new int[n];
        int l = 0, r = 0;
        for (int i = 1; i < n; i++) {
            if (i <= r) {
                z[i] = Math.min(r - i + 1, z[i - l]);
            }
            while (i + z[i] < n && s[z[i]] == s[i + z[i]]) {
                z[i]++;
            }
            if (i + z[i] - 1 > r) {
                l = i;
                r = i + z[i] - 1;
            }
        }
        return z;
    }

    // Try to find a valid p so that the operation on s yields target-pattern T
    // Returns p (1..n) or -1 if none.
    static int findP(int n, int k, char[] s, char[] T) {
        // Build U2 = reverse(T) # s
        char[] u2 = new char[n + 1 + n];
        // reverse(T)
        for (int i = 0; i < n; i++) {
            u2[i] = T[n - 1 - i];
        }
        u2[n] = '#';
        System.arraycopy(s, 0, u2, n + 1, n);
        int[] z2 = buildZ(u2);
        // p_max = how many chars of s match reverse(T)
        int pMax = z2[n + 1];
        if (pMax <= 0) {
            // no nonzero p can satisfy the reversed-prefix condition
            return -1;
        }
        if (pMax >= n) {
            // full match => we can take p = n
            return n;
        }
        // Now build U1 = T # s to check the first condition
        char[] u1 = new char[n + 1 + n];
        System.arraycopy(T, 0, u1, 0, n);
        u1[n] = '#';
        System.arraycopy(s, 0, u1, n + 1, n);
        int[] z1 = buildZ(u1);
        // Try each p in [1..pMax]
        for (int p = 1; p <= pMax; p++) {
            // we need s[p..n-1] == T[0..n-p-1], i.e. Z1 at position (n+1+p)
            if (z1[n + 1 + p] >= (n - p)) {
                return p;
            }
        }
        return -1;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            String[] nk = in.readLine().trim().split(" ");
            int n = Integer.parseInt(nk[0]);
            int k = Integer.parseInt(nk[1]);
            char[] s = in.readLine().trim().toCharArray();

            // Build the two alternating-block patterns T0, T1
            int m = n / k;  // number of blocks
            char[] T0 = new char[n];
            char[] T1 = new char[n];

            for (int block = 0; block < m; block++) {
                // block even => same as block 0; block odd => opposite
                char c0 = ((block & 1) == 0 ? '0' : '1');
                char c1 = ((block & 1) == 0 ? '1' : '0');
                for (int i = 0; i < k; i++) {
                    T0[block * k + i] = c0;
                    T1[block * k + i] = c1;
                }
            }

            // Try T0 first
            int ans = findP(n, k, s, T0);
            if (ans < 0) {
                // Try T1
                ans = findP(n, k, s, T1);
            }
            out.println(ans);
        }

        out.flush();
    }
}