import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            String[] nm = br.readLine().split(" ");
            int n = Integer.parseInt(nm[0]);
            int m = Integer.parseInt(nm[1]);

            char[][] grid = new char[n][];
            for (int i = 0; i < n; i++) {
                grid[i] = br.readLine().trim().toCharArray();
            }

            // 1) Find connected components of '#'
            int N = n*m;
            int[] compId = new int[N];
            Arrays.fill(compId, -1);

            List<Integer> compSize = new ArrayList<>();
            int origMax = 0;
            int compCount = 0;

            // directions for 4-connectivity
            int[] di = {1,-1,0,0};
            int[] dj = {0,0,1,-1};

            // BFS buffer
            int[] queue = new int[N];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    if (grid[i][j]=='#' && compId[i*m+j] < 0) {
                        // start new component
                        compSize.add(0);
                        int head = 0, tail = 0;
                        queue[tail++] = i*m + j;
                        compId[i*m+j] = compCount;
                        while (head < tail) {
                            int u = queue[head++];
                            int ui = u/m, uj = u%m;
                            compSize.set(compCount, compSize.get(compCount)+1);
                            for (int d=0; d<4; d++) {
                                int vi = ui+di[d], vj = uj+dj[d];
                                if (vi>=0 && vi<n && vj>=0 && vj<m) {
                                    int v = vi*m + vj;
                                    if (grid[vi][vj]=='#' && compId[v]<0) {
                                        compId[v] = compCount;
                                        queue[tail++] = v;
                                    }
                                }
                            }
                        }
                        origMax = Math.max(origMax, compSize.get(compCount));
                        compCount++;
                    }
                }
            }

            // 2) Build per-row and per-column lists of component‐IDs
            ArrayList<Integer>[] compIDsRow = new ArrayList[n];
            ArrayList<Integer>[] compIDsCol = new ArrayList[m];
            for (int i=0; i<n; i++) compIDsRow[i] = new ArrayList<>();
            for (int j=0; j<m; j++) compIDsCol[j] = new ArrayList<>();

            // Also track how many '#' in each row/column,
            // and for each component the distinct rows/columns it occupies
            int[] rowHash = new int[n], colHash = new int[m];
            ArrayList<Integer>[] compRows = new ArrayList[compCount];
            ArrayList<Integer>[] compCols = new ArrayList[compCount];
            for (int k=0; k<compCount; k++) {
                compRows[k] = new ArrayList<>();
                compCols[k] = new ArrayList<>();
            }

            for (int i=0; i<n; i++) {
                for (int j=0; j<m; j++) {
                    if (grid[i][j]=='#') {
                        int id = compId[i*m+j];
                        rowHash[i]++;
                        colHash[j]++;
                        compIDsRow[i].add(id);
                        compIDsCol[j].add(id);
                        compRows[id].add(i);
                        compCols[id].add(j);
                    }
                }
            }

            // 3) Deduplicate each row's and column's component‐list
            for (int i=0; i<n; i++) {
                ArrayList<Integer> rowList = compIDsRow[i];
                if (rowList.size()>1) {
                    Collections.sort(rowList);
                    int write=0;
                    for (int x: rowList) {
                        if (write==0 || rowList.get(write-1)!=x) {
                            rowList.set(write++, x);
                        }
                    }
                    while (rowList.size()>write) rowList.remove(rowList.size()-1);
                }
            }
            for (int j=0; j<m; j++) {
                ArrayList<Integer> colList = compIDsCol[j];
                if (colList.size()>1) {
                    Collections.sort(colList);
                    int write=0;
                    for (int x: colList) {
                        if (write==0 || colList.get(write-1)!=x) {
                            colList.set(write++, x);
                        }
                    }
                    while (colList.size()>write) colList.remove(colList.size()-1);
                }
            }

            // 4) Deduplicate each component's row‐set and column‐set
            for (int k=0; k<compCount; k++) {
                ArrayList<Integer> CR = compRows[k];
                Collections.sort(CR);
                int wr = 0;
                for (int x: CR) {
                    if (wr==0 || CR.get(wr-1) != x) {
                        CR.set(wr++, x);
                    }
                }
                while (CR.size()>wr) CR.remove(CR.size()-1);

                ArrayList<Integer> CC = compCols[k];
                Collections.sort(CC);
                int wc = 0;
                for (int x: CC) {
                    if (wc==0 || CC.get(wc-1) != x) {
                        CC.set(wc++, x);
                    }
                }
                while (CC.size()>wc) CC.remove(CC.size()-1);
            }

            // 5) Compute row‐window sums and col‐window sums
            // rowSum[r] = sum of sizes of distinct components in rows r-1, r, r+1
            long[] rowSum = new long[n], colSum = new long[m];
            int[] seenRow = new int[compCount], seenCol = new int[compCount];
            Arrays.fill(seenRow, -1);
            Arrays.fill(seenCol, -1);

            for (int r=0; r<n; r++) {
                long s = 0;
                for (int dr=-1; dr<=1; dr++) {
                    int rr = r+dr;
                    if (rr<0 || rr>=n) continue;
                    for (int id: compIDsRow[rr]) {
                        if (seenRow[id] != r) {
                            seenRow[id] = r;
                            s += compSize.get(id);
                        }
                    }
                }
                rowSum[r] = s;
            }

            for (int c=0; c<m; c++) {
                long s = 0;
                for (int dc=-1; dc<=1; dc++) {
                    int cc = c+dc;
                    if (cc<0 || cc>=m) continue;
                    for (int id: compIDsCol[cc]) {
                        if (seenCol[id] != c) {
                            seenCol[id] = c;
                            s += compSize.get(id);
                        }
                    }
                }
                colSum[c] = s;
            }

            // 6) Build the initial "H(r,c)" in a single array F[]
            // F[r*m + c] = rowSum[r]-rowHash[r] + colSum[c]-colHash[c] + overlap
            int[] F = new int[N];
            for (int r=0; r<n; r++) {
                int baseR = (int)(rowSum[r] - rowHash[r]);
                for (int c=0; c<m; c++) {
                    int idx = r*m + c;
                    int overlap = (grid[r][c]=='#' ? 1 : 0);
                    F[idx] = baseR + (int)(colSum[c] - colHash[c]) + overlap;
                }
            }

            // 7) Subtract overcount for each component k:
            //    for all (r,c) in Rk x Ck, do F[r*m + c] -= compSize[k]
            for (int k=0; k<compCount; k++) {
                int sz = compSize.get(k);
                // build Rk = compRows[k] ± 1
                ArrayList<Integer> Rk = new ArrayList<>();
                for (int r0: compRows[k]) {
                    if (!Rk.contains(r0)) Rk.add(r0);
                    if (r0>0   && !Rk.contains(r0-1)) Rk.add(r0-1);
                    if (r0+1<n && !Rk.contains(r0+1)) Rk.add(r0+1);
                }
                // build Ck
                ArrayList<Integer> Ck = new ArrayList<>();
                for (int c0: compCols[k]) {
                    if (!Ck.contains(c0)) Ck.add(c0);
                    if (c0>0    && !Ck.contains(c0-1)) Ck.add(c0-1);
                    if (c0+1<m  && !Ck.contains(c0+1)) Ck.add(c0+1);
                }
                // subtract sz from F[r*m + c]
                for (int r0: Rk) {
                    int base = r0*m;
                    for (int c0: Ck) {
                        F[base + c0] -= sz;
                    }
                }
            }

            // 8) Find the maximum F[r*m + c]
            int bestF = Integer.MIN_VALUE;
            for (int v: F) {
                bestF = Math.max(bestF, v);
            }
            if (bestF < 0) bestF = 0; 
            // (If bestF<0, it means even the best choice loses more than it gains,
            //  but we can always pick a row/column with no adjacent '#' so we get exactly plusSize.)

            // 9) Answer is max(origMax, plusSize + bestF)
            int plusSize = n + m - 1;
            int answer = Math.max(origMax, plusSize + bestF);
            out.println(answer);
        }

        out.flush();
    }
}