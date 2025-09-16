import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        int t = Integer.parseInt(br.readLine().trim());

        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            long[] a = new long[n+1];
            st = new StringTokenizer(br.readLine());
            long totalSum = 0;
            for (int i = 1; i <= n; i++) {
                a[i] = Long.parseLong(st.nextToken());
                totalSum += a[i];
            }

            // Build adjacency list for the tree
            List<Integer>[] adj = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) {
                adj[i] = new ArrayList<>();
            }
            for (int i = 0; i < n-1; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                adj[u].add(v);
                adj[v].add(u);
            }

            // We will 2-color by a simple DFS from node 1.
            boolean[] color = new boolean[n+1];
            // color[i] == false -> color 0, true -> color 1
            long sum0 = 0, sum1 = 0;

            // Iterative DFS stack: we store (node, parent)
            Deque<int[]> stack = new ArrayDeque<>();
            stack.push(new int[]{1, 0});  // start at node 1, parent = 0
            color[1] = false;

            while (!stack.isEmpty()) {
                int[] top = stack.pop();
                int u = top[0], p = top[1];

                // Accumulate into the appropriate color sum
                if (!color[u]) sum0 += a[u];
                else           sum1 += a[u];

                // Visit children
                for (int v : adj[u]) {
                    if (v == p) continue;
                    color[v] = !color[u];  // alternate color
                    stack.push(new int[]{v, u});
                }
            }

            // The answer is totalSum + min(sum0, sum1)
            long ans = totalSum + Math.min(sum0, sum1);
            sb.append(ans).append('\n');
        }

        // Output all answers
        System.out.print(sb.toString());
    }
}