import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(in.readLine().trim());
        // We'll need up to sum(n) <= 2e5, so we size arrays generously.
        int MAX = 200_000 + 5;

        // adjacency of alt-edges by their start u
        ArrayList<int[]>[] byStart = new ArrayList[MAX];
        for(int i = 0; i < MAX; i++) {
            byStart[i] = new ArrayList<>();
        }

        // Segment tree arrays
        long[] seg = new long[4*MAX];     // seg[v] = max in that node's interval
        long[] lazy = new long[4*MAX];    // lazy[v]>=0 means "set entire seg[v]'s interval = lazy[v]"

        // Build / reset the segment tree for indices [1..n], all zeros
        Runnable build = () -> {
            Arrays.fill(seg, 0L);
            Arrays.fill(lazy, -1L);
        };

        // push a lazy "set-to" down
        BiConsumer<Integer,Integer> push = (node, len) -> {
            long x = lazy[node];
            if (x >= 0) {
                int lc = node*2, rc = node*2+1;
                seg[lc] = x;
                seg[rc] = x;
                lazy[lc] = x;
                lazy[rc] = x;
                lazy[node] = -1L;
            }
        };

        // range-set [ql..qr] = val
        class Update {
            void upd(int node, int l, int r, int ql, int qr, long val) {
                if (ql > r || qr < l) return;
                if (ql <= l && r <= qr) {
                    seg[node] = val;
                    lazy[node] = val;
                    return;
                }
                push.accept(node, r-l+1);
                int mid = (l+r) >>> 1;
                upd(node*2, l, mid, ql, qr, val);
                upd(node*2+1, mid+1, r, ql, qr, val);
                seg[node] = Math.max(seg[node*2], seg[node*2+1]);
            }
        }
        Update updater = new Update();

        // point-query
        class Query {
            long query(int node, int l, int r, int pos) {
                if (l==r) return seg[node];
                push.accept(node, r-l+1);
                int mid = (l+r)>>>1;
                if (pos<=mid) return query(node*2, l, mid, pos);
                else         return query(node*2+1, mid+1, r, pos);
            }
            // find first index >= startPos with seg[idx] >= threshold, or return n+1
            int firstGE(int node, int l, int r, int startPos, long threshold) {
                if (startPos>r || seg[node]<threshold) return (MAX-1); 
                if (l==r) return l;
                push.accept(node, r-l+1);
                int mid = (l+r)>>>1;
                int res;
                if (startPos<=mid) {
                    res = firstGE(node*2, l, mid, startPos, threshold);
                    if (res<=mid) return res;
                    return firstGE(node*2+1, mid+1, r, startPos, threshold);
                } else {
                    return firstGE(node*2+1, mid+1, r, startPos, threshold);
                }
            }
        }
        Query querier = new Query();

        StringBuilder sb = new StringBuilder();
        for(int _case=0;_case<t;_case++) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());
            // clear adjacency
            for(int i=1;i<=n;i++) byStart[i].clear();
            // read alt-bridges
            for(int i=0;i<m;i++) {
                st = new StringTokenizer(in.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                byStart[u].add(new int[]{u,v});
            }

            // reset dp‐array in the segtree
            build.run();

            // We'll build answer for s=1..n-1
            // Process s in increasing order, each time "un‐hide" edges with start u=s
            // Actually for s we need edges u<s, so we add byStart[s-1] at iteration s.
            sb.setLength(0);
            for(int s=1; s<n; s++) {
                int unew = s-1;
                if (unew>=1) {
                    for(var e: byStart[unew]) {
                        int u = e[0], v = e[1];
                        // gain
                        long w = (v - u) - 1L;
                        // dp[u]
                        long cur = querier.query(1,1,n,u);
                        long cand = cur + w;
                        long oldv = querier.query(1,1,n,v);
                        if (cand>oldv) {
                            // find first pos >= v where dp[pos] >= cand
                            int r = querier.firstGE(1,1,n,v,cand);
                            // if none found r==n+1
                            if (r>n) r = n+1;
                            int rr = r-1;  // we'll update [v..rr]
                            if (v<=rr) {
                                updater.upd(1,1,n,v,rr,cand);
                            }
                        }
                    }
                }
                // now dp[n] = maximum gain Elsie can collect by the time she reaches n
                long bestAtN = querier.query(1,1,n,n);
                // Bessie wins iff dp[n] < s
                sb.append(bestAtN < s ? '1' : '0');
            }
            sb.append('\n');
            System.out.print(sb);
        }
    }
}