import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(in.readLine().trim());
        StringBuilder sb = new StringBuilder();
        
        while (t-- > 0) {
            int n = Integer.parseInt(in.readLine().trim());
            int[] deg = new int[n+1];
            for (int i = 0; i < n-1; i++) {
                StringTokenizer st = new StringTokenizer(in.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                deg[u]++; 
                deg[v]++;
            }
            String s = in.readLine().trim();
            
            // Identify the leaves (degree 1, but not node 1),
            // and count f0/f1/q among those leaves:
            int f0 = 0, f1 = 0, ql = 0;
            for (int i = 2; i <= n; i++) {
                if (deg[i] == 1) {  // it's a leaf
                    char c = s.charAt(i-1);
                    if (c == '0') f0++;
                    else if (c == '1') f1++;
                    else            ql++;
                }
            }
            
            // Count total '?' in the whole tree:
            int totQ = 0;
            for (char c : s.toCharArray()) {
                if (c == '?') totQ++;
            }
            boolean rootQ = (s.charAt(0) == '?');
            // Number of irrelevant '?' = total '?' minus
            // (leaf '?' plus 1 if root is '?'):
            int irrelevant = totQ - ql - (rootQ ? 1 : 0);
            boolean hasIrrelevant = (irrelevant > 0);
            
            int answer;
            // Case A: root is already fixed
            if (!rootQ) {
                char r = s.charAt(0);
                int base = (r == '0' ? f1 : f0);
                // plus the ceiling of ql/2:
                int add = (ql + 1) / 2;
                answer = base + add;
            }
            else {
                // Root is '?'.  We compute two candidates:
                // C1 = Iris picks root first --> max(f0,f1) + floor(ql/2)
                int C1 = Math.max(f0, f1) + (ql / 2);
                // C2 = Dora picks root first --> min(f0,f1) + ceil(ql/2)
                int C2 = Math.min(f0, f1) + ((ql + 1) / 2);
                
                if (hasIrrelevant) {
                    // Iris can force whichever scenario she likes:
                    answer = Math.max(C1, C2);
                } else {
                    // No irrelevant '?': relevant play must start with Iris
                    answer = C1;
                }
            }
            
            sb.append(answer).append('\n');
        }
        
        System.out.print(sb);
    }
}