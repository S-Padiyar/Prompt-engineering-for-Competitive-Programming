import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            String s = br.readLine();
            int n = s.length();
            List<Character> U = new ArrayList<>();
            List<Integer> cList = new ArrayList<>();
            int run = 0;
            for (int i = 0; i < n; i++) {
                char ch = s.charAt(i);
                if (ch == 'a') {
                    run++;
                } else {
                    cList.add(run);
                    run = 0;
                    U.add(ch);
                }
            }
            cList.add(run);
            int k = U.size();
            if (k == 0) {
                out.println(n - 1);
                continue;
            }
            char[] up = new char[k];
            for (int i = 0; i < k; i++) up[i] = U.get(i);
            int[] c = new int[k + 1];
            for (int i = 0; i <= k; i++) c[i] = cList.get(i);
            List<Integer> divisors = new ArrayList<>();
            for (int d = 1; d * d <= k; d++) {
                if (k % d == 0) {
                    divisors.add(d);
                    if (d != k / d) divisors.add(k / d);
                }
            }
            long ans = 0;
            for (int r : divisors) {
                int m = k / r;
                boolean ok = true;
                for (int i = 0; i + r < k; i++) {
                    if (up[i] != up[i + r]) {
                        ok = false;
                        break;
                    }
                }
                if (!ok) continue;
                for (int i = 1; i + r < k; i++) {
                    if (c[i] != c[i + r]) {
                        ok = false;
                        break;
                    }
                }
                if (!ok) continue;
                int y0max = Integer.MAX_VALUE;
                for (int j = 0; j < m; j++) {
                    y0max = Math.min(y0max, c[j * r]);
                }
                int yEmax = Integer.MAX_VALUE;
                for (int j = 1; j <= m; j++) {
                    yEmax = Math.min(yEmax, c[j * r]);
                }
                ans += (long)(y0max + 1) * (yEmax + 1);
            }
            out.println(ans);
        }
        out.flush();
    }
}