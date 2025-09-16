import java.io.*;
import java.util.*;

public class Main {
    static class Fenwick {
        int n;
        int[] f;
        Fenwick(int n) { this.n = n; f = new int[n+1]; }
        void update(int i, int v) {
            for (; i<=n; i += i&-i) f[i]+=v;
        }
        int query(int i) {
            int s=0;
            for (; i>0; i -= i&-i) s+=f[i];
            return s;
        }
        int rangeSum(int l, int r) {
            if (l>r) return 0;
            return query(r) - query(l-1);
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            long c = Long.parseLong(st.nextToken());
            long[] a = new long[n+1];
            st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }
            // prefix sums
            long[] pref = new long[n+1];
            for (int i = 1; i <= n; i++) pref[i] = pref[i-1] + a[i];
            // prefix max from 2..i
            long[] pm2 = new long[n+1];
            pm2[1] = Long.MIN_VALUE;
            for (int i = 2; i <= n; i++) {
                pm2[i] = (i==2 ? a[2] : Math.max(pm2[i-1], a[i]));
            }
            // suffix max for i+1..n
            long[] sm = new long[n+2];
            sm[n] = Long.MIN_VALUE;
            for (int i = n-1; i >= 1; i--) {
                sm[i] = Math.max(sm[i+1], a[i+1]);
            }

            // answers, init to "infinity"
            int INF = n+5;
            int[] ans = new int[n+1];
            Arrays.fill(ans, INF);

            // Case 1: zero-exclusion possibility
            // i==1
            if (n>1) {
                if (a[1] + c >= sm[1]) {
                    ans[1] = 0;
                }
            } else {
                // only one candidate
                ans[1] = 0;
            }
            // i>1
            for (int i = 2; i <= n; i++) {
                // Condition for i to win with no exclusions:
                // a[i] > a[1]+c, a[i] > max(a2..a[i-1]), a[i] >= max(a[i+1..n])
                if (a[i] > a[1] + c
                        && (i==2 || a[i] > pm2[i-1])
                        && (i==n || a[i] >= sm[i])) {
                    ans[i] = 0;
                }
            }

            // Prepare queries for caseA
            // We'll only re-compute those ans[i]>0
            class Query {
                long X;
                int idx;
                Query(long X, int idx) { this.X = X; this.idx = idx; }
            }
            ArrayList<Query> queries = new ArrayList<>(n);
            for (int i = 1; i <= n; i++) {
                if (ans[i] != 0) {  // only for these do we need to compute
                    long baseU = c + pref[i-1];
                    long X = a[i] + baseU;
                    queries.add(new Query(X, i));
                }
            }
            // sort queries by descending X
            queries.sort((u, v) -> Long.compare(v.X, u.X));

            // Build list of all (a_k, k), descending by a_k
            class Elem {
                long val;
                int pos;
                Elem(long v, int p) { val=v; pos=p; }
            }
            ArrayList<Elem> elems = new ArrayList<>(n);
            for (int k = 1; k <= n; k++) {
                elems.add(new Elem(a[k], k));
            }
            elems.sort((u, v) -> Long.compare(v.val, u.val)); // descending by val

            Fenwick fw = new Fenwick(n);
            int ptr = 0;
            // Sweep queries
            for (Query q: queries) {
                long X = q.X;
                int i = q.idx;
                // Activate all suffix‐elements whose a_k > X
                while (ptr < n && elems.get(ptr).val > X) {
                    fw.update(elems.get(ptr).pos, 1);
                    ptr++;
                }
                // Count how many of those activated lie in k>i
                int extra = fw.rangeSum(i+1, n);
                int cand = (i-1) + extra;
                ans[i] = Math.min(ans[i], cand);
            }

            // Output answers
            for (int i = 1; i <= n; i++) {
                sb.append(ans[i]).append(i==n?'\n':' ');
            }
        }
        System.out.print(sb);
    }
}