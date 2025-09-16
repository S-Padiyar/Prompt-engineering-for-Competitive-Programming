import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 1_000_000_007;

    // fast exponentiation mod
    static long modPow(long base, long exp) {
        long result = 1 % MOD;
        base %= MOD;
        while (exp > 0) {
            if ((exp & 1) == 1) {
                result = (result * base) % MOD;
            }
            base = (base * base) % MOD;
            exp >>= 1;
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();

        for (int _case = 0; _case < t; _case++) {
            st = new StringTokenizer(br.readLine());
            long l = Long.parseLong(st.nextToken());
            long r = Long.parseLong(st.nextToken());
            long k = Long.parseLong(st.nextToken());

            // Compute c = floor(9/k).  If k>=10 then c=0 => no valid numbers
            long c = (k >= 10 ? 0 : 9 / k);

            if (c == 0) {
                sb.append(0).append('\n');
                continue;
            }

            // Let base = c+1
            long base = c + 1;

            // We want (c+1)^l * ((c+1)^(r-l) - 1) mod MOD
            long part1 = modPow(base, l);       // (c+1)^l % MOD
            long part2 = modPow(base, r - l);   // (c+1)^(r-l) % MOD

            long ans = part1 * ((part2 - 1 + MOD) % MOD) % MOD;
            sb.append(ans).append('\n');
        }

        System.out.print(sb);
    }
}