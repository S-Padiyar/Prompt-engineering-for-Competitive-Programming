import java.io.*;
import java.util.*;

public class Main {
    static class DSU {
        int[] p, r;
        public DSU(int n) {
            p = new int[n];
            r = new int[n];
            for (int i = 0; i < n; i++) {
                p[i] = i;
                r[i] = 0;
            }
        }
        int find(int x) {
            if (p[x] != x) p[x] = find(p[x]);
            return p[x];
        }
        boolean union(int x, int y) {
            x = find(x);
            y = find(y);
            if (x == y) return false;
            if (r[x] < r[y]) {
                p[x] = y;
            } else if (r[y] < r[x]) {
                p[y] = x;
            } else {
                p[y] = x;
                r[x]++;
            }
            return true;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter pw = new PrintWriter(System.out);

        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            int[] a = new int[n];
            StringTokenizer st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            // ansU[x], ansV[x] will store the edge chosen at operation x (1-based).
            int[] ansU = new int[n], ansV = new int[n];
            boolean possible = true;
            DSU dsu = new DSU(n);

            // For each x = n-1 down to 1, pick exactly one cross-component pair
            for (int x = n - 1; x >= 1; x--) {
                // remToRoot[r] = a DSU-root that we've seen with remainder r = a[u]%x
                // nodeWithRem[r] = one example vertex index that achieves that remainder
                int[] remToRoot = new int[x];
                int[] nodeWithRem = new int[x];
                // initialize to -1 meaning "unseen"
                for (int r = 0; r < x; r++) {
                    remToRoot[r] = -1;
                }

                boolean found = false;
                for (int u = 0; u < n; u++) {
                    int ru = dsu.find(u);
                    int r = a[u] % x;
                    if (remToRoot[r] == -1) {
                        // first time we see this remainder in this operation
                        remToRoot[r] = ru;
                        nodeWithRem[r] = u;
                    } else if (remToRoot[r] != ru) {
                        // we found two vertices in different components
                        int v = nodeWithRem[r];
                        dsu.union(u, v);
                        ansU[x] = u + 1;    // convert to 1-based
                        ansV[x] = v + 1;
                        found = true;
                        break;
                    }
                    // else same component -> skip
                }
                if (!found) {
                    // no valid cross-component edge at this x
                    possible = false;
                    break;
                }
            }

            if (!possible) {
                pw.println("No");
            } else {
                pw.println("Yes");
                // Output the edges in the order of operations 1..n-1
                for (int x = 1; x < n; x++) {
                    pw.println(ansU[x] + " " + ansV[x]);
                }
            }
        }
        pw.flush();
    }
}