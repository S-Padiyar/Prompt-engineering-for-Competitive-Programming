import java.io.*;
import java.util.*;

public class Main {
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
        FastReader(InputStream in) {
            br = new BufferedReader(new InputStreamReader(in));
        }
        String next() {
            while (st == null || !st.hasMoreTokens()) {
                try {
                    String line = br.readLine();
                    if (line == null) return null;
                    st = new StringTokenizer(line);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return st.nextToken();
        }
        int nextInt() { return Integer.parseInt(next()); }
        long nextLong() { return Long.parseLong(next()); }
    }

    public static void main(String[] args) {
        FastReader in = new FastReader(System.in);
        PrintWriter out = new PrintWriter(System.out);

        int t = in.nextInt();
        while (t-- > 0) {
            int n = in.nextInt();
            int m = in.nextInt();
            long v = in.nextLong();
            long[] a = new long[n+1];
            for (int i = 1; i <= n; i++) {
                a[i] = in.nextLong();
            }

            // Compute prefix sums for quick range sums
            long[] pref = new long[n+1];
            pref[0] = 0;
            for (int i = 1; i <= n; i++) {
                pref[i] = pref[i-1] + a[i];
            }

            // P[i] = maximum # of disjoint segments of sum>=v in a[1..i]
            int[] P = new int[n+1];
            {
                long curSum = 0;
                int cnt = 0;
                for (int i = 1; i <= n; i++) {
                    curSum += a[i];
                    if (curSum >= v) {
                        cnt++;
                        curSum = 0;
                    }
                    P[i] = cnt;
                }
            }

            // S[i] = maximum # of disjoint segments of sum>=v in a[i..n]
            // We'll build S[1..n], plus S[n+1]=0 for convenience.
            int[] S = new int[n+2];
            {
                long curSum = 0;
                int cnt = 0;
                for (int i = n; i >= 1; i--) {
                    curSum += a[i];
                    if (curSum >= v) {
                        cnt++;
                        curSum = 0;
                    }
                    S[i] = cnt;
                }
                S[n+1] = 0;
            }

            // If even the full array cannot produce >=m segments, answer = -1
            if (P[n] < m) {
                out.println(-1);
                continue;
            }

            long ans = Long.MIN_VALUE;
            // Try Alice's piece starting at l=1..n
            for (int l = 1; l <= n; l++) {
                int already = P[l-1];
                int need = m - already;
                int r;
                if (need <= 0) {
                    // creatures can be made entirely outside by the full suffix
                    r = n;
                } else {
                    // binary search for largest i in [1..n+1] with S[i] >= need
                    int low = 1, high = n+1, best = 0;
                    while (low <= high) {
                        int mid = (low + high) >>> 1;
                        if (S[mid] >= need) {
                            best = mid;
                            low = mid + 1;
                        } else {
                            high = mid - 1;
                        }
                    }
                    // best=0 means no suffix index had S[i]>=need => impossible for this l
                    if (best == 0) {
                        continue;
                    }
                    // best is the start of the suffix; Alice's r = best-1
                    r = best - 1;
                }
                // we must have r>=l for a non-empty piece
                if (r >= l) {
                    long aliceSum = pref[r] - pref[l-1];
                    if (aliceSum > ans) {
                        ans = aliceSum;
                    }
                }
            }

            // We know P[n]>=m, so Alice can always get an empty piece of sum=0 at worst.
            ans = Math.max(ans, 0L);

            out.println(ans);
        }

        out.flush();
    }
}