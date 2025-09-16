import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 1_000_000_007;

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(in.readLine().trim());

        while (t-- > 0) {
            String s = in.readLine().trim();
            int n = s.length();

            // Build prefix sums P_0..P_n, interpreting '0'->-1, '1'->+1
            // And group positions by prefix sum in a hashmap.
            Map<Integer, List<Integer>> map = new HashMap<>();
            int sum = 0;
            // Position 0 has prefix sum 0
            map.computeIfAbsent(0, k -> new ArrayList<>()).add(0);

            for (int i = 1; i <= n; i++) {
                char c = s.charAt(i - 1);
                if (c == '1') sum++;
                else sum--;
                map.computeIfAbsent(sum, k -> new ArrayList<>()).add(i);
            }

            long answer = 0;

            // For each group of positions with the same prefix sum:
            // positions = [p0, p1, ..., p_{k-1}]
            // we accumulate sum over i<j of (p_i+1)*(n - p_j +1).
            for (List<Integer> positions : map.values()) {
                long Sleft = 0; // Sleft = sum of (p_i+1) for i<j
                for (int j = 0; j < positions.size(); j++) {
                    int v = positions.get(j);
                    if (j > 0) {
                        long waysRight = (long)(n - v + 1);
                        answer = (answer + Sleft * waysRight) % MOD;
                    }
                    // Add (v+1) into Sleft for future j
                    Sleft = (Sleft + (v + 1)) % MOD;
                }
            }

            System.out.println(answer);
        }

        in.close();
    }
}