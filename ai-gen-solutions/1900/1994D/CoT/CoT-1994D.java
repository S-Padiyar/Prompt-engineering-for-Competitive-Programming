import java.io.*;
import java.util.*;

public class Main {
    static int n;
    static long[] a;
    // adj[x] = list of right‐side nodes j that x can match to
    static ArrayList<Integer>[] adj;
    // matchL[x] = matched right‐node for left‐node x  (or -1)
    // matchR[j] = matched left‐node for right‐node j (or -1)
    static int[] matchL, matchR;
    static boolean[] seen;  // for DFS on right side

    // Try to find augmenting path starting from left‐node x
    static boolean dfs(int x) {
        for (int j : adj[x]) {
            if (seen[j]) continue;
            seen[j] = true;
            // either j is free, or we can re-route its current match
            if (matchR[j] == -1 || dfs(matchR[j])) {
                matchL[x] = j;
                matchR[j] = x;
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            n = Integer.parseInt(br.readLine().trim());
            a = new long[n];
            StringTokenizer st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }
            // If n=1, no edges needed, always connected trivially
            if (n == 1) {
                out.println("Yes");
                continue;
            }

            // 1) Pick r = index of minimal a[i]
            int r = 0;
            for (int i = 1; i < n; i++) {
                if (a[i] < a[r]) r = i;
            }

            // 2) Build list of the other n-1 vertices
            int M = n - 1;
            int[] ids = new int[M]; // ids[j] = original vertex index for right‐node j
            {
                int ptr = 0;
                for (int i = 0; i < n; i++) {
                    if (i == r) continue;
                    ids[ptr++] = i;
                }
            }

            // 3) Build adjacency for x=1..M
            adj = new ArrayList[M+1];
            for (int x = 1; x <= M; x++) {
                adj[x] = new ArrayList<>();
            }
            for (int j = 0; j < M; j++) {
                long diff = Math.abs(a[ids[j]] - a[r]);
                // For each x in [1..M], if x divides diff, add edge
                // (We could factor diff, but for M<=2000 a simple loop is fine.)
                for (int x = 1; x <= M; x++) {
                    if (diff % x == 0) {
                        adj[x].add(j);
                    }
                }
            }

            // 4) Try to find a perfect matching from {1..M} -> {right‐nodes}
            matchL = new int[M+1];
            matchR = new int[M];
            Arrays.fill(matchL, -1);
            Arrays.fill(matchR, -1);

            boolean ok = true;
            // We can improve speed by sorting x's by increasing adjacency size
            Integer[] ops = new Integer[M];
            for (int i = 0; i < M; i++) ops[i] = i+1;
            Arrays.sort(ops, Comparator.comparingInt(x -> adj[x].size()));

            for (int x : ops) {
                seen = new boolean[M];
                if (!dfs(x)) {
                    ok = false;
                    break;
                }
            }

            if (!ok) {
                out.println("No");
            } else {
                out.println("Yes");
                // We must print the edges in order x=1..M
                // operation x connects (r, ids[ matchL[x] ])
                for (int x = 1; x <= M; x++) {
                    int j = matchL[x];
                    int v = ids[j];
                    // +1 to convert 0-based to 1-based
                    out.printf("%d %d\n", r+1, v+1);
                }
            }
        }
        out.flush();
    }
}