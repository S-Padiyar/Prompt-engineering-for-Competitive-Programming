import java.io.*;
import java.util.*;

public class Main {
    static final long INF = (long)9e18;
    // We'll have at most 2*n states and about 4*m + h edges per test.
    // Over all tests n <= 2e5, m <= 2e5, so we'll allocate generously:
    static final int MAXN2 = 400_005;      // 2 * max n
    static final int MAXE  = 1_200_000;    // up to ~4*m + h ~ 1e6
    
    static int[] head = new int[MAXN2];
    static int[] to   = new int[MAXE];
    static int[] wgt  = new int[MAXE];
    static int[] nxt  = new int[MAXE];
    static int edgeCnt;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        StringBuilder sb = new StringBuilder();
        
        int T = Integer.parseInt(br.readLine().trim());
        while (T-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());
            int h = Integer.parseInt(st.nextToken());

            // Read horse vertices (1-based in input, convert to 0-based)
            st = new StringTokenizer(br.readLine());
            int[] horses = new int[h];
            for (int i = 0; i < h; i++) {
                horses[i] = Integer.parseInt(st.nextToken()) - 1;
            }

            // Initialize adjacency list for 2n states
            int N2 = 2 * n; 
            edgeCnt = 0;
            Arrays.fill(head, 0, N2, -1);

            // Read the edges and add 4 directed arcs per undirected edge
            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken()) - 1;
                int v = Integer.parseInt(st.nextToken()) - 1;
                int w = Integer.parseInt(st.nextToken());
                // on foot <-> on foot
                addEdge(u,   v,   w);
                addEdge(v,   u,   w);
                // on horse <-> on horse  (half the time)
                addEdge(u+n, v+n, w/2);
                addEdge(v+n, u+n, w/2);
            }

            // Add zero-cost arcs for mounting horse
            for (int a : horses) {
                addEdge(a, a + n, 0);
            }

            // Dijkstra from Marian's start = state 0 (vertex 0 on foot)
            long[] distM = new long[N2];
            dijkstra(0, N2, distM);

            // Dijkstra from Robin's start = state (n-1 on foot)
            long[] distR = new long[N2];
            dijkstra(n - 1, N2, distR);

            // Compute answer = min over all original vertices of max(arrive times)
            long ans = INF;
            for (int u = 0; u < n; u++) {
                long tM = Math.min(distM[u],     distM[u + n]);
                long tR = Math.min(distR[u],     distR[u + n]);
                if (tM < INF && tR < INF) {
                    ans = Math.min(ans, Math.max(tM, tR));
                }
            }
            sb.append(ans == INF ? -1 : ans).append('\n');
        }
        
        System.out.print(sb);
    }

    // Add a directed edge u -> v of cost w
    static void addEdge(int u, int v, int w) {
        to[edgeCnt]  = v;
        wgt[edgeCnt] = w;
        nxt[edgeCnt] = head[u];
        head[u]      = edgeCnt++;
    }

    // We store (distance<<20) | node in the PQ to avoid building a custom class.
    // We must ensure distance<<20 stays within 63 bits (it does, since dist<=2e11).
    static final int SHIFT = 20;
    static final int MASK  = (1 << SHIFT) - 1;

    static void dijkstra(int src, int N, long[] dist) {
        Arrays.fill(dist, 0, N, INF);
        dist[src] = 0L;
        PriorityQueue<Long> pq = new PriorityQueue<>();
        pq.add((0L << SHIFT) | src);

        while (!pq.isEmpty()) {
            long c = pq.poll();
            int u       = (int)(c & MASK);
            long d_u    = c >>> SHIFT;
            if (d_u != dist[u]) continue;   // stale entry

            // Relax all out‐edges
            for (int e = head[u]; e != -1; e = nxt[e]) {
                int v     = to[e];
                long ndist = d_u + wgt[e];
                if (ndist < dist[v]) {
                    dist[v] = ndist;
                    pq.add((ndist << SHIFT) | v);
                }
            }
        }
    }
}