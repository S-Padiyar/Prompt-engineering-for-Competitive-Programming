import java.io.*;
import java.util.*;

public class Main {
    static final int INF = 1_000_000_000;

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer tok = new StringTokenizer(in.readLine());
        int t = Integer.parseInt(tok.nextToken());

        while (t-- > 0) {
            tok = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(tok.nextToken());
            int q = Integer.parseInt(tok.nextToken());

            int[] a = new int[n+1];
            tok = new StringTokenizer(in.readLine());
            for (int i = 1; i <= n; i++) {
                a[i] = Integer.parseInt(tok.nextToken());
            }

            // We will compute badR[ℓ] = smallest r where [ℓ..r] has a crossing,
            // or n+1 if none.
            int[] badR = new int[n+2];

            // For each value x in [1..n], we keep a TreeSet of its positions
            // currently in the window [ℓ..r].
            @SuppressWarnings("unchecked")
            TreeSet<Integer>[] pos = new TreeSet[n+1];
            for (int i = 1; i <= n; i++) {
                pos[i] = new TreeSet<>();
            }

            // fPos[x], lPos[x] = first and last positions of x in the window,
            // or 0 if x is not present.
            int[] fPos = new int[n+1], lPos = new int[n+1];

            // A map from interval‐start → interval‐end, for all x in window.
            // Holds all [fPos[x]..lPos[x]].
            TreeMap<Integer,Integer> intervals = new TreeMap<>();

            // A small helper to detect a “crossing” of two intervals.
            // [f1..l1] crosses [f2..l2] iff
            //   f1 < f2 <= l1 < l2.
            BiPredicate<int[], int[]> cross = (i1, i2) -> 
                      i1[0] < i2[0] && i2[0] <= i1[1] && i1[1] < i2[1];

            int r = 0;
            // Slide ℓ from 1..n
            for (int ℓ = 1; ℓ <= n; ℓ++) {
                // Try to expand r as far as we can while keeping the window
                // [ℓ..r+1] laminar.
                while (r < n) {
                    int p = r + 1;
                    int x = a[p];

                    // old interval of x:
                    int oldCount = pos[x].size();
                    int oldF = fPos[x], oldL = lPos[x];

                    // new count, new interval if we insert p:
                    int newF = oldCount == 0 ? p : Math.min(oldF, p);
                    int newL = oldCount == 0 ? p : Math.max(oldL, p);

                    // remove the old interval of x (if any)
                    if (oldCount > 0) {
                        intervals.remove(oldF);
                    }

                    // check crossing with neighbors
                    boolean conflict = false;
                    // check next
                    Map.Entry<Integer,Integer> nxt = intervals.ceilingEntry(newF);
                    if (nxt != null) {
                        int[] Inew = {newF, newL};
                        int[] Inxt = {nxt.getKey(), nxt.getValue()};
                        if (cross.test(Inew, Inxt)) {
                            conflict = true;
                        }
                    }
                    // check previous
                    if (!conflict) {
                        Map.Entry<Integer,Integer> prv = intervals.lowerEntry(newF);
                        if (prv != null) {
                            int[] Inew = {newF, newL};
                            int[] Iprv = {prv.getKey(), prv.getValue()};
                            if (cross.test(Iprv, Inew)) {
                                conflict = true;
                            }
                        }
                    }

                    if (conflict) {
                        // roll back removing the old interval
                        if (oldCount > 0) {
                            intervals.put(oldF, oldL);
                        }
                        break;
                    }

                    // commit: insert the new interval
                    intervals.put(newF, newL);
                    pos[x].add(p);
                    fPos[x] = newF;
                    lPos[x] = newL;
                    r++;
                }

                // at this point [ℓ..r] is laminar but [ℓ..(r+1)] is not (or r=n).
                badR[ℓ] = r + 1;

                // now remove ℓ from the window to move ℓ→ℓ+1
                {
                    int x = a[ℓ];
                    int oldCount = pos[x].size();
                    int oldF = fPos[x], oldL = lPos[x];

                    // remove old interval
                    if (oldCount > 0) {
                        intervals.remove(oldF);
                    }
                    // remove the position ℓ
                    pos[x].remove(ℓ);

                    // recompute the new interval of x
                    int newCount = oldCount - 1;
                    if (newCount == 0) {
                        fPos[x] = lPos[x] = 0;
                    } else {
                        int nf = pos[x].first();
                        int nl = pos[x].last();
                        fPos[x] = nf;
                        lPos[x] = nl;
                        intervals.put(nf, nl);
                    }
                }
            }

            // answer queries in O(1) each
            for (int i = 0; i < q; i++) {
                tok = new StringTokenizer(in.readLine());
                int L = Integer.parseInt(tok.nextToken());
                int R = Integer.parseInt(tok.nextToken());
                // good iff R < badR[L]
                out.write((R < badR[L]) ? "YES\n" : "NO\n");
            }
        }

        out.flush();
    }
}