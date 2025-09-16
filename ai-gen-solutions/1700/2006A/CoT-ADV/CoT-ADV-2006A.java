import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int t = Integer.parseInt(br.readLine());
        // We will process each test in O(n).

        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine());
            List<Integer>[] adj = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) {
                adj[i] = new ArrayList<>();
            }

            // Read edges
            for (int i = 0; i < n-1; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken()), v = Integer.parseInt(st.nextToken());
                adj[u].add(v);
                adj[v].add(u);
            }

            // Read the string s[1..n]
            char[] s = (" " + br.readLine()).toCharArray();

            // 1) Identify leaves: i != 1 and adj[i].size() == 1
            boolean[] isLeaf = new boolean[n+1];
            for (int i = 2; i <= n; i++) {
                if (adj[i].size() == 1) {
                    isLeaf[i] = true;
                }
            }

            // 2) Count L0, L1, U over leaves; and m over internal '?'
            int L0 = 0, L1 = 0, U = 0, m = 0;
            for (int i = 1; i <= n; i++) {
                if (isLeaf[i]) {
                    if (s[i] == '0')      L0++;
                    else if (s[i] == '1') L1++;
                    else                  U++;   // leaf '?'
                } else {
                    // either root=1 or internal
                    if (i != 1 && s[i] == '?') {
                        m++;  // internal '?'
                    }
                }
            }

            long ans = 0;
            // CASE A: root is fixed
            if (s[1] == '0' || s[1] == '1') {
                // base = # of leaves != root
                int base = (s[1] == '0' ? L1 : L0);
                // Iris moves first on the U leaves => she secures ceil(U/2)
                int takeLeaves = (U + 1) / 2;
                ans = base + takeLeaves;
            }
            else {
                // root == '?'
                // Option 1: Iris sets root herself => Dora starts leaves => Iris gets floor(U/2)
                long V1 = Math.max(L0, L1) + (U / 2);
                if (m == 0) {
                    // no dummy moves => Iris has to do Option1
                    ans = V1;
                } else {
                    // Option 2: Iris forces Dora to set root => Iris starts leaves => she gets ceil(U/2)
                    long V2 = Math.min(L0, L1) + ((U + 1) / 2);
                    ans = Math.max(V1, V2);
                }
            }

            System.out.println(ans);
        }
    }
}