import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // Use BufferedReader/PrintWriter for fast I/O
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        
        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            int n = Integer.parseInt(in.readLine().trim());
            
            // Read attack powers
            long[] a = new long[n + 1];
            StringTokenizer st = new StringTokenizer(in.readLine());
            for (int i = 1; i <= n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }
            
            // Build the tree
            List<Integer>[] adj = new ArrayList[n + 1];
            for (int i = 1; i <= n; i++) {
                adj[i] = new ArrayList<>();
            }
            for (int i = 0; i < n - 1; i++) {
                st = new StringTokenizer(in.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                adj[u].add(v);
                adj[v].add(u);
            }
            
            // BFS to 2-color the tree
            int[] color = new int[n + 1];
            Arrays.fill(color, -1);
            long sum0 = 0, sum1 = 0;
            
            Deque<Integer> queue = new ArrayDeque<>();
            queue.add(1);
            color[1] = 0;
            
            while (!queue.isEmpty()) {
                int u = queue.poll();
                if (color[u] == 0) {
                    sum0 += a[u];
                } else {
                    sum1 += a[u];
                }
                for (int v : adj[u]) {
                    if (color[v] == -1) {
                        color[v] = color[u] ^ 1;  // alternate color 0<->1
                        queue.add(v);
                    }
                }
            }
            
            // Two options: kill color0 in round1 or color1 in round1
            long option1 = sum0 + 2 * sum1;  // color0 first, color1 second
            long option2 = 2 * sum0 + sum1;  // color1 first, color0 second
            long ans = Math.min(option1, option2);
            
            out.println(ans);
        }
        
        out.flush();
    }
}