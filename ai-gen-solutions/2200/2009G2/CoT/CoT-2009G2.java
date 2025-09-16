import java.io.*;
import java.util.*;

public class YunliSubarrayQueries {
    static class Fenwick {
        int n;
        long[] f;
        Fenwick(int _n) {
            n = _n;
            f = new long[n+2];
        }
        // point-update: f[i] += v
        void update(int i, long v) {
            for (; i<=n; i += i&-i) f[i] += v;
        }
        // prefix-sum query
        long query(int i) {
            long s = 0;
            for (; i>0; i -= i&-i) s += f[i];
            return s;
        }
        // range-add [l..r] += v
        void rangeAdd(int l, int r, long v) {
            if (l>r) return;
            update(l, v);
            if (r+1 <= n) update(r+1, -v);
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int T = Integer.parseInt(in.readLine().trim());
        StringBuilder out = new StringBuilder();
        while (T-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int k = Integer.parseInt(st.nextToken());
            int q = Integer.parseInt(st.nextToken());
            int[] a = new int[n];
            st = new StringTokenizer(in.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            // 1) Build r'[i] = a[i] - i, compress
            int[] rRaw = new int[n];
            for (int i = 0; i < n; i++) {
                rRaw[i] = a[i] - i;
            }
            // compress
            int[] comp = rRaw.clone();
            Arrays.sort(comp);
            int mUniq = 0;
            for (int x : comp)
                if (mUniq == 0 || comp[mUniq-1] != x)
                    comp[mUniq++] = x;
            // map
            for (int i = 0; i < n; i++) {
                int idx = Arrays.binarySearch(comp, 0, mUniq, rRaw[i]);
                rRaw[i] = idx;
            }

            // 2) Slide a window of length k over rRaw to compute c[1..M]
            int M = n - k + 1;
            int[] c = new int[M+1];
            int[] cnt = new int[mUniq];
            int[] freqOfFreq = new int[k+1];
            int maxf = 0;

            // init first window
            for (int i = 0; i < k; i++) {
                int v = rRaw[i];
                int old = cnt[v]++;
                if (old <= k) freqOfFreq[old]--;
                freqOfFreq[old+1]++;
                if (old+1 > maxf) maxf = old+1;
            }
            c[1] = maxf;

            for (int i = 2; i <= M; i++) {
                // remove rRaw[i-2], add rRaw[i+k-2]
                int outVal = rRaw[i-2];
                int old = cnt[outVal]--;
                freqOfFreq[old]--;
                freqOfFreq[old-1]++;
                if (old == maxf && freqOfFreq[old] == 0) maxf--;
                int inVal = rRaw[i+k-2];
                old = cnt[inVal]++;
                freqOfFreq[old]--;
                freqOfFreq[old+1]++;
                if (old+1 > maxf) maxf = old+1;
                c[i] = maxf;
            }

            // 3) Compute L[i], R[i] on c[] (1..M)
            int[] L = new int[M+1], R = new int[M+1];
            // L = previous >=
            {
                Deque<Integer> stk = new ArrayDeque<>();
                for (int i = 1; i <= M; i++) {
                    while (!stk.isEmpty() && c[stk.peek()] < c[i]) {
                        stk.pop();
                    }
                    L[i] = stk.isEmpty() ? 0 : stk.peek();
                    stk.push(i);
                }
            }
            // R = next >
            {
                Arrays.fill(R, M+1);
                Deque<Integer> stk = new ArrayDeque<>();
                for (int i = 1; i <= M; i++) {
                    while (!stk.isEmpty() && c[stk.peek()] < c[i]) {
                        R[stk.pop()] = i;
                    }
                    stk.push(i);
                }
                // remainder default to M+1
            }

            // 4) Read queries, bucket them by their 'u = r-k+1'
            @SuppressWarnings("unchecked")
            ArrayList<int[]>[] byU = new ArrayList[M+2];
            for (int i = 1; i <= M+1; i++) {
                byU[i] = new ArrayList<>();
            }
            long[] ans = new long[q];
            for (int qi = 0; qi < q; qi++) {
                st = new StringTokenizer(in.readLine());
                int l = Integer.parseInt(st.nextToken());
                int r = Integer.parseInt(st.nextToken());
                // in c-space:
                int u = r - k + 1;
                // number of terms is (u - l + 1)
                long terms = (long)u - l + 1;
                long mk = terms * k;
                byU[u].add(new int[]{l, qi, (int)mk});
            }

            // 5) Fenwicks for dynamic and static
            Fenwick bitDynC  = new Fenwick(M+1);
            Fenwick bitDynCJ = new Fenwick(M+1);
            Fenwick bitStat  = new Fenwick(M+1);

            // We'll sweep u=1..M; at u=i we add index i to dynamic,
            // and at u=R[i] we remove it from dynamic & add to static.
            @SuppressWarnings("unchecked")
            ArrayList<Integer>[] remAt = new ArrayList[M+2];
            for (int i = 1; i <= M+1; i++) {
                remAt[i] = new ArrayList<>();
            }
            for (int i = 1; i <= M; i++) {
                int rpos = R[i];
                if (rpos <= M) {
                    remAt[rpos].add(i);
                }
            }

            // sweep
            for (int u = 1; u <= M; u++) {
                // 5a) add i=u to dynamic
                {
                    int i = u;
                    int left = L[i] + 1;
                    int right = M;
                    long cv = c[i];
                    bitDynC.rangeAdd(left, right, cv);
                    bitDynCJ.rangeAdd(left, right, cv * i);
                }
                // 5b) handle removals at this u
                for (int i : remAt[u]) {
                    int left = L[i] + 1, right = M;
                    long cv = c[i];
                    // remove from dynamic
                    bitDynC.rangeAdd(left, right, -cv);
                    bitDynCJ.rangeAdd(left, right, -cv * i);
                    // add to static
                    long w = cv * (R[i] - i);
                    bitStat.rangeAdd(left, right, w);
                }
                // 5c) answer queries with this u
                for (int[] qq : byU[u]) {
                    int l = qq[0], idx = qq[1];
                    long mk = (long)qq[2];
                    // query point l
                    long Dsum = bitDynC.query(l);
                    long Dji  = bitDynCJ.query(l);
                    long Ssum = bitStat.query(l);
                    // total sum of g(j) from j=l..u
                    long sumg = Dsum*(u+1L) - Dji + Ssum;
                    ans[idx] = mk - sumg;
                }
            }

            // 6) output
            for (long v : ans) {
                out.append(v).append('\n');
            }
        }
        System.out.print(out);
    }
}