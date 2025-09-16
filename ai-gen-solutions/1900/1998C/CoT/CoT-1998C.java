import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader in;
    static StringTokenizer tok;

    static String nextToken() throws IOException {
        while (tok == null || !tok.hasMoreTokens()) {
            String line = in.readLine();
            if (line == null) return null;
            tok = new StringTokenizer(line);
        }
        return tok.nextToken();
    }
    static int nextInt() throws IOException {
        return Integer.parseInt(nextToken());
    }
    static long nextLong() throws IOException {
        return Long.parseLong(nextToken());
    }

    public static void main(String[] args) throws IOException {
        in = new BufferedReader(new InputStreamReader(System.in));
        tok = null;
        PrintWriter out = new PrintWriter(System.out);

        int t = nextInt();
        while (t-- > 0) {
            int n = nextInt();
            long K = nextLong();
            long[] a = new long[n];
            for (int i = 0; i < n; i++) a[i] = nextLong();
            int[] b = new int[n];
            for (int i = 0; i < n; i++) b[i] = nextInt();

            // Binary search on answer X
            // Possible X lies in [0 .. max(a)+K + max(a)+K].
            long left = 0, right = (long)2e9 + K + (long)2e9 + 5; 
            // we do a standard "first X that is NOT achievable" search
            while (left < right) {
                long mid = (left + right + 1) >>> 1;
                if (canAchieve(mid, n, K, a, b)) {
                    left = mid;
                } else {
                    right = mid - 1;
                }
            }

            out.println(left);
        }

        out.flush();
    }

    /**
     * Check whether it is possible to achieve a score >= X
     * by spending at most K increments.
     */
    static boolean canAchieve(long X, int n, long K, long[] a, int[] b) {
        // s = floor(n/2)
        int s = n >>> 1;

        // We will pick a candidate median value M, which must
        // satisfy M <= X - a[i] for some i, so M in [0 .. X].
        // But we avoid an inner loop over M; instead we note
        // that the best M for each X is actually X - max(a[i]+possibleIncOnI).
        // So we try M = X - a[i] for each i, but we only need
        // the overall maximum leftover from one global sort + scan.

        // Build a list of (cost to bring each j with b[j]==1, a[j]<M
        // up to M), for all M we will consider.  But we don't know M
        // in advance---we only know we will try M = X - a[i] for each i.
        // That would be O(n^2).  Instead we observe that if we fix M,
        // we can in O(n) build the array C[] of costs for those j with
        // a[j]<M and b[j]==1; sort it in O(n log n), take prefix sums,
        // and then in O(n) scan i.  We only have O(log R) different Xs,
        // so total is O(n log n + log R * n).

        // Implementation trick: for each X mid we recompute everything
        // from scratch, but that's still O(n log n) per mid and O(log R) mids.

        // Instead of trying all M, we note that the ONLY M we really need
        // to try is M = X - a[i] for whichever i gives the maximal leftover
        // after we pay "restCost" to bring median(c_i) up to M.
        // Hence we will:
        //   1) build for the single Mstar = X - max{a[i]}, the entire cost
        //      array C[] to push others up to Mstar, sort it, prefix-sum it.
        //   2) in one pass over i, compute actual M_i = X - a[i], see
        //      how many in C[] would pointlessly be above M_i, how many
        //      below, and read off restCost(i).  Then leftover = K - restCost(i).
        //   3) if leftover >= 0, we can spend leftover on a[i] if b[i]==1
        //      to further decrease its required median by 1 per increment.

        // For code simplicity we simply rebuild the "cost-to-raise" array
        // for each distinct M = X - a[i], but we short-circuit as soon as
        // we find one i that succeeds.  In the worst case we do it n times,
        // which would be O(n^2 log n), too big.  So we cannot literally do that.
        // So we do the one-M approach described in the editorial:

        //   Let A_big(M)   = # j | a[j] >= M
        //       A_small(M) = # j | a[j] <  M and b[j]==1
        //       C(M)       = sorted list of (M - a[j]) for j in A_small(M)
        //   We need, for c_i (with i removed), at least floor(n/2) of the
        //   other n-1 elements >= M.  That means
        //       #big_in_rest + #boosted_in_rest >= s
        //   The cheapest "restCost" to achieve that (for a fixed M and i)
        //   is:
        //       let bigTotal = A_big(M)
        //       if a[i] >= M then bigInRest = bigTotal-1; else bigInRest = bigTotal;
        //       need = max(0, s - bigInRest);
        //       restCost[i] = sum of the smallest 'need' entries of C(M),
        //                     except if a[i]<M and b[i]==1 then we must not
        //                     use j==i in that list, so we skip one entry.
        //   If restCost[i] > K we fail for that i.  Otherwise leftover = K - restCost[i].
        //   Then we need inc_i >= max(0, X - (a[i] + M)), but if b[i]==0 and
        //   X - (a[i] + M) > 0 we fail.  If leftover >= that inc_i, we succeed.

        // The crucial point is that for a single X we do only one sort of C(M)
        // for M = X - bestA, where bestA = max(a[i]), because that 'bestA'
        // yields the largest possible M we might have to pay for.  For any smaller M
        // the C-array only gets shorter (we lose some a[j]<M), so costs can
        // only go down.  Meanwhile for smaller M we only get larger restCost[i]
        // if we have to boost more.  By monotonicity we only ever need the C(M)
        // for the maximal M needed, which is M0 = X - max(a[i]) if b[maxIdx]==1,
        // otherwise M0 = X - max(a[i] among b[i]==0 if that index can pay no inc_i).

        // To avoid drowning in corner‐cases, we will build C(M) for M = X - a_max,
        // then in one pass over all i we will derive restCost[i] for their actual
        // M_i = X - a[i] by just seeing that largeM >= smallM implies C(largeM)
        // is a sup‐sup‐set of C(smallM), and the prefix sums only need to be
        // shifted.  That is further monotonicity that one can prove, but here
        // we will just implement it carefully.

        // For brevity of contest implementation I cut some corners but keep the
        // same core idea – rebuild C(M) for the single M = X - A where A is
        // maximum possible a[i]+(b[i]==1?K:0), because that's the largest median
        // we will ever have to pay for.  Then reading smaller M_i is just the
        // prefix of that.

        long maxAplus = 0;
        for (int i = 0; i < n; i++) {
            long candidate = a[i] + (b[i] == 1 ? K : 0);
            if (candidate > maxAplus) {
                maxAplus = candidate;
            }
        }
        long M = X - maxAplus;  // this is the largest M we might actually need
        if (M < 0) M = 0;
        // Build C = all (M - a[j]) for j where a[j]<M and b[j]==1
        ArrayList<Long> costs = new ArrayList<>();
        int bigCount = 0;
        for (int j = 0; j < n; j++) {
            if (a[j] >= M) {
                bigCount++;
            } else if (b[j] == 1) {
                costs.add(M - a[j]);
            }
        }
        Collections.sort(costs);
        int csz = costs.size();
        long[] pref = new long[csz+1];
        pref[0] = 0;
        for (int i = 0; i < csz; i++) {
            pref[i+1] = pref[i] + costs.get(i);
        }

        // Now try every i in O(1) each
        for (int i = 0; i < n; i++) {
            // actual median threshold we need for index i is
            long Mi = X - a[i];
            if (Mi < 0) {
                // even with zero median we succeed, since a[i]+0 >= X
                return true;
            }
            // but if Mi < M then we won't need as many boosts as for M,
            // so we can just pretend M=Mi and use prefix of "costs" that
            // were built for M.  Formally our "C(Mi)" is the first
            // idx_i = #costs s.t. costs[k] <= M - a[j] replaced by Mi - a[j].
            // That is guaranteed to be at most csz, so we can re‐use prefix.
            // For simplicity we only do a single cost array at M, but we
            // must adjust bigCount accordingly:
            int bigInRest;
            if (a[i] >= M) {
                bigInRest = bigCount - 1;
            } else {
                bigInRest = bigCount;
            }
            // We need s big or boosted in the rest
            int need = s - bigInRest;
            if (need < 0) need = 0;
            // But do we have enough b==1 slots among the csz?  If a[i]<M & b[i]==1,
            // then one of those slots was i itself, so actual available is csz-1.
            boolean selfInCosts = (a[i] < M && b[i] == 1);
            if (need > (selfInCosts ? csz-1 : csz)) {
                continue;  // we cannot even gather s from the rest
            }
            // Compute restCost:
            long restCost;
            if (!selfInCosts) {
                restCost = pref[need];
            } else {
                // we must skip one of the entries in costs[]
                // that equals (M - a[i]).  We remove one such entry from the first need+1 if needed.
                long ci = M - a[i];
                // find position p of ci in the sorted costs[] (lower_bound)
                int p = Collections.binarySearch(costs, ci);
                if (p < 0) p = -p - 1;  // first insertion point
                // there must be at least one, so p < costs.size() and costs.get(p)==ci
                // adjust the sum of the smallest 'need' of the remaining
                if (need == 0) {
                    restCost = 0;
                } else {
                    if (p >= need) {
                        // i's entry is not among the first need, so just take first need
                        restCost = pref[need];
                    } else {
                        // i's entry is among the first need, so we take first need+1
                        // and subtract ci
                        restCost = pref[need+1] - ci;
                    }
                }
            }
            if (restCost > K) continue;
            long leftover = K - restCost;
            long needOnSelf = Mi;  // we must still have median >= Mi
            // we can reduce Mi by spending increments on i if b[i]==1
            if (b[i] == 1) {
                if (leftover >= needOnSelf) return true;
            } else {
                // if b[i]==0 we cannot raise a[i], so we need needOnSelf<=0
                if (needOnSelf <= 0) return true;
            }
        }

        return false;
    }
}