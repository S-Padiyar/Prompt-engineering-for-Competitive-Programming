import java.io.*;
import java.util.*;

public class Main {
    static final int MAXN = 100000;
    // divisors[i] will hold all positive divisors of i
    static ArrayList<Integer>[] divisors = new ArrayList[MAXN + 1];
    // groupArr[u][k] = count of prior j with u_j = u and (v_j / u_j) = k
    static int[][] groupArr = new int[MAXN + 1][];

    public static void main(String[] args) throws IOException {
        // Fast IO
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        int t = Integer.parseInt(st.nextToken());

        // Precompute divisors for all 1..MAXN
        for (int i = 1; i <= MAXN; i++) {
            divisors[i] = new ArrayList<>();
        }
        for (int d = 1; d <= MAXN; d++) {
            for (int m = d; m <= MAXN; m += d) {
                divisors[m].add(d);
            }
        }
        // Preallocate the groupArr tables
        for (int u = 1; u <= MAXN; u++) {
            // groupArr[u].length = floor(MAXN/u) + 1
            groupArr[u] = new int[MAXN / u + 1];
        }

        StringBuilder output = new StringBuilder();
        // We will remember exactly which (u,k) cells we increment
        // so that we can reset them to 0 after each test case.
        ArrayList<Integer> usedU = new ArrayList<>();
        ArrayList<Integer> usedK = new ArrayList<>();

        while (t-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int[] p = new int[n + 1];
            st = new StringTokenizer(br.readLine());
            for (int i = 1; i <= n; i++) {
                p[i] = Integer.parseInt(st.nextToken());
            }

            long ans = 0;
            usedU.clear();
            usedK.clear();

            // Process i=1..n
            for (int i = 1; i <= n; i++) {
                int pi = p[i];
                int g = gcd(i, pi);
                int ui = i / g;
                int vi = pi / g;

                // Enumerate all possible u_j that divide v_i
                for (int uj : divisors[vi]) {
                    // Only consider prior j with u_j = uj
                    int maxK = n / uj;  // we only care up to floor(n/uj)
                    if (maxK == 0) continue;

                    int d = gcd(ui, uj);
                    int tstep = ui / d;
                    if (tstep > maxK) continue;

                    // Sum over k = tstep, 2*tstep, ... ≤ maxK
                    int[] arr = groupArr[uj];
                    for (int k = tstep; k <= maxK; k += tstep) {
                        ans += arr[k];
                    }
                }

                // Now insert i into groupArr[ui] at slot k_i = (v_i / u_i)
                int k_i = vi / ui;
                groupArr[ui][k_i]++;
                usedU.add(ui);
                usedK.add(k_i);
            }

            // Output answer for this test
            output.append(ans).append("\n");

            // Reset those cells to zero for the next test
            for (int idx = 0; idx < usedU.size(); idx++) {
                groupArr[usedU.get(idx)][usedK.get(idx)] = 0;
            }
        }

        // Flush all answers
        System.out.print(output);
    }

    // Fast gcd
    static int gcd(int a, int b) {
        while (b != 0) {
            int t = a % b;
            a = b;
            b = t;
        }
        return a;
    }
}