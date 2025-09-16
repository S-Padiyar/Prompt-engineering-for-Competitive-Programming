import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder output = new StringBuilder();
        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            int[] a = new int[n];
            StringTokenizer st = new StringTokenizer(br.readLine(), " ");
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            int answer = 0;
            // First, any row with a[i] >= 5 must be row‐dyed at cost 1, and
            // it breaks any block‐placement across that boundary.
            for (int i = 0; i < n; i++) {
                if (a[i] >= 5) {
                    answer++;
                }
            }

            // Process each maximal contiguous segment where 0 <= a[i] <= 4 by DP
            int i = 0;
            while (i < n) {
                // skip pre‐dyed rows
                if (a[i] >= 5) {
                    i++;
                    continue;
                }
                // segment start
                int start = i;
                while (i < n && a[i] <= 4) {
                    i++;
                }
                int end = i - 1;  // [start..end] all have a[i]<=4

                // Precompute b[i] = ceil(a[i]/2), and eligibility of blocks between i and i+1
                int len = end - start + 1;
                int[] b = new int[len];
                boolean[] canBlockNext = new boolean[len]; 
                for (int j = 0; j < len; j++) {
                    b[j] = (a[start + j] + 1) >> 1;  // ceil(a/2)
                }
                for (int j = 0; j + 1 < len; j++) {
                    // we can place one 2×2 block between row (start+j) and (start+j+1)
                    canBlockNext[j] = (a[start + j] >= 2 && a[start + j + 1] >= 2);
                }
                canBlockNext[len - 1] = false;  // no block beyond segment end

                // dpPrev[c] = min cost up to row j-1 with prev = c (0 or 1)
                final int INF = 1_000_000_000;
                int[] dpPrev = new int[2], dpCur = new int[2];
                dpPrev[0] = 0;      // before the first row, we placed no block
                dpPrev[1] = INF;    // impossible

                // Walk through the rows in this segment
                for (int j = 0; j < len; j++) {
                    dpCur[0] = dpCur[1] = INF;
                    for (int prev = 0; prev <= 1; prev++) {
                        if (dpPrev[prev] >= INF) continue;
                        // choose c = 0 or 1 if allowed
                        for (int c = 0; c <= 1; c++) {
                            if (c == 1 && !canBlockNext[j]) continue;
                            // row‐dye needed if (prev + c) < b[j]
                            int needRowDye = (prev + c < b[j]) ? 1 : 0;
                            int cost = dpPrev[prev] + c + needRowDye;
                            if (cost < dpCur[c]) {
                                dpCur[c] = cost;
                            }
                        }
                    }
                    // shift
                    dpPrev[0] = dpCur[0];
                    dpPrev[1] = dpCur[1];
                }
                // at segment end we must have placed c=0 beyond it
                answer += dpPrev[0];
            }

            output.append(answer).append('\n');
        }

        System.out.print(output);
    }
}