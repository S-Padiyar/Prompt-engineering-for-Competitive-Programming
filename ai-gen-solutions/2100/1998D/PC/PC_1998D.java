import java.io.*;
import java.util.*;

public class Main {
    static final int INF = 1_000_000_000;

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder out = new StringBuilder();
        int T = Integer.parseInt(in.readLine());
        while (T-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());

            // 1) Read alternative edges
            List<Integer>[] altFrom = new ArrayList[n+1];
            List<Integer>[] altTo   = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) {
                altFrom[i] = new ArrayList<>();
                altTo[i]   = new ArrayList<>();
            }
            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(in.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                altFrom[u].add(v);
                altTo[v].add(u);
            }

            // 2) distE[i] = shortest #moves Elsie needs 1->i
            int[] distE = new int[n+1];
            Arrays.fill(distE, INF);
            distE[1] = 0;
            for (int i = 2; i <= n; i++) {
                // main edge (i-1)->i
                distE[i] = distE[i-1] + 1;
                // alternative edges u->i
                for (int u : altTo[i]) {
                    distE[i] = Math.min(distE[i], distE[u] + 1);
                }
            }

            // 3) distR[i] = shortest #moves Elsie needs i->n
            int[] distR = new int[n+1];
            Arrays.fill(distR, INF);
            distR[n] = 0;
            for (int i = n-1; i >= 1; i--) {
                // main edge i->i+1
                distR[i] = distR[i+1] + 1;
                // alternative edges i->v
                for (int v : altFrom[i]) {
                    distR[i] = Math.min(distR[i], distR[v] + 1);
                }
            }

            // 4) Compute L[j] = max(2, j-distE[j]+1)
            int[] L = new int[n+1];
            for (int j = 1; j <= n; j++) {
                int x = j - distE[j] + 1;
                if (x < 2) x = 2;
                L[j] = x;
            }

            // 5) Bucket each alt-edge (u->v) by u and by v with weight w = distE[u]+1+distR[v]
            @SuppressWarnings("unchecked")
            List<int[]>[] byU = new ArrayList[n+1], byV = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) {
                byU[i] = new ArrayList<>();
                byV[i] = new ArrayList<>();
            }
            for (int u = 1; u <= n; u++) {
                for (int v : altFrom[u]) {
                    int w = distE[u] + 1 + distR[v];
                    if (w < INF) {
                        byU[u].add(new int[]{v, w});
                        byV[v].add(new int[]{u, w});
                    }
                }
            }

            // 6) Sweep s = 2..n-1
            //    forbidden block = union of [L[j], j] that contain s = a single interval [lo..hi]
            int lo = n+1, hi = 0, ptr = 2;
            // uLim = lo-1 = s-1, vLim = hi
            int uLim = 1, vLim = 0;

            // multiset of jump-weights for edges u->v with u<=uLim and v>vLim
            TreeMap<Integer,Integer> mult = new TreeMap<>();
            BiConsumer<Integer,Integer> add = (w,__) ->
                mult.put(w, mult.getOrDefault(w,0)+1);
            BiConsumer<Integer,Integer> rem = (w,__) -> {
                int c = mult.get(w);
                if (c==1) mult.remove(w);
                else       mult.put(w,c-1);
            };

            // Initialize for s=2 => uLim=1 => add edges from u=1
            for (int[] vw : byU[1]) {
                if (vw[0] > vLim) add.accept(vw[1],0);
            }

            char[] ans = new char[n];
            ans[1] = '1';  // s=1 => Bessie collapses island 1 immediately

            for (int s = 2; s < n; s++) {
                // (a) grow hi while L[ptr] <= s
                while (ptr <= n && L[ptr] <= s) {
                    int oldHi = hi;
                    hi = ptr;
                    ptr++;
                    // remove all edges whose v in (oldHi..hi]
                    for (int v = oldHi+1; v <= hi; v++) {
                        for (int[] uw : byV[v]) {
                            if (uw[0] <= uLim) rem.accept(uw[1],0);
                        }
                    }
                }
                // (b) raise uLim = s-1 => add edges whose u in (old_uLim..s-1]
                int newULim = s-1;
                for (int u = uLim+1; u <= newULim; u++) {
                    for (int[] vw : byU[u]) {
                        if (vw[0] > hi) add.accept(vw[1],0);
                    }
                }
                uLim = newULim;
                vLim = hi;

                // (c) compute Elsie's best time D(s)
                int D;
                if (s > hi) {
                    // block empty
                    D = distE[n];
                } else if (mult.isEmpty()) {
                    D = INF;
                } else {
                    D = mult.firstKey();
                }

                // (d) compare with Bessie's time (n-s-1)
                ans[s] = (D <= n-s-1) ? '0' : '1';
            }

            // output s=1..n-1
            for (int i = 1; i < n; i++) out.append(ans[i]);
            out.append('\n');
        }
        System.out.print(out);
    }
}