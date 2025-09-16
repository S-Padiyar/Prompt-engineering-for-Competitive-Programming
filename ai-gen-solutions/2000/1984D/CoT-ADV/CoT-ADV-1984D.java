import java.io.*;
import java.util.*;

public class Main {
    // Double rolling hash
    static final long MOD1 = 1000000007, MOD2 = 1000000009;
    static final long BASE = 91138233;

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int T = Integer.parseInt(in.readLine());
        StringBuilder output = new StringBuilder();
        while (T-- > 0) {
            String s = in.readLine();
            int n = s.length();
            // 1) Record positions of non-'a'
            List<Integer> posList = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                if (s.charAt(i) != 'a') posList.add(i);
            }
            int k = posList.size();
            if (k == 0) {
                // all 'a' -> any t = "a"^m for m=2..n
                output.append(n - 1).append('\n');
                continue;
            }
            // Convert to 0-based int array
            int[] p = new int[k];
            for (int i = 0; i < k; i++) p[i] = posList.get(i);

            // 2) Precompute double rolling hash
            long[] pow1 = new long[n+1], pow2 = new long[n+1];
            long[] h1 = new long[n+1], h2 = new long[n+1];
            pow1[0] = pow2[0] = 1;
            for (int i = 1; i <= n; i++) {
                pow1[i] = (pow1[i-1] * BASE) % MOD1;
                pow2[i] = (pow2[i-1] * BASE) % MOD2;
            }
            for (int i = 0; i < n; i++) {
                h1[i+1] = (h1[i]*BASE + (s.charAt(i))) % MOD1;
                h2[i+1] = (h2[i]*BASE + (s.charAt(i))) % MOD2;
            }
            // helper to get hash of s[l..r] inclusive, 0-based
            class Hash {
                long get1(int l, int r) {
                    long res = h1[r+1] - (h1[l] * pow1[r-l+1] % MOD1);
                    if (res < 0) res += MOD1;
                    return res;
                }
                long get2(int l, int r) {
                    long res = h2[r+1] - (h2[l] * pow2[r-l+1] % MOD2);
                    if (res < 0) res += MOD2;
                    return res;
                }
                boolean equals(int a, int b, int len) {
                    // compare s[a..a+len-1] vs s[b..b+len-1]
                    if (a+len-1 >= n || b+len-1 >= n) return false;
                    return get1(a, a+len-1) == get1(b, b+len-1)
                        && get2(a, a+len-1) == get2(b, b+len-1);
                }
            }
            Hash RH = new Hash();

            // 3) Build gaps and limits
            int[] gap = new int[k];
            int[] limit = new int[k];
            for (int j = 1; j < k; j++) {
                gap[j] = p[j] - p[j-1];
                limit[j] = gap[j] - 1; 
            }

            // 4) Compute z[j] = LCP of s[p[0]..] and s[p[j]..]
            int[] z = new int[k];
            for (int j = 1; j < k; j++) {
                // binary search LCP
                int low = 0, high = n - Math.max(p[0], p[j]);
                while (low < high) {
                    int mid = (low + high + 1) >>> 1;
                    if (RH.equals(p[0], p[j], mid)) low = mid;
                    else high = mid - 1;
                }
                z[j] = low;
            }

            // 5a) Precompute maxJ[m] = largest j with limit[j] >= m
            int maxM = n;  // we'll only use up to n
            int[] maxJ = new int[maxM+2];
            for (int j = 1; j < k; j++) {
                int lim = limit[j];
                if (lim <= 0) continue;
                if (lim > maxM) lim = maxM;
                // For x=1..lim, we can say maxJ[x] = max(maxJ[x], j)
                // We do it by a short loop; sum of all lims is O(n).
                for (int x = 1; x <= lim; x++) {
                    if (maxJ[x] < j) maxJ[x] = j;
                }
            }

            // 5b) Precompute bestZ[m] = min{ z[j] : limit[j] >= m }
            // We'll bucket j by limit[j].
            @SuppressWarnings("unchecked")
            ArrayList<Integer>[] bucket = new ArrayList[maxM+2];
            for (int i = 0; i <= maxM; i++) bucket[i] = new ArrayList<>();
            for (int j = 1; j < k; j++) {
                int lim = limit[j];
                if (lim >= 1 && lim <= maxM) bucket[lim].add(j);
            }
            int INF = n+5;
            int[] bestZ = new int[maxM+2];
            int curMin = INF;
            // sweep m downward
            for (int m = maxM; m >= 1; m--) {
                for (int j : bucket[m]) {
                    if (z[j] < curMin) curMin = z[j];
                }
                bestZ[m] = curMin;
            }

            // 6) Loop m, test coverage + matching
            int answer = 0;
            int lastPos = p[k-1];
            for (int m = 1; m <= maxM; m++) {
                // find t(m)
                int j = maxJ[m]; 
                int startPos = (j == 0 ? p[0] : p[j]);
                // coverage check
                if (m < (lastPos - startPos + 1)) continue;
                // substring‐equality check: z[j']>=m for every gap_j'>m
                // is captured by bestZ[m] >= m
                if (bestZ[m] < m) continue;
                // t = s[startPos … startPos+m-1] certainly starts with non-'a'.
                // Count it.
                answer++;
            }
            output.append(answer).append('\n');
        }
        System.out.print(output);
    }
}