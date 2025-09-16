import java.io.*;
import java.util.*;

public class Main {
    static int n;
    static long[] a;
    static ArrayList<Integer>[] children;

    // We return a pair [ops, costC]:
    //  ops = minimal operations in subtree(v),
    //  costC = minimal cost to raise a[v] by +1 w/o breaking v's subtree constraints.
    static class Pair {
        long ops, costC;
        Pair(long o, long c) { ops = o; costC = c; }
    }

    static Pair dfs(int v) {
        // If leaf, no children => no local constraint to enforce downward.
        // costC = 1 since raising a leaf by +1 costs exactly one operation.
        if (children[v].isEmpty()) {
            return new Pair(0L, 1L);
        }

        long totalOps = 0L;
        long sumChildA = 0L;
        long minChildCost = Long.MAX_VALUE;

        // First, solve each child's subtree
        for (int u : children[v]) {
            Pair sub = dfs(u);
            totalOps += sub.ops;       // all ops needed in child's subtree
            sumChildA += a[u];         // sum of the children's original a[u]
            minChildCost = Math.min(minChildCost, sub.costC);
        }

        // Enforce v's own constraint: a[v] <= sum of its children
        long deficit = Math.max(0L, a[v] - sumChildA);
        // To buy each unit of child-sum costs minChildCost
        totalOps += deficit * minChildCost;

        // costC[v] = 1 + min cost among children
        long costCv = 1L + minChildCost;
        return new Pair(totalOps, costCv);
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            n = Integer.parseInt(br.readLine().trim());
            a = new long[n+1];
            children = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) {
                children[i] = new ArrayList<>();
            }
            // Read a[1..n]
            StringTokenizer st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }
            // Read parents p2..pn, build child‐lists
            st = new StringTokenizer(br.readLine());
            for (int i = 2; i <= n; i++) {
                int p = Integer.parseInt(st.nextToken());
                children[p].add(i);
            }

            // Run the DP from root=1
            Pair ans = dfs(1);
            // ans.ops is the minimal total # of +1 ops
            System.out.println(ans.ops);
        }
    }
}