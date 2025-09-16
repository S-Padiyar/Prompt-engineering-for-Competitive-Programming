import java.io.*;
import java.util.*;

public class Main {
    static final int INF = 1000000000;
    // We'll reuse this array to map divisor-value -> its index in 'divs'.
    static int[] val2idx = new int[100001];  // x <= 1e5

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter  pw = new PrintWriter(new OutputStreamWriter(System.out));

        // initialize val2idx to -1
        Arrays.fill(val2idx, -1);

        int t = Integer.parseInt(br.readLine());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int x = Integer.parseInt(st.nextToken());
            int[] a = new int[n+1];
            st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            // 1) Factor x and enumerate its divisors
            List<Integer> primes = new ArrayList<>();
            List<Integer> exps   = new ArrayList<>();
            int tmp = x;
            for (int p = 2; p*p <= tmp; p++) {
                if (tmp % p == 0) {
                    int e = 0;
                    while (tmp % p == 0) {
                        tmp /= p; e++;
                    }
                    primes.add(p);
                    exps.add(e);
                }
            }
            if (tmp > 1) {
                primes.add(tmp);
                exps.add(1);
            }
            // Recursively build all divisors of x:
            List<Integer> divs = new ArrayList<>();
            buildDivisors(0, 1, primes, exps, divs);
            Collections.sort(divs);
            int D = divs.size();

            // map each divisor value -> its index
            for (int i = 0; i < D; i++) {
                val2idx[divs.get(i)] = i;
            }

            // Precompute rem[i] = index of divisor x/divs[i]
            int[] rem = new int[D];
            for (int i = 0; i < D; i++) {
                int v = divs.get(i);
                rem[i] = val2idx[x / v];
            }

            // Precompute transitions: trans[s][d] = index of s_div * d_div if it still divides x, else -1
            int[][] trans = new int[D][D];
            for (int s = 0; s < D; s++) {
                int sv = divs.get(s);
                for (int d = 0; d < D; d++) {
                    long prod = 1L * sv * divs.get(d);
                    if (prod <= x && x % prod == 0) {
                        trans[s][d] = val2idx[(int)prod];
                    } else {
                        trans[s][d] = -1;
                    }
                }
            }

            // f[state] = smallest index j where a subset of inserted cards can produce divisor 'state'
            // initialize to INF
            int[] f = new int[D];
            Arrays.fill(f, INF);

            // We do the DP for the segment-partition problem:
            //  dp[i] = 1 + min{ dp[j] : pos_i ≤ j < i }
            // we'll maintain a monotonic deque over dp[] to get range-min in O(1)/step
            Deque<Integer> dq = new ArrayDeque<>();
            int[] dp = new int[n+1];
            dp[0] = 0;
            dq.add(0);

            int pos = 0;  // pos_i = leftmost 'j' that is forbidden; segment must start > pos

            for (int i = 1; i <= n; i++) {
                int bestStart = 0;

                // Only if a[i] divides x do we possibly form new dangerous subset:
                if (x % a[i] == 0) {
                    int dState = val2idx[a[i]];
                    int rState = rem[dState];
                    if (f[rState] < INF) {
                        // we found a subset in the prefix whose product = x/a[i],
                        // so adding this card closes the product to x, subset ends at i,
                        // and the minimal index in that subset is f[rState].
                        bestStart = f[rState];
                    }
                }

                // Update pos_i
                pos = Math.max(pos, bestStart);

                // Now dp[i] = 1 + min_{j in [pos..i-1]} dp[j].
                // Pop from front of deque any index < pos
                while (!dq.isEmpty() && dq.peekFirst() < pos) {
                    dq.pollFirst();
                }
                // front of deque now gives argmin j in [pos..i-1]
                dp[i] = dp[dq.peekFirst()] + 1;

                // push i into our deque of dp-indices, maintaining increasing dp[] values
                while (!dq.isEmpty() && dp[dq.peekLast()] >= dp[i]) {
                    dq.pollLast();
                }
                dq.addLast(i);

                // Finally update our small subset‐sum DP 'f' with this new card,
                // but only if a[i] divides x.
                if (x % a[i] == 0) {
                    int dState = val2idx[a[i]];
                    // Copy of f to avoid double‐counting the same card twice
                    int[] g = Arrays.copyOf(f, D);
                    // Combine the new card's divisor dState with every old state
                    for (int s = 0; s < D; s++) {
                        if (g[s] < INF) {
                            int t = trans[s][dState];
                            if (t >= 0) {
                                // a subset that made 's' had minimal index g[s],
                                // adding the new card (index i) we get 't',
                                // whose minimal index is min(g[s], i).
                                int mi = g[s] < i ? g[s] : i;
                                if (mi < f[t]) {
                                    f[t] = mi;
                                }
                            }
                        }
                    }
                    // Also the subset of size 1 = {this card} makes divisor = a[i]:
                    if (i < f[dState]) {
                        f[dState] = i;
                    }
                }
            }

            pw.println(dp[n]);

            // clean up val2idx for this test
            for (int v : divs) {
                val2idx[v] = -1;
            }
        }

        pw.flush();
        pw.close();
    }

    // Recursively build all divisors of x given its prime factorization
    static void buildDivisors(int idx, int curr,
                              List<Integer> primes,
                              List<Integer> exps,
                              List<Integer> out) {
        if (idx == primes.size()) {
            out.add(curr);
            return;
        }
        int p = primes.get(idx);
        int e = exps.get(idx);
        int val = curr;
        for (int i = 0; i <= e; i++) {
            buildDivisors(idx+1, val, primes, exps, out);
            val *= p;
        }
    }
}