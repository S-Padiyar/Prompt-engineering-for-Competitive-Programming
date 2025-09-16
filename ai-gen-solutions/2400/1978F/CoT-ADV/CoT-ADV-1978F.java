import java.io.*;
import java.util.*;

public class Main {
    static final int MAXA = 1_000_000;
    static int[] spf = new int[MAXA+1];
    // occ[p] will hold the list of diagonal‐indices i where a[i] % p == 0
    static ArrayList<Integer>[] occ = new ArrayList[MAXA+1];
    static {
        // Sieve for smallest prime factor
        for (int i = 2; i <= MAXA; i++) {
            if (spf[i] == 0) {
                // i is prime
                for (long j = i; j <= MAXA; j += i) {
                    if (spf[(int)j] == 0) {
                        spf[(int)j] = i;
                    }
                }
            }
        }
        // Initialize the lists lazily
        for (int i = 0; i <= MAXA; i++) {
            occ[i] = new ArrayList<>();
        }
    }

    // Simple union‐find (disjoint‐set) on 0..n-1
    static class UF {
        int[] parent, rank;
        int comps;
        UF(int n) {
            parent = new int[n];
            rank   = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
                rank[i]   = 0;
            }
            comps = n;
        }
        int find(int x) {
            return parent[x]==x ? x : (parent[x]=find(parent[x]));
        }
        void union(int x, int y) {
            x = find(x);
            y = find(y);
            if (x == y) return;
            // union by rank
            if (rank[x] < rank[y]) {
                parent[x] = y;
            } else if (rank[x] > rank[y]) {
                parent[y] = x;
            } else {
                parent[y] = x;
                rank[x]++;
            }
            comps--;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer tok = new StringTokenizer(in.readLine());
        int t = Integer.parseInt(tok.nextToken());

        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            tok = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(tok.nextToken());
            int k = Integer.parseInt(tok.nextToken());

            tok = new StringTokenizer(in.readLine());
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(tok.nextToken());
            }

            UF uf = new UF(n);

            // Keep track of which primes we actually used, so we can clear their lists later
            ArrayList<Integer> usedPrimes = new ArrayList<>();

            // 1) Factor each a[i], record each prime => append i to occ[p]
            for (int i = 0; i < n; i++) {
                int x = a[i];
                // factor x into distinct primes
                int lastP = -1;
                while (x > 1) {
                    int p = spf[x];
                    if (p != lastP) {
                        if (occ[p].isEmpty()) {
                            usedPrimes.add(p);
                        }
                        occ[p].add(i);
                        lastP = p;
                    }
                    x /= p;
                }
            }

            // 2) For each used prime p, sort its occ[p]-list and union neighbors on the ring
            for (int p : usedPrimes) {
                ArrayList<Integer> lst = occ[p];
                if (lst.size() > 1) {
                    Collections.sort(lst);
                    // union consecutive on the _circular_ ring of size n
                    for (int i = 0; i + 1 < lst.size(); i++) {
                        int d1 = lst.get(i), d2 = lst.get(i+1);
                        int diff = d2 - d1;
                        int ring = Math.min(diff, n - diff);
                        if (ring <= k) {
                            uf.union(d1, d2);
                        }
                    }
                    // also the wrap from last back to first
                    int d1 = lst.get(lst.size()-1), d2 = lst.get(0);
                    int diff = (d2 + n) - d1;         // linear
                    int ring = Math.min(diff, n - diff);
                    if (ring <= k) {
                        uf.union(d1, d2);
                    }
                }
                // clear for next test
                lst.clear();
            }

            // 3) Number of connected components = uf.comps
            sb.append(uf.comps).append('\n');
        }

        // Print all answers
        System.out.print(sb);
    }
}