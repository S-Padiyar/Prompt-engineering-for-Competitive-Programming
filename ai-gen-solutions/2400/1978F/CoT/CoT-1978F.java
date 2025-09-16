import java.io.*;
import java.util.*;

public class Main {
    // MAXA = maximum a[i] ≤ 1e6
    static final int MAXA = 1000000;
    static int[] spf = new int[MAXA+1];  // smallest prime factor

    // Precompute SPF sieve once
    static {
        for (int i = 2; i <= MAXA; i++) {
            if (spf[i] == 0) {
                // i is prime
                for (int j = i; j <= MAXA; j += i) {
                    if (spf[j] == 0) {
                        spf[j] = i;
                    }
                }
            }
        }
    }

    // DSU (Union-Find) for diagonals 0..n-1
    static int[] parent, rnk;
    static int[] compSize, repr; 

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
        if (rnk[a] < rnk[b]) {
            parent[a] = b;
        } else if (rnk[b] < rnk[a]) {
            parent[b] = a;
        } else {
            parent[b] = a;
            rnk[a]++;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        int t = Integer.parseInt(st.nextToken());

        // We will use a HashMap<prime, List<diagonalIndices>>
        // and clear it each test.
        HashMap<Integer, List<Integer>> primeMap = new HashMap<>();

        // DSU arrays sized to max possible n = 1e6
        parent = new int[1000005];
        rnk    = new int[1000005];
        compSize = new int[1000005];
        repr      = new int[1000005];

        while (t-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            long k = Long.parseLong(st.nextToken());  

            st = new StringTokenizer(br.readLine());
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            // 1) Count how many singletons come from a[u]==1
            long answer = 0;
            ArrayList<Integer> nonOneUs = new ArrayList<>();
            for (int u = 0; u < n; u++) {
                if (a[u] == 1) {
                    // All n cells on diagonal u are isolated
                    answer += n;
                } else {
                    // We'll put u into DSU and into primeMap
                    nonOneUs.add(u);
                }
            }

            // Initialize DSU for those diagonals
            for (int u: nonOneUs) {
                parent[u] = u;
                rnk[u] = 0;
            }

            // Build primeMap: for each diagonal u with a[u]>1, factor out distinct primes
            primeMap.clear();
            for (int u: nonOneUs) {
                int x = a[u];
                while (x > 1) {
                    int p = spf[x];
                    List<Integer> lst = primeMap.get(p);
                    if (lst == null) {
                        lst = new ArrayList<>();
                        primeMap.put(p, lst);
                    }
                    lst.add(u);
                    // divide out all copies of p
                    while (x % p == 0) {
                        x /= p;
                    }
                }
            }

            // For each prime, link those diagonals whose cyclic-distance ≤ k
            for (Map.Entry<Integer, List<Integer>> ent : primeMap.entrySet()) {
                List<Integer> pos = ent.getValue();
                Collections.sort(pos);          // sort by diagonal index
                int m = pos.size();
                if (m <= 1) continue;
                // link consecutive if direct distance ≤ k
                for (int i = 0; i+1 < m; i++) {
                    long d = (long)pos.get(i+1) - pos.get(i);
                    if (d <= k) {
                        union(pos.get(i), pos.get(i+1));
                    }
                }
                // link wrap-around if (pos[0] + n - pos[m-1]) ≤ k
                long wrap = (long)pos.get(0) + n - pos.get(m-1);
                if (wrap <= k) {
                    union(pos.get(0), pos.get(m-1));
                }
            }

            // Count meta-components via DSU
            ArrayList<Integer> roots = new ArrayList<>();
            for (int u: nonOneUs) {
                int r = find(u);
                if (compSize[r] == 0) {
                    roots.add(r);
                    repr[r] = u;   // remember one member
                }
                compSize[r]++;
            }

            // Now for each DSU-root:
            for (int r: roots) {
                int sz = compSize[r];
                if (sz > 1) {
                    // all diagonals in this meta-component fuse to ONE component
                    answer += 1;
                } else {
                    // singleton diagonal {u}
                    int u = repr[r];
                    // number of segments on diagonal u
                    if ( (u == 0) || (k >= n) ) {
                        answer += 1;
                    } else {
                        answer += 2;
                    }
                }
            }

            // cleanup compSize for next test
            for (int r: roots) {
                compSize[r] = 0;
            }

            // Done with this test
            System.out.println(answer);
        }

        br.close();
    }
}