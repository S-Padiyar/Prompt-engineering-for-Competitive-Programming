import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // Fast I/O via BufferedReader
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            // Read n and k
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int k = Integer.parseInt(st.nextToken());

            // Read array a[] for G1
            st = new StringTokenizer(br.readLine());
            int sumA = 0;
            for (int i = 0; i < n; i++) {
                sumA += Integer.parseInt(st.nextToken());
            }

            // Skip G1's m1 edges
            int m1 = Integer.parseInt(br.readLine().trim());
            for (int i = 0; i < m1; i++) {
                br.readLine();
            }

            // Read array b[] for G2
            st = new StringTokenizer(br.readLine());
            int sumB = 0;
            for (int i = 0; i < n; i++) {
                sumB += Integer.parseInt(st.nextToken());
            }

            // Skip G2's m2 edges
            int m2 = Integer.parseInt(br.readLine().trim());
            for (int i = 0; i < m2; i++) {
                br.readLine();
            }

            // Condition 1: total outgoing = n?
            if (sumA + sumB != n) {
                sb.append("NO\n");
                continue;
            }

            // Condition 2 & 3:
            // If k==2, any even cycle is fine; otherwise we must have all outgoing in one graph.
            if (k == 2) {
                sb.append("YES\n");
            } else {
                // For k>2, we must avoid 2-cycles in the cross-graph matching:
                // so either sumA==0 (all outgoing in G2) or sumB==0 (all outgoing in G1).
                if (sumA == 0 || sumB == 0) {
                    sb.append("YES\n");
                } else {
                    sb.append("NO\n");
                }
            }
        }

        // Output all answers
        System.out.print(sb.toString());
    }
}