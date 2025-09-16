import java.io.*;
import java.util.*;

public class Main {
    static int N;
    static int[] px, py;    // prefix sums
    static int[] dx, dy;    // original deltas per character
    static NodeTX[] segx;
    static NodeTY[] segy;
    static String s;
    static int n, k, w, h;

    // A little segment-tree node for px[]
    static class NodeTX {
        int mn, mx;
    }
    // A little segment-tree node for py[]
    static class NodeTY {
        int mn, mx;
    }

    // Build segment tree on px[] in segx[1..4n]
    static void buildX(int idx, int l, int r) {
        if (l == r) {
            segx[idx].mn = segx[idx].mx = px[l];
        } else {
            int mid = (l + r) >>> 1;
            buildX(idx<<1, l, mid);
            buildX(idx<<1|1, mid+1, r);
            segx[idx].mn = Math.min(segx[idx<<1].mn, segx[idx<<1|1].mn);
            segx[idx].mx = Math.max(segx[idx<<1].mx, segx[idx<<1|1].mx);
        }
    }
    // Build segment tree on py[] in segy[1..4n]
    static void buildY(int idx, int l, int r) {
        if (l == r) {
            segy[idx].mn = segy[idx].mx = py[l];
        } else {
            int mid = (l + r) >>> 1;
            buildY(idx<<1, l, mid);
            buildY(idx<<1|1, mid+1, r);
            segy[idx].mn = Math.min(segy[idx<<1].mn, segy[idx<<1|1].mn);
            segy[idx].mx = Math.max(segy[idx<<1].mx, segy[idx<<1|1].mx);
        }
    }

    // Find the minimal j in [ql..qr] such that px[j]<Tlo or px[j]>Thi, or -1 if none.
    static int queryX(int idx, int l, int r, int ql, int qr, int Tlo, int Thi) {
        if (r < ql || l > qr) return -1;
        if (ql <= l && r <= qr) {
            if (segx[idx].mn >= Tlo && segx[idx].mx <= Thi) {
                return -1; // no violation here
            }
        }
        if (l == r) {
            // l==r is inside [ql..qr] and either <Tlo or >Thi
            return l;
        }
        int mid = (l + r) >>> 1;
        int res = queryX(idx<<1, l, mid, ql, qr, Tlo, Thi);
        if (res != -1) return res;
        return queryX(idx<<1|1, mid+1, r, ql, qr, Tlo, Thi);
    }

