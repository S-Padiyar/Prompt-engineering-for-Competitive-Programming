import java.io.*;
import java.util.*;

public class Main {
    // extended GCD: returns (g,x,y) so that a*x + b*y = g = gcd(a,b)
    static long[] extgcd(long a, long b) {
        if(b==0) return new long[]{a,1,0};
        long[] r = extgcd(b, a%b);
        long g = r[0], x = r[2], y = r[1] - (a/b)*r[2];
        return new long[]{g, x, y};
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int t = Integer.parseInt(br.readLine());
        while(t-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            long k = Long.parseLong(st.nextToken());
            long w = Long.parseLong(st.nextToken());
            long h = Long.parseLong(st.nextToken());
            String s = br.readLine();

            // Mx = 2w, My = 2h
            long Mx = 2*w, My = 2*h;

            // Build prefix sums X[i], Y[i]
            int[] X = new int[n], Y = new int[n];
            long cx=0, cy=0;
            for(int i=0; i<n; i++){
                char c = s.charAt(i);
                if(c=='L') cx--;
                else if(c=='R') cx++;
                else if(c=='U') cy++;
                else cy--;
                X[i] = (int)cx;
                Y[i] = (int)cy;
            }
            // total displacement of one full script
            long Araw = cx, Braw = cy;

            // reduce mod Mx, My to decide periodicity
            long A = ((Araw % Mx) + Mx) % Mx;
            long B = ((Braw % My) + My) % My;

            // gcds for the linear congruences
            long dx = extgcd(A, Mx)[0];
            long dy = extgcd(B, My)[0];

            // We'll need A'=A/dx, Mx'=Mx/dx, and similarly for B
            long Ax = (dx==0 ? 0 : A/dx), MxP = (dx==0 ? 1 : Mx/dx);
            long By = (dy==0 ? 0 : B/dy), MyP = (dy==0 ? 1 : My/dy);

            // see if A≡0 mod Mx, B≡0 mod My
            boolean Azero = (A==0);
            boolean Bzero = (B==0);

            // Precompute inverses when needed
            long invA = 0, invB = 0;
            if(!Azero) {
                // invert Ax mod MxP
                long[] e = extgcd(Ax, MxP);
                invA = ((e[1] % MxP) + MxP) % MxP;
            }
            if(!Bzero) {
                long[] e = extgcd(By, MyP);
                invB = ((e[1] % MyP) + MyP) % MyP;
            }

            long answer = 0;

            if(Azero && Bzero) {
                // both coordinates must individually hit 0 mod Mx, My
                int cnt = 0;
                for(int i=0; i<n; i++){
                    long xm = ((X[i] % Mx)+Mx)%Mx;
                    long ym = ((Y[i] % My)+My)%My;
                    if(xm==0 && ym==0) cnt++;
                }
                answer = cnt * k;
            }
            else if(Azero) {
                // require Xprefix ≡ 0 mod Mx, and solve the one congruence in r for Y
                for(int i=0; i<n; i++){
                    long xm = ((X[i] % Mx)+Mx)%Mx;
                    if(xm!=0) continue;
                    long ym = ((Y[i] % My)+My)%My;
                    if(ym % dy != 0) continue;
                    // solve (r-1)*B ≡ -Y[i] (mod My)
                    long Rhs = (My - ym) % My;  // ≡ -ym mod My
                    Rhs /= dy;                  // divides cleanly
                    long r0 = (1 + (Rhs * invB) % MyP) % MyP;
                    if(r0==0) r0 = MyP;
                    if(r0 <= k) {
                        answer += 1 + (k - r0)/MyP;
                    }
                }
            }
            else if(Bzero) {
                // symmetric
                for(int i=0; i<n; i++){
                    long ym = ((Y[i] % My)+My)%My;
                    if(ym!=0) continue;
                    long xm = ((X[i] % Mx)+Mx)%Mx;
                    if(xm % dx != 0) continue;
                    long Rhs = (Mx - xm) % Mx;
                    Rhs /= dx;
                    long r0 = (1 + (Rhs * invA) % MxP) % MxP;
                    if(r0==0) r0 = MxP;
                    if(r0 <= k) {
                        answer += 1 + (k - r0)/MxP;
                    }
                }
            }
            else {
                // general case: two congruences → CRT
                // we'll need lcm(Tx,Ty) and also an inverse to merge x->y
                long gT = gcd(MxP, MyP);
                long lcmT = (MxP/gT) * MyP;  // watch overflow fits in 64-bit
                long k1 = MxP/gT, k2 = MyP/gT;
                // invert k1 mod k2
                long invK1 = 0;
                {
                    long[] e = extgcd(k1, k2);
                    invK1 = ((e[1] % k2) + k2) % k2;
                }

                for(int i=0; i<n; i++){
                    long xm = ((X[i] % Mx)+Mx)%Mx;
                    if(xm % dx != 0) continue;
                    long ym = ((Y[i] % My)+My)%My;
                    if(ym % dy != 0) continue;

                    // solve (r-1)*A ≡ -xm (mod Mx)  →  r ≡ u (mod MxP)
                    long Rx = ((Mx - xm)%Mx)/dx;  // cleanly divisible
                    long ux = (1 + (Rx * invA) % MxP) % MxP;
                    if(ux==0) ux = MxP;

                    // similarly for y
                    long Ry = ((My - ym)%My)/dy;
                    long uy = (1 + (Ry * invB) % MyP) % MyP;
                    if(uy==0) uy = MyP;

                    // now we have r≡ux (mod MxP), r≡uy (mod MyP)
                    if((ux - uy) % gT != 0) continue;  // no CRT solution

                    // CRT: r0 = ux + MxP * t,  t ≡ (uy - ux)/gT * inv(k1)  (mod k2)
                    long diff = (uy - ux) / gT;  // integer
                    diff %= k2; 
                    if(diff<0) diff += k2;
                    long t0 = (diff * invK1) % k2;
                    long r0 = ux + MxP * t0;
                    // bring into [1..lcmT]
                    r0 %= lcmT;
                    if(r0<=0) r0 += lcmT;

                    if(r0 <= k) {
                        answer += 1 + (k - r0)/lcmT;
                    }
                }
            }

            System.out.println(answer);
        }
    }

    // simple gcd
    static long gcd(long a, long b) {
        while(b!=0) {
            long t = a%b;
            a=b;  b=t;
        }
        return a;
    }
}