import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // Fast I/O
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter    pw = new PrintWriter(new BufferedOutputStream(System.out));
        StringTokenizer st = new StringTokenizer(br.readLine());

        int n = Integer.parseInt(st.nextToken());
        int q = Integer.parseInt(st.nextToken());

        // Read the sticks
        long[] a = new long[n];
        st = new StringTokenizer(br.readLine());
        for (int i = 0; i < n; i++) {
            a[i] = Long.parseLong(st.nextToken());
        }

        // For each query
        for (int qi = 0; qi < q; qi++) {
            st = new StringTokenizer(br.readLine());
            int l = Integer.parseInt(st.nextToken()) - 1; // 0-based
            int r = Integer.parseInt(st.nextToken()) - 1;
            int len = r - l + 1;

            // If the range is large enough, we guarantee two triangles
            if (len >= 49) {
                pw.println("YES");
                continue;
            }

            // Otherwise, extract and sort
            long[] temp = new long[len];
            for (int i = 0; i < len; i++) {
                temp[i] = a[l + i];
            }
            Arrays.sort(temp);

            // Find up to two non-overlapping good triples
            int firstIdx = -1;  // where we found the first triangle
            boolean ok = false;
            for (int i = 0; i + 2 < len; i++) {
                if (temp[i] + temp[i + 1] > temp[i + 2]) {
                    // Found a triangle at i..i+2
                    if (firstIdx == -1) {
                        firstIdx = i;  // record the first
                    } else if (i >= firstIdx + 3) {
                        // Non-overlapping with the first
                        ok = true;
                        break;
                    }
                }
            }

            pw.println(ok ? "YES" : "NO");
        }

        pw.flush();
    }
}