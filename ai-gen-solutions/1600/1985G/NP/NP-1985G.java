import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 1_000_000_007;

    // Fast exponentiation x^e mod MOD
    static long modPow(long x, long e) {
        long result = 1;
        x %= MOD;
        while (e > 0) {
            if ((e & 1) == 1) {
                result = (result * x) % MOD;
            }
            x = (x * x) % MOD;
            e >>= 1;
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            st = new StringTokenizer(br.readLine());
            long l = Long.parseLong(st.nextToken());
            long r = Long.parseLong(st.nextToken());
            long k = Long.parseLong(st.nextToken());

            // Maximum digit allowed so that k*d < 10
            long B = 9 / k;
            if (B == 0) {
                // No nonzero leading digit possible
                sb.append(0).append('\n');
                continue;
            }

            long base = (B + 1) % MOD;

            // (B+1)^l  mod
            long p1 = modPow(base, l);
            // (B+1)^(r-l) mod
            long p2 = modPow(base, r - l);

            // ans = p1 * (p2 - 1) mod
            long ans = (p1 * ((p2 - 1 + MOD) % MOD)) % MOD;
            sb.append(ans).append('\n');
        }

        System.out.print(sb);
    }
}