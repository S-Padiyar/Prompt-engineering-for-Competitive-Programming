import java.io.*;
import java.util.*;

public class Main {
    static class Event {
        long pos;
        int delta;
        Event() {}
        Event(long p, int d) { pos = p; delta = d; }
    }
    
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(in.readLine());
        int T = Integer.parseInt(st.nextToken());
        StringBuilder sb = new StringBuilder();
        
        // Process each test case
        while (T-- > 0) {
            st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            long m = Long.parseLong(st.nextToken());
            int k = Integer.parseInt(st.nextToken());
            
            long[] h = new long[n];
            long[] x = new long[n];
            st = new StringTokenizer(in.readLine());
            long hMax = 0;
            for (int i = 0; i < n; i++) {
                h[i] = Long.parseLong(st.nextToken());
                hMax = Math.max(hMax, h[i]);
            }
            st = new StringTokenizer(in.readLine());
            for (int i = 0; i < n; i++) {
                x[i] = Long.parseLong(st.nextToken());
            }
            
            // Prepare event buffer (2 events per interval at most)
            Event[] events = new Event[2 * n];
            for (int i = 0; i < 2*n; i++) {
                events[i] = new Event();
            }
            
            // Binary search on t = number of attacks
            long left = 1, right = hMax, answer = -1;
            while (left <= right) {
                long mid = (left + right) >>> 1;
                if (canKillK(mid, n, m, k, h, x, events)) {
                    answer = mid;
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            }
            
            sb.append(answer).append('\n');
        }
        
        System.out.print(sb.toString());
    }
    
    /**
     * Checks if, with t = 'attacks', we can choose a position p
     * that kills at least k enemies.
     */
    static boolean canKillK(long t, int n, long m, int k,
                            long[] h, long[] x, Event[] events) {
        int evCnt = 0;
        
        // Build intervals [L, R] in which p must lie to kill each enemy
        for (int i = 0; i < n; i++) {
            // Minimum per-hit damage needed to kill i in t hits:
            long needed = (h[i] + t - 1) / t;  // ceil(h[i]/t)
            if (needed > m) {
                // Cannot kill this enemy in t hits
                continue;
            }
            long radius = m - needed;  // allowed |p - x[i]| <= radius
            long L = x[i] - radius;
            long R = x[i] + radius;
            // We do +1 at L, and -1 at R+1 for an inclusive range [L..R].
            events[evCnt].pos = L;
            events[evCnt++].delta = +1;
            events[evCnt].pos = R + 1;
            events[evCnt++].delta = -1;
        }
        
        // If fewer than k intervals are even alive, impossible
        if (evCnt / 2 < k) return false;
        
        // Sort events by position
        Arrays.sort(events, 0, evCnt, new Comparator<Event>() {
            public int compare(Event a, Event b) {
                return Long.compare(a.pos, b.pos);
            }
        });
        
        // Sweep‐line: accumulate deltas, look for a point with sum >= k
        int curr = 0;
        int idx = 0;
        while (idx < evCnt) {
            long currPos = events[idx].pos;
            int sumDelta = 0;
            // process all events at the same position
            while (idx < evCnt && events[idx].pos == currPos) {
                sumDelta += events[idx].delta;
                idx++;
            }
            curr += sumDelta;
            if (curr >= k) {
                return true;
            }
        }
        
        return false;
    }
}