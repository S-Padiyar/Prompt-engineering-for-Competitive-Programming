import java.io.*;
import java.util.StringTokenizer;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder out = new StringBuilder();
        int t = Integer.parseInt(in.readLine().trim());
        
        // We will reuse arrays up to the maximum total N = 200000.
        int MAXN = 200000;
        long[] a = new long[MAXN + 5];
        int[] B = new int[MAXN + 5];
        int[] nxt = new int[MAXN + 5];
        long[] depth = new long[MAXN + 5];
        
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            long x = Long.parseLong(st.nextToken());
            
            st = new StringTokenizer(in.readLine());
            for (int i = 1; i <= n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }
            
            // 1) Two‐pointer to compute B[i]:
            //    B[i] = first j >= i with sum(a[i..j]) > x, or INF (we'll use n+1) otherwise.
            long sum = 0;
            int r = 1;  // window is [i .. r-1], sum = sum of that window
            for (int i = 1; i <= n; i++) {
                // Expand r while we can keep sum + a[r] <= x
                while (r <= n && sum + a[r] <= x) {
                    sum += a[r];
                    r++;
                }
                // Now either r>n or sum + a[r] > x
                if (r <= n) {
                    B[i] = r;
                } else {
                    B[i] = n + 1;  // "no crossing"
                }
                // Slide the window: remove a[i] if it's inside
                if (r > i) {
                    sum -= a[i];
                } else {
                    // r == i means we never included a[i] (a[i] itself > x)
                    // so move r forward
                    r = i + 1;
                }
            }
            
            // 2) Build nxt[i]
            for (int i = 1; i <= n; i++) {
                if (B[i] <= n) nxt[i] = B[i] + 1;
                else           nxt[i] = 0;
            }
            
            // 3) DP depth from the right
            depth[n+1] = 0;
            for (int i = n; i >= 1; i--) {
                if (nxt[i] == 0) {
                    depth[i] = 0;
                } else {
                    depth[i] = 1 + depth[nxt[i]];
                }
            }
            
            // 4) sum total_bad
            long totalBad = 0;
            for (int i = 1; i <= n; i++) {
                totalBad += depth[i];
            }
            
            // 5) answer = total_subsegments - total_bad
            long totalSub = (long) n * (n + 1) / 2;
            long answer = totalSub - totalBad;
            
            out.append(answer).append('\n');
        }
        
        System.out.print(out);
    }
}