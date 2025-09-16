import java.io.*;
import java.util.*;

public class Main {
    static class Fenwick {
        int n;
        long[] fenwCount, fenwSum;
        Fenwick(int n) {
            this.n = n;
            fenwCount = new long[n+1];
            fenwSum   = new long[n+1];
        }
        // add cnt to count at pos, and add val to sum at pos
        void update(int pos, long cnt, long val) {
            for(int i = pos; i <= n; i += i & -i) {
                fenwCount[i] += cnt;
                fenwSum[i]   += val;
            }
        }
        // sum of counts in [1..pos]
        long sumCount(int pos) {
            long s = 0;
            for(int i = pos; i > 0; i -= i & -i)
                s += fenwCount[i];
            return s;
        }
        // sum of values in [1..pos]
        long sumVal(int pos) {
            long s = 0;
            for(int i = pos; i > 0; i -= i & -i)
                s += fenwSum[i];
            return s;
        }
        // find smallest idx such that sumCount(idx) >= k
        int selectKth(long k) {
            int pos = 0;
            int bitMask = 1 << (31 - Integer.numberOfLeadingZeros(n));
            for(; bitMask > 0; bitMask >>= 1) {
                int nxt = pos + bitMask;
                if(nxt <= n && fenwCount[nxt] < k) {
                    k -= fenwCount[nxt];
                    pos = nxt;
                }
            }
            return pos+1;
        }
        // sum of the top K elements in the multiset
        // we store values in descending order at indices 1..n
        long sumTopK(long K, int[] compVal) {
            if(K <= 0) return 0;
            long totalCnt = sumCount(n);
            if(K >= totalCnt) {
                // take all
                return sumVal(n);
            }
            // find index t where the prefix count just reaches K
            int t = selectKth(K);
            long before = sumCount(t-1);
            long sumBefore = sumVal(t-1);
            long needed = K - before;     // how many from this bucket
            return sumBefore + needed * compVal[t];
        }
        // the (K)-th largest element (K=1→largest, K=2→2nd largest)
        long kthLargest(long K, int[] compVal) {
            long totalCnt = sumCount(n);
            if(K > totalCnt) return 0;
            int t = selectKth(K);
            return compVal[t];
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        int T = Integer.parseInt(br.readLine().trim());
        while(T-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            long c = Long.parseLong(st.nextToken());
            long[] a = new long[n+1];
            st = new StringTokenizer(br.readLine());
            for(int i = 1; i <= n; i++) {
                a[i] = Long.parseLong(st.nextToken());
            }

            // prefix sums of a
            long[] pref = new long[n+1];
            for(int i = 1; i <= n; i++)
                pref[i] = pref[i-1] + a[i];

            // 1) find original winner with no exclusions
            //    candidate 1 gets a[1]+c, all others get a[j].
            long bestVotes = a[1] + c;
            int winnerNoEx = 1;
            for(int j = 2; j <= n; j++) {
                long vj = a[j];
                if(vj > bestVotes) {
                    bestVotes = vj;
                    winnerNoEx = j;
                }
            }

            // 2) compress values of a[] in descending order
            long[] tmp = Arrays.copyOfRange(a, 1, n+1);
            Arrays.sort(tmp);
            // dedupe
            int m = 0;
            for(int i = 0; i < n; i++) {
                if(i == 0 || tmp[i] != tmp[i-1]) {
                    tmp[m++] = tmp[i];
                }
            }
            // now tmp[0..m-1] is ascending distinct; we want descending
            int[] compVal = new int[m+1];
            Map<Long,Integer> mp = new HashMap<>();
            for(int i = 0; i < m; i++) {
                compVal[m-i] = (int)tmp[i];  // i=0 → compVal[m], i=m-1 → compVal[1]
                mp.put(tmp[i], m-i);
            }

            Fenwick fenw = new Fenwick(m);
            long[] ans = new long[n+1];

            // process i=n..1; fenw holds a[j] for j>i
            for(int i = n; i >= 1; i--) {
                long base = c + pref[i-1];
                long V0   = a[i] + base;

                if(i == winnerNoEx) {
                    ans[i] = 0;
                } else {
                    long suffixSize = fenw.sumCount(m);
                    if(suffixSize == 0) {
                        // no one to compete → we must only have excluded 1..i-1
                        ans[i] = (i-1);
                    } else {
                        // check if V0 already beats the largest in suffix
                        long mx1 = fenw.kthLargest(1, compVal);
                        if(V0 >= mx1) {
                            ans[i] = (i-1);  // no further excludes needed
                        } else {
                            // binary search minimal k in [1..suffixSize]
                            long left = 1, right = suffixSize;
                            long needK = suffixSize; 
                            while(left <= right) {
                                long mid = (left + right) >>> 1;
                                long sumTop = fenw.sumTopK(mid, compVal);
                                long nextVal = fenw.kthLargest(mid+1, compVal);
                                if(V0 + sumTop >= nextVal) {
                                    needK = mid;
                                    right = mid-1;
                                } else {
                                    left = mid+1;
                                }
                            }
                            ans[i] = (i-1) + needK;
                        }
                    }
                }

                // finally insert a[i] into our Fenwick so it will appear in future suffixes
                int cidx = mp.get(a[i]);
                fenw.update(cidx, 1, a[i]);
            }

            // output
            for(int i = 1; i <= n; i++) {
                sb.append(ans[i]).append(i==n?'\n':' ');
            }
        }
        System.out.print(sb);
    }
}