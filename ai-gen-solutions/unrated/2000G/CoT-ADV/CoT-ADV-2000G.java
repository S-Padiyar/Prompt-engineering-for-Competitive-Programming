import java.io.*;
import java.util.*;

public class Main {
    static class Edge {
        int to;
        int busTime, walkTime;
        Edge(int v, int b, int w) { to = v; busTime = b; walkTime = w; }
    }
    static final long INF = Long.MAX_VALUE / 4;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int T = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();

        while (T-- > 0) {
            // Read n, m
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());

            // Read t0, t1, t2
            st = new StringTokenizer(br.readLine());
            long t0 = Long.parseLong(st.nextToken());
            long t1 = Long.parseLong(st.nextToken());
            long t2 = Long.parseLong(st.nextToken());

            // Build graph
            List<Edge>[] graph = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) graph[i] = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                int l1 = Integer.parseInt(st.nextToken());
                int l2 = Integer.parseInt(st.nextToken());
                graph[u].add(new Edge(v, l1, l2));
                graph[v].add(new Edge(u, l1, l2));
            }

            // 1) Compute f(0): earliest arrival if start at time 0
            long f0 = dijkstraArrivalTime(n, graph, t1, t2, t0, 0L);
            if (f0 > t0) {
                // Not possible even if we start at t=0
                sb.append(-1).append('\n');
                continue;
            }

            // We only need to search S in [0, t0 - f0]
            long low = 0, high = t0 - f0, ans = 0;
            while (low <= high) {
                long mid = (low + high) / 2;
                // If we start at mid and still can arrive by t0, it's feasible
                long arrival = dijkstraArrivalTime(n, graph, t1, t2, t0, mid);
                if (arrival <= t0) {
                    ans = mid;          // feasible => try later departure
                    low = mid + 1;
                } else {
                    high = mid - 1;     // not feasible => depart earlier
                }
            }
            sb.append(ans).append('\n');
        }

        System.out.print(sb);
    }

    /**
     * Run a time‐aware Dijkstra from node 1 to node n.
     * dist[u] = earliest time we can arrive at u.
     * We start at dist[1] = startTime.
     * walking edges (l2) always cost +l2.
     * bus edges (l1) cost +l1, but if the bus would overlap
     * [t1,t2], we wait until t2 first.
     *
     * Returns dist[n] (possibly INF).
     */
    static long dijkstraArrivalTime(int n, List<Edge>[] graph,
                                    long t1, long t2, long t0,
                                    long startTime) {
        long[] dist = new long[n+1];
        Arrays.fill(dist, INF);
        dist[1] = startTime;

        // Min‐heap on (current arrival time, node)
        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingLong(a->a[0]));
        pq.offer(new long[]{startTime, 1});

        while (!pq.isEmpty()) {
            long[] top = pq.poll();
            long curT = top[0];
            int u = (int)top[1];
            if (curT > dist[u]) continue;
            if (u == n) {
                // once we pop node n, it's its final shortest time
                return curT;
            }
            // If even the current time > t0, no need to expand further
            if (curT > t0) continue;

            for (Edge e: graph[u]) {
                // 1) walking
                long wtime = curT + e.walkTime;
                if (wtime < dist[e.to]) {
                    dist[e.to] = wtime;
                    pq.offer(new long[]{wtime, e.to});
                }
                // 2) bus
                // if we would start now and run into the call window [t1,t2],
                // must wait until t2
                long depart = (curT < t2 && curT + e.busTime > t1) ? t2 : curT;
                long btime = depart + e.busTime;
                if (btime < dist[e.to]) {
                    dist[e.to] = btime;
                    pq.offer(new long[]{btime, e.to});
                }
            }
        }
        return dist[n];
    }
}