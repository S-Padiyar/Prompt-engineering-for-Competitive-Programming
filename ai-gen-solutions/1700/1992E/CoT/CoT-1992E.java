import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));

        int t = Integer.parseInt(in.readLine().trim());
        for (int _case = 0; _case < t; _case++) {
            int n = Integer.parseInt(in.readLine().trim());
            String s = String.valueOf(n);
            int L = s.length();

            // Special case n=1
            if (n == 1) {
                // All a=2..10000, b=a-1
                out.write("9999\n");
                for (int a = 2; a <= 10000; a++) {
                    out.write(a + " " + (a - 1) + "\n");
                }
                continue;
            }

            int delta = n - L;  // must be >0 for n>1
            BigInteger BIG_DELTA = BigInteger.valueOf(delta);
            int maxA = 10000, maxB = 10000;

            // We'll build prefixes of the infinite repetition of s
            List<int[]> ans = new ArrayList<>();
            BigInteger prefixVal = BigInteger.ZERO;
            // We'll cycle through digits of s
            char[] digits = s.toCharArray();
            int pos = 0;

            // A reasonable cut-off: as soon as (P(k)-k) > delta*10000, further a's >10000
            BigInteger limit = BIG_DELTA.multiply(BigInteger.valueOf(maxA));

            // Try prefix lengths k = 1,2,3,...
            for (int k = 1; k <= 200; k++) {
                // Extend prefix by one digit
                prefixVal = prefixVal.multiply(BigInteger.TEN)
                                     .add(BigInteger.valueOf(digits[pos] - '0'));
                pos = (pos + 1) % L;

                BigInteger numer = prefixVal.subtract(BigInteger.valueOf(k));
                // If numer < 0, a would be negative or zero → skip
                if (numer.signum() <= 0) {
                    continue;
                }
                // If numer > delta*10000, we can stop
                if (numer.compareTo(limit) > 0) {
                    break;
                }

                // Check divisibility
                if (numer.mod(BIG_DELTA).equals(BigInteger.ZERO)) {
                    BigInteger aBig = numer.divide(BIG_DELTA);
                    int a = aBig.intValue();
                    if (a >= 1 && a <= maxA) {
                        int b = a * L - k;
                        if (b >= 1 && b <= maxB) {
                            // Valid pair
                            ans.add(new int[]{a, b});
                        }
                    }
                }
            }

            // Sort by increasing a
            ans.sort(Comparator.comparingInt(x -> x[0]));

            // Output
            out.write(ans.size() + "\n");
            for (int[] p : ans) {
                out.write(p[0] + " " + p[1] + "\n");
            }
        }
        out.flush();
    }
}