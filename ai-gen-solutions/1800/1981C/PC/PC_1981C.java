import java.io.*;
import java.util.*;

public class Main {
    static final int INF = 1_000_000_000;

    // A small interval class
    static class Interval {
        int l, r;
        Interval(int _l, int _r) { l = _l; r = _r; }
    }

    // Merge a list of intervals (possibly overlapping) into a minimal list of non-overlapping intervals
    static List<Interval> mergeIntervals(List<Interval> v) {
        if (v.isEmpty()) return v;
        // sort by left endpoint
        v.sort(Comparator.comparingInt(a -> a.l));
        List<Interval> res = new ArrayList<>();
        Interval cur = v.get(0);
        for (int i = 1; i < v.size(); i++) {
            Interval nx = v.get(i);
            if (nx.l <= cur.r + 1) {
                // overlap or adjacent => extend
                cur.r = Math.max(cur.r, nx.r);
            } else {
                res.add(cur);
                cur = nx;
            }
        }
        res.add(cur);
        return res;
    }

    // Intersect a list of intervals with [fl..fr] and return the sub-list
    static List<Interval> intersectWithFixed(List<Interval> v, int fl, int fr) {
        if (v.isEmpty()) return v;
        List<Interval> res = new ArrayList<>();
        for (Interval in : v) {
            int nl = Math.max(in.l, fl);
            int nr = Math.min(in.r, fr);
            if (nl <= nr) {
                res.add(new Interval(nl, nr));
            }
        }
        return mergeIntervals(res);
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        int t = Integer.parseInt(br.readLine().trim());

        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            int[] a = new int[n];
            StringTokenizer st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            // fwd[i] = possible intervals for b[i] given constraints from the left
            List<Interval>[] fwd = new List[n];
            // bwd[i] = possible intervals for b[i] given constraints from the right
            List<Interval>[] bwd = new List[n];

            // Forward pass
            // initialize fwd[0]
            fwd[0] = new ArrayList<>();
            if (a[0] == -1) {
                fwd[0].add(new Interval(1, INF));
            } else {
                fwd[0].add(new Interval(a[0], a[0]));
            }

            boolean ok = true;
            // propagate forward
            for (int i = 1; i < n; i++) {
                List<Interval> tmp = new ArrayList<>();
                for (Interval iv : fwd[i - 1]) {
                    long L = iv.l, R = iv.r;
                    // children
                    long cl = 2 * L, cr = 2 * R + 1;
                    if (cl <= INF) {
                        int il = (int) cl;
                        int ir = (int) Math.min(cr, INF);
                        tmp.add(new Interval(il, ir));
                    }
                    // parent
                    int pl = iv.l / 2;
                    int pr = iv.r / 2;
                    if (pl <= pr) {
                        tmp.add(new Interval(pl, pr));
                    }
                }
                // merge, then intersect with the fixed value (if any)
                tmp = mergeIntervals(tmp);
                int fl = (a[i] == -1 ? 1 : a[i]);
                int fr = (a[i] == -1 ? INF : a[i]);
                tmp = intersectWithFixed(tmp, fl, fr);
                if (tmp.isEmpty()) {
                    ok = false;
                    break;
                }
                fwd[i] = tmp;
            }

            if (!ok) {
                bw.write("-1\n");
                continue;
            }

            // Backward pass
            bwd[n - 1] = new ArrayList<>();
            if (a[n - 1] == -1) {
                bwd[n - 1].add(new Interval(1, INF));
            } else {
                bwd[n - 1].add(new Interval(a[n - 1], a[n - 1]));
            }

            for (int i = n - 2; i >= 0; i--) {
                List<Interval> tmp = new ArrayList<>();
                for (Interval iv : bwd[i + 1]) {
                    long L = iv.l, R = iv.r;
                    // parent
                    int pl = iv.l / 2;
                    int pr = iv.r / 2;
                    if (pl <= pr) {
                        tmp.add(new Interval(pl, pr));
                    }
                    // children
                    long cl = 2 * L, cr = 2 * R + 1;
                    if (cl <= INF) {
                        int il = (int) cl;
                        int ir = (int) Math.min(cr, INF);
                        tmp.add(new Interval(il, ir));
                    }
                }
                tmp = mergeIntervals(tmp);
                int fl = (a[i] == -1 ? 1 : a[i]);
                int fr = (a[i] == -1 ? INF : a[i]);
                tmp = intersectWithFixed(tmp, fl, fr);
                if (tmp.isEmpty()) {
                    ok = false;
                    break;
                }
                bwd[i] = tmp;
            }

            if (!ok) {
                bw.write("-1\n");
                continue;
            }

            // Intersect fwd and bwd to get final possible intervals
            List<Interval>[] fin = new List[n];
            for (int i = 0; i < n; i++) {
                List<Interval> merged = new ArrayList<>();
                for (Interval i1 : fwd[i]) {
                    for (Interval i2 : bwd[i]) {
                        int L = Math.max(i1.l, i2.l);
                        int R = Math.min(i1.r, i2.r);
                        if (L <= R) merged.add(new Interval(L, R));
                    }
                }
                merged = mergeIntervals(merged);
                if (merged.isEmpty()) {
                    ok = false;
                    break;
                }
                fin[i] = merged;
            }
            if (!ok) {
                bw.write("-1\n");
                continue;
            }

            // Finally, build an explicit solution b[] by greedy
            int[] b = new int[n];
            // pick smallest in fin[0]
            b[0] = fin[0].get(0).l;
            for (int i = 1; i < n; i++) {
                int prev = b[i - 1];
                // candidates = parent or children of prev
                List<Interval> cand = new ArrayList<>();
                int pl = prev / 2;
                if (pl >= 1) {
                    cand.add(new Interval(pl, pl));
                }
                long cl = 2L * prev, cr = 2L * prev + 1;
                if (cl <= INF) {
                    int il = (int) cl;
                    int ir = (int) Math.min(cr, INF);
                    cand.add(new Interval(il, ir));
                }
                // intersect cand with fin[i], pick smallest feasible
                int best = Integer.MAX_VALUE;
                for (Interval c : cand) {
                    for (Interval f : fin[i]) {
                        int L = Math.max(c.l, f.l);
                        int R = Math.min(c.r, f.r);
                        if (L <= R && L < best) {
                            best = L;
                        }
                    }
                }
                if (best == Integer.MAX_VALUE) {
                    ok = false;
                    break;
                }
                b[i] = best;
            }

            if (!ok) {
                bw.write("-1\n");
            } else {
                // print the found sequence
                for (int i = 0; i < n; i++) {
                    if (i > 0) bw.write(" ");
                    bw.write(Integer.toString(b[i]));
                }
                bw.write("\n");
            }
        }

        bw.flush();
    }
}