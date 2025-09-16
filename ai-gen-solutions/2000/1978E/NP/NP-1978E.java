import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int T = Integer.parseInt(br.readLine().trim());
        while (T-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            String s = br.readLine().trim();
            String t = br.readLine().trim();

            // 1-based indexing
            int[] s0 = new int[n+2];   // s0[i]=1 if s[i]=='0'
            int[] t1 = new int[n+2];   // t1[i]=1 if t[i]=='1'
            for (int i = 1; i <= n; i++) {
                s0[i] = (s.charAt(i-1) == '0' ? 1 : 0);
                t1[i] = (t.charAt(i-1) == '1' ? 1 : 0);
            }

            // Build ALeft and ARight
            int[] ALeft  = new int[n+2];
            int[] ARight = new int[n+2];
            // ALeft[j] = 1 if t[j-1]=='1' OR (s[j-2]==s[j]==0), for j>=3
            for (int j = 3; j <= n; j++) {
                if (t1[j-1] == 1 || (s0[j-2]==1 && s0[j]==1)) {
                    ALeft[j] = 1;
                }
            }
            // ARight[j] = 1 if t[j+1]=='1' OR (s[j]==s[j+2]==0), for j<=n-2
            for (int j = 1; j <= n-2; j++) {
                if (t1[j+1] == 1 || (s0[j]==1 && s0[j+2]==1)) {
                    ARight[j] = 1;
                }
            }

            // Prefix sums
            int[] pZ   = new int[n+2];  // count of zeros in s
            int[] pT   = new int[n+2];  // count of ones in t
            int[] pZL  = new int[n+2];  // count of positions i where s0[i] & ALeft[i]
            int[] pZR  = new int[n+2];  // count of positions i where s0[i] & ARight[i]
            int[] pZLR = new int[n+2];  // count of positions i where s0[i]&ALeft[i]&ARight[i]

            for (int i = 1; i <= n; i++) {
                pZ[i]   = pZ[i-1]   + s0[i];
                pT[i]   = pT[i-1]   + t1[i];
                pZL[i]  = pZL[i-1]  + (s0[i]==1 && ALeft[i]==1 ? 1 : 0);
                pZR[i]  = pZR[i-1]  + (s0[i]==1 && ARight[i]==1 ? 1 : 0);
                pZLR[i] = pZLR[i-1] + (s0[i]==1 && ALeft[i]==1 && ARight[i]==1 ? 1 : 0);
            }

            int q = Integer.parseInt(br.readLine().trim());
            while (q-- > 0) {
                StringTokenizer st = new StringTokenizer(br.readLine());
                int l = Integer.parseInt(st.nextToken());
                int r = Integer.parseInt(st.nextToken());
                int k = r - l + 1;

                // number of original 1's in s[l..r]
                int zeros = pZ[r] - pZ[l-1];
                int origOnes = k - zeros;

                if (k < 3) {
                    // no interior position to flip
                    out.println(origOnes);
                    continue;
                }

                // if exactly length 3, only j = l+1 can flip, and only if 
                // s[j]==0 and t[l]==1 and t[r]==1
                if (k == 3) {
                    int j = l+1;
                    int add = (s0[j]==1 && t1[l]==1 && t1[r]==1) ? 1 : 0;
                    out.println(origOnes + add);
                    continue;
                }

                // for k >= 4, we have:
                // 1) j = l+1
                int add1 = 0;
                int j1 = l+1;
                if (s0[j1]==1 && t1[l]==1 && ARight[j1]==1) {
                    add1 = 1;
                }

                // 2) j = r-1
                int add2 = 0;
                int j2 = r-1;
                if (s0[j2]==1 && ALeft[j2]==1 && t1[r]==1) {
                    add2 = 1;
                }

                // 3) j in [l+2 .. r-2], use pZLR
                int add3 = 0;
                if (r-2 >= l+2) {
                    add3 = pZLR[r-2] - pZLR[l+1];
                }

                out.println(origOnes + add1 + add2 + add3);
            }
        }

        out.flush();
    }
}