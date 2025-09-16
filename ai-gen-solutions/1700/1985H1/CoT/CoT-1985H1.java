import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader in;
    static PrintWriter out;
    static StringTokenizer tok;

    // Read next token
    static String next() throws IOException {
        while (tok == null || !tok.hasMoreTokens()) {
            String line = in.readLine();
            if (line == null) return null;
            tok = new StringTokenizer(line);
        }
        return tok.nextToken();
    }

    // Read next integer
    static int nextInt() throws IOException {
        return Integer.parseInt(next());
    }

    public static void main(String[] args) throws IOException {
        in  = new BufferedReader(new InputStreamReader(System.in));
        out = new PrintWriter(System.out);
        int t = nextInt();
        while (t-- > 0) {
            int n = nextInt();
            int m = nextInt();
            String[] grid = new String[n];
            for (int i = 0; i < n; i++) {
                grid[i] = in.readLine();
            }

            // Flattened id array: id[k] = component ID of cell k, or -1 if '.'
            int N = n * m;
            int[] id = new int[N];
            Arrays.fill(id, -1);

            // Count of '#' in each row/column initially
            int[] rowCount = new int[n];
            int[] colCount = new int[m];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    if (grid[i].charAt(j) == '#') {
                        rowCount[i]++;
                        colCount[j]++;
                    }
                }
            }

            // Directions for BFS
            int[] dr = {1, -1, 0, 0};
            int[] dc = {0, 0, 1, -1};

            // List of component sizes
            ArrayList<Integer> compSize = new ArrayList<>();
            int compCount = 0;

            // BFS queue (flattened indices)
            int[] queue = new int[N];

            // Label components
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    if (grid[i].charAt(j) == '#' && id[i*m + j] == -1) {
                        // Start BFS
                        int head = 0, tail = 0;
                        queue[tail++] = i*m + j;
                        id[i*m + j] = compCount;
                        int size = 0;
                        while (head < tail) {
                            int cur = queue[head++];
                            size++;
                            int r = cur / m;
                            int c = cur % m;
                            // Explore 4 neighbors
                            for (int d = 0; d < 4; d++) {
                                int nr = r + dr[d];
                                int nc = c + dc[d];
                                if (nr >= 0 && nr < n && nc >= 0 && nc < m) {
                                    int nxt = nr*m + nc;
                                    if (grid[nr].charAt(nc) == '#' && id[nxt] == -1) {
                                        id[nxt] = compCount;
                                        queue[tail++] = nxt;
                                    }
                                }
                            }
                        }
                        compSize.add(size);
                        compCount++;
                    }
                }
            }

            // baseMax = largest existing component
            int baseMax = 0;
            for (int sz : compSize) {
                if (sz > baseMax) baseMax = sz;
            }

            // We'll now try flipping each row and each column.
            // To avoid recounting the same component twice in one flip,
            // we keep a "lastSeen" array indexed by component ID,
            // plus a global iteration counter.
            int[] lastSeen = new int[compCount];
            int iter = 1;
            int answer = baseMax;

            // Try flipping each row r
            for (int r = 0; r < n; r++, iter++) {
                int mergedSum = 0;
                // 1) components already in row r
                for (int j = 0; j < m; j++) {
                    if (grid[r].charAt(j) == '#') {
                        int cid = id[r*m + j];
                        if (lastSeen[cid] != iter) {
                            mergedSum += compSize.get(cid);
                            lastSeen[cid] = iter;
                        }
                    }
                }
                // 2) components just above row r
                if (r > 0) {
                    for (int j = 0; j < m; j++) {
                        if (grid[r-1].charAt(j) == '#') {
                            int cid = id[(r-1)*m + j];
                            if (lastSeen[cid] != iter) {
                                mergedSum += compSize.get(cid);
                                lastSeen[cid] = iter;
                            }
                        }
                    }
                }
                // 3) components just below row r
                if (r+1 < n) {
                    for (int j = 0; j < m; j++) {
                        if (grid[r+1].charAt(j) == '#') {
                            int cid = id[(r+1)*m + j];
                            if (lastSeen[cid] != iter) {
                                mergedSum += compSize.get(cid);
                                lastSeen[cid] = iter;
                            }
                        }
                    }
                }
                // + newly flipped cells in row r
                int newCells = m - rowCount[r];
                answer = Math.max(answer, mergedSum + newCells);
            }

            // Try flipping each column c
            for (int c = 0; c < m; c++, iter++) {
                int mergedSum = 0;
                // 1) components already in column c
                for (int i = 0; i < n; i++) {
                    if (grid[i].charAt(c) == '#') {
                        int cid = id[i*m + c];
                        if (lastSeen[cid] != iter) {
                            mergedSum += compSize.get(cid);
                            lastSeen[cid] = iter;
                        }
                    }
                }
                // 2) components to the left of column c
                if (c > 0) {
                    for (int i = 0; i < n; i++) {
                        if (grid[i].charAt(c-1) == '#') {
                            int cid = id[i*m + (c-1)];
                            if (lastSeen[cid] != iter) {
                                mergedSum += compSize.get(cid);
                                lastSeen[cid] = iter;
                            }
                        }
                    }
                }
                // 3) components to the right of column c
                if (c+1 < m) {
                    for (int i = 0; i < n; i++) {
                        if (grid[i].charAt(c+1) == '#') {
                            int cid = id[i*m + (c+1)];
                            if (lastSeen[cid] != iter) {
                                mergedSum += compSize.get(cid);
                                lastSeen[cid] = iter;
                            }
                        }
                    }
                }
                // + newly flipped cells in column c
                int newCells = n - colCount[c];
                answer = Math.max(answer, mergedSum + newCells);
            }

            out.println(answer);
        }
        out.flush();
    }
}