import java.io.*;
import java.util.*;

public class Main {
    static final int SMALL = 1200;  // threshold between "small x" and "large x"

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder output = new StringBuilder();
        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int q = Integer.parseInt(st.nextToken());

            int[] a = new int[n];
            st = new StringTokenizer(in.readLine());
            int maxA = 0;
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
                if (a[i] > maxA) maxA = a[i];
            }

            // freq[v] = how many times value v appears
            int[] freq = new int[maxA + 1];
            for (int v : a) freq[v]++;

            // build the list of distinct values
            ArrayList<Integer> vals = new ArrayList<>();
            for (int v = 0; v <= maxA; v++) {
                if (freq[v] > 0) vals.add(v);
            }

            // prefix sums S[v] = number of elements <= v
            int[] S = new int[maxA + 1];
            S[0] = freq[0];
            for (int v = 1; v <= maxA; v++) {
                S[v] = S[v - 1] + freq[v];
            }

            // median position
            int mpos = (n + 2) / 2;

            // compute original median (for any x > maxA, no reduction is possible)
            int origMedian = 0;
            for (int v = 0; v <= maxA; v++) {
                if (S[v] >= mpos) {
                    origMedian = v;
                    break;
                }
            }

            // read all queries, mark which x appear
            int[] queries = new int[q];
            boolean[] seen = new boolean[maxA + q + 5]; 
            // size is maxA+q+5 to safely cover x up to n even if n>maxA
            int maxQ = 0;
            for (int i = 0; i < q; i++) {
                int x = Integer.parseInt(in.readLine().trim());
                queries[i] = x;
                if (!seen[x]) {
                    seen[x] = true;
                    if (x > maxQ) maxQ = x;
                }
            }

            // answers[x] will hold the median-of-remainders for that x
            int[] answers = new int[maxQ + 1];
            Arrays.fill(answers, -1);

            // special case: any x > maxA -> same as original median
            for (int x = maxA + 1; x <= maxQ; x++) {
                if (seen[x]) answers[x] = origMedian;
            }

            // === Handle small x ===
            // We'll reuse a small array f[] of size SMALL to accumulate counts
            int[] f = new int[SMALL];
            for (int x = 1; x <= maxQ && x <= SMALL; x++) {
                if (!seen[x]) continue;
                if (answers[x] != -1) continue;
                // clear f[0..x-1]
                for (int r = 0; r < x; r++) f[r] = 0;
                // accumulate freq[v] into remainder classes v % x
                for (int v : vals) {
                    int r = v % x;
                    f[r] += freq[v];
                }
                // find the median remainder
                int cum = 0, ans = 0;
                for (int r = 0; r < x; r++) {
                    cum += f[r];
                    if (cum >= mpos) {
                        ans = r;
                        break;
                    }
                }
                answers[x] = ans;
            }

            // === Handle large x ===
            // A helper to compute how many a_i have (a_i % x) <= R
            for (int x = 1; x <= maxQ; x++) {
                if (!seen[x] || answers[x] != -1) continue;
                // We'll do a binary search on r in [0..x-1]
                int lo = 0, hi = x - 1;
                while (lo < hi) {
                    int mid = (lo + hi) >>> 1;
                    // compute P(mid) = # of a_i with (a_i % x) <= mid
                    int total = 0;
                    for (int start = 0; start <= maxA; start += x) {
                        int end = start + mid;
                        if (end > maxA) end = maxA;
                        int blockCount = S[end] - (start > 0 ? S[start - 1] : 0);
                        total += blockCount;
                        if (total >= mpos) break;  // early stop
                    }
                    if (total >= mpos) {
                        hi = mid;
                    } else {
                        lo = mid + 1;
                    }
                }
                answers[x] = lo;
            }

            // output the answers in the original query order
            for (int i = 0; i < q; i++) {
                output.append(answers[queries[i]]).append(' ');
                // clear seen[] for reuse in the next test
                // we only need to clear the ones we actually saw
            }
            output.append('\n');

            // cleanup the 'seen' array for the x's we used
            for (int i = 0; i < q; i++) {
                seen[queries[i]] = false;
            }
        }

        System.out.print(output);
    }
}