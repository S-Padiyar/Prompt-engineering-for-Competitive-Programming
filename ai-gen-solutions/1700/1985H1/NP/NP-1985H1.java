import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder   sb = new StringBuilder();
        int t = Integer.parseInt(br.readLine().trim());
        
        // Directions for 4-connected BFS
        final int[] d4i = {-1, 1, 0, 0};
        final int[] d4j = { 0, 0,-1, 1};
        
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());
            char[][] grid = new char[n][];
            for (int i = 0; i < n; i++) {
                grid[i] = br.readLine().toCharArray();
            }
            
            int N = n*m;               // total cells
            int[] comp = new int[N];   // component id of each cell (0 if '.')
            int[] compSize = new int[N+1]; // compSize[id] = size of component id
            int origMax = 0;
            
            // BFS queue
            int[] queue = new int[N];
            
            // 1) Label all components
            int compCount = 0;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    int idx = i*m + j;
                    if (grid[i][j] == '#' && comp[idx] == 0) {
                        // new component
                        compCount++;
                        comp[idx] = compCount;
                        int sz = 1;
                        
                        // BFS
                        int head = 0, tail = 0;
                        queue[tail++] = idx;
                        while (head < tail) {
                            int cur = queue[head++];
                            int ci = cur / m, cj = cur % m;
                            for (int d = 0; d < 4; d++) {
                                int ni = ci + d4i[d], nj = cj + d4j[d];
                                if (0 <= ni && ni < n && 0 <= nj && nj < m) {
                                    int nidx = ni*m + nj;
                                    if (grid[ni][nj] == '#' && comp[nidx] == 0) {
                                        comp[nidx] = compCount;
                                        queue[tail++] = nidx;
                                        sz++;
                                    }
                                }
                            }
                        }
                        
                        compSize[compCount] = sz;
                        origMax = Math.max(origMax, sz);
                    }
                }
            }
            
            // We'll use a "lastUsed" stamp array to avoid double counting
            int[] lastUsed  = new int[compCount+1];
            int   timestamp = 0;
            int   bestPaint = 0;
            
            // 2) Try painting each row
            for (int i = 0; i < n; i++) {
                timestamp++;
                int sum = m;  // we get m new '#' cells
                int stamp = timestamp;
                
                for (int j = 0; j < m; j++) {
                    int idx = i*m + j;
                    // check above
                    if (i > 0) {
                        int c = comp[idx - m];
                        if (c != 0 && lastUsed[c] != stamp) {
                            lastUsed[c] = stamp;
                            sum += compSize[c];
                        }
                    }
                    // check below
                    if (i+1 < n) {
                        int c = comp[idx + m];
                        if (c != 0 && lastUsed[c] != stamp) {
                            lastUsed[c] = stamp;
                            sum += compSize[c];
                        }
                    }
                }
                bestPaint = Math.max(bestPaint, sum);
            }
            
            // 3) Try painting each column
            for (int j = 0; j < m; j++) {
                timestamp++;
                int sum = n;  // we get n new '#' cells
                int stamp = timestamp;
                
                for (int i = 0; i < n; i++) {
                    int idx = i*m + j;
                    // check left
                    if (j > 0) {
                        int c = comp[idx - 1];
                        if (c != 0 && lastUsed[c] != stamp) {
                            lastUsed[c] = stamp;
                            sum += compSize[c];
                        }
                    }
                    // check right
                    if (j+1 < m) {
                        int c = comp[idx + 1];
                        if (c != 0 && lastUsed[c] != stamp) {
                            lastUsed[c] = stamp;
                            sum += compSize[c];
                        }
                    }
                }
                bestPaint = Math.max(bestPaint, sum);
            }
            
            // Final answer for this test
            sb.append(Math.max(origMax, bestPaint)).append('\n');
        }
        
        // Output all at once
        System.out.print(sb);
    }
}