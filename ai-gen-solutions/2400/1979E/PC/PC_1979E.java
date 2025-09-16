import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        int t = Integer.parseInt(st.nextToken());

        StringBuilder sb = new StringBuilder();
        // We'll reuse one HashMap per test to avoid reallocation overhead.
        HashMap<Long,Integer> map = new HashMap<>();

        outer:
        for (int _case = 0; _case < t; _case++) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int d = Integer.parseInt(st.nextToken());

            // Read points, build map
            map.clear();
            int[] X = new int[n];
            int[] Y = new int[n];
            for (int i = 0; i < n; i++) {
                st = new StringTokenizer(br.readLine());
                int x = Integer.parseInt(st.nextToken());
                int y = Integer.parseInt(st.nextToken());
                X[i] = x;
                Y[i] = y;
                long key = encode(x, y);
                map.put(key, i+1);  // store 1-based index
            }

            int D = d / 2;         // always integer because d is even
            boolean found = false;
            int ai=0, bi=0, ci=0;

            // Precompute all 4 patterns of Family 1 (45°‐square)
            int[][] fam1_dx = new int[4][2];
            int[][] fam1_dy = new int[4][2];
            int idx = 0;
            for (int s1 = -1; s1 <= 1; s1 += 2) {
                for (int s2 = -1; s2 <= 1; s2 += 2) {
                    fam1_dx[idx][0] = s1 * D;  // v1.x
                    fam1_dy[idx][0] = s2 * D;  // v1.y
                    fam1_dx[idx][1] = s1 * D;  // v2.x
                    fam1_dy[idx][1] = -s2 * D; // v2.y
                    idx++;
                }
            }

            // Precompute Family 2 (3:1) if d divisible by 4
            boolean useFam2 = (d % 4 == 0);
            int u = d / 4;
            int a = 3 * u, b = u;
            int[][] fam2_dx = null, fam2_dy = null;
            if (useFam2) {
                fam2_dx = new int[4][2];
                fam2_dy = new int[4][2];
                idx = 0;
                for (int s1 = -1; s1 <= 1; s1 += 2) {
                    for (int s2 = -1; s2 <= 1; s2 += 2) {
                        fam2_dx[idx][0] = s1 * a; fam2_dy[idx][0] = s2 * b;
                        fam2_dx[idx][1] = s1 * b; fam2_dy[idx][1] = s2 * a;
                        idx++;
                    }
                }
            }

            // Try every point as the "anchor" A
            for (int i = 0; i < n && !found; i++) {
                int x = X[i], y = Y[i];
                long baseKey = encode(x, y);

                // Family 1
                for (int k = 0; k < 4 && !found; k++) {
                    int x1 = x + fam1_dx[k][0],  y1 = y + fam1_dy[k][0];
                    int x2 = x + fam1_dx[k][1],  y2 = y + fam1_dy[k][1];
                    long k1 = encode(x1, y1);
                    long k2 = encode(x2, y2);
                    Integer j = map.get(k1);
                    Integer l = map.get(k2);
                    if (j != null && l != null) {
                        ai = i+1; bi = j; ci = l;
                        found = true;
                    }
                }

                // Family 2
                if (!found && useFam2) {
                    for (int k = 0; k < 4 && !found; k++) {
                        int x1 = x + fam2_dx[k][0], y1 = y + fam2_dy[k][0];
                        int x2 = x + fam2_dx[k][1], y2 = y + fam2_dy[k][1];
                        long k1 = encode(x1, y1);
                        long k2 = encode(x2, y2);
                        Integer j = map.get(k1);
                        Integer l = map.get(k2);
                        if (j != null && l != null) {
                            ai = i+1; bi = j; ci = l;
                            found = true;
                        }
                    }
                }
            }

            if (!found) {
                sb.append("0 0 0\n");
            } else {
                sb.append(ai).append(' ')
                  .append(bi).append(' ')
                  .append(ci).append('\n');
            }
        }

        // print all answers
        System.out.print(sb.toString());
    }

    // encode a pair (x,y) into a single 64-bit key
    static long encode(int x, int y) {
        return ( (long)x << 32 ) ^ (y & 0xffffffffL);
    }
}