import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer tok = new StringTokenizer(in.readLine());
        int n = Integer.parseInt(tok.nextToken());
        int m = Integer.parseInt(tok.nextToken());

        // Read a_i
        tok = new StringTokenizer(in.readLine());
        int[] a = new int[n];
        int maxA = 0;
        for (int i = 0; i < n; i++) {
            a[i] = Integer.parseInt(tok.nextToken());
            if (a[i] > maxA) maxA = a[i];
        }

        // Read b_i and form d_i = a_i - b_i
        tok = new StringTokenizer(in.readLine());
        final int INF = 0x3f3f3f3f;
        int[] minDeltaAtA = new int[maxA + 1];
        Arrays.fill(minDeltaAtA, INF);

        int amin = maxA;
        for (int i = 0; i < n; i++) {
            int b = Integer.parseInt(tok.nextToken());
            int d = a[i] - b;
            // record the minimal d at index a[i]
            if (d < minDeltaAtA[a[i]]) {
                minDeltaAtA[a[i]] = d;
            }
            if (a[i] < amin) amin = a[i];
        }

        // Build bestDelta[x] = min net-cost of any class with a_i <= x
        int[] bestDelta = new int[maxA + 1];
        int cur = INF;
        for (int x = 0; x <= maxA; x++) {
            if (minDeltaAtA[x] < cur) {
                cur = minDeltaAtA[x];
            }
            bestDelta[x] = cur;
        }

        // Build the "breakpoints" pos[] where bestDelta changes, starting at pos[0]=amin
        // We'll have an extra pos at maxA+1 to mark the end.
        ArrayList<Integer> posList = new ArrayList<>(1000000);
        ArrayList<Integer> deltaList = new ArrayList<>(1000000);

        // We only care about x >= amin (below that no craft+melts are possible).
        int last = INF;
        for (int x = amin; x <= maxA; x++) {
            if (bestDelta[x] != last) {
                posList.add(x);
                deltaList.add(bestDelta[x]);
                last = bestDelta[x];
            }
        }
        // Finally mark the end
        posList.add(maxA + 1);
        // We will have posList.size() = #breakpoints+1, deltaList.size() = #breakpoints

        // Copy into arrays
        int S = posList.size();
        int[] pos = new int[S];
        for (int i = 0; i < S; i++) pos[i] = posList.get(i);
        int segCount = S - 1; // we have segCount segments, indexed 0..segCount-1
        int[] dseg = new int[segCount];
        for (int i = 0; i < segCount; i++) dseg[i] = deltaList.get(i);

        // Now process each metal-type c_j
        tok = new StringTokenizer(in.readLine());
        long ans = 0;

        for (int j = 0; j < m; j++) {
            long x = Long.parseLong(tok.nextToken());
            if (x < amin) {
                // Not enough for ANY craft+melts; we can only do final crafts of size 'amin'
                ans += x / amin;
                continue;
            }
            // find the segment k where pos[k] <= x < pos[k+1]
            // binary search among [0..segCount-1]
            int lo = 0, hi = segCount - 1, k = 0;
            while (lo <= hi) {
                int mid = (lo + hi) >>> 1;
                if (pos[mid] <= x) {
                    k = mid;
                    lo = mid + 1;
                } else {
                    hi = mid - 1;
                }
            }
            long xp = 0;
            // repeatedly do bulk cycles in each segment
            while (x >= amin) {
                int d = dseg[k];
                int bound = pos[k];
                // we can do t = floor((x - bound)/d) + 1 cycles of this best class
                long t = (x - bound) / d + 1;
                xp += 2L * t;
                x  -= t * d;
                // now drop to the segment that covers the new x
                // i.e. the largest index k' < k with pos[k'] <= x
                while (k > 0 && x < pos[k]) k--;
                // if x<amin we'll exit
            }
            // final crafts of size amin
            xp += x / amin;
            ans += xp;
        }

        // Print the total
        System.out.println(ans);
    }
}