import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            int n = Integer.parseInt(in.readLine().trim());
            StringTokenizer st = new StringTokenizer(in.readLine());
            long[] a = new long[n];
            long sum = 0;
            for (int i = 0; i < n; i++) {
                a[i] = Long.parseLong(st.nextToken());
                sum += a[i];
            }
            long r = sum % n;
            if (r == 0) {
                // Sum is divisible by n, so we would need 0 operations.
                // That's only possible if they are already all equal.
                boolean allEqual = true;
                for (int i = 1; i < n; i++) {
                    if (a[i] != a[0]) {
                        allEqual = false;
                        break;
                    }
                }
                out.println(allEqual ? 0 : -1);
            } else {
                // sum mod n != 0, we take x = floor(sum/n), and must make exactly r moves.
                out.println(r);
            }
        }
        out.flush();
    }
}