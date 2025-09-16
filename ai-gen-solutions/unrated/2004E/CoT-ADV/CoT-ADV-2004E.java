import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // Fast input
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        
        int t = Integer.parseInt(br.readLine().trim());
        // We'll store each test's a[i] in a list, to process after we know the max-value
        List<int[]> testCases = new ArrayList<>(t);
        int globalMax = 0;
        
        // Read all test cases, track global maximum a_i
        for (int _case = 0; _case < t; _case++) {
            int n = Integer.parseInt(br.readLine().trim());
            int[] a = new int[n];
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
                if (a[i] > globalMax) {
                    globalMax = a[i];
                }
            }
            testCases.add(a);
        }
        
        // Sieve up to globalMax to get smallest prime factor (spf)
        int N = globalMax;
        int[] spf = new int[N + 1];       // spf[x] = smallest prime factor of x
        int[] oddPrimeIdx = new int[N + 1]; 
        // oddPrimeIdx[p] = index among odd primes for prime p>2, otherwise -1
        Arrays.fill(oddPrimeIdx, -1);
        
        // Linear sieve to fill spf[]
        List<Integer> primes = new ArrayList<>();
        for (int i = 2; i <= N; i++) {
            if (spf[i] == 0) {
                // i is prime
                spf[i] = i;
                primes.add(i);
            }
            for (int p : primes) {
                long prod = 1L * p * i;
                if (prod > N) break;
                spf[(int)prod] = p;
                if (p == spf[i]) {
                    // Once p divides i, we stop in a linear sieve
                    break;
                }
            }
        }
        
        // Assign indices to odd primes p>2
        int oddIdx = 0;
        for (int p : primes) {
            if (p > 2) {
                oddPrimeIdx[p] = oddIdx++;
            }
        }
        
        // Now process each test, compute XOR of Grundy values, and output
        StringBuilder sb = new StringBuilder();
        for (int[] a : testCases) {
            int xorSum = 0;
            for (int x : a) {
                int g;
                if (x == 1) {
                    // Only move: remove 1 -> 0, so mex{0}=1
                    g = 1;
                } else if ((x & 1) == 0) {
                    // Even sizes all have Grundy value 0
                    g = 0;
                } else {
                    // Odd > 1: use index of smallest prime factor + 2
                    int p = spf[x];
                    g = oddPrimeIdx[p] + 2;
                }
                xorSum ^= g;
            }
            sb.append((xorSum != 0) ? "Alice\n" : "Bob\n");
        }
        
        // Print all answers at once
        System.out.print(sb.toString());
    }
}