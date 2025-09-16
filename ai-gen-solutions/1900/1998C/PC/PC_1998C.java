import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader in;
    static StringTokenizer tok;

    static String next() throws IOException {
        while (tok == null || !tok.hasMoreTokens()) {
            String line = in.readLine();
            if (line == null) return null;
            tok = new StringTokenizer(line);
        }
        return tok.nextToken();
    }

    static int nextInt() throws IOException {
        return Integer.parseInt(next());
    }
    static long nextLong() throws IOException {
        return Long.parseLong(next());
    }

    // Returns first index i in arr[] such that arr[i] >= key,
    // or arr.length if all elements < key.
    static int lowerBound(long[] arr, long key) {
        int lo = 0, hi = arr.length;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (arr[mid] < key) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }

    public static void main(String[] args) throws IOException {
        in = new BufferedReader(new InputStreamReader(System.in));
        tok = null;

        int t = nextInt();
        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            int n = nextInt();
            long k = nextLong();
            long[] a = new long[n];
            int[] b = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = nextLong();
            }
            for (int i = 0; i < n; i++) {
                b[i] = nextInt();
            }

            // Sort a copy so we can do medians and >= d counts quickly
            long[] all = a.clone();
            Arrays.sort(all);

            // K = how many elements must be >= d to raise the median of an (n-1)-array
            int K = n - (n/2);

            // Precompute the two key positions in 'all' for median-with-one-removed:
            // Mth = all[floor(n/2)-1], M1th = all[floor(n/2)]
            long Mth  = all[n/2 - 1];
            long M1th = all[n/2];

            // Case A preprocessing:
            // maxBaseIfB1 = max over i with b[i]==1 of (a[i] + median(c_i))
            long maxBaseIfB1 = Long.MIN_VALUE;
            // Case B preprocessing:
            // amax0 = max a[i] among those with b[i]==0
            long amax0 = Long.MIN_VALUE;

            for (int i = 0; i < n; i++) {
                long medCi = (a[i] <= Mth ? M1th : Mth);
                if (b[i] == 1) {
                    maxBaseIfB1 = Math.max(maxBaseIfB1, a[i] + medCi);
                } else {
                    amax0 = Math.max(amax0, a[i]);
                }
            }

            // Build the list of raisable values (those with b[i]==1),
            // sort them, build prefix sums.
            ArrayList<Long> list = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if (b[i] == 1) list.add(a[i]);
            }
            Collections.sort(list);
            int R = list.size();
            long[] rais = new long[R];
            long[] pref = new long[R+1];
            for (int i = 0; i < R; i++) {
                rais[i] = list.get(i);
                pref[i+1] = pref[i] + rais[i];
            }

            // A predicate that checks if we can achieve score >= D
            java.util.function.LongPredicate canAchieve = (D) -> {
                // --- Case A: pick i with b[i]==1, invest all k in a[i]
                if (maxBaseIfB1 != Long.MIN_VALUE && maxBaseIfB1 + k >= D) {
                    return true;
                }

                // --- Case B: pick i0 with b[i0]==0, invest all k in the median of the others
                if (amax0 == Long.MIN_VALUE) {
                    // no zero-bit index => can't do case B
                    return false;
                }
                long d = D - amax0;
                if (d <= 0) {
                    // needing median >=0 is always free
                    return true;
                }

                // Count how many are already >= d in the whole array:
                int idxAll = lowerBound(all, d);
                int cntFree = (n - idxAll);
                // exclude the chosen i0 if it was among those >= d
                if (amax0 >= d) cntFree--;

                // Count how many are < d but raisable (b[j]==1)
                int idxR = lowerBound(rais, d);
                int cntRaisable = idxR;  // those at indices [0..idxR-1]

                if (cntFree + cntRaisable < K) {
                    return false;
                }
                int need = Math.max(0, K - Math.max(0, cntFree));
                if (need > cntRaisable) {
                    return false;
                }
                // cost = sum_{i<need} (d - rais[i]) = need*d - sum(ra[i])
                long sumSmall = pref[need];
                long cost = need * d - sumSmall;
                return cost <= k;
            };

            // Binary‐search the maximum D for which canAchieve(D) is true
            long lo = 0, hi = (long)2e18;
            while (lo < hi) {
                long mid = (lo + hi + 1) >>> 1;
                if (canAchieve.test(mid)) {
                    lo = mid;
                } else {
                    hi = mid - 1;
                }
            }

            sb.append(lo).append('\n');
        }

        System.out.print(sb);
    }
}