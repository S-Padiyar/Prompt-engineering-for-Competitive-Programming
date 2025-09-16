import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        int t = Integer.parseInt(in.readLine().trim());

        // We will reuse these structures across test cases to avoid repeated allocations
        List<List<Integer>> groups = new ArrayList<>();
        long[] dp = null, suf = null;

        for (int tc = 0; tc < t; tc++) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            long m = Long.parseLong(st.nextToken());

            // First pass: read all sequences, compute d_i and e_i.
            // Keep track of maxD = max(d_i).
            int maxD = 0;
            List<int[]> seqs = new ArrayList<>(n);

            for (int i = 0; i < n; i++) {
                st = new StringTokenizer(in.readLine());
                int li = Integer.parseInt(st.nextToken());
                int[] a = new int[li];
                for (int j = 0; j < li; j++)
                    a[j] = Integer.parseInt(st.nextToken());
                seqs.add(a);
            }

            // Compute (d_i,e_i) for each sequence
            int[] ds = new int[n], es = new int[n];
            for (int i = 0; i < n; i++) {
                int[] a = seqs.get(i);
                Arrays.sort(a);
                // remove duplicates in place
                int sz = 0;
                for (int x : a) {
                    if (sz == 0 || a[sz-1] != x) a[sz++] = x;
                }
                // find d_i = mex(a)
                int d = 0;
                for (int j = 0; j < sz; j++) {
                    if (a[j] == d) d++;
                    else if (a[j] > d) break;
                }
                ds[i] = d;
                maxD = Math.max(maxD, d);
                // find e_i = mex(a ∪ {d})
                int e = d;
                for (int j = 0; j < sz; j++) {
                    if (a[j] == e) e++;
                    else if (a[j] > e) break;
                }
                es[i] = e;
            }

            // K = 1 + maxD
            int K = maxD + 1;

            // Prepare groups: group[d] collects all e_i with that d_i=d
            if (groups.size() < K) {
                // enlarge
                while (groups.size() < K) groups.add(new ArrayList<>());
            }
            // clear only 0..K-1
            for (int i = 0; i < K; i++) groups.get(i).clear();
            for (int i = 0; i < n; i++) {
                int d = ds[i], e = es[i];
                groups.get(d).add(e);
            }

            // Prepare dp[0..K], suf[0..K]
            dp = new long[K+1];
            suf = new long[K+1];
            dp[K] = K;      // for x>=K, f(x)=x
            suf[K] = 0;     // not used beyond K

            // Build dp backwards
            for (int x = K - 1; x >= 0; x--) {
                // suffix max of dp[y] for y>x
                suf[x] = Math.max(suf[x+1], dp[x+1]);

                // candidate1 = x  (we can always choose no more ops)
                long best = x;
                // candidate2 = suffix max
                best = Math.max(best, suf[x]);
                // candidate3 = from e_i of all sequences with d_i=x
                for (int e : groups.get(x)) {
                    long val = (e <= K ? dp[e] : e);
                    if (val > best) best = val;
                }
                dp[x] = best;
            }

            // Now sum f(0..m):
            // If m < K, just sum dp[0..m].
            // Otherwise sum dp[0..K-1] + sum_{k=K..m} k
            long result = 0;
            if (m < K) {
                for (int i = 0; i <= m; i++) result += dp[i];
            } else {
                // sum dp[0..K-1]
                for (int i = 0; i < K; i++) result += dp[i];
                // sum K + (K+1)+...+m = (m(m+1)/2 - (K-1)K/2)
                long totalUpToM = m * (m+1) / 2;
                long totalUpToKm1 = (long)(K-1) * K / 2;
                result += (totalUpToM - totalUpToKm1);
            }

            out.println(result);
        }

        out.flush();
    }
}