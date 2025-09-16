import java.io.*;
import java.util.*;

public class Main {
    static final int B = 550;   // threshold for small k
    static int n, q;
    static int[] a, Qi, Qk;
    static String[] ans;

    // Persistent segment tree arrays
    static int MAXNODES = 4000000;  // ~ n * log2(n)
    static int[] Lch = new int[MAXNODES];
    static int[] Rch = new int[MAXNODES];
    static int[] Sum = new int[MAXNODES];
    static int nodeCnt = 0;
    static int[] roots;            // roots[l] = version for level l

    public static void main(String[] args) throws IOException {
        new Main().run();
    }

    void run() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        n = Integer.parseInt(st.nextToken());
        q = Integer.parseInt(st.nextToken());

        a = new int[n+1];
        st = new StringTokenizer(br.readLine());
        for (int i = 1; i <= n; i++) {
            a[i] = Integer.parseInt(st.nextToken());
        }

        Qi = new int[q];
        Qk = new int[q];
        ans = new String[q];
        // bucket small‐k queries
        @SuppressWarnings("unchecked")
        ArrayList<Integer>[] smallQ = new ArrayList[B+1];
        for (int k=1; k<=B; k++) smallQ[k] = new ArrayList<>();
        ArrayList<Integer> largeQ = new ArrayList<>();

        for (int i = 0; i < q; i++) {
            st = new StringTokenizer(br.readLine());
            Qi[i] = Integer.parseInt(st.nextToken());
            Qk[i] = Integer.parseInt(st.nextToken());
            if (Qk[i] <= B) smallQ[Qk[i]].add(i);
            else largeQ.add(i);
        }

        // 1) small k by direct simulation
        BitSet[] fought = new BitSet[B+1];
        for (int k=1; k<=B; k++) {
            BitSet bs = new BitSet(n+2);
            fought[k] = bs;
            int level=1, fights=0;
            for (int i=1; i<=n; i++) {
                if (level <= a[i]) {
                    bs.set(i);
                    fights++;
                    if (fights % k == 0) level++;
                }
            }
            for (int idx: smallQ[k]) {
                ans[idx] = bs.get(Qi[idx]) ? "YES" : "NO";
            }
        }

        // 2) build PST for levels 1..Lmax
        int Lmax = n/(B+1) + 5;
        // collect positions by monster‐level
        int maxA = 0;
        for (int i=1; i<=n; i++) if (a[i]>maxA) maxA=a[i];
        int up = Math.max(maxA, Lmax);

        ArrayList<Integer>[] byValue = new ArrayList[up+1];
        for (int v=0; v<=up; v++) byValue[v] = new ArrayList<>();
        for (int i=1; i<=n; i++) {
            if (a[i] <= up) byValue[a[i]].add(i);
        }

        roots = new int[Lmax+2];
        // start with empty tree
        roots[up+1] = buildEmpty(1, n);
        // build from top down: version v = include all positions with level v
        for (int v = up; v >= 1; v--) {
            int rt = roots[v+1];
            for (int pos : byValue[v]) {
                rt = pstUpdate(rt, 1, n, pos, 1);
            }
            if (v <= Lmax) roots[v] = rt;
        }
        // versions >Lmax same as Lmax
        for (int v=Lmax+1; v<=up+1; v++) {
            if (v<=Lmax) roots[v] = roots[Lmax];
        }

        // 3) answer large-k queries
        for (int idx : largeQ) {
            int iPos = Qi[idx], kVal = Qk[idx];
            int ai = a[iPos];
            // fast YES cases
            if (kVal >= iPos || (long)kVal * ai > (iPos-1L)) {
                ans[idx] = "YES";
                continue;
            }
            long fights = 0;
            int cur = 1, level = 1;
            while (cur < iPos && level <= Lmax) {
                int rt = roots[level];
                int have = querySum(rt, 1, n, cur, iPos-1);
                if (have <= 0) break;
                if (have < kVal) {
                    fights += have;
                    break;
                } else {
                    fights += kVal;
                    // find k-th in [cur..iPos-1]
                    int p = queryKth(rt, 1, n, cur, iPos-1, kVal);
                    cur = p+1;
                    level++;
                }
            }
            ans[idx] = (fights < (long)kVal*ai ? "YES" : "NO");
        }

        // 4) output
        StringBuilder sb = new StringBuilder(q*4);
        for (int i=0; i<q; i++) {
            sb.append(ans[i]).append('\n');
        }
        System.out.print(sb);
    }

    // build empty segment tree: returns root index
    int buildEmpty(int l, int r) {
        int nd = nodeCnt++;
        Sum[nd] = 0;
        if (l < r) {
            int m = (l+r)>>>1;
            Lch[nd] = buildEmpty(l, m);
            Rch[nd] = buildEmpty(m+1, r);
        }
        return nd;
    }

    // point-update: add 'val' at position 'pos'
    int pstUpdate(int old, int l, int r, int pos, int val) {
        int nd = nodeCnt++;
        Lch[nd] = Lch[old];
        Rch[nd] = Rch[old];
        Sum[nd] = Sum[old] + val;
        if (l < r) {
            int m = (l+r) >>> 1;
            if (pos <= m) {
                Lch[nd] = pstUpdate(Lch[old], l, m, pos, val);
            } else {
                Rch[nd] = pstUpdate(Rch[old], m+1, r, pos, val);
            }
        }
        return nd;
    }

    // range-sum on version root: sum over [ql..qr]
    int querySum(int nd, int l, int r, int ql, int qr) {
        if (nd == 0 || qr < l || r < ql) return 0;
        if (ql <= l && r <= qr) return Sum[nd];
        int m = (l+r) >>> 1;
        return querySum(Lch[nd], l, m, ql, qr)
             + querySum(Rch[nd], m+1, r, ql, qr);
    }

    // find the k-th '1' in [ql..qr] in version nd
    int queryKth(int nd, int l, int r, int ql, int qr, int k) {
        if (l == r) return l;
        int m = (l+r) >>> 1;
        // count in left child intersect [ql..qr]
        int leftCount = 0;
        if (ql <= m) {
            leftCount = querySum(Lch[nd], l, m, ql, qr);
        }
        if (leftCount >= k) {
            return queryKth(Lch[nd], l, m, ql, qr, k);
        } else {
            return queryKth(Rch[nd], m+1, r, ql, qr, k - leftCount);
        }
    }
}