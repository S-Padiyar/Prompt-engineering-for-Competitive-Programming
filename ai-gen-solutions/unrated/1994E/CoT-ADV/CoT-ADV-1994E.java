import java.io.*;
import java.util.*;

public class Main {
    static final int MAXN = 1000000 + 5;
    static ArrayList<Integer>[] g = new ArrayList[MAXN];
    static int[] sz = new int[MAXN];
    static int[] in = new int[MAXN], out = new int[MAXN], depth = new int[MAXN], parent = new int[MAXN];
    static int timer;
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        int T = Integer.parseInt(br.readLine().trim());
        // initialize adjacency lists
        for (int i = 0; i < MAXN; i++) {
            g[i] = new ArrayList<>();
        }
        int nodeBase = 0;
        while (T-- > 0) {
            int K = Integer.parseInt(br.readLine().trim());
            nodeBase = 0;
            List<Integer> treeRoots = new ArrayList<>(K);
            // read all trees
            for (int t = 0; t < K; t++) {
                int n = Integer.parseInt(br.readLine().trim());
                nodeBase += n;
                // read parents for 2..n
                StringTokenizer st = new StringTokenizer(br.readLine());
                parent[1] = 0;  // root has no parent
                int offset = nodeBase - n;
                for (int i = 2; i <= n; i++) {
                    int p = Integer.parseInt(st.nextToken());
                    parent[i + offset] = p + offset;
                    g[p + offset].add(i + offset);
                }
                treeRoots.add(nodeBase - n + 1);
            }
            // run a DFS on each tree to compute subtree sizes
            timer = 0;
            for (int root : treeRoots) {
                depth[root] = 0;
                dfs(root);
            }
            // collect all nodes in one array
            int totalNodes = nodeBase;
            Integer[] allNodes = new Integer[totalNodes];
            for (int i = 1; i <= totalNodes; i++) {
                allNodes[i - 1] = i;
            }
            // sort them by depth descending
            Arrays.sort(allNodes, (a, b) -> Integer.compare(depth[b], depth[a]));

            // selected nodes that supply bits
            ArrayList<Integer> chosen = new ArrayList<>();
            int answerMask = 0;
            // try bits from highest (20) down to 0
            for (int b = 20; b >= 0; b--) {
                int want = 1 << b;
                for (int v : allNodes) {
                    if ((sz[v] & want) == 0) continue;  // this node doesn't have bit b
                    boolean conflict = false;
                    // check against previously chosen nodes
                    for (int u : chosen) {
                        // u is ancestor of v or v is ancestor of u ?
                        if ((in[u] <= in[v] && in[v] < out[u]) ||
                            (in[v] <= in[u] && in[u] < out[v])) {
                            conflict = true;
                            break;
                        }
                    }
                    if (!conflict) {
                        // we can claim node v for bit b
                        chosen.add(v);
                        answerMask |= want;
                        break;
                    }
                }
            }

            sb.append(answerMask).append("\n");

            // clear adjacency for next test
            for (int i = 1; i <= totalNodes; i++) {
                g[i].clear();
            }
        }

        System.out.print(sb);
    }

    // standard DFS to compute:
    //  - depth[]
    //  - in[v], out[v] (euler-time interval)
    //  - sz[v] = subtree size
    static void dfs(int v) {
        in[v] = timer++;
        sz[v] = 1;
        for (int c : g[v]) {
            depth[c] = depth[v] + 1;
            dfs(c);
            sz[v] += sz[c];
        }
        out[v] = timer;
    }
}