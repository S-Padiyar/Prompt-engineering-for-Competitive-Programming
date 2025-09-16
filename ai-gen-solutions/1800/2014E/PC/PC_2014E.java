import java.io.*;
import java.util.*;

public class Main {
    static final long INF = Long.MAX_VALUE / 4;

    static class Edge {
        int to;
        int w;
        Edge(int t, int ww) { to = t; w = ww; }
    }

    // Dijkstra over 2*n states: (u,0) = on foot, (u,1) = on horseback.
    // horse[u] = true if there's a horse at u.
    // Returns an array best[u] = min time to reach u in either state.
    static long[] multiStateDijkstra(int n, List<Edge>[] adj, boolean[] horse, int start) {
        int N = 2 * n;
        long[] dist = new long[N];
        Arrays.fill(dist, INF);
        // priority queue of (dist, nodeIndex)
        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingLong(a -> a[0]));

        // start at (start, state=0)
        dist[start] = 0L;
        pq.add(new long[]{0L, start});

        while (!pq.isEmpty()) {
            long[] cur = pq.poll();
            long cd = cur[0];
            int idx = (int) cur[1];
            if (cd > dist[idx]) continue;
            
            int u = idx < n ? idx : (idx - n);
            int state = idx < n ? 0 : 1;

            // If on foot and there's a horse here, try mounting for 0 cost:
            if (state == 0 && horse[u]) {
                int nxt = u + n; // (u,1)
                if (dist[nxt] > cd) {
                    dist[nxt] = cd;
                    pq.add(new long[]{cd, nxt});
                }
            }

            // Traverse edges
            for (Edge e : adj[u]) {
                int v = e.to;
                long cost = cd + (state == 0 ? e.w : e.w / 2);
                int nxtIdx = (state == 0 ? v : v + n);
                if (dist[nxtIdx] > cost) {
                    dist[nxtIdx] = cost;
                    pq.add(new long[]{cost, nxtIdx});
                }
            }
        }

        // Build the best[u] = min(dist[u,0], dist[u,1])
        long[] best = new long[n];
        for (int u = 0; u < n; u++) {
            best[u] = Math.min(dist[u], dist[u + n]);
        }
        return best;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());
            int h = Integer.parseInt(st.nextToken());

            // read horse locations
            boolean[] horse = new boolean[n];
            st = new StringTokenizer(in.readLine());
            for (int i = 0; i < h; i++) {
                int v = Integer.parseInt(st.nextToken()) - 1;
                horse[v] = true;
            }

            // build graph
            List<Edge>[] adj = new ArrayList[n];
            for (int i = 0; i < n; i++) adj[i] = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(in.readLine());
                int u = Integer.parseInt(st.nextToken()) - 1;
                int v = Integer.parseInt(st.nextToken()) - 1;
                int w = Integer.parseInt(st.nextToken());
                adj[u].add(new Edge(v, w));
                adj[v].add(new Edge(u, w));
            }

            // Dijkstra from Marian's start (1 → index 0)
            long[] Dm = multiStateDijkstra(n, adj, horse, 0);
            // Dijkstra from Robin's start (n → index n-1)
            long[] Dr = multiStateDijkstra(n, adj, horse, n - 1);

            // find min over vertices of max(Dm[u], Dr[u])
            long ans = INF;
            for (int u = 0; u < n; u++) {
                if (Dm[u] < INF && Dr[u] < INF) {
                    long meet = Math.max(Dm[u], Dr[u]);
                    if (meet < ans) ans = meet;
                }
            }
            out.println(ans == INF ? -1 : ans);
        }
        out.flush();
    }
}