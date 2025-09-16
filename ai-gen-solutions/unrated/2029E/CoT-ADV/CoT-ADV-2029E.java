import java.io.*;
import java.util.*;

public class Main {
    static final int MAX = 400_000;
    // divisors[u] = all d >= 2 such that d | u
    static ArrayList<Integer>[] divisors = new ArrayList[MAX+1];

    public static void main(String[] args) throws IOException {
        // Precompute divisors >= 2 for every number up to MAX
        for(int i = 0; i <= MAX; i++) {
            divisors[i] = new ArrayList<>();
        }
        for(int d = 2; d <= MAX; d++) {
            for(int mult = d; mult <= MAX; mult += d) {
                divisors[mult].add(d);
            }
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter pw = new PrintWriter(System.out);
        int t = Integer.parseInt(br.readLine().trim());
        while(t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            String[] parts = br.readLine().split(" ");
            int[] a = new int[n];
            for(int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(parts[i]);
            }
            if(n == 1) {
                // Trivially choose x = a[0] itself
                pw.println(a[0]);
                continue;
            }
            // find min and max
            int mn = a[0], mx = a[0];
            for(int v: a) {
                if(v < mn) mn = v;
                if(v > mx) mx = v;
            }
            // Gather all divisors of mn (>=2) as candidates
            ArrayList<Integer> cand = divisors[mn];
            int K = cand.size();  // number of candidates
            // Prepare a fast lookup: which values are in the input?
            boolean[] isTarget = new boolean[mx+1];
            for(int v: a) isTarget[v] = true;

            // Bit‐mask width: how many 64‐bit words to hold K bits?
            int W = (K + 63) >>> 6;
            // dp[u][w] is a flattened array of longs: size (mx+1)*W
            long[] dp = new long[(mx+1)*W];

            // Initially mark dp[xᵢ] bit i = 1
            for(int i = 0; i < K; i++) {
                int x = cand.get(i);
                int idx = i >>> 6;
                int pos = i & 63;
                dp[x*W + idx] |= (1L << pos);
            }

            // valid[i] = is candidate i still alive (can reach all seen targets)
            boolean[] valid = new boolean[K];
            Arrays.fill(valid, true);
            int validCount = K;

            // Sweep from u=2..mx, propagating reachability
            mainLoop: 
            for(int u = 2; u <= mx; u++) {
                // Fetch the bitset for dp[u]
                int base = u*W;
                boolean any = false;
                for(int w = 0; w < W; w++) {
                    if(dp[base + w] != 0L) { any = true; break; }
                }
                if(!any) continue;  // no candidate can reach u

                // If u is one of our targets, disqualify those candidates
                if(isTarget[u]) {
                    long maskWord;
                    for(int i = 0; i < K; i++) {
                        if(!valid[i]) continue;
                        int w = i >>> 6, b = i & 63;
                        maskWord = dp[base + w];
                        if(((maskWord >> b) & 1L) == 0L) {
                            valid[i] = false;
                            validCount--;
                            if(validCount == 0) {
                                pw.println(-1);
                                break mainLoop;
                            }
                        }
                    }
                }
                // Propagate dp[u] to dp[u + d] for each divisor d of u
                for(int d: divisors[u]) {
                    int v = u + d;
                    if(v > mx) continue;
                    int vb = v*W;
                    // OR the W‐word bitmask
                    for(int w = 0; w < W; w++) {
                        dp[vb + w] |= dp[base + w];
                    }
                }
            }

            // If we fell out normally and validCount>0, pick any surviving candidate
            if(validCount > 0) {
                for(int i = 0; i < K; i++) {
                    if(valid[i]) {
                        pw.println(cand.get(i));
                        break;
                    }
                }
            }
            // Clear isTarget for next test
            for(int v: a) isTarget[v] = false;
        }
        pw.close();
    }
}