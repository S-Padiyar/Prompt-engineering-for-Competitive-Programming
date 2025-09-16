import java.io.*;
import java.util.*;

public class Main {
    static class Node {
        int t, e, i, L, R, id;
        long w1, w2, w3;
        boolean isQuery;
    }

    static int M;           // number of windows = n-k+1
    static int[] prevSm, nextSm;   // prev smaller, next smaller indices
    static int[] cost;      // cost[1..M]
    static long[] totA, totAi;     // totals from the 1D sweep
    static long[] ansC1, ansA, ansAi;  // partial sums from CDQ
    static Node[] events;   // all point+query events
    static long[] BIT1, BIT2, BIT3;  // Fenwick arrays (for 3 weights)
    static int BITn;

    // Fenwick utility: point-update, range-prefix-sum
    static void bitUpdate(int idx, long d1, long d2, long d3) {
        for (int x = idx; x <= BITn; x += x & -x) {
            BIT1[x] += d1;
            BIT2[x] += d2;
            BIT3[x] += d3;
        }
    }
    static long[] bitQuery(int idx) {
        long s1=0, s2=0, s3=0;
        for (int x = idx; x > 0; x -= x & -x) {
            s1 += BIT1[x];
            s2 += BIT2[x];
            s3 += BIT3[x];
        }
        return new long[]{s1, s2, s3};
    }

