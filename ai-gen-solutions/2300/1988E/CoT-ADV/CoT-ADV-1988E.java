import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int T = Integer.parseInt(in.readLine().trim());
        // We will re-use arrays up to total N ≤ 10^6 over all tests.
        // We'll allocate them lazily per test.
        while (T-- > 0) {
            int n = Integer.parseInt(in.readLine().trim());
            int[] a = new int[n+2];
            StringTokenizer st = new StringTokenizer(in.readLine());
            for (int i = 1; i <= n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }

            // 1) Compute PL[] (previous smaller) and NL[] (next smaller)
            int[] PL = new int[n+2], NL = new int[n+2];
            // values a[0] = a[n+1] = +∞ sentinel
            a[0] = a[n+1] = Integer.MAX_VALUE;

            // compute PL with a stack
            {
                Deque<Integer> stk = new ArrayDeque<>();
                stk.push(0);
                for (int i = 1; i <= n; i++) {
                    while (a[stk.peek()] > a[i]) {
                        stk.pop();
                    }
                    PL[i] = stk.peek();
                    stk.push(i);
                }
            }
            // compute NL with a stack
            {
                Deque<Integer> stk = new ArrayDeque<>();
                stk.push(n+1);
                for (int i = n; i >= 1; i--) {
                    while (a[stk.peek()] > a[i]) {
                        stk.pop();
                    }
                    NL[i] = stk.peek();
                    stk.push(i);
                }
            }

            // 2) Precompute W1, W2, X and totalF = sum X
            long[] W1 = new long[n+2], W2 = new long[n+2], X = new long[n+2];
            long totalF = 0;
            for (int j = 1; j <= n; j++) {
                long leftCnt = j - PL[j];
                long rightCnt = NL[j] - j;
                long xj = (long) a[j] * leftCnt * rightCnt;
                X[j] = xj;
                totalF += xj;

                W1[j] = (long) a[j] * rightCnt;   // for termLeft
                W2[j] = (long) a[j] * leftCnt;    // for termRight
            }

            // 3) Build termLeft and termRight by difference-arrays + prefix-sum
            long[] diffA = new long[n+3], diffB = new long[n+3];
            // termLeft[i] = i * A[i] - B[i],
            // where A[i] = sum of W1[j] over j>=i with PL[j]<i<=j,
            // and   B[i] = sum of W1[j]*PL[j] over same j-range.

            // Range update for each j: i runs from (PL[j]+1) to j.
            for (int j = 1; j <= n; j++) {
                int L = PL[j] + 1, R = j;
                if (L <= R) {
                    diffA[L] += W1[j];
                    diffA[R+1] -= W1[j];
                    diffB[L] += W1[j] * PL[j];
                    diffB[R+1] -= W1[j] * PL[j];
                }
            }
            long[] termLeft = new long[n+2];
            long curA = 0, curB = 0;
            for (int i = 1; i <= n; i++) {
                curA += diffA[i];
                curB += diffB[i];
                termLeft[i] = (long)i * curA - curB;
            }

            // Now build termRight similarly
            Arrays.fill(diffA, 0);
            Arrays.fill(diffB, 0);
            // termRight[i] = U[i] - i*V[i],
            // where U[i] = sum W2[j]*NL[j] over j≤i with i<NL[j],
            //       V[i] = sum W2[j]      over same j-range.
            // So each j updates i in [j..NL[j]-1].
            for (int j = 1; j <= n; j++) {
                int L = j, R = NL[j] - 1;
                if (L <= R) {
                    diffA[L] += W2[j] * NL[j];
                    diffA[R+1] -= W2[j] * NL[j];
                    diffB[L] += W2[j];
                    diffB[R+1] -= W2[j];
                }
            }
            long[] termRight = new long[n+2];
            curA = curB = 0;
            for (int i = 1; i <= n; i++) {
                curA += diffA[i];
                curB += diffB[i];
                termRight[i] = curA - (long)i * curB;
            }

            // 4) Now for each i, S_i = termLeft[i] + termRight[i] - X[i],
            //    answer = totalF - S_i
            StringBuilder ans = new StringBuilder();
            for (int i = 1; i <= n; i++) {
                long Si = termLeft[i] + termRight[i] - X[i];
                long fi = totalF - Si;
                ans.append(fi).append(' ');
            }
            out.println(ans);
        }
        out.flush();
    }
}