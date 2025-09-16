import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer tok = new StringTokenizer(in.readLine());
        int t = Integer.parseInt(tok.nextToken());
        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            tok = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(tok.nextToken());
            int[] p = new int[n+1];
            tok = new StringTokenizer(in.readLine());
            for (int i = 1; i <= n; i++) {
                p[i] = Integer.parseInt(tok.nextToken());
            }
            
            // buckets[b] will hold the list of a_j for all processed j with b_j = b
            @SuppressWarnings("unchecked")
            ArrayList<Integer>[] buckets = new ArrayList[n+1];
            for (int i = 1; i <= n; i++) {
                buckets[i] = new ArrayList<>();
            }
            
            long ans = 0;
            for (int i = 1; i <= n; i++) {
                int g = gcd(i, p[i]);
                int a_i = p[i] / g;
                int b_i = i   / g;
                
                // enumerate divisors of a_i
                for (int d = 1; d * d <= a_i; d++) {
                    if (a_i % d == 0) {
                        int d1 = d;
                        int d2 = a_i / d;
                        
                        // check bucket for b_j = d1
                        for (int a_j : buckets[d1]) {
                            if (a_j % b_i == 0) {
                                ans++;
                            }
                        }
                        
                        // if distinct, also check bucket for b_j = d2
                        if (d1 != d2) {
                            for (int a_j : buckets[d2]) {
                                if (a_j % b_i == 0) {
                                    ans++;
                                }
                            }
                        }
                    }
                }
                
                // now "insert" index i into bucket b_i
                buckets[b_i].add(a_i);
            }
            
            sb.append(ans).append('\n');
        }
        System.out.print(sb);
    }
    
    // fast gcd
    static int gcd(int x, int y) {
        while (y != 0) {
            int r = x % y;
            x = y;
            y = r;
        }
        return x;
    }
}