import java.io.*;
import java.util.*;

public class Main {
    static final int MAXK = 700;  // cutoff between "small k" and "large k"

    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader();
        PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));

        int n = in.nextInt();
        int q = in.nextInt();
        int[] a = new int[n+1];
        for (int i = 1; i <= n; i++) {
            a[i] = in.nextInt();
        }

        // We'll store the answers here.
        boolean[] ans = new boolean[q];

        // Separate queries into "small k" (k <= MAXK) buckets
        // and a list for "large k" (k > MAXK).
        @SuppressWarnings("unchecked")
        ArrayList<Query>[] smallQ = new ArrayList[MAXK+1];
        for (int k = 1; k <= MAXK; k++) smallQ[k] = new ArrayList<>();
        ArrayList<Query> largeQ = new ArrayList<>(q);

        for (int iq = 0; iq < q; iq++) {
            int i = in.nextInt();
            int k = in.nextInt();
            if (k <= MAXK) {
                smallQ[k].add(new Query(i, k, iq));
            } else {
                largeQ.add(new Query(i, k, iq));
            }
        }

        // 1) Handle all small k with direct O(n) simulation each.
        for (int k = 1; k <= MAXK; k++) {
            ArrayList<Query> list = smallQ[k];
            if (list.isEmpty()) continue;
            // sort queries by position i
            Collections.sort(list, Comparator.comparingInt(x -> x.i));
            int ptr = 0;
            int fights = 0;  // f_j in the explanation
            for (int pos = 1; pos <= n; pos++) {
                // answer all queries that want to know about monster `pos`
                while (ptr < list.size() && list.get(ptr).i == pos) {
                    Query qu = list.get(ptr);
                    ans[qu.idx] = (fights < (long)a[pos]*k);
                    ptr++;
                }
                // then update fights for the pos-th monster
                if (fights < (long)a[pos]*k) {
                    fights++;
                }
            }
        }

        // 2) Precompute "posList[t]" = sorted positions j where a[j] >= t,
        //    for t = 1..TMAX.  We only need TMAX ~ n/MAXK + a small margin.
        int TMAX = n / MAXK + 2;
        // first count how many positions per threshold
        int[] cnt = new int[TMAX+1];
        for (int j = 1; j <= n; j++) {
            int up = Math.min(a[j], TMAX);
            for (int t = 1; t <= up; t++) {
                cnt[t]++;
            }
        }
        // build the arrays
        int[][] posList = new int[TMAX+1][];
        for (int t = 1; t <= TMAX; t++) {
            posList[t] = new int[cnt[t]];
        }
        // fill them
        int[] fillPtr = new int[TMAX+1];
        for (int j = 1; j <= n; j++) {
            int up = Math.min(a[j], TMAX);
            for (int t = 1; t <= up; t++) {
                posList[t][fillPtr[t]++] = j;
            }
        }

        // 3) Handle the large-k queries individually:
        //    if k >= i, instantly YES.  otherwise do the threshold-stepping.
        for (Query qu : largeQ) {
            int i = qu.i, k = qu.k, idxQ = qu.idx;
            if (k >= i) {
                // f_{i-1} <= i-1 < a_i * k always
                ans[idxQ] = true;
                continue;
            }
            long required = (long)a[i] * k;
            long f = 0;    // how many fights we've counted so far
            int curPos = 0; // we only look in (curPos, i)
            int thr = 1;   // we need a[j] >= thr to fight

            // we fix the upper bound of positions once
            int upBound = i;
            while (thr <= TMAX && f < required && curPos < i-1) {
                int[] arr = posList[thr];
                if (arr.length == 0) break;
                // how many in arr[] lie in (curPos, i-1] ?
                // We'll find two binary-search bounds
                int start = lowerBound(arr, curPos+1);
                int end   = lowerBound(arr, upBound);
                int available = end - start;
                if (available >= k) {
                    // we can take exactly k
                    f += k;
                    curPos = arr[start + (k-1)];
                    thr++;
                } else {
                    // we can only take `available`
                    f += available;
                    break;
                }
            }

            ans[idxQ] = (f < required);
        }

        // 4) Print
        for (int i = 0; i < q; i++) {
            out.println(ans[i] ? "YES" : "NO");
        }
        out.flush();
    }

    // A small wrapper for queries
    static class Query {
        int i, k, idx;
        Query(int i, int k, int idx) {
            this.i = i; this.k = k; this.idx = idx;
        }
    }

    // lower_bound: first index where arr[pos] >= key
    static int lowerBound(int[] arr, int key) {
        int lo = 0, hi = arr.length;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (arr[mid] < key) lo = mid + 1;
            else           hi = mid;
        }
        return lo;
    }

    // fast I/O
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
        FastReader() {
            br = new BufferedReader(new InputStreamReader(System.in), 1<<20);
        }
        String next() {
            while (st == null || !st.hasMoreTokens()) {
                try {
                    st = new StringTokenizer(br.readLine());
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return st.nextToken();
        }
        int nextInt() {
            return Integer.parseInt(next());
        }
    }
}