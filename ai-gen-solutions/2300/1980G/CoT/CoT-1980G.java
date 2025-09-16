import java.io.*;
import java.util.*;

public class Main {
    static final int MAXNODE = 6300000;
    // For each node in the trie:
    //   child[node][0 or 1] = index of that bit-child (0 if none)
    //   cnt[node][0 or 1]   = how many values of even/odd‐depth pass here
    static int[][] child = new int[MAXNODE][2];
    static int[][] cnt   = new int[MAXNODE][2];
    static int    totNodes = 1;  // next free trie‐node index
    
    // Insert or delete one fValue with parity p (0=even depth,1=odd depth)
    static void trieUpdate(int root, int fValue, int p, int delta) {
        // delta = +1 to insert, -1 to delete
        int node = root;
        cnt[node][p] += delta;
        for (int bit = 29; bit >= 0; --bit) {
            int b = (fValue >>> bit) & 1;
            if (child[node][b] == 0) {
                child[node][b] = totNodes++;
            }
            node = child[node][b];
            cnt[node][p] += delta;
        }
    }
    
    // Query max XOR of t0 with any fValue of parity p in this trie
    // Returns -1 if no such value
    static int trieQuery(int root, int t0, int p) {
        int node = root;
        if (cnt[node][p] == 0) return -1;  // no values with parity p
        int ans = 0;
        for (int bit = 29; bit >= 0; --bit) {
            int tb = (t0 >>> bit) & 1;
            // we'd like to go to child[1-tb] if it has any p‐values
            int want = 1 - tb;
            if (child[node][want] != 0 && cnt[child[node][want]][p] > 0) {
                ans |= (1 << bit);
                node = child[node][want];
            } else {
                node = child[node][1 - want];
            }
        }
        return ans;
    }
    
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        int T = Integer.parseInt(br.readLine().trim());
        
        // Adjacency storage for the tree
        int[] head = new int[200_005];
        int[] to   = new int[400_005];
        int[] wgt  = new int[400_005];
        int[] nxt  = new int[400_005];
        
        // f[i] = XOR‐prefix from root=1 to i
        // depth[i] in the tree, we only care depth%2
        int[] f = new int[200_005];
        int[] depth = new int[200_005];
        
        StringBuilder output = new StringBuilder();
        while (T-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());
            
            // build adjacency
            Arrays.fill(head, 1, n+1, -1);
            int eidx = 0;
            for (int i = 1; i < n; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                int w = Integer.parseInt(st.nextToken());
                to[eidx] = v; wgt[eidx] = w; nxt[eidx] = head[u]; head[u] = eidx++;
                to[eidx] = u; wgt[eidx] = w; nxt[eidx] = head[v]; head[v] = eidx++;
            }
            
            // BFS to compute f[] and depth[]
            Deque<Integer> dq = new ArrayDeque<>();
            dq.add(1);
            f[1] = 0;
            depth[1] = 0;
            boolean[] seen = new boolean[n+1];
            seen[1] = true;
            while (!dq.isEmpty()) {
                int u = dq.poll();
                for (int ed = head[u]; ed != -1; ed = nxt[ed]) {
                    int v = to[ed];
                    if (!seen[v]) {
                        seen[v] = true;
                        depth[v] = depth[u] + 1;
                        f[v] = f[u] ^ wgt[ed];
                        dq.add(v);
                    }
                }
            }
            
            // Build a brand‐new trie for this test
            int root = totNodes++;  
            
            // Insert all f[i], keep track of how many even/odd
            int[] sz = new int[2];
            for (int i = 1; i <= n; i++) {
                int p = (depth[i] & 1);
                trieUpdate(root, f[i], p, +1);
                sz[p]++;
            }
            
            int tagOdd = 0;  // the running XOR‐tag for odd‐depth f[i]
            
            // Process queries
            for (int qi = 0; qi < m; qi++) {
                st = new StringTokenizer(br.readLine());
                String op = st.nextToken();
                if (op.charAt(0) == '^') {
                    int y = Integer.parseInt(st.nextToken());
                    tagOdd ^= y; 
                } else {
                    int v = Integer.parseInt(st.nextToken());
                    int x = Integer.parseInt(st.nextToken());
                    int p = (depth[v] & 1);
                    
                    // t0 = f[v] ^ x in the *original*‐f sense
                    int t0 = f[v] ^ x;
                    
                    // Case 1: pick u of SAME parity p, but we must not pick v itself.
                    // If sz[p]>1, we can safely remove v, query, restore.
                    int bestSame = -1;
                    if (sz[p] > 1) {
                        trieUpdate(root, f[v], p, -1);
                        sz[p]--;
                        bestSame = trieQuery(root, t0, p);
                        trieUpdate(root, f[v], p, +1);
                        sz[p]++;
                    }
                    
                    // Case 2: pick u of OPPOSITE parity 1-p
                    int bestOpp = -1;
                    if (sz[1-p] > 0) {
                        bestOpp = trieQuery(root, t0, 1 - p);
                    }
                    
                    // The cycle‐XOR if we pick same‐parity u is exactly bestSame.
                    // If we pick opposite‐parity u, the real XOR is bestOpp ^ tagOdd.
                    int ansSame = (bestSame < 0 ? Integer.MIN_VALUE : bestSame);
                    int ansOpp  = (bestOpp  < 0 ? Integer.MIN_VALUE : (bestOpp ^ tagOdd));
                    int ans = Math.max(ansSame, ansOpp);
                    
                    output.append(ans).append(' ');
                }
            }
            output.append('\n');
        }
        
        System.out.print(output);
    }
}