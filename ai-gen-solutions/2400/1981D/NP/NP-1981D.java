import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(in.readLine().trim());
        int[] ns = new int[t];
        for (int i = 0; i < t; i++) {
            ns[i] = Integer.parseInt(in.readLine().trim());
        }

        // Precompute the needed k for each test, and track the maximum k.
        int[] ks = new int[t];
        int maxK = 0;
        for (int i = 0; i < t; i++) {
            int n = ns[i];
            int need = n - 1; // we need >= need distinct edges
            // solve k*(k+1)/2 >= need
            // k^2 + k - 2*need >= 0  =>  k = ceil((sqrt(1+8*need)-1)/2)
            double disc = Math.sqrt(1 + 8.0 * need);
            int k0 = (int)Math.ceil((disc - 1) / 2.0);
            // force k odd:
            if ((k0 & 1) == 0) k0++;
            ks[i] = k0;
            if (k0 > maxK) maxK = k0;
        }

        // We will need at most maxK primes.  Sieve a bit above that.
        int sieveN = 200000;  // more than enough to get ~18000 primes
        boolean[] isComp = new boolean[sieveN + 1];
        ArrayList<Integer> primes = new ArrayList<>();
        for (int i = 2; i <= sieveN; i++) {
            if (!isComp[i]) {
                primes.add(i);
                if (primes.size() >= maxK) break;
                for (long j = (long)i*i; j <= sieveN; j += i) {
                    isComp[(int)j] = true;
                }
            }
        }

        // We'll process test by test.  For each k we rebuild the adjacency
        // of the graph on k nodes (with loops) and run Hierholzer.
        StringBuilder sb = new StringBuilder();
        for (int tc = 0; tc < t; tc++) {
            int n = ns[tc];
            int k = ks[tc];
            int E = k * (k + 1) / 2;        // #distinct edges
            int deg = k + 1;               // each vertex degree
            int totalAdj = k * deg;        // total adjacency entries

            // Build adjacency in flat arrays:
            //   start[u] = offset in nbr[], eid[] where u's list begins
            int[] start = new int[k+1];
            for (int u = 0; u < k; u++) {
                start[u] = u * deg;
            }
            start[k] = totalAdj;

            int[] ptr = new int[k];        // to fill adjacency
            int[] nbr = new int[totalAdj];
            int[] eid = new int[totalAdj];
            boolean[] usedEdge = new boolean[E];

            // Fill edges (u,v) for 0<=u<=v<k
            // For a loop u==v we put TWO adjacency entries at u.
            int edgeId = 0;
            for (int u = 0; u < k; u++) {
                for (int v = u; v < k; v++) {
                    int id = edgeId++;
                    // adjacency for u->v
                    int pu = start[u] + ptr[u]++;
                    nbr[pu] = v; 
                    eid[pu] = id;
                    if (u == v) {
                        // second copy for the loop
                        pu = start[u] + ptr[u]++;
                        nbr[pu] = v;
                        eid[pu] = id;
                    } else {
                        // adjacency for v->u
                        int pv = start[v] + ptr[v]++;
                        nbr[pv] = u;
                        eid[pv] = id;
                    }
                }
            }

            // Hierholzer's algorithm for Euler cycle on 0..k-1
            int[] iter = new int[k];
            int[] stack = new int[E + 1];
            int sp = 0;
            stack[sp++] = 0;

            int[] path = new int[E + 1];
            int pLen = 0;

            while (sp > 0) {
                int u = stack[sp - 1];
                int base = start[u];
                // skip used edges
                while (iter[u] < deg && usedEdge[eid[base + iter[u]]]) {
                    iter[u]++;
                }
                if (iter[u] == deg) {
                    // no more edges; backtrack
                    path[pLen++] = u;
                    sp--;
                } else {
                    int idx = base + iter[u]++;
                    int e  = eid[idx];
                    if (usedEdge[e]) continue;
                    usedEdge[e] = true;
                    int v = nbr[idx];
                    stack[sp++] = v;
                }
            }
            // Now path[0..pLen-1] is the Euler cycle in reverse.
            // We need the first n vertices in forward order:
            // forward i-th = path[pLen-1 - i].
            for (int i = 0; i < n; i++) {
                int node = path[pLen - 1 - i];
                sb.append(primes.get(node));
                if (i + 1 < n) sb.append(' ');
            }
            sb.append('\n');
        }

        // Print all answers
        System.out.print(sb.toString());
    }
}