import java.io.*;
import java.util.*;

public class Main {
    static final long INF = (long)1e18;

    // A custom min‐heap of (time,node), no decrease‐key,
    // just push new entries and ignore stale ones.
    static class MinHeap {
        int size;
        int[] node;
        long[] time;

        MinHeap(int cap) {
            node = new int[cap+1];
            time = new long[cap+1];
            size = 0;
        }

        void clear() {
            size = 0;
        }

        boolean isEmpty() {
            return size == 0;
        }

        void push(int u, long t) {
            size++;
            node[size] = u;
            time[size] = t;
            siftUp(size);
        }

        // peek root (without removing)
        int  peekNode() { return node[1]; }
        long peekTime() { return time[1]; }

        // remove root
        void pop() {
            node[1] = node[size];
            time[1] = time[size];
            size--;
            siftDown(1);
        }

        void siftUp(int i) {
            while (i > 1) {
                int p = i >>> 1;
                if (time[p] <= time[i]) break;
                swap(p, i);
                i = p;
            }
        }

        void siftDown(int i) {
            while (true) {
                int l = i<<1, r = l+1, best = i;
                if (l <= size && time[l] < time[best]) best = l;
                if (r <= size && time[r] < time[best]) best = r;
                if (best == i) break;
                swap(best, i);
                i = best;
            }
        }

        void swap(int i, int j) {
            long tt = time[i]; time[i] = time[j]; time[j] = tt;
            int uu = node[i]; node[i] = node[j]; node[j] = uu;
        }
    }

    // Graph in adjacency‐list form with two costs per edge.
    static int[] head, nxt, to;
    static long[] btime, wtime;  // bus time, walk time
    static int edgeCnt;
    static int n, m;
    static long t0, t1, t2;
    static long[] dist;          // used in Dijkstra
    static MinHeap heap;

    static void addEdge(int u, int v, long b, long w) {
        to[edgeCnt] = v;
        btime[edgeCnt] = b;
        wtime[edgeCnt] = w;
        nxt[edgeCnt] = head[u];
        head[u] = edgeCnt++;
    }

    // Return true iff we can start at time S from node 1
    // and reach node n no later than t0 under the bus‐ban in [t1,t2].
    static boolean feasible(long S) {
        // initialize
        for (int i = 1; i <= n; i++) dist[i] = INF;
        dist[1] = S;
        heap.clear();
        heap.push(1, S);

        while (!heap.isEmpty()) {
            int u = heap.peekNode();
            long cur = heap.peekTime();
            heap.pop();
            // stale entry?
            if (cur != dist[u]) continue;
            // if we already exceed t0, no point in continuing
            if (cur > t0) break;
            // if we've reached the target in time, success
            if (u == n) return true;

            // relax all edges
            for (int e = head[u]; e != -1; e = nxt[e]) {
                int v = to[e];
                // 1) walking is always allowed
                long nt = cur + wtime[e];
                if (nt < dist[v] && nt <= t0) {
                    dist[v] = nt;
                    heap.push(v, nt);
                }
                // 2) bus only if either (start< t1 and finish≤ t1) or start≥ t2
                long bt = btime[e];
                if (cur < t1) {
                    if (cur + bt <= t1) {
                        nt = cur + bt;
                        if (nt < dist[v] && nt <= t0) {
                            dist[v] = nt;
                            heap.push(v, nt);
                        }
                    }
                } else if (cur >= t2) {
                    nt = cur + bt;
                    if (nt < dist[v] && nt <= t0) {
                        dist[v] = nt;
                        heap.push(v, nt);
                    }
                }
            }
        }
        // if dist[n] ≤ t0 we returned true above
        return false;
    }

    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader();
        PrintWriter out = new PrintWriter(System.out);

        int T = in.nextInt();
        while (T-- > 0) {
            n = in.nextInt();
            m = in.nextInt();
            t0 = in.nextLong();
            t1 = in.nextLong();
            t2 = in.nextLong();

            // allocate graph
            head = new int[n+1];
            nxt  = new int[2*m];
            to   = new int[2*m];
            btime= new long[2*m];
            wtime= new long[2*m];
            Arrays.fill(head, -1);
            edgeCnt = 0;

            for (int i = 0; i < m; i++) {
                int u = in.nextInt();
                int v = in.nextInt();
                long b = in.nextLong();
                long w = in.nextLong();
                addEdge(u, v, b, w);
                addEdge(v, u, b, w);
            }

            // For Dijkstra we need a dist[] plus a heap
            dist = new long[n+1];
            // heap capacity: in worst case we push up to ~2*m entries per run
            heap = new MinHeap(2*m + 5);

            // Binary‐search the maximum S in [0..t0] for which feasible(S) is true
            long lo = 0, hi = t0, ans = -1;
            while (lo <= hi) {
                long mid = (lo + hi) >>> 1;
                if (feasible(mid)) {
                    ans = mid;
                    lo = mid + 1;
                } else {
                    hi = mid - 1;
                }
            }

            out.println(ans);
        }
        out.flush();
    }

    // fast input
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
        int nextInt()    throws IOException { return Integer.parseInt(next()); }
        long nextLong()  throws IOException { return Long.parseLong(next()); }
    }
}