import java.io.*;
import java.util.*;

public class Main {
    // Fast gcd
    static int gcd(int a, int b) {
        while (b != 0) {
            int t = a % b;
            a = b;
            b = t;
        }
        return a;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int t = Integer.parseInt(in.readLine().trim());
        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            // Read n
            int n = Integer.parseInt(in.readLine().trim());
            int[] a = new int[n];
            st = new StringTokenizer(in.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            // Compute G = gcd of all elements
            int totalGcd = a[0];
            for (int i = 1; i < n; i++) {
                totalGcd = gcd(totalGcd, a[i]);
            }

            // If only one element, answer is a[0].
            if (n == 1) {
                sb.append(a[0]).append('\n');
                continue;
            }

            // Find index of the minimum element => best a1
            int minIdx = 0;
            for (int i = 1; i < n; i++) {
                if (a[i] < a[minIdx]) {
                    minIdx = i;
                }
            }

            // Greedy pick:
            boolean[] used = new boolean[n];
            int currentG = a[minIdx];
            used[minIdx] = true;
            long answer = currentG;  // sum starts with g1 = min element
            int usedCount = 1;

            // While we haven't used all and the prefix GCD can still drop
            while (usedCount < n && currentG > totalGcd) {
                int bestNextG = Integer.MAX_VALUE;
                int bestIdx = -1;
                // Scan all unused elements to see which one yields the smallest gcd
                for (int i = 0; i < n; i++) {
                    if (!used[i]) {
                        int g = gcd(currentG, a[i]);
                        if (g < bestNextG) {
                            bestNextG = g;
                            bestIdx = i;
                        }
                    }
                }
                // Use that element
                used[bestIdx] = true;
                currentG = bestNextG;
                answer += currentG;
                usedCount++;
            }

            // If we still have unused elements, each adds totalGcd to the sum
            if (usedCount < n) {
                answer += (long)(n - usedCount) * totalGcd;
            }

            sb.append(answer).append('\n');
        }

        System.out.print(sb);
    }
}