import java.io.*;

public class Main {
    static final int MAXVAL = 1_000_000_000;

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder out = new StringBuilder();

        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            int n = Integer.parseInt(in.readLine().trim());
            String[] sa = in.readLine().split(" ");
            int[] a = new int[n+1]; // 1-based; -1 means unknown
            for (int i = 1; i <= n; i++) {
                a[i] = Integer.parseInt(sa[i-1]);
            }

            // collect positions of known values
            java.util.List<Integer> known = new java.util.ArrayList<>();
            for (int i = 1; i <= n; i++) {
                if (a[i] != -1) known.add(i);
            }

            int[] b = new int[n+1]; // our answer
            boolean ok = true;

            // if no known entries, just do 1,2,1,2,... this always works
            if (known.isEmpty()) {
                for (int i = 1; i <= n; i++) {
                    b[i] = (i % 2 == 1 ? 1 : 2);
                }
            } else {
                // copy known entries
                for (int p : known) {
                    b[p] = a[p];
                }

                // 1) segment before the first known: only-right-known
                int first = known.get(0);
                if (first > 1) {
                    if (!fillRightOnly(b, first)) {
                        ok = false;
                    }
                }

                // 2) segments between known[i] and known[i+1]
                for (int i = 0; i + 1 < known.size() && ok; i++) {
                    int lpos = known.get(i);
                    int rpos = known.get(i+1);
                    if (rpos == lpos + 1) continue; // no gap
                    if (!fillBothKnown(b, lpos, rpos)) {
                        ok = false;
                        break;
                    }
                }

                // 3) segment after the last known: only-left-known
                int last = known.get(known.size()-1);
                if (last < n && ok) {
                    if (!fillLeftOnly(b, last, n)) {
                        ok = false;
                    }
                }
            }

            if (!ok) {
                out.append("-1\n");
            } else {
                for (int i = 1; i <= n; i++) {
                    out.append(b[i]).append(i == n ? "\n" : " ");
                }
            }
        }

        System.out.print(out);
    }

    // Fill backwards from b[r] to b[1..r-1].
    // rBound = r, gap length = r-1
    // We simply do the pattern: 2*x, x, 2*x, x, ... going leftwards.
    static boolean fillRightOnly(int[] b, int rBound) {
        int curr = b[rBound];
        int len = rBound - 1;
        for (int t = 1; t <= len; t++) {
            int nxt;
            if ((t & 1) == 1) {
                // append 0 side
                nxt = curr * 2;
            } else {
                // go up
                nxt = curr / 2;
            }
            if (nxt < 1 || nxt > MAXVAL) return false;
            b[rBound - t] = nxt;
            curr = nxt;
        }
        return true;
    }

    // Fill forwards from b[l] to b[l+1..n]
    // left-only known at lBound
    static boolean fillLeftOnly(int[] b, int lBound, int n) {
        int curr = b[lBound];
        int len = n - lBound;
        for (int t = 1; t <= len; t++) {
            int nxt;
            if ((t & 1) == 1) {
                // append 0
                nxt = curr * 2;
            } else {
                // go up
                nxt = curr / 2;
            }
            if (nxt < 1 || nxt > MAXVAL) return false;
            b[lBound + t] = nxt;
            curr = nxt;
        }
        return true;
    }

    // Fill the gap between two known positions lBound < rBound
    // b[lBound] and b[rBound] are known. We must make exactly
    // (rBound - lBound) steps that connect them in the tree.
    static boolean fillBothKnown(int[] b, int lBound, int rBound) {
        int S0 = b[lBound];
        int S1 = b[rBound];
        int len = rBound - lBound;

        // get binary representations
        String bs0 = Integer.toBinaryString(S0);
        String bs1 = Integer.toBinaryString(S1);
        int d0 = bs0.length();
        int d1 = bs1.length();

        // compute LCA depth = common prefix length
        int cp = 0;
        int mn = Math.min(d0, d1);
        while (cp < mn && bs0.charAt(cp) == bs1.charAt(cp)) {
            cp++;
        }
        // up moves from S0 to LCA
        int up = d0 - cp;
        // down moves from LCA to S1
        int down = d1 - cp;
        int distMin = up + down;

        // feasibility check
        if (len < distMin || ((len - distMin) & 1) != 0) {
            return false;
        }
        int extra = (len - distMin) / 2;

        // build the path of length = len+1 from S0 to S1
        java.util.List<Integer> path = new java.util.ArrayList<>(len+1);
        path.add(S0);
        int curr = S0;

        // first do 'extra' up-down loops: each loop is (append0, up)
        for (int i = 0; i < extra; i++) {
            // append 0
            curr = curr * 2;
            path.add(curr);
            // go up
            curr = curr / 2;
            path.add(curr);
        }
        // now curr == S0 again

        // do the minimal up-moves
        for (int i = 0; i < up; i++) {
            curr = curr / 2;
            path.add(curr);
        }
        // now curr is LCA

        // do the minimal down-moves by using the suffix of bs1 from cp..end
        for (int i = cp; i < d1; i++) {
            curr = curr * 2 + (bs1.charAt(i) - '0');
            path.add(curr);
        }

        // path.size() must be len+1
        if (path.size() != len+1) {
            return false;
        }
        // and the last must be S1
        if (path.get(len).intValue() != S1) {
            return false;
        }

        // write into b[lBound+1 .. rBound-1]
        for (int i = 1; i < len; i++) {
            b[lBound + i] = path.get(i);
        }
        return true;
    }
}