import java.io.*;
import java.util.*;

public class Main {
    static final int MAXN = 200_000;
    // adjacency by forward-star (edge‐list) representation
    static int[] head = new int[MAXN+5], to = new int[2*(MAXN+5)], nxt = new int[2*(MAXN+5)];
    static int ec = 0;
    static long[] a = new long[MAXN+5];
    static long[] dp0 = new long[MAXN+5], dp1 = new long[MAXN+5];
    static int[] parent = new int[MAXN+5];
    static int[] stack = new int[MAXN+5], order = new int[MAXN+5];
    
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        int t = Integer.parseInt(br.readLine());
        PrintWriter pw = new PrintWriter(new BufferedOutputStream(System.out));
        
        while (t-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            long c = Long.parseLong(st.nextToken());
            // read gold values
            st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }
            // reset graph heads
            for (int i = 1; i <= n; i++) {
                head[i] = -1;
            }
            ec = 0;
            // read edges
            for (int i = 0; i < n - 1; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                addEdge(u, v);
                addEdge(v, u);
            }
            
            // build a DFS order and parent[], iterative
            int sp = 0, os = 0;
            stack[sp++] = 1;
            parent[1] = 0;
            while (sp > 0) {
                int u = stack[--sp];
                order[os++] = u;
                for (int e = head[u]; e != -1; e = nxt[e]) {
                    int v = to[e];
                    if (v == parent[u]) continue;
                    parent[v] = u;
                    stack[sp++] = v;
                }
            }
            
            // bottom‐up DP in reverse of the order
            for (int i = os - 1; i >= 0; i--) {
                int u = order[i];
                long best0 = 0, best1 = a[u];
                for (int e = head[u]; e != -1; e = nxt[e]) {
                    int v = to[e];
                    if (v == parent[u]) continue;
                    // If u is not chosen, we take max(dp0[v], dp1[v])
                    best0 += Math.max(dp0[v], dp1[v]);
                    // If u is chosen, we take max(dp0[v], dp1[v] - 2c)
                    best1 += Math.max(dp0[v], dp1[v] - 2*c);
                }
                dp0[u] = best0;
                dp1[u] = best1;
            }
            
            long ans = Math.max(dp0[1], dp1[1]);
            // we can always pick nothing for 0 if negative
            if (ans < 0) ans = 0;
            pw.println(ans);
        }
        
        pw.flush();
        pw.close();
    }
    
    static void addEdge(int u, int v) {
        to[ec] = v;
        nxt[ec] = head[u];
        head[u] = ec++;
    }
}