import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder output = new StringBuilder();

        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            String sN = Integer.toString(n);
            int L = sN.length();
            
            // Special case n == 1
            if (n == 1) {
                // For a from 2..10000, set b = a - 1
                output.append(9999).append('\n');
                for (int a = 2; a <= 10000; a++) {
                    output.append(a).append(' ').append(a - 1).append('\n');
                }
                continue;
            }

            // General case n != 1
            // We will collect solutions in a TreeSet to avoid duplicates and keep sorted by 'a'.
            TreeSet<Long> seen = new TreeSet<>();
            List<int[]> solutions = new ArrayList<>();
            
            int maxPrefixLen = 7;  // we only need to try p <= 7
            int denom = n - L;     // will be non-zero since n>1 => n>L always
            
            // Try all ways to pick a prefix of length p = k*L + r, with 0<=r<L, k>=0
            // ensuring 1 <= p <= maxPrefixLen
            for (int k = 0; k * L <= maxPrefixLen; k++) {
                for (int r = 0; r < L; r++) {
                    int p = k * L + r;
                    if (p < 1 || p > maxPrefixLen) continue;

                    // Build the prefix string of length p
                    StringBuilder prefix = new StringBuilder();
                    while (prefix.length() < p) {
                        prefix.append(sN);
                    }
                    prefix.setLength(p);
                    int U = Integer.parseInt(prefix.toString());

                    // Solve a * (n - L) = U - p
                    int numer = U - p;
                    if (numer <= 0 || numer % denom != 0) continue;
                    int a = numer / denom;
                    if (a < 1 || a > 10000) continue;

                    // Compute b = a*L - p
                    int b = a * L - p;
                    if (b < 1 || b > 10000) continue;
                    // b <= a*n automatically since n > L
                    
                    // Avoid duplicates
                    long key = ((long)a << 20) | b;
                    if (!seen.contains(key)) {
                        seen.add(key);
                        solutions.add(new int[]{a, b});
                    }
                }
            }

            // Sort by 'a' (and then by 'b')
            solutions.sort(Comparator.<int[]>comparingInt(p -> p[0])
                                    .thenComparingInt(p -> p[1]));

            // Output
            output.append(solutions.size()).append('\n');
            for (int[] sol : solutions) {
                output.append(sol[0]).append(' ').append(sol[1]).append('\n');
            }
        }

        // Flush all at once
        System.out.print(output.toString());
    }
}