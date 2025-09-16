import java.io.*;
import java.util.*;

public class Main {
    static final int MAXA = 400_000;
    static ArrayList<Integer>[] divs = new ArrayList[MAXA+1];

    // for reverse BFS
    static int[] visRev = new int[MAXA+1];
    static int  revTS = 1;

    // for forward BFS
    static int[] visFwd = new int[MAXA+1];
    static int  fwdTS = 1;

    // to map a value up to MAXA -> index in current test's a[]
    // or -1 if not present
    static int[] idxOf = new int[MAXA+1];

    public static void main(String[] args) throws IOException {
        // Precompute divisors
        for (int i = 1; i <= MAXA; i++) {
            divs[i] = new ArrayList<>();
        }
        for (int d = 1; d <= MAXA; d++) {
            for (int multiple = d; multiple <= MAXA; multiple += d) {
                divs[multiple].add(d);
            }
        }
        Arrays.fill(idxOf, -1);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(br.readLine().trim());
        StringTokenizer st;
        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            int[] a = new int[n];
            st = new StringTokenizer(br.readLine());
            int minA = Integer.MAX_VALUE, maxA = 0;
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
                minA = Math.min(minA, a[i]);
                maxA = Math.max(maxA, a[i]);
                idxOf[a[i]] = i;
            }
            // Sort just so we know minA = a[0]
            Arrays.sort(a);

            // 1) Reverse BFS from m = a[0] to collect all x that can generate m
            ArrayList<Integer> candidates = new ArrayList<>();
            revTS++;
            int[] queue = new int[MAXA+1];
            int ql = 0, qr = 0;

            // Start from m
            queue[qr++] = a[0];
            visRev[a[0]] = revTS;

            while (ql < qr) {
                int v = queue[ql++];
                candidates.add(v);
                // For each divisor d>=2 of v, predecessor is v-d
                for (int d : divs[v]) {
                    if (d < 2) continue;
                    int u = v - d;
                    if (u < 2) break; // all smaller u won't be >=2
                    if (visRev[u] != revTS) {
                        visRev[u] = revTS;
                        queue[qr++] = u;
                    }
                }
            }

            // 2) Try each candidate x in descending order
            Collections.sort(candidates, Collections.reverseOrder());
            int answer = -1;

            for (int x : candidates) {
                // Quickly skip if x > every a[i] (impossible)
                if (x > maxA) continue;

                // Forward BFS from x, see if we visit all a[i]
                fwdTS++;
                int reachedCount = 0;
                queue = new int[MAXA+1];
                ql = 0; qr = 0;
                queue[qr++] = x;
                visFwd[x] = fwdTS;

                // Check if x itself is one of the a's
                if (idxOf[x] >= 0) reachedCount++;

                while (ql < qr && reachedCount < n) {
                    int v = queue[ql++];
                    for (int d : divs[v]) {
                        if (d < 2) continue;
                        int w = v + d;
                        if (w > maxA) continue;
                        if (visFwd[w] != fwdTS) {
                            visFwd[w] = fwdTS;
                            queue[qr++] = w;
                            int ix = idxOf[w];
                            if (ix >= 0) {
                                reachedCount++;
                                if (reachedCount == n) break;
                            }
                        }
                    }
                }
                if (reachedCount == n) {
                    answer = x;
                    break;
                }
            }

            sb.append(answer).append('\n');

            // Cleanup idxOf for next test
            for (int v : a) {
                idxOf[v] = -1;
            }
        }

        System.out.print(sb);
    }
}