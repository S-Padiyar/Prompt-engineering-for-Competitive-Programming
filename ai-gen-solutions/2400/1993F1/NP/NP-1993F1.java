import java.io.*;
import java.util.*;

public class Main {
    static final long INF = (long)1e18;

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));

        int T = Integer.parseInt(in.readLine());
        while (T-- > 0) {
            String[] tok = in.readLine().split(" ");
            int n = Integer.parseInt(tok[0]);
            int k = Integer.parseInt(tok[1]);
            int w = Integer.parseInt(tok[2]);
            int h = Integer.parseInt(tok[3]);
            String s = in.readLine();

            // We will build for each of the 4 flip‐states S = 0..3
            // state bit0 = flipLR, bit1 = flipUD
            long[][] dx = new long[4][n+1], dy = new long[4][n+1];
            long[] minDx = new long[4], maxDx = new long[4], minDy = new long[4], maxDy = new long[4];
            long[] sumDx = new long[4], sumDy = new long[4];
            @SuppressWarnings("unchecked")
            HashMap<Long,Integer>[] zeroCnt = new HashMap[4];

            // Precompute for each state
            for (int st = 0; st < 4; st++) {
                boolean fLR = (st & 1) != 0;
                boolean fUD = (st & 2) != 0;
                zeroCnt[st] = new HashMap<>();
                minDx[st] = minDy[st] = 0;
                maxDx[st] = maxDy[st] = 0;
                dx[st][0] = 0;
                dy[st][0] = 0;
                for (int i = 1; i <= n; i++) {
                    char c = s.charAt(i-1);
                    long ddx=0, ddy=0;
                    switch(c) {
                        case 'L': ddx = fLR ? +1 : -1; break;
                        case 'R': ddx = fLR ? -1 : +1; break;
                        case 'U': ddy = fUD ? -1 : +1; break;
                        case 'D': ddy = fUD ? +1 : -1; break;
                    }
                    dx[st][i] = dx[st][i-1] + ddx;
                    dy[st][i] = dy[st][i-1] + ddy;
                    if (dx[st][i] < minDx[st]) minDx[st] = dx[st][i];
                    if (dx[st][i] > maxDx[st]) maxDx[st] = dx[st][i];
                    if (dy[st][i] < minDy[st]) minDy[st] = dy[st][i];
                    if (dy[st][i] > maxDy[st]) maxDy[st] = dy[st][i];

                    // record how many times this prefix sums to (dx,dy)
                    long key = (dx[st][i]<<32) ^ (dy[st][i]&0xffffffffL);
                    zeroCnt[st].put(key, zeroCnt[st].getOrDefault(key,0) + 1);
                }
                sumDx[st] = dx[st][n];
                sumDy[st] = dy[st][n];
            }

            // Now we actually walk.
            long ans = 0;
            long x = 0, y = 0;
            int state = 0;    // no flips yet
            int runs = k;     // how many full s‐runs remain

            // We'll do a tiny loop of at most a few partial‐scan passes;
            // after that, we expect to be in a state where the WHOLE script is "safe"
            // i.e. minDx[state]>= -x, maxDx[state]<= w-x, etc., and then we bulk finish remaining runs in O(1).
            for (int pass = 0; pass < 3 && runs > 0; pass++) {
                // Is the entire script s safe from going out of bounds if we start at (x,y) in "state"?
                if (x + minDx[state] >= 0 && x + maxDx[state] <= w
                 && y + minDy[state] >= 0 && y + maxDy[state] <= h) {
                    // Yes: bulk‐apply all runs at once
                    long key = ((-x)<<32) ^ ((-y)&0xffffffffL);
                    int oneRunHits = zeroCnt[state].getOrDefault(key, 0);
                    ans += (long)oneRunHits * runs;
                    x += sumDx[state] * runs;
                    y += sumDy[state] * runs;
                    runs = 0;
                    break;
                }
                // Otherwise we must do exactly one partial‐scan of s until the first boundary-hit.
                // We find the smallest i in [1..n] that forces out‐of‐bounds.
                int flipDim = -1; // 0=L/R boundary (x), 1=U/D boundary (y)
                int hitI = -1;
                for (int i = 1; i <= n; i++) {
                    long xx = x + dx[state][i];
                    long yy = y + dy[state][i];
                    if (xx < 0 || xx > w) {
                        flipDim = 0;
                        hitI = i;
                        break;
                    }
                    if (yy < 0 || yy > h) {
                        flipDim = 1;
                        hitI = i;
                        break;
                    }
                }
                // Everything up to hitI-1 is valid movement; simulate them
                for (int i = 1; i < hitI; i++) {
                    x += dx[state][i] - dx[state][i-1];
                    y += dy[state][i] - dy[state][i-1];
                    if (x == 0 && y == 0) ans++;
                }
                // Now at i = hitI we would go out of bounds; so we flip that dimension's bit,
                // do NOT move, and continue with the same i=hitI (still within the same run of s).
                if (flipDim == 0) {
                    state ^= 1;  // flip L<->R
                } else {
                    state ^= 2;  // flip U<->D
                }
                // We do NOT advance x,y, we do NOT consume a "run" yet,
                // we remain at the same pass, same run, next iteration will re-scan from i=1.
            }

            // If there's still runs left, we must now certainly be in a "safe" state,
            // so just bulk‐apply them all:
            if (runs > 0) {
                long key = ((-x)<<32) ^ ((-y)&0xffffffffL);
                int oneRunHits = zeroCnt[state].getOrDefault(key, 0);
                ans += (long)oneRunHits * runs;
                // x += sumDx[state] * runs;  // we don't really need final x,y anymore
                // y += sumDy[state] * runs;
                runs = 0;
            }

            out.println(ans);
        }
        out.flush();
    }
}