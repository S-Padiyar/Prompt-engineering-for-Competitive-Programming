import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.StringTokenizer;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            int[] a = new int[n];
            StringTokenizer st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }
            Arrays.sort(a);
            
            int L = 0, R = n - 1;
            int last = 0;      // Alice's last eaten tastiness
            int aliceCount = 0;
            
            while (true) {
                // Alice's turn: pick the smallest > last
                while (L <= R && a[L] <= last) {
                    L++;
                }
                if (L > R) {
                    // No valid move for Alice
                    break;
                }
                // Alice eats a[L]
                last = a[L];
                aliceCount++;
                L++;
                
                // Bob's turn: eat the largest remaining if any
                if (L <= R) {
                    R--;
                } else {
                    break;
                }
            }
            
            sb.append(aliceCount).append('\n');
        }
        System.out.print(sb);
    }
}