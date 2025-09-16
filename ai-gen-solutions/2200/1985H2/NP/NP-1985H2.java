import java.io.*;
import java.util.*;

public class Main {
    static int n, m;
    static char[] grid;        // flattened grid: grid[i*m + j]
    static int[] compId;       // compId[p] = component index of cell p, or -1 if '.'
    static int[] compSize;     // size of each component
    static int compCount;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(br.readLine().trim());
        for (int _case = 0; _case < t; _case++) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            n = Integer.parseInt(st.nextToken());
            m = Integer.parseInt(st.nextToken());

            // Read grid in a 1D array
            grid = new char[n*m];
            compId = new int[n*m];
            Arrays.fill(compId, -1);

            for (int i = 0; i < n; i++) {
                String line = br.readLine();
                for (int j = 0; j < m; j++) {
                    grid[i*m + j] = line.charAt(j);
                }
            }

            // Step 1: BFS to label connected components of '#'
            compCount = 0;
            List<Integer> sizesList = new ArrayList<>();
            int[] queue = new int[n*m];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    int idx = i*m + j;
                    if (grid[idx] == '#' && compId[idx] == -1) {
                        // BFS from (i,j)
                        int head = 0, tail = 0;
                        queue[tail++] = idx;
                        compId[idx] = compCount;
                        int cnt = 0;
                        while (head < tail) {
                            int p = queue[head++];
                            cnt++;
                            int r = p / m, c = p % m;
                            // four neighbors
                            if (r > 0)      addNeighbor(p - m, compCount, queue);
                            if (r+1 < n)    addNeighbor(p + m, compCount, queue);
                            if (c > 0)      addNeighbor(p - 1, compCount, queue);
                            if (c+1 < m)    addNeighbor(p + 1, compCount, queue);
                        }
                        sizesList.add(cnt);
                        compCount++;
                    }
                }
            }
            compSize = new int[compCount];
            for (int i = 0; i < compCount; i++) {
                compSize[i] = sizesList.get(i);
            }

            // Step 2: Build compsRow[r], compsCol[c], and also dotRow/dotCol
            List<List<Integer>> compsRow = new ArrayList<>(n);
            for (int i = 0; i < n; i++) compsRow.add(new ArrayList<>());
            List<List<Integer>> compsCol = new ArrayList<>(m);
            for (int j = 0; j < m; j++) compsCol.add(new ArrayList<>());

            int[] dotRow = new int[n], dotCol = new int[m];
            boolean[] mark = new boolean[compCount];

            // Build compsRow[r]
            for (int r = 0; r < n; r++) {
                int c0 = r*m;  // base index for row r
                for (int c = 0; c < m; c++) {
                    int idx = c0 + c;
                    if (grid[idx] == '.') {
                        dotRow[r]++;
                        // see if above or below is a '#' to attach
                        if (r > 0) {
                            int upId = compId[idx - m];
                            if (upId >= 0 && !mark[upId]) {
                                mark[upId] = true;
                                compsRow.get(r).add(upId);
                            }
                        }
                        if (r+1 < n) {
                            int dnId = compId[idx + m];
                            if (dnId >= 0 && !mark[dnId]) {
                                mark[dnId] = true;
                                compsRow.get(r).add(dnId);
                            }
                        }
                    } else {
                        // it's '#'
                        int k = compId[idx];
                        if (!mark[k]) {
                            mark[k] = true;
                            compsRow.get(r).add(k);
                        }
                    }
                }
                // clear marks
                for (int k : compsRow.get(r)) {
                    mark[k] = false;
                }
            }

            // Build compsCol[c]
            for (int c = 0; c < m; c++) {
                for (int r = 0; r < n; r++) {
                    int idx = r*m + c;
                    if (grid[idx] == '.') {
                        dotCol[c]++;
                        // left / right
                        if (c > 0) {
                            int L = compId[idx - 1];
                            if (L >= 0 && !mark[L]) {
                                mark[L] = true;
                                compsCol.get(c).add(L);
                            }
                        }
                        if (c+1 < m) {
                            int R = compId[idx + 1];
                            if (R >= 0 && !mark[R]) {
                                mark[R] = true;
                                compsCol.get(c).add(R);
                            }
                        }
                    } else {
                        int k = compId[idx];
                        if (!mark[k]) {
                            mark[k] = true;
                            compsCol.get(c).add(k);
                        }
                    }
                }
                // clear marks
                for (int k : compsCol.get(c)) {
                    mark[k] = false;
                }
            }

            // Also build compCols[k] = list of columns that comp k appears in
            List<List<Integer>> compCols = new ArrayList<>(compCount);
            for (int k = 0; k < compCount; k++) {
                compCols.add(new ArrayList<>());
            }
            for (int c = 0; c < m; c++) {
                for (int k : compsCol.get(c)) {
                    compCols.get(k).add(c);
                }
            }

            // Build Rsum/A[r], Csum/B[c]
            long[] Rsum = new long[n], Csum = new long[m];
            for (int r = 0; r < n; r++) {
                long sum = 0;
                for (int k : compsRow.get(r)) {
                    sum += compSize[k];
                }
                Rsum[r] = sum;
            }
            for (int c = 0; c < m; c++) {
                long sum = 0;
                for (int k : compsCol.get(c)) {
                    sum += compSize[k];
                }
                Csum[c] = sum;
            }

            long[] B = new long[m];
            for (int c = 0; c < m; c++) {
                B[c] = Csum[c] + dotCol[c];
            }

            // For each row r we will build D[c] = B[c] - isDot(r,c),
            // then for each comp k in row r subtract compSize[k] from D at all c in compCols[k].
            // We maximize D[c].
            long answer = 0;
            long[] D = new long[m];

            for (int r = 0; r < n; r++) {
                long Ar = Rsum[r] + dotRow[r];
                // initialize D
                int base = r*m;
                for (int c = 0; c < m; c++) {
                    D[c] = B[c] - (grid[base + c] == '.' ? 1 : 0);
                }
                // subtract overlaps
                for (int k : compsRow.get(r)) {
                    int sz = compSize[k];
                    for (int c : compCols.get(k)) {
                        D[c] -= sz;
                    }
                }
                // find max
                long bestD = Long.MIN_VALUE;
                for (int c = 0; c < m; c++) {
                    if (D[c] > bestD) bestD = D[c];
                }
                long cand = Ar + bestD;
                if (cand > answer) answer = cand;
            }

            out.println(answer);
        }

        out.flush();
    }

    // Helper for BFS: if p in bounds, grid[p]=='#' and not yet visited, add to queue
    static void addNeighbor(int p, int compIdx, int[] queue) {
        if (grid[p] == '#' && compId[p] == -1) {
            compId[p] = compIdx;
            queue[queueTail++] = p;
        }
    }

    // We'll rewrite BFS carefully since we need a tail pointer
    static int queueTail;
}