import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int x = Integer.parseInt(st.nextToken());

            int[] a = new int[n];
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            // 1) Compute all divisors of x
            List<Integer> divs = new ArrayList<>();
            for (int d = 1; d * (long)d <= x; d++) {
                if (x % d == 0) {
                    divs.add(d);
                    if (d * d != x) {
                        divs.add(x / d);
                    }
                }
            }
            Collections.sort(divs);
            int m = divs.size();

            // 2) Map each divisor -> its index
            Map<Integer,Integer> divIndex = new HashMap<>();
            for (int i = 0; i < m; i++) {
                divIndex.put(divs.get(i), i);
            }
            int idx1 = divIndex.get(1);
            int idxX = divIndex.get(x);

            // 3) DP array for the "current segment"
            boolean[] reachable = new boolean[m];
            reachable[idx1] = true;  // we can always form product = 1 by choosing nothing

            int segments = 1;  // we will form at least one segment

            // 4) Process each card
            for (int val : a) {
                // If val does not divide x, it cannot help form x -> skip
                if (x % val != 0) {
                    continue;
                }
                int g = val;          // this card's “weight”
                int gIdx = divIndex.get(g);

                // Try merging this card into the current segment
                boolean[] nextReachable = reachable.clone();
                for (int j = 0; j < m; j++) {
                    if (!reachable[j]) continue;
                    long prod = divs.get(j) * (long)g;
                    if (prod <= x && x % prod == 0) {
                        int idx = divIndex.get((int)prod);
                        nextReachable[idx] = true;
                    }
                }

                // If we now can form x, this segment has become GOOD => we must cut
                if (nextReachable[idxX]) {
                    segments++;
                    // Reset DP for a new segment
                    reachable = new boolean[m];
                    reachable[idx1] = true;
                    // Re‐process this card in the fresh DP
                    if (g > 1) {
                        reachable[gIdx] = true;
                    }
                } else {
                    // Safe to accept this card in the current segment
                    reachable = nextReachable;
                }
            }

            sb.append(segments).append('\n');
        }

        System.out.print(sb);
    }
}