import java.io.*;
import java.util.*;

public class Main {
    // A standard segment‐tree for point‐update and range‐max query on [1..n].
    static class SegTree {
        int n;
        int[] st;
        SegTree(int _n) {
            n = 1;
            while (n < _n) n <<= 1;
            st = new int[2*n];
        }
        // set position i (1-based) to value v
        void update(int i, int v) {
            i = i + n - 1;
            st[i] = v;
            for (i >>= 1; i > 0; i >>= 1) {
                st[i] = Math.max(st[2*i], st[2*i+1]);
            }
        }
        // maximum on interval [l..r], 1-based
        int query(int l, int r) {
            if (l > r) return 0;
            int res = 0;
            l = l + n - 1; 
            r = r + n - 1;
            while (l <= r) {
                if ((l & 1) == 1) res = Math.max(res, st[l++]);
                if ((r & 1) == 0) res = Math.max(res, st[r--]);
                l >>= 1; 
                r >>= 1;
            }
            return res;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        int T = Integer.parseInt(in.readLine().trim());
        while (T-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int Q = Integer.parseInt(st.nextToken());
            int[] a = new int[n+1];
            st = new StringTokenizer(in.readLine());
            for (int i = 1; i <= n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            // Build global occurrence lists
            ArrayList<Integer>[] occ = new ArrayList[n+1];
            for (int v = 1; v <= n; v++) occ[v] = new ArrayList<>();
            for (int i = 1; i <= n; i++) {
                occ[a[i]].add(i);
            }
            // For each index i, store its position in occ[a[i]]
            int[] posIdx = new int[n+1];
            for (int v = 1; v <= n; v++) {
                ArrayList<Integer> L = occ[v];
                for (int j = 0; j < L.size(); j++) {
                    posIdx[L.get(j)] = j;
                }
            }

            // fIdx[v], hIdx[v]: the window‐local first/last indices into occ[v].
            int[] fIdx = new int[n+1], hIdx = new int[n+1];
            int[] cnt  = new int[n+1];
            Arrays.fill(fIdx, -1);
            Arrays.fill(hIdx, -1);

            // R[l] = the minimal r that BREAKS laminarity for window starting at l.
            int[] R = new int[n+2];

            // Segment tree for M[i]=gpos of interval starting at i, or 0 if none.
            SegTree seg = new SegTree(n);

            // Two‐pointer sweep
            int r = 1;    // current window is [l..r-1]
            for (int l = 1; l <= n; l++) {
                // extend r as far as we can
                while (r <= n) {
                    int v = a[r];
                    int idx = posIdx[r];  // which occurrence of v is this?
                    if (cnt[v] == 0) {
                        // new interval [r,r] – cannot cross
                        fIdx[v] = hIdx[v] = idx;
                        seg.update(r, r);
                        cnt[v]++;
                        r++;
                    } else {
                        // interval grows from [f..oldg] to [f..r]
                        int firstPos = occ[v].get(fIdx[v]);
                        int oldg     = occ[v].get(hIdx[v]);
                        // test if any other interval [f_u..g_u] with f_u<firstPos
                        // has g_u > oldg
                        int bestLeft = seg.query(1, firstPos-1);
                        if (bestLeft > oldg) {
                            // crossing would occur → stop extensions
                            break;
                        }
                        // safe to grow
                        hIdx[v] = idx;
                        seg.update(firstPos, r);
                        cnt[v]++;
                        r++;
                    }
                }
                // r is the first breaker (or r==n+1)
                R[l] = r;

                // remove a[l] from the window
                int w = a[l];
                int id = posIdx[l];
                if (--cnt[w] == 0) {
                    // delete the whole interval
                    int fp = occ[w].get(fIdx[w]);
                    seg.update(fp, 0);
                    fIdx[w] = hIdx[w] = -1;
                } else {
                    // still has copies.  Maybe we removed the first or last.
                    if (id == fIdx[w]) {
                        // lost the first
                        int oldFp = occ[w].get(fIdx[w]);
                        int gpos  = occ[w].get(hIdx[w]);
                        seg.update(oldFp, 0);
                        fIdx[w]++;
                        int newFp = occ[w].get(fIdx[w]);
                        seg.update(newFp, gpos);
                    } else if (id == hIdx[w]) {
                        // lost the last
                        int fp   = occ[w].get(fIdx[w]);
                        hIdx[w]--; 
                        int newG = occ[w].get(hIdx[w]);
                        seg.update(fp, newG);
                    }
                    // if it was an interior occurrence, no change to [f..g]
                }
            }

            // Answer queries in O(1) each
            for (int i = 0; i < Q; i++) {
                st = new StringTokenizer(in.readLine());
                int L = Integer.parseInt(st.nextToken());
                int RR= Integer.parseInt(st.nextToken());
                out.println(RR < R[L] ? "YES" : "NO");
            }
        }
        out.flush();
    }
}