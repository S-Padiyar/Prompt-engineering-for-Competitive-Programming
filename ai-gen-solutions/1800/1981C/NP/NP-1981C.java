import java.io.*;
import java.util.*;

public class Main {
    static final long INF = (long)1e18;
    static final long MAXB = (long)1e9;

    // Compute floor of the minimum reachable in rem steps from y
    static long fmin(long y, int rem) {
        if (rem == 0) return y;
        // k = floor(log2(y))
        int k = 63 - Long.numberOfLeadingZeros(y);
        if (rem <= k) {
            // We can just shift down rem times
            return y >> rem;
        } else {
            // We hit 1 after k downs, then we have rem-k steps
            int r2 = rem - k;
            // If we have an even number of extra steps we end at 1, else at 2
            return (r2 % 2 == 0 ? 1 : 2);
        }
    }

    // Compute the maximum reachable in rem "up" steps from y
    static long fmax(long y, int rem) {
        if (rem >= 31) {
            // Clearly 2^31>1e9, so we'll exceed 1e9 if rem big
            return INF;
        }
        long mul = 1L << rem;  // 2^rem
        return y * mul + (mul - 1);
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int t = Integer.parseInt(in.readLine().trim());
        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            int n = Integer.parseInt(in.readLine().trim());
            long[] a = new long[n];
            st = new StringTokenizer(in.readLine());
            for (int i = 0; i < n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }

            // Collect indices of known entries
            ArrayList<Integer> known = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if (a[i] != -1) known.add(i);
            }

            long[] b = Arrays.copyOf(a, n);
            boolean ok = true;

            // Case 1: no known => just alternate 1,2
            if (known.isEmpty()) {
                b[0] = 1;
                for (int i = 1; i < n; i++) {
                    b[i] = (b[i-1] == 1 ? 2 : 1);
                }
            } else {
                // 2) Fill head before first known
                int first = known.get(0);
                for (int i = first; i > 0; i--) {
                    long cur = b[i];
                    long down = cur / 2;
                    if (down >= 1) {
                        b[i-1] = down;
                    } else {
                        b[i-1] = cur * 2;
                    }
                }

                // 3) Fill tail after last known
                int last = known.get(known.size() - 1);
                for (int i = last; i < n - 1; i++) {
                    long cur = b[i];
                    long down = cur / 2;
                    if (down >= 1) {
                        b[i+1] = down;
                    } else {
                        b[i+1] = cur * 2;
                    }
                }

                // 4) Fill internal segments
                for (int k = 0; k + 1 < known.size(); k++) {
                    int L = known.get(k);
                    int R = known.get(k+1);
                    long A = b[L], C = b[R];
                    int D = R - L;

                    // Quick reachability check
                    if (C < fmin(A, D) || C > fmax(A, D)) {
                        ok = false;
                        break;
                    }
                    // Greedy construct b[L+1..R]
                    long x = A;
                    for (int i = L; i < R; i++) {
                        int rem = R - i;      // steps including this one
                        int remNext = rem - 1;// steps after choosing b[i+1]

                        // try down
                        boolean placed = false;
                        long down = x / 2;
                        if (down >= 1) {
                            if (C >= fmin(down, remNext) && C <= fmax(down, remNext)) {
                                b[i+1] = down;
                                x = down;
                                placed = true;
                            }
                        }
                        // try up = 2x
                        if (!placed) {
                            long up1 = 2 * x;
                            if (up1 <= MAXB) {
                                if (C >= fmin(up1, remNext) && C <= fmax(up1, remNext)) {
                                    b[i+1] = up1;
                                    x = up1;
                                    placed = true;
                                }
                            }
                        }
                        // try up = 2x+1
                        if (!placed) {
                            long up2 = 2 * x + 1;
                            if (up2 <= MAXB) {
                                if (C >= fmin(up2, remNext) && C <= fmax(up2, remNext)) {
                                    b[i+1] = up2;
                                    x = up2;
                                    placed = true;
                                }
                            }
                        }
                        if (!placed) {
                            ok = false;
                            break;
                        }
                    }
                    if (!ok) break;
                    // at the end we must have b[R]==C
                    if (b[R] != C) {
                        ok = false;
                        break;
                    }
                }
            }

            if (!ok) {
                sb.append("-1\n");
            } else {
                for (int i = 0; i < n; i++) {
                    sb.append(b[i]).append(i+1<n? ' ':'\n');
                }
            }
        }

        System.out.print(sb);
    }
}