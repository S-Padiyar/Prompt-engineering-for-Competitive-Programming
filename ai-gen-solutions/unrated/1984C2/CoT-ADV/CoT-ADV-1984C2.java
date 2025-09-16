import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 998244353;
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer tok = new StringTokenizer(in.readLine());
        int t = Integer.parseInt(tok.nextToken());
        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            tok = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(tok.nextToken());
            tok = new StringTokenizer(in.readLine());
            
            // We'll maintain at most two endpoints.
            // Each endpoint: value v, count cnt ways to get exactly v.
            long[] vals = new long[2];
            long[] cnts = new long[2];
            int size = 1;
            vals[0] = 0;
            cnts[0] = 1;

            for (int i = 0; i < n; i++) {
                long ai = Long.parseLong(tok.nextToken());
                // Up to 4 new candidate pairs:
                long[] candV = new long[4];
                long[] candC = new long[4];
                int csz = 0;

                // Generate from each existing endpoint
                for (int j = 0; j < size; j++) {
                    long vprev = vals[j];
                    long ways = cnts[j];

                    long t1 = vprev + ai;
                    long t2 = t1 >= 0 ? t1 : -t1;  // |t1|

                    candV[csz] = t1;
                    candC[csz] = ways;
                    csz++;

                    candV[csz] = t2;
                    candC[csz] = ways;
                    csz++;
                }

                // Merge duplicates on the fly and track global min/max
                long newLow = Long.MAX_VALUE, newHigh = Long.MIN_VALUE;
                long waysLow = 0, waysHigh = 0;

                for (int j = 0; j < csz; j++) {
                    long v = candV[j];
                    long w = candC[j] % MOD;
                    // Update min
                    if (v < newLow) {
                        newLow = v;
                        waysLow = w;
                    } else if (v == newLow) {
                        waysLow = (waysLow + w) % MOD;
                    }
                    // Update max
                    if (v > newHigh) {
                        newHigh = v;
                        waysHigh = w;
                    } else if (v == newHigh) {
                        waysHigh = (waysHigh + w) % MOD;
                    }
                }

                // Prepare for next iteration
                if (newLow == newHigh) {
                    size = 1;
                    vals[0] = newLow;
                    cnts[0] = waysLow;  // same as waysHigh
                } else {
                    size = 2;
                    vals[0] = newLow; cnts[0] = waysLow;
                    vals[1] = newHigh; cnts[1] = waysHigh;
                }
            }

            // At the end, the answer is the count for the largest endpoint
            long answer;
            if (size == 1) {
                answer = cnts[0] % MOD;
            } else {
                // vals[1] is the max (we kept them sorted)
                answer = cnts[1] % MOD;
            }
            sb.append(answer).append('\n');
        }

        System.out.print(sb);
    }
}