    // Same for py[]
    static int queryY(int idx, int l, int r, int ql, int qr, int Tlo, int Thi) {
        if (r < ql || l > qr) return -1;
        if (ql <= l && r <= qr) {
            if (segy[idx].mn >= Tlo && segy[idx].mx <= Thi) {
                return -1;
            }
        }
        if (l == r) return l;
        int mid = (l + r) >>> 1;
        int res = queryY(idx<<1, l, mid, ql, qr, Tlo, Thi);
        if (res != -1) return res;
        return queryY(idx<<1|1, mid+1, r, ql, qr, Tlo, Thi);
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter pw = new PrintWriter(System.out);

        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            String[] parts = br.readLine().trim().split(" ");
            n = Integer.parseInt(parts[0]);
            k = Integer.parseInt(parts[1]);
            w = Integer.parseInt(parts[2]);
            h = Integer.parseInt(parts[3]);
            s = br.readLine().trim();

            // Build dx, dy
            dx = new int[n+1];
            dy = new int[n+1];
            for (int i = 0; i < n; i++) {
                char c = s.charAt(i);
                if (c == 'L') dx[i+1] = -1;
                else if (c == 'R') dx[i+1] = +1;
                else dx[i+1] = 0;
                if (c == 'D') dy[i+1] = -1;
                else if (c == 'U') dy[i+1] = +1;
                else dy[i+1] = 0;
            }

            // Prefix sums
            px = new int[n+1];
            py = new int[n+1];
            for (int i = 1; i <= n; i++) {
                px[i] = px[i-1] + dx[i];
                py[i] = py[i-1] + dy[i];
            }

            // Build segment trees
            segx = new NodeTX[4*(n+1)];
            for (int i = 0; i < segx.length; i++) segx[i] = new NodeTX();
            buildX(1, 0, n);

            segy = new NodeTY[4*(n+1)];
            for (int i = 0; i < segy.length; i++) segy[i] = new NodeTY();
            buildY(1, 0, n);

            // Map from (px[i],py[i]) to list of i
            HashMap<Long, ArrayList<Integer>> map = new HashMap<>();
            for (int i = 1; i <= n; i++) {
                long key = (((long)px[i])<<32) ^ (py[i] & 0xffffffffL);
                map.computeIfAbsent(key, z->new ArrayList<>()).add(i);
            }
            long ans = 0;

            // State
            int runsLeft = k;
            int pos = 1;          // next script-index
            int x0 = 0, y0 = 0;   // current physical coords
            int fx = +1, fy = +1; // flags (+1=normal, -1=flipped)

            while (runsLeft > 0) {
                // We are at the start of a block, script-index = pos, physical = (x0,y0), flags=(fx,fy).
                // We want the earliest violation in x or y.
                int baseX = px[pos-1], baseY = py[pos-1];

                // compute thresholds Tlo/T_hi for x
                int TloX, ThiX;
                if (fx == +1) {
                    TloX = baseX - x0;           // px[j] < TloX => x<0
                    ThiX = baseX + (w - x0);     // px[j] > ThiX => x>w
                } else {
                    // x0 - (px[j]-baseX) <0 <=> px[j] > baseX + x0
                    // x0 - (px[j]-baseX) >w <=> px[j] < baseX + x0 - w
                    TloX = baseX + x0 - w;
                    ThiX = baseX + x0;
                }
                int i1 = queryX(1, 0, n, pos, n, TloX, ThiX);
                if (i1<0) i1 = n+1;

                // same for y
                int TloY, ThiY;
                if (fy == +1) {
                    TloY = baseY - y0;
                    ThiY = baseY + (h - y0);
                } else {
                    TloY = baseY + y0 - h;
                    ThiY = baseY + y0;
                }
                int i2 = queryY(1, 0, n, pos, n, TloY, ThiY);
                if (i2<0) i2 = n+1;

                int j0 = Math.min(i1, i2);

                if (j0 > n) {
                    // no violation in [pos..n], we can finish this run safely
                    int len = n - pos + 1;
                    if (len > 0) {
                        // count how many i in [pos..n] land at (0,0)
                        // solve x0 + fx*(px[i]-baseX)=0 => px[i]=baseX - fx*x0
                        // likewise py[i]= baseY - fy*y0
                        int tx = baseX - fx*x0;
                        int ty = baseY - fy*y0;
                        long key = (((long)tx)<<32) ^ (ty & 0xffffffffL);
                        ArrayList<Integer> lst = map.get(key);
                        if (lst != null) {
                            // count indices in lst that lie in [pos..n]
                            int low = Collections.binarySearch(lst, pos);
                            if (low<0) low = -low-1;
                            ans += (lst.size() - low);
                        }
                    }
                    // update the robot's final (x,y) after the whole run:
                    x0 += fx * (px[n] - baseX);
                    y0 += fy * (py[n] - baseY);
                    // next run
                    pos = 1;
                    runsLeft--;
                    // flags fx,fy stay the same
                } else {
                    // violation at j0, so we first do [pos..j0-1] safely
                    int seglen = j0 - pos;
                    if (seglen > 0) {
                        int tx = baseX - fx*x0;
                        int ty = baseY - fy*y0;
                        long key = (((long)tx)<<32) ^ (ty & 0xffffffffL);
                        ArrayList<Integer> lst = map.get(key);
                        if (lst != null) {
                            // count in [pos..j0-1]
                            int low = Collections.binarySearch(lst, pos);
                            if (low<0) low = -low-1;
                            int up = Collections.binarySearch(lst, j0);
                            if (up<0) up = -up-1;
                            ans += (up - low);
                        }
                    }
                    // now apply step j0 under reflection
                    boolean rx = (i1 == j0);
                    boolean ry = (i2 == j0);
                    // position _before_ step j0 is
                    int xprev = x0 + fx*(px[j0-1] - baseX);
                    int yprev = y0 + fy*(py[j0-1] - baseY);
                    // the raw dx,dy = dx[j0], dy[j0]
                    int ddx = dx[j0], ddy = dy[j0];
                    int effdx = (rx ? -fx : fx)*ddx;
                    int effdy = (ry ? -fy : fy)*ddy;
                    int xnew = xprev + effdx;
                    int ynew = yprev + effdy;
                    x0 = xnew;
                    y0 = ynew;
                    if (rx) fx = -fx;
                    if (ry) fy = -fy;
                    // advance pos
                    pos = j0 + 1;
                    if (pos > n) {
                        // that ended a run
                        pos = 1;
                        runsLeft--;
                    }
                }
            }

            pw.println(ans);
        }

        pw.flush();
        pw.close();
    }
}