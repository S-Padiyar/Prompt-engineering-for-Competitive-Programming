import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        StringTokenizer tok = new StringTokenizer(in.readLine());
        int t = Integer.parseInt(tok.nextToken());

        // We will re-allocate per test since sum(n+m) ≤ 2e5.
        while (t-- > 0) {
            tok = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(tok.nextToken());
            int m = Integer.parseInt(tok.nextToken());

            // Build adjacency list for the DAG (main chain + m extras)
            ArrayList<Integer>[] adj = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) {
                adj[i] = new ArrayList<>();
            }
            // main bridges i->i+1
            for (int i = 1; i < n; i++) {
                adj[i].add(i+1);
            }
            // extra bridges
            for (int i = 0; i < m; i++) {
                tok = new StringTokenizer(in.readLine());
                int u = Integer.parseInt(tok.nextToken());
                int v = Integer.parseInt(tok.nextToken());
                adj[u].add(v);
            }

            // 1) BFS from 1 to compute dist[i] = min #edges from 1 to i
            final int INF = n + 5;
            int[] dist = new int[n+1];
            Arrays.fill(dist, INF);
            dist[1] = 0;
            Deque<Integer> dq = new ArrayDeque<>();
            dq.add(1);
            while (!dq.isEmpty()) {
                int u = dq.poll();
                for (int v : adj[u]) {
                    if (dist[v] == INF) {
                        dist[v] = dist[u] + 1;
                        dq.add(v);
                    }
                }
            }

            // 2) Group nodes by dist
            int maxd = 0;
            for (int i = 1; i <= n; i++) {
                if (dist[i] < INF && dist[i] > maxd) {
                    maxd = dist[i];
                }
            }
            ArrayList<Integer>[] byDist = new ArrayList[maxd+1];
            for (int d = 0; d <= maxd; d++) {
                byDist[d] = new ArrayList<>();
            }
            for (int i = 1; i <= n; i++) {
                if (dist[i] < INF) {
                    byDist[dist[i]].add(i);
                }
            }

            // 3) For each i, predMin[i] = smallest u that appears just before i on some shortest path
            int[] predMin = new int[n+1];
            Arrays.fill(predMin, n+1);
            predMin[1] = 0;  // dummy

            // process in increasing dist
            for (int d = 0; d < maxd; d++) {
                for (int u : byDist[d]) {
                    for (int v : adj[u]) {
                        // only edges that go to dist+1
                        if (dist[v] == d + 1) {
                            // candidate predecessor is 'u'
                            if (u < predMin[v]) {
                                predMin[v] = u;
                            }
                        }
                    }
                }
            }

            // 4) Build difference array for intervals [L_i,R_i]
            //    where L_i = predMin[i]+1, R_i = i-dist[i]-1, if L_i <= R_i
            int[] diff = new int[n+2];
            for (int i = 2; i <= n; i++) {
                if (dist[i] == INF) continue;     // unreachable
                int L = predMin[i] + 1;
                int R = i - dist[i] - 1;
                if (L <= R && L <= n-1 && R >= 1) {
                    if (L < 1) L = 1;
                    if (R > n-1) R = n-1;
                    diff[L]++;
                    diff[R+1]--;
                }
            }

            // 5) Prefix-sum and output
            StringBuilder ans = new StringBuilder(n-1);
            int cover = 0;
            for (int s = 1; s < n; s++) {
                cover += diff[s];
                // cover>0 => s in some losing-interval => Bessie loses => '0'
                // cover==0 => Bessie never intercepted => '1'
                ans.append(cover > 0 ? '0' : '1');
            }

            out.println(ans);
        }

        out.flush();
    }
}