import java.io.*;
import java.util.*;

public class Main {
    static int n, m;
    static char[][] grid;
    static int[][] compId;
    static int[] sizeComp;
    static int[] minR, maxR, minC, maxC;
    static int[] rowHashCount, colHashCount;
    static int[] dr = {1, -1, 0, 0};
    static int[] dc = {0, 0, 1, -1};

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(in.readLine().trim());
        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            n = Integer.parseInt(st.nextToken());
            m = Integer.parseInt(st.nextToken());
            grid = new char[n+1][m+1];
            for (int i = 1; i <= n; i++) {
                String line = in.readLine();
                for (int j = 1; j <= m; j++) {
                    grid[i][j] = line.charAt(j-1);
                }
            }

            // 1) Label components
            compId = new int[n+1][m+1];
            // Worst case number of components ≤ n*m
            sizeComp = new int[n*m + 5];
            minR = new int[n*m + 5];
            maxR = new int[n*m + 5];
            minC = new int[n*m + 5];
            maxC = new int[n*m + 5];
            int compCount = 0;
            int origMax = 0;

            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= m; j++) {
                    if (grid[i][j] == '#' && compId[i][j] == 0) {
                        compCount++;
                        int id = compCount;
                        // initialize bounding box
                        minR[id] = minC[id] = Integer.MAX_VALUE;
                        maxR[id] = maxC[id] = -1;
                        // BFS
                        Deque<int[]> dq = new ArrayDeque<>();
                        dq.offer(new int[]{i, j});
                        compId[i][j] = id;
                        sizeComp[id] = 0;
                        while (!dq.isEmpty()) {
                            int[] u = dq.poll();
                            int r = u[0], c = u[1];
                            sizeComp[id]++;
                            // update bbox
                            minR[id] = Math.min(minR[id], r);
                            maxR[id] = Math.max(maxR[id], r);
                            minC[id] = Math.min(minC[id], c);
                            maxC[id] = Math.max(maxC[id], c);

                            for (int d = 0; d < 4; d++) {
                                int rr = r + dr[d], cc = c + dc[d];
                                if (rr >= 1 && rr <= n && cc >= 1 && cc <= m
                                  && grid[rr][cc] == '#' 
                                  && compId[rr][cc] == 0) {
                                    compId[rr][cc] = id;
                                    dq.offer(new int[]{rr, cc});
                                }
                            }
                        }
                        origMax = Math.max(origMax, sizeComp[id]);
                    }
                }
            }

            // 2) Build difference arrays for rowSum, colSum, and 2D overlap
            long[] rowDiff = new long[n+2];
            long[] colDiff = new long[m+2];
            long[][] diff2D = new long[n+2][m+2];

            for (int id = 1; id <= compCount; id++) {
                int r1 = Math.max(1, minR[id] - 1);
                int r2 = Math.min(n, maxR[id] + 1);
                int c1 = Math.max(1, minC[id] - 1);
                int c2 = Math.min(m, maxC[id] + 1);
                long sz = sizeComp[id];

                // 1D row interval [r1..r2]
                rowDiff[r1] += sz;
                rowDiff[r2+1] -= sz;
                // 1D col interval [c1..c2]
                colDiff[c1] += sz;
                colDiff[c2+1] -= sz;

                // 2D rectangle [r1..r2] x [c1..c2]
                diff2D[r1][c1]     += sz;
                diff2D[r2+1][c1]   -= sz;
                diff2D[r1][c2+1]   -= sz;
                diff2D[r2+1][c2+1] += sz;
            }

            // 3) Prefix-sum to get rowSum and colSum
            long[] rowSum = new long[n+2];
            long[] colSum = new long[m+2];
            for (int i = 1; i <= n; i++)
                rowSum[i] = rowSum[i-1] + rowDiff[i];
            for (int j = 1; j <= m; j++)
                colSum[j] = colSum[j-1] + colDiff[j];

            // 4) 2D prefix-sum to get overlap[r][c]
            long[][] overlap = new long[n+2][m+2];
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= m; j++) {
                    overlap[i][j]
                      = diff2D[i][j]
                      + overlap[i-1][j]
                      + overlap[i][j-1]
                      - overlap[i-1][j-1];
                }
            }

            // 5) Count how many '#' in each row/col
            rowHashCount = new int[n+1];
            colHashCount = new int[m+1];
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= m; j++) {
                    if (grid[i][j] == '#') {
                        rowHashCount[i]++;
                        colHashCount[j]++;
                    }
                }
            }

            // 6) Compute the best possible after one operation
            long best = origMax;  // maybe don't do any op
            long constantCross = (long)n + m - 1;

            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= m; j++) {
                    // R + C - O = sum of unique comp sizes that touch row i or col j
                    long R = rowSum[i];
                    long C = colSum[j];
                    long O = overlap[i][j];
                    // subtract original '#' on that cross
                    long sub = rowHashCount[i] + colHashCount[j]
                               - (grid[i][j] == '#' ? 1 : 0);
                    long total = constantCross + (R + C - O) - sub;
                    if (total > best) best = total;
                }
            }

            sb.append(best).append("\n");
        }

        System.out.print(sb);
    }
}