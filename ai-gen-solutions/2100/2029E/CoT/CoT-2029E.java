import java.io.*;
import java.util.*;

public class Main {
    static final int MAXA = 400_000;
    // divisors[v] will hold all d >= 2 that divide v
    static ArrayList<Integer>[] divisors = new ArrayList[MAXA+1];

    public static void main(String[] args) throws IOException {
        // 1) Precompute divisors >= 2 for every number up to MAXA
        for (int i = 0; i <= MAXA; i++) {
            divisors[i] = new ArrayList<>();
        }
        for (int d = 2; d <= MAXA; d++) {
            for (int m = d; m <= MAXA; m += d) {
                divisors[m].add(d);
            }
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        int t = Integer.parseInt(br.readLine().trim());

        // scratch arrays we will reuse
        boolean[] visPre = new boolean[MAXA+1];
        boolean[] visF   = new boolean[MAXA+1];
        boolean[] isTarget = new boolean[MAXA+1];
        boolean[] counted  = new boolean[MAXA+1];

        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine().trim());
            StringTokenizer st = new StringTokenizer(br.readLine());
            int[] a = new int[n];
            int L = MAXA, M = 2;
            for (int i = 0; i < n; i++) {
                a[i] = Integer.parseInt(st.nextToken());
                if (a[i] < L) L = a[i];
                if (a[i] > M) M = a[i];
                isTarget[a[i]] = true;  // mark which are our ai
            }

            // 2) Backward-BFS from L to build the candidate set
            Queue<Integer> q = new ArrayDeque<>();
            ArrayList<Integer> preList = new ArrayList<>();
            q.add(L);
            visPre[L] = true;
            preList.add(L);

            while (!q.isEmpty()) {
                int y = q.poll();
                for (int d : divisors[y]) {
                    int xCand = y - d;
                    if (xCand >= 2 && !visPre[xCand]) {
                        visPre[xCand] = true;
                        preList.add(xCand);
                        q.add(xCand);
                    }
                }
            }

            // We have a backward-reachable set of L in preList
            // Clear visPre for the next test reuse
            for (int v : preList) {
                visPre[v] = false;
            }

            // Sort candidates descending so that large x get tried first
            Collections.sort(preList, Collections.reverseOrder());

            int answer = -1;

            // We will need to quickly check how many of the a[i] we visit
            // in the forward BFS, so we use counted[] and reset it after each attempt
            ArrayList<Integer> countedList = new ArrayList<>();

            outer:
            for (int x : preList) {
                // Forward BFS from x, we only push v+d <= M
                Queue<Integer> qf = new ArrayDeque<>();
                ArrayList<Integer> visList = new ArrayList<>();
                qf.add(x);
                visF[x] = true;
                visList.add(x);

                int found = 0;

                if (isTarget[x]) {
                    counted[x] = true;
                    countedList.add(x);
                    found++;
                }

                while (!qf.isEmpty() && found < n) {
                    int u = qf.poll();
                    for (int d : divisors[u]) {
                        int v = u + d;
                        if (v <= M && !visF[v]) {
                            visF[v] = true;
                            visList.add(v);
                            if (isTarget[v] && !counted[v]) {
                                counted[v] = true;
                                countedList.add(v);
                                found++;
                                if (found == n) {
                                    answer = x;
                                    break;
                                }
                            }
                            qf.add(v);
                        }
                    }
                    if (answer != -1) break;
                }

                // clear visF
                for (int v2 : visList) {
                    visF[v2] = false;
                }
                // clear counted
                for (int v2 : countedList) {
                    counted[v2] = false;
                }
                countedList.clear();

                if (answer != -1) {
                    break outer;
                }
            }

            out.println(answer);

            // clear isTarget for next test
            for (int v : a) {
                isTarget[v] = false;
            }
        }

        out.flush();
    }
}