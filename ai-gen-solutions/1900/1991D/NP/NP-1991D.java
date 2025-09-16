import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        
        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            
            if (n == 1) {
                sb.append(1).append('\n');
                sb.append("1\n");
            }
            else if (n == 2) {
                sb.append(2).append('\n');
                sb.append("1 2\n");
            }
            else if (n == 3) {
                // Uses 2 colors; one optimal choice is (1,2,2)
                sb.append(2).append('\n');
                sb.append("1 2 2\n");
            }
            else if (n == 4) {
                // Uses 3 colors; one known optimal is (1,2,2,3)
                sb.append(3).append('\n');
                sb.append("1 2 2 3\n");
            }
            else if (n == 5) {
                // Uses 3 colors; one known optimal is (1,2,2,3,3)
                sb.append(3).append('\n');
                sb.append("1 2 2 3 3\n");
            }
            else {
                // n >= 6 => needs 4 colors, and the pattern (i-1)%4+1 is always valid
                sb.append(4).append('\n');
                for (int i = 1; i <= n; i++) {
                    int c = ((i - 1) % 4) + 1;
                    sb.append(c).append(i == n ? '\n' : ' ');
                }
            }
        }
        
        System.out.print(sb);
    }
}