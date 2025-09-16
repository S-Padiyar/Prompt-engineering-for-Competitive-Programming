import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // Fast I/O
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter pw = new PrintWriter(System.out);

        int t = Integer.parseInt(br.readLine().trim());
        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int k = Integer.parseInt(st.nextToken());
            String sStr = br.readLine().trim();
            char[] s = sStr.toCharArray();

            int answer = -1;

            // Try both patterns: pType=0 for t0 starting with 0, pType=1 for t1 starting with 1
            outer:
            for (int pType = 0; pType < 2; pType++) {
                // Build the k‐alternating pattern t
                char[] tpat = new char[n];
                for (int i = 0; i < n; i++) {
                    int block = (i / k);
                    char zeroOne = (block % 2 == 0 ? '0' : '1');
                    if (pType == 0) {
                        tpat[i] = zeroOne;            // t0
                    } else {
                        tpat[i] = (zeroOne == '0' ? '1' : '0'); // t1
                    }
                }

                // Build r = reverse(tpat)
                char[] r = new char[n];
                for (int i = 0; i < n; i++) {
                    r[i] = tpat[n - 1 - i];
                }

                // 1) Compute preLen = max p such that s[0..p-1] == r[0..p-1]
                int preLen = n;
                for (int i = 0; i < n; i++) {
                    if (s[i] != r[i]) {
                        preLen = i;
                        break;
                    }
                }

                // 2) Build Z‐string: U = tpat + '#' + s
                int L = n + 1 + n;
                char[] U = new char[L];
                for (int i = 0; i < n; i++) U[i] = tpat[i];
                U[n] = '#';
                for (int i = 0; i < n; i++) U[n + 1 + i] = s[i];

                // 3) Z‐algorithm on U
                int[] Z = new int[L];
                int l = 0, rptr = 0;
                for (int i = 1; i < L; i++) {
                    if (i <= rptr) {
                        Z[i] = Math.min(rptr - i + 1, Z[i - l]);
                    }
                    while (i + Z[i] < L && U[Z[i]] == U[i + Z[i]]) {
                        Z[i]++;
                    }
                    if (i + Z[i] - 1 > rptr) {
                        l = i;
                        rptr = i + Z[i] - 1;
                    }
                }

                // 4) Extract suf array: suf[i] = LCP(s[i..], tpat)
                //    s starts at U-index (n+1), so for s at index i: U-index = n+1 + i
                int[] suf = new int[n + 1];
                for (int i = 0; i < n; i++) {
                    suf[i] = Z[n + 1 + i];
                }
                suf[n] = 0; // beyond end

                // 5) Scan p = 1..preLen
                //    Need s[0..p-1]==r[0..p-1] and s[p..] matches tpat[0..n-p-1]
                for (int p = 1; p <= preLen; p++) {
                    if (p < n) {
                        // need the suffix-match: suf[p] >= (n - p)
                        if (suf[p] >= n - p) {
                            answer = p;
                            break outer;
                        }
                    } else {
                        // p==n ⇒ suffix is empty, prefix already matched
                        answer = p;
                        break outer;
                    }
                }
            }

            pw.println(answer);
        }

        pw.flush();
    }
}