import java.io.*;
import java.util.*;

public class Main {
    // Maximum sum of n over all tests is 200k, each insertion uses ≤31 nodes
    // We allocate a global pool of about 6.3 million nodes.
    static final int MAXNODE = 200_000 * 31 + 5;
    static int[] ch0 = new int[MAXNODE], ch1 = new int[MAXNODE];
    static int[] cntEven = new int[MAXNODE], cntOdd = new int[MAXNODE];
    static int nodesUsed;

    // Insert or remove (delta = +1 or -1) value val into the trie,
    // in group g: 0=even, 1=odd.
    static void trieUpdate(int val, int g, int delta) {
        int node = 0;
        // update root count
        if (g == 0) cntEven[node] += delta;
        else          cntOdd[node]  += delta;

        // for bits 30..0
        for (int b = 30; b >= 0; b--) {
            int bit = (val >>> b) & 1;
            if (bit == 0) {
                if (ch0[node] == 0) {
                    ch0[node] = nodesUsed;
                    // initialize the new node
                    ch0[nodesUsed] = ch1[nodesUsed] = 0;
                    cntEven[nodesUsed] = cntOdd[nodesUsed] = 0;
                    nodesUsed++;
                }
                node = ch0[node];
            } else {
                if (ch1[node] == 0) {
                    ch1[node] = nodesUsed;
                    ch0[nodesUsed] = ch1[nodesUsed] = 0;
                    cntEven[nodesUsed] = cntOdd[nodesUsed] = 0;
                    nodesUsed++;
                }
                node = ch1[node];
            }
            if (g == 0) cntEven[node] += delta;
            else         cntOdd[node]  += delta;
        }
    }

    // Query the trie for group g, with query‐value qz,
    // returning {maxXor, the tBest that achieves it}, or maxXor=-1 if empty.
    static int[] trieQuery(int qz, int g) {
        int rootCount = (g == 0 ? cntEven[0] : cntOdd[0]);
        if (rootCount == 0) {
            // that group is empty
            return new int[]{-1, 0};
        }
        int node = 0;
        int bestXor = 0;
        int tBest = 0;
        for (int b = 30; b >= 0; b--) {
            int qb = (qz >>> b) & 1;
            // we want tBit ^ qb = 1 => tBit = 1 - qb
            int want = qb ^ 1;
            int nxt = (want == 0 ? ch0[node] : ch1[node]);
            int cntNxt = (nxt == 0 ? 0 : (g == 0 ? cntEven[nxt] : cntOdd[nxt]));
            if (cntNxt > 0) {
                // we can go that way
                tBest = (tBest << 1) | want;
                bestXor = (bestXor << 1) | 1;
                node = nxt;
            } else {
                // must go the other way
                int other = qb; // since otherBit = qb => tBit^qb=0
                tBest = (tBest << 1) | other;
                bestXor = (bestXor << 1) | 0;
                node = (other == 0 ? ch0[node] : ch1[node]);
            }
        }
        return new int[]{bestXor, tBest};
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        int T = Integer.parseInt(in.readLine().trim());
        while (T-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());

            // Read the tree
            ArrayList<int[]>[] adj = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) adj[i] = new ArrayList<>();
            for (int i = 0; i < n-1; i++) {
                st = new StringTokenizer(in.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                int w = Integer.parseInt(st.nextToken());
                adj[u].add(new int[]{v,w});
                adj[v].add(new int[]{u,w});
            }

            // Compute A[u] = XOR from root=1 to u, and parity depth[u]%2
            int[] A = new int[n+1], depthParity = new int[n+1];
            Deque<int[]> stack = new ArrayDeque<>();
            stack.push(new int[]{1,0}); // (node, parent)
            A[1] = 0;
            depthParity[1] = 0;
            while (!stack.isEmpty()) {
                int[] cur = stack.pop();
                int u = cur[0], p = cur[1];
                for (int[] e : adj[u]) {
                    int v = e[0], w = e[1];
                    if (v == p) continue;
                    A[v] = A[u] ^ w;
                    depthParity[v] = depthParity[u] ^ 1;
                    stack.push(new int[]{v,u});
                }
            }

            // Reset trie for this test
            nodesUsed = 1;
            ch0[0] = ch1[0] = 0;
            cntEven[0] = cntOdd[0] = 0;

            // Insert all A[u] into the trie, in even/odd group
            for (int u = 1; u <= n; u++) {
                trieUpdate(A[u], depthParity[u], +1);
            }

            // Process queries
            int Y = 0;  // global XOR so far
            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(in.readLine());
                String type = st.nextToken();
                if (type.charAt(0) == '^') {
                    // global XOR‐update
                    int y = Integer.parseInt(st.nextToken());
                    Y ^= y;
                } else {
                    // "? v x"
                    int v = Integer.parseInt(st.nextToken());
                    int x = Integer.parseInt(st.nextToken());
                    // compute z = A[v] ^ x, but A[v] is itself possibly flipped by Y if depthParity[v]==1
                    int Av_current = A[v] ^ (depthParity[v]==1 ? Y : 0);
                    int z = Av_current ^ x;

                    // groupEven query with qz = z
                    int[] qe = trieQuery(z, 0);
                    // groupOdd query with qz = z ^ Y  (because odd-group values were A[u]^Y)
                    int[] qo = trieQuery(z ^ Y, 1);

                    int bestEven = qe[0], tEven = qe[1];
                    int bestOdd  = qo[0], tOdd  = qo[1];

                    // Suppose v is in group g = depthParity[v].
                    int g = depthParity[v];
                    // the qz we used for that group:
                    int qzg = (g==0 ? z : z^Y);
                    int bestG = (g==0 ? bestEven : bestOdd);
                    int tG    = (g==0 ? tEven   : tOdd);

                    // maybe the best is from u=v; that yields exactly x.
                    // If tG == A[v] (original) and that value was unique, we must exclude it.
                    int origAv = A[v];
                    boolean exclude = false;
                    if (tG == origAv && bestG == x) {
                        // check how many copies of origAv in that group
                        int node = 0;
                        for (int b = 30; b >= 0; b--) {
                            int bit = (origAv >>> b) & 1;
                            node = (bit==0 ? ch0[node] : ch1[node]);
                        }
                        int cntAtLeaf = (g==0 ? cntEven[node] : cntOdd[node]);
                        if (cntAtLeaf == 1) {
                            exclude = true;
                        }
                    }

                    int ans;
                    if (!exclude) {
                        // no need to exclude, answer is max of bestEven,bestOdd
                        ans = Math.max(bestEven, bestOdd);
                    } else {
                        // temporarily remove A[v] from group g
                        trieUpdate(origAv, g, -1);
                        // re-query just that group
                        int newBestG = trieQuery(qzg, g)[0];
                        // re-insert A[v]
                        trieUpdate(origAv, g, +1);
                        // now compare with other group
                        if (g == 0) {
                            ans = Math.max(newBestG, bestOdd);
                        } else {
                            ans = Math.max(bestEven, newBestG);
                        }
                    }
                    sb.append(ans).append(' ');
                }
            }
            sb.append('\n');
        }
        System.out.print(sb);
    }
}