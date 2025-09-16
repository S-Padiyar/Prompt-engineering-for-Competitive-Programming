import java.io.*;
import java.util.*;

public class Main {
    static final int MAXN = 100_000;
    static final int T = 50;       // number of smallest elements to keep in each node

    int n, q;
    int[] a;
    List<Integer>[] seg;

    @SuppressWarnings("unchecked")
    public Main() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer tk = new StringTokenizer(br.readLine());
        n = Integer.parseInt(tk.nextToken());
        q = Integer.parseInt(tk.nextToken());
        a = new int[n];
        tk = new StringTokenizer(br.readLine());
        for (int i = 0; i < n; i++) {
            a[i] = Integer.parseInt(tk.nextToken());
        }
        seg = new List[4*n];
        build(1, 0, n-1);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < q; i++) {
            tk = new StringTokenizer(br.readLine());
            int L = Integer.parseInt(tk.nextToken())-1;
            int R = Integer.parseInt(tk.nextToken())-1;
            List<Integer> smalls = query(1, 0, n-1, L, R);
            // smalls already sorted
            if (canTwoTriangles(smalls)) {
                sb.append("YES\n");
            } else {
                sb.append("NO\n");
            }
        }
        System.out.print(sb);
    }

    // build segment tree: each node keeps up to T smallest elements, sorted
    private void build(int idx, int l, int r) {
        if (l == r) {
            List<Integer> one = new ArrayList<>();
            one.add(a[l]);
            seg[idx] = one;
            return;
        }
        int mid = (l + r) >>> 1;
        build(idx<<1, l, mid);
        build(idx<<1|1, mid+1, r);
        seg[idx] = mergeSmalls(seg[idx<<1], seg[idx<<1|1]);
    }

    // merge two sorted lists, keep only first T
    private List<Integer> mergeSmalls(List<Integer> A, List<Integer> B) {
        List<Integer> C = new ArrayList<>(T+1);
        int i=0, j=0;
        while (C.size() < T && (i < A.size() || j < B.size())) {
            if (j >= B.size() || (i < A.size() && A.get(i) <= B.get(j))) {
                C.add(A.get(i++));
            } else {
                C.add(B.get(j++));
            }
        }
        return C;
    }

    // query segment tree for [L..R], returns sorted list of up to T smallest
    private List<Integer> query(int idx, int l, int r, int L, int R) {
        if (r < L || R < l) return Collections.emptyList();
        if (L <= l && r <= R) {
            return seg[idx];
        }
        int mid = (l + r) >>> 1;
        List<Integer> left = query(idx<<1, l, mid, L, R);
        List<Integer> right = query(idx<<1|1, mid+1, r, L, R);
        return mergeSmalls(left, right);
    }

    // Given a sorted list S (size up to T), determine if we can pick two disjoint
    // triples that each form a nondegenerate triangle.
    private boolean canTwoTriangles(List<Integer> S) {
        int m = S.size();
        // First, collect all i s.t. S[i]+S[i+1]>S[i+2]
        // Then see if we have two such i's at distance >= 3
        List<Integer> good = new ArrayList<>();
        for (int i = 0; i + 2 < m; i++) {
            long x = S.get(i), y = S.get(i+1), z = S.get(i+2);
            if (x + y > z) {
                good.add(i);
            }
        }
        // look for two indices i<j with j>=i+3
        int sz = good.size();
        if (sz >= 2) {
            int first = good.get(0);
            for (int k = 1; k < sz; k++) {
                if (good.get(k) >= first + 3) {
                    return true;
                }
            }
        }
        // As a fallback, do the simple greedy: pick the first triangle, remove its elements,
        // then see if any other triangle remains.
        if (good.size() == 0) return false;
        int i0 = good.get(0);
        // Build a smaller list without S[i0], S[i0+1], S[i0+2]
        List<Integer> rem = new ArrayList<>(m-3);
        for (int i = 0; i < m; i++) {
            if (i==i0 || i==i0+1 || i==i0+2) continue;
            rem.add(S.get(i));
        }
        int mm = rem.size();
        for (int i = 0; i + 2 < mm; i++) {
            long x = rem.get(i), y = rem.get(i+1), z = rem.get(i+2);
            if (x + y > z) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        new Main();
    }
}