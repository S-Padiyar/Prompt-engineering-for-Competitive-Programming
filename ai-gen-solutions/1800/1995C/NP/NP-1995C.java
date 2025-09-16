import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int t = Integer.parseInt(br.readLine().trim());
        final double EPS = 1e-12;
        // Precompute 1/ln(2) for log2 conversions
        final double INV_LN2 = 1.0 / Math.log(2.0);

        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            st = new StringTokenizer(br.readLine());
            long[] a = new long[n];
            for (int i = 0; i < n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }

            long ops = 0;
            boolean impossible = false;

            // Initialize prevLog = ln(a[0]) or 0 if a[0]==1
            double prevLog = (a[0] == 1 ? 0.0 : Math.log(a[0]));

            for (int i = 1; i < n; i++) {
                if (a[i] == 1) {
                    // We can never raise 1.  If prevLog > 0, we cannot match or exceed.
                    if (prevLog > EPS) {
                        impossible = true;
                        break;
                    }
                    // else prevLog stays 0
                } else {
                    // a[i] >= 2
                    double A = Math.log(a[i]);
                    if (A + EPS >= prevLog) {
                        // no squaring needed
                        prevLog = A;
                    } else {
                        // we need k so that 2^k * A >= prevLog
                        double ratio = prevLog / A;
                        // compute log2(ratio)
                        double lg2 = Math.log(ratio) * INV_LN2;
                        // take ceil, with a small epsilon to avoid fp-errors
                        int k = (int) Math.ceil(lg2 - 1e-15);
                        if (k < 1) k = 1;

                        ops += k;
                        // new prevLog = A * 2^k
                        // Math.scalb(x,n) == x * 2^n
                        prevLog = Math.scalb(A, k);
                    }
                }
            }

            if (impossible) {
                sb.append("-1\n");
            } else {
                sb.append(ops).append('\n');
            }
        }

        System.out.print(sb);
    }
}