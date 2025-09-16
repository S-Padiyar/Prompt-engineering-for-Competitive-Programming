import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(in.readLine());
        int n = Integer.parseInt(st.nextToken());
        int m = Integer.parseInt(st.nextToken());

        // Read a[i], b[i].
        int[] a = new int[n];
        st = new StringTokenizer(in.readLine());
        int maxA = 0;
        for (int i = 0; i < n; i++) {
            a[i] = Integer.parseInt(st.nextToken());
            if (a[i] > maxA) maxA = a[i];
        }
        int[] b = new int[n];
        st = new StringTokenizer(in.readLine());
        for (int i = 0; i < n; i++) {
            b[i] = Integer.parseInt(st.nextToken());
        }

        // bestD[x] = minimal d = a[i]-b[i] among classes with a[i]==x
        final int INF = Integer.MAX_VALUE / 2;
        int[] bestD = new int[maxA + 1];
        Arrays.fill(bestD, INF);
        for (int i = 0; i < n; i++) {
            int d = a[i] - b[i];
            int ai = a[i];
            if (d < bestD[ai]) {
                bestD[ai] = d;
            }
        }

        // Build prefix minima prefD[x] = min(bestD[1..x]),
        // and also bestA[x] = the a-value that attained it.
        int[] prefD = new int[maxA + 1];
        int[] bestA = new int[maxA + 1];
        prefD[0] = INF;
        bestA[0] = -1;
        int curMinD = INF, curBestA = -1;
        for (int x = 1; x <= maxA; x++) {
            if (bestD[x] < curMinD) {
                curMinD = bestD[x];
                curBestA = x;
            }
            prefD[x] = curMinD;
            bestA[x] = curBestA;
        }

        // Now process each metal type.
        st = new StringTokenizer(in.readLine());
        long answer = 0L;
        for (int j = 0; j < m; j++) {
            long C = Long.parseLong(st.nextToken());
            long expGained = 0L;

            // Greedy loop
            while (true) {
                // Clamp index into [0..maxA]
                int x = (C > maxA ? maxA : (int) C);
                int d = prefD[x];
                int ai = bestA[x];
                if (d >= INF || ai < 0 || C < ai) {
                    // No further cycle can be done.
                    break;
                }
                // How many cycles with (ai, d)?
                long t = (C - ai) / d + 1;
                expGained += 2L * t;
                C -= t * d;
            }

            answer += expGained;
        }

        // Output the total EXP.
        System.out.println(answer);
    }
}