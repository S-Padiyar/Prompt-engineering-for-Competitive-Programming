import java.io.*;
import java.util.*;

public class Main {
    static final long INF = (long)4e18;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int T = Integer.parseInt(br.readLine());
        // We will reuse buffers across testcases but 
        // total n over all tests <= 2e5 so it's safe.
        while (T-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            long k = Long.parseLong(st.nextToken());

            long[] a = new long[n];
            int[] b = new int[n];
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                b[i] = Integer.parseInt(st.nextToken());
            }

            // Sort all a[] for quick rank queries
            Arrays.sort(a);

            // Build list of those a[i] where b[i]=1, also sorted
            List<Long> B1 = new ArrayList<>();
            long M0 = -INF, M1 = -INF;
            for (int i = 0; i < n; i++) {
                if (b[i] == 1) {
                    B1.add(a[i]);
                    M1 = Math.max(M1, a[i]);
                } else {
                    M0 = Math.max(M0, a[i]);
                }
            }
            Collections.sort(B1);

            // Prefix sums of B1 for quick "sum of largest t of B1< m"
            int sz1 = B1.size();
            long[] prefB1 = new long[sz1+1];
            prefB1[0] = 0;
            for (int i = 0; i < sz1; i++) {
                prefB1[i+1] = prefB1[i] + B1.get(i);
            }

            // Our d = floor(n/2), but to have a'_d >= m in size-n array
            // we need at least (n - d + 1) elements >= m.
            int d = n/2;
            int needed = n - d + 1;

            // Extract the unique sorted values of a[] as candidate m's
            List<Long> uniq = new ArrayList<>();
            uniq.add(a[0]);
            for (int i = 1; i < n; i++) {
                if (a[i] != a[i-1]) {
                    uniq.add(a[i]);
                }
            }

            // A helper to compute costMed(m): the min ops to ensure a'_d >= m,
            // or INF if impossible.
            class CostMed {
                long get(long m) {
                    // how many a[] < m ?
                    int lt = Arrays.binarySearch(a, m);
                    if (lt < 0) lt = -lt - 1;
                    // freeCount = how many already >= m
                    int freeCnt = n - lt;
                    int t = Math.max(0, needed - freeCnt);
                    if (t == 0) {
                        return 0;
                    }
                    // among B1, how many are < m ?
                    int idx = Collections.binarySearch(B1, m);
                    if (idx < 0) idx = -idx - 1;
                    if (idx < t) return INF; // not enough boostable
                    // sum of the t largest in B1[0..idx-1]
                    long sumLastT = prefB1[idx] - prefB1[idx - t];
                    return t * m - sumLastT;
                }
            }
            CostMed cm = new CostMed();

            // Evaluate S(m) for each unique a[i]
            long answer = 0;
            for (long m : uniq) {
                long c = cm.get(m);
                if (c > k) continue;
                long rem = k - c;
                long bestMax;
                // we may choose the b=0 max if that is larger,
                // or else boost M1 by rem (if M1 exists).
                bestMax = M0;
                if (M1 > -INF/2) {
                    bestMax = Math.max(bestMax, M1 + rem);
                }
                answer = Math.max(answer, m + bestMax);
            }

            // Also binary‐search for the largest m (not necessarily in uniq)
            // for which costMed(m) <= k
            long lo = 0, hi = a[n-1] + k + 1;
            while (lo < hi) {
                long mid = (lo + hi + 1) >>> 1;
                if (cm.get(mid) <= k) {
                    lo = mid;
                } else {
                    hi = mid - 1;
                }
            }
            // lo is now the maximum m s.t. costMed(m)<=k
            if (lo > 0) {
                long c = cm.get(lo);
                if (c <= k) {
                    long rem = k - c;
                    long bestMax = M0;
                    if (M1 > -INF/2) {
                        bestMax = Math.max(bestMax, M1 + rem);
                    }
                    answer = Math.max(answer, lo + bestMax);
                }
            }

            System.out.println(answer);
        }
    }
}