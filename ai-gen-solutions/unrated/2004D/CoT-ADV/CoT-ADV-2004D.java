import java.io.*;
import java.util.*;

public class Main {
    // Map each color character to an integer 0..3
    // B=0, G=1, R=2, Y=3
    static int code(char c) {
        switch(c) {
            case 'B': return 0;
            case 'G': return 1;
            case 'R': return 2;
            case 'Y': return 3;
        }
        return -1;
    }
    
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        int t = Integer.parseInt(br.readLine().trim());
        
        // We'll need a quick mapping from unordered color-pair (c1,c2) to 0..5
        int[][] pairIndex = new int[4][4];
        // the six valid pairs and their assigned indices
        int[][] pairs = {
            {0,1}, // BG
            {0,2}, // BR
            {0,3}, // BY
            {1,2}, // GR
            {1,3}, // GY
            {2,3}  // RY
        };
        for(int i = 0; i < 6; i++) {
            int a = pairs[i][0], b = pairs[i][1];
            pairIndex[a][b] = pairIndex[b][a] = i;
        }
        
        StringBuilder out = new StringBuilder();
        while(t-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int q = Integer.parseInt(st.nextToken());
            
            // Read the portal-pairs for each city
            String[] cityPortals = br.readLine().split(" ");
            // For each of the 6 unordered pairs, collect the city-indices that have exactly that pair
            List<Integer>[] pos = new ArrayList[6];
            for(int i = 0; i < 6; i++) pos[i] = new ArrayList<>();
            
            // Also store the two color-codes for each city
            int[][] cityCols = new int[n][2];
            
            for(int i = 0; i < n; i++) {
                char c1 = cityPortals[i].charAt(0);
                char c2 = cityPortals[i].charAt(1);
                int x1 = code(c1), x2 = code(c2);
                cityCols[i][0] = x1;
                cityCols[i][1] = x2;
                int idx = pairIndex[x1][x2];
                pos[idx].add(i+1);  // store 1-based city index
            }
            
            // The insertion always came in increasing order, so lists are sorted already.
            // But to be safe, we can skip an explicit sort.
            
            while(q-- > 0) {
                st = new StringTokenizer(br.readLine());
                int x = Integer.parseInt(st.nextToken());
                int y = Integer.parseInt(st.nextToken());
                
                if(x == y) {
                    out.append("0\n");
                    continue;
                }
                
                int colX1 = cityCols[x-1][0], colX2 = cityCols[x-1][1];
                int colY1 = cityCols[y-1][0], colY2 = cityCols[y-1][1];
                
                // If they share a color, direct teleport is possible
                boolean share = (colX1 == colY1 || colX1 == colY2
                              || colX2 == colY1 || colX2 == colY2);
                if(share) {
                    out.append(Math.abs(x - y)).append('\n');
                    continue;
                }
                
                int lo = Math.min(x, y), hi = Math.max(x, y);
                int ans = Integer.MAX_VALUE;
                
                // Try each combination c in {colX1,colX2}, d in {colY1,colY2}, c!=d
                int[] pickX = {colX1, colX2};
                int[] pickY = {colY1, colY2};
                
                for(int cx : pickX) {
                    for(int cy : pickY) {
                        if(cx == cy) continue; // can't happen anyway
                        int pi = pairIndex[cx][cy];
                        List<Integer> list = pos[pi];
                        if(list.isEmpty()) continue;
                        
                        // Binary search for the first index >= lo
                        int idx = Collections.binarySearch(list, lo);
                        if(idx < 0) idx = -idx - 1;
                        
                        // If there's an entry in [lo, hi], cost = hi-lo
                        if(idx < list.size() && list.get(idx) <= hi) {
                            ans = Math.min(ans, hi - lo);
                        } else {
                            // Check the nearest on the left
                            if(idx > 0) {
                                int p = list.get(idx - 1);
                                int cost = (lo - p) + (hi - p);
                                ans = Math.min(ans, cost);
                            }
                            // Check the nearest on the right
                            if(idx < list.size()) {
                                int p = list.get(idx);
                                int cost = (p - lo) + (p - hi);
                                ans = Math.min(ans, cost);
                            }
                        }
                    }
                }
                
                if(ans == Integer.MAX_VALUE) ans = -1;
                out.append(ans).append('\n');
            }
        }
        
        System.out.print(out);
    }
}