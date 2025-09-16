import java.io.*;
import java.util.*;

public class Main {
    static class Edge {
        int to;
        long busTime, walkTime;
        Edge(int _to, long _b, long _w) {
            to = _to;
            busTime = _b;
            walkTime = _w;
        }
    }

    static int n, m;
    static long T0, T1, T2;
    static List<Edge>[] adj;
    static final long INF = Long.MAX_VALUE / 4;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        int tc = Integer.parseInt(br.readLine().trim());
        while (tc-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            n = Integer.parseInt(st.nextToken());
            m = Integer.parseInt(st.nextToken());
            st = new StringTokenizer(br.readLine());
            T0 = Long.parseLong(st.nextToken());
            T1 = Long.parseLong(st.nextToken());
            T2 = Long.parseLong(st.nextToken());
            // build adjacency
            adj = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) {
                adj[i] = new ArrayList<>();
            }
            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                long l1 = Long.parseLong(st.nextToken());
                long l2 = Long.parseLong(st.nextToken());
                adj[u].add(new Edge(v, l1, l2));
                adj[v].add(new Edge(u, l1, l2));
            }

            // Binary search the latest departure x in [0..T0]
            long lo = 0, hi = T0, ans = -1;
            while (lo <= hi) {
                long mid = (lo + hi) >>> 1;
                if (canReach(mid)) {
                    ans = mid;
                    lo = mid + 1;
                } else {
                    hi = mid - 1;
                }
            }
            sb.append(ans).append('\n');
        }
        System.out.print(sb);
    }

    /**
     * Check if we can depart at time x from node 1 and reach node n by time T0,
     * respecting that bus-rides cannot intersect [T1,T2).
     */
    static boolean canReach(long x) {
        // dist[u] = earliest time we can arrive at u
        long[] dist = new long[n+1];
        Arrays.fill(dist, INF);
        dist[1] = x;

        // Min-heap by arrival-time
        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingLong(a -> a[0]));
        pq.offer(new long[]{x, 1});

        while (!pq.isEmpty()) {
            long[] cur = pq.poll();
            long t = cur[0];
            int u = (int) cur[1];
            if (t > dist[u]) continue;
            if (t > T0) continue;         // no point if we already exceed T0
            if (u == n) return true;     // reached n in time

            for (Edge e : adj[u]) {
                int v = e.to;

                // 1) try walking
                long wt = t + e.walkTime;
                if (wt <= T0 && wt < dist[v]) {
                    dist[v] = wt;
                    pq.offer(new long[]{wt, v});
                }

                // 2) try bus
                long depart;
                // if we can start & finish before the call window ...
                if (t + e.busTime <= T1 || t >= T2) {
                    depart = t;
                } else {
                    // otherwise wait until T2 to start
                    depart = T2;
                }
                long bt = depart + e.busTime;
                if (bt <= T0 && bt < dist[v]) {
                    dist[v] = bt;
                    pq.offer(new long[]{bt, v});
                }
            }
        }
        return false;
    }
}