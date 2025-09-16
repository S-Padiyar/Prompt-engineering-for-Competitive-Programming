import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    static PrintWriter out = new PrintWriter(System.out);
    static StringTokenizer tok;

    // --- fast input helpers ---
    static String nextToken() throws IOException {
        while (tok == null || !tok.hasMoreTokens()) {
            String line = in.readLine();
            if (line == null) return null;
            tok = new StringTokenizer(line);
        }
        return tok.nextToken();
    }
    static int nextInt() throws IOException {
        String s = nextToken();
        if (s == null) System.exit(0);
        return Integer.parseInt(s);
    }
    static void die() {
        // must exit immediately on reading −1 from judge
        System.exit(0);
    }

    // --- graph data ---
    static int n, m;
    static ArrayList<Integer>[] adj;
    // for bipartiteness test
    static int[] bcolor, parent, depth;
    static boolean isBip;
    static int foundU, foundV;

    // for Alice's odd‐cycle strategy
    static ArrayList<Integer> cycle;
    static boolean[] inC;
    static int[] posW;
    static int lastW;
    static int[] colState;
    static int uncoloredLeft;

    public static void main(String[] args) throws IOException {
        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            // read graph
            String[] sp = in.readLine().trim().split("\\s+");
            n = Integer.parseInt(sp[0]);
            m = Integer.parseInt(sp[1]);
            adj = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) adj[i] = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                sp = in.readLine().trim().split("\\s+");
                int u = Integer.parseInt(sp[0]);
                int v = Integer.parseInt(sp[1]);
                adj[u].add(v);
                adj[v].add(u);
            }

            // -- check bipartiteness with BFS, record an odd‐cycle edge if found --
            bcolor = new int[n+1];
            parent = new int[n+1];
            depth  = new int[n+1];
            isBip = true;
            foundU = -1; 
            foundV = -1;
            Queue<Integer> q = new ArrayDeque<>();
            for (int i = 1; i <= n && isBip; i++) {
                if (bcolor[i] == 0) {
                    bcolor[i] = 1;
                    parent[i] = -1;
                    depth[i] = 0;
                    q.add(i);
                    while (!q.isEmpty() && isBip) {
                        int u = q.poll();
                        for (int v : adj[u]) {
                            if (bcolor[v] == 0) {
                                bcolor[v] = -bcolor[u];
                                parent[v] = u;
                                depth[v] = depth[u] + 1;
                                q.add(v);
                            } else if (bcolor[v] == bcolor[u]) {
                                // found an odd‐cycle edge (u,v)
                                isBip = false;
                                foundU = u;
                                foundV = v;
                                break;
                            }
                        }
                    }
                }
            }

            if (isBip) {
                playBob();
            } else {
                playAlice();
            }
        }
        out.flush();
    }

    // --- Bob's strategy on bipartite graph ---
    static void playBob() throws IOException {
        out.println("Bob");
        out.flush();

        // Partition A = bcolor==1, B = bcolor==-1
        TreeSet<Integer> A = new TreeSet<>(), B = new TreeSet<>();
        for (int i = 1; i <= n; i++) {
            if (bcolor[i] == 1) A.add(i);
            else               B.add(i);
        }

        for (int round = 0; round < n; round++) {
            int a = nextInt();
            int b = nextInt();
            if (a < 0) die();  // judge error
            int pick = -1, col = -1;
            // try to use our fixed coloring: A→1, B→2
            if ((a == 1 || b == 1) && !A.isEmpty()) {
                pick = A.pollFirst();
                col  = 1;
            } else if ((a == 2 || b == 2) && !B.isEmpty()) {
                pick = B.pollFirst();
                col  = 2;
            } else {
                // fallback if something unexpected happens
                if (!A.isEmpty()) {
                    pick = A.pollFirst(); col = 1;
                } else {
                    pick = B.pollFirst(); col = 2;
                }
            }
            out.printf("%d %d\n", pick, col);
            out.flush();
        }
    }

    // --- Alice's strategy on non‐bipartite graph ---
    static void playAlice() throws IOException {
        out.println("Alice");
        out.flush();

        // Reconstruct an explicit odd cycle from foundU--foundV
        ArrayList<Integer> pathU = new ArrayList<>(), pathV = new ArrayList<>();
        int x = foundU, y = foundV;
        while (x != y) {
            if (depth[x] > depth[y]) {
                pathU.add(x);
                x = parent[x];
            } else {
                pathV.add(y);
                y = parent[y];
            }
        }
        // x==y is LCA
        pathU.add(x);
        Collections.reverse(pathV);
        cycle = new ArrayList<>(pathU);
        cycle.addAll(pathV);

        inC = new boolean[n+1];
        for (int v : cycle) inC[v] = true;
        posW = new int[n+1];
        colState = new int[n+1];
        uncoloredLeft = n;

        // Shrink the odd cycle until it has length 3
        while (cycle.size() > 3) {
            // build index‐map of current cycle
            for (int i = 0; i < cycle.size(); i++) {
                posW[ cycle.get(i) ] = i;
            }
            // force Bob to pick among colors {1,2}
            out.println("1 2");
            out.flush();

            int vtx = nextInt();
            int c   = nextInt();
            if (vtx < 0) die();
            colState[vtx] = c;
            uncoloredLeft--;

            // if he colored outside the cycle, ignore
            if (!inC[vtx]) continue;
            // else we shrink around this cycle‐vertex
            lastW = vtx;
            shrinkCycle();
        }

        // Now cycle.size()==3.  Keep offering (1,2).  Eventually these
        // 3 vertices on a triangle must get only 2 colors, forcing a mono‐edge.
        while (uncoloredLeft > 0) {
            out.println("1 2");
            out.flush();
            int vtx = nextInt();
            int c   = nextInt();
            if (vtx < 0) die();
            colState[vtx] = c;
            uncoloredLeft--;
        }
    }

    // Remove lastW from the current cycle and re‐attach so as to keep an odd cycle
    static void shrinkCycle() {
        int w = lastW;
        int L = cycle.size();
        int idx = posW[w];
        // cycle neighbors of w in the current ring
        int iNext = (idx + 1) % L;
        int iPrev = (idx - 1 + L) % L;

        // build the two chordless paths between these neighbors
        ArrayList<Integer> P1 = new ArrayList<>(); // iNext → ... → iPrev
        {
            int cur = iNext;
            while (true) {
                P1.add(cycle.get(cur));
                if (cur == iPrev) break;
                cur = (cur + 1) % L;
            }
        }
        ArrayList<Integer> P2 = new ArrayList<>(); // iPrev → ... → iNext
        {
            int cur = iPrev;
            while (true) {
                P2.add(cycle.get(cur));
                if (cur == iNext) break;
                cur = (cur - 1 + L) % L;
            }
        }

        // we want the path whose number of edges is odd.
        // Path P has (#vertices-1) edges, so edges%2==1 <=> vertices%2==0
        ArrayList<Integer> keep = (P1.size() % 2 == 0 ? P1 : P2);

        // rebuild cycle = w + keep
        for (int v : cycle) inC[v] = false;
        ArrayList<Integer> newC = new ArrayList<>();
        newC.add(w);
        newC.addAll(keep);
        cycle = newC;
        for (int v : cycle) inC[v] = true;
    }
}