import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // Fast input
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        
        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();
        
        final double EPS = 1e-12;
        
        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            long[] a = new long[n];
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }
            
            // lastLog = ln(value of the previous element after operations)
            double lastLog = Math.log(a[0]);
            long ops = 0;
            boolean impossible = false;
            
            for (int i = 1; i < n; i++) {
                double curLog = Math.log(a[i]);
                
                // If already >= last, no operations needed
                if (curLog + EPS >= lastLog) {
                    lastLog = curLog;
                    continue;
                }
                
                // If a[i] == 1 but lastLog > 0, we can never raise it
                if (a[i] == 1) {
                    impossible = true;
                    break;
                }
                
                // Otherwise, square until curLog >= lastLog
                int cnt = 0;
                while (curLog + EPS < lastLog) {
                    curLog *= 2.0;
                    cnt++;
                    // If curLog is still zero (only if a[i]==1), we can never increase
                    if (Math.abs(curLog) < EPS) {
                        impossible = true;
                        break;
                    }
                }
                if (impossible) break;
                
                ops += cnt;
                lastLog = curLog;  // Now the new "last" is a[i]^(2^cnt)
            }
            
            if (impossible) {
                sb.append(-1).append('\n');
            } else {
                sb.append(ops).append('\n');
            }
        }
        
        // Print all answers
        System.out.print(sb);
    }
}