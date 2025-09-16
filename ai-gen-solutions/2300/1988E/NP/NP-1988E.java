import java.io.*;
import java.util.*;

public class Main {
    static FastInput in = new FastInput();
    static PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));

    public static void main(String[] args) throws IOException {
        int T = in.nextInt();
        while (T-- > 0) {
            int n = in.nextInt();
            int[] a = new int[n+2];
            for (int i = 1; i <= n; i++) {
                a[i] = in.nextInt();
            }
            // 1) compute ple and nle
            int[] ple = new int[n+2];
            int[] nle = new int[n+2];
            {
                Deque<Integer> st = new ArrayDeque<>();
                // previous strictly smaller
                for (int i = 1; i <= n; i++) {
                    while (!st.isEmpty() && a[st.peek()] >= a[i]) {
                        st.pop();
                    }
                    ple[i] = st.isEmpty() ? 0 : st.peek();
                    st.push(i);
                }
                st.clear();
                // next smaller or equal
                for (int i = n; i >= 1; i--) {
                    while (!st.isEmpty() && a[st.peek()] > a[i]) {
                        st.pop();
                    }
                    nle[i] = st.isEmpty() ? (n+1) : st.peek();
                    st.push(i);
                }
            }

            // 2) we will build two difference‐arrays A_diff,B_diff so that
            //      S1[i] = A[i]*i + B[i],
            //    where S1[i] is sum of mins of all subarrays that include a[i].
            long[] A_diff = new long[n+3], B_diff = new long[n+3];

            // For each k, it is min exactly on subarrays [l..r] with
            //    ple[k] < l <= k <= r < nle[k].
            // For each such subarray and each i in [l..r], we must add a[k].
            // Group by i:
            //  i in [ple[k]+1 .. k]:
            //      count of (l..r) with l<=i<=r  = (i - ple[k]) * (nle[k] - k).
            //  i in [k+1 .. nle[k]-1]:
            //      count                     = (k - ple[k]) * (nle[k] - i).
            // So on [L+1..k] we add f(i)= w1*i - w1*L  with w1 = a[k]*(nle[k]-k).
            //    on [k+1..R-1] we add g(i)= -w2*i + w2*R with w2 = a[k]*(k-ple[k]).
            for (int k = 1; k <= n; k++) {
                int L = ple[k], R = nle[k];
                long v = a[k];
                long w1 = v * (R - k);
                // range [L+1..k], add +w1*i  + (-w1*L)
                if (L+1 <= k) {
                    A_diff[L+1] += w1;
                    A_diff[k+1]  -= w1;
                    B_diff[L+1] += -w1 * L;
                    B_diff[k+1]  -= -w1 * L;
                }
                long w2 = v * (k - L);
                // range [k+1..R-1], add -w2*i + w2*R
                if (k+1 <= R-1) {
                    A_diff[k+1]   += -w2;
                    A_diff[R]     -= -w2;
                    B_diff[k+1]   += w2 * R;
                    B_diff[R]     -= w2 * R;
                }
            }

            // prefix‐sum to get A[i], B[i]
            long[] A = new long[n+2], B = new long[n+2];
            long curA=0, curB=0;
            for (int i = 1; i <= n; i++) {
                curA += A_diff[i];
                curB += B_diff[i];
                A[i] = curA;
                B[i] = curB;
            }

            // Now S1[i] = A[i]*i + B[i]
            long[] S1 = new long[n+2];
            for (int i = 1; i <= n; i++) {
                S1[i] = A[i] * i + B[i];
            }

            // 3) compute prefF[i] = sum of min over ALL subarrays of a[1..i],
            //    by the standard O(n) “sum of subarray minima” DP
            long[] prefF = new long[n+2];
            {
                Deque<Integer> st = new ArrayDeque<>();
                long[] dp = new long[n+2];
                for (int i = 1; i <= n; i++) {
                    while (!st.isEmpty() && a[st.peek()] > a[i]) {
                        st.pop();
                    }
                    int prev = st.isEmpty() ? 0 : st.peek();
                    dp[i] = dp[prev] + (long)a[i]*(i - prev);
                    prefF[i] = prefF[i-1] + dp[i];
                    st.push(i);
                }
            }
            // 4) compute suffF[i] = sum of min over ALL subarrays of a[i..n],
            //    similarly from the right
            long[] suffF = new long[n+3];
            {
                Deque<Integer> st = new ArrayDeque<>();
                long[] dp = new long[n+3];
                for (int i = n; i >= 1; i--) {
                    while (!st.isEmpty() && a[st.peek()] >= a[i]) {
                        st.pop();
                    }
                    int nxt = st.isEmpty() ? (n+1) : st.peek();
                    dp[i] = dp[nxt] + (long)a[i]*(nxt - i);
                    suffF[i] = suffF[i+1] + dp[i];
                    st.push(i);
                }
            }

            // 5) the total f(a) is prefF[n]
            long FA = prefF[n];

            // 6) finally for each i,
            //    f(b_i) = f(a) - S1[i].
            // (one checks via partitioning each subarray of a into those
            //  fully left, fully right, or including i; and the crossing‐gap
            //  subarrays of b exactly replace the “including‐i” ones.)
            StringBuilder ans = new StringBuilder(n*10);
            for (int i = 1; i <= n; i++) {
                long fi = FA - S1[i];
                ans.append(fi).append(' ');
            }
            out.println(ans);
        }
        out.flush();
    }

    static class FastInput {
        BufferedReader br;
        StringTokenizer st;
        FastInput() {
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
    }
}