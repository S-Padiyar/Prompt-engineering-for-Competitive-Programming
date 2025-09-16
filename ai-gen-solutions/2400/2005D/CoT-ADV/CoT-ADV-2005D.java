import java.io.*;
import java.util.*;

public class Main {
    static final int MAXN = 200_000;
    static int[] a = new int[MAXN+5], b = new int[MAXN+5];
    static int[] preA = new int[MAXN+5], preB = new int[MAXN+5];
    static int[] sufA = new int[MAXN+5], sufB = new int[MAXN+5];
    static int n;

    // Compute gcd
    static int gcd(int x, int y) {
        if (x == 0) return y;
        if (y == 0) return x;
        return Integer.gcd(x, y);
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        int T = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();

        while (T-- > 0) {
            n = Integer.parseInt(br.readLine().trim());
            st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= n; i++) a[i] = Integer.parseInt(st.nextToken());
            st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= n; i++) b[i] = Integer.parseInt(st.nextToken());

            // 1) Build prefix-gcd arrays
            preA[0] = 0;
            preB[0] = 0;
            for (int i = 1; i <= n; i++) {
                preA[i] = gcd(preA[i-1], a[i]);
                preB[i] = gcd(preB[i-1], b[i]);
            }

            // 2) Build suffix-gcd arrays (with an extra slot at n+1 = 0)
            sufA[n+1] = 0;
            sufB[n+1] = 0;
            for (int i = n; i >= 1; i--) {
                sufA[i] = gcd(sufA[i+1], a[i]);
                sufB[i] = gcd(sufB[i+1], b[i]);
            }

            // 3) Precompute change-points of sufA and sufB
            //    We only care where sufA[i] != sufA[i+1].
            ArrayList<Integer> changeA = new ArrayList<>();
            ArrayList<Integer> changeB = new ArrayList<>();
            for (int i = 1; i <= n; i++) {
                if (sufA[i] != sufA[i+1]) changeA.add(i);
                if (sufB[i] != sufB[i+1]) changeB.add(i);
            }

            long bestSum = 0, ways = 0;

            // 4) For each l, we sweep r from l..n in O(log n) segments.
            for (int l = 1; l <= n; l++) {
                // Compressed list of (gcdVal, startR) for b[l..r]
                ArrayList<int[]> segB = new ArrayList<>();
                // Compressed list of (gcdVal, startR) for a[l..r]
                ArrayList<int[]> segA = new ArrayList<>();
                // initially empty ->  gcd(b_l..b_{l-1}) = 0
                segB.add(new int[]{0, l});
                segA.add(new int[]{0, l});

                // Indices into change-point lists for suffix
                int idxA = Collections.binarySearch(changeA, l) >= 0
                         ? Collections.binarySearch(changeA, l)
                         : -Collections.binarySearch(changeA, l)-1;
                int idxB = Collections.binarySearch(changeB, l) >= 0
                         ? Collections.binarySearch(changeB, l)
                         : -Collections.binarySearch(changeB, l)-1;

                int curR = l;
                while (curR <= n) {
                    // 1) Extend gcd-lists by adding position curR
                    int valB = b[curR], valA = a[curR];
                    // Update segB
                    ArrayList<int[]> nextB = new ArrayList<>();
                    nextB.add(new int[]{valB, curR}); 
                    for (int[] p : segB) {
                        int ng = gcd(p[0], valB);
                        if (nextB.get(nextB.size()-1)[0] != ng) {
                            nextB.add(new int[]{ng, p[1]});
                        }
                    }
                    segB = nextB;
                    // Update segA
                    ArrayList<int[]> nextA = new ArrayList<>();
                    nextA.add(new int[]{valA, curR});
                    for (int[] p : segA) {
                        int ng = gcd(p[0], valA);
                        if (nextA.get(nextA.size()-1)[0] != ng) {
                            nextA.add(new int[]{ng, p[1]});
                        }
                    }
                    segA = nextA;

                    // 2) Build the list of breakpoints from:
                    //    - segB starts,
                    //    - segA starts,
                    //    - next suffix-change in A,
                    //    - next suffix-change in B.
                    int INF = n+1;
                    TreeSet<Integer> cuts = new TreeSet<>();
                    // from segB
                    for (int[] p : segB) cuts.add(p[1]);
                    // from segA
                    for (int[] p : segA) cuts.add(p[1]);
                    // suffix A-change
                    if (idxA < changeA.size()) cuts.add(changeA.get(idxA));
                    // suffix B-change
                    if (idxB < changeB.size()) cuts.add(changeB.get(idxB));

                    // We look for the smallest cut >= curR
                    Integer start = cuts.ceiling(curR);
                    if (start == null) break;
                    // then also look for next cut > start to bound the interval
                    Integer next = cuts.higher(start);
                    int intervalEnd = (next == null ? n : next - 1);

                    // But all of r in [start..intervalEnd] share
                    //  gcd(b_l..b_r)= some Y
                    //  gcd(a_l..a_r)= some V
                    //  gcd( preA[l-1], sufA[r+1] ) = pA
                    //  gcd( preB[l-1], sufB[r+1] ) = pB
                    // So the swapped-GCDs:
                    //   G1 = gcd(pA, Y),   G2 = gcd(pB, V)
                    //
                    // We extract Y,V by looking up segB, segA, and
                    // pA,pB by index in changeA/changeB.

                    // find Y
                    int Y = 0;
                    for (int[] p : segB) {
                        if (p[1] <= start) Y = p[0];
                        else break;
                    }
                    // find V
                    int V = 0;
                    for (int[] p : segA) {
                        if (p[1] <= start) V = p[0];
                        else break;
                    }
                    // find pA = gcd(preA[l-1], sufA[start+1])
                    int borderApos = start+1;
                    int pA = gcd(preA[l-1], borderApos<=n ? sufA[borderApos] : 0);
                    // find pB
                    int borderBpos = start+1;
                    int pB = gcd(preB[l-1], borderBpos<=n ? sufB[borderBpos] : 0);

                    int G1 = gcd(pA, Y);
                    int G2 = gcd(pB, V);
                    long sum = (long)G1 + G2;
                    long len = intervalEnd - start + 1;
                    if (sum > bestSum) {
                        bestSum = sum;
                        ways = len;
                    } else if (sum == bestSum) {
                        ways += len;
                    }

                    // Advance curR beyond this interval
                    curR = intervalEnd + 1;

                    // Advance suffix-change indices if we passed them
                    while (idxA < changeA.size() && changeA.get(idxA) < curR) idxA++;
                    while (idxB < changeB.size() && changeB.get(idxB) < curR) idxB++;
                }
            }

            sb.append(bestSum).append(" ").append(ways).append("\n");
        }

        System.out.print(sb);
    }
}