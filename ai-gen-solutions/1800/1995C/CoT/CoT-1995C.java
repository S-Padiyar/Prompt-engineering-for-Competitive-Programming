import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(br.readLine().trim());
        final double EPS = 1e-12;

        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            StringTokenizer st = new StringTokenizer(br.readLine());
            long[] a = new long[n];
            double[] L = new double[n];

            for (int i = 0; i < n; i++) {
                a[i] = Long.parseLong(st.nextToken());
                L[i] = Math.log(a[i]);
            }

            long ans = 0;
            boolean ok = true;

            // Process from left to right
            for (int i = 1; i < n; i++) {
                // If already non-decreasing, continue
                if (L[i] + EPS >= L[i - 1]) {
                    continue;
                }
                // If a[i] was 1, squaring won't help
                if (a[i] == 1) {
                    ok = false;
                    break;
                }
                // Otherwise, keep squaring (doubling log) until it is big enough
                int cnt = 0;
                double curLog = L[i];
                while (curLog + EPS < L[i - 1]) {
                    curLog *= 2.0;
                    cnt++;
                }
                ans += cnt;
                L[i] = curLog;
            }

            out.println(ok ? ans : -1);
        }
        out.flush();
    }
}