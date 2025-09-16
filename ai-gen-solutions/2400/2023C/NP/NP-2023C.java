import java.io.*;
import java.util.*;

public class Main {
    static int[] dist;
    static List<Integer>[] adj;

    // Assigns to dist[] a labeling mod k so that along each edge u->v
    // dist[v] = (dist[u] + 1) % k.  Assumes graph is strongly connected
    // and every cycle length is divisible by k, so no conflicts arise.
    static void buildDistanceModK(int n, int k) {
        Arrays.fill(dist, -1);
        Deque<Integer> queue = new ArrayDeque<>();
        dist[0] = 0;
        queue.add(0);
        while (!queue.isEmpty()) {
            int u = queue.poll();
            int du = dist[u];
            for (int v : adj[u]) {
                if (dist[v] == -1) {
                    dist[v] = (du + 1) % k;
                    queue.add(v);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        int T = Integer.parseInt(br.readLine().trim());

        while (T-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int k = Integer.parseInt(st.nextToken());

            // Read types for G1
            int[] a = new int[n];
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }
            // Read G1
            int m1 = Integer.parseInt(br.readLine().trim());
            adj = new ArrayList[n];
            for (int i = 0; i < n; i++) adj[i] = new ArrayList<>();
            for (int i = 0; i < m1; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken()) - 1;
                int v = Integer.parseInt(st.nextToken()) - 1;
                adj[u].add(v);
            }

            // Compute D1
            dist = new int[n];
            buildDistanceModK(n, k);
            int[] D1 = Arrays.copyOf(dist, n);

            // Read types for G2
            int[] b = new int[n];
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                b[i] = Integer.parseInt(st.nextToken());
            }
            // Read G2
            int m2 = Integer.parseInt(br.readLine().trim());
            adj = new ArrayList[n];
            for (int i = 0; i < n; i++) adj[i] = new ArrayList<>();
            for (int i = 0; i < m2; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken()) - 1;
                int v = Integer.parseInt(st.nextToken()) - 1;
                adj[u].add(v);
            }

            // Compute D2
            dist = new int[n];
            buildDistanceModK(n, k);
            int[] D2 = Arrays.copyOf(dist, n);

            // Count total out‐vertices
            int A1 = 0, B1 = 0;
            for (int x : a) if (x == 1) A1++;
            for (int x : b) if (x == 1) B1++;
            // Must match n
            if (A1 + B1 != n) {
                sb.append("NO\n");
                continue;
            }
            // If all out‐edges go one way, no mixed cycle can form
            if (A1 == 0 || B1 == 0) {
                sb.append("YES\n");
                continue;
            }

            // Build our four count‐arrays of length k
            int[] A1cnt = new int[k], C1cnt = new int[k];
            int[] B1cnt = new int[k], C2cnt = new int[k];
            for (int i = 0; i < n; i++) {
                if (a[i] == 1)      A1cnt[D1[i]]++;
                else                C1cnt[D1[i]]++;
                if (b[i] == 1)      B1cnt[D2[i]]++;
                else                C2cnt[D2[i]]++;
            }

            // Check the two matching‐constraints
            boolean ok = true;
            for (int ℓ = 0; ℓ < k; ℓ++) {
                int nxt = (ℓ + 1) % k;
                if (A1cnt[ℓ] != C2cnt[nxt] || B1cnt[ℓ] != C1cnt[nxt]) {
                    ok = false;
                    break;
                }
            }
            sb.append(ok ? "YES\n" : "NO\n");
        }

        System.out.print(sb);
    }
}