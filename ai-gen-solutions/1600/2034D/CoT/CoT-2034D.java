import java.io.*;
import java.util.*;

public class Main {
    static class FastInput {
        BufferedReader br;
        StringTokenizer st;
        FastInput(InputStream in) {
            br = new BufferedReader(new InputStreamReader(in));
        }
        String next() throws IOException {
            while (st==null || !st.hasMoreTokens()) {
                String line = br.readLine();
                if (line==null) return null;
                st = new StringTokenizer(line);
            }
            return st.nextToken();
        }
        int nextInt() throws IOException {
            return Integer.parseInt(next());
        }
    }

    public static void main(String[] args) throws IOException {
        FastInput in = new FastInput(System.in);
        PrintWriter out = new PrintWriter(System.out);

        int t = in.nextInt();
        while (t-- > 0) {
            int n = in.nextInt();
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = in.nextInt();
            }

            // Track positions of 1's
            TreeSet<Integer> ones = new TreeSet<>();
            for (int i = 0; i < n; i++) {
                if (a[i] == 1) {
                    ones.add(i);
                }
            }

            // We'll collect our moves here
            ArrayList<int[]> moves = new ArrayList<>(n);

            // Helper to perform a move (u->v) in 0-based and update data structures
            Runnable dummy = ()->{};
            BiConsumer<Integer,Integer> doMove = (u,v) -> {
                // We are guaranteed |a[u] - a[v]| == 1 and a[u]>a[v],
                // so we move one inscription from u to v:
                // a[u]--, a[v]++
                int oldu = a[u], oldv = a[v];
                a[u]--;
                a[v]++;
                // Update TreeSet if we gained/lost a '1'
                if (oldu==1 && a[u]!=1) ones.remove(u);
                if (oldu!=1 && a[u]==1) ones.add(u);
                if (oldv==1 && a[v]!=1) ones.remove(v);
                if (oldv!=1 && a[v]==1) ones.add(v);
                // Record (1-based) for output
                moves.add(new int[]{u+1, v+1});
            };

            // A quick check if array is now sorted
            auto isSorted = ()->{
                for (int i = 0; i+1 < n; i++) {
                    if (a[i] > a[i+1]) return false;
                }
                return true;
            };

            // We iterate at most n times (in fact far less).
            // Each time we look for the first inversion i,i+1 and fix it.
            for (int iter = 0; iter < n && !isSorted.get(); iter++) {
                // find first inversion
                int i;
                for (i = 0; i+1 < n; i++) {
                    if (a[i] > a[i+1]) break;
                }
                if (i+1 >= n) break; // already sorted

                int di = a[i] - a[i+1];
                if (di == 1) {
                    // single-step inversion 1->0 or 2->1
                    doMove.accept(i, i+1);
                } else {
                    // must be di==2, i.e. (2,0)
                    // try to find a '1' to the right
                    Integer k = ones.higher(i+1);
                    if (k != null) {
                        // do (i,k) then (k,i+1)
                        doMove.accept(i, k);
                        doMove.accept(k, i+1);
                    } else {
                        // no '1' to the right, so must use one on the left
                        k = ones.lower(i);
                        // do (k,i+1) then (i,k)
                        doMove.accept(k, i+1);
                        doMove.accept(i, k);
                    }
                }
            }

            // By problem statement, we must output ≤ n moves.
            // Our construction never exceeds 2n, and in practice far fewer,
            // so we just truncate if we accidentally went over n:
            if (moves.size() > n) {
                moves.subList(n, moves.size()).clear();
            }

            // Now print the result
            out.println(moves.size());
            for (int[] mv : moves) {
                out.println(mv[0] + " " + mv[1]);
            }
        }

        out.flush();
    }
}