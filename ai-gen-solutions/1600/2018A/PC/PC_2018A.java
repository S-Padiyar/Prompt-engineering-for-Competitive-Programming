import java.io.*;
import java.util.StringTokenizer;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        
        int t = Integer.parseInt(in.readLine().trim());
        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            long k = Long.parseLong(st.nextToken());
            
            long A = 0;      // sum of a_i
            long M = 0;      // max of a_i
            st = new StringTokenizer(in.readLine());
            for (int i = 0; i < n; i++) {
                long ai = Long.parseLong(st.nextToken());
                A += ai;
                if (ai > M) {
                    M = ai;
                }
            }
            
            // Upper bound on s: s*M <= A+k  =>  s <= (A+k)/M
            long maxByM = (A + k) / M;
            int upper = (int)Math.min(n, maxByM);
            
            int answer = 0;
            // Try every s from 1..upper
            for (int s = 1; s <= upper; s++) {
                // L = floor((A+k)/s)
                long L = (A + k) / s;
                // ceil(A/s) = (A + s - 1) / s
                long needT = (A + s - 1) / s;
                // R = max(M, ceil(A/s))
                long R = Math.max(M, needT);
                
                if (L >= R) {
                    answer = s;
                }
            }
            
            sb.append(answer).append('\n');
        }
        
        System.out.print(sb);
    }
}