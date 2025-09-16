import java.io.*;
import java.util.*;

public class Main {
    static int[] parent, sz, nxt, head, tail;
    static int n;
    static long W;        // total sum of weights
    static long S;        // sum of revealed weights
    static long fully;    // C = count of pairs that are already fully known

    // DSU find with path compression
    static int find(int x) {
        return parent[x]==x ? x : (parent[x]=find(parent[x]));
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter    pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
        int t = Integer.parseInt(br.readLine().trim());
        while(t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            n = Integer.parseInt(st.nextToken());
            W = Long.parseLong(st.nextToken());

            // read the parent array
            int[] p = new int[n+1];  
            st = new StringTokenizer(br.readLine());
            for(int i=2; i<=n; i++){
                p[i] = Integer.parseInt(st.nextToken());
            }

            // initialize DSU
            parent = new int[n+1];
            sz     = new int[n+1];
            nxt    = new int[n+1];
            head   = new int[n+1];
            tail   = new int[n+1];
            for(int i=1; i<=n; i++){
                parent[i] = i;
                sz[i] = 1;
                head[i] = i;    // each comp's list is just the single label i
                tail[i] = i;
                nxt[i]  = 0;    // end of list
            }

            S = 0;
            fully = 0;  // how many adj‐pairs are already fully known
            StringBuilder ans = new StringBuilder();

            // process the n-1 events
            for(int e=0; e<n-1; e++){
                st = new StringTokenizer(br.readLine());
                int x = Integer.parseInt(st.nextToken());
                long w = Long.parseLong(st.nextToken());
                // accumulate revealed weight
                S += w;

                // union the edge (p[x], x)
                int rx = find(x), ry = find(p[x]);
                if(rx != ry) {
                    // always merge the smaller into the larger
                    if(sz[rx] > sz[ry]){
                        int tmp=rx; rx=ry; ry=tmp;
                    }
                    // rx is smaller root, ry is larger root
                    // scan the labels in rx's component
                    int cur = head[rx];
                    while(cur!=0){
                        // check neighbor cur-1 (if >=2)
                        if(cur>1 && find(cur-1)==ry){
                            fully++;
                        }
                        // check neighbor cur+1 (mod n)
                        int nb = (cur==n ? 1 : cur+1);
                        if(find(nb)==ry){
                            fully++;
                        }
                        cur = nxt[cur];
                    }
                    // append rx's list to ry's list
                    nxt[tail[ry]] = head[rx];
                    tail[ry]      = tail[rx];
                    head[rx]      = tail[rx] = 0;

                    // standard DSU union
                    parent[rx] = ry;
                    sz[ry]     += sz[rx];
                }

                // number of adj‐pairs that still have at least one unknown edge:
                long unknownPairs = n - fully;
                long U = W - S;  // remaining total weight
                // answer = 2*S + unknownPairs * U
                long out = 2*S + unknownPairs * U;
                ans.append(out).append(' ');
            }

            pw.println(ans.toString().trim());
        }
        pw.flush();
    }
}