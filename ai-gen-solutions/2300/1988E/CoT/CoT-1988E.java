import java.io.*;
import java.util.*;

public class Main {
    static final int MAXN = 1000000 + 5;
    static int[] a = new int[MAXN];
    static int[] L = new int[MAXN], R = new int[MAXN];
    static long[] wL = new long[MAXN], wR = new long[MAXN];
    static long[] preF = new long[MAXN], sufF = new long[MAXN];
    static long[] bitCntL = new long[MAXN], bitSumL = new long[MAXN];
    static long[] bitCntR = new long[MAXN], bitSumR = new long[MAXN];
    static int N;

    // Fenwick methods (1-based):
    static void fenwUpdate(long[] bit, int i, long delta) {
        for (; i <= N; i += i & -i) {
            bit[i] += delta;
        }
    }
    static long fenwSum(long[] bit, int i) {
        long s = 0;
        for (; i > 0; i -= i & -i) {
            s += bit[i];
        }
        return s;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));
        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            N = Integer.parseInt(br.readLine().trim());
            StringTokenizer st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= N; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            // 1) Compute preF[i] = f(a[1..i]) in O(n).
            {
                long running = 0;
                // stack of (value, count)
                ArrayDeque<long[]> stck = new ArrayDeque<>();
                preF[0] = 0;
                for (int i = 1; i <= N; i++) {
                    long cnt = 1;
                    while (!stck.isEmpty() && stck.peek()[0] >= a[i]) {
                        long[] top = stck.pop();
                        running -= top[0] * top[1];
                        cnt += top[1];
                    }
                    stck.push(new long[]{a[i], cnt});
                    running += (long)a[i] * cnt;
                    preF[i] = preF[i - 1] + running;
                }
            }

            // 2) Compute sufF[i] = f(a[i..N]) in O(n), reversed.
            {
                long running = 0;
                ArrayDeque<long[]> stck = new ArrayDeque<>();
                sufF[N + 1] = 0;
                for (int i = N; i >= 1; i--) {
                    long cnt = 1;
                    while (!stck.isEmpty() && stck.peek()[0] >= a[i]) {
                        long[] top = stck.pop();
                        running -= top[0] * top[1];
                        cnt += top[1];
                    }
                    stck.push(new long[]{a[i], cnt});
                    running += (long)a[i] * cnt;
                    sufF[i] = sufF[i + 1] + running;
                }
            }

            // 3) Compute next smaller to the right (R) and previous smaller to the left (L).
            {
                // L[i]: 1 + index of previous smaller.  If none, L[i] = 1.
                ArrayDeque<Integer> stck = new ArrayDeque<>();
                for (int i = 1; i <= N; i++) {
                    while (!stck.isEmpty() && a[stck.peek()] > a[i]) {
                        stck.pop();
                    }
                    int prev = stck.isEmpty() ? 0 : stck.peek();
                    L[i] = prev + 1;
                    stck.push(i);
                }
                // R[i]: index of next smaller minus 1.  If none, R[i] = N.
                stck.clear();
                for (int i = N; i >= 1; i--) {
                    while (!stck.isEmpty() && a[stck.peek()] > a[i]) {
                        stck.pop();
                    }
                    int nxt = stck.isEmpty() ? N+1 : stck.peek();
                    R[i] = nxt - 1;
                    stck.push(i);
                }
            }

            // 4) Precompute the two weight arrays:
            //    wL[i] = (i - L[i] + 1),   wR[i] = (R[i] - i + 1).
            for (int i = 1; i <= N; i++) {
                wL[i] = i - L[i] + 1;
                wR[i] = R[i] - i + 1;
            }

            // 5) We'll sweep i=1..N, maintaining
            //      Cross(i) = sum_{k < i < ℓ} wL[k]*wR[ℓ]*min(a[k],a[ℓ])
            //    via two Fenwicks over “values = a[*]” for the sets
            //      S_L = {1..i−1},  each with weight wL[k],
            //      S_R = {i+1..N}, each with weight wR[ℓ].
            //
            //    Fenwicks store two arrays:  cnt[v] = ∑ weights of elements with value=v,
            //                                 sum[v] = ∑ (value * weight).
            //    Then ∑_{value≤X} sum[v] + X*(totalCnt - ∑_{value≤X} cnt[v])
            //    is exactly ∑ weights[*] · min(value[*], X).

            // Clear Fenwicks
            for (int i = 1; i <= N; i++) {
                bitCntL[i] = bitSumL[i] = 0;
                bitCntR[i] = bitSumR[i] = 0;
            }

            // Initialize S_R = {2..N}.
            long totalWR = 0;
            for (int i = 2; i <= N; i++) {
                totalWR += wR[i];
                fenwUpdate(bitCntR, a[i], wR[i]);
                fenwUpdate(bitSumR, a[i], wR[i] * a[i]);
            }
            long totalWL = 0;         // empty S_L
            long cross = 0;

            long[] ans = new long[N+1];
            for (int i = 1; i <= N; i++) {
                // f(b_i) = preF[i-1] + sufF[i+1] + Cross(i)
                ans[i] = preF[i-1] + sufF[i+1] + cross;

                if (i < N) {
                    //  Remove i+1 from S_R => we must subtract all pairs with ℓ = i+1
                    long valR = a[i+1], wr = wR[i+1];

                    // deltaMinus = wr * ∑_{k ∈ S_L} wL[k]·min(a[k], valR)
                    long cL_le = fenwSum(bitCntL, valR);
                    long sL_le = fenwSum(bitSumL, valR);
                    long deltaMinus = wr * (sL_le + valR * (totalWL - cL_le));

                    // actually remove it
                    fenwUpdate(bitCntR, valR, -wr);
                    fenwUpdate(bitSumR, valR, -wr * valR);
                    totalWR -= wr;

                    // deltaPlus = wL[i] * ∑_{ℓ ∈ S_R} wR[ℓ]·min(a[ℓ], a[i])
                    long valL = a[i], wl = wL[i];
                    long cR_le = fenwSum(bitCntR, valL);
                    long sR_le = fenwSum(bitSumR, valL);
                    long deltaPlus = wl * (sR_le + valL * (totalWR - cR_le));

                    // add i into S_L
                    fenwUpdate(bitCntL, valL, wl);
                    fenwUpdate(bitSumL, valL, wl * valL);
                    totalWL += wl;

                    cross = cross - deltaMinus + deltaPlus;
                }
            }

            // 6) Print answers
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= N; i++) {
                sb.append(ans[i]).append(' ');
            }
            pw.println(sb.toString().trim());
        }

        pw.flush();
    }
}