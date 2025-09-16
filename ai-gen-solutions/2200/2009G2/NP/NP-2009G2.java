import java.io.*;
import java.util.*;

public class Main {
    static class FastInput {
        BufferedReader br;
        StringTokenizer st;
        FastInput(InputStream in) { br = new BufferedReader(new InputStreamReader(in)); }
        String next() throws IOException {
            while (st == null || !st.hasMoreTokens()) {
                String line = br.readLine();
                if (line == null) return null;
                st = new StringTokenizer(line);
            }
            return st.nextToken();
        }
        int nextInt() throws IOException { return Integer.parseInt(next()); }
    }

    // BIT for range‐add, point‐query
    static class BIT {
        int n;
        long[] bit;
        BIT(int n) { this.n = n; bit = new long[n+1]; }
        // add v to position p
        void add(int p, long v) {
            for (; p <= n; p += p & -p) bit[p] += v;
        }
        // range add +v to [l..r]
        void rangeAdd(int l, int r, long v) {
            if (l > r) return;
            add(l, v);
            if (r+1 <= n) add(r+1, -v);
        }
        // point query at p
        long query(int p) {
            long s = 0;
            for (; p > 0; p -= p & -p) s += bit[p];
            return s;
        }
    }

    static class Event {
        int ql, qr;     // range in t‐axis
        long a, b;      // we add a*t + b
        Event(int ql, int qr, long a, long b) {
            this.ql = ql;  this.qr = qr;  this.a = a;  this.b = b;
        }
    }

    static class Query {
        int u, idx;
        Query(int u, int idx) { this.u = u; this.idx = idx; }
    }

