import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 1_000_000_007;

    // A small struct to hold our 5 values:
    // len    = block length (long)
    // all    = are all elements in the block good?
    // pre    = length of prefix of good elements
    // suf    = length of suffix of good elements
    // F      = sum of L*(L+1)/2 over runs (mod MOD)
    static class Node {
        long len, pre, suf;
        boolean all;
        int F;      // stored mod MOD
        Node(long _len, boolean _all, long _pre, long _suf, int _F) {
            len = _len; all = _all; pre = _pre; suf = _suf; F = _F;
        }
    }

    // dpPow[L][k] = Node for block size 2^L, threshold k
    // only valid for 0 <= L <= 60, 0 <= k <= 60
    static Node[][] dpPow = new Node[61][61];

    // Compute the Node for a perfect block of length 2^L with threshold k.
    // popcount(i) <= k
    static Node buildPow(int L, int k) {
        if (k < 0) {
            // threshold < 0 => nobody is good
            long length = 1L << L;
            return new Node(length, false, 0, 0, 0);
        }
        if (dpPow[L][k] != null) {
            return dpPow[L][k];
        }
        if (L == 0) {
            // block of length 1: only index 0 => popcount(0)=0
            // it is good iff k >= 0
            dpPow[L][k] = new Node(1, true, 1, 1, 1);
            return dpPow[L][k];
        }
        // split into two halves, size 2^(L-1):
        //   left  half uses threshold k
        //   right half uses threshold k-1
        Node left  = buildPow(L - 1, k);
        Node right = buildPow(L - 1, k - 1);

        long len    = left.len + right.len;
        boolean all = left.all & right.all;
        long pre    = left.all ? (left.len + right.pre) : left.pre;
        long suf    = right.all ? (right.len + left.suf) : right.suf;

        int sumF = left.F;
        sumF = (sumF + right.F) % MOD;
        long cross = ( (left.suf % MOD) * (right.pre % MOD) ) % MOD;
        sumF = (int)((sumF + cross) % MOD);

        dpPow[L][k] = new Node(len, all, pre, suf, sumF);
        return dpPow[L][k];
    }

    // For an arbitrary n (not necessarily power of two) with threshold k,
    // we memoize in a small per-test map.
    static Map<Long, Map<Integer,Node>> dpSize;

    static Node buildSize(long n, int k) {
        if (n <= 0) {
            // empty block
            return new Node(0, false, 0, 0, 0);
        }
        if (k < 0) {
            // no one is good
            return new Node(n, false, 0, 0, 0);
        }
        // is it exactly a power of two?
        int L = 63 - Long.numberOfLeadingZeros(n);
        long pow = 1L << L;
        if (pow == n) {
            // delegate to buildPow
            return buildPow(L, k);
        }
        // memo check
        Map<Integer,Node> m = dpSize.get(n);
        if (m != null && m.containsKey(k)) {
            return m.get(k);
        }

        // split [0..n) into [0..2^L) and [2^L..n)
        long r = n - pow;
        Node left  = buildPow(L, k);
        Node right = buildSize(r, k - 1);

        long len    = left.len + right.len;
        boolean all = left.all & right.all;
        long pre    = left.all ? (left.len + right.pre) : left.pre;
        long suf    = right.all ? (right.len + left.suf) : right.suf;

        int sumF = left.F;
        sumF = (sumF + right.F) % MOD;
        long cross = ((left.suf % MOD) * (right.pre % MOD)) % MOD;
        sumF = (int)((sumF + cross) % MOD);

        Node res = new Node(len, all, pre, suf, sumF);
        if (m == null) {
            m = new HashMap<>();
            dpSize.put(n, m);
        }
        m.put(k, res);
        return res;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(in.readLine().trim());
        StringBuilder sb = new StringBuilder();

        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            long n = Long.parseLong(st.nextToken());
            int k  = Integer.parseInt(st.nextToken());

            // Per test we clear the dpSize
            dpSize = new HashMap<>();

            // Build the final Node for [0..n) with threshold k.
            Node ans = buildSize(n, k);

            // The answer is ans.F (modulo 1e9+7)
            sb.append(ans.F).append('\n');
        }
        System.out.print(sb);
    }
}