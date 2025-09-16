import java.io.*;
import java.util.*;

public class Main {
    // Maximum possible a[i]
    static final int MAXA = 100000;
    // spf[x] = smallest prime factor of x
    static int[] spf = new int[MAXA+1];
    // divisors[x] = list of all divisors of x in ascending order
    static ArrayList<Integer>[] divisors = new ArrayList[MAXA+1];

    // freq[v] = how many times v is still in the multiset
    static int[] freq = new int[MAXA+1];
    // f[d] = how many remaining a-values are multiples of d
    static int[] f = new int[MAXA+1];

    // To clear only the touched indices of freq[] and f[] between tests:
    static boolean[] freqTouched = new boolean[MAXA+1];
    static boolean[] fTouched    = new boolean[MAXA+1];
    static ArrayList<Integer> freqList = new ArrayList<>();
    static ArrayList<Integer> fList    = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        initSieveAndDivisors();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        int t = Integer.parseInt(st.nextToken());

        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());

            int[] a = new int[n];
            st = new StringTokenizer(br.readLine());
            int maxInTest = 0;
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
                if (a[i] > maxInTest) maxInTest = a[i];
            }

            // Build freq[] and f[]
            for (int v : a) {
                if (!freqTouched[v]) {
                    freqTouched[v] = true;
                    freqList.add(v);
                }
                freq[v]++;
                // increment f[d] for every divisor d of v
                for (int d : divisors[v]) {
                    if (!fTouched[d]) {
                        fTouched[d] = true;
                        fList.add(d);
                    }
                    f[d]++;
                }
            }

            // 1) Pick the smallest a[i] as the first element
            int minVal = 1;
            while (minVal <= maxInTest && freq[minVal] == 0) {
                minVal++;
            }
            long answer = 0;
            int g = minVal;
            answer += g;

            // Remove one instance of minVal from freq[], f[], decrease count
            removeValue(minVal);

            int remaining = n - 1;
            // Greedy loop
            while (remaining > 0) {
                if (g == 1) {
                    // once gcd is 1, it stays 1 for all future picks
                    answer += (long) remaining;
                    break;
                }

                // Factor g into prime-powers p^e
                ArrayList<Integer> gPrimes = new ArrayList<>();
                ArrayList<Integer> gPows   = new ArrayList<>();
                {
                    int x = g;
                    while (x > 1) {
                        int p = spf[x], cnt = 0;
                        while (spf[x] == p) {
                            x /= p;
                            cnt++;
                        }
                        int pPow = 1;
                        for (int i = 0; i < cnt; i++) pPow *= p;
                        gPrimes.add(p);
                        gPows.add(pPow);
                    }
                }

                // Try all divisors d of g, in ascending order,
                // to find the minimal d with countWanted(d) > 0.
                int bestD = g;  // fallback
                for (int d : divisors[g]) {
                    // We need to count how many x in multiset have gcd(g,x) == d
                    // We'll do inclusion/exclusion on the primes dividing (g/d).
                    // Those are exactly the primes p for which d % pPow < pPow
                    ArrayList<Integer> badPows = new ArrayList<>();
                    for (int i = 0; i < gPrimes.size(); i++) {
                        int pPow = gPows.get(i);
                        if (d % pPow != 0) {
                            badPows.add(pPow);
                        }
                    }
                    int kBad = badPows.size();
                    int cnt = 0;
                    // Inclusion-Exclusion over subsets of badPows
                    int subsets = 1 << kBad;
                    for (int mask = 0; mask < subsets; mask++) {
                        int mul = d;
                        int bits = Integer.bitCount(mask);
                        for (int j = 0; j < kBad; j++) {
                            if (((mask >>> j) & 1) != 0) {
                                mul *= badPows.get(j);
                            }
                        }
                        if (mul > MAXA) continue;  // out of range
                        int sign = ((bits & 1) == 1) ? -1 : +1;
                        cnt += sign * f[mul];
                    }
                    if (cnt > 0) {
                        bestD = d;
                        break;
                    }
                }

                if (bestD == g) {
                    // gcd won't decrease any more; all remaining picks have gcd = g
                    answer += (long) g * remaining;
                    break;
                }

                // Otherwise we do pick bestD as the next gcd
                answer += bestD;
                // We must remove one actual a-value x with gcd(g, x) == bestD
                // We'll scan multiples of bestD until we find freq[x]>0 and gcd(g,x)==bestD
                int foundX = -1;
                for (int x = bestD; x <= maxInTest; x += bestD) {
                    if (freq[x] > 0 && Integer.gcd(g, x) == bestD) {
                        foundX = x;
                        break;
                    }
                }
                // It must exist
                if (foundX < 0) throw new RuntimeException("Logic error: no witness for gcd.");
                removeValue(foundX);

                // Update gcd
                g = bestD;
                remaining--;
            }

            sb.append(answer).append('\n');

            // Clear freq[] and f[] at touched indices
            for (int v : freqList) {
                freq[v] = 0;
                freqTouched[v] = false;
            }
            freqList.clear();
            for (int d : fList) {
                f[d] = 0;
                fTouched[d] = false;
            }
            fList.clear();
        }

        System.out.print(sb.toString());
    }

    // Remove one instance of value v from freq[] and decrement all f[d]
    // for d | v.
    static void removeValue(int v) {
        freq[v]--;
        for (int d : divisors[v]) {
            f[d]--;
        }
    }

    // Precompute spf[] and divisors[] up to MAXA
    static void initSieveAndDivisors() {
        // SPF sieve
        for (int i = 2; i <= MAXA; i++) {
            if (spf[i] == 0) {
                for (int j = i; j <= MAXA; j += i) {
                    if (spf[j] == 0) {
                        spf[j] = i;
                    }
                }
            }
        }
        spf[1] = 1;

        // Divisors lists
        for (int i = 1; i <= MAXA; i++) {
            divisors[i] = new ArrayList<>();
        }
        for (int d = 1; d <= MAXA; d++) {
            for (int x = d; x <= MAXA; x += d) {
                divisors[x].add(d);
            }
        }
        // Because d goes upward, divisors[x] is already in ascending order
    }
}