import java.io.*;
import java.util.*;

public class Main {
    static final int INF = 1_000_000_000;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        // Map unordered color-pairs to IDs 0..5
        int[][] pairID = new int[4][4];
        for (int i = 0; i < 4; i++)
            Arrays.fill(pairID[i], -1);
        int id = 0;
        int[][] pairs = {{0,1},{0,2},{0,3},{1,2},{1,3},{2,3}};
        for (int i = 0; i < 6; i++) {
            int c1 = pairs[i][0], c2 = pairs[i][1];
            pairID[c1][c2] = pairID[c2][c1] = i;
        }

        // Read number of testcases
        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int q = Integer.parseInt(st.nextToken());

            // For each of the 6 color-pairs, store a sorted list of city-indices
            List<Integer>[] pos = new ArrayList[6];
            for (int i = 0; i < 6; i++) {
                pos[i] = new ArrayList<>();
            }

            // Record each city's two colors
            int[] c1 = new int[n+1], c2 = new int[n+1];

            // Read the n portal-strings
            st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= n; i++) {
                String s = st.nextToken();
                int colA = charToColor(s.charAt(0));
                int colB = charToColor(s.charAt(1));
                c1[i] = colA;
                c2[i] = colB;
                int pid = pairID[colA][colB];
                pos[pid].add(i);
            }

            // The lists are filled in increasing order of i, so they are already sorted.

            // Process queries
            while (q-- > 0) {
                st = new StringTokenizer(br.readLine());
                int x = Integer.parseInt(st.nextToken());
                int y = Integer.parseInt(st.nextToken());

                // Gather colors of x and y
                int ax = c1[x], bx = c2[x];
                int ay = c1[y], by = c2[y];

                int best = INF;

                // 1) Direct move if they share a color
                if (ax == ay || ax == by || bx == ay || bx == by) {
                    best = Math.abs(x - y);
                }

                // 2) Two-step via a city that bridges one color of x to one color of y
                // We will normalize so x <= y
                if (x > y) {
                    int tmp = x; x = y; y = tmp;
                    // swap the color pairs of x,y too
                    tmp = ax; ax = ay; ay = tmp;
                    tmp = bx; bx = by; by = tmp;
                }

                // Try all 4 cross-pairs (c in {ax,bx}, d in {ay,by}) with c!=d
                int[] xcols = {ax, bx};
                int[] ycols = {ay, by};

                for (int cx : xcols) {
                    for (int cy : ycols) {
                        if (cx == cy) continue;  // would be direct, already handled
                        int pid = pairID[cx][cy];
                        List<Integer> list = pos[pid];
                        if (list.isEmpty()) continue;

                        // lowerBound for x
                        int idx = lowerBound(list, x);
                        // 2a) If there's an element in [x, y], cost = y-x
                        if (idx < list.size() && list.get(idx) <= y) {
                            best = Math.min(best, y - x);
                        } else {
                            // 2b) Predecessor <= x
                            if (idx > 0) {
                                int k = list.get(idx - 1);
                                int cost = (x - k) + (y - k); // = x+y -2k
                                best = Math.min(best, cost);
                            }
                            // 2c) Successor >= y (if idx < size, and list.get(idx)>y or idx==size)
                            if (idx < list.size()) {
                                int k = list.get(idx);
                                int cost = (k - x) + (k - y); // = 2k - x - y
                                best = Math.min(best, cost);
                            }
                        }
                    }
                }

                out.println((best == INF) ? -1 : best);
            }
        }

        out.flush();
        out.close();
    }

    // Map portal letter to integer 0..3
    static int charToColor(char c) {
        switch (c) {
            case 'B': return 0;
            case 'G': return 1;
            case 'R': return 2;
            case 'Y': return 3;
        }
        return -1;
    }

    // Classic lower_bound in an ArrayList<Integer>
    static int lowerBound(List<Integer> list, int key) {
        int lo = 0, hi = list.size();
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (list.get(mid) < key) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo;
    }
}