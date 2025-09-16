import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // Fast input reader
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        PrintWriter pw = new PrintWriter(System.out);

        // Max value from problem constraints
        final int MAXV = 200_000;
        // pos[x] will store the 1-based index of value x in array b (or 0 if absent)
        int[] pos = new int[MAXV + 1];
        // temporary storage for b-values to reset pos[] each test
        List<Integer> used = new ArrayList<>();

        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());

            // Read array a
            int[] a = new int[n];
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            // Read array b, record positions
            used.clear();
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                int bv = Integer.parseInt(st.nextToken());
                pos[bv] = i + 1;  // store 1-based
                used.add(bv);
            }

            // Build the permutation P: c[i] = position of a[i] in b
            int[] c = new int[n + 1];  // 1-based permutation
            boolean possible = true;
            for (int i = 0; i < n; i++) {
                int idxInB = pos[a[i]];
                if (idxInB == 0) {
                    // a[i] not found in b => different sets
                    possible = false;
                    break;
                }
                c[i + 1] = idxInB;
            }

            if (!possible) {
                pw.println("NO");
            } else {
                // Count cycles in permutation c[1..n]
                boolean[] vis = new boolean[n + 1];
                int cycles = 0;
                for (int i = 1; i <= n; i++) {
                    if (!vis[i]) {
                        cycles++;
                        // walk the cycle
                        int cur = i;
                        while (!vis[cur]) {
                            vis[cur] = true;
                            cur = c[cur];
                        }
                    }
                }
                // permutation parity = (n - cycles) mod 2
                int parity = (n - cycles) & 1;  // 0 = even, 1 = odd
                pw.println(parity == 0 ? "YES" : "NO");
            }

            // Reset pos[] for the elements of b
            for (int x : used) {
                pos[x] = 0;
            }
        }

        pw.flush();
        pw.close();
    }
}