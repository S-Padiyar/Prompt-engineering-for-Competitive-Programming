import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder output = new StringBuilder();
        while (t-- > 0) {
            String[] nx = br.readLine().split(" ");
            int n = Integer.parseInt(nx[0]);
            // In the easy version x = n, so we only compute f(n).
            String[] as = br.readLine().split(" ");
            int[] a = new int[n];
            boolean allOnes = true;
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(as[i]);
                if (a[i] != 1) allOnes = false;
            }
            // Shortcut: if all values are 1, then every position can win.
            if (allOnes) {
                output.append(n).append("\n");
                continue;
            }
            
            int countGood = 0;
            
            // Try each j = 0..n-1 as the survivor position.
            for (int j = 0; j < n; j++) {
                long curr = a[j];
                int L = j - 1, R = j + 1;
                // Greedy-fight until either we exhaust all balls,
                // or we get stuck.
                while (true) {
                    boolean canLeft  = (L >= 0 && a[L] <= curr);
                    boolean canRight = (R < n && a[R] <= curr);
                    if (!canLeft && !canRight) {
                        // both sides too strong -- stuck
                        break;
                    }
                    if (canLeft && canRight) {
                        // fight the smaller one first
                        if (a[L] <= a[R]) {
                            curr += a[L];
                            L--;
                        } else {
                            curr += a[R];
                            R++;
                        }
                    } else if (canLeft) {
                        curr += a[L];
                        L--;
                    } else { // canRight
                        curr += a[R];
                        R++;
                    }
                    // if we've eaten all on both sides, success
                    if (L < 0 && R >= n) {
                        countGood++;
                        break;
                    }
                }
            }
            
            output.append(countGood).append("\n");
        }
        System.out.print(output);
    }
}