import java.io.*;
import java.util.*;

public class Main {
    static int N;
    static long[] a;
    static ArrayList<Integer>[] children;
    static long[] dp0, inc;  // dp0[v], inc[v]=marginal cost to raise a'[v] by 1

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            N = Integer.parseInt(br.readLine().trim());
            a = new long[N+1];
            StringTokenizer st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= N; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }
            children = new ArrayList[N+1];
            for (int i = 1; i <= N; i++) {
                children[i] = new ArrayList<>();
            }
            // read parents
            st = new StringTokenizer(br.readLine());
            for (int i = 2; i <= N; i++) {
                int p = Integer.parseInt(st.nextToken());
                children[p].add(i);
            }
            dp0 = new long[N+1];
            inc = new long[N+1];
            dfs(1);
            sb.append(dp0[1]).append('\n');
        }
        System.out.print(sb);
    }

    // Depth–first search to fill dp0[v], inc[v]
    static void dfs(int v) {
        if (children[v].isEmpty()) {
            // leaf
            dp0[v] = 0;
            inc[v] = 1;   // raising a leaf by 1 costs exactly 1 operation
            return;
        }
        long sumDP0 = 0;
        long sumA    = 0;
        long best    = Long.MAX_VALUE;
        for (int u : children[v]) {
            dfs(u);
            sumDP0 += dp0[u];
            sumA    += a[u];
            best     = Math.min(best, inc[u]);
        }
        // how many total units we must push into the children so that their sum >= a[v]?
        long deficit = a[v] - sumA;
        if (deficit < 0) deficit = 0;
        dp0[v] = sumDP0 + deficit * best;
        // marginal cost to raise a'[v] by one more unit:
        inc[v] = best + 1;
        // Note: we do NOT modify a[v] itself here.  a[v] remains original.
        // dp0[v] already accounts for the necessary child‐side increments.
    }
}