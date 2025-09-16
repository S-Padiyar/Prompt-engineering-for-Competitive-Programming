import java.util.*;

public class Main {
    static int[] parent, rank;
    static int[] head, nextNode;
    static int[] ansU, ansV;
    static int n;
    static int[] a;

    // DSU find with path compression
    static int findSet(int x) {
        if (parent[x] != x) {
            parent[x] = findSet(parent[x]);
        }
        return parent[x];
    }

    // DSU union by rank
    static void unionSet(int x, int y) {
        x = findSet(x);
        y = findSet(y);
        if (x == y) return;
        if (rank[x] < rank[y]) {
            parent[x] = y;
        } else if (rank[y] < rank[x]) {
            parent[y] = x;
        } else {
            parent[y] = x;
            rank[x]++;
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int t = in.nextInt();

        while (t-- > 0) {
            n = in.nextInt();
            a = new int[n+1];
            for (int i = 1; i <= n; i++) {
                a[i] = in.nextInt();
            }

            // Initialize DSU
            parent = new int[n+1];
            rank   = new int[n+1];
            for (int i = 1; i <= n; i++) {
                parent[i] = i;
                rank[i]   = 0;
            }

            // Prepare storage for answers: ansU[x], ansV[x] = edge chosen at label x
            ansU = new int[n];
            ansV = new int[n];

            // Helper arrays for residue-bucketing
            head     = new int[n];    // we'll only use indices [0..x-1]
            nextNode = new int[n+1];  // nextNode[i] chains i in the bucket list

            boolean possible = true;

            // Build edges from largest x (n-1) down to 1
            for (int x = n - 1; x >= 1; x--) {
                // Reset head[0..x-1] = -1
                Arrays.fill(head, 0, x, -1);

                // Bucket each vertex i by its residue r = a[i] % x
                for (int i = 1; i <= n; i++) {
                    int r = a[i] % x;
                    nextNode[i] = head[r];
                    head[r]     = i;
                }

                // Attempt to find any cross-component pair in the same residue
                boolean found = false;
                for (int r = 0; r < x && !found; r++) {
                    int listHead = head[r];
                    if (listHead == -1) continue;
                    int repNode = -1, repRoot = -1;
                    // Walk the bucket's linked list
                    for (int cur = listHead; cur != -1; cur = nextNode[cur]) {
                        int root = findSet(cur);
                        if (repNode < 0) {
                            // first node in this residue
                            repNode = cur;
                            repRoot = root;
                        } else if (root != repRoot) {
                            // found u = repNode, v = cur in different DSU components
                            unionSet(repNode, cur);
                            ansU[x] = repNode;
                            ansV[x] = cur;
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    possible = false;
                    break;
                }
            }

            if (!possible) {
                System.out.println("No");
            } else {
                System.out.println("Yes");
                // Output edges in order x=1..n-1
                for (int x = 1; x <= n - 1; x++) {
                    System.out.println(ansU[x] + " " + ansV[x]);
                }
            }
        }

        in.close();
    }
}