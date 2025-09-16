import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder output = new StringBuilder();
        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            if (n == 1) {
                output.append(1).append('\n');
                output.append(1).append('\n');
            }
            else if (n == 2) {
                output.append(2).append('\n');
                output.append("1 2").append('\n');
            }
            else if (n == 3) {
                // Optimal is 2‐colourable: one can do {1}, {2,3}
                output.append(2).append('\n');
                output.append("1 2 2").append('\n');
            }
            else if (n == 4) {
                // Optimal is 3‐colourable
                output.append(3).append('\n');
                // e.g. 1–2–2–3 is known to work
                output.append("1 2 2 3").append('\n');
            }
            else if (n == 5) {
                // Optimal is 3‐colourable
                output.append(3).append('\n');
                // for example 1–2–2–3–3
                output.append("1 2 2 3 3").append('\n');
            }
            else {
                // n >= 6: we proved χ(G) = 4, and (i mod 4)+1 is a valid 4‐colouring
                output.append(4).append('\n');
                for (int i = 1; i <= n; i++) {
                    int col = (i % 4) + 1;
                    output.append(col).append(i == n ? '\n' : ' ');
                }
            }
        }
        System.out.print(output);
    }
}