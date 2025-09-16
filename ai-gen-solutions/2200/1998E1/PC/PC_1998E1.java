import java.io.*;
import java.util.*;

public class Main {
  public static void main(String[] args) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    StringTokenizer st = new StringTokenizer(in.readLine());
    int t = Integer.parseInt(st.nextToken());
    StringBuilder out = new StringBuilder();

    while (t-- > 0) {
      st = new StringTokenizer(in.readLine());
      int n = Integer.parseInt(st.nextToken());
      // (x = n in this easy version, so we only need f(n))
      long[] a = new long[n+2];
      st = new StringTokenizer(in.readLine());
      for (int i = 1; i <= n; i++) {
        a[i] = Long.parseLong(st.nextToken());
      }

      // 1) prefix sums
      long[] pref = new long[n+2];
      for (int i = 1; i <= n; i++) {
        pref[i] = pref[i-1] + a[i];
      }
      // 2) suffix sums
      long[] suff = new long[n+3];
      for (int i = n; i >= 1; i--) {
        suff[i] = suff[i+1] + a[i];
      }

      // 3) maxT[i] = max_{k≤i} (a[k] + pref[k])
      long[] maxT = new long[n+2];
      maxT[1] = a[1] + pref[1];
      for (int i = 2; i <= n; i++) {
        long val = a[i] + pref[i];
        maxT[i] = Math.max(maxT[i-1], val);
      }

      // 4) maxV[i] = max_{k≥i} (a[k] - pref[k-1])
      long[] maxV = new long[n+3];
      maxV[n+1] = Long.MIN_VALUE;
      for (int i = n; i >= 1; i--) {
        long v = a[i] - pref[i-1];
        maxV[i] = Math.max(v, maxV[i+1]);
      }

      int cnt = 0;
      for (int j = 1; j <= n; j++) {
        // compute minimal needed strengths on each side
        long Lneed = 0;
        if (j > 1) {
          Lneed = maxT[j-1] - pref[j-1];
          if (Lneed < 0) Lneed = 0;
        }
        long Rneed = 0;
        if (j < n) {
          Rneed = pref[j] + maxV[j+1];
          if (Rneed < 0) Rneed = 0;
        }

        // A) eat left first, then right
        boolean canA = (a[j] >= Lneed)
                    && (a[j] + pref[j-1] >= Rneed);
        // B) eat right first, then left
        boolean canB = (a[j] >= Rneed)
                    && (a[j] + suff[j+1] >= Lneed);

        if (canA || canB) {
          cnt++;
        }
      }

      out.append(cnt).append("\n");
    }

    System.out.print(out);
  }
}