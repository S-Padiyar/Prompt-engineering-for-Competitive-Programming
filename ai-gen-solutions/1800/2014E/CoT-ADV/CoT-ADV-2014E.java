import java.io.*;
import java.util.*;

public class Main {
    static final long INF = Long.MAX_VALUE / 4;
    
    static class Edge {
        int to;
        int w;
        Edge(int _t, int _w) { to = _t; w = _w; }
    }
    
    static class State {
        int node;
        long dist;
        int horseId;
        State(int _n, long _d, int _h) {
            node = _n; dist = _d; horseId = _h;
        }
    }
    
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        
        int t = Integer.parseInt(br.readLine());
        StringBuilder sb = new StringBuilder();
        
        while (t-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());
            int h = Integer.parseInt(st.nextToken());
            
            // Read horse locations
            st = new StringTokenizer(br.readLine());
            int[] horseAt = new int[h];
            for (int i = 0; i < h; i++) {
                horseAt[i] = Integer.parseInt(st.nextToken());
            }
            
            // Build graph
            List<Edge>[] adj = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) adj[i] = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                int w = Integer.parseInt(st.nextToken());
                adj[u].add(new Edge(v, w));
                adj[v].add(new Edge(u, w));
            }
            
            // 1) Dijkstra on foot from Marian (1)
            long[] distM = new long[n+1];
            dijkstra(1, distM, adj, n);
            // 2) Dijkstra on foot from Robin (n)
            long[] distR = new long[n+1];
            dijkstra(n, distR, adj, n);
            
            // Prepare 2-label arrays for horses:
            // bestHorseM[v][0..1], horseIdM[v][..], same for Robin.
            long[][] bestHM = new long[n+1][2];
            int[][]  idHM   = new int[n+1][2];
            long[][] bestHR = new long[n+1][2];
            int[][]  idHR   = new int[n+1][2];
            for (int i = 1; i <= n; i++) {
                bestHM[i][0] = bestHM[i][1] = INF;
                idHM[i][0] = idHM[i][1] = -1;
                bestHR[i][0] = bestHR[i][1] = INF;
                idHR[i][0] = idHR[i][1] = -1;
            }
            
            // 3) Multi‐label Dijkstra for Marian using horses
            multiLabelDijkstra(distM, horseAt, adj, bestHM, idHM);
            // 4) Multi‐label Dijkstra for Robin using horses
            multiLabelDijkstra(distR, horseAt, adj, bestHR, idHR);
            
            // Now combine the four cases:
            long answer = INF;
            
            // Case 1: no horses
            for (int v = 1; v <= n; v++) {
                if (distM[v] < INF && distR[v] < INF) {
                    long time = Math.max(distM[v], distR[v]);
                    if (time < answer) answer = time;
                }
            }
            
            // Case 2: only Marian uses
            for (int v = 1; v <= n; v++) {
                if (bestHM[v][0] < INF && distR[v] < INF) {
                    long time = Math.max(bestHM[v][0], distR[v]);
                    if (time < answer) answer = time;
                }
            }
            
            // Case 3: only Robin uses
            for (int v = 1; v <= n; v++) {
                if (bestHR[v][0] < INF && distM[v] < INF) {
                    long time = Math.max(distM[v], bestHR[v][0]);
                    if (time < answer) answer = time;
                }
            }
            
            // Case 4: both use distinct horses
            for (int v = 1; v <= n; v++) {
                long m1 = bestHM[v][0], m2 = bestHM[v][1];
                long r1 = bestHR[v][0], r2 = bestHR[v][1];
                int jm1 = idHM[v][0], jm2 = idHM[v][1];
                int jr1 = idHR[v][0], jr2 = idHR[v][1];
                
                if (m1 == INF || r1 == INF) continue;
                long best = INF;
                // If top horses differ, take them
                if (jm1 != jr1) {
                    best = Math.max(m1, r1);
                } else {
                    // try (m2, r1) or (m1, r2)
                    if (jm2 >= 0) {
                        best = Math.min(best, Math.max(m2, r1));
                    }
                    if (jr2 >= 0) {
                        best = Math.min(best, Math.max(m1, r2));
                    }
                }
                if (best < answer) answer = best;
            }
            
            sb.append(answer >= INF/2 ? -1 : answer).append('\n');
        }
        
        System.out.print(sb.toString());
    }
    
    /** Standard Dijkstra from source s, on foot (full edge weights). */
    static void dijkstra(int s, long[] dist, List<Edge>[] adj, int n) {
        Arrays.fill(dist, INF);
        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingLong(a -> a[0]));
        dist[s] = 0;
        pq.add(new long[]{0, s});
        
        while (!pq.isEmpty()) {
            long[] cur = pq.poll();
            long cd = cur[0];
            int u = (int)cur[1];
            if (cd > dist[u]) continue;
            for (Edge e: adj[u]) {
                int v = e.to;
                long nd = cd + e.w;
                if (nd < dist[v]) {
                    dist[v] = nd;
                    pq.add(new long[]{nd, v});
                }
            }
        }
    }
    
    /**
     * Multi‐label Dijkstra for up to two best horse‐based arrivals at each node.
     * distSrc[v] is time on foot to v.  horseAt[j] is vertex of horse j.
     * We run Dijkstra in the half‐weight graph, seeded with (horseAt[j], distSrc[horseAt[j]]).
     * We keep at each node only the two best (horseId, dist) pairs.
     */
    static void multiLabelDijkstra(
            long[] distSrc,
            int[] horseAt,
            List<Edge>[] adj,
            long[][] bestD,
            int[][] bestId)
    {
        int n = distSrc.length - 1;
        // Min‐heap by distance
        PriorityQueue<State> pq = new PriorityQueue<>(Comparator.comparingLong(a -> a.dist));
        
        // Seed with all reachable horses
        for (int j = 0; j < horseAt.length; j++) {
            int node = horseAt[j];
            long d0 = distSrc[node];
            if (d0 < INF) {
                pq.add(new State(node, d0, j));
            }
        }
        
        while (!pq.isEmpty()) {
            State st = pq.poll();
            int u = st.node;
            long cd = st.dist;
            int hid = st.horseId;
            
            // Try to insert this label into bestD[u]/bestId[u]
            // If it's not among the top two distinct, skip
            boolean inserted = false;
            // 1) If this horse is already present and we found a better dist, update it
            for (int k = 0; k < 2; k++) {
                if (bestId[u][k] == hid) {
                    if (cd < bestD[u][k]) {
                        bestD[u][k] = cd;
                        // we'll re‐sort below
                        inserted = true;
                    }
                    break;
                }
            }
            if (!inserted) {
                // If there's room for a new second entry
                if (bestId[u][0] < 0) {
                    bestId[u][0] = hid;
                    bestD[u][0] = cd;
                    inserted = true;
                } else if (bestId[u][1] < 0) {
                    bestId[u][1] = hid;
                    bestD[u][1] = cd;
                    inserted = true;
                } else {
                    // If we have two, see if we can replace the worse
                    // Only if new dist is strictly smaller
                    int worseIdx = bestD[u][0] > bestD[u][1] ? 0 : 1;
                    if (cd < bestD[u][worseIdx]) {
                        bestD[u][worseIdx] = cd;
                        bestId[u][worseIdx] = hid;
                        inserted = true;
                    }
                }
            }
            if (!inserted) {
                // This label is not among the best two for u
                continue;
            }
            // Re‐sort the two entries so that [0] is best
            if (bestId[u][1] >= 0 && bestD[u][1] < bestD[u][0]) {
                long tmpd = bestD[u][0]; bestD[u][0] = bestD[u][1]; bestD[u][1] = tmpd;
                int tmpi = bestId[u][0]; bestId[u][0] = bestId[u][1]; bestId[u][1] = tmpi;
            }
            
            // Relax edges in the half‐weight graph
            for (Edge e: adj[u]) {
                int v = e.to;
                long nd = cd + (e.w >> 1); // w/2
                pq.add(new State(v, nd, hid));
            }
        }
    }
}