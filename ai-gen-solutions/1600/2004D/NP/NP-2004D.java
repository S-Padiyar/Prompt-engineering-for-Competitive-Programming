import java.io.*;
import java.util.*;

public class Main {
    static final int INF = 1_000_000_000;
    // Map 'B','G','R','Y' -> 0,1,2,3
    static int colorIndex(char c) {
        switch(c) {
            case 'B': return 0;
            case 'G': return 1;
            case 'R': return 2;
            default:  return 3; // 'Y'
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int q = Integer.parseInt(st.nextToken());

            // Read city portal‐pairs and build a bitmask of colors per city
            int[] mask = new int[n+1];
            st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= n; i++) {
                String s = st.nextToken();
                int c1 = colorIndex(s.charAt(0));
                int c2 = colorIndex(s.charAt(1));
                mask[i] = (1<<c1) | (1<<c2);
            }

            // Build segment‐tree size = next power of two >= n
            int size = 1;
            while (size < n) size <<= 1;
            int segSize = size << 1;

            // larr/rarr store the [L,R] interval of each node
            int[] larr = new int[segSize];
            int[] rarr = new int[segSize];
            // ds[node][a][b] = min cost from color a at left boundary to color b at right boundary
            int[][][] ds = new int[segSize][4][4];

            // Initialize leaves
            for (int i = size; i < size + n; i++) {
                int city = i - size + 1;
                larr[i] = rarr[i] = city;
                // fill INF
                for (int a = 0; a < 4; a++)
                    Arrays.fill(ds[i][a], INF);
                // if city has color a, staying in a costs 0
                for (int a = 0; a < 4; a++) {
                    if ((mask[city] & (1<<a)) != 0) {
                        ds[i][a][a] = 0;
                    }
                }
                // the two portal‐colors at city can switch at cost 0
                int twoColors = mask[city];
                int c0 = Integer.numberOfTrailingZeros(twoColors);
                int c1 = 31 - Integer.numberOfLeadingZeros(twoColors);
                if (c0 != c1) {
                    ds[i][c0][c1] = 0;
                    ds[i][c1][c0] = 0;
                }
            }
            // Mark empty leaves (i > n) with invalid interval
            for (int i = size + n; i < segSize; i++) {
                larr[i] = 1;
                rarr[i] = 0; // invalid means l>r
            }

            // Build up the internal nodes
            for (int i = size - 1; i >= 1; i--) {
                mergeNode(i<<1, (i<<1)|1, i, mask, larr, rarr, ds);
            }

            // Process queries
            while (q-- > 0) {
                st = new StringTokenizer(br.readLine());
                int x = Integer.parseInt(st.nextToken());
                int y = Integer.parseInt(st.nextToken());
                if (x > y) {
                    int tmp = x; x = y; y = tmp;
                }
                // We will do the standard iterative segment‐tree range query on [x-1, y)
                // but we must merge nodes left‐to‐right using the same merge logic
                int li = x-1 + size;
                int ri = y   + size;

                // curL represents the merged segment so far from the left
                boolean haveL = false;
                int Ll=0, Lr=0;
                int[][] dL = new int[4][4];  // will point into ds when haveL==false, or store merges

                // curR for the right side (we will later merge L and R)
                boolean haveR = false;
                int Rl=0, Rr=0;
                int[][] dR = new int[4][4];

                // temp array for merges
                int[][] tmp = new int[4][4];

                while (li < ri) {
                    if ((li & 1) == 1) {
                        // merge curL with node li
                        if (!haveL) {
                            // first node for L
                            haveL = true;
                            Ll = larr[li];
                            Lr = rarr[li];
                            dL = ds[li];
                        } else {
                            mergeAny(Ll, Lr, dL, 
                                     larr[li], rarr[li], ds[li],
                                     mask, tmp);
                            Lr = rarr[li];
                            // swap tmp -> dL
                            int[][] swap = dL; dL = tmp; tmp = swap;
                        }
                        li++;
                    }
                    if ((ri & 1) == 1) {
                        ri--;
                        // merge node ri with curR
                        if (!haveR) {
                            haveR = true;
                            Rl = larr[ri];
                            Rr = rarr[ri];
                            dR = ds[ri];
                        } else {
                            mergeAny(larr[ri], rarr[ri], ds[ri],
                                     Rl, Rr, dR,
                                     mask, tmp);
                            Rl = larr[ri];
                            int[][] swap = dR; dR = tmp; tmp = swap;
                        }
                    }
                    li >>= 1;
                    ri >>= 1;
                }

                // now final merge L & R
                int[][] dAll;
                if (!haveL) {
                    if (!haveR) {
                        // empty
                        dAll = new int[4][4];
                        for (int a = 0; a < 4; a++)
                            Arrays.fill(dAll[a], INF);
                    } else {
                        dAll = dR;
                    }
                } else if (!haveR) {
                    dAll = dL;
                } else {
                    mergeAny(Ll, Lr, dL,
                             Rl, Rr, dR,
                             mask, tmp);
                    dAll = tmp;
                }

                // Finally take min over the two colors at x and the two at y
                int ans = INF;
                int mx = mask[x];
                int my = mask[y];
                for (int cx = 0; cx < 4; cx++) if ((mx & (1<<cx))!=0) {
                    for (int cy = 0; cy < 4; cy++) if ((my & (1<<cy))!=0) {
                        ans = Math.min(ans, dAll[cx][cy]);
                    }
                }
                if (ans >= INF) ans = -1;
                sb.append(ans).append('\n');
            }
        }

