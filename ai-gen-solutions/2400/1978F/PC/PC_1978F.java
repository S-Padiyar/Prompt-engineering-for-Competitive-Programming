import java.io.*;
import java.util.*;

public class Main {
    static final int MAXA = 1_000_000;
    // spf[x] = smallest prime factor of x
    static int[] spf = new int[MAXA + 1];

    // Build smallest-prime-factor sieve in O(MAXA)
    static void buildSieve() {
        for (int i = 2; i <= MAXA; i++) {
            if (spf[i] == 0) {
                // i is prime
                for (long j = i; j <= MAXA; j += i) {
                    if (spf[(int) j] == 0) {
                        spf[(int) j] = i;
                    }
                }
            }
        }
    }

    // Union-Find / Disjoint Set
    static int[] parent, rank;
    static int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]);
        }
        return parent[x];
    }
    static void union(int a, int b) {
        a = find(a);
        b = find(b);
        if (a == b) return;
        // union by rank
        if (rank[a] < rank[b]) {
            parent[a] = b;
        } else if (rank[b] < rank[a]) {
            parent[b] = a;
        } else {
            parent[b] = a;
            rank[a]++;
        }
    }

    public static void main(String[] args) throws IOException {
        buildSieve();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));

        int t = Integer.parseInt(br.readLine().trim());

        // lastPos[p] = last index i where prime p was seen in current test
        // 0 means "not seen yet"
        int[] lastPos = new int[MAXA + 1];
        // We'll keep track of which primes we actually used,
        // so we can reset only those entries in lastPos.
        ArrayList<Integer> usedPrimes = new ArrayList<>();

        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int k = Integer.parseInt(st.nextToken());
            st = new StringTokenizer(br.readLine());
            int[] a = new int[n+1];
            for (int i = 1; i <= n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            // Initialize union-find
            parent = new int[n+1];
            rank   = new int[n+1];
            for (int i = 1; i <= n; i++) {
                parent[i] = i;
                rank[i]   = 0;
            }

            usedPrimes.clear();

            // Sweep over the array indices
            for (int i = 1; i <= n; i++) {
                int x = a[i];
                // Factor x via spf[], collecting distinct primes
                int lastP = -1;
                while (x > 1) {
                    int p = spf[x];
                    if (p != lastP) {
                        if (lastPos[p] == 0) {
                            // first time seeing prime p in this test
                            usedPrimes.add(p);
                        } else {
                            int prevI = lastPos[p];
                            // if within distance k in the diagonal-sense we union
                            if (i - prevI <= k) {
                                union(i, prevI);
                            }
                        }
                        lastPos[p] = i;
                        lastP = p;
                    }
                    x /= p;
                }
            }

            // Count distinct roots => number of connected components
            int comps = 0;
            for (int i = 1; i <= n; i++) {
                if (find(i) == i) comps++;
            }
            pw.println(comps);

            // Reset lastPos for the primes we used
            for (int p : usedPrimes) {
                lastPos[p] = 0;
            }
        }

        pw.flush();
        pw.close();
        br.close();
    }
}