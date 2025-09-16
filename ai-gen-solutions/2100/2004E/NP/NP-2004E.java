import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader();
        int t = in.nextInt();

        // Read all test cases into memory, track the global maximum A_i
        int[][] tests = new int[t][];
        int maxA = 1;
        for (int i = 0; i < t; i++) {
            int n = in.nextInt();
            tests[i] = new int[n];
            for (int j = 0; j < n; j++) {
                int a = in.nextInt();
                tests[i][j] = a;
                if (a > maxA) maxA = a;
            }
        }

        // Build a linear sieve up to maxA to compute:
        //   spf[x] = smallest prime factor of x  (for x>=2)
        //   primeIndex[p] = index of prime p in the list 2,3,5,7,...
        int N = maxA;
        int[] spf = new int[N + 1];
        int[] primeIndex = new int[N + 1];
        // Upper bound on #primes up to 1e7 is about 664,579 → we allocate a little extra
        int[] primes = new int[N / 10 + 10];
        int pc = 0;

        for (int i = 2; i <= N; i++) {
            if (spf[i] == 0) {
                // i is prime
                spf[i] = i;
                primes[pc] = i;
                primeIndex[i] = ++pc;  // primeIndex[2]=1, [3]=2, [5]=3, ...
            }
            // mark spf for i * primes[j]
            for (int j = 0; j < pc; j++) {
                int p = primes[j];
                if (p > spf[i] || (long)i * p > N) {
                    break;
                }
                spf[i * p] = p;
            }
        }

        // Process each test
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < t; i++) {
            int xorSum = 0;
            for (int a : tests[i]) {
                int g;
                if (a == 1) {
                    g = 1;            // g(1)=1
                } else if ((a & 1) == 0) {
                    g = 0;            // all even a -> g(a)=0
                } else {
                    // a>1 is odd -> g(a) = primeIndex[spf[a]]
                    g = primeIndex[spf[a]];
                }
                xorSum ^= g;
            }
            sb.append(xorSum != 0 ? "Alice\n" : "Bob\n");
        }

        // Output results
        System.out.print(sb);
    }

    // Fast input reader
    static class FastReader {
        final private BufferedInputStream in;
        private byte[] buf = new byte[1 << 16];
        private int bufLen = 0, bufIdx = 0;

        FastReader() {
            in = new BufferedInputStream(System.in);
        }

        private int read() throws IOException {
            if (bufLen == -1) return -1;
            if (bufIdx >= bufLen) {
                bufIdx = 0;
                bufLen = in.read(buf);
                if (bufLen <= 0) {
                    bufLen = -1;
                    return -1;
                }
            }
            return buf[bufIdx++];
        }

        int nextInt() throws IOException {
            int c, val = 0;
            do {
                c = read();
            } while (c != -1 && (c < '0' || c > '9'));
            if (c == -1) return -1;
            for (; c >= '0' && c <= '9'; c = read()) {
                val = val * 10 + (c - '0');
            }
            return val;
        }
    }
}