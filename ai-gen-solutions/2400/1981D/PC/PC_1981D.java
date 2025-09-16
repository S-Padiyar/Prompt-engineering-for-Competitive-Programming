import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            int m = n - 1;  // need m distinct adjacent‐pair products

            // 1) Compute minimal k
            int k;
            if (m <= 1) {
                k = 1;
            } else if (m <= 3) {
                k = 2;
            } else {
                double D = 1.0 + 8.0 * m;
                double root = Math.sqrt(D);
                int kk = (int)((root - 1.0) / 2.0);
                while ((long)kk * (kk + 1) / 2 < m) {
                    kk++;
                }
                if (kk > 2 && (kk % 2 == 0)) {
                    kk++;
                }
                k = kk;
            }

            // 2) Build the multigraph: k vertices, loops and one edge for each i<j
            int E = k * (k + 1) / 2;            // total edges
            int[] head = new int[k + 1];
            int[] nxt  = new int[2 * E];
            int[] to   = new int[2 * E];
            int[] eid  = new int[2 * E];
            int[] deg  = new int[k + 1];
            Arrays.fill(head, -1);
            int ec = 0, id = 0;

            for (int i = 1; i <= k; i++) {
                for (int j = i; j <= k; j++) {
                    if (i == j) {
                        // loop at i: two incidences
                        to[ec] = j; eid[ec] = id; nxt[ec] = head[i]; head[i] = ec; deg[i]++; ec++;
                        to[ec] = j; eid[ec] = id; nxt[ec] = head[i]; head[i] = ec; deg[i]++; ec++;
                    } else {
                        // edge i--j
                        to[ec] = j; eid[ec] = id; nxt[ec] = head[i]; head[i] = ec; deg[i]++; ec++;
                        to[ec] = i; eid[ec] = id; nxt[ec] = head[j]; head[j] = ec; deg[j]++; ec++;
                    }
                    id++;
                }
            }

            // 3) Find a start vertex (for k=2 we start at an odd‐degree vertex)
            int start = 1, oddCount = 0, firstOdd = 1;
            for (int i = 1; i <= k; i++) {
                if ((deg[i] & 1) == 1) {
                    oddCount++;
                    if (oddCount == 1) {
                        firstOdd = i;
                    }
                }
            }
            if (oddCount == 2) {
                start = firstOdd;
            }

            // 4) Hierholzer to find Eulerian trail/circuit
            boolean[] usedEdge = new boolean[id];
            int[] cur = new int[k + 1];
            System.arraycopy(head, 0, cur, 0, k + 1);
            int[] stack = new int[E + 1], circuit = new int[E + 1];
            int sp = 0, cp = 0;

            stack[sp++] = start;
            while (sp > 0) {
                int v = stack[sp - 1];
                int c = cur[v];
                while (c != -1 && usedEdge[eid[c]]) {
                    c = nxt[c];
                }
                cur[v] = c;
                if (c == -1) {
                    // backtrack
                    sp--;
                    circuit[cp++] = v;
                } else {
                    // traverse edge c
                    usedEdge[eid[c]] = true;
                    cur[v] = nxt[c];
                    stack[sp++] = to[c];
                }
            }

            // 5) Output the first n vertices of the reversed circuit
            for (int i = 0; i < n; i++) {
                int val = circuit[cp - 1 - i];
                sb.append(val).append(i + 1 < n ? ' ' : '\n');
            }
        }

        // print all answers
        System.out.print(sb.toString());
    }
}