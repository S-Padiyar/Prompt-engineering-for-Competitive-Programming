import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int t = Integer.parseInt(in.readLine());
        StringBuilder output = new StringBuilder();

        while (t-- > 0) {
            st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int d = Integer.parseInt(st.nextToken());

            // Read and transform points to (s,t) = (x+y, x-y)
            int[] S = new int[n];
            int[] T = new int[n];
            for (int i = 0; i < n; i++) {
                st = new StringTokenizer(in.readLine());
                int x = Integer.parseInt(st.nextToken());
                int y = Integer.parseInt(st.nextToken());
                S[i] = x + y;
                T[i] = x - y;
            }

            // Build map from (s,t) -> original index
            HashMap<Long,Integer> map = new HashMap<>(n*2);
            for (int i = 0; i < n; i++) {
                long key = pack(S[i], T[i]);
                map.put(key, i);
            }

            // We'll attempt to find a triple
            boolean found = false;
            int ai=0, bi=0, ci=0;

            // Offsets for the 4 orientations
            // each orientation has two required corners:
            int[][] offs = {
                {+d,  0,   0, +d},   // bottom-left corner
                {-d,  0,   0, +d},   // bottom-right
                {+d,  0,   0, -d},   // top-left
                {-d,  0,   0, -d}    // top-right
            };

            outer:
            for (int i = 0; i < n; i++) {
                int s0 = S[i], t0 = T[i];
                for (int[] o : offs) {
                    long k1 = pack(s0 + o[0], t0 + o[1]);
                    long k2 = pack(s0 + o[2], t0 + o[3]);
                    Integer jIdx = map.get(k1);
                    Integer kIdx = map.get(k2);
                    if (jIdx != null && kIdx != null) {
                        // We found three corners of an L∞ square
                        ai = i + 1;    // back to 1-based
                        bi = jIdx + 1;
                        ci = kIdx + 1;
                        found = true;
                        break outer;
                    }
                }
            }

            if (found) {
                output.append(ai).append(' ')
                      .append(bi).append(' ')
                      .append(ci).append('\n');
            } else {
                output.append("0 0 0\n");
            }
        }

        System.out.print(output);
    }

    // Pack two 32-bit ints (s,t) into one 64-bit key
    static long pack(int a, int b) {
        return (((long)a) << 32) ^ (b & 0xffffffffL);
    }
}