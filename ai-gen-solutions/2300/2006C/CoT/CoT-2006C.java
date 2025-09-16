import java.io.*;
import java.util.*;

public class Main {
    static int gcd(int a, int b) {
        while (b != 0) {
            int t = a % b;
            a = b;
            b = t;
        }
        return a;
    }
    // test if x is 0 or a power of two
    static boolean isPow2OrZero(int x) {
        return (x & (x - 1)) == 0;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(in.readLine());
        int t = Integer.parseInt(st.nextToken());

        // Reusable arrays up to total N = 400,000
        int[] a = new int[400000];
        int[] L = new int[400000], R = new int[400000];
        int[] stack = new int[400000];

        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            st = new StringTokenizer(in.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            // Compute L[i] = nearest index < i with a[L[i]] < a[i], else -1
            int top = 0;
            for (int i = 0; i < n; i++) {
                while (top > 0 && a[stack[top-1]] >= a[i]) {
                    top--;
                }
                L[i] = (top == 0 ? -1 : stack[top-1]);
                stack[top++] = i;
            }

            // Compute R[i] = nearest index > i with a[R[i]] < a[i], else n
            top = 0;
            for (int i = n - 1; i >= 0; i--) {
                while (top > 0 && a[stack[top-1]] >= a[i]) {
                    top--;
                }
                R[i] = (top == 0 ? n : stack[top-1]);
                stack[top++] = i;
            }

            long ans = 0;
            // Count all singletons
            ans += n;

            // Temporary arrays to hold the "gcd runs"
            // at most ~32 runs each side
            int[] gLval = new int[32],  gLcnt = new int[32];
            int[] gRval = new int[32],  gRcnt = new int[32];

            for (int i = 0; i < n; i++) {
                int leftBound = L[i], rightBound = R[i];
                int szL = 0, szR = 0;

                // Build gcd‐runs for ℓ<i
                int cur = 0;
                int pos = i - 1;
                while (pos > leftBound) {
                    int d = a[pos] - a[i];
                    cur = gcd(cur, d);
                    if (szL == 0 || gLval[szL-1] != cur) {
                        gLval[szL] = cur;
                        gLcnt[szL] = 1;
                        szL++;
                    } else {
                        gLcnt[szL - 1]++;
                    }
                    // once it hits 1 it stays 1 → we can bulk‐assign the rest
                    if (cur == 1) {
                        int rem = pos - leftBound - 1; 
                        if (rem > 0) {
                            gLcnt[szL - 1] += rem;
                        }
                        break;
                    }
                    pos--;
                }

                // Build gcd‐runs for r>i
                cur = 0;
                pos = i + 1;
                while (pos < rightBound) {
                    int d = a[pos] - a[i];
                    cur = gcd(cur, d);
                    if (szR == 0 || gRval[szR-1] != cur) {
                        gRval[szR] = cur;
                        gRcnt[szR] = 1;
                        szR++;
                    } else {
                        gRcnt[szR - 1]++;
                    }
                    if (cur == 1) {
                        int rem = rightBound - pos - 1;
                        if (rem > 0) {
                            gRcnt[szR - 1] += rem;
                        }
                        break;
                    }
                    pos++;
                }

                // (a) ℓ<i, r=i
                for (int j = 0; j < szL; j++) {
                    if (isPow2OrZero(gLval[j])) {
                        ans += gLcnt[j];
                    }
                }
                // (b) ℓ=i, r>i
                for (int j = 0; j < szR; j++) {
                    if (isPow2OrZero(gRval[j])) {
                        ans += gRcnt[j];
                    }
                }

                // (c) ℓ<i<r
                for (int x = 0; x < szL; x++) {
                    int gx = gLval[x], cx = gLcnt[x];
                    for (int y = 0; y < szR; y++) {
                        int gy = gRval[y], cy = gRcnt[y];
                        boolean good;
                        if (gx == 0 && gy == 0) {
                            // All elements same
                            good = true;
                        } else if (gx == 0) {
                            // gcd = gy
                            good = isPow2OrZero(gy);
                        } else if (gy == 0) {
                            // gcd = gx
                            good = isPow2OrZero(gx);
                        } else {
                            // gcd(gx,gy) has no odd factor ↔ odd(gx) and odd(gy) are coprime
                            int ox = gx >>> Integer.numberOfTrailingZeros(gx);
                            int oy = gy >>> Integer.numberOfTrailingZeros(gy);
                            good = (gcd(ox, oy) == 1);
                        }
                        if (good) {
                            ans += (long)cx * cy;
                        }
                    }
                }
            }

            sb.append(ans).append('\n');
        }

        System.out.print(sb);
    }
}