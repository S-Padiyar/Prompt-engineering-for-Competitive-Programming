import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        
        int T = Integer.parseInt(br.readLine().trim());
        while (T-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            
            // Special case n=1
            if (n == 1) {
                // For n=1 any a=2..10000 with b=a-1 works
                int cnt = 10000 - 1;  // a=2..10000 => 9999 pairs
                out.println(cnt);
                for (int a = 2; a <= 10000; a++) {
                    out.println(a + " " + (a - 1));
                }
                continue;
            }
            
            // For n>1
            String strn = Integer.toString(n);
            int d = strn.length();
            int denom = n - d;  // must be >0 since n>1 => n >= 2, d <=3, so n-d >0 for n>1.
            
            // We'll build up the infinite repetition of strn, but only as far as needed.
            StringBuilder rep = new StringBuilder();
            
            // Precompute the threshold for stopping:
            BigInteger bigDenom = BigInteger.valueOf(denom);
            BigInteger limitNum = bigDenom.multiply(BigInteger.valueOf(10000));
            
            // To avoid duplicates, we store in a set
            Set<Long> seen = new HashSet<>();
            List<int[]> ans = new ArrayList<>();
            
            for (int t = 1; ; t++) {
                // ensure rep.length() >= t by appending strn if necessary
                while (rep.length() < t) {
                    rep.append(strn);
                }
                // take the first t digits
                String prefix = rep.substring(0, t);
                BigInteger A = new BigInteger(prefix);
                
                // num = A - t
                BigInteger num = A.subtract(BigInteger.valueOf(t));
                if (num.signum() < 0) {
                    // too small; go on
                } else {
                    // see if divisible by denom
                    BigInteger[] divmod = num.divideAndRemainder(bigDenom);
                    if (divmod[1].signum() == 0) {
                        BigInteger aBig = divmod[0];
                        if (aBig.signum() > 0 && aBig.compareTo(BigInteger.valueOf(10000)) <= 0) {
                            int a = aBig.intValue();
                            int b = a*d - t;
                            if (b >= 1 && b <= 10000 && b <= a*n) {
                                long code = (((long)a) << 32) | (long)b;
                                if (!seen.contains(code)) {
                                    seen.add(code);
                                    ans.add(new int[]{a, b});
                                }
                            }
                        }
                    }
                }
                
                // if num > denom * 10000 then further t will only increase A => a > 10000
                if (num.compareTo(limitNum) > 0) {
                    break;
                }
            }
            
            // sort by 'a' ascending, then by 'b'
            ans.sort(Comparator.<int[]>comparingInt(x->x[0]).thenComparingInt(x->x[1]));
            
            // output
            out.println(ans.size());
            for (int[] p : ans) {
                out.println(p[0] + " " + p[1]);
            }
        }
        out.flush();
    }
}