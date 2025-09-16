import java.io.*;
import java.util.*;

public class Main {
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
        FastReader(InputStream in) { br = new BufferedReader(new InputStreamReader(in)); }
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

    // Fenwick tree for point updates, prefix‐sum queries
    static class Fenwick {
        int n;
        int[] f;
        Fenwick(int n) {
            this.n = n;
            f = new int[n+1];
        }
        // add v at position i (1-based)
        void update(int i, int v) {
            for (; i <= n; i += i & -i)
                f[i] += v;
        }
        // sum f[1..i]
        int query(int i) {
            int s = 0;
            for (; i > 0; i -= i & -i)
                s += f[i];
            return s;
        }
    }

    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader(System.in);
        PrintWriter out = new PrintWriter(System.out);

        int t = in.nextInt();
        while (t-- > 0) {
            int n = in.nextInt();
            int[] a = new int[n];
            int[] b = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = in.nextInt();
            }
            for (int i = 0; i < n; i++) {
                b[i] = in.nextInt();
            }
            // Map each value of a to its 1-based index
            HashMap<Integer,Integer> pos = new HashMap<>(n*2);
            for (int i = 0; i < n; i++) {
                pos.put(a[i], i+1);
            }
            // Build array c: c[i] = position of b[i] in a
            int[] c = new int[n];
            boolean ok = true;
            for (int i = 0; i < n; i++) {
                Integer p = pos.get(b[i]);
                if (p == null) {
                    // b[i] not in a
                    ok = false;
                    break;
                }
                c[i] = p;
            }
            if (!ok) {
                out.println("NO");
                continue;
            }

            // Compute inversion-parity of c[] in O(n log n)
            Fenwick fenw = new Fenwick(n);
            int invParity = 0;  // 0 = even, 1 = odd
            for (int i = 0; i < n; i++) {
                // number of already-seen elements = i
                // number of those <= c[i] = fenw.query(c[i])
                // so number of those > c[i] = i - fenw.query(c[i])
                int smallerOrEqual = fenw.query(c[i]);
                int greater = i - smallerOrEqual;
                invParity ^= (greater & 1);
                fenw.update(c[i], 1);
            }

            out.println(invParity == 0 ? "YES" : "NO");
        }

        out.flush();
    }
}