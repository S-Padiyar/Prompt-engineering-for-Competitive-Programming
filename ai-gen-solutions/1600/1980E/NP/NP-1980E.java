import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int t = Integer.parseInt(in.readLine().trim());
        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());
            int NM = n * m;

            // rowA[v], colA[v] = position of value v in A
            int[] rowA = new int[NM + 1];
            int[] colA = new int[NM + 1];

            // Read A and record positions
            for (int i = 0; i < n; i++) {
                st = new StringTokenizer(in.readLine());
                for (int j = 0; j < m; j++) {
                    int v = Integer.parseInt(st.nextToken());
                    rowA[v] = i;
                    colA[v] = j;
                }
            }

            // We will record, for each row i of B, which row in A all its entries come from
            // likewise for each column j of B, which column in A they come from.
            int[] rowMap = new int[n];
            int[] colMap = new int[m];
            Arrays.fill(rowMap, -1);
            Arrays.fill(colMap, -1);

            boolean ok = true;

            // Read B and check consistency
            for (int i = 0; i < n; i++) {
                st = new StringTokenizer(in.readLine());
                for (int j = 0; j < m; j++) {
                    int v = Integer.parseInt(st.nextToken());
                    if (!ok) {
                        // Still consume input tokens even if already failed
                        continue;
                    }
                    int ra = rowA[v];
                    int ca = colA[v];

                    // Check row consistency for row i of B
                    if (rowMap[i] < 0) {
                        rowMap[i] = ra;
                    } else if (rowMap[i] != ra) {
                        ok = false;
                    }

                    // Check column consistency for column j of B
                    if (colMap[j] < 0) {
                        colMap[j] = ca;
                    } else if (colMap[j] != ca) {
                        ok = false;
                    }
                }
            }

            sb.append(ok ? "YES\n" : "NO\n");
        }

        System.out.print(sb);
    }
}