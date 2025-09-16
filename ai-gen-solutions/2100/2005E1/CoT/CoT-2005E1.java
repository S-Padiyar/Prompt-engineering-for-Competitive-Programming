import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int l = Integer.parseInt(st.nextToken());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());

            // read the array a[0..l-1]
            int[] a = new int[l];
            st = new StringTokenizer(in.readLine());
            for (int i = 0; i < l; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            // collect positions of each value 1..7
            ArrayList<Integer>[] posR = new ArrayList[8];
            ArrayList<Integer>[] posC = new ArrayList[8];
            for (int v = 1; v <= 7; v++) {
                posR[v] = new ArrayList<>();
                posC[v] = new ArrayList<>();
            }
            for (int r = 1; r <= n; r++) {
                st = new StringTokenizer(in.readLine());
                for (int c = 1; c <= m; c++) {
                    int v = Integer.parseInt(st.nextToken());
                    posR[v].add(r);
                    posC[v].add(c);
                }
            }

            // DP arrays of size (n+2)x(m+2) so we can safely look at index+1
            boolean[][] nextDP = new boolean[n+2][m+2];
            boolean[][] currDP = new boolean[n+2][m+2];
            boolean[][] good   = new boolean[n+2][m+2];

            // nextDP initially all false = losing if you are to move after l (no array left).
            // We'll fill dp for i = l down to 1.
            for (int i = l - 1; i >= 0; i--) {
                int val = a[i];

                // 1) clear GOOD array
                for (int r = 1; r <= n; r++) {
                    Arrays.fill(good[r], 1, m+1, false);
                }

                // 2) mark GOOD[r][c] = true iff picking (r,c) (of value a[i]) makes
                //    the opponent lose, i.e. nextDP[r+1][c+1] == false
                ArrayList<Integer> rr = posR[val];
                ArrayList<Integer> cc = posC[val];
                int sz = rr.size();
                for (int j = 0; j < sz; j++) {
                    int r = rr.get(j);
                    int c = cc.get(j);
                    if (!nextDP[r+1][c+1]) {
                        good[r][c] = true;
                    }
                }

                // 3) clear currDP entirely (we'll rebuild it)
                for (int r = 1; r <= n+1; r++) {
                    Arrays.fill(currDP[r], 1, m+2, false);
                }

                // 4) build currDP[R][C] = OR over GOOD in rectangle [R..n]x[C..m]
                for (int r = n; r >= 1; r--) {
                    for (int c = m; c >= 1; c--) {
                        currDP[r][c] = good[r][c]
                                     || currDP[r+1][c]
                                     || currDP[r][c+1];
                    }
                }

                // 5) swap nextDP <-> currDP for the next iteration
                boolean[][] tmp = nextDP;
                nextDP = currDP;
                currDP = tmp;
            }

            // After finishing i=1, nextDP[1][1] tells us if the first player wins
            out.println(nextDP[1][1] ? 'T' : 'N');
        }

        out.flush();
    }
}