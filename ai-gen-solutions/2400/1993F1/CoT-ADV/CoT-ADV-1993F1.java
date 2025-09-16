import java.io.*;
import java.util.*;

public class Main {
    static final long INF = Long.MAX_VALUE/4;

    // Extended GCD: returns (g, x, y) so that a*x + b*y = g = gcd(a,b)
    // We only need x,y when gcd=1 to compute inverses.
    static long[] extGcd(long a, long b) {
        if (b == 0) return new long[]{a, 1, 0};
        long[] rec = extGcd(b, a % b);
        long g = rec[0], x = rec[2], y = rec[1] - (a / b) * rec[2];
        return new long[]{g, x, y};
    }

    // Modular inverse of a mod m, assuming gcd(a,m)=1, m>1
    static long modInv(long a, long m) {
        long[] e = extGcd(a, m);
        // e[1] is x so that a*x + m*y = 1
        long res = e[1] % m;
        if (res < 0) res += m;
        return res;
    }

    // gcd
    static long gcd(long a, long b) {
        while (b != 0) {
            long t = a % b;
            a = b; b = t;
        }
        return a;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(in.readLine().trim());
        StringBuilder output = new StringBuilder();

        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n   = Integer.parseInt(st.nextToken());
            long K  = Long.parseLong(st.nextToken());  // number of repeats
            int w   = Integer.parseInt(st.nextToken());
            int h   = Integer.parseInt(st.nextToken());
            String s = in.readLine().trim();

            // moduli for the unfolded checks
            int m1 = 2*w;
            int m2 = 2*h;

            // Compute total shift per script, mod m1,m2
            long Sx = 0, Sy = 0;
            for (char c: s.toCharArray()) {
                if (c=='L') Sx -= 1;
                else if (c=='R') Sx += 1;
                else if (c=='U') Sy += 1;
                else           Sy -= 1;
            }
            Sx = ((Sx % m1) + m1) % m1;
            Sy = ((Sy % m2) + m2) % m2;

            // Solve i*Sx + x_j ≡ 0 (mod m1), i*Sy + y_j ≡ 0 (mod m2)
            // Precompute data for the x‐congruence:
            long g1 = gcd(Sx, m1);
            long m1p = m1 / g1;      // the reduced modulus
            long invSx = (m1p>1 ? modInv(Sx/g1, m1p) : 0);

            // For the y‐congruence
            long g2 = gcd(Sy, m2);
            long m2p = m2 / g2;
            long invSy = (m2p>1 ? modInv(Sy/g2, m2p) : 0);

            // For CRT on (mod m1p) and (mod m2p):
            // We'll need gcd(m1p, m2p) etc.
            long g12 = gcd(m1p, m2p);
            long invM1p = 0;
            if (m2p / g12 > 1) {
                invM1p = modInv(m1p/g12, m2p/g12);
            }
            long totalVisits = 0;

            // Step through the script once, keeping track of prefix sums mod m1, m2
            long curX = 0, curY = 0;
            for (int j = 0; j < n; j++) {
                char c = s.charAt(j);
                // Update unfolded position mod m1,m2
                if (c == 'L') curX = (curX - 1 + m1) % m1;
                else if (c == 'R') curX = (curX + 1) % m1;
                else if (c == 'U') curY = (curY + 1) % m2;
                else               curY = (curY - 1 + m2) % m2;

                // We want i*Sx + curX ≡ 0 (mod m1)
                // i*Sy + curY ≡ 0 (mod m2)
                long tx = (m1 - curX) % m1;
                long ty = (m2 - curY) % m2;

                // Check basic divisibility
                if (tx % g1 != 0 || ty % g2 != 0) {
                    continue;  // no solution for this j
                }

                // Solve each separately
                long ux = (m1p==1 ? 0 : ((tx/g1) % m1p * invSx) % m1p);
                long uy = (m2p==1 ? 0 : ((ty/g2) % m2p * invSy) % m2p);

                // Now CRT: i ≡ ux (mod m1p), i ≡ uy (mod m2p)
                long r, L;  // result residue and overall modulus
                if (m1p == 1 && m2p == 1) {
                    // both free => i ≡ 0 (mod 1)
                    r = 0;  L = 1;
                } else if (m1p == 1) {
                    // only y matters
                    r = uy;  L = m2p;
                } else if (m2p == 1) {
                    r = ux;  L = m1p;
                } else {
                    // full CRT
                    long diff = uy - ux;
                    if (diff % g12 != 0) {
                        continue;  // no simultaneous solution
                    }
                    long mod2 = m2p / g12;
                    long t0 = ((diff/g12) % mod2 + mod2) % mod2;
                    long k0 = (t0 * invM1p) % mod2;
                    r = ux + k0 * m1p;
                    L = m1p * mod2;  // lcm(m1p,m2p)
                    r %= L;
                    if (r<0) r+=L;
                }

                // Count how many i in [0..K-1] satisfy i ≡ r (mod L)
                if (r < K) {
                    long cnt = ((K-1 - r) / L) + 1;
                    totalVisits += cnt;
                }
            }

            output.append(totalVisits).append('\n');
        }

        System.out.print(output);
    }
}