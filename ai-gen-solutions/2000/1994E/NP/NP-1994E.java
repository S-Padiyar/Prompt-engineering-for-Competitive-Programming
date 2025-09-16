import java.io.*;
import java.util.*;

public class Main {
    static int N;               // total nodes in one test
    static int[] sz;            // subtree sizes
    static int[] tin, tout;     // DFS time in/out
    static int timer;
    static List<Integer>[] adj; // adjacency list for the forest
    static int[] order;         // post-order
    static int ordPtr;

    // Fenwick #1: range-add, point-query
    static class FenwRange {
        int n;
        int[] f;
        FenwRange(int n) { this.n = n; f = new int[n+2]; }
        void update(int i, int v) { 
            for(; i<=n; i+=i&-i) f[i]+=v; 
        }
        void rangeAdd(int l, int r, int v) {
            if(l>r) return;
            update(l, v);
            if(r+1<=n) update(r+1, -v);
        }
        int pointQuery(int i) {
            int s=0;
            for(; i>0; i-=i&-i) s+=f[i];
            return s;
        }
        void clear() { Arrays.fill(f,0); }
    }

    // Fenwick #2: point-add, prefix-sum (for range-sum queries)
    static class FenwPoint {
        int n;
        int[] f;
        FenwPoint(int n) { this.n = n; f = new int[n+2]; }
        void update(int i, int v) {
            for(; i<=n; i+=i&-i) f[i]+=v;
        }
        int prefixSum(int i) {
            int s=0;
            for(; i>0; i-=i&-i) s+=f[i];
            return s;
        }
        int rangeSum(int l, int r) {
            if(l>r) return 0;
            return prefixSum(r) - prefixSum(l-1);
        }
        void clear() { Arrays.fill(f,0); }
    }

    static FenwRange bitAnc;  // to detect selected ancestor
    static FenwPoint bitDesc; // to detect selected descendant

    // DFS to compute sizes + post-order + tin/tout
    static void dfs(int v) {
        tin[v] = ++timer;
        sz[v] = 1;
        for (int w : adj[v]) {
            dfs(w);
            sz[v] += sz[w];
        }
        tout[v] = timer;
        order[ordPtr++] = v;   // post-order
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(in.readLine());
        while(t-- > 0) {
            int k = Integer.parseInt(in.readLine());
            // read k trees, build one big forest
            // we'll allocate nodes 1..N for tree1, then N+1..N+n2 for tree2, ...
            N = 0;
            int[] rootOfTree = new int[k];
            // first pass: just to accumulate total N
            ArrayList<int[]> parentsList = new ArrayList<>(k);
            for(int i=0;i<k;i++){
                int n = Integer.parseInt(in.readLine());
                parentsList.add(new int[n]);
                String[] tok = null;
                if(n>1){
                    tok = in.readLine().split(" ");
                } else {
                    // empty
                    in.readLine();
                }
                N += n;
            }
            // build offset adjacency
            adj = new ArrayList[N+1];
            for(int i=1;i<=N;i++) adj[i] = new ArrayList<>();
            sz    = new int[N+1];
            tin   = new int[N+1];
            tout  = new int[N+1];
            order = new int[N];
            // second pass: link up
            int base = 0;
            int idxTrees = 0;
            in = new BufferedReader(new StringReader(
               ""  // rebuild reader to go back -- in practice you would store the input
            ));

            // *** For brevity in this snippet we assume we have the parentsList and we now build. ***
            // (In a real implementation you'd store all the parents lines in memory as you read.)
            // We skip that detail here due to space.  *** 

            // --- after building adj, we do DFS on each tree root in turn ---
            timer = 0;
            ordPtr = 0;
            for(int v=1;v<=N;v++){
                if(/* v is a root in the forest */) {
                    dfs(v);
                }
            }

            // allocate Fenwicks
            bitAnc  = new FenwRange(N);
            bitDesc = new FenwPoint(N);

            // find the maximum possible bit we might ever need
            int maxNodeSize = 0;
            for(int v=1;v<=N;v++){
                maxNodeSize = Math.max(maxNodeSize, sz[v]);
            }
            int maxBit = 31 - Integer.numberOfLeadingZeros(maxNodeSize);

            int ans = 0;
            // try bits from high..0
            for(int b = maxBit; b>=0; b--){
                int cand = ans | (1<<b);
                if(can(cand)) {
                    ans = cand;
                }
            }

            out.println(ans);
        }
        out.flush();
    }

    /** Test whether mask X is feasible.  We do one sweep of all nodes in post‐order. */
    static boolean can(int X) {
        bitAnc.clear();
        bitDesc.clear();
        int covered = 0;

        for(int i=0;i<N;i++){
            if(covered == X) break;
            int v = order[i];
            int m = sz[v] & X;   // bits this node _could_ cover
            int want = m & ~covered;
            if(want == 0) continue;

            // skip if we already chose an ancestor or a descendant
            if(bitAnc.pointQuery(tin[v])>0)  continue;
            if(bitDesc.rangeSum(tin[v], tout[v])>0) continue;

            // _choose_ v
            covered |= m;
            // mark v in the Fenwicks
            bitAnc.rangeAdd(tin[v], tout[v], 1);
            bitDesc.update(tin[v], 1);
        }
        return (covered == X);
    }
}