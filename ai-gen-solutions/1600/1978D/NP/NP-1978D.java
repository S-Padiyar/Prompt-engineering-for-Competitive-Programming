import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        int T = Integer.parseInt(in.readLine());
        while (T-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            long c = Long.parseLong(st.nextToken());
            long[] a = new long[n+1];
            st = new StringTokenizer(in.readLine());
            for (int i = 1; i <= n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }
            // 1) find initial winner with no exclusions
            //    each j's votes = a[j] + (j==m ? c : 0), where m=smallest index among all (i.e. 1)
            //    so only candidate 1 gets +c.  winner is the max of a[j], tie -> smaller index.
            //    we can just compute votes directly:
            long bestVotes = Long.MIN_VALUE;
            int winner = 1;
            for (int i = 1; i <= n; i++) {
                long votes = a[i];
                if (i == 1) votes += c;
                if (votes > bestVotes || (votes == bestVotes && i < winner)) {
                    bestVotes = votes;
                    winner = i;
                }
            }
            // 2) prefix sums S0[i] = c + sum_{j<i} a[j]
            long[] prefix = new long[n+1];
            prefix[0] = 0;
            for (int i = 1; i <= n; i++) {
                prefix[i] = prefix[i-1] + a[i];
            }
            long[] ans = new long[n+1];
            // We'll compute Option A for each i, but override ans[winner]=0
            // For each i != winner: build max-heap of a[j] for j>i, then greedily pop
            for (int i = 1; i <= n; i++) {
                if (i == winner) {
                    ans[i] = 0;
                    continue;
                }
                // initial U0 = c + sum_{j<i} a[j]
                long U = c + prefix[i-1];
                long base = a[i] + U;
                // build a max-heap of rivals j>i
                PriorityQueue<Long> pq = new PriorityQueue<>(Collections.reverseOrder());
                for (int j = i+1; j <= n; j++) pq.add(a[j]);
                // greedily exclude largest until no rival exceeds i's votes
                long t = 0;
                while (!pq.isEmpty() && pq.peek() > base) {
                    long x = pq.poll();
                    U += x;           // those fans become undecided -> go to i
                    base = a[i] + U;  // i's new vote total
                    t++;
                }
                // we also count i-1 exclusions for all j<i
                ans[i] = (i-1) + t;
            }
            // print
            for (int i = 1; i <= n; i++) {
                out.print(ans[i] + (i==n ? "\n" : " "));
            }
        }
        out.flush();
    }
}