    // CDQ over events[t=0..N-1], dividing by t
    static void cdq(int L, int R) {
        if (L >= R) return;
        int mid = (L + R) >>> 1;
        cdq(L, mid);
        // collect "points" from [L..mid] and "queries" from [mid+1..R]
        List<Node> pts = new ArrayList<>(), qry = new ArrayList<>();
        for (int i = L; i <= mid; i++) {
            if (!events[i].isQuery) pts.add(events[i]);
        }
        for (int i = mid+1; i <= R; i++) {
            if (events[i].isQuery) qry.add(events[i]);
        }
        // sort by e
        pts.sort(Comparator.comparingInt(a->a.e));
        qry.sort(Comparator.comparingInt(a->a.e));

        // sweep e, maintain Fenwick on the i-coordinate
        int p = 0;
        for (Node Q : qry) {
            while (p < pts.size() && pts.get(p).e <= Q.e) {
                Node P = pts.get(p++);
                // add its weights
                bitUpdate(P.i, P.w1, P.w2, P.w3);
            }
            // now query range [Q.L..Q.R]
            long[] sumRight = bitQuery(Q.R);
            long[] sumLeft  = bitQuery(Q.L - 1);
            ansC1[Q.id] += (sumRight[0] - sumLeft[0]);
            ansA[Q.id]  += (sumRight[1] - sumLeft[1]);
            ansAi[Q.id] += (sumRight[2] - sumLeft[2]);
        }
        // rollback
        for (int i = 0; i < p; i++) {
            Node P = pts.get(i);
            bitUpdate(P.i, -P.w1, -P.w2, -P.w3);
        }

        cdq(mid+1, R);
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter   pw = new PrintWriter(new OutputStreamWriter(System.out));
        StringTokenizer st;

        int T = Integer.parseInt(br.readLine());
        while (T-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int k = Integer.parseInt(st.nextToken());
            int q = Integer.parseInt(st.nextToken());
            int[] a = new int[n+1];
            st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= n; i++) a[i] = Integer.parseInt(st.nextToken());

            // If k>n, no valid window => but problem guarantees r>=l+k-1, so this never happens.
            M = n - k + 1;

            // 1) Compute c[i] = a[i]-i, i=1..n
            int[] c = new int[n+1];
            for (int i = 1; i <= n; i++) c[i] = a[i] - i;

            // 2) Slide a window of length k over c[.] to compute cost[i] = k - maxFreq
            cost = new int[M+1];
            HashMap<Integer,int[]> freq = new HashMap<>();
            // freq maps (value c) -> [current count of c in window, freqCount array index]
            // We'll track freqCount[f] = how many distinct c's occur exactly f times
            int[] freqCount = new int[k+1];
            int currMax = 0;
            // init first window i=1..k
            for (int i = 1; i <= k; i++) {
                int v = c[i];
                int[] arr = freq.computeIfAbsent(v, x -> new int[]{0,0});
                int old = arr[0];
                if (old > 0) freqCount[old]--;
                arr[0] = old+1;
                freqCount[old+1]++;
                currMax = Math.max(currMax, old+1);
            }
            cost[1] = k - currMax;

            for (int start = 2; start <= M; start++) {
                // remove c[start-1], add c[start+k-1]
                int drop = c[start-1], add = c[start+k-1];
                {
                    int[] arr = freq.get(drop);
                    int old = arr[0];
                    freqCount[old]--;
                    arr[0] = old-1;
                    if (old-1 > 0) freqCount[old-1]++;
                    if (freqCount[currMax] == 0) currMax--;
                }
                {
                    int[] arr = freq.computeIfAbsent(add, x -> new int[]{0,0});
                    int old = arr[0];
                    if (old > 0) freqCount[old]--;
                    arr[0] = old+1;
                    freqCount[old+1]++;
                    currMax = Math.max(currMax, old+1);
                }
                cost[start] = k - currMax;
            }

            // 3) prev/next smaller for cost[1..M]
            prevSm = new int[M+1];
            nextSm = new int[M+1];
            Deque<Integer> stck = new ArrayDeque<>();
            // previous smaller
            for (int i = 1; i <= M; i++) {
                while (!stck.isEmpty() && cost[stck.peek()] >= cost[i]) stck.pop();
                prevSm[i] = stck.isEmpty() ? 0 : stck.peek();
                stck.push(i);
            }
            stck.clear();
            // next smaller
            for (int i = M; i >= 1; i--) {
                while (!stck.isEmpty() && cost[stck.peek()] > cost[i]) stck.pop();
                nextSm[i] = stck.isEmpty() ? (M+1) : stck.peek();
                stck.push(i);
            }

            // Pre‐compute activation times and end‐times
            // activation t[i] = prevSm[i]+1, end‐time e[i] = nextSm[i]-1
            int[] tact = new int[M+1], eend = new int[M+1];
            for (int i = 1; i <= M; i++) {
                tact[i] = prevSm[i] + 1;
                eend[i] = nextSm[i] - 1;
            }

            // Read queries, convert r->R=r-k+1
            int[] Lq = new int[q], Rq = new int[q];
            List<Integer>[] byL = new ArrayList[M+2];
            for (int i = 1; i <= M; i++) byL[i] = new ArrayList<>();
            for (int i = 0; i < q; i++) {
                st = new StringTokenizer(br.readLine());
                int L = Integer.parseInt(st.nextToken());
                int R = Integer.parseInt(st.nextToken());
                Lq[i] = L;
                Rq[i] = R - k + 1;
                byL[L].add(i);
            }

            // 4) Sweep in order of l to build totA, totAi
            totA  = new long[q];
            totAi = new long[q];
            // Fenwicks for these
            long[] fA   = new long[M+2];
            long[] fAi  = new long[M+2];
            // activation buckets
            List<Integer>[] acts = new ArrayList[M+2];
            for (int i = 1; i <= M; i++) acts[i] = new ArrayList<>();
            for (int i = 1; i <= M; i++) acts[tact[i]].add(i);

            // Fenwick routines for this sweep
            class Fwt {
                void upd(long[] F, int x, long v) {
                    for (; x <= M; x += x&-x) F[x] += v;
                }
                long sum(long[] F, int x) {
                    long s=0;
                    for (; x>0; x-=x&-x) s+=F[x];
                    return s;
                }
                long range(long[] F, int l, int r) {
                    if (l>r) return 0L;
                    return sum(F,r) - sum(F,l-1);
                }
            }
            Fwt fwt = new Fwt();

            for (int l = 1; l <= M; l++) {
                // activate points i with tact[i]==l
                for (int i : acts[l]) {
                    fwt.upd(fA,  i, cost[i]);
                    fwt.upd(fAi, i, (long)cost[i]*i);
                }
                // answer queries that begin at l
                for (int qi : byL[l]) {
                    int Rpos = Rq[qi];
                    totA[qi]  = fwt.range(fA,  l, Rpos);
                    totAi[qi] = fwt.range(fAi, l, Rpos);
                }
            }

            // 5) Build the combined events array for CDQ
            events = new Node[M + q];
            int idx = 0;
            for (int i = 1; i <= M; i++) {
                Node nd = new Node();
                nd.isQuery = false;
                nd.t = tact[i];
                nd.e = eend[i];
                nd.i = i;
                // weights w1,w2,w3:
                long cst = cost[i];
                nd.w2 = cst;               // for Σ cost[i]
                nd.w3 = cst * i;           // for Σ cost[i]*i
                nd.w1 = cst * (eend[i] - i + 1);  // for Σ cost[i]*(end-i+1)
                events[idx++] = nd;
            }
            for (int qi = 0; qi < q; qi++) {
                Node nd = new Node();
                nd.isQuery = true;
                nd.t = Lq[qi];
                nd.e = Rq[qi];
                nd.L = Lq[qi];
                nd.R = Rq[qi];
                nd.id = qi;
                events[idx++] = nd;
            }
            // sort by t, and points before queries at equal t
            Arrays.sort(events, 0, idx,
                (x,y)-> {
                    if (x.t != y.t) return x.t - y.t;
                    return (x.isQuery?1:0) - (y.isQuery?1:0);
                }
            );

            // Prepare arrays for CDQ results
            ansC1 = new long[q];
            ansA  = new long[q];
            ansAi = new long[q];

            // Init Fenwick for CDQ (size M on the i-axis)
            BITn = M;
            BIT1 = new long[M+2];
            BIT2 = new long[M+2];
            BIT3 = new long[M+2];

            // Run CDQ to fill ansC1[], ansA[], ansAi[]
            cdq(0, idx-1);

            // 6) Final assembly of each query's answer
            //    ans = (sum w1 for e<=R) 
            //          + (R+1)*( Σ cost[i] for e>R ) 
            //          - ( Σ cost[i]*i for e>R )
            //    but Σ cost[i] for e>R = totA - ansA[qi], etc.
            StringBuilder sb = new StringBuilder();
            for (int qi = 0; qi < q; qi++) {
                long c1  = ansC1[qi];
                long aLE = ansA[qi];
                long aiLE= ansAi[qi];
                long Aall = totA[qi],  Aiall = totAi[qi];
                long Rv = Rq[qi];
                long truncatedCountA  = Aall   - aLE;
                long truncatedCountAi = Aiall  - aiLE;
                long part2 = (Rv + 1) * truncatedCountA  - truncatedCountAi;
                long answer = c1 + part2;
                sb.append(answer).append('\n');
            }

            pw.print(sb);
            pw.flush();
        }
        pw.close();
    }
}
