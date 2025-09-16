import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(in.readLine());
        while (t-- > 0) {
            int n = Integer.parseInt(in.readLine());
            int[] deg = new int[n+1];
            for (int i = 0; i < n-1; i++) {
                StringTokenizer st = new StringTokenizer(in.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                deg[u]++;
                deg[v]++;
            }
            String s = in.readLine();
            
            // Count leaves (i>1 with deg[i]==1), fixed leaves, leaf '?', total '?'
            int a0 = 0, a1 = 0, leafQ = 0, totalQ = 0;
            for (int i = 1; i <= n; i++) {
                char c = s.charAt(i-1);
                if (c == '?') totalQ++;
                // leaf = deg[i]==1 and i!=1
                if (i != 1 && deg[i] == 1) {
                    if (c == '0')      a0++;
                    else if (c == '1') a1++;
                    else               leafQ++;
                }
            }

            // How many non-root non-leaf '?' remain = dummy moves
            boolean rootQ = (s.charAt(0) == '?');
            int d = totalQ - (rootQ ? 1 : 0) - leafQ;

            int answer;
            if (!rootQ) {
                // root is already fixed
                int R = s.charAt(0) - '0';
                int base = (R == 0 ? a1 : a0);
                // Iris starts the leaf picks, she gets ceil(leafQ/2)
                int irisLeaves = (leafQ + 1) / 2;
                answer = base + irisLeaves;
            } else {
                // root is '?': two-phase minimax with d dummy moves
                int Bmax = Math.max(a0, a1);
                int Bmin = Math.min(a0, a1);
                int floorHalf = leafQ / 2;
                int ceilHalf  = (leafQ + 1) / 2;
                int X = Bmax + floorHalf;   // if Iris manages root (and thus Dora starts leaves)
                int Y = Bmin + ceilHalf;    // if Dora manages root (and thus Iris starts leaves)
                if ((d & 1) == 0) {
                    // even number of dummies → Iris can guarantee the X‐scenario
                    answer = X;
                } else {
                    // odd number of dummies → she can force the better of X,Y
                    answer = Math.max(X, Y);
                }
            }

            out.println(answer);
        }
        out.flush();
    }
}