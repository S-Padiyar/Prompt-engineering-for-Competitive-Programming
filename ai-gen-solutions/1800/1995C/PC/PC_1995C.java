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
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            // prevLog = ln of the previous array value b[i-1].
            double prevLog = Math.log(a[0]);
            long ops = 0;
            boolean ok = true;

            for (int i = 1; i < n; i++) {
                double curLog = Math.log(a[i]);
                // If already non-decreasing, no ops needed
                if (curLog + EPS >= prevLog) {
                    prevLog = curLog;
                } else {
                    // If a[i]==1 but prevLog>0 => impossible
                    if (a[i] == 1) {
                        ok = false;
                        break;
                    }
                    // Otherwise keep squaring a[i] until its log >= prevLog
                    double tlog = curLog;
                    int cnt = 0;
                    while (tlog + EPS < prevLog) {
                        tlog *= 2.0;
                        cnt++;
                    }
                    ops += cnt;
                    prevLog = tlog;
                }
            }

            out.println(ok ? ops : -1);
        }

        out.flush();
    }
}