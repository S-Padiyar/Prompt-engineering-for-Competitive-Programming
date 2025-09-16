import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(in.readLine());
        int t = Integer.parseInt(st.nextToken());

        // We'll accumulate all n across tests up to 2e5,
        // and do each test in O(n log n + n).
        while (t-- > 0) {
            st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            long m = Long.parseLong(st.nextToken());

            long[] a = new long[n], c = new long[n];
            st = new StringTokenizer(in.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }
            st = new StringTokenizer(in.readLine());
            for (int i = 0; i < n; i++) {
                c[i] = Long.parseLong(st.nextToken());
            }

            // Sort by petal‐count a[i]
            Integer[] idx = new Integer[n];
            for (int i = 0; i < n; i++) idx[i] = i;
            Arrays.sort(idx, Comparator.comparingLong(i -> a[i]));

            long best = 0;

            // Sweep through sorted flowers
            for (int _i = 0; _i < n; _i++) {
                int i = idx[_i];
                long v = a[i], cnt0 = c[i];

                // 1) Use only petal‐count = v
                long maxTakeV = Math.min(cnt0, m / v);
                best = Math.max(best, maxTakeV * v);

                // 2) If there's an adjacent v+1 type, do a 2‐type knapsack in O(1)
                if (_i + 1 < n) {
                    int j = idx[_i + 1];
                    if (a[j] == v + 1) {
                        long cnt1 = c[j];
                        // Y_hi = max # of (v+1) flowers we can even afford by cost
                        long Y_hi = Math.min(cnt1, m / (v + 1));

                        // Region 1: "we fill all cheap v‐flowers, then add as many expensive (v+1) as budget allows"
                        // That boundary y1 solves: (m - y*(v+1)) >= v*cnt0  =>  y <= (m - v*cnt0)/(v+1).
                        long y1 = -1;
                        if (m >= cnt0 * v) {
                            y1 = (m - cnt0 * v) / (v + 1);
                            if (y1 > cnt1) y1 = cnt1;
                        }
                        long f1 = 0;
                        if (y1 >= 0) {
                            f1 = cnt0 * v + y1 * (v + 1);
                        }

                        // Region 2: "we don't have enough cheap flowers to fill the remainder,
                        // we pick y of the expensive, then floor((m - y*(v+1))/v) of the cheap"
                        long Y_lo = (y1 >= 0 ? y1 + 1 : 0);
                        long f2 = 0;
                        if (Y_lo <= Y_hi) {
                            // If Y_hi - Y_lo + 1 >= v, we cover all residues mod v => we can hit total=m exactly
                            long length = Y_hi - Y_lo + 1;
                            if (length >= v) {
                                f2 = m;  // we can saturate the budget
                            } else {
                                // We want to minimize r = (m - y*(v+1)) mod v = (m - y) mod v
                                long t0 = m % v;
                                long z_lo = Y_lo % v;
                                long hiEnd = z_lo + length - 1;

                                long rMin;
                                if (hiEnd < v) {
                                    // no wrap
                                    long z_hi = hiEnd;
                                    if (t0 >= z_lo && t0 <= z_hi) {
                                        rMin = 0;
                                    } else if (t0 < z_lo) {
                                        rMin = t0 + v - z_lo;
                                    } else {
                                        rMin = t0 - z_hi;
                                    }
                                } else {
                                    // wrap around
                                    long z_hiWrap = (hiEnd) % v;
                                    if ((t0 >= z_lo && t0 < v) || (t0 >= 0 && t0 <= z_hiWrap)) {
                                        rMin = 0;
                                    } else {
                                        // t0 is between z_hiWrap and z_lo on the circle
                                        long r1 = t0 - z_hiWrap;          // from the lower segment
                                        long r2 = (t0 + v) - z_lo;        // from the upper segment
                                        rMin = Math.min(r1, r2);
                                    }
                                }
                                f2 = m - rMin;
                            }
                        }

                        // Also check the pure "all expensive first" corner
                        long yAllExp = Y_hi;
                        long rem = m - yAllExp * (v + 1);
                        long takeCheap = Math.min(cnt0, rem / v);
                        long f3 = yAllExp * (v + 1) + takeCheap * v;

                        best = Math.max(best, Math.max(f1, Math.max(f2, f3)));
                    }
                }
            }

            System.out.println(best);
        }
    }
}