import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader();
        PrintWriter out = new PrintWriter(System.out);

        int t = in.nextInt();
        while (t-- > 0) {
            int n = in.nextInt();
            int x = in.nextInt(); // guaranteed x == n in E1
            int[] a = new int[n+2];
            for (int i = 1; i <= n; i++) {
                a[i] = in.nextInt();
            }
            // trivial case
            if (n == 1) {
                out.println(1);
                continue;
            }

            // prefix sums and suffix sums
            long[] pref = new long[n+2], suff = new long[n+2];
            for (int i = 1; i <= n; i++) {
                pref[i] = pref[i-1] + a[i];
            }
            for (int i = n; i >= 1; i--) {
                suff[i] = suff[i+1] + a[i];
            }

            // max to the left (strictly) and to the right (strictly)
            int[] maxL = new int[n+2], maxR = new int[n+2];
            maxL[1] = 0;
            for (int i = 2; i <= n; i++) {
                maxL[i] = Math.max(maxL[i-1], a[i-1]);
            }
            maxR[n] = 0;
            for (int i = n-1; i >= 1; i--) {
                maxR[i] = Math.max(maxR[i+1], a[i+1]);
            }

            // for the "pure left->right" check we need to know at each j
            // whether we can absorb left side ONE BY ONE in order j-1, j-2, ... ,1
            // which is equivalent to demanding
            //   pref[j] >= max_{i<j} ( pref[i] + a[i] )
            long[] B = new long[n+2], maxB = new long[n+2];
            for (int i = 1; i <= n; i++) {
                B[i] = pref[i] + (long)a[i];
                maxB[i] = i==1 ? B[i] : Math.max(maxB[i-1], B[i]);
            }
            // similarly for the "pure right->left" check:
            //   suff[j] >= max_{i>j} ( suff[i] + a[i] )
            long[] D = new long[n+2], sufD = new long[n+2];
            for (int i = n; i >= 1; i--) {
                D[i] = suff[i] + (long)a[i];
                sufD[i] = (i==n ? D[i] : Math.max(sufD[i+1], D[i]));
            }

            int cnt = 0;
            long total = pref[n];
            // We'll simulate the greedy interleaving only if both pure attempts fail.
            for (int j = 1; j <= n; j++) {
                // pure-left-then-right?
                boolean leftOk = (j==1) 
                                || (pref[j] >= maxB[j-1]); 
                boolean canPureLeftThenRight = false;
                if (leftOk) {
                    long S = pref[j];
                    // to absorb the right side one-by-one, S must >= every a[k], k>j.
                    // equivalently S >= maxR[j]
                    if (S >= maxR[j]) {
                        canPureLeftThenRight = true;
                    }
                }

                if (canPureLeftThenRight) {
                    cnt++;
                    continue;
                }

                // pure-right-then-left?
                boolean rightOk = (j==n)
                                 || (suff[j] >= sufD[j+1]);
                boolean canPureRightThenLeft = false;
                if (rightOk) {
                    long S = suff[j];
                    if (S >= maxL[j]) {
                        canPureRightThenLeft = true;
                    }
                }

                if (canPureRightThenLeft) {
                    cnt++;
                    continue;
                }

                // otherwise we do the greedy interleaving
                long s = a[j];
                int L = j, R = j;
                boolean bad = false;
                while (L > 1 || R < n) {
                    if (L > 1 && R < n) {
                        // pick the smaller neighbor first
                        if (a[L-1] <= a[R+1]) {
                            if (a[L-1] <= s) {
                                s += a[L-1];
                                L--;
                            } else if (a[R+1] <= s) {
                                s += a[R+1];
                                R++;
                            } else {
                                bad = true;
                                break;
                            }
                        } else {
                            if (a[R+1] <= s) {
                                s += a[R+1];
                                R++;
                            } else if (a[L-1] <= s) {
                                s += a[L-1];
                                L--;
                            } else {
                                bad = true;
                                break;
                            }
                        }
                    } else if (L > 1) {
                        if (a[L-1] <= s) {
                            s += a[L-1];
                            L--;
                        } else {
                            bad = true;
                            break;
                        }
                    } else {
                        // only R < n
                        if (a[R+1] <= s) {
                            s += a[R+1];
                            R++;
                        } else {
                            bad = true;
                            break;
                        }
                    }
                }
                if (!bad) {
                    // we managed to expand L=1 and R=n
                    cnt++;
                }
            }

            out.println(cnt);
        }

        out.flush();
    }

    // fast input
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
        FastReader() {
            br = new BufferedReader(new InputStreamReader(System.in));
        }
        String next() throws IOException {
            while (st==null || !st.hasMoreTokens()) {
                st = new StringTokenizer(br.readLine());
            }
            return st.nextToken();
        }
        int nextInt() throws IOException {
            return Integer.parseInt(next());
        }
        long nextLong() throws IOException {
            return Long.parseLong(next());
        }
    }
}