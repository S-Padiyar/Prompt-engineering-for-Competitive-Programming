import java.io.*;
import java.util.*;

public class Main {
  static class State {
    // t = death-time of the best merged run of this color so far
    // r = how many elements remain in that run exactly at time t (so next second they'd start disappearing).
    long t, r;
    State() { t = 0; r = 0; }
  }

  public static void main(String[] args) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    StringTokenizer tok = new StringTokenizer(in.readLine());
    int T = Integer.parseInt(tok.nextToken());

    StringBuilder out = new StringBuilder();
    while (T-- > 0) {
      tok = new StringTokenizer(in.readLine());
      int n = Integer.parseInt(tok.nextToken());

      long[] A = new long[n+1];
      int[]  B = new int[n+1];
      for (int i = 1; i <= n; i++){
        tok = new StringTokenizer(in.readLine());
        A[i] = Long.parseLong(tok.nextToken());
        B[i] = Integer.parseInt(tok.nextToken());
      }

      // We'll keep track of P[i] = strength of prefix up to i
      long[] P = new long[n+1];
      // And for each color, the last block index we saw of that color
      int[] last   = new int[n+1];
      // And each color's current merged-state
      State[] st   = new State[n+1];
      for (int c = 0; c <= n; c++) {
        last[c] = 0;
        st[c] = new State();
      }

      P[0] = 0;

      for (int i = 1; i <= n; i++){
        int  c = B[i];
        long a = A[i];

        // 1) The middle will die at time (P[i-1] - P[last[c]])
        long t_mid = P[i-1] - P[ last[c] ];

        // Option A: do not merge, this block alone dies at time a, residual = 0
        long bestT = a;
        long bestR = 0;

        // Option B: try to merge with the old state st[c]
        if (last[c] != 0) {
          State old = st[c];
          // both must still be alive at t_mid => t_mid < old.t  AND  t_mid < a
          if (t_mid < old.t && t_mid < a) {
            // merged death-time:
            long mergedDeath = old.t + a - t_mid;
            // at that moment there's nothing left (residual=0)
            bestT = Math.max(bestT, mergedDeath);
            bestR = 0;
          }
        }

        // save the new state for color c
        st[c].t = bestT;
        st[c].r = bestR;

        last[c] = i;

        // 2) The whole prefix's strength P[i] is either what it was, or improved by finishing
        //    the newly updated color-component later.
        P[i] = Math.max(P[i-1], bestT);
      }

      // print P[1..n]
      for (int i = 1; i <= n; i++) {
        out.append(P[i]).append(i==n?'\n':' ');
      }
    }

    System.out.print(out);
  }
}