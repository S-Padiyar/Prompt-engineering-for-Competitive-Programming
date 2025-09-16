import java.io.*;
import java.util.*;

public class Main {
    static final int MAXN = 1000000 + 5;
    // adjacency in "forward star" form
    static int[] head = new int[MAXN], nxt = new int[MAXN], to = new int[MAXN];
    static int[] parent = new int[MAXN], deg = new int[MAXN], need = new int[MAXN];
    static int[] queue = new int[MAXN], tmp = new int[MAXN];
    
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        StringTokenizer st;
        
        int t = Integer.parseInt(br.readLine());
        int ePtr = 0;  // pointer into our edge‐arrays
        
        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine());
            // initialize
            for (int i = 1; i <= n; i++) {
                head[i] = -1;
                deg[i] = 0;
            }
            ePtr = 0;
            
            // read parents p[2..n]
            st = new StringTokenizer(br.readLine());
            parent[1] = 0;
            for (int i = 2; i <= n; i++) {
                int p = Integer.parseInt(st.nextToken());
                parent[i] = p;
                // build child‐list
                to[ePtr] = i;
                nxt[ePtr] = head[p];
                head[p] = ePtr++;
                deg[p]++;
            }
            
            // we will process leaves first
            int qh = 0, qt = 0;
            for (int i = 1; i <= n; i++) {
                if (deg[i] == 0) {
                    queue[qt++] = i;
                }
            }
            
            // BFS‐like DP
            while (qh < qt) {
                int u = queue[qh++];
                
                if (head[u] < 0) {
                    // no children => leaf => need=0
                    need[u] = 0;
                } else {
                    // gather children's needs
                    int cnt = 0;
                    for (int e = head[u]; e != -1; e = nxt[e]) {
                        tmp[cnt++] = need[to[e]];
                    }
                    // sort ascending
                    Arrays.sort(tmp, 0, cnt);
                    // binary‐search minimal H
                    // lower bound from max-child‐need+1
                    int maxChildNeed = tmp[cnt-1];
                    int low = maxChildNeed + 1;
                    // also must have total capacity >= cnt:
                    //   sum_{d=1..H} 2^d = 2^{H+1}-2 >= cnt
                    //  => H >= ceil(log2(cnt+2)) - 1
                    if (cnt > 0) {
                        int tval = cnt + 2;
                        int fl = 31 - Integer.numberOfLeadingZeros(tval);
                        int ce = ((tval & (tval-1)) == 0 ? fl : fl+1);
                        low = Math.max(low, ce - 1);
                    }
                    int high = low + cnt; // a safe upper bound
                    
                    int ans = high;
                    while (low <= high) {
                        int mid = (low + high) >>> 1;
                        if (canSchedule(mid, tmp, cnt)) {
                            ans = mid;
                            high = mid - 1;
                        } else {
                            low = mid + 1;
                        }
                    }
                    need[u] = ans;
                }
                
                // push parent once all its children done
                int p = parent[u];
                if (p != 0) {
                    if (--deg[p] == 0) {
                        queue[qt++] = p;
                    }
                }
            }
            
            // answer for this test is need[1]
            out.println(need[1]);
        }
        
        out.flush();
    }
    
    /** 
     * Check if a perfect binary tree of height H can
     * schedule these cnt child‐needs (in tmp[0..cnt)),
     * i.e. deadlines d_i = H - tmp[k-1-i], sorted ascending,
     * and capacity at time x is 2^1 + ... + 2^x = 2^{x+1}-2.
     */
    static boolean canSchedule(int H, int[] arr, int cnt) {
        for (int i = 0; i < cnt; i++) {
            // l_i = largest‐first => arr[cnt-1-i]
            int lval = arr[cnt-1 - i];
            int d = H - lval;
            if (d < 1) return false;
            if (d < 31) {
                long cap = (1L << (d+1)) - 2; 
                if (cap < i+1) return false;
            }
            // if d>=31, capacity >> 1e6 so surely ok
        }
        return true;
    }
}