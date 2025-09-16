import java.io.*;
import java.util.*;

public class Main {
    static class FastInput {
        BufferedReader br;
        StringTokenizer st;
        FastInput(InputStream in) {
            br = new BufferedReader(new InputStreamReader(in));
        }
        String nextToken() throws IOException {
            while (st == null || !st.hasMoreTokens()) {
                String line = br.readLine();
                if (line == null) return null;
                st = new StringTokenizer(line);
            }
            return st.nextToken();
        }
        int nextInt() throws IOException {
            return Integer.parseInt(nextToken());
        }
    }

    // compute gcd in O(log(min(a,b)))
    static int gcd(int a, int b) {
        while (b != 0) {
            int t = a % b;
            a = b;
            b = t;
        }
        return a;
    }

    public static void main(String[] args) throws IOException {
        FastInput in = new FastInput(System.in);
        StringBuilder out = new StringBuilder();

        int t = in.nextInt();
        while (t-- > 0) {
            int n = in.nextInt();
            int[] a = new int[n+1], b = new int[n+1];
            for (int i = 1; i <= n; i++) a[i] = in.nextInt();
            for (int i = 1; i <= n; i++) b[i] = in.nextInt();

            // prefix-gcd arrays P_a, P_b
            int[] Pa = new int[n+1], Pb = new int[n+1];
            Pa[0] = 0; Pb[0] = 0;
            for (int i = 1; i <= n; i++) {
                Pa[i] = gcd(Pa[i-1], a[i]);
                Pb[i] = gcd(Pb[i-1], b[i]);
            }
            // suffix-gcd arrays S_a, S_b, extended to n+1
            int[] Sa = new int[n+2], Sb = new int[n+2];
            Sa[n+1] = 0; Sb[n+1] = 0;
            for (int i = n; i >= 1; i--) {
                Sa[i] = gcd(Sa[i+1], a[i]);
                Sb[i] = gcd(Sb[i+1], b[i]);
            }
            // for each i, endSa[i] = last index >= i where Sa[] is constant
            int[] endSa = new int[n+2], endSb = new int[n+2];
            endSa[n+1] = n+1; endSb[n+1] = n+1;
            for (int i = n; i >= 1; i--) {
                if (Sa[i] == Sa[i+1]) endSa[i] = endSa[i+1];
                else              endSa[i] = i;
                if (Sb[i] == Sb[i+1]) endSb[i] = endSb[i+1];
                else              endSb[i] = i;
            }

            // Reverse arrays into a', b'
            int[] ar = new int[n+1], br = new int[n+1];
            for (int i = 1; i <= n; i++) {
                ar[i] = a[n+1 - i];
                br[i] = b[n+1 - i];
            }

            // We'll build two small "prev" lists storing (gcd, startPos) for suffixes ending at i
            final int MAXK = 64;  // more than enough, actual size ~ O(log max(a_i))
            int[] paG = new int[MAXK], paP = new int[MAXK];
            int[] pbG = new int[MAXK], pbP = new int[MAXK];
            int szA = 0, szB = 0; // sizes of those lists

            long bestSum = 0, ways = 0;

            // We'll also need some scratch space to build the merged gcd‐segments:
            int[] tmpG = new int[MAXK], tmpP = new int[MAXK];
            int[] curG = new int[MAXK], curP = new int[MAXK];

            // For each i in reversed, that corresponds to l = n+1-i in the original.
            for (int i = 1; i <= n; i++) {
                int l = n + 1 - i;

                // 1) Update the "prev" list for ar[] to reflect gcds of subarrays ending at i
                //    Build tmp = {gcd(ar[i], oldG), oldP} plus (ar[i], i)
                int tSz = 0;
                for (int k = 0; k < szA; k++) {
                    int g = gcd(paG[k], ar[i]);
                    tmpG[tSz] = g;
                    tmpP[tSz] = paP[k];
                    tSz++;
                }
                tmpG[tSz] = ar[i];
                tmpP[tSz] = i;
                tSz++;

                // Deduplicate by gcd‐value, keep minimal startPos
                int cSz = 0;
                for (int k = 0; k < tSz; k++) {
                    int g = tmpG[k], p = tmpP[k];
                    int found = -1;
                    for (int x = 0; x < cSz; x++) {
                        if (curG[x] == g) {
                            found = x;
                            break;
                        }
                    }
                    if (found >= 0) {
                        if (p < curP[found]) curP[found] = p;
                    } else {
                        curG[cSz] = g;
                        curP[cSz] = p;
                        cSz++;
                    }
                }
                // Sort cur[] by curP ascending
                for (int x = 0; x < cSz; x++) {
                    for (int y = x+1; y < cSz; y++) {
                        if (curP[y] < curP[x]) {
                            int tg = curG[x], tp = curP[x];
                            curG[x] = curG[y]; curP[x] = curP[y];
                            curG[y] = tg;    curP[y] = tp;
                        }
                    }
                }
                // swap cur -> paG,paP
                szA = cSz;
                for (int k = 0; k < cSz; k++) {
                    paG[k] = curG[k];
                    paP[k] = curP[k];
                }

                // 2) Same for br[]
                tSz = 0;
                for (int k = 0; k < szB; k++) {
                    int g = gcd(pbG[k], br[i]);
                    tmpG[tSz] = g;
                    tmpP[tSz] = pbP[k];
                    tSz++;
                }
                tmpG[tSz] = br[i];
                tmpP[tSz] = i;
                tSz++;

                cSz = 0;
                for (int k = 0; k < tSz; k++) {
                    int g = tmpG[k], p = tmpP[k];
                    int found = -1;
                    for (int x = 0; x < cSz; x++) {
                        if (curG[x] == g) {
                            found = x;
                            break;
                        }
                    }
                    if (found >= 0) {
                        if (p < curP[found]) curP[found] = p;
                    } else {
                        curG[cSz] = g;
                        curP[cSz] = p;
                        cSz++;
                    }
                }
                for (int x = 0; x < cSz; x++) {
                    for (int y = x+1; y < cSz; y++) {
                        if (curP[y] < curP[x]) {
                            int tg = curG[x], tp = curP[x];
                            curG[x] = curG[y]; curP[x] = curP[y];
                            curG[y] = tg;    curP[y] = tp;
                        }
                    }
                }
                szB = cSz;
                for (int k = 0; k < cSz; k++) {
                    pbG[k] = curG[k];
                    pbP[k] = curP[k];
                }

                // Now paG/paP (size szA) encode all distinct gcd(a_l..a_r) blocks for r>=l
                // and pbG/pbP do the same for gcd(b_l..b_r).

                // Build listA = intervals on r where gcd(a_l..a_r) = fixed
                // Note: paP[] holds reversed-start positions; we translate to r-interval:
                int[] Armin = new int[szA], Armax = new int[szA], Agval = new int[szA];
                {
                    int idx = 0;
                    // we must walk k=szA-1 down to 0, so r-min is ascending
                    for (int k = szA-1; k >= 0; k--) {
                        int p = paP[k];
                        int rmax = (n+1) - p;
                        int rmin;
                        if (k == szA - 1) {
                            rmin = l;
                        } else {
                            rmin = (n+2) - paP[k+1];
                        }
                        Armin[idx] = rmin;
                        Armax[idx] = rmax;
                        Agval[idx] = paG[k];
                        idx++;
                    }
                    // idx == szA
                }

                // Build listB similarly
                int[] Brmin = new int[szB], Brmax = new int[szB], Bgval = new int[szB];
                {
                    int idx = 0;
                    for (int k = szB-1; k >= 0; k--) {
                        int p = pbP[k];
                        int rmax = (n+1) - p;
                        int rmin;
                        if (k == szB - 1) {
                            rmin = l;
                        } else {
                            rmin = (n+2) - pbP[k+1];
                        }
                        Brmin[idx] = rmin;
                        Brmax[idx] = rmax;
                        Bgval[idx] = pbG[k];
                        idx++;
                    }
                }

                // Build listC = intervals for G_A^out = gcd(Pa[l-1], Sa[r+1])
                ArrayList<int[]> listC = new ArrayList<>();
                int ptr = l+1;
                while (ptr <= n+1) {
                    int e = endSa[ptr];
                    int rmin = ptr - 1;
                    int rmax = e - 1;
                    int go = gcd(Pa[l-1], Sa[ptr]);
                    listC.add(new int[]{rmin, rmax, go});
                    ptr = e + 1;
                }

                // Build listD = intervals for G_B^out = gcd(Pb[l-1], Sb[r+1])
                ArrayList<int[]> listD = new ArrayList<>();
                ptr = l+1;
                while (ptr <= n+1) {
                    int e = endSb[ptr];
                    int rmin = ptr - 1;
                    int rmax = e - 1;
                    int go = gcd(Pb[l-1], Sb[ptr]);
                    listD.add(new int[]{rmin, rmax, go});
                    ptr = e + 1;
                }

                // Now we have four interval‐lists over r in [l..n]:
                //   (Armin,Armax,Agval),
                //   (Brmin,Brmax,Bgval),
                //   listC of {rmin,rmax, outA},
                //   listD of {rmin,rmax, outB}.
                // Each list partitions [l..n] in ascending order of rmin.
                // We do a 4‐way “two‐pointer” merge, jumping from block to block.

                int iA = 0, iB = 0, iC2 = 0, iD2 = 0;
                int r0 = l;
                while (r0 <= n) {
                    // find which block covers r0 in each list
                    // the lists are guaranteed to cover continuously
                    // so we just use the current pointers.

                    int end = Integer.MAX_VALUE;
                    end = Math.min(end, Armax[iA]);
                    end = Math.min(end, Brmax[iB]);
                    end = Math.min(end, listC.get(iC2)[1]);
                    end = Math.min(end, listD.get(iD2)[1]);

                    // compute the sum for [r0..end]
                    int inA = listB.get(iB).length==3 ? listB.get(iB)[2] /* gcd(b_l..b_r) */ : Bgval[iB];
                    // Actually listB is Bgval: gcd(b_l..b_r).
                    int gcdA = gcd(Agval[iA], listC.get(iC2)[2]);  // outA vs inA
                    int gcdB = gcd(listD.get(iD2)[2], Bgval[iB]);
                    long s = (long)gcdA + gcdB;

                    if (s > bestSum) {
                        bestSum = s;
                        ways = end - r0 + 1L;
                    } else if (s == bestSum) {
                        ways += (end - r0 + 1L);
                    }

                    // jump
                    r0 = end + 1;
                    // advance any pointer whose block ended at 'end'
                    if (Armax[iA] == end) iA++;
                    if (Brmax[iB] == end) iB++;
                    if (listC.get(iC2)[1] == end) iC2++;
                    if (listD.get(iD2)[1] == end) iD2++;
                }
            }

            out.append(bestSum).append(" ").append(ways).append("\n");
        }

        System.out.print(out);
    }
}