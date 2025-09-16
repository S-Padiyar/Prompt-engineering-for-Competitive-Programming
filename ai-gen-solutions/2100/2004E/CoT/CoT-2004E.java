import java.io.*;
import java.util.StringTokenizer;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader  br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter  bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st;

        int t = Integer.parseInt(br.readLine());
        int[] ns = new int[t];
        int[][] allA = new int[t][];
        int maxA = 1;

        // Read input, remember max(a_i)
        for (int i = 0; i < t; i++) {
            ns[i] = Integer.parseInt(br.readLine());
            allA[i] = new int[ns[i]];
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < ns[i]; j++) {
                allA[i][j] = Integer.parseInt(st.nextToken());
                if (allA[i][j] > maxA) {
                    maxA = allA[i][j];
                }
            }
        }

        // Sieve up to maxA to find smallest prime factor (spf)
        int M = maxA;
        int[] spf = new int[M + 1]; // spf[x] = smallest prime factor of x
        // Standard sieve for spf
        for (int i = 2; i <= M; i++) {
            if (spf[i] == 0) {
                spf[i] = i;  // i is prime
                if ((long) i * i <= M) {
                    for (int j = i * i; j <= M; j += i) {
                        if (spf[j] == 0) {
                            spf[j] = i;
                        }
                    }
                }
            }
        }

        // Build an array primeIndex[p] = 1-based index of prime p in (2,3,5,7,...)
        int[] primeIndex = new int[M + 1];
        int idx = 0;
        for (int i = 2; i <= M; i++) {
            if (spf[i] == i) {
                // i is prime
                primeIndex[i] = ++idx;
            }
        }

        // Process each test case
        for (int ti = 0; ti < t; ti++) {
            int xorSum = 0;
            for (int x : allA[ti]) {
                if (x == 1) {
                    xorSum ^= 1;
                } else if ((x & 1) == 0) {
                    // even => Grundy = 0
                } else {
                    // odd > 1 => Grundy = index of smallest prime factor
                    int p = spf[x];
                    xorSum ^= primeIndex[p];
                }
            }
            // If xorSum != 0, Alice wins; otherwise Bob
            bw.write((xorSum != 0 ? "Alice" : "Bob") + "\n");
        }

        bw.flush();
    }
}