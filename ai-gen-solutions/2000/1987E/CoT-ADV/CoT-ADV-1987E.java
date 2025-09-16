import java.io.*;
import java.util.*;

public class Main {
    static int n;
    static long[] a;            // a[v] will be updated to its "fixed" value
    static long[] ops;          // ops[v] = min operations to fix subtree v
    static long[] cost;         // cost[v] = cost to raise a[v] by +1 after fixing its subtree
    static List<Integer>[] children;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            n = Integer.parseInt(br.readLine().trim());
            a = new long[n+1];
            ops = new long[n+1];
            cost = new long[n+1];
            children = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) {
                children[i] = new ArrayList<>();
            }

            // Read the a[i]
            st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }

            // Read parents p[2..n], build child lists
            st = new StringTokenizer(br.readLine());
            for (int i = 2; i <= n; i++) {
                int p = Integer.parseInt(st.nextToken());
                children[p].add(i);
            }

            // Run DFS from root = 1
            dfs(1);

            // The answer is ops[1]
            sb.append(ops[1]).append('\n');
        }

        System.out.print(sb.toString());
    }

    // Post-order DFS: fix the subtree under v, compute ops[v] & cost[v]
    static void dfs(int v) {
        // If v is a leaf, no child‐constraint to fix
        if (children[v].isEmpty()) {
            ops[v] = 0;
            cost[v] = 1;  // raising a[v] by 1 just costs 1 op
            return;
        }

        long sumOps = 0;       // total ops in all child subtrees
        long S = 0;            // sum of child a[u]
        long minCost = Long.MAX_VALUE;
        int bestChild = -1;

        // First, solve children
        for (int u : children[v]) {
            dfs(u);
            sumOps += ops[u];
            S += a[u];
            if (cost[u] < minCost) {
                minCost = cost[u];
                bestChild = u;
            }
        }

        // If children's sum S < a[v], we must push 'deficit' units into children
        if (S < a[v]) {
            long deficit = a[v] - S;
            // We will always push into the child of smallest cost
            sumOps += deficit * minCost;
            // simulate raising that child's value
            a[bestChild] += deficit;
            S += deficit;
        }

        // Now the local constraint a[v] <= S is satisfied
        ops[v] = sumOps;

        // Compute cost[v]: cost to raise a[v] by +1 later
        long surplus = S - a[v];
        if (surplus >= 1) {
            // We have slack, so a single '+1 at v' costs exactly 1
            cost[v] = 1;
        } else {
            // No slack; raising a[v] by 1 will force one unit into children first
            cost[v] = 1 + minCost;
        }
    }
}