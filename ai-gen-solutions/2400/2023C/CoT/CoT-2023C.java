import java.io.*;
import java.util.*;

public class Main {
    static final int MAXN = 200000;
    static List<Integer>[] adj1 = new ArrayList[MAXN+1];
    static List<Integer>[] adj2 = new ArrayList[MAXN+1];
    static int[] label1 = new int[MAXN+1];
    static int[] label2 = new int[MAXN+1];
    static boolean[] vis    = new boolean[MAXN+1];
    static int[] queue      = new int[MAXN+1];

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder out = new StringBuilder();
        for (int i = 1; i <= MAXN; i++) {
            adj1[i] = new ArrayList<>();
            adj2[i] = new ArrayList<>();
        }

        int T = Integer.parseInt(br.readLine().trim());
        while (T-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int k = Integer.parseInt(st.nextToken());

            // read types for G1
            st = new StringTokenizer(br.readLine());
            int[] a = new int[n+1];
            int o1 = 0;
            for (int i = 1; i <= n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
                if (a[i] == 1) o1++;
            }

            // read edges of G1
            int m1 = Integer.parseInt(br.readLine().trim());
            for (int i = 1; i <= n; i++) adj1[i].clear();
            for (int i = 0; i < m1; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                adj1[u].add(v);
            }

            // read types for G2
            st = new StringTokenizer(br.readLine());
            int[] b = new int[n+1];
            int o2 = 0;
            for (int i = 1; i <= n; i++) {
                b[i] = Integer.parseInt(st.nextToken());
                if (b[i] == 1) o2++;
            }

            // read edges of G2
            int m2 = Integer.parseInt(br.readLine().trim());
            for (int i = 1; i <= n; i++) adj2[i].clear();
            for (int i = 0; i < m2; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                adj2[u].add(v);
            }

            // Quick feasibility by counts
            if (o1 + o2 != n) {
                out.append("NO\n");
                continue;
            }
            // For k=1 or k=2, any matching is safe once counts match
            if (k <= 2) {
                out.append("YES\n");
                continue;
            }

            // 1) Build the residue-labeling for G1 and G2
            bfsLabel(1, n, k, adj1, label1);
            bfsLabel(1, n, k, adj2, label2);

            // 2) Build histograms
            int[] H1out = new int[k], H1in = new int[k];
            int[] H2out = new int[k], H2in = new int[k];

            for (int i = 1; i <= n; i++) {
                int r1 = label1[i];
                if (a[i] == 1) H1out[r1]++; else H1in[r1]++;
                int r2 = label2[i];
                if (b[i] == 1) H2out[r2]++; else H2in[r2]++;
            }

            // 3) We need a shift C so that
            //    H1out[r] == H2in[(r+C)%k]  for all r,
            //    H1in [r] == H2out[(r+C+2)%k]  for all r.
            // Do these two cyclic-match checks by KMP.

            // (a) Find all C so that  H1out matches H2in shifted by C
            List<Integer> shifts1 = findCyclicMatches(H1out, H2in, k);

            // (b) For the second, define H1in2[r] = H1in[(r-2)%k],
            //     and match that against H2out shifted by C.
            int[] H1in2 = new int[k];
            for (int r = 0; r < k; r++) {
                int rr = (r - 2) % k;
                if (rr < 0) rr += k;
                H1in2[r] = H1in[rr];
            }
            List<Integer> shifts2 = findCyclicMatches(H1in2, H2out, k);

            // check if there's a shift in common
            boolean ok = false;
            // put smaller list into a hash set for O(1) lookup
            Set<Integer> set2 = new HashSet<>(shifts2);
            for (int C : shifts1) {
                if (set2.contains(C)) {
                    ok = true;
                    break;
                }
            }
            out.append(ok ? "YES\n" : "NO\n");
        }

        System.out.print(out);
    }

    /** BFS on directed graph to assign labels mod k so that
        every edge u->v has label[v] = (label[u]+1)%k. */
    static void bfsLabel(int start, int n, int k, List<Integer>[] adj, int[] label) {
        Arrays.fill(vis, 0, n+1, false);
        int head = 0, tail = 0;
        queue[tail++] = start;
        vis[start] = true;
        label[start] = 0;
        while (head < tail) {
            int u = queue[head++];
            int lu = label[u];
            for (int v : adj[u]) {
                if (!vis[v]) {
                    vis[v] = true;
                    label[v] = (lu + 1) % k;
                    queue[tail++] = v;
                }
            }
        }
        // graph is strongly connected, so all n vertices get visited
    }

    /**
     * We look for ALL shifts C in [0..k-1] so that
     *   P[r] == T[(r+C)%k] for all r=0..k-1,
     * i.e. the array P matches the array T cyclically shifted by C.
     * We do it by making a text of length 2k : T|T, and then KMP
     * to find all occurrences of P in that text, restricted to start<k.
     */
    static List<Integer> findCyclicMatches(int[] P, int[] T, int k) {
        // build text TT = T, then T again
        int[] TT = new int[2*k];
        for (int i = 0; i < k; i++) {
            TT[i]     = T[i];
            TT[i + k] = T[i];
        }

        // build KMP failure for P
        int[] fail = new int[k];
        for (int i = 1, j = 0; i < k; i++) {
            while (j > 0 && P[i] != P[j]) j = fail[j - 1];
            if (P[i] == P[j]) j++;
            fail[i] = j;
        }

        // search P in TT
        List<Integer> res = new ArrayList<>();
        for (int i = 0, j = 0; i < 2*k; i++) {
            while (j > 0 && TT[i] != P[j]) j = fail[j - 1];
            if (TT[i] == P[j]) j++;
            if (j == k) {
                int start = i - k + 1; // potential shift
                if (start < k) {
                    res.add(start);
                }
                j = fail[j - 1];
            }
        }
        return res;
    }
}