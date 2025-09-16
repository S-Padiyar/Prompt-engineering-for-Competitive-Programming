import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            int n = Integer.parseInt(in.readLine().trim());
            int[] a = new int[n], b = new int[n];
            StringTokenizer st = new StringTokenizer(in.readLine());
            for (int i = 0; i < n; i++) a[i] = Integer.parseInt(st.nextToken());
            st = new StringTokenizer(in.readLine());
            for (int i = 0; i < n; i++) b[i] = Integer.parseInt(st.nextToken());

            // 1) Check same multiset of values
            int[] sa = a.clone();
            int[] sbv = b.clone();
            Arrays.sort(sa);
            Arrays.sort(sbv);
            if (!Arrays.equals(sa, sbv)) {
                sb.append("NO\n");
                continue;
            }

            // 2) Build a map from value->sortedIndex
            //    so we can locate each value's position in 'a' and 'b'.
            //    Then compute the (n-1) gap-lengths for adjacent sorted-values.
            Map<Integer,Integer> indexInA = new HashMap<>(n);
            Map<Integer,Integer> indexInB = new HashMap<>(n);
            for (int i = 0; i < n; i++) {
                indexInA.put(a[i], i);
                indexInB.put(b[i], i);
            }

            int[] da = new int[n-1], db = new int[n-1];
            for (int i = 0; i + 1 < n; i++) {
                int v1 = sa[i], v2 = sa[i+1];
                int pa = indexInA.get(v1), pa2 = indexInA.get(v2);
                int pb = indexInB.get(v1), pb2 = indexInB.get(v2);
                da[i] = Math.abs(pa2 - pa);
                db[i] = Math.abs(pb2 - pb);
            }
            Arrays.sort(da);
            Arrays.sort(db);
            if (!Arrays.equals(da, db)) {
                sb.append("NO\n");
                continue;
            }

            // 3) Check permutation parity of a and b (relative to the sorted array).
            //    We compress a[] and b[] to their ranks 0..n-1 and count inversions mod 2.
            int[] compA = new int[n], compB = new int[n];
            for (int i = 0; i < n; i++) {
                // find the rank of a[i] in the sorted array sa[]: binary search
                compA[i] = Arrays.binarySearch(sa, a[i]);
                compB[i] = Arrays.binarySearch(sa, b[i]);
            }

            int parityA = inversionParity(compA);
            int parityB = inversionParity(compB);
            if (parityA != parityB) {
                sb.append("NO\n");
            } else {
                sb.append("YES\n");
            }
        }

        System.out.print(sb);
    }

    // Return inversion count mod 2 of the permutation p[0..n-1], values are 0..n-1
    // We use a Fenwick tree to accumulate counts and accumulate the parity.
    private static int inversionParity(int[] p) {
        int n = p.length;
        Fenwick fw = new Fenwick(n);
        int parity = 0;
        for (int i = 0; i < n; i++) {
            int x = p[i];
            // how many of the already‐seen elements are > x ?
            // total seen so far = i, number <= x = fw.sum(x)
            int lessEq = fw.sum(x);
            int greater = i - lessEq;
            if ((greater & 1) != 0) parity ^= 1;
            fw.update(x, 1);
        }
        return parity;
    }

    // Simple Fenwick (BIT) for sums on 0..n-1 (internally 1..n).
    static class Fenwick {
        int n;
        int[] f;
        Fenwick(int n) {
            this.n = n;
            f = new int[n+1];
        }
        // add v at position i (0-based)
        void update(int i, int v) {
            for (int x = i+1; x <= n; x += x & -x)
                f[x] += v;
        }
        // sum of f[0..i]  (0-based)
        int sum(int i) {
            int s = 0;
            for (int x = i+1; x > 0; x -= x & -x)
                s += f[x];
            return s;
        }
    }
}