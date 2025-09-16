import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        
        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();
        
        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            long[] a = new long[n];
            st = new StringTokenizer(br.readLine());
            long sum = 0;
            for (int i = 0; i < n; i++) {
                a[i] = Long.parseLong(st.nextToken());
                sum += a[i];
            }
            
            // k = floor(sum / n), number of operations needed = sum - n*k
            long k = sum / n;
            long needed = sum - k * n;  // this is >= 0
            
            // Build c[i] = a[i] - k
            long[] c = new long[n];
            for (int i = 0; i < n; i++) {
                c[i] = a[i] - k;
            }
            
            // Find a start index s using the "gas station" trick
            long curr = 0;
            int start = 0;
            for (int i = 0; i < n; i++) {
                curr += c[i];
                if (curr < 0) {
                    curr = 0;
                    start = i + 1;
                }
            }
            if (start >= n) start = 0;  // wrap if needed
            
            // One pass simulation
            long prev = 0;
            long usedOps = 0;
            boolean ok = true;
            for (int tpos = 0; tpos < n; tpos++) {
                int idx = (start + tpos) % n;
                long cp = c[idx] + prev;
                if (cp < 0) {
                    ok = false;
                    break;
                }
                long ops = cp / 2;      // how many "–2 here, +1 next" we do
                usedOps += ops;
                prev = ops;             // that many +1's carry to the next slot
            }
            
            if (!ok || usedOps != needed) {
                sb.append("-1\n");
            } else {
                sb.append(usedOps).append('\n');
            }
        }
        
        System.out.print(sb);
    }
}