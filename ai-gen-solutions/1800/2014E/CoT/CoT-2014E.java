import java.io.*;
import java.util.*;

public class Main {
    static final long INF = Long.MAX_VALUE / 4;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter pw = new PrintWriter(System.out);
        StringTokenizer st;

        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());
            int h = Integer.parseInt(st.nextToken());

            // Read horse‐vertices
            st = new StringTokenizer(br.readLine());
            boolean[] hasHorse = new boolean[n];
            for (int i = 0; i < h; i++) {
                int x = Integer.parseInt(st.nextToken()) - 1;
                hasHorse[x] = true;
            }

            // We build a state‐expanded graph with 2*n nodes:
            //  [0..n-1]   = foot‐state (u,0)
            //  [n..2n-1] = horse‐state (u,1)
            int N = 2 * n;

            // Adjacency‐list via head/next arrays for speed
            int[] head = new int[N];
            Arrays.fill(head, -1);
            int estEdges = 4 * m + h;  // roughly
            int[] to   = new int[estEdges * 2]; // *2 for undirected foot‐foot & horse‐horse
            long[] wgt = new long[estEdges * 2];
            int[] nxt  = new int[estEdges * 2];
            int edgeCount = 0;

            // Helper to add a directed edge u->v with cost w
            class EAdder {
                void add(int u, int v, long w) {
                    to[edgeCount] = v;
                    wgt[edgeCount] = w;
                    nxt[edgeCount] = head[u];
                    head[u] = edgeCount++;
                }
            }
            EAdder adder = new EAdder();

            // Read original edges and add 4 directed edges per undirected:
            //  foot->foot, horse->horse (half cost), both directions.
            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken()) - 1;
                int v = Integer.parseInt(st.nextToken()) - 1;
                long w = Long.parseLong(st.nextToken());
                // foot‐foot
                adder.add(u, v, w);
                adder.add(v, u, w);
                // horse‐horse (w/2)
                adder.add(u + n, v + n, w/2);
                adder.add(v + n, u + n, w/2);
            }

            // Add zero‐cost edges (u,0)->(u,1) at the horse vertices
            for (int u = 0; u < n; u++) {
                if (hasHorse[u]) {
                    adder.add(u, u + n, 0L);
                }
            }

            // Dijkstra from source s in [0..2n-1]
            // Returns dist[] array of size 2n
            class Dijkstra {
                long[] run(int s) {
                    long[] dist = new long[N];
                    Arrays.fill(dist, INF);
                    dist[s] = 0;
                    PriorityQueue<int[]> pq = new PriorityQueue<>(
                        Comparator.comparingLong(a -> a[0])
                    );
                    pq.add(new int[]{0, s});  // {distance, node}

                    while (!pq.isEmpty()) {
                        int[] top = pq.poll();
                        long cd = top[0];
                        int u = top[1];
                        if (cd != dist[u]) continue;
                        // relax edges
                        for (int e = head[u]; e != -1; e = nxt[e]) {
                            int v = to[e];
                            long nd = cd + wgt[e];
                            if (nd < dist[v]) {
                                dist[v] = nd;
                                pq.add(new int[]{(int)nd, v});
                            }
                        }
                    }
                    return dist;
                }
            }

            // Run two Dijkstras
            Dijkstra dij = new Dijkstra();
            long[] distM = dij.run(0);        // from (1,0)
            long[] distR = dij.run(n - 1);    // from (n,0)

            // Compute best possible meeting time
            long answer = INF;
            for (int u = 0; u < n; u++) {
                long tM = Math.min(distM[u], distM[u + n]);
                long tR = Math.min(distR[u], distR[u + n]);
                if (tM < INF && tR < INF) {
                    long meet = Math.max(tM, tR);
                    if (meet < answer) answer = meet;
                }
            }
            if (answer == INF) answer = -1;
            pw.println(answer);
        }

        pw.flush();
        pw.close();
    }
}