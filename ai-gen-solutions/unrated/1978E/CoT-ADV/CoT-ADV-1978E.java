import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder out = new StringBuilder();
        int T = Integer.parseInt(in.readLine().trim());
        while (T-- > 0) {
            int n = Integer.parseInt(in.readLine().trim());
            String sLine = in.readLine().trim();
            String tLine = in.readLine().trim();
            char[] s = new char[n+2], t = new char[n+2];
            for (int i = 1; i <= n; i++) {
                s[i] = sLine.charAt(i-1);
                t[i] = tLine.charAt(i-1);
            }
            // 1) prefix sum of '1's in s
            int[] prefS = new int[n+2];
            for (int i = 1; i <= n; i++) {
                prefS[i] = prefS[i-1] + (s[i]=='1' ? 1 : 0);
            }
            // 2) canB[i] = whether b[i] ends up =1 after phase 1
            boolean[] canB = new boolean[n+2];
            for (int i = 2; i <= n-1; i++) {
                if (t[i]=='1') {
                    canB[i] = true;
                } else {
                    // need s[i-1]=='0' and s[i+1]=='0'
                    if (s[i-1]=='0' && s[i+1]=='0') {
                        canB[i] = true;
                    }
                }
            }
            // 3) goodA[i] = whether a[i] can newly become 1
            //    i in [2..n-1], needs s[i]=='0' && canB[i-1] && canB[i+1]
            int[] prefG = new int[n+2];
            for (int i = 2; i <= n-1; i++) {
                int add = 0;
                if (s[i]=='0' && canB[i-1] && canB[i+1]) {
                    add = 1;
                }
                prefG[i] = prefG[i-1] + add;
            }
            // fill the rest of prefG
            for (int i = n; i <= n; i++) {
                prefG[i] = prefG[i-1];
            }

            // process queries
            int q = Integer.parseInt(in.readLine().trim());
            while (q-- > 0) {
                StringTokenizer st = new StringTokenizer(in.readLine());
                int l = Integer.parseInt(st.nextToken());
                int r = Integer.parseInt(st.nextToken());
                int len = r - l + 1;
                // base = original # of 1s in s[l..r]
                int baseOnes = prefS[r] - prefS[l-1];
                if (len <= 2) {
                    out.append(baseOnes).append('\n');
                    continue;
                }
                // interior full‐use of prefG
                int L2 = l + 2, R2 = r - 2;
                int sumInterior = 0;
                if (L2 <= R2) {
                    sumInterior = prefG[R2] - prefG[L2-1];
                }
                // boundary checks at i1 = l+1, i2 = r-1
                int bc1 = 0, bc2 = 0;
                int i1 = l + 1;
                if (i1 <= r - 1 && s[i1]=='0') {
                    boolean left1 = (t[l]=='1');
                    boolean right1;
                    if (i1+1 < r) {
                        right1 = canB[i1+1];
                    } else {
                        right1 = (t[r]=='1');
                    }
                    if (left1 && right1) bc1 = 1;
                }
                int i2 = r - 1;
                if (i2 > i1 && s[i2]=='0') {
                    boolean left2;
                    if (i2-1 > l) {
                        left2 = canB[i2-1];
                    } else {
                        left2 = (t[l]=='1');
                    }
                    boolean right2 = (t[r]=='1');
                    if (left2 && right2) bc2 = 1;
                }
                int extra = sumInterior + bc1 + bc2;
                out.append(baseOnes + extra).append('\n');
            }
        }
        System.out.print(out);
    }
}