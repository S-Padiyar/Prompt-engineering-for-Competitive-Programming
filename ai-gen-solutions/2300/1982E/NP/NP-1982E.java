import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class Main {
    static final int MAXD = 60;
    static final int MAXK = 60;
    static final int Kdim = MAXK + 2;    // indices 0..61 represent thresholds -1..60
    static final int MOD = 1_000_000_007;

    // We'll store for each d=0..60 and each kidx=0..61
    // a small struct of 5 fields:
    //   len  = length 2^d
    //   all  = true if every point is allowed
    //   pre  = length of maximal prefix of allowed
    //   suf  = length of maximal suffix of allowed
    //   ans  = sum of L*(L+1)/2 over zero‐runs inside the block
    static Node[][] DP = new Node[MAXD+1][Kdim];

    // A small record for merging
    static class Node {
        long len;   // up to 2^60
        boolean all;
        long pre, suf;
        int ans;    // mod 1e9+7

        Node() {}

        Node(long len, boolean all, long pre, long suf, int ans) {
            this.len = len;
            this.all = all;
            this.pre = pre;
            this.suf = suf;
            this.ans = ans;
        }
    }

    // Merge two adjacent intervals A, B -> C
    static Node merge(Node A, Node B) {
        Node C = new Node();
        C.len = A.len + B.len;
        C.all = A.all && B.all;
        C.pre = A.all ? (A.len + B.pre) : A.pre;
        C.suf = B.all ? (B.len + A.suf) : B.suf;
        long cross = ((A.suf % MOD) * (B.pre % MOD)) % MOD;
        C.ans = (int)(((long)A.ans + B.ans + cross) % MOD);
        return C;
    }

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        // 1) Precompute DP table
        // d = 0: block of size 1.  That single point t=0 has popcount=0.
        // so it's allowed iff K' >= 0.
        for (int kidx = 0; kidx < Kdim; kidx++) {
            int Kp = kidx - 1;      // actual threshold
            boolean ok = (Kp >= 0);
            DP[0][kidx] = new Node(
                1,       // len = 1
                ok,      // all
                ok ? 1 : 0,   // pre
                ok ? 1 : 0,   // suf
                ok ? 1 : 0    // ans = 1*(1+1)/2 = 1 if allowed
            );
        }
        // Build up to d=60
        for (int d = 1; d <= MAXD; d++) {
            for (int kidx = 0; kidx < Kdim; kidx++) {
                Node A = DP[d-1][kidx];
                int bidx = (kidx == 0 ? 0 : kidx - 1);
                Node B = DP[d-1][bidx];
                DP[d][kidx] = merge(A, B);
            }
            // Fix the len field = 2^d
            long l = 1L << d;
            for (int kidx = 0; kidx < Kdim; kidx++) {
                DP[d][kidx].len = l;
            }
        }

        // 2) Answer queries
        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            long n = Long.parseLong(st.nextToken());
            int k = Integer.parseInt(st.nextToken());

            // We'll build up the merged interval [0..n-1]
            Node cur = new Node(0, true, 0, 0, 0);
            int popH = 0;   // popcount of the high‐bits prefix processed

            for (int d = MAXD; d >= 0; d--) {
                if (((n >>> d) & 1L) != 0) {
                    // we have a block of size 2^d aligned at the current position
                    int Kp = k - popH;
                    if (Kp < -1)   Kp = -1;
                    if (Kp > MAXK) Kp = MAXK;
                    int kidx = Kp + 1;

                    // merge with DP[d][kidx]
                    cur = merge(cur, DP[d][kidx]);
                    popH++;
                }
            }

            out.println(cur.ans);
        }

        out.flush();
    }
}