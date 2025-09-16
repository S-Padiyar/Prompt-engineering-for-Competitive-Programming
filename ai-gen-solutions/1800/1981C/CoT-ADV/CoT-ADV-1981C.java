import java.io.*;
import java.util.*;

public class Main {
    static final long MAXV = 1_000_000_000L;

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            int n = Integer.parseInt(in.readLine().trim());
            long[] a = new long[n+1];
            StringTokenizer st = new StringTokenizer(in.readLine());
            List<Integer> pos = new ArrayList<>();
            for (int i = 1; i <= n; i++) {
                a[i] = Long.parseLong(st.nextToken());
                if (a[i] != -1) pos.add(i);
            }

            // Case 1: everything is -1 => simple alternating fill 1,2,1,2,...
            if (pos.isEmpty()) {
                for (int i = 1; i <= n; i++) {
                    sb.append((i % 2 == 1 ? 1 : 2)).append(' ');
                }
                sb.append('\n');
                continue;
            }

            // Prepare result array, copy known entries
            long[] b = new long[n+2];
            for (int p : pos) b[p] = a[p];

            // Pre‐check feasibility between known anchors
            boolean ok = true;
            for (int i = 0; i + 1 < pos.size(); i++) {
                int p1 = pos.get(i), p2 = pos.get(i+1);
                long v1 = a[p1], v2 = a[p2];
                int gap = p2 - p1;
                int d = distanceInBinaryTree(v1, v2);
                if (d > gap || ((gap - d) & 1) != 0) {
                    ok = false;
                    break;
                }
            }
            if (!ok) {
                sb.append("-1\n");
                continue;
            }

            // A helper to fill a gap between two anchors (lpos, lval) => (rpos, rval)
            // with exactly (rpos - lpos) steps in the tree, padding by 2‐cycles at the start.
            // We then write into b[] for all positions in [lpos..rpos].
            class SegmentFiller {
                List<Long> fill(int lp, long lv, int rp, long rv) {
                    int gap = rp - lp;
                    // 1) build the shortest path from lv to rv
                    List<Long> basePath = getShortestPath(lv, rv);
                    int baseLen = basePath.size() - 1;     // number of edges
                    int extra = gap - baseLen;
                    // pad by 2‐cycles at lv
                    List<Long> walk = new ArrayList<>(gap + 1);
                    walk.add(lv);
                    for (int k = 0; k < extra/2; k++) {
                        walk.add(2*lv);
                        walk.add(lv);
                    }
                    // append the remainder of the path
                    for (int i = 1; i < basePath.size(); i++) {
                        walk.add(basePath.get(i));
                    }
                    return walk;
                }
            }
            SegmentFiller sf = new SegmentFiller();

            // Fill the segment before the first known anchor, if any
            int first = pos.get(0);
            if (first > 1) {
                long rv = a[first];
                int gap = first - 0;
                // choose an X so that (gap, dist(X, rv)) match parity
                long lv = (gap % 2 == 0 ? rv : makeNeighbor(rv));
                List<Long> walk = sf.fill(0, lv, first, rv);
                // write walk[1..first] into b[1..first]
                for (int i = 1; i <= first; i++) {
                    b[i] = walk.get(i);
                }
            }

            // Fill between known anchors
            for (int i = 0; i + 1 < pos.size(); i++) {
                int lpos = pos.get(i), rpos = pos.get(i+1);
                long lval = b[lpos], rval = b[rpos];
                List<Long> walk = sf.fill(lpos, lval, rpos, rval);
                for (int k = 0; k < walk.size(); k++) {
                    int idx = lpos + k;
                    b[idx] = walk.get(k);
                }
            }

            // Fill after the last known anchor, if any
            int last = pos.get(pos.size()-1);
            if (last < n) {
                long lv = b[last];
                int gap = (n+1) - last;
                long rv = (gap % 2 == 0 ? lv : makeNeighbor(lv));
                List<Long> walk = sf.fill(last, lv, n+1, rv);
                // assign walk[1..n-last] into b[last+1..n]
                for (int k = 1; k < walk.size() && last + k <= n; k++) {
                    b[last + k] = walk.get(k);
                }
            }

            // Output result
            for (int i = 1; i <= n; i++) {
                sb.append(b[i]).append(' ');
            }
            sb.append('\n');
        }

        System.out.print(sb.toString());
    }

    /**  
     * Compute the distance between two positive integers u,v in the infinite
     * binary‐tree graph (parent = floor(x/2), children = 2x,2x+1).
     */
    static int distanceInBinaryTree(long u, long v) {
        // climb ancestors of u
        Map<Long,Integer> distU = new HashMap<>();
        long x = u;
        int d = 0;
        while (x >= 1) {
            distU.put(x, d++);
            if (x == 1) break;
            x /= 2;
        }
        // climb from v until we find a common ancestor
        long y = v;
        int d2 = 0;
        while (y >= 1) {
            if (distU.containsKey(y)) {
                return distU.get(y) + d2;
            }
            y /= 2;
            d2++;
        }
        // should never happen, the root=1 is common
        return Integer.MAX_VALUE;
    }

    /**
     * Construct the shortest path (list of nodes) in that tree from u to v.
     * We find the LCA by marking ancestors of u, then climb v.
     */
    static List<Long> getShortestPath(long u, long v) {
        // mark ancestors of u
        Map<Long,Integer> distU = new HashMap<>();
        List<Long> ancU = new ArrayList<>();
        long x = u;
        int d = 0;
        while (x >= 1) {
            distU.put(x, d++);
            ancU.add(x);
            if (x == 1) break;
            x /= 2;
        }
        // climb from v to find LCA
        long y = v;
        int d2 = 0;
        while (y >= 1 && !distU.containsKey(y)) {
            y /= 2;
            d2++;
        }
        long lca = y;
        int du = distU.get(lca);
        int dv = d2;

        // build path u -> ... -> lca
        List<Long> path = new ArrayList<>();
        // ancU[0] = u, ancU[1] = u/2, ..., ancU[du] = lca
        for (int i = 0; i <= du; i++) {
            path.add(ancU.get(i));
        }

        // build path from lca down to v by retracing v's climb
        List<Long> down = new ArrayList<>();
        y = v;
        while (y != lca) {
            down.add(y);
            y /= 2;
        }
        Collections.reverse(down);

        // append
        path.addAll(down);
        return path;
    }

    /**  
     * Pick one neighbor of x (parent if >=1, otherwise a valid child)
     * so as to shift parity of distance by exactly one
     */
    static long makeNeighbor(long x) {
        long p = x/2;
        if (p >= 1) return p;
        // if x=1, parent=0 not valid, go to child 2*1=2
        return 2*x;
    }
}