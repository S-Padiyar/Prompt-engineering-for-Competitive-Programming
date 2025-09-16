import java.io.*;
import java.util.*;

public class Main {
    static final long BASE = 91138233;
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter  pw = new PrintWriter(new OutputStreamWriter(System.out));
        int T = Integer.parseInt(br.readLine());
        
        // We will need powers of BASE up to the maximum total length ≤ 3e5
        int MAXN = 300_000 + 5;
        long[] pow = new long[MAXN];
        pow[0] = 1;
        for (int i = 1; i < MAXN; i++) {
            pow[i] = pow[i - 1] * BASE;  // mod 2^64 automatically
        }
        
        while (T-- > 0) {
            String s = br.readLine();
            int n = s.length();
            
            // Collect positions of non‐'a' characters
            List<Integer> posList = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if (s.charAt(i) != 'a') {
                    posList.add(i);
                }
            }
            int m = posList.size();
            
            // Case 1: all 'a'
            if (m == 0) {
                // any t = "aa", "aaa", ..., "a"*n  (n−1 choices)
                pw.println(n - 1);
                continue;
            }
            
            // Build rolling hash of s
            long[] h = new long[n + 1];
            h[0] = 0;
            for (int i = 0; i < n; i++) {
                h[i + 1] = h[i] * BASE + (s.charAt(i));
            }
            
            // convenience to binary‐search on the posList
            int[] p = new int[m];
            for (int i = 0; i < m; i++) p[i] = posList.get(i);
            
            int x = p[0], y = p[m - 1];
            long A = (long)(x + 1) * (long)(n - y);  // one‐block solutions
            
            // Now the multi‐block solutions for L < d
            int d = y - x + 1;
            long B = 0;
            
            // for each L=1..d−1, try the greedy covering
            coverLoop:
            for (int L = 1; L < d; L++) {
                int idx = 0;
                int coveredEnd = -1;
                
                // We'll record the hash of the first block to compare
                long firstHash = 0;
                boolean     isFirst = true;
                
                while (idx < m) {
                    int pk = p[idx];
                    // we must start block so as to cover pk
                    int start = Math.max(coveredEnd + 1, pk - L + 1);
                    // if it doesn't actually cover pk or overruns the string, fail
                    if (start > pk || start + L > n) {
                        continue coverLoop;
                    }
                    // compute hash of s[start..start+L-1]
                    long thisHash = h[start + L] - h[start] * pow[L];
                    if (isFirst) {
                        firstHash = thisHash;
                        isFirst = false;
                    } else {
                        if (thisHash != firstHash) {
                            continue coverLoop;
                        }
                    }
                    // update covered-end
                    coveredEnd = start + L - 1;
                    // skip all p's now covered
                    // i.e. find first p[?]>coveredEnd
                    int lo = idx + 1, hi = m;
                    while (lo < hi) {
                        int mid = (lo + hi) >>> 1;
                        if (p[mid] <= coveredEnd) lo = mid + 1;
                        else                hi = mid;
                    }
                    idx = lo;
                }
                // if we exhausted all non-'a' positions, it's a valid L
                B++;
            }
            
            pw.println(A + B);
        }
        
        pw.flush();
    }
}