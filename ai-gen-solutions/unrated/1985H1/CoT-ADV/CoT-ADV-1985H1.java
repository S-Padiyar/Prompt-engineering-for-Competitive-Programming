import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // Fast I/O
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        int t = Integer.parseInt(br.readLine().trim());

        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());

            // Read the grid
            char[][] grid = new char[n][];
            for (int i = 0; i < n; i++) {
                grid[i] = br.readLine().toCharArray();
            }

            // Step 1: Label connected components of '#' via iterative DFS
            int[][] compId = new int[n][m];
            // compSize[id] = size of component 'id'
            // We allocate maximum possible components = n*m (worst case)
            int[] compSize = new int[n*m + 1];
            int compCount = 0;
            int ans0 = 0;  // largest original component

            int[] stack = new int[n*m];  // for DFS: store i*m + j
            int[] dx = {1, -1, 0, 0}, dy = {0, 0, 1, -1};

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    if (grid[i][j] == '#' && compId[i][j] == 0) {
                        // New component
                        compCount++;
                        int sp = 0;
                        compId[i][j] = compCount;
                        stack[sp++] = i*m + j;
                        int size = 0;

                        // DFS
                        while (sp > 0) {
                            int v = stack[--sp];
                            int x = v / m, y = v % m;
                            size++;

                            for (int d = 0; d < 4; d++) {
                                int nx = x + dx[d], ny = y + dy[d];
                                if (nx>=0 && nx<n && ny>=0 && ny<m
                                        && grid[nx][ny]=='#' 
                                        && compId[nx][ny]==0) {
                                    compId[nx][ny] = compCount;
                                    stack[sp++] = nx*m + ny;
                                }
                            }
                        }
                        compSize[compCount] = size;
                        ans0 = Math.max(ans0, size);
                    }
                }
            }

            // If there were no '#' at all, best we can do is fill one row or column
            if (compCount == 0) {
                out.println(Math.max(n, m));
                continue;
            }

            // Precompute how many '#' in each row and column
            int[] rowCount = new int[n], colCount = new int[m];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    if (grid[i][j] == '#') {
                        rowCount[i]++;
                        colCount[j]++;
                    }
                }
            }

            // We'll mark which component IDs we've already added
            boolean[] seen = new boolean[compCount + 1];
            List<Integer> list = new ArrayList<>();

            int answer = ans0;

            // Try filling each row
            for (int r = 0; r < n; r++) {
                list.clear();
                long sumC = 0;
                // For each column j, look at rows r-1, r, r+1
                for (int j = 0; j < m; j++) {
                    for (int dr = -1; dr <= 1; dr++) {
                        int i2 = r + dr;
                        if (i2 < 0 || i2 >= n) continue;
                        if (grid[i2][j] == '#') {
                            int id = compId[i2][j];
                            if (!seen[id]) {
                                seen[id] = true;
                                list.add(id);
                                sumC += compSize[id];
                            }
                        }
                    }
                }
                // Size = m (new row) + sum of those component sizes - rowCount[r]
                long mergedSize = (long)m + sumC - rowCount[r];
                answer = (int)Math.max(answer, mergedSize);

                // reset seen[]
                for (int id : list) {
                    seen[id] = false;
                }
            }

            // Try filling each column
            for (int c = 0; c < m; c++) {
                list.clear();
                long sumC = 0;
                // For each row i, look at columns c-1, c, c+1
                for (int i = 0; i < n; i++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        int j2 = c + dc;
                        if (j2 < 0 || j2 >= m) continue;
                        if (grid[i][j2] == '#') {
                            int id = compId[i][j2];
                            if (!seen[id]) {
                                seen[id] = true;
                                list.add(id);
                                sumC += compSize[id];
                            }
                        }
                    }
                }
                // Size = n (new column) + sum of those component sizes - colCount[c]
                long mergedSize = (long)n + sumC - colCount[c];
                answer = (int)Math.max(answer, mergedSize);

                // reset
                for (int id : list) {
                    seen[id] = false;
                }
            }

            out.println(answer);
        }

        out.flush();
    }
}