        System.out.print(sb.toString());
    }

    /** 
     * Merge two built segment‐tree children A=2*i,B=2*i+1 into parent idx.
     */
    static void mergeNode(int aIdx, int bIdx, int pIdx,
                          int[] mask, int[] L, int[] R,
                          int[][][] ds) {
        int l1 = L[aIdx], r1 = R[aIdx];
        int l2 = L[bIdx], r2 = R[bIdx];
        L[pIdx] = l1;
        R[pIdx] = r2;
        int[][] d1 = ds[aIdx], d2 = ds[bIdx], d3 = ds[pIdx];

        // clear d3
        for (int i = 0; i < 4; i++) Arrays.fill(d3[i], INF);

        if (l1 > r1) {
            // left empty => copy right
            for (int i = 0; i < 4; i++)
                System.arraycopy(d2[i], 0, d3[i], 0, 4);
            return;
        }
        if (l2 > r2) {
            // right empty => copy left
            for (int i = 0; i < 4; i++)
                System.arraycopy(d1[i], 0, d3[i], 0, 4);
            return;
        }

        // boundary between r1 and l2
        int mInter = mask[r1] & mask[l2];
        if (mInter == 0) {
            // no color to cross => all INF
            return;
        }
        // for each color c in the intersection
        for (int c = 0; c < 4; c++) {
            if (((mInter >> c) & 1) != 0) {
                // cross cost = 1 in color c
                for (int a = 0; a < 4; a++) {
                    int da = d1[a][c];
                    if (da >= INF) continue;
                    da += 1;
                    for (int b = 0; b < 4; b++) {
                        int val = da + d2[c][b];
                        if (val < d3[a][b]) d3[a][b] = val;
                    }
                }
            }
        }
    }

    /**
     * Merge any two segments described by (l1..r1,d1) and (l2..r2,d2)
     * into the result array 'res' (4x4).
     */
    static void mergeAny(int l1, int r1, int[][] d1,
                         int l2, int r2, int[][] d2,
                         int[] mask, int[][] res) {
        // initialize INF
        for (int i = 0; i < 4; i++) Arrays.fill(res[i], INF);

        if (l1 > r1) {
            // just copy d2
            for (int i = 0; i < 4; i++)
                System.arraycopy(d2[i], 0, res[i], 0, 4);
            return;
        }
        if (l2 > r2) {
            // just copy d1
            for (int i = 0; i < 4; i++)
                System.arraycopy(d1[i], 0, res[i], 0, 4);
            return;
        }

        int mInter = mask[r1] & mask[l2];
        if (mInter == 0) {
            // no boundary crossing possible => stays INF
            return;
        }
        // for each crossing‐color c
        for (int c = 0; c < 4; c++) {
            if (((mInter >> c) & 1) != 0) {
                // cost to cross = 1
                for (int a = 0; a < 4; a++) {
                    int da = d1[a][c];
                    if (da >= INF) continue;
                    da += 1;
                    for (int b = 0; b < 4; b++) {
                        int val = da + d2[c][b];
                        if (val < res[a][b]) res[a][b] = val;
                    }
                }
            }
        }
    }
}