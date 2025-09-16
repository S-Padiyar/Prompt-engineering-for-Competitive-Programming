import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader();
        PrintWriter out = new PrintWriter(System.out);

        int t = in.nextInt();
        while (t-- > 0) {
            int n = in.nextInt();
            int m = in.nextInt();

            List<Integer>[] adj = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) {
                adj[i] = new ArrayList<>();
            }
            for (int i = 0; i < m; i++) {
                int u = in.nextInt();
                int v = in.nextInt();
                adj[u].add(v);
                adj[v].add(u);
            }

            // color[i] = 0 (unvisited), 1 or 2 = two bipartite sides
            int[] color = new int[n+1];
            boolean isBip = true;

            for (int i = 1; i <= n; i++) {
                if (color[i] == 0) {
                    // BFS from i
                    Queue<Integer> q = new ArrayDeque<>();
                    color[i] = 1;
                    q.add(i);
                    while (!q.isEmpty() && isBip) {
                        int u = q.poll();
                        for (int v : adj[u]) {
                            if (color[v] == 0) {
                                color[v] = 3 - color[u];
                                q.add(v);
                            } else if (color[v] == color[u]) {
                                isBip = false;
                                break;
                            }
                        }
                    }
                    if (!isBip) break;
                }
            }

            out.println(isBip ? "Bob" : "Alice");
        }
        out.flush();
    }

    // Fast IO helper
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
        FastReader() {
            br = new BufferedReader(new InputStreamReader(System.in));
        }
        String next() throws IOException {
            while (st == null || !st.hasMoreTokens()) {
                String s = br.readLine();
                if (s == null) return null;
                st = new StringTokenizer(s);
            }
            return st.nextToken();
        }
        int nextInt() throws IOException {
            return Integer.parseInt(next());
        }
    }
}