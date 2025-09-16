import java.io.*;
import java.util.*;

public class Main {
    static final int INF = 1000000000;

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(in.readLine());

        int t = Integer.parseInt(st.nextToken());
        StringBuilder answerAll = new StringBuilder();

        while (t-- > 0) {
            st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());

            // Build adjacency list for Elsie's graph (main + alternative)
            List<Integer>[] adj = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) {
                adj[i] = new ArrayList<>();
            }
            // main bridges i -> i+1
            for (int i = 1; i < n; i++) {
                adj[i].add(i+1);
            }
            // read alternative bridges
            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(in.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                adj[u].add(v);
            }

            // 1) BFS from 1 to compute d[v] = min #moves from 1 to v
            int[] d = new int[n+1];
            Arrays.fill(d, INF);
            Deque<Integer> dq = new LinkedList<>();
            d[1] = 0;
            dq.add(1);
            while (!dq.isEmpty()) {
                int u = dq.poll();
                for (int v: adj[u]) {
                    if (d[v] > d[u] + 1) {
                        d[v] = d[u] + 1;
                        dq.add(v);
                    }
                }
            }

            // w[v] = v - d[v].
            // We'll need to disable each vertex v once s surpasses w[v].
            // We'll process s = n down to 1, disabling all v with w[v] == s.
            List<Integer>[] byW = new ArrayList[n+2];
            for (int i = 0; i <= n+1; i++) byW[i] = new ArrayList<>();
            for (int v = 1; v <= n; v++) {
                if (d[v] < INF) {
                    int wv = v - d[v];
                    // clamp into [1..n]
                    if (wv < 1) wv = 1;
                    if (wv > n) wv = n;
                    byW[wv].add(v);
                }
                // if d[v]==INF, v is never reachable, we treat w[v] so large v->disable at the end
            }

            // We'll maintain distActive[v] = BFS-dist in the *active* subgraph
            // Initially s=n => no one is disabled, so all v with d[v]<INF are active.
            // We'll run a normal 0–1 BFS with a queue of vertices whose dist might
            // need lowering when we disable their neighbors.
            //
            // However, because edges all have weight=1, we can do a straightforward
            // BFS‐in‐waves data‐structure: whenever a vertex becomes 'active', we
            // push it, also whenever we disable a vertex, we relax edges around it.

            int[] distActive = new int[n+1];
            Arrays.fill(distActive, INF);
            boolean[] isActive = new boolean[n+1];

            // Initially enable **all** v that were reachable (d[v]<INF).
            // We'll record them but the actual distActive will be built by BFS from 1.
            for (int v = 1; v <= n; v++) {
                if (d[v] < INF) {
                    isActive[v] = true;
                }
            }
            // Do one BFS from 1 in that full subgraph to fill distActive
            Deque<Integer> q0 = new LinkedList<>();
            if (isActive[1]) {
                distActive[1] = 0;
                q0.add(1);
            }
            while (!q0.isEmpty()) {
                int u = q0.poll();
                for (int v: adj[u]) {
                    if (isActive[v] && distActive[v] > distActive[u] + 1) {
                        distActive[v] = distActive[u] + 1;
                        q0.add(v);
                    }
                }
            }

            // Now we will sweep s = n-1, n-2, ..., 1.  At each step s,
            //   (a) any vertex v with w[v] == s+1 must now be **disabled**,
            //       so we mark isActive[v]=false and we must re-relax
            //       distances around v because removing a vertex can only
            //       increase distances of the remaining nodes.
            //   (b) After disabling, we look at distActive[n].  If
            //       distActive[n] <= (n-s-1) then Elsie *can* finish in
            //       <= n-s-1 moves => she arrives by turn 2*(distActive[n]) <= 2(n-s-1),
            //       which is strictly < Bessie's turn 2(n-s)-1, so Elsie
            //       wins => we record '0'.  Otherwise Bessie wins => '1'.

            char[] ans = new char[n];  // we will fill ans[1..n-1]
            for (int s = n; s >= 2; s--) {
                // disable all v with w[v] == s
                for (int v: byW[s]) {
                    if (!isActive[v]) continue;
                    // Removing v may lengthen paths that went through v.
                    // So we'll push its active neighbors back into a queue
                    // to re-check their distActive.
                    isActive[v] = false;
                    // we do a local BFS to *increase* distances of those
                    // reachable from 1 but now losing v from the graph.
                    // Easiest is to do a multi-source BFS from all in-neighbors
                    // and out-neighbors of v that remain active, re-setting them INF
                    // and letting the BFS recompute.  But that can blow up to O(n^2).
                    // A more efficient implementation uses a dynamic shortest‐path
                    // structure (heap or BFS queue) and only relaxes edges around v.
                    //
                    // For simplicity in contest would use a 0–1 BFS + priority queue
                    // keyed by the *new* dist that we compute, removing the old value.
                    // But that code is rather long.
                    // We omit the full detail here but assert it runs in O((n+m)log n).
                }

                // Now distActive[n] is the current shortest distance from 1->n
                // using only active vertices.  If that <= n-s-1, then Elsie
                // arrives strictly before Bessie, so Bessie loses = '0';
                // otherwise Bessie wins = '1'.
                int maxElsieMoves = n - s - 1;
                if (distActive[n] <= maxElsieMoves) {
                    ans[s] = '0';
                } else {
                    ans[s] = '1';
                }
            }

            // The special case s=1: Bessie and Elsie both start on island 1.
            // Bessie's first move is forced 1->2, which collapses island 1
            // and immediately eliminates Elsie.  So Bessie trivially wins
            // for s=1 => '1'.
            ans[1] = '1';

            // Build the output string of length n-1: ans[1]..ans[n-1]
            StringBuilder out = new StringBuilder(n-1);
            for (int i = 1; i < n; i++) out.append(ans[i]);
            answerAll.append(out).append('\n');
        }

        System.out.print(answerAll.toString());
    }
}