import java.io.*;
import java.util.*;

public class Main {
    static final int MAXNODE = 6500000;  // ~ 2e5 * 31 + a little extra
    static int[][] nxt = new int[MAXNODE][2];
    static int[] cnt = new int[MAXNODE];
    static int nextNode;
    static int[] root = new int[2];

    // Insert val into trie #t (t = 0 or 1)
    static void trieInsert(int t, int val) {
        int cur = root[t];
        cnt[cur]++;
        for (int b = 30; b >= 0; b--) {
            int bit = (val >> b) & 1;
            if (nxt[cur][bit] == 0) {
                nxt[cur][bit] = nextNode++;
            }
            cur = nxt[cur][bit];
            cnt[cur]++;
        }
    }

    // Remove val from trie #t
    static void trieRemove(int t, int val) {
        int cur = root[t];
        cnt[cur]--;
        for (int b = 30; b >= 0; b--) {
            int bit = (val >> b) & 1;
            cur = nxt[cur][bit];
            cnt[cur]--;
        }
    }

    // Query maximum (val XOR stored) in trie #t; returns -1 if empty
    static int trieQuery(int t, int val) {
        int cur = root[t];
        if (cnt[cur] == 0) return -1;
        int res = 0;
        for (int b = 30; b >= 0; b--) {
            int bit = (val >> b) & 1;
            // prefer opposite branch if nonempty
            int opp = nxt[cur][bit ^ 1];
            if (opp != 0 && cnt[opp] > 0) {
                res |= (1 << b);
                cur = opp;
            } else {
                cur = nxt[cur][bit];
            }
        }
        return res;
    }

    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader();
        StringBuilder output = new StringBuilder();
        int T = in.nextInt();
        while (T-- > 0) {
            int n = in.nextInt(), m = in.nextInt();
            List<int[]>[] adj = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) {
                adj[i] = new ArrayList<>();
            }
            for (int i = 0; i < n-1; i++) {
                int u = in.nextInt();
                int v = in.nextInt();
                int w = in.nextInt();
                adj[u].add(new int[]{v, w});
                adj[v].add(new int[]{u, w});
            }
            // BFS to compute A[u] and parity[u]
            int[] A = new int[n+1];
            int[] parity = new int[n+1];
            boolean[] seen = new boolean[n+1];
            Deque<Integer> dq = new ArrayDeque<>();
            dq.add(1); seen[1] = true;
            A[1] = 0; parity[1] = 0;
            while (!dq.isEmpty()) {
                int u = dq.poll();
                for (int[] e : adj[u]) {
                    int v = e[0], w = e[1];
                    if (!seen[v]) {
                        seen[v] = true;
                        parity[v] = parity[u] ^ 1;
                        A[v] = A[u] ^ w;
                        dq.add(v);
                    }
                }
            }

            // Initialize our 2 tries
            nextNode = 2;
            root[0] = 0; root[1] = 1;
            cnt[0] = cnt[1] = 0;
            nxt[0][0] = nxt[0][1] = 0;
            nxt[1][0] = nxt[1][1] = 0;

            // Insert each A[u] into the appropriate parity-trie
            for (int u = 1; u <= n; u++) {
                trieInsert(parity[u], A[u]);
            }

            int Y = 0;  // cumulative XOR from "^ y" updates
            for (int qi = 0; qi < m; qi++) {
                char c = in.next().charAt(0);
                if (c == '^') {
                    int y = in.nextInt();
                    Y ^= y;
                } else {
                    // query "? v x"
                    int v = in.nextInt();
                    int x = in.nextInt();

                    // Temporarily remove A[v] from its parity-trie
                    int p = parity[v];
                    trieRemove(p, A[v]);

                    // Prepare the two patterns to XOR-query
                    int val0 = A[v] ^ x;       // same-parity
                    int val1 = A[v] ^ Y ^ x;   // opposite-parity

                    int ans0 = trieQuery(p, val0);
                    int ans1 = trieQuery(p^1, val1);
                    int ans = Math.max(ans0, ans1);
                    output.append(ans).append(' ');

                    // Re-insert A[v]
                    trieInsert(p, A[v]);
                }
            }
            output.append('\n');

            // Clear out the trie nodes we used in this test
            for (int i = 0; i < nextNode; i++) {
                cnt[i] = 0;
                nxt[i][0] = nxt[i][1] = 0;
            }
        }
        System.out.print(output);
    }

    /** Fast I/O **/
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
        FastReader() {
            br = new BufferedReader(new InputStreamReader(System.in));
        }
        String next() throws IOException {
            while (st == null || !st.hasMoreTokens())
                st = new StringTokenizer(br.readLine());
            return st.nextToken();
        }
        int nextInt() throws IOException {
            return Integer.parseInt(next());
        }
    }
}