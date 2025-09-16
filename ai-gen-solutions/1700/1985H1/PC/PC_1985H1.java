import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            String[] nm = br.readLine().trim().split(" ");
            int n = Integer.parseInt(nm[0]);
            int m = Integer.parseInt(nm[1]);
            int N = n * m;

            // Read grid into a flat array
            char[] grid = new char[N];
            int[] origHashRow = new int[n];
            int[] origHashCol = new int[m];
            for (int i = 0; i < n; i++) {
                String line = br.readLine();
                for (int j = 0; j < m; j++) {
                    char c = line.charAt(j);
                    grid[i*m + j] = c;
                    if (c == '#') {
                        origHashRow[i]++;
                        origHashCol[j]++;
                    }
                }
            }

            // comp[idx] = component-id of cell idx, or -1 if '.'
            int[] comp = new int[N];
            Arrays.fill(comp, -1);

            // We will store sizes of each component in compSize[]
            // We don't yet know how many components, but at most N.
            int[] compSize = new int[N];
            int compCount = 0;
            int initialMax = 0;

            // BFS queue
            int[] queue = new int[N];

            // Label components by BFS
            for (int start = 0; start < N; start++) {
                if (grid[start] == '#' && comp[start] < 0) {
                    // New component
                    int head = 0, tail = 0;
                    queue[tail++] = start;
                    comp[start] = compCount;
                    int sizeC = 1;

                    while (head < tail) {
                        int cur = queue[head++];
                        int r = cur / m, c = cur % m;

                        // Up
                        if (r > 0) {
                            int nb = cur - m;
                            if (grid[nb] == '#' && comp[nb] < 0) {
                                comp[nb] = compCount;
                                queue[tail++] = nb;
                                sizeC++;
                            }
                        }
                        // Down
                        if (r < n - 1) {
                            int nb = cur + m;
                            if (grid[nb] == '#' && comp[nb] < 0) {
                                comp[nb] = compCount;
                                queue[tail++] = nb;
                                sizeC++;
                            }
                        }
                        // Left
                        if (c > 0) {
                            int nb = cur - 1;
                            if (grid[nb] == '#' && comp[nb] < 0) {
                                comp[nb] = compCount;
                                queue[tail++] = nb;
                                sizeC++;
                            }
                        }
                        // Right
                        if (c < m - 1) {
                            int nb = cur + 1;
                            if (grid[nb] == '#' && comp[nb] < 0) {
                                comp[nb] = compCount;
                                queue[tail++] = nb;
                                sizeC++;
                            }
                        }
                    }

                    compSize[compCount] = sizeC;
                    if (sizeC > initialMax) {
                        initialMax = sizeC;
                    }
                    compCount++;
                }
            }

            // lastSeen[cid] helps us sum each component-size at most once per row/column pass
            int[] lastSeen = new int[compCount];
            int stamp = 1;
            int answer = initialMax;

            // Try filling each row r
            for (int r = 0; r < n; r++) {
                stamp++;
                int sumSizes = 0;
                // look at rows r-1, r, r+1
                for (int dr = -1; dr <= 1; dr++) {
                    int rr = r + dr;
                    if (rr < 0 || rr >= n) continue;
                    int base = rr * m;
                    for (int j = 0; j < m; j++) {
                        int idx = base + j;
                        int cid = comp[idx];
                        if (cid >= 0 && lastSeen[cid] != stamp) {
                            lastSeen[cid] = stamp;
                            sumSizes += compSize[cid];
                        }
                    }
                }
                int merged = m - origHashRow[r] + sumSizes;
                if (merged > answer) answer = merged;
            }

            // Try filling each column c
            for (int c = 0; c < m; c++) {
                stamp++;
                int sumSizes = 0;
                // look at columns c-1, c, c+1
                for (int dc = -1; dc <= 1; dc++) {
                    int cc = c + dc;
                    if (cc < 0 || cc >= m) continue;
                    for (int i = 0; i < n; i++) {
                        int idx = i*m + cc;
                        int cid = comp[idx];
                        if (cid >= 0 && lastSeen[cid] != stamp) {
                            lastSeen[cid] = stamp;
                            sumSizes += compSize[cid];
                        }
                    }
                }
                int merged = n - origHashCol[c] + sumSizes;
                if (merged > answer) answer = merged;
            }

            out.println(answer);
        }

        out.flush();
    }
}