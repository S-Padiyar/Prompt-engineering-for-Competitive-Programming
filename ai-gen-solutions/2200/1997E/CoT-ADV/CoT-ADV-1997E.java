import java.io.*;
import java.util.*;

public class Main {
    static final int MAXN = 200_000;
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(in.readLine());
        int n = Integer.parseInt(st.nextToken());
        int q = Integer.parseInt(st.nextToken());
        int[] a = new int[n+1];
        st = new StringTokenizer(in.readLine());
        for(int i = 1; i <= n; i++){
            a[i] = Integer.parseInt(st.nextToken());
        }
        // We choose a bucket threshold B ~ sqrt(n).
        final int B = 450;
        // Store queries: for small k <= B, bucket them; for large, store in list.
        ArrayList<int[]>[] smallQ = new ArrayList[B+1];
        for(int k = 1; k <= B; k++){
            smallQ[k] = new ArrayList<>();
        }
        List<int[]> largeQ = new ArrayList<>();
        // answers
        String[] ans = new String[q];
        for(int qi = 0; qi < q; qi++){
            st = new StringTokenizer(in.readLine());
            int idx = Integer.parseInt(st.nextToken());
            int k   = Integer.parseInt(st.nextToken());
            if(k <= B){
                smallQ[k].add(new int[]{idx, qi});
            } else {
                largeQ.add(new int[]{idx, k, qi});
            }
        }
        // 1) Process small k by doing one full O(n) scan per k.
        //    Record fight[i] and answer all queries in that bucket.
        for(int k = 1; k <= B; k++){
            if(smallQ[k].isEmpty()) continue;
            int fights = 0;
            boolean[] fightHere = new boolean[n+1];
            for(int i = 1; i <= n; i++){
                if(fights < k * (long)a[i]){
                    fightHere[i] = true;
                    fights++;
                } else {
                    fightHere[i] = false;
                }
            }
            // answer queries for this k
            for(int[] qu : smallQ[k]){
                int idx = qu[0], qid = qu[1];
                ans[qid] = fightHere[idx] ? "YES" : "NO";
            }
        }
        // 2) For each large-k query, simulate until we either pass idx
        //    or the running fight–count reaches k (no more future fights for a_i=1).
        for(int[] qu : largeQ){
            int idx = qu[0], k = qu[1], qid = qu[2];
            int fights = 0;
            boolean didFight = false;
            for(int i = 1; i <= idx; i++){
                if(fights >= k) {
                    // from now on, any a[i]==1 will see fights<k*1 fail.
                    // but maybe a[i]>1 can still pass.  We just keep going
                    // until i==idx or fights<k*a[i] triggers.
                    // In practice, however, once fights>=k we very quickly
                    // give up if a[i]<=1, but for correctness we check all.
                }
                if((long)fights < (long)k * a[i]){
                    fights++;
                    if(i == idx){
                        didFight = true;
                        break;
                    }
                } else {
                    // this monster quits (flees).  If it is the idx-th, we know answer.
                    if(i == idx){
                        didFight = false;
                        break;
                    }
                }
            }
            ans[qid] = didFight ? "YES" : "NO";
        }

        // Output all answers
        PrintWriter out = new PrintWriter(System.out);
        for(int i = 0; i < q; i++){
            out.println(ans[i]);
        }
        out.flush();
    }
}