    public static void main(String[] args) throws IOException {
        FastInput in = new FastInput(System.in);
        PrintWriter out = new PrintWriter(System.out);

        int T = in.nextInt();
        while (T-- > 0) {
            int n = in.nextInt();
            int k = in.nextInt();
            int Q = in.nextInt();
            int[] a = new int[n+1];
            for (int i = 1; i <= n; i++) a[i] = in.nextInt();

            // 1) build d[i] = a[i] - i, then slide an O(1)-amortized window
            //    of size k to compute c[1..n-k+1].
            int Np = n - k + 1;  // number of windows
            int[] d = new int[n+1];
            for (int i = 1; i <= n; i++) {
                d[i] = a[i] - i;
            }
            // we map d-values in [-n..n] to [1..2n+1]
            int SHIFT = n + 1;
            int M = 2*n + 5;
            int[] freq = new int[M];
            int[] cntF = new int[k+2];
            int maxf = 0;

            // helper to add one element x
            Runnable resetFreq = () -> {
                // we'll manually zero freq and cnt arrays
                Arrays.fill(freq, 0);
                Arrays.fill(cntF, 0);
                maxf = 0;
            };
            resetFreq.run();

            // add x into window
            class Add {
                void addVal(int x) {
                    int oldf = freq[x];
                    int newf = oldf + 1;
                    freq[x] = newf;
                    if (oldf > 0) cntF[oldf]--;
                    cntF[newf]++;
                    if (newf > maxf) maxf = newf;
                }
                void remVal(int x) {
                    int oldf = freq[x];
                    int newf = oldf - 1;
                    freq[x] = newf;
                    cntF[oldf]--;
                    if (newf > 0) cntF[newf]++;
                    if (oldf == maxf && cntF[oldf] == 0) {
                        maxf--;
                    }
                }
            }
            var ad = new Add();

            // build c[1]
            for (int i = 1; i <= k; i++) {
                ad.addVal(d[i] + SHIFT);
            }
            int[] c = new int[Np+1];
            c[1] = maxf;
            for (int s = 2; s <= Np; s++) {
                ad.remVal(d[s-1] + SHIFT);
                ad.addVal(d[s + k - 1] + SHIFT);
                c[s] = maxf;
            }

            // 2) compute L[s] and r[s] (prev‐greater, next‐greater)
            int[] L = new int[Np+1], R = new int[Np+1];
            { // prev‐greater
                Deque<Integer> st = new ArrayDeque<>();
                for (int i = 1; i <= Np; i++) {
                    while (!st.isEmpty() && c[st.peek()] <= c[i]) st.pop();
                    L[i] = st.isEmpty() ? 0 : st.peek();
                    st.push(i);
                }
            }
            { // next‐greater
                Deque<Integer> st = new ArrayDeque<>();
                for (int i = Np; i >= 1; i--) {
                    while (!st.isEmpty() && c[st.peek()] <= c[i]) st.pop();
                    R[i] = st.isEmpty() ? (Np+1) : st.peek();
                    st.push(i);
                }
            }

            // We'll build events over "l" from 1..Np.  Each s spawns TWO
            // rectangles in the (l, t)-plane: one where contribution is
            // linear in t, one constant.
            ArrayList<Event>[] events = new ArrayList[Np+2];
            for (int i = 1; i <= Np+1; i++) events[i] = new ArrayList<>();

            // precompute A[s]
            long[] A = new long[Np+1];
            for (int s = 1; s <= Np; s++) {
                A[s] = (long)c[s] * (R[s] - s);
            }

            for (int s = 1; s <= Np; s++) {
                int pStart = L[s] + 1;
                int pEnd = s + 1;    // we will "remove" at l = s+1
                // region1: t in [s .. R[s]-2], weight = c[s]*(t+1-s)
                //           = c[s]*t + c[s]*(1-s)
                int q1 = s;
                int q2 = R[s] - 2;
                if (q1 <= q2) {
                    long a1 = c[s];
                    long b1 = (long)c[s] * (1 - s);
                    events[pStart].add(new Event(q1, q2, +a1, +b1));
                    if (pEnd <= Np+1)
                        events[pEnd].add(new Event(q1, q2, -a1, -b1));
                }
                // region2: t in [R[s]-1 .. Np], weight = A[s]
                int r1 = R[s] - 1;
                int r2 = Np;
                if (r1 <= r2) {
                    long b2 = A[s];
                    events[pStart].add(new Event(r1, r2, 0, +b2));
                    if (pEnd <= Np+1)
                        events[pEnd].add(new Event(r1, r2, 0, -b2));
                }
            }

            // read queries
            @SuppressWarnings("unchecked")
            ArrayList<Query>[] queriesAt = new ArrayList[Np+1];
            for (int i = 1; i <= Np; i++) queriesAt[i] = new ArrayList<>();
            long[] finalAns = new long[Q];
            int[] ql = new int[Q], qr = new int[Q];
            for (int i = 0; i < Q; i++) {
                int l = in.nextInt();
                int r = in.nextInt();
                ql[i] = l;  qr[i] = r;
                // u = r-k+1
                int u = r - k + 1;
                // we only sweep l from 1..Np, so u>=l always valid
                queriesAt[l].add(new Query(u,i));
            }

            // Two BITs for the “a*t + b” trick:
            // we store in bitA the coefficient of t, in bitB the constant
            BIT bitA = new BIT(Np), bitB = new BIT(Np);

            // sweep l = 1..Np
            for (int l = 1; l <= Np; l++) {
                // apply all rectangle‐add events at this l
                for (Event e : events[l]) {
                    bitA.rangeAdd(e.ql, e.qr, e.a);
                    bitB.rangeAdd(e.ql, e.qr, e.b);
                }
                // answer all queries with this l
                for (Query qq : queriesAt[l]) {
                    int u = qq.u;
                    long acoef = bitA.query(u);
                    long bconst = bitB.query(u);
                    long sumMt = acoef * u + bconst;
                    finalAns[qq.idx] = sumMt;
                }
            }

            // Finally print answers: each query is
            //    sum_j f(...) = (#terms)*k  -  sum_{t=l..u} M(l,t)
            // #terms = (r-(l+k-1)+1) = (r-l-k+2)
            for (int i = 0; i < Q; i++) {
                int l = ql[i], r = qr[i];
                long terms = (long)(r - l - k + 2);
                long ans = terms * k - finalAns[i];
                out.println(ans);
            }
        }

        out.flush();
    }
}