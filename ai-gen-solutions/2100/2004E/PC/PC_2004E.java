import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader();
        int t = in.nextInt();
        
        // Maximum total piles ≤ 300_000
        final int MAX_PILES = 300_000;
        int[] ns = new int[t];
        int[] offsets = new int[t];
        int[] data = new int[MAX_PILES];
        
        int idx = 0;
        int maxA = 0;
        
        // Read all test cases in one pass, flatten piles into data[]
        for (int ti = 0; ti < t; ti++) {
            int n = in.nextInt();
            ns[ti] = n;
            offsets[ti] = idx;
            for (int j = 0; j < n; j++) {
                int a = in.nextInt();
                data[idx++] = a;
                if (a > maxA) maxA = a;
            }
        }
        int totalPiles = idx;  // actual number of piles read
        
        // Build spf[] (smallest prime factor) and pi[] (prime count ≤ x)
        int N = Math.max(1, maxA);
        int[] spf = new int[N + 1];
        int[] pi  = new int[N + 1];
        ArrayList<Integer> primes = new ArrayList<>();
        
        pi[0] = 0;
        if (N >= 1) pi[1] = 0;
        for (int i = 2; i <= N; i++) {
            if (spf[i] == 0) {
                spf[i] = i;
                primes.add(i);
            }
            // pi[i] = pi[i-1] + (is i prime ? 1 : 0)
            pi[i] = pi[i - 1] + (spf[i] == i ? 1 : 0);
            
            for (int p : primes) {
                long ip = (long)i * p;
                if (p > spf[i] || ip > N) break;
                spf[(int)ip] = p;
            }
        }
        // By convention:
        if (N >= 1) spf[1] = 1;
        
        // Process each test case: compute XOR of Grundy values
        StringBuilder sb = new StringBuilder();
        idx = 0;
        for (int ti = 0; ti < t; ti++) {
            int xorsum = 0;
            int n = ns[ti];
            for (int j = 0; j < n; j++, idx++) {
                int a = data[idx];
                int g;
                if (a == 1) {
                    g = 1;
                } else if ((a & 1) == 0) {
                    // even numbers ≥2 have Grundy 0
                    g = 0;
                } else {
                    // odd > 1: Grundy = π( spf[a] )
                    g = pi[ spf[a] ];
                }
                xorsum ^= g;
            }
            sb.append( xorsum != 0 ? "Alice\n" : "Bob\n" );
        }
        
        // Output all answers at once
        System.out.print(sb);
    }
    
    // Fast input reader
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
        FastReader() {
            br = new BufferedReader(new InputStreamReader(System.in));
        }
        String next() throws IOException {
            while (st == null || !st.hasMoreTokens()) {
                String line = br.readLine();
                if (line == null) return null;
                st = new StringTokenizer(line);
            }
            return st.nextToken();
        }
        int nextInt() throws IOException {
            return Integer.parseInt(next());
        }
    }
}