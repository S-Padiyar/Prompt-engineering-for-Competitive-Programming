import java.io.*;
import java.util.*;

public class Main {
  public static void main(String[] args) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    PrintWriter out = new PrintWriter(System.out);

    int t = Integer.parseInt(in.readLine().trim());
    while (t-- > 0) {
      int n = Integer.parseInt(in.readLine().trim());
      long[] a = new long[n];
      StringTokenizer st = new StringTokenizer(in.readLine());
      long S = 0;
      for (int i = 0; i < n; i++) {
        a[i] = Long.parseLong(st.nextToken());
        S += a[i];
      }

      // 1) The only possible target k is at most floor(S/n).
      long k = S / n;

      // 2) Build b[i] = a[i] - k.
      long[] b = new long[n];
      for (int i = 0; i < n; i++) {
        b[i] = a[i] - k;
      }

      // 3) Solve the cyclic system:
      //      - x[i-1] + 2*x[i] = b[i],  for i=0..n-1  (with x[-1] = x[n-1]).
      //
      // We'll do the standard trick:
      //   x[i] = alpha[i] * x[0] + beta[i],
      // then enforce the i=0 equation to find x[0].

      double[] alpha = new double[n];
      double[] beta  = new double[n];

      // i = 0 row: 2*x[0] - x[n-1] = b[0]
      // treat x[n-1] as if it were an unknown multiple of x[0], we start
      // the forward sweep at i=1.
      alpha[0] = 1.0;   // x[0] = 1*x[0] + 0
      beta[0]  = 0.0;

      // Forward sweep for i=1..n-1
      //    -x[i-1] + 2 x[i] = b[i]
      // =>  x[i] = ( b[i] + x[i-1] ) / 2
      for (int i = 1; i < n; i++) {
        alpha[i] = alpha[i-1] / 2.0;
        beta[i]  = (beta[i-1] + b[i]) / 2.0;
      }

      // Now the closure at i=0 gives:
      //    2*x[0] - x[n-1] = b[0]
      //    2*x0 - [ alpha[n-1]*x0 + beta[n-1] ] = b[0]
      // => x0 * (2 - alpha[n-1]) = b[0] + beta[n-1]
      double denom = 2.0 - alpha[n-1];
      double numer = b[0] + beta[n-1];

      // If denom is zero or numer/denom not an integer => no integral solution.
      if (Math.abs(denom) < 1e-14) {
        out.println(-1);
        continue;
      }
      double x0d = numer / denom;

      // Check x0d is very close to an integer:
      long x0 = Math.round(x0d);
      if (Math.abs(x0d - x0) > 1e-8) {
        // Not integral
        out.println(-1);
        continue;
      }

      // Reconstruct all x's and check nonnegativity + integer
      boolean ok = true;
      long[] x = new long[n];
      x[0] = x0;
      for (int i = 1; i < n; i++) {
        double xd = alpha[i] * x0 + beta[i];
        long xi = Math.round(xd);
        if (Math.abs(xd - xi) > 1e-8 || xi < 0) {
          ok = false;
          break;
        }
        x[i] = xi;
      }
      if (!ok || x[n-1] < 0) {
        out.println(-1);
      } else {
        // Everything is fine: the minimal m = sum x[i].
        long m = 0;
        for (long xi : x) m += xi;
        out.println(m);
      }
    }
    out.flush();
  }
}