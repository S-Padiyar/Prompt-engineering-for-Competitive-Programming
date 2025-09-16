import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static StringTokenizer st;

    public static void main(String[] args) throws IOException {
        // Read n, k, q
        st = new StringTokenizer(br.readLine());
        int n = Integer.parseInt(st.nextToken());
        int k = Integer.parseInt(st.nextToken());
        int q = Integer.parseInt(st.nextToken());

        // We'll build the matrix b[i][j] on the fly into a flattened array:
        // flat[j*n + i] = b[i][j], where i in [0..n-1], j in [0..k-1].
        int[] flat = new int[n * k];
        int[] prefixOR = new int[k];  // column-wise running OR

        // Read each row of a[], accumulate into prefixOR and store in flat
        for (int i = 0; i < n; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < k; j++) {
                int aij = Integer.parseInt(st.nextToken());
                prefixOR[j] |= aij;
                flat[j * n + i] = prefixOR[j];
            }
        }

        StringBuilder answer = new StringBuilder();

        // Process queries
        for (int _q = 0; _q < q; _q++) {
            // Number of requirements in this query
            st = new StringTokenizer(br.readLine());
            int m = Integer.parseInt(st.nextToken());

            // We'll keep an intersection [L,R], 1-based.  Initially [1..n].
            int L = 1, R = n;
            boolean possible = true;

            // Read and apply each requirement
            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(br.readLine());
                int r = Integer.parseInt(st.nextToken()) - 1;  // 0-based column
                char op = st.nextToken().charAt(0);
                int c = Integer.parseInt(st.nextToken());

                if (!possible) {
                    // We already know it's impossible, just consume input
                    continue;
                }

                int base = r * n;  // offset in 'flat' for column r

                if (op == '<') {
                    // we need b[i,r] < c
                    // find the first index p with b[p]>=c  (lowerBound)
                    int p = lowerBound(flat, base, n, c);
                    // valid rows are [1..p]
                    //   since p is 0-based, that means [1..p] in 1-based is [1..p]
                    //     (if p==0, it's empty; if p==n, it means all are <c)
                    R = Math.min(R, p);
                } else {
                    // op == '>'
                    // we need b[i,r] > c
                    // find first index p with b[p]>c  (upperBound)
                    int p = upperBound(flat, base, n, c);
                    // valid rows are [p+1..n] in 1-based
                    L = Math.max(L, p + 1);
                }

                if (L > R) {
                    possible = false;
                }
            }

            // Output the result for this query
            answer.append((possible && L <= R) ? L : -1)
                  .append('\n');
        }

        // Print all answers
        System.out.print(answer);
    }

    // Find first index in flat[base..base+len-1] with value >= key
    // Returns an index in [0..len], i.e. 'len' if none >= key.
    static int lowerBound(int[] flat, int base, int len, int key) {
        int lo = 0, hi = len;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (flat[base + mid] >= key) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return lo;
    }

    // Find first index in flat[base..base+len-1] with value > key
    // Returns an index in [0..len], i.e. 'len' if none > key.
    static int upperBound(int[] flat, int base, int len, int key) {
        int lo = 0, hi = len;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (flat[base + mid] > key) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return lo;
    }
}