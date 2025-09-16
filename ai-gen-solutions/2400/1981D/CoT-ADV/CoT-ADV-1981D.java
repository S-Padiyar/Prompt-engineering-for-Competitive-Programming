import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        int t = Integer.parseInt(br.readLine().trim());
        
        // We will reuse a single 'used' matrix up to max k ~ 1415
        final int MAXK = 1500;
        boolean[][] used = new boolean[MAXK+1][MAXK+1];
        int[] ptr = new int[MAXK+1];  // for each node, next candidate neighbor
        
        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            int m = n - 1;  // we need m distinct adjacent pairs
            
            // 1) find smallest k so that k*(k+1)/2 >= m
            //    then if equality holds and k is even, bump k by 1
            int k = (int)Math.floor((Math.sqrt(1 + 8L*m) - 1)/2);
            while ((long)k*(k+1)/2 < m) k++;
            long totalEdges = (long)k*(k+1)/2;
            if (totalEdges == m && k % 2 == 0) {
                // avoid the parity trap where no Euler trail covers exactly all edges
                k++;
                totalEdges = (long)k*(k+1)/2;
            }
            
            // 2) clear 'used' for nodes 1..k and reset pointers
            for (int i = 1; i <= k; i++) {
                ptr[i] = 1;
                Arrays.fill(used[i], 1, k+1, false);
            }
            
            // 3) build the walk/sequence
            int[] a = new int[n];
            a[0] = 1;        // start at node 1
            int cur = 1;
            
            for (int i = 1; i < n; i++) {
                // advance ptr[cur] until we find an unused edge {cur, ptr[cur]}
                while (ptr[cur] <= k && used[cur][ptr[cur]]) {
                    ptr[cur]++;
                }
                // ptr[cur] must be <= k because we arranged totalEdges > (n-1) or parity is OK
                int nxt = ptr[cur];
                // mark undirected edge used
                used[cur][nxt] = true;
                used[nxt][cur] = true;
                a[i] = nxt;
                cur = nxt;
            }
            
            // 4) output the sequence
            for (int i = 0; i < n; i++) {
                bw.write(a[i] + (i+1<n ? " " : "\n"));
            }
        }
        
        bw.flush();
    }
}