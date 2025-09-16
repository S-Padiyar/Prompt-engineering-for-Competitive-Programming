import java.io.*;
import java.util.*;

public class Main {
    static final int MOD = 1_000_000_007;
    // dpMemo[pos][ones] caches the BlockState for tight==0
    static BlockState[][] dpMemo;
    static boolean[][] seen;
    static int B;          // number of bits in N
    static int K;          // the k from input
    static int[] nbits;    // binary digits of N
    static long[] pow2;    // pow2[i] = 2^i
    static long[] sufVal;  // sufVal[pos] = numeric value of N's suffix from pos..B-1

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(in.readLine().trim());
        pow2 = new long[61];
        pow2[0] = 1;
        for (int i = 1; i <= 60; i++) pow2[i] = pow2[i-1] << 1;

        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            long n = Long.parseLong(st.nextToken());
            K = Integer.parseInt(st.nextToken());
            long N = n - 1;  // we handle [0..N]

            // special case: if K >= 60, all numbers < 2^60 are good
            if (K >= 60) {
                long x = n % MOD;
                long ans = x * ((x + 1) % MOD) % MOD * ((MOD+1)/2) % MOD;
                sb.append(ans).append('\n');
                continue;
            }
            if (N < 0) {
                // n=0 impossible by constraints, but just in case
                sb.append(0).append('\n');
                continue;
            }

            // build binary of N
            String bs = Long.toBinaryString(N);
            B = bs.length();
            nbits = new int[B];
            for (int i = 0; i < B; i++) {
                nbits[i] = bs.charAt(i) - '0';
            }
            // build suffix values
            sufVal = new long[B+1];
            sufVal[B] = 0;
            for (int i = B-1; i >= 0; i--) {
                int bit = nbits[i];
                long contrib = bit * pow2[B-1 - i];
                sufVal[i] = sufVal[i+1] + contrib;
            }

            // prepare memo
            dpMemo = new BlockState[B][K+2];
            seen   = new boolean[B][K+2];
            // compute dp
            BlockState full = dp(0, 0, true);
            sb.append(full.sum).append('\n');
        }
        System.out.print(sb);
    }

    // digit-dp: at position pos, with cnt ones so far, and tight?
    static BlockState dp(int pos, int ones, boolean tight) {
        if (pos == B) {
            // reached a single number leaf
            if (ones <= K) {
                // it's good
                return new BlockState(1, true, 1, 1, 1);
            } else {
                // bad
                return new BlockState(1, false, 0, 0, 0);
            }
        }
        // can we use memo?  only if tight==false
        if (!tight && seen[pos][ones]) {
            return dpMemo[pos][ones];
        }

        int maxBit = tight ? nbits[pos] : 1;
        BlockState acc = null;
        for (int b = 0; b <= maxBit; b++) {
            int nones = ones + (b == 1 ? 1 : 0);
            boolean nt = tight && (b == maxBit);

            BlockState child;
            if (nones > K) {
                // all numbers in that subtree are bad
                long length;
                if (!nt) {
                    // free subtree of depth (B-pos-1) => 2^(B-pos-1) numbers
                    length = pow2[B-1 - pos];
                } else {
                    // still tight => count how many follow
                    // exactly sufVal[pos+1]+1
                    length = sufVal[pos+1] + 1;
                }
                child = new BlockState(length, false, 0, 0, 0);
            } else {
                // go deeper
                child = dp(pos+1, nones, nt);
            }

            if (acc == null) {
                acc = child;
            } else {
                acc = merge(acc, child);
            }
        }

        if (!tight) {
            seen[pos][ones] = true;
            dpMemo[pos][ones] = acc;
        }
        return acc;
    }

    // Merge two consecutive blocks A then B:
    // len = A.len + B.len
    // full = A.full & B.full
    // head = A.full ? A.len + B.head : A.head
    // tail = B.full ? B.len + A.tail : B.tail
    // sum = A.sum + B.sum + (A.tail * B.head) mod
    static BlockState merge(BlockState A, BlockState B) {
        long len = A.len + B.len;
        boolean full = A.full && B.full;
        long head = A.full ? (A.len + B.head) : A.head;
        long tail = B.full ? (B.len + A.tail) : B.tail;

        long s = A.sum + B.sum;
        s %= MOD;
        long extra = ( (A.tail % MOD) * (B.head % MOD) ) % MOD;
        s = (s + extra) % MOD;
        return new BlockState(len, full, head, tail, s);
    }

    // encapsulates a "block" of consecutive integers
    static class BlockState {
        long len;      // how many ints in that block
        boolean full;  // are *all* of them "good"?
        long head;     // length of initial run of goods
        long tail;     // length of final run of goods
        long sum;      // what the scan would add to ans if cur started at 0

        BlockState(long _len, boolean _full, long _head, long _tail, long _sum) {
            len = _len; full = _full; head = _head; tail = _tail; sum = _sum;
        }
    }
}