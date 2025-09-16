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
            List<Integer>[] adj = new List[n+1];
            for (int i = 1; i <= n; i++) {
                adj[i] = new ArrayList<>();
            }
            for (int i = 0; i < m; i++) {
                int u = in.nextInt();
                int v = in.nextInt();
                adj[u].add(v);
                adj[v].add(u);
            }
            // color[i] = 0 => unvisited; 1 or -1 => two sides
            int[] color = new int[n+1];
            boolean isBip = true;
            for (int i = 1; i <= n && isBip; i++) {
                if (color[i] == 0) {
                    // BFS from i
                    Deque<Integer> dq = new ArrayDeque<>();
                    color[i] = 1;
                    dq.add(i);
                    while (!dq.isEmpty() && isBip) {
                        int u = dq.poll();
                        for (int v : adj[u]) {
                            if (color[v] == 0) {
                                color[v] = -color[u];
                                dq.add(v);
                            } else if (color[v] == color[u]) {
                                // same color on two ends => not bipartite
                                isBip = false;
                                break;
                            }
                        }
                    }
                }
            }
            if (isBip) {
                out.println("Bob");
            } else {
                out.println("Alice");
            }
        }

        out.flush();
    }

    // fast input
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