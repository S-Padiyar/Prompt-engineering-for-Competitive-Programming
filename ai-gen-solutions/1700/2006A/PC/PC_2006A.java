import java.io.*;
import java.util.*;

public class Main {
    static FastInput in = new FastInput();
    static StringBuilder sb = new StringBuilder();

    public static void main(String[] args) throws IOException {
        int t = in.nextInt();
        while(t-- > 0) {
            int n = in.nextInt();
            List<Integer>[] adj = new ArrayList[n+1];
            for(int i = 1; i <= n; i++) adj[i] = new ArrayList<>();
            for(int i = 0; i < n-1; i++) {
                int u = in.nextInt(), v = in.nextInt();
                adj[u].add(v);
                adj[v].add(u);
            }
            char[] s = (" " + in.next()).toCharArray();

            // We root at 1, do a BFS to identify parent & leaves
            int[] parent = new int[n+1];
            parent[1] = -1;
            Deque<Integer> dq = new ArrayDeque<>();
            dq.add(1);
            // BFS
            while(!dq.isEmpty()) {
                int u = dq.poll();
                for(int v: adj[u]) {
                    if(v == parent[u]) continue;
                    parent[v] = u;
                    dq.add(v);
                }
            }

            // Count leaves: a leaf is (v!=1 && adj[v].size()==1)
            int F0 = 0, F1 = 0, uLeaves = 0, eIrrelevant = 0;
            for(int v = 2; v <= n; v++) {
                if(adj[v].size() == 1) {
                    // it's a leaf
                    if(s[v] == '0')      F0++;
                    else if(s[v] == '1') F1++;
                    else                 uLeaves++;
                } else {
                    // interior non-leaf
                    if(v != 1 && s[v] == '?') {
                        eIrrelevant++;
                    }
                }
            }

            // Case A: root is fixed
            if(s[1] == '0' || s[1] == '1') {
                int r = s[1] - '0';
                // leaves mismatching r = if r==0, count of 1-leaves; else count of 0-leaves
                int base = (r == 0 ? F1 : F0);
                // in the leaf-game, Iris goes first on uLeaves leaves => she gets ceil(uLeaves/2).
                int leafGain = (uLeaves + 1) / 2;
                sb.append(base + leafGain).append('\n');
            }
            else {
                // root = '?'
                // option 1: Iris grabs root => gets max(F0,F1), then Dora starts leaves => Iris only floor(u/2)
                int opt1 = Math.max(F0, F1) + (uLeaves / 2);

                // option 2: Iris lets Dora set root => Dora picks min(F0,F1), then Iris starts leaves => Iris ceil(u/2)
                int opt2 = Math.min(F0, F1) + ((uLeaves + 1) / 2);

                // if no irrelevant passes are available, Iris must do opt1.
                // otherwise she can choose the better of the two.
                int ans = (eIrrelevant == 0 ? opt1 : Math.max(opt1, opt2));
                sb.append(ans).append('\n');
            }
        }
        System.out.print(sb);
    }

    // FastInput for speed
    static class FastInput {
        BufferedReader br;
        StringTokenizer st;
        FastInput() {
            br = new BufferedReader(new InputStreamReader(System.in));
        }
        String next() throws IOException {
            while (st == null || !st.hasMoreTokens()) {
                st = new StringTokenizer(br.readLine());
            }
            return st.nextToken();
        }
        int nextInt() throws IOException {
            return Integer.parseInt(next());
        }
    }
}