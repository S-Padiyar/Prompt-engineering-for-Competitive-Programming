import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 1_000_000_007;
    
    // Fast exponentiation modulo MOD
    static long modPow(long base, long exp) {
        long result = 1;
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
        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            long l = Long.parseLong(st.nextToken());
            long r = Long.parseLong(st.nextToken());
            long k = Long.parseLong(st.nextToken());

            // Compute m = floor(9 / k)
            long m = 9 / k;
            if (m == 0) {
                // No nonzero digits allowed => no valid n >= 1
                sb.append(0).append('\n');
                continue;
            }

            // Digit-length range:
            // if l==0, min length = 1, so d1 = 1 => d1-1 = 0
            // if l>0, min length = l+1, so d1-1 = l
            long d1minus1 = (l == 0 ? 0 : l);
            long d2 = r;

            long base = m + 1;  // we raise this to powers

            // Compute (m+1)^{d2} and (m+1)^{d1minus1}
            long term2 = modPow(base, d2);
            long term1 = modPow(base, d1minus1);

            // Answer is term2 - term1  (mod MOD)
            long ans = term2 - term1;
            if (ans < 0) ans += MOD;
            sb.append(ans).append('\n');
        }

        // Output all answers
        System.out.print(sb.toString());
    }
}