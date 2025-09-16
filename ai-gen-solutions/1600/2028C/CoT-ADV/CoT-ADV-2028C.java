import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());
            long v = Long.parseLong(st.nextToken());

            long[] a = new long[n+1];
            st = new StringTokenizer(in.readLine());
            for (int i = 1; i <= n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }

            // Prefix sums for fast subarray sum queries
            long[] pref = new long[n+1];
            for (int i = 1; i <= n; i++) {
                pref[i] = pref[i-1] + a[i];
            }

            // Greedily cut from the left
            // L[k] = position where the k-th segment (sum>=v) ends.
            ArrayList<Integer> L = new ArrayList<>();
            L.add(0);  // L[0] = 0 means "no left segments"
            long sum = 0;
            for (int i = 1; i <= n; i++) {
                sum += a[i];
                if (sum >= v) {
                    L.add(i);
                    sum = 0;
                }
            }
            int A = L.size() - 1;  // number of full segments we got from left

            // Greedily cut from the right
            // R[k] = starting index of the k-th segment from the right
            ArrayList<Integer> R = new ArrayList<>();
            R.add(n + 1);  // R[0] = n+1 means "no right segments"
            sum = 0;
            for (int i = n; i >= 1; i--) {
                sum += a[i];
                if (sum >= v) {
                    R.add(i);
                    sum = 0;
                }
            }
            int B = R.size() - 1;  // number of full segments from right

            // If even greedily we can't muster m segments total, impossible
            if (A + B < m) {
                out.println(-1);
                continue;
            }

            // Try splitting beasts segments i from left, m-i from right
            long ans = -1;
            // i ranges from max(0, m-B) to min(A, m)
            int from = Math.max(0, m - B);
            int to   = Math.min(A, m);
            for (int i = from; i <= to; i++) {
                int j = m - i;      // segments from right
                int leftEnd  = L.get(i);      // last index used by left beasts
                int rightBeg = R.get(j);      // first index used by right beasts
                // Beasts regions must be disjoint
                if (leftEnd < rightBeg) {
                    // Alice's chunk is [leftEnd+1 .. rightBeg-1]
                    int lo = leftEnd + 1;
                    int hi = rightBeg - 1;
                    long candidate = (lo <= hi
                                      ? pref[hi] - pref[leftEnd]
                                      : 0L);
                    if (candidate > ans) ans = candidate;
                }
            }

            out.println(ans);
        }

        out.flush();
    }
}