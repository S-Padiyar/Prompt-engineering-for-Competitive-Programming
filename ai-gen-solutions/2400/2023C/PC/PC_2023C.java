import java.io.*;
import java.util.*;

public class Main {
    static int n, k;
    static ArrayList<Integer>[] g1, g2;
    static int[] a, b, p, q;
    static int[] out1, in1, out2, in2;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int T = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();
        while (T-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            n = Integer.parseInt(st.nextToken());
            k = Integer.parseInt(st.nextToken());

            // read a_i for G1
            a = new int[n];
            st = new StringTokenizer(br.readLine());
            int sumA = 0;
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
                sumA += a[i];
            }

            // read G1 edges
            g1 = new ArrayList[n];
            for (int i = 0; i < n; i++) g1[i] = new ArrayList<>();
            int m1 = Integer.parseInt(br.readLine().trim());
            for (int i = 0; i < m1; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken()) - 1;
                int v = Integer.parseInt(st.nextToken()) - 1;
                g1[u].add(v);
            }

            // read b_i for G2
            b = new int[n];
            st = new StringTokenizer(br.readLine());
            int sumB = 0;
            for (int i = 0; i < n; i++) {
                b[i] = Integer.parseInt(st.nextToken());
                sumB += b[i];
            }

            // read G2 edges
            g2 = new ArrayList[n];
            for (int i = 0; i < n; i++) g2[i] = new ArrayList<>();
            int m2 = Integer.parseInt(br.readLine().trim());
            for (int i = 0; i < m2; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken()) - 1;
                int v = Integer.parseInt(st.nextToken()) - 1;
                g2[u].add(v);
            }

            // QUICK checks
            if (sumA + sumB != n) {
                // cannot even match counts
                sb.append("NO\n");
                continue;
            }
            if (sumA == 0 || sumB == 0) {
                // all edges in one direction => no mixed cycles => always OK
                sb.append("YES\n");
                continue;
            }

            // Build potentials p[] on G1 and q[] on G2
            p = new int[n];
            Arrays.fill(p, -1);
            p[0] = 0;
            dfsBuild(0, g1, p);

            q = new int[n];
            Arrays.fill(q, -1);
            q[0] = 0;
            dfsBuild(0, g2, q);

            // Build the residue‐histograms
            out1 = new int[k];  // G1‐out residues
            in1  = new int[k];  // G1‐in  residues
            out2 = new int[k];  // G2‐out residues
            in2  = new int[k];  // G2‐in  residues
            for (int i = 0; i < n; i++) {
                if (a[i] == 1) out1[p[i]]++;
                else            in1[p[i]]++;
                if (b[i] == 1) out2[q[i]]++;
                else            in2[q[i]]++;
            }

            // We must check if out1 is some cyclic shift s of in2,
            // AND      out2 is the *same* shift+2 of in1.
            // We do two Z‐algorithm searches:
            //   find all s so that out1 == rotateLeft(in2,s),
            //   find all t so that out2 == rotateLeft(in1,t).
            // Then ask: is there s with (s-2 mod k) among those t's ?

            boolean[] canS = findShifts(in2, out1);
            boolean[] canT = findShifts(in1, out2);

            boolean ok = false;
            for (int s = 0; s < k; s++) {
                if (!canS[s]) continue;
                int t = (s - 2) % k;
                if (t < 0) t += k;
                if (canT[t]) { ok = true; break; }
            }
            sb.append(ok ? "YES\n" : "NO\n");
        }
        System.out.print(sb);
    }

    // Build the mod‐k “depth” arrays by a DFS labeling.
    static void dfsBuild(int root, ArrayList<Integer>[] g, int[] depth) {
        // depth[root] = 0 already
        int[] stack = new int[g.length];
        int top = 0;
        stack[top++] = root;
        while (top > 0) {
            int u = stack[--top];
            int du = depth[u];
            for (int v : g[u]) {
                if (depth[v] < 0) {
                    depth[v] = (du + 1) % k;
                    stack[top++] = v;
                }
            }
        }
    }

    // returns a boolean array shiftOk[0..k-1], where shiftOk[s]=true
    // iff rotating `text` left by s positions gives exactly `pattern`.
    static boolean[] findShifts(int[] text, int[] pattern) {
        // We build the array A = pattern + [-1] + text+text, run Z-algo,
        // then for every i in the text+text portion with Z[i]>=k,
        // we record the shift s=i-(1+pattern.length).
        int K = pattern.length;
        int total = K + 1 + 2*K;
        int[] A = new int[total];
        for (int i = 0; i < K; i++) A[i] = pattern[i];
        A[K] = -1; // separator
        for (int i = 0; i < 2*K; i++) {
            A[K+1 + i] = text[i % K];
        }

        int[] Z = new int[total];
        // standard Z‐function
        int L = 0, R = 0;
        for (int i = 1; i < total; i++) {
            if (i <= R) Z[i] = Math.min(R - i + 1, Z[i - L]);
            while (i + Z[i] < total && A[Z[i]] == A[i + Z[i]]) {
                Z[i]++;
            }
            if (i + Z[i] - 1 > R) {
                L = i; R = i + Z[i] - 1;
            }
        }

        boolean[] shiftOk = new boolean[K];
        int start = K + 1;        // start of the doubled‐text region in A
        int end   = K + 1 + 2*K;  // exclusive end
        for (int i = start; i < end; i++) {
            if (Z[i] >= K) {
                int s = i - start;   // s in [0..2K-K] = [0..K]
                if (s < K) shiftOk[s] = true;
            }
        }
        return shiftOk;
    }
}