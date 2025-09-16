import java.io.*;
import java.util.*;

public class Main {
    // Fast GCD
    private static int gcd(int a, int b) {
        while (b != 0) {
            int t = a % b;
            a = b;
            b = t;
        }
        return a < 0 ? -a : a;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        int t = Integer.parseInt(br.readLine());

        // We will reuse these small arrays for the dynamic-programming state
        // Since the number of distinct gcds per position is O(log A), ~< 32
        int MAXS = 64;
        int[] prevG = new int[MAXS], curG = new int[MAXS];
        long[] prevCnt = new long[MAXS], curCnt = new long[MAXS];

        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine());
            st = new StringTokenizer(br.readLine());
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            // Edge case: length 1
            if (n == 1) {
                sb.append(1).append('\n');
                continue;
            }

            // Build the adjacent-differences array b of length n-1
            int[] b = new int[n - 1];
            for (int i = 0; i < n - 1; i++) {
                b[i] = Math.abs(a[i + 1] - a[i]);
            }

            // We'll accumulate the count of "good" subarrays of b (length >= 1)
            long goodSubB = 0;

            // "prev" list is initially empty (no subarrays ending at -1)
            int prevSize = 0;

            // Sweep b from left to right
            for (int i = 0; i < n - 1; i++) {
                int x = b[i];
                int curSize = 0;

                // 1) Start with the single-element subarray [i..i]
                curG[curSize] = x;
                curCnt[curSize] = 1;
                curSize++;

                // 2) Extend all old subarrays by b[i]
                for (int j = 0; j < prevSize; j++) {
                    int g = gcd(prevG[j], x);
                    long c = prevCnt[j];
                    // Merge runs of the same gcd
                    if (curG[curSize - 1] == g) {
                        curCnt[curSize - 1] += c;
                    } else {
                        curG[curSize] = g;
                        curCnt[curSize] = c;
                        curSize++;
                    }
                }

                // 3) Count how many of these have gcd a power of two (or zero)
                for (int j = 0; j < curSize; j++) {
                    int g = curG[j];
                    // Test "power-of-two or zero" by (g & (g-1)) == 0
                    if ((g & (g - 1)) == 0) {
                        goodSubB += curCnt[j];
                    }
                }

                // 4) Move cur → prev for the next iteration
                System.arraycopy(curG, 0, prevG, 0, curSize);
                System.arraycopy(curCnt, 0, prevCnt, 0, curSize);
                prevSize = curSize;
            }

            // Total brilliant subarrays of a = singletons (n)
            //                                  + good subarrays of b (length>=1)
            long answer = n + goodSubB;
            sb.append(answer).append('\n');
        }

        System.out.print(sb);
    }
}