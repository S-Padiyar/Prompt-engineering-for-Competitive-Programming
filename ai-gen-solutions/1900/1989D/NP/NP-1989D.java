import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader();
        int n = in.nextInt();
        int m = in.nextInt();

        // Read a[i], b[i], compute d[i] = a[i] - b[i]
        // We only care for each distinct d = 1..1e6 the minimal a.
        final int DMAX = 1000000;
        final int INF = Integer.MAX_VALUE;
        int[] startCost = new int[DMAX+1];
        Arrays.fill(startCost, INF);

        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = in.nextInt();
        }
        for (int i = 0; i < n; i++) {
            int b = in.nextInt();
            int d = a[i] - b;  // guaranteed > 0
            if (a[i] < startCost[d]) {
                startCost[d] = a[i];
            }
        }

        // Find global minimal net-cost and its minimal start cost
        int Dmin = -1, Amin = INF;
        for (int d = 1; d <= DMAX; d++) {
            if (startCost[d] < INF) {
                if (Dmin < 0 || d < Dmin) {
                    Dmin = d;
                    Amin = startCost[d];
                }
            }
        }

        // If no class at all (should not happen under problem constraints),
        // every pile yields 0 XP.
        if (Dmin < 0) {
            // read c[] and print 0
            for (int j = 0; j < m; j++) {
                in.nextInt();
            }
            System.out.println(0);
            return;
        }

        // A = upper bound on leftover after doing as many Dmin-cycles as possible
        int A = Amin + Dmin - 1;

        // Gather pairs (startCost[d], d) for d with startCost[d] <= A
        ArrayList<int[]> pairs = new ArrayList<>();
        for (int d = 1; d <= DMAX; d++) {
            if (startCost[d] <= A) {
                pairs.add(new int[]{ startCost[d], d });
            }
        }
        // Sort by startCost ascending
        pairs.sort(Comparator.comparingInt(x -> x[0]));

        // Build min_d_for_R[0..A]
        int[] min_d_for_R = new int[A+1];
        int p = 0;
        int bestD = INF;
        for (int R = 0; R <= A; R++) {
            while (p < pairs.size() && pairs.get(p)[0] == R) {
                bestD = Math.min(bestD, pairs.get(p)[1]);
                p++;
            }
            min_d_for_R[R] = bestD;
        }

        // DP for leftover: dpRem[R] = max #cycles from leftover R
        int[] dpRem = new int[A+1];
        dpRem[0] = 0;
        for (int R = 1; R <= A; R++) {
            int d0 = min_d_for_R[R];
            if (d0 == INF) {
                dpRem[R] = 0;
            } else {
                dpRem[R] = 1 + dpRem[R - d0];
            }
        }

        // Process each pile and accumulate XP
        long totalXP = 0;
        for (int j = 0; j < m; j++) {
            long C = in.nextLong();
            long kMain = 0;
            long rem = C;
            if (C >= Amin) {
                // do as many Dmin-cycles as we can
                kMain = ((C - Amin) / Dmin) + 1;
                rem = C - kMain * Dmin;
            }
            // now rem <= A
            int R = (int) rem; 
            long kTail = dpRem[R];
            long cycles = kMain + kTail;
            totalXP += 2L * cycles;  // each cycle = 2 XP
        }

        System.out.println(totalXP);
    }

    // Fast I/O
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;

        FastReader() {
            br = new BufferedReader(new InputStreamReader(System.in));
        }
        String next() throws IOException {
            while (st == null || !st.hasMoreTokens()) {
                String s = br.readLine();
                if (s == null) return null;
                st = new StringTokenizer(s);
            }
            return st.nextToken();
        }
        int nextInt() throws IOException {
            return Integer.parseInt(next());
        }
        long nextLong() throws IOException {
            return Long.parseLong(next());
        }
    }
}