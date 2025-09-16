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

            st = new StringTokenizer(in.readLine());
            long sumA = 0;
            long maxA = 0;
            for (int i = 0; i < n; i++) {
                long a = Long.parseLong(st.nextToken());
                sumA += a;
                if (a > maxA) maxA = a;
            }

            // Binary search for the maximum feasible s in [1..n]
            int left = 1, right = n, ans = 1;
            while (left <= right) {
                int mid = (left + right) >> 1;
                if (isFeasible(mid, sumA, maxA, k)) {
                    ans = mid;
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }

            sb.append(ans).append('\n');
        }

        System.out.print(sb);
    }

    /**
     * Check if a deck size s is feasible.
     *
     * We need some integer m such that
     *   (1) m >= maxA
     *   (2) ceil(sumA/s) <= m <= floor((sumA + k)/s)
     *
     * Denote
     *   L = max( maxA, ceil(sumA/s) )
     *   R =     floor((sumA + k)/s)
     * Then s is feasible iff L <= R.
     */
    static boolean isFeasible(int s, long sumA, long maxA, long k) {
        // Compute ceil(sumA / s)
        long low1 = (sumA + s - 1) / s;
        long L = Math.max(maxA, low1);
        long R = (sumA + k) / s;
        return L <= R;
    }
}