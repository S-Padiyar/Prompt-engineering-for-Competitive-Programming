import java.io.*;
import java.util.*;

public class Main {
    static class Query {
        int idx;   // which query number (to place answer in correct order)
        int pos;   // the monster index i in the query
        Query(int idx, int pos) {
            this.idx = idx;
            this.pos = pos;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter   pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));

        StringTokenizer st = new StringTokenizer(br.readLine());
        int n = Integer.parseInt(st.nextToken());
        int q = Integer.parseInt(st.nextToken());

        // Read monster levels a[1..n].
        int[] a = new int[n+1];
        st = new StringTokenizer(br.readLine());
        for (int i = 1; i <= n; i++) {
            a[i] = Integer.parseInt(st.nextToken());
        }

        // Group queries by their k.
        @SuppressWarnings("unchecked")
        List<Query>[] byK = new ArrayList[n+1];
        for (int k = 1; k <= n; k++) {
            byK[k] = new ArrayList<>();
        }

        // We'll store answers in ans[0..q-1].
        boolean[] ans = new boolean[q];

        // Read queries
        for (int qi = 0; qi < q; qi++) {
            st = new StringTokenizer(br.readLine());
            int pos = Integer.parseInt(st.nextToken());
            int k   = Integer.parseInt(st.nextToken());
            byK[k].add(new Query(qi, pos));
        }

        // For each distinct k that actually has queries, do one O(n)-pass simulation.
        for (int k = 1; k <= n; k++) {
            if (byK[k].isEmpty()) continue;

            // Sort those queries by the monster position.
            List<Query> list = byK[k];
            Collections.sort(list, (x,y)->Integer.compare(x.pos, y.pos));

            int f = 0;   // how many fights we've done so far
            int qptr = 0; // pointer into the sorted queries

            // Walk through monsters 1..n
            for (int i = 1; i <= n; i++) {
                // Before we fight/flee the i-th monster, our "fight-count" is f,
                // so our level is L = 1 + (f / k).  We fight exactly if f < k * a[i].
                while (qptr < list.size() && list.get(qptr).pos == i) {
                    Query qu = list.get(qptr++);
                    ans[qu.idx] = (f < (long)k * a[i]);
                }

                // Now actually do the fight or not, to update f for the next monster.
                if (f < (long)k * a[i]) {
                    f++;
                }
            }
        }

        // Print answers in the input order
        for (int i = 0; i < q; i++) {
            pw.println(ans[i] ? "YES" : "NO");
        }
        pw.flush();
    }
}