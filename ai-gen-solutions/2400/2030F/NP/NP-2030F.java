import java.io.*;
import java.util.*;

public class Main {
    static final int INF = 1000000007;
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter   pw = new PrintWriter(new OutputStreamWriter(System.out));
        int T = Integer.parseInt(br.readLine().trim());
        while (T-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int q = Integer.parseInt(st.nextToken());
            
            // Read the array
            int[] a = new int[n];
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }
            
            // 1) Build next-occurrence array
            int[] nxt  = new int[n];
            int[] last = new int[n+1];      // values are 1..n
            Arrays.fill(last, -1);
            for (int i = n-1; i >= 0; i--) {
                int v = a[i];
                nxt[i] = last[v];
                last[v] = i;
            }
            
            // 2) Collect the "base" intervals [i, nxt[i]]
            //    (only where nxt[i] != -1)
            List<Interval> intervals = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if (nxt[i] != -1) {
                    intervals.add(new Interval(i, nxt[i]));
                }
            }
            
            // Sort intervals in descending order of 'r'
            intervals.sort((A,B) -> Integer.compare(B.r, A.r));
            
            // Build an iterative segment tree for range-min on starts;
            // size = next power of two ≥ n
            int size = 1;
            while (size < n) size <<= 1;
            int[] seg = new int[2*size];
            Arrays.fill(seg, INF);
            
            // best[i] = minimal 'badR' for an interval that starts exactly at i
            int[] best = new int[n];
            Arrays.fill(best, INF);
            
            // We'll sweep intervals by descending r1, and
            // maintain a pointer p2 into the same array (also descending r2).
            int p2 = 0;
            
            for (Interval I1 : intervals) {
                int l1 = I1.l, r1 = I1.r;
                
                // Insert into the seg-tree all intervals whose r2 > r1
                while (p2 < intervals.size()
                       && intervals.get(p2).r > r1)
                {
                    int L2 = intervals.get(p2).l;
                    int R2 = intervals.get(p2).r;
                    // point-update at L2 -> value = R2
                    int pos = L2 + size;
                    seg[pos] = R2;
                    for (pos >>= 1; pos > 0; pos >>= 1) {
                        seg[pos] = Math.min(seg[2*pos], seg[2*pos+1]);
                    }
                    p2++;
                }
                
                // Query the segment-tree on (l1, r1) => [l1+1 .. r1-1] in 0-based
                if (l1 + 1 <= r1 - 1) {
                    int lo = l1+1 + size, hi = r1-1 + size;
                    int v = INF;
                    while (lo <= hi) {
                        if ((lo & 1) == 1) { v = Math.min(v, seg[lo]); lo++; }
                        if ((hi & 1) == 0) { v = Math.min(v, seg[hi]); hi--; }
                        lo >>= 1;  hi >>= 1;
                    }
                    if (v < INF) {
                        best[l1] = Math.min(best[l1], v);
                    }
                }
            }
            
            // 3) Build forbidR[L] = min(best[L], best[L+1], …, best[n-1])
            int[] forbidR = new int[n];
            int running = INF;
            for (int i = n-1; i >= 0; i--) {
                running = Math.min(running, best[i]);
                forbidR[i] = running;
            }
            
            // 4) Finally R[L] = (forbidR[L] == INF ? n-1 : forbidR[L]-1)
            int[] R = new int[n];
            for (int i = 0; i < n; i++) {
                if (forbidR[i] == INF) R[i] = n-1;
                else                  R[i] = forbidR[i] - 1;
            }
            
            // 5) Answer queries in O(1)
            while (q-- > 0) {
                st = new StringTokenizer(br.readLine());
                int L = Integer.parseInt(st.nextToken()) - 1;
                int Rq= Integer.parseInt(st.nextToken()) - 1;
                pw.println(R[L] >= Rq ? "YES" : "NO");
            }
        }
        pw.flush();
    }
    
    static class Interval {
        int l, r;
        Interval(int _l, int _r) { l=_l;  r=_r; }
    }
}

best[ℓ1] = min(best[ℓ1], badR);