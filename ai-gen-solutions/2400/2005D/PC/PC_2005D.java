import java.io.*;
import java.util.*;

public class Main {
    static final int MAXN = 500_000 + 5;
    static int[] a = new int[MAXN], b = new int[MAXN];
    static int[] pa = new int[MAXN], pb = new int[MAXN];
    static int[] sa = new int[MAXN], sb = new int[MAXN];
    
    // Temporary arrays for the "running gcd segments" of a and b
    static int[] aGL = new int[64], aG  = new int[64],  // aGL = left endpoints, aG = gcd-values
                 newGL = new int[64], newG = new int[64];
    static int[] bGL = new int[64], bG  = new int[64],
                 newBL = new int[64], newBG = new int[64];

    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader();
        PrintWriter out = new PrintWriter(System.out);

        int T = in.nextInt();
        while (T-- > 0) {
            int n = in.nextInt();
            for (int i = 1; i <= n; i++) {
                a[i] = in.nextInt();
            }
            for (int i = 1; i <= n; i++) {
                b[i] = in.nextInt();
            }
            // Build prefix gcds
            pa[0] = 0; pb[0] = 0;
            for (int i = 1; i <= n; i++) {
                pa[i] = gcd(pa[i - 1], a[i]);
                pb[i] = gcd(pb[i - 1], b[i]);
            }
            // Build suffix gcds
            sa[n + 1] = 0; sb[n + 1] = 0;
            for (int i = n; i >= 1; i--) {
                sa[i] = gcd(sa[i + 1], a[i]);
                sb[i] = gcd(sb[i + 1], b[i]);
            }

            // Collect the break‐points of pa and pb
            // i is a "break" if pa[i] != pa[i-1],  it affects l = i+1
            List<Integer> lpA = new ArrayList<>(), lpB = new ArrayList<>();
            for (int i = 1; i < n; i++) {
                if (pa[i] != pa[i - 1]) lpA.add(i + 1);
                if (pb[i] != pb[i - 1]) lpB.add(i + 1);
            }
            // Now we sweep r from 1..n
            long bestSum = 0, ways = 0;
            int sizeA = 0, sizeB = 0; // sizes of the running-lists for 'a' and 'b'
            int ptrA = 0, ptrB = 0;   // pointers into lpA, lpB for how many are <= r

            for (int r = 1; r <= n; r++) {
                // 1) update the running‐gcd segments for a ending at r
                int newA = 0;
                for (int i = 0; i < sizeA; i++) {
                    int g = gcd(aG[i], a[r]);
                    if (newA == 0 || g != newG[newA - 1]) {
                        newGL[newA] = aGL[i];
                        newG[newA++] = g;
                    }
                }
                if (newA == 0 || newG[newA - 1] != a[r]) {
                    newGL[newA] = r;
                    newG[newA++] = a[r];
                }
                // swap new into old
                sizeA = newA;
                for (int i = 0; i < sizeA; i++) {
                    aGL[i] = newGL[i];
                    aG[i]  = newG[i];
                }

                // 2) same for b
                int newB = 0;
                for (int i = 0; i < sizeB; i++) {
                    int g = gcd(bG[i], b[r]);
                    if (newB == 0 || g != newBG[newB - 1]) {
                        newBL[newB] = bGL[i];
                        newBG[newB++] = g;
                    }
                }
                if (newB == 0 || newBG[newB - 1] != b[r]) {
                    newBL[newB] = r;
                    newBG[newB++] = b[r];
                }
                sizeB = newB;
                for (int i = 0; i < sizeB; i++) {
                    bGL[i] = newBL[i];
                    bG[i]  = newBG[i];
                }

                // 3) advance the lpA/lpB pointers to include all breaks ≤ r
                while (ptrA < lpA.size() && lpA.get(ptrA) <= r) ptrA++;
                while (ptrB < lpB.size() && lpB.get(ptrB) <= r) ptrB++;

                // 4) merge the 4 sorted lists of break‐points:
                //    { aGL[0..sizeA-1], bGL[0..sizeB-1], lpA[0..ptrA-1], lpB[0..ptrB-1] }
                //    plus we will append (r+1) at end.
                int[] Ls = new int[sizeA + sizeB + ptrA + ptrB + 1];
                int ia=0, ib=0, iA=0, iB=0, plen=0;
                int last = -1;
                while (ia < sizeA || ib < sizeB || iA < ptrA || iB < ptrB) {
                    int m = Integer.MAX_VALUE;
                    if (ia < sizeA) m = Math.min(m, aGL[ia]);
                    if (ib < sizeB) m = Math.min(m, bGL[ib]);
                    if (iA < ptrA)  m = Math.min(m, lpA.get(iA));
                    if (iB < ptrB)  m = Math.min(m, lpB.get(iB));
                    if (m != last) Ls[plen++] = m;
                    last = m;
                    if (ia < sizeA && aGL[ia] == m) ia++;
                    if (ib < sizeB && bGL[ib] == m) ib++;
                    if (iA < ptrA  && lpA.get(iA) == m) iA++;
                    if (iB < ptrB  && lpB.get(iB) == m) iB++;
                }
                // finally append r+1
                if (r+1 != last) Ls[plen++] = r+1;

                // 5) now walk those breaks in ascending order: between Ls[k]..Ls[k+1]-1,
                //    all four gcd‐subvalues are constant.  We just compute once per interval.
                int idxApt = 0, idxBpt = 0;  // to find which block of aGL/bGL covers L
                for (int k = 0; k+1 < plen; k++) {
                    int L = Ls[k], R = Ls[k+1] - 1;
                    int len = R - L + 1;
                    if (len <= 0) continue;
                    // advance idxApt until next block would overshoot
                    while (idxApt+1 < sizeA && aGL[idxApt+1] <= L) idxApt++;
                    while (idxBpt+1 < sizeB && bGL[idxBpt+1] <= L) idxBpt++;
                    int gaSeg = aG[idxApt], gbSeg = bG[idxBpt];

                    // out‐of‐segment gcds
                    int outA = gcd(pa[L-1], sa[r+1]);
                    int outB = gcd(pb[L-1], sb[r+1]);
                    // final swapped‐in gcd
                    int G1 = gcd(outA, gbSeg);
                    int G2 = gcd(outB, gaSeg);
                    int sum = G1 + G2;

                    if (sum > bestSum) {
                        bestSum = sum;
                        ways = len;
                    } else if (sum == bestSum) {
                        ways += len;
                    }
                }
            }

            out.println(bestSum + " " + ways);
        }
        out.flush();
    }

    // fast iterative gcd
    static int gcd(int x, int y) {
        while (y != 0) {
            int t = x % y;
            x = y;
            y = t;
        }
        return x;
    }

    // fast reader
    static class FastReader {
        final int BS = 1<<20;
        byte[] buf = new byte[BS];
        int bi = BS, bn = BS;
        InputStream in = System.in;
        int read() throws IOException {
            if(bi>=bn){
                bi=0; bn=in.read(buf);
                if(bn<0) return -1;
            }
            return buf[bi++];
        }
        int nextInt() throws IOException {
            int c, x = 0;
            do { c = read(); if(c<0) return -1; } while(c!='-' && (c<'0'||c>'9'));
            boolean neg = (c=='-');
            if(neg) c=read();
            for(; c>='0'&&c<='9'; c=read()) x = x*10 + (c-'0');
            return neg ? -x : x;
        }
    }
}