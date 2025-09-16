import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        StringTokenizer st;

        int t = Integer.parseInt(br.readLine());
        while (t-- > 0) {
            // Read n, m
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());
            int N = n * m;

            // For each x from 1..N, record its position in A: (rA[x], cA[x])
            int[] rA = new int[N + 1], cA = new int[N + 1];
            for (int i = 0; i < n; i++) {
                st = new StringTokenizer(br.readLine());
                for (int j = 0; j < m; j++) {
                    int x = Integer.parseInt(st.nextToken());
                    rA[x] = i;
                    cA[x] = j;
                }
            }

            // Record its position in B: (rB[x], cB[x])
            int[] rB = new int[N + 1], cB = new int[N + 1];
            for (int i = 0; i < n; i++) {
                st = new StringTokenizer(br.readLine());
                for (int j = 0; j < m; j++) {
                    int x = Integer.parseInt(st.nextToken());
                    rB[x] = i;
                    cB[x] = j;
                }
            }

            // Build row‐map P: for each original row i, P[i] = target row
            int[] P = new int[n];
            Arrays.fill(P, -1);
            boolean ok = true;
            for (int x = 1; x <= N && ok; x++) {
                int fromRow = rA[x];
                int toRow = rB[x];
                if (P[fromRow] == -1) {
                    P[fromRow] = toRow;
                } else if (P[fromRow] != toRow) {
                    ok = false;  // inconsistent row‐mapping
                }
            }

            // Build column‐map Q
            int[] Q = new int[m];
            Arrays.fill(Q, -1);
            for (int x = 1; x <= N && ok; x++) {
                int fromCol = cA[x];
                int toCol = cB[x];
                if (Q[fromCol] == -1) {
                    Q[fromCol] = toCol;
                } else if (Q[fromCol] != toCol) {
                    ok = false;  // inconsistent column‐mapping
                }
            }

            // Check P is a bijection on [0..n-1], and Q is a bijection on [0..m-1]
            if (ok) {
                boolean[] usedRow = new boolean[n];
                for (int i = 0; i < n && ok; i++) {
                    if (P[i] < 0 || P[i] >= n || usedRow[P[i]]) {
                        ok = false;
                    } else {
                        usedRow[P[i]] = true;
                    }
                }
                boolean[] usedCol = new boolean[m];
                for (int j = 0; j < m && ok; j++) {
                    if (Q[j] < 0 || Q[j] >= m || usedCol[Q[j]]) {
                        ok = false;
                    } else {
                        usedCol[Q[j]] = true;
                    }
                }
            }

            out.println(ok ? "YES" : "NO");
        }

        out.flush();
    }
}