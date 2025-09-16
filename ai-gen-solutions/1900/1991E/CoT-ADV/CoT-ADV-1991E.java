import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int t = in.nextInt();
        while (t-- > 0) {
            int n = in.nextInt(), m = in.nextInt();
            List<List<Integer>> adj = new ArrayList<>(n+1);
            for (int i = 0; i <= n; i++) {
                adj.add(new ArrayList<>());
            }
            for (int i = 0; i < m; i++) {
                int u = in.nextInt();
                int v = in.nextInt();
                adj.get(u).add(v);
                adj.get(v).add(u);
            }
            
            // Attempt to two-color the graph (1 and 2)
            int[] color = new int[n+1]; // 0 = unvisited, 1 or 2 = the two colors
            boolean isBipartite = true;
            
            Queue<Integer> queue = new ArrayDeque<>();
            // Graph is connected but we'll code for general case:
            for (int start = 1; start <= n && isBipartite; start++) {
                if (color[start] == 0) {
                    color[start] = 1;
                    queue.add(start);
                    while (!queue.isEmpty() && isBipartite) {
                        int u = queue.poll();
                        for (int v : adj.get(u)) {
                            if (color[v] == 0) {
                                color[v] = 3 - color[u]; // alternate 1 <-> 2
                                queue.add(v);
                            } else if (color[v] == color[u]) {
                                // Found same colors on an edge => not bipartite
                                isBipartite = false;
                                break;
                            }
                        }
                    }
                }
            }
            
            // If bipartite, Bob can safely avoid a monochrome edge.
            // Otherwise Alice can force a monochrome edge on an odd cycle.
            System.out.println(isBipartite ? "Bob" : "Alice");
        }
        in.close();
    }
}