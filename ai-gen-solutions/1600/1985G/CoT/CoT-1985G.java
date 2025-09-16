import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 1_000_000_007;

    // fast exponentiation a^e mod
    static long modPow(long a, long e) {
        long res = 1 % MOD;
        a %= MOD;
        while (e > 0) {
            if ((e & 1) == 1) {
                res = (res * a) % MOD;
            }
            a = (a * a) % MOD;
            e >>>= 1;
        }
        return res;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            long l = Long.parseLong(st.nextToken());
            long r = Long.parseLong(st.nextToken());
            long k = Long.parseLong(st.nextToken());

            // m = floor(9/k). If m == 0, no nonzero digit is allowed -> 0 solutions.
            long m = 9 / k;
            if (m == 0) {
                out.println(0);
                continue;
            }
            long T = m + 1;            // base of the geometric progression
            long a = l;                // exponent for T^(l)
            long N = r - l;            // number of terms is r - l

            // powA = T^a mod, powN = T^N mod
            long powA = modPow(T, a);
            long powN = modPow(T, N);

            // result = powA * (powN - 1) mod
            long ans = powN - 1;
            if (ans < 0) ans += MOD;
            ans = (powA * ans) % MOD;

            out.println(ans);
        }
        out.flush();
    }
}