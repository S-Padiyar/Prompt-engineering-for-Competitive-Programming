import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter    pw = new PrintWriter(System.out);
        int t = Integer.parseInt(br.readLine());
        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine());
            StringTokenizer st = new StringTokenizer(br.readLine());
            int[] a = new int[n+1];
            for (int i = 1; i <= n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }
            // Count how many 0,1,2
            int c0=0, c1=0, c2=0;
            for (int i = 1; i <= n; i++) {
                if (a[i]==0) c0++;
                else if (a[i]==1) c1++;
                else c2++;
            }
            // Three sets storing positions of 0s,1s,2s
            TreeSet<Integer> set0 = new TreeSet<>();
            TreeSet<Integer> set1 = new TreeSet<>();
            TreeSet<Integer> set2 = new TreeSet<>();
            for (int i = 1; i <= n; i++) {
                if (a[i]==0) set0.add(i);
                else if (a[i]==1) set1.add(i);
                else             set2.add(i);
            }

            List<int[]> moves = new ArrayList<>();

            // Pass 1: Make positions 1..c0 all zeros
            for (int i = 1; i <= c0; i++) {
                if (a[i] == 0) continue;
                if (a[i] == 1) {
                    // Swap with a 0 somewhere to the right
                    Integer j = set0.higher(i);
                    // perform swap(i,j)
                    moves.add(new int[]{i, j});
                    // update sets and a[]
                    set1.remove(i); set0.remove(j);
                    a[i] = 0;  a[j] = 1;
                    set0.add(i); set1.add(j);
                } else {
                    // a[i] == 2.  First swap with a 1
                    Integer k = set1.higher(i);
                    moves.add(new int[]{i, k});
                    set2.remove(i); set1.remove(k);
                    a[i] = 1;  a[k] = 2;
                    set1.add(i); set2.add(k);

                    // Now a[i] == 1, swap with a 0
                    Integer j = set0.higher(i);
                    moves.add(new int[]{i, j});
                    set1.remove(i); set0.remove(j);
                    a[i] = 0;  a[j] = 1;
                    set0.add(i); set1.add(j);
                }
            }

            // Pass 2: Make positions c0+1..c0+c1 all ones
            int startOne = c0+1, endOne = c0+c1;
            for (int i = startOne; i <= endOne; i++) {
                if (a[i] == 1) continue;
                // then a[i] must be 2, swap with a 1 in the suffix
                Integer k = set1.higher(endOne);
                moves.add(new int[]{i, k});
                set2.remove(i); set1.remove(k);
                a[i] = 1;  a[k] = 2;
                set1.add(i); set2.add(k);
            }

            // Output result
            pw.println(moves.size());
            for (int[] mv : moves) {
                pw.println(mv[0] + " " + mv[1]);
            }
        }
        pw.flush();
        pw.close();
    }
}