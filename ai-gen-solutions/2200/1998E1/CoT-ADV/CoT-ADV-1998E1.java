import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            // x == n in this easy version; we ignore the second input.
            in.readLine();  // read and discard x or empty rest

            st = new StringTokenizer(in.readLine());
            long[] a = new long[n+1];
            for (int i = 1; i <= n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }

            // We'll count how many j in [1..n] can survive
            int count = 0;
            for (int j = 1; j <= n; j++) {
                long v = a[j];
                int L = j - 1, R = j + 1;
                // Try to absorb neighbors greedily
                while (true) {
                    if (L < 1 && R > n) {
                        // All absorbed
                        count++;
                        break;
                    }
                    if (L < 1) {
                        // Only right side left
                        if (a[R] <= v) {
                            v += a[R];
                            R++;
                        } else {
                            break;
                        }
                    } else if (R > n) {
                        // Only left side left
                        if (a[L] <= v) {
                            v += a[L];
                            L--;
                        } else {
                            break;
                        }
                    } else {
                        // Both sides exist: pick the smaller to absorb
                        if (a[L] <= a[R]) {
                            if (a[L] <= v) {
                                v += a[L];
                                L--;
                            } else {
                                break;
                            }
                        } else {
                            if (a[R] <= v) {
                                v += a[R];
                                R++;
                            } else {
                                break;
                            }
                        }
                    }
                }
            }

            out.println(count);
        }

        out.flush();
    }
}