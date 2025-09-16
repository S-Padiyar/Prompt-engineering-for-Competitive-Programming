import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            StringTokenizer st = new StringTokenizer(br.readLine());
            int[] a = new int[n+1];
            for (int i = 1; i <= n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }
            // Count how many 0s, 1s, 2s
            int cz = 0, co = 0, ct2 = 0;
            for (int i = 1; i <= n; i++) {
                if (a[i] == 0) cz++;
                else if (a[i] == 1) co++;
                else ct2++;
            }
            // Store positions of 0,1,2 in TreeSets for fast queries
            TreeSet<Integer> zeros = new TreeSet<>();
            TreeSet<Integer> ones  = new TreeSet<>();
            TreeSet<Integer> twos  = new TreeSet<>();
            for (int i = 1; i <= n; i++) {
                if (a[i] == 0) zeros.add(i);
                else if (a[i] == 1) ones.add(i);
                else twos.add(i);
            }

            // Record moves here
            List<int[]> moves = new ArrayList<>();

            // Phase 1: fix the first cz positions to 0
            for (int i = 1; i <= cz; i++) {
                if (a[i] == 0) continue;
                // case a[i] == 1: direct swap with some zero j > cz
                if (a[i] == 1) {
                    // find a zero outside the prefix
                    Integer j = zeros.higher(cz);
                    // swap i,j
                    moves.add(new int[]{i, j});
                    // update data structures
                    zeros.remove(j); zeros.add(i);
                    ones.remove(i);  ones.add(j);
                    a[i] = 0;  a[j] = 1;
                }
                else { // a[i] == 2: need two swaps via some 1
                    // pick any one-position k
                    Integer k = ones.first(); 
                    // 1) swap i,k
                    moves.add(new int[]{i, k});
                    twos.remove(i);    twos.add(k);
                    ones.remove(k);    ones.add(i);
                    a[i] = 1;  a[k] = 2;

                    // 2) swap this new '1' at i with a zero j>cz
                    Integer j = zeros.higher(cz);
                    moves.add(new int[]{i, j});
                    zeros.remove(j); zeros.add(i);
                    ones.remove(i);  ones.add(j);
                    a[i] = 0;  a[j] = 1;
                }
            }

            // Phase 2: fix the last ct2 positions to 2
            int twoStart = n - ct2 + 1;
            for (int i = n; i >= twoStart; i--) {
                if (a[i] == 2) continue;
                // a[i] == 1, swap with a two in < twoStart
                Integer j = twos.lower(twoStart);
                moves.add(new int[]{i, j});
                twos.remove(j); twos.add(i);
                ones.remove(i); ones.add(j);
                a[i] = 2;  a[j] = 1;
            }

            // Output
            out.println(moves.size());
            for (int[] mv : moves) {
                out.println(mv[0] + " " + mv[1]);
            }
        }
        out.flush();
    }
}