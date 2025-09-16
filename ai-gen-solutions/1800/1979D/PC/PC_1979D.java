import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            String[] nk = in.readLine().split(" ");
            int n = Integer.parseInt(nk[0]), k = Integer.parseInt(nk[1]);
            String s = in.readLine().trim();
            char[] T0 = new char[n], T1 = new char[n];
            for (int i = 0; i < n; i++) {
                int b = (i / k) % 2;
                T0[i] = (b == 0 ? '0' : '1');
                T1[i] = (b == 0 ? '1' : '0');
            }
            int ans = -1;
            for (int x = 0; x < 2 && ans < 0; x++) {
                char[] T = (x == 0 ? T0 : T1);
                char[] revT = new char[n];
                for (int i = 0; i < n; i++) revT[i] = T[n - 1 - i];
                int lcp = 0;
                while (lcp < n && s.charAt(lcp) == revT[lcp]) lcp++;
                char[] V = new char[2 * n + 1];
                for (int i = 0; i < n; i++) V[i] = T[i];
                V[n] = '#';
                for (int i = 0; i < n; i++) V[n + 1 + i] = s.charAt(i);
                int[] Z = buildZ(V);
                for (int p = 1; p <= n; p++) {
                    if (p > lcp) break;
                    if (p == n || Z[n + 1 + p] >= n - p) {
                        ans = p;
                        break;
                    }
                }
            }
            out.println(ans);
        }
        out.flush();
    }

    static int[] buildZ(char[] s) {
        int n = s.length;
        int[] Z = new int[n];
        int l = 0, r = 0;
        for (int i = 1; i < n; i++) {
            if (i <= r) Z[i] = Math.min(r - i + 1, Z[i - l]);
            while (i + Z[i] < n && s[Z[i]] == s[i + Z[i]]) Z[i]++;
            if (i + Z[i] - 1 > r) {
                l = i;
                r = i + Z[i] - 1;
            }
        }
        return Z;
    }
}