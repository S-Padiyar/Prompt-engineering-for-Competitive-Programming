import java.io.*;
import java.util.*;

public class Main {
    static class DSU {
        int[] parent, rank;
        DSU(int n) {
            parent = new int[n+1];
            rank   = new int[n+1];
            for(int i = 1; i <= n; i++){
                parent[i] = i;
                rank[i] = 0;
            }
        }
        int find(int x){
            return parent[x] == x ? x : (parent[x] = find(parent[x]));
        }
        boolean union(int x, int y){
            x = find(x); y = find(y);
            if(x == y) return false;
            if(rank[x] < rank[y]) {
                parent[x] = y;
            } else if(rank[y] < rank[x]) {
                parent[y] = x;
            } else {
                parent[y] = x;
                rank[x]++;
            }
            return true;
        }
    }
    
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter   out= new PrintWriter(System.out);
        
        int t = Integer.parseInt(br.readLine().trim());
        while(t-- > 0){
            int n = Integer.parseInt(br.readLine().trim());
            StringTokenizer st = new StringTokenizer(br.readLine());
            int[] a = new int[n+1];
            for(int i = 1; i <= n; i++){
                a[i] = Integer.parseInt(st.nextToken());
            }
            
            // Prepare DSU
            DSU dsu = new DSU(n);
            // Will hold the chosen edge for operation x (1 <= x <= n-1).
            int[][] answer = new int[n][2];
            boolean possible = true;
            
            // We'll reuse these buckets for each x
            ArrayList<Integer>[] buckets = new ArrayList[n+1];
            for(int i = 0; i <= n; i++){
                buckets[i] = new ArrayList<>();
            }
            
            // Greedily attempt to pick an edge for x = n-1 down to 1
            for(int x = n-1; x >= 1; x--){
                // Clear buckets 0..x-1
                for(int r = 0; r < x; r++){
                    buckets[r].clear();
                }
                // Distribute vertices into buckets by a[i] % x
                for(int i = 1; i <= n; i++){
                    int r = a[i] % x;
                    buckets[r].add(i);
                }
                
                // Try to find a bucket with at least two DIFFERENT-component vertices
                boolean found = false;
                int bu = -1, bv = -1;
                for(int r = 0; r < x && !found; r++){
                    ArrayList<Integer> list = buckets[r];
                    if(list.size() < 2) continue;
                    int u0 = list.get(0);
                    int pu0 = dsu.find(u0);
                    for(int j = 1; j < list.size(); j++){
                        int v = list.get(j);
                        int pv = dsu.find(v);
                        if(pu0 != pv){
                            bu = u0;
                            bv = v;
                            found = true;
                            break;
                        }
                    }
                }
                
                if(!found){
                    possible = false;
                    break;
                }
                // Union those components, record the edge
                dsu.union(bu, bv);
                answer[x][0] = bu;
                answer[x][1] = bv;
            }
            
            if(!possible){
                out.println("No");
            } else {
                out.println("Yes");
                // Output in increasing order of x = 1..n-1
                for(int x = 1; x <= n-1; x++){
                    out.println(answer[x][0] + " " + answer[x][1]);
                }
            }
        }
        
        out.flush();
    }
}