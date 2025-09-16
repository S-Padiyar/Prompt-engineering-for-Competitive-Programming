import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader in;
    static StringBuilder out = new StringBuilder();
    static StringTokenizer tok;
    
    static int nextInt() throws IOException {
        while (tok == null || !tok.hasMoreTokens()) {
            String line = in.readLine();
            if (line == null) return -1;
            tok = new StringTokenizer(line);
        }
        return Integer.parseInt(tok.nextToken());
    }
    
    public static void main(String[] args) throws IOException {
        in = new BufferedReader(new InputStreamReader(System.in));
        int t = nextInt();
        while (t-- > 0) {
            int n = nextInt(), q = nextInt();
            int[] a = new int[n];
            int maxVal = 0;
            for (int i = 0; i < n; i++) {
                a[i] = nextInt();
                if (a[i] > maxVal) maxVal = a[i];
            }
            // median position m = (n+2)/2  (1-based)
            int m = (n + 2) >> 1;
            
            // Build frequency of original a[i], and prefix-sum pf[]
            int[] freq = new int[maxVal + 1];
            for (int v : a) freq[v]++;
            int[] pf = new int[maxVal + 1];
            pf[0] = freq[0];
            for (int i = 1; i <= maxVal; i++) {
                pf[i] = pf[i - 1] + freq[i];
            }
            // If x > maxVal, the array mod x is just the original array,
            // so its median is the original median.  We read it off pf[].
            int originalMedian = 0;
            for (int i = 0; i <= maxVal; i++) {
                if (pf[i] >= m) {
                    originalMedian = i;
                    break;
                }
            }
            
            // We'll cache answers in ans[x], -1 meaning "not yet computed"
            int[] ans = new int[n + 1];
            Arrays.fill(ans, -1);
            
            // Precompute for small x <= B by direct counting a_i % x
            // B = ~ sqrt(maxVal)
            int B = (int)Math.sqrt(maxVal) + 1;
            if (B > n) B = n;  // no need to exceed n
            for (int x = 1; x <= B; x++) {
                int[] cnt = new int[x];
                for (int v : a) {
                    cnt[v % x]++;
                }
                int sum = 0;
                for (int r = 0; r < x; r++) {
                    sum += cnt[r];
                    if (sum >= m) {
                        ans[x] = r;
                        break;
                    }
                }
            }
            
            // Helper to compute S_x(v) = # of i with (a_i mod x) <= v
            // in O(maxVal/x) by using pf[]
            class Checker {
                int x;
                Checker(int x) { this.x = x; }
                // return true if S_x(v) >= m
                boolean ok(int v) {
                    int sum = 0;
                    for (int k = 0; ; k++) {
                        int L = k * x;
                        if (L > maxVal) break;
                        int R = L + v;
                        if (R > maxVal) R = maxVal;
                        if (L == 0)
                            sum += pf[R];
                        else
                            sum += pf[R] - pf[L - 1];
                        if (sum >= m) return true;
                    }
                    return false;
                }
            }
            
            // Process queries
            while (q-- > 0) {
                int x = nextInt();
                if (x <= B) {
                    // precomputed
                    out.append(ans[x]).append(' ');
                } else if (x > maxVal) {
                    // trivial: same as original median
                    out.append(originalMedian).append(' ');
                } else {
                    // we may need to binary-search the remainder
                    if (ans[x] == -1) {
                        Checker c = new Checker(x);
                        int lo = 0, hi = x - 1;
                        while (lo < hi) {
                            int mid = (lo + hi) >> 1;
                            if (c.ok(mid)) hi = mid;
                            else lo = mid + 1;
                        }
                        ans[x] = lo;
                    }
                    out.append(ans[x]).append(' ');
                }
            }
            out.append('\n');
        }
        System.out.print(out);
    }
}