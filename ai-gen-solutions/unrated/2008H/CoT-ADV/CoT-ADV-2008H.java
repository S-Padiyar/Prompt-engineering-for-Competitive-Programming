import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int T = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();

        while (T-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int q = Integer.parseInt(st.nextToken());

            // Read the array and build frequency
            int[] cnt = new int[n+1];
            int maxA = 0;
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                int v = Integer.parseInt(st.nextToken());
                cnt[v]++;
                if (v > maxA) maxA = v;
            }

            // Prefix sums of frequencies
            int[] pref = new int[maxA + 1];
            for (int v = 1; v <= maxA; v++) {
                pref[v] = pref[v-1] + cnt[v];
            }

            // Median index (upper median for even n)
            int k = (n + 2) / 2;

            // Read queries and collect distinct x's
            int[] queries = new int[q];
            HashSet<Integer> distinctX = new HashSet<>();
            for (int i = 0; i < q; i++) {
                int x = Integer.parseInt(br.readLine().trim());
                queries[i] = x;
                distinctX.add(x);
            }

            // Map from x to answer
            // We know x <= n, so we can use an array of size (n+1) to store answers
            int[] answerOfX = new int[n+1];
            Arrays.fill(answerOfX, -1);

            // For each distinct x, binary‐search the smallest residue r with C(r) ≥ k
            for (int x : distinctX) {
                int R = Math.min(x - 1, maxA);  // max possible residue
                int low = 0, high = R;
                while (low < high) {
                    int mid = (low + high) >>> 1;
                    if (countUpTo(mid, x, pref, maxA, k) >= k) {
                        high = mid;
                    } else {
                        low = mid + 1;
                    }
                }
                answerOfX[x] = low;
            }

            // Output in original query order
            for (int x : queries) {
                sb.append(answerOfX[x]).append(' ');
            }
            sb.append('\n');
        }

        System.out.print(sb.toString());
    }

    /**
     * Compute how many a[i] satisfy (a[i] mod x) <= r,
     * by summing frequencies in blocks of size x, using prefix sums.
     * We stop early if the count reaches the target k.
     */
    static int countUpTo(int r, int x, int[] pref, int maxA, int target) {
        int cnt = 0;

        // block j = 0 => range [1 .. r]
        int right = Math.min(r, maxA);
        if (right >= 1) {
            cnt += pref[right];
            if (cnt >= target) return cnt;
        }

        // blocks j = 1..floor(maxA/x)
        int blocks = maxA / x;
        for (int j = 1; j <= blocks; j++) {
            int left = j * x;
            int rr = j * x + r;
            if (left > maxA) break;
            if (rr > maxA) rr = maxA;
            // sum cnt[left..rr]
            cnt += pref[rr] - pref[left - 1];
            if (cnt >= target) break;
        }
        return cnt;
    }
}