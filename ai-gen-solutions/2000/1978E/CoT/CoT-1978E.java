import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        int T = Integer.parseInt(br.readLine().trim());
        while (T-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            String s = br.readLine();
            String t = br.readLine();

            // We build arrays 1-based, with a little padding.
            int[] A   = new int[n+3];  // A[k]=1 if s[k]=='0'
            int[] L1  = new int[n+3];
            int[] L2  = new int[n+3];
            int[] R1  = new int[n+3];
            int[] R2  = new int[n+3];
            int[] preS1 = new int[n+3]; // prefix sum of (s[k]=='1')
            
            // Fill A and preS1
            for (int i = 1; i <= n; i++) {
                char sc = s.charAt(i-1);
                A[i] = (sc == '0') ? 1 : 0;
                preS1[i] = preS1[i-1] + (sc == '1' ? 1 : 0);
            }

            // Fill L1, L2, R1, R2
            for (int k = 1; k <= n; k++) {
                // L1[k] = t[k-1]=='1' ?
                if (k >= 2 && t.charAt(k-2) == '1') L1[k] = 1;
                // L2[k] = (k>=3 && s[k-2]=='0') ?
                if (k >= 3 && s.charAt(k-3) == '0')   L2[k] = 1;
                // R1[k] = t[k+1]=='1' ?
                if (k <= n-1 && t.charAt(k) == '1')   R1[k] = 1;
                // R2[k] = (k<=n-2 && s[k+2]=='0') ?
                if (k <= n-2 && s.charAt(k+1) == '0') R2[k] = 1;
            }

            // Build four prefix sums for the four types.
            int[] pre1 = new int[n+3];
            int[] pre2 = new int[n+3];
            int[] pre3 = new int[n+3];
            int[] pre4 = new int[n+3];

            for (int k = 1; k <= n; k++) {
                pre1[k] = pre1[k-1];
                pre2[k] = pre2[k-1];
                pre3[k] = pre3[k-1];
                pre4[k] = pre4[k-1];

                if (A[k] == 1) {
                    boolean l1 = L1[k]==1, l2 = L2[k]==1;
                    boolean r1 = R1[k]==1, r2 = R2[k]==1;
                    // Type 1:   L1 & R1
                    if (l1 && r1) {
                        pre1[k]++;
                    }
                    // Type 2:  L1 & !R1 & R2
                    else if (l1 && !r1 && r2) {
                        pre2[k]++;
                    }
                    // Type 3:  !L1 & L2 & R1
                    else if (!l1 && l2 && r1) {
                        pre3[k]++;
                    }
                    // Type 4:  !L1 & L2 & !R1 & R2
                    else if (!l1 && l2 && !r1 && r2) {
                        pre4[k]++;
                    }
                }
            }

            // Now answer queries
            int q = Integer.parseInt(br.readLine().trim());
            while (q-- > 0) {
                StringTokenizer st = new StringTokenizer(br.readLine());
                int L = Integer.parseInt(st.nextToken());
                int R = Integer.parseInt(st.nextToken());

                // 1) count original ones in s[L..R]
                int ones = preS1[R] - preS1[L-1];

                // 2) count how many zeros can flip
                int flips = 0;
                int k1 = L+1, k2 = R-1;
                if (k1 <= k2) {
                    // type1 in [k1..k2]
                    flips += pre1[k2] - pre1[k1-1];
                    // type2 in [k1..R-2]
                    int e2 = R-2;
                    if (e2 >= k1) flips += pre2[e2] - pre2[k1-1];
                    // type3 in [L+2..k2]
                    int s3 = L+2;
                    if (s3 <= k2) flips += pre3[k2] - pre3[s3-1];
                    // type4 in [L+2..R-2]
                    if (s3 <= e2) flips += pre4[e2] - pre4[s3-1];
                }

                sb.append(ones + flips).append('\n');
            }
        }

        System.out.print(sb);
    }
}