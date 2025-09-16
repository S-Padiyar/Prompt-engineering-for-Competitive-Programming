import java.io.*;
import java.util.*;

public class Main {
    static final int MAXN = 500_005;
    static int[] a = new int[MAXN], b = new int[MAXN];
    static int[] PA = new int[MAXN], PB = new int[MAXN];
    static int[] SA = new int[MAXN], SB = new int[MAXN];

    // gcd helper
    static int gcd(int x, int y) {
        while (y != 0) {
            int t = x % y;
            x = y;
            y = t;
        }
        return x;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            StringTokenizer st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= n; i++) a[i] = Integer.parseInt(st.nextToken());
            st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= n; i++) b[i] = Integer.parseInt(st.nextToken());

            // Build prefix gcds
            PA[0] = 0;
            PB[0] = 0;
            for (int i = 1; i <= n; i++) {
                PA[i] = gcd(PA[i - 1], a[i]);
                PB[i] = gcd(PB[i - 1], b[i]);
            }
            // Build suffix gcds (SA[i] = gcd(a[i..n]))
            SA[n + 1] = 0;
            SB[n + 1] = 0;
            for (int i = n; i >= 1; i--) {
                SA[i] = gcd(a[i], SA[i + 1]);
                SB[i] = gcd(b[i], SB[i + 1]);
            }

            // Extract runs of SA and SB: at most O(log) runs each
            ArrayList<Integer> saVal = new ArrayList<>(),
                             saStart = new ArrayList<>(),
                             saEnd = new ArrayList<>();
            {
                int last = -1, runStart = 1;
                for (int i = 1; i <= n + 1; i++) {
                    if (SA[i] != last) {
                        if (last != -1) {
                            saVal.add(last);
                            saStart.add(runStart);
                            saEnd.add(i - 1);
                        }
                        last = SA[i];
                        runStart = i;
                    }
                }
                // last run
                saVal.add(last);
                saStart.add(runStart);
                saEnd.add(n + 1);
            }
            ArrayList<Integer> sbVal = new ArrayList<>(),
                             sbStart = new ArrayList<>(),
                             sbEnd = new ArrayList<>();
            {
                int last = -1, runStart = 1;
                for (int i = 1; i <= n + 1; i++) {
                    if (SB[i] != last) {
                        if (last != -1) {
                            sbVal.add(last);
                            sbStart.add(runStart);
                            sbEnd.add(i - 1);
                        }
                        last = SB[i];
                        runStart = i;
                    }
                }
                sbVal.add(last);
                sbStart.add(runStart);
                sbEnd.add(n + 1);
            }

            // We'll build for b the dynamic "suffix starting at l" gcd‐runs
            // and same for a.  We keep them in small arrays.
            int[] xVal = new int[64], xR = new int[64];
            int[] tmpXVal = new int[64], tmpXR = new int[64];
            int xLen = 0;
            int[] yVal = new int[64], yR = new int[64];
            int[] tmpYVal = new int[64], tmpYR = new int[64];
            int yLen = 0;

            long bestSum = -1, ways = 0;

            // Process l from n down to 1
            for (int l = n; l >= 1; l--) {
                // ----- update runs for gcd(b[l..r]) -----
                int newXL = 0;
                tmpXVal[newXL] = b[l];
                tmpXR[newXL] = l;
                newXL++;
                for (int i = 0; i < xLen; i++) {
                    int g = gcd(b[l], xVal[i]);
                    if (g == tmpXVal[newXL - 1]) {
                        // extend run
                        tmpXR[newXL - 1] = xR[i];
                    } else {
                        tmpXVal[newXL] = g;
                        tmpXR[newXL] = xR[i];
                        newXL++;
                    }
                }
                // swap tmp -> xVal
                xLen = newXL;
                System.arraycopy(tmpXVal, 0, xVal, 0, xLen);
                System.arraycopy(tmpXR, 0, xR, 0, xLen);

                // ----- update runs for gcd(a[l..r]) -----
                int newYL = 0;
                tmpYVal[newYL] = a[l];
                tmpYR[newYL] = l;
                newYL++;
                for (int i = 0; i < yLen; i++) {
                    int g = gcd(a[l], yVal[i]);
                    if (g == tmpYVal[newYL - 1]) {
                        tmpYR[newYL - 1] = yR[i];
                    } else {
                        tmpYVal[newYL] = g;
                        tmpYR[newYL] = yR[i];
                        newYL++;
                    }
                }
                yLen = newYL;
                System.arraycopy(tmpYVal, 0, yVal, 0, yLen);
                System.arraycopy(tmpYR, 0, yR, 0, yLen);

                // The outside‐gcd parameters
                int P_l = PA[l - 1];
                int Q_l = PB[l - 1];

                // Build the runs of r for P = gcd(P_l, SA[r+1])
                int[] pVal = new int[64], pS = new int[64], pE = new int[64];
                int pLen = 0;
                for (int i = 0; i < saVal.size(); i++) {
                    int v = saVal.get(i);
                    int js = saStart.get(i), je = saEnd.get(i);
                    int rs = js - 1;      // r+1=js   =>   r=js-1
                    int re = je - 1;      // r+1=je   =>   r=je-1
                    if (rs < l) rs = l;
                    if (re > n) re = n;
                    if (rs > re) continue;
                    int g = gcd(P_l, v);
                    if (pLen > 0 && g == pVal[pLen - 1]) {
                        // merge
                        pE[pLen - 1] = re;
                    } else {
                        pVal[pLen] = g;
                        pS[pLen] = rs;
                        pE[pLen] = re;
                        pLen++;
                    }
                }

                // Build the runs of r for Q = gcd(Q_l, SB[r+1])
                int[] qVal = new int[64], qS = new int[64], qE = new int[64];
                int qLen = 0;
                for (int i = 0; i < sbVal.size(); i++) {
                    int v = sbVal.get(i);
                    int js = sbStart.get(i), je = sbEnd.get(i);
                    int rs = js - 1, re = je - 1;
                    if (rs < l) rs = l;
                    if (re > n) re = n;
                    if (rs > re) continue;
                    int g = gcd(Q_l, v);
                    if (qLen > 0 && g == qVal[qLen - 1]) {
                        qE[qLen - 1] = re;
                    } else {
                        qVal[qLen] = g;
                        qS[qLen] = rs;
                        qE[qLen] = re;
                        qLen++;
                    }
                }

                // We now have 4 small lists of runs over r in [l..n]:
                //   1) xVal[i],   run from r_start = (i==0?l:xR[i-1]+1)  to r_end = xR[i]
                //   2) yVal[i],   similarly
                //   3) pVal[i],   run from pS[i] to pE[i]
                //   4) qVal[i],   run from qS[i] to qE[i]
                //
                // We do a 4‐way merge of all the distinct run‐start positions,
                // then on each merged subrange [start..end] we know all four values
                // are constant.  We compute gcd(P,X)+gcd(Q,Y) once, multiply by
                // (end-start+1), and update (bestSum, ways).

                // Merge run‐starts in sorted order, unique
                int ix = 0, iy = 0, ip = 0, iq = 0;
                int[] Z = new int[256]; // will hold sorted unique run‐starts
                int zLen = 0, lastZ = -1;
                while (true) {
                    int nx = (ix < xLen ? (ix == 0 ? l : xR[ix - 1] + 1) : Integer.MAX_VALUE);
                    int ny = (iy < yLen ? (iy == 0 ? l : yR[iy - 1] + 1) : Integer.MAX_VALUE);
                    int np = (ip < pLen ? pS[ip] : Integer.MAX_VALUE);
                    int nq = (iq < qLen ? qS[iq] : Integer.MAX_VALUE);
                    int m = Math.min(Math.min(nx, ny), Math.min(np, nq));
                    if (m > n) break;
                    if (m != lastZ) {
                        Z[zLen++] = m;
                        lastZ = m;
                    }
                    if (nx == m) ix++;
                    if (ny == m) iy++;
                    if (np == m) ip++;
                    if (nq == m) iq++;
                }

                // Now walk those Z‐intervals
                int px = 0, py = 0, pp = 0, pq = 0;
                for (int i = 0; i < zLen; i++) {
                    int start = Z[i];
                    int end = (i + 1 < zLen ? Z[i + 1] - 1 : n);

                    // advance each pointer so it covers 'start'
                    while (px + 1 < xLen && (px + 1 == 0 ? l : xR[px] + 1) <= start) px++;
                    while (py + 1 < yLen && (py + 1 == 0 ? l : yR[py] + 1) <= start) py++;
                    while (pp + 1 < pLen && pS[pp + 1] <= start) pp++;
                    while (pq + 1 < qLen && qS[pq + 1] <= start) pq++;

                    int X = xVal[px], Y = yVal[py], P = pVal[pp], Q = qVal[pq];
                    int sum = gcd(P, X) + gcd(Q, Y);

                    long cnt = (end - start + 1);
                    if (sum > bestSum) {
                        bestSum = sum;
                        ways = cnt;
                    } else if (sum == bestSum) {
                        ways += cnt;
                    }
                }
            }

            sb.append(bestSum).append(" ").append(ways).append("\n");
        }
        System.out.print(sb);
    }
}