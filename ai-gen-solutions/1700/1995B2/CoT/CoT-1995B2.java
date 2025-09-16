import java.io.*;
import java.util.*;

public class Main {
    static class Flower implements Comparable<Flower> {
        long a, c;
        Flower(long a, long c) { this.a = a; this.c = c; }
        public int compareTo(Flower o) {
            return Long.compare(this.a, o.a);
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter     pw = new PrintWriter(new OutputStreamWriter(System.out));
        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n    = Integer.parseInt(st.nextToken());
            long m   = Long.parseLong(st.nextToken());
            Flower[] f = new Flower[n];
            st = new StringTokenizer(br.readLine());
            long[] a = new long[n];
            for (int i = 0; i < n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                long ci = Long.parseLong(st.nextToken());
                f[i] = new Flower(a[i], ci);
            }
            Arrays.sort(f);

            // Track the best answer so far
            long ans = 0;
            // 1) Try using just one type
            for (int i = 0; i < n; i++) {
                long petals = f[i].a;
                long cnt    = f[i].c;
                // we can buy at most floor(m/petals) flowers of this type
                long take   = Math.min(cnt, m / petals);
                ans = Math.max(ans, take * petals);
            }

            // 2) Try every adjacent pair whose a differ by exactly 1
            for (int i = 0; i + 1 < n; i++) {
                if (f[i+1].a == f[i].a + 1) {
                    long x = f[i].a;      // cheaper coin size
                    long U = f[i].c;      // max count of x
                    long V = f[i+1].c;    // max count of x+1
                    ans = Math.max(ans, solvePair(x, U, V, m));
                }
            }

            pw.println(ans);
        }
        pw.flush();
    }

    /**
     * Solve the 2‐denomination bounded‐knapsack
     *    maximize S = b*x + e*(x+1)  subject to
     *      0<=b<=U,  0<=e<=V,  b*x + e*(x+1) <= m
     * in O(1) time by observing we either take all we can
     * up to capacity or we adjust by a small modulo‐residue argument.
     */
    static long solvePair(long x, long U, long V, long m) {
        long y = x + 1;

        // If even the full stock fits, take it all:
        //    U*x + V*y <= m  ⇒ answer = U*x + V*y
        long full = U * x + V * y;
        if (full <= m) {
            return full;
        }

        // B = max number of y‐coins we could ever afford by price
        long B = m / y;
        if (B > V) B = V;

        // Region1: for a from 0..A we have enough money to take U x‐coins for free,
        // so sum(a) = a*y + U*x, which is increasing in a.
        long A = -1;
        if (m >= U * x) {
            long tmp = (m - U*x) / y;
            A = Math.min(tmp, V);
        }
        long S1 = 0;
        if (A >= 0) {
            S1 = A * y + U * x;
        }
        // Region2: a runs from A+1..B, now b = floor((m – a*y)/x), so
        //  sum(a) = m – ((m – a*y) mod x).  We just need the minimal residue
        //  in that interval.  If the interval length >= x or the minimal
        //  residue hits 0, we achieve exactly m; otherwise we subtract the
        //  smallest possible positive remainder.
        long start = Math.max(A + 1, 0L);
        long S2 = 0;
        if (start <= B) {
            long K = B - start + 1;   // interval length
            long r0      = m % x;
            long startM  = start % x;
            // r_start = (m – start*y) mod x = (r0 – start mod x) mod x
            long rStart  = (r0 - startM) % x;
            if (rStart < 0) rStart += x;

            if (K >= x || rStart < K) {
                // we can cover a residue=0 somewhere ⇒ sum = m
                S2 = m;
            } else {
                // minimal residue = rStart – (K-1)
                long rmin = rStart - (K - 1);
                S2 = m - rmin;
            }
        }

        return Math.max(S1, S2);
    }
}