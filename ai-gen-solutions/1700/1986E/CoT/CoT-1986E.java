import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(br.readLine().trim());
        final long INF = Long.MAX_VALUE / 4;

        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            long k = Long.parseLong(st.nextToken());
            
            st = new StringTokenizer(br.readLine());
            // Group by residue mod k
            HashMap<Long, ArrayList<Long>> map = new HashMap<>();
            for (int i = 0; i < n; i++) {
                long a = Long.parseLong(st.nextToken());
                long r = a % k;
                map.computeIfAbsent(r, x -> new ArrayList<>()).add(a);
            }

            // Count how many buckets have odd size
            int oddCount = 0;
            for (ArrayList<Long> v : map.values()) {
                if ((v.size() & 1) == 1) {
                    oddCount++;
                }
            }
            // Must match n%2
            if (oddCount != (n & 1)) {
                out.println(-1);
                continue;
            }

            long answer = 0;
            boolean impossible = false;

            // Process each residue‐bucket independently
            for (ArrayList<Long> v : map.values()) {
                Collections.sort(v);
                int m = v.size();
                // If even, pair adjacently
                if ((m & 1) == 0) {
                    for (int i = 0; i < m; i += 2) {
                        // cost = (v[i+1] - v[i]) / k
                        answer += (v.get(i+1) - v.get(i)) / k;
                    }
                } else {
                    // m odd: do the DP to decide which single element to leave out
                    // 1) prefix DP
                    long[] dpPref = new long[m+1];
                    dpPref[0] = 0;
                    for (int i = 1; i <= m; i++) {
                        if ((i & 1) == 1) {
                            dpPref[i] = INF;  // can't pair an odd count
                        } else {
                            long cost = (v.get(i-1) - v.get(i-2)) / k;
                            dpPref[i] = dpPref[i-2] + cost;
                        }
                    }
                    // 2) suffix DP
                    // dpSuf[i] = minimal cost to pair v[i..m] if that segment length is even
                    long[] dpSuf = new long[m+2];
                    dpSuf[m+1] = 0;
                    for (int i = m; i >= 1; i--) {
                        int len = (m - i + 1);
                        if ((len & 1) == 1) {
                            dpSuf[i] = INF;
                        } else {
                            long cost = (v.get(i) - v.get(i-1)) / k;
                            dpSuf[i] = dpSuf[i+2] + cost;
                        }
                    }
                    // 3) try skipping each t in [1..m]
                    long best = INF;
                    for (int tIndex = 1; tIndex <= m; tIndex++) {
                        long c1 = dpPref[tIndex-1];
                        long c2 = dpSuf[tIndex+1];
                        if (c1 < INF && c2 < INF) {
                            best = Math.min(best, c1 + c2);
                        }
                    }
                    if (best >= INF) {
                        impossible = true;
                        break;
                    }
                    answer += best;
                }
            }

            out.println(impossible ? -1 : answer);
        }

        out.flush();
    }
}