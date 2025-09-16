import java.io.*;
import java.util.*;

public class Main {
    static class Edge {
        int to, id;
        Edge(int t, int i) { to = t; id = i; }
    }

    // For the first DFS (finding bridges), we will push two types
    // of actions on the stack: a "visit" of a node, and a "post"
    // callback after finishing a child.
    static class Action1 {
        int type;     // 0 = visit node, 1 = post-child
        int u;        // current node
        int parentEdge;  // for type=0: the edge we came in on
        int idx;      // for type=0: which adjacency index we're up to
        int v, eid;   // for type=1: v is the child, eid is the edge-id
        Action1(int type, int u, int parentEdge, int idx, int v, int eid) {
            this.type = type; this.u = u;
            this.parentEdge = parentEdge; this.idx = idx;
            this.v = v; this.eid = eid;
        }
    }

    // For the second DFS (on the bridge-tree), similar idea:
    static class Action2 {
        int type;     // 0 = visit comp-node, 1 = post-child
        int u;        // the component-node
        int parent;   // parent in the tree
        int idx;      // adjacency index (for type=0)
        int child;    // for type=1: which child we just processed
        Action2(int type, int u, int parent, int idx, int child) {
            this.type = type; this.u = u; this.parent = parent;
            this.idx = idx; this.child = child;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        int T = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();
        while (T-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());

            // Read graph
            List<Edge>[] adj = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) adj[i] = new ArrayList<>();
            int[] U = new int[m], V = new int[m];
            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                U[i] = u;  V[i] = v;
                adj[u].add(new Edge(v, i));
                adj[v].add(new Edge(u, i));
            }

            // 1) Find bridges via an iterative Tarjan low-link DFS
            boolean[] visited = new boolean[n+1];
            int[] tin = new int[n+1], low = new int[n+1];
            boolean[] isBridge = new boolean[m];
            int timer = 0;

            // Graph is connected, so one DFS from node 1 suffices
            Deque<Action1> stack1 = new ArrayDeque<>();
            stack1.addLast(new Action1(0, 1, -1, 0, 0, 0));

            while (!stack1.isEmpty()) {
                Action1 act = stack1.peekLast();
                if (act.type == 0) {
                    // Visiting node u
                    int u = act.u;
                    if (act.idx == 0) {
                        visited[u] = true;
                        tin[u] = low[u] = ++timer;
                    }
                    if (act.idx < adj[u].size()) {
                        Edge e = adj[u].get(act.idx);
                        act.idx++;
                        int v = e.to, eid = e.id;
                        if (eid == act.parentEdge) {
                            // skip the edge back to parent
                            continue;
                        }
                        if (visited[v]) {
                            // a back-edge
                            low[u] = Math.min(low[u], tin[v]);
                        } else {
                            // tree-edge => recurse on v,
                            // but first schedule a post-child action
                            stack1.addLast(new Action1(1, u, 0, 0, v, eid));
                            stack1.addLast(new Action1(0, v, eid, 0, 0, 0));
                        }
                    } else {
                        // all neighbors done
                        stack1.removeLast();
                    }
                } else {
                    // Post-child action
                    int u = act.u, v = act.v, eid = act.eid;
                    // low-link update
                    low[u] = Math.min(low[u], low[v]);
                    // check bridge condition
                    if (low[v] > tin[u]) {
                        isBridge[eid] = true;
                    }
                    stack1.removeLast();
                }
            }

            // 2) Build 2-edge-connected components by DFS on non-bridge edges
            int[] comp = new int[n+1];
            int compCnt = 0;
            int[] compSizeTemp = new int[n+1];  // at most n components
            for (int i = 1; i <= n; i++) {
                if (comp[i] == 0) {
                    compCnt++;
                    int size = 0;
                    Deque<Integer> dq = new ArrayDeque<>();
                    dq.addLast(i);
                    comp[i] = compCnt;
                    while (!dq.isEmpty()) {
                        int u = dq.removeLast();
                        size++;
                        for (Edge e : adj[u]) {
                            if (isBridge[e.id]) continue;
                            int v = e.to;
                            if (comp[v] == 0) {
                                comp[v] = compCnt;
                                dq.addLast(v);
                            }
                        }
                    }
                    compSizeTemp[compCnt] = size;
                }
            }

            // 3) Build the bridge-tree on the components
            @SuppressWarnings("unchecked")
            List<Integer>[] tree = new ArrayList[compCnt+1];
            for (int i = 1; i <= compCnt; i++) tree[i] = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                if (isBridge[i]) {
                    int c1 = comp[U[i]], c2 = comp[V[i]];
                    tree[c1].add(c2);
                    tree[c2].add(c1);
                }
            }

            // 4) DFS on the tree to compute subtree sums and max product
            long[] subtreeSum = new long[compCnt+1];
            long maxProd = 0;
            Deque<Action2> stack2 = new ArrayDeque<>();
            // pick component 1 as root (tree is connected)
            stack2.addLast(new Action2(0, 1, -1, 0, 0));

            while (!stack2.isEmpty()) {
                Action2 act = stack2.peekLast();
                if (act.type == 0) {
                    // entering component node u
                    int u = act.u, p = act.parent;
                    if (act.idx == 0) {
                        subtreeSum[u] = compSizeTemp[u];
                    }
                    if (act.idx < tree[u].size()) {
                        int v = tree[u].get(act.idx);
                        act.idx++;
                        if (v == p) continue;
                        // post-child marker
                        stack2.addLast(new Action2(1, u, 0, 0, v));
                        // recurse
                        stack2.addLast(new Action2(0, v, u, 0, 0));
                    } else {
                        // all children done
                        stack2.removeLast();
                    }
                } else {
                    // Post-child: we've finished v
                    int u = act.u, v = act.child;
                    long s = subtreeSum[v];
                    subtreeSum[u] += s;
                    long prod = s * (n - s);
                    if (prod > maxProd) maxProd = prod;
                    stack2.removeLast();
                }
            }

            // 5) compute final answer
            long initPairs = (long)n * (n-1) / 2;
            long ans = initPairs - maxProd;
            sb.append(ans).append('\n');
        }
        System.out.print(sb);
    }
}