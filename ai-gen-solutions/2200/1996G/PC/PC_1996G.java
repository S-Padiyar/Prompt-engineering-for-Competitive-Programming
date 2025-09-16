import java.io.*;
import java.util.*;

public class Main {
    static final int MAXM = 200_000 + 5;
    static long[] rnd = new long[MAXM];

    public static void main(String[] args) throws IOException {
        Random rand = new Random(1234567L);
        for (int i = 0; i < MAXM; i++) {
            rnd[i] = rand.nextLong();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter pw = new PrintWriter(new BufferedOutputStream(System.out));
        StringTokenizer st = new StringTokenizer(br.readLine());
        int t = Integer.parseInt(st.nextToken());
        while (t-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken()), m = Integer.parseInt(st.nextToken());
            long[] ev = new long[n];
            Arrays.fill(ev, 0L);

            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(br.readLine());
                int a = Integer.parseInt(st.nextToken()) - 1;
                int b = Integer.parseInt(st.nextToken()) - 1;
                int dCW = (b - a + n) % n;
                int dCCW = n - dCW;
                int L, R;
                long tag = rnd[i];
                if (dCW <= dCCW) {
                    L = a;
                    R = (b - 1 + n) % n;
                } else {
                    L = b;
                    R = (a - 1 + n) % n;
                }
                if (L <= R) {
                    ev[L] ^= tag;
                    if (R + 1 < n) ev[R + 1] ^= tag;
                } else {
                    ev[L] ^= tag;
                    ev[0] ^= tag;
                    if (R + 1 < n) ev[R + 1] ^= tag;
                }
            }

            long[] hs = new long[n];
            long cur = 0;
            for (int i = 0; i < n; i++) {
                cur ^= ev[i];
                hs[i] = cur;
            }
            Arrays.sort(hs);

            int best = 1, cnt = 1;
            for (int i = 1; i < n; i++) {
                if (hs[i] == hs[i - 1]) cnt++;
                else {
                    if (cnt > best) best = cnt;
                    cnt = 1;
                }
            }
            if (cnt > best) best = cnt;
            pw.println(n - best);
        }
        pw.flush();
    }
}