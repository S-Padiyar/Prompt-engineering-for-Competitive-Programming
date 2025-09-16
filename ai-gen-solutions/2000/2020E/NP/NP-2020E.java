import java.io.*;
import java.util.*;

public class Main {
    static final int MAXM = 1024;
    static final int MOD = 1_000_000_007;
    static int[] S = new int[MAXM];
    static int inv10000, inv1024;
    // For each x in [0..1023], list of all k in [0..1023] with popcount(x&k)%2==1
    static int[][] oddList = new int[MAXM][];

    // fast exp base^e mod MOD
    static int modExp(int base, int e) {
        long res = 1, b = base;
        while (e > 0) {
            if ((e & 1) != 0) res = (res * b) % MOD;
            b = (b * b) % MOD;
            e >>= 1;
        }
        return (int)res;
    }

    public static void main(String[] args) throws IOException {
        // Precompute inverses of 10000 and 1024
        inv10000 = modExp(10000, MOD-2);
        inv1024  = modExp(1024,  MOD-2);

        // Precompute S[k] = sum_{v=0..1023} v^2 * (-1)^{popcount(v&k)}
        int[] v2 = new int[MAXM];
        for (int v = 0; v < MAXM; v++) {
            long vv = v;
            v2[v] = (int)((vv*vv) % MOD);
        }
        for (int k = 0; k < MAXM; k++) {
            long sum = 0;
            for (int v = 0; v < MAXM; v++) {
                int p = Integer.bitCount(v & k) & 1; // 0 or 1
                if (p == 0) sum += v2[v];
                else        sum -= v2[v];
            }
            sum %= MOD;
            if (sum < 0) sum += MOD;
            S[k] = (int)sum;
        }

        // Precompute oddList[x] = all k with popcount(x&k)%2==1
        for (int x = 0; x < MAXM; x++) {
            ArrayList<Integer> list = new ArrayList<>();
            for (int k = 0; k < MAXM; k++) {
                if (((Integer.bitCount(x & k)) & 1) == 1) {
                    list.add(k);
                }
            }
            oddList[x] = list.stream().mapToInt(i->i).toArray();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        int t = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            int[] a = new int[n];
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }
            st = new StringTokenizer(br.readLine());
            // We'll form P_x = product of (1-2p_i) for i with a_i=x
            int[] P_x = new int[MAXM];
            Arrays.fill(P_x, 1);
            boolean[] used = new boolean[MAXM];
            ArrayList<Integer> usedX = new ArrayList<>();

            for (int i = 0; i < n; i++) {
                int pi = Integer.parseInt(st.nextToken());
                // p_i mod = pi/10000 mod
                long pmod = (pi * 1L * inv10000) % MOD;
                // term = (1 - 2*p_i) mod
                long term = (1 - 2*pmod) % MOD;
                if (term < 0) term += MOD;
                int x = a[i];
                P_x[x] = (int)((P_x[x] * term) % MOD);
                if (!used[x]) {
                    used[x] = true;
                    usedX.add(x);
                }
            }

            // Build DP* array
            int[] dpStar = new int[MAXM];
            Arrays.fill(dpStar, 1);
            for (int x : usedX) {
                int px = P_x[x];
                // multiply px into dpStar[k] for those k in oddList[x]
                for (int k : oddList[x]) {
                    dpStar[k] = (int)((dpStar[k] * 1L * px) % MOD);
                }
            }

            // Sum up S[k]*dpStar[k], then multiply by inv1024
            long ans = 0;
            for (int k = 0; k < MAXM; k++) {
                ans = (ans + dpStar[k]*1L*S[k]) % MOD;
            }
            ans = (ans * inv1024) % MOD;

            sb.append(ans).append('\n');
        }

        System.out.print(sb);
    }
}