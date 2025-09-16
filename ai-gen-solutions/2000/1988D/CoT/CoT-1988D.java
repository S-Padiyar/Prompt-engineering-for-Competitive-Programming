import java.io.*;
import java.util.*;

public class Main {
    static class FastInput {
        BufferedReader br;
        StringTokenizer st;
        FastInput(InputStream in) {
            br = new BufferedReader(new InputStreamReader(in));
        }
        String nextToken() throws IOException {
            while (st == null || !st.hasMoreTokens()) {
                String line = br.readLine();
                if (line == null) return null;
                st = new StringTokenizer(line);
            }
            return st.nextToken();
        }
        int nextInt() throws IOException {
            return Integer.parseInt(nextToken());
        }
        long nextLong() throws IOException {
            return Long.parseLong(nextToken());
        }
    }
    
    public static void main(String[] args) throws IOException {
        FastInput in = new FastInput(System.in);
        StringBuilder sb = new StringBuilder();
        
        int t = in.nextInt();
        while (t-- > 0) {
            int n = in.nextInt();
            long[] a = new long[n+1];
            for (int i = 1; i <= n; i++) {
                a[i] = in.nextLong();
            }
            
            // Build adjacency list for the tree
            List<Integer>[] adj = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) {
                adj[i] = new ArrayList<>();
            }
            for (int i = 0; i < n-1; i++) {
                int x = in.nextInt();
                int y = in.nextInt();
                adj[x].add(y);
                adj[y].add(x);
            }
            
            // BFS to 2-color the tree, accumulate sum0 and sum1
            long sum0 = 0, sum1 = 0;
            int[] color = new int[n+1];
            Arrays.fill(color, -1);
            
            Deque<Integer> queue = new ArrayDeque<>();
            queue.add(1);
            color[1] = 0;
            
            while (!queue.isEmpty()) {
                int u = queue.poll();
                if (color[u] == 0) sum0 += a[u];
                else             sum1 += a[u];
                
                for (int v : adj[u]) {
                    if (color[v] == -1) {
                        color[v] = 1 - color[u];
                        queue.add(v);
                    }
                }
            }
            
            long total = sum0 + sum1;
            long ans = total + Math.min(sum0, sum1);
            sb.append(ans).append('\n');
        }
        
        System.out.print(sb);
    }
}