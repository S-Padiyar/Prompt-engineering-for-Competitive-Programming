import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader();
        PrintWriter out = new PrintWriter(System.out);

        int t = in.nextInt();
        while (t-- > 0) {
            int n = in.nextInt();
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = in.nextInt();
            }
            Arrays.sort(a);

            int l = 0, r = n - 1;
            int last = 0;   // Alice's last eaten tastiness (initially below 1)
            int ans = 0;    // how many Alice has eaten

            while (true) {
                // Alice's move: find the smallest a[l] > last
                while (l <= r && a[l] <= last) {
                    l++;
                }
                if (l > r) {
                    // no valid cake for Alice
                    break;
                }
                // Alice eats a[l]
                last = a[l];
                ans++;
                l++;

                // Bob's move: remove the largest remaining (if any)
                if (l <= r) {
                    r--;
                }
            }

            out.println(ans);
        }
        out.flush();
        out.close();
    }

    // Fast I/O
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;

        FastReader() {
            br = new BufferedReader(new InputStreamReader(System.in));
        }

        String next() throws IOException {
            while (st == null || !st.hasMoreTokens()) {
                String line = br.readLine();
                if (line == null) return null;
                st = new StringTokenizer(line);
            }
            return st.nextToken();
        }

        int nextInt() throws IOException {
            return Integer.parseInt(next());
        }
    }
}