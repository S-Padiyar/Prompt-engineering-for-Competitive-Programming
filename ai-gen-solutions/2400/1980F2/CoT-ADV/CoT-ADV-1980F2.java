import java.io.*;
import java.util.*;

public class Main {
    static class SegTree {
        int n;
        int[] tree;
        // Build a segment tree (1-based indexing) over data[1..n].
        SegTree(int n) {
            this.n = n;
            tree = new int[4*(n+1)];
        }
        void build(int node, int l, int r, int[] data) {
            if (l == r) {
                tree[node] = data[l];
            } else {
                int mid = (l+r)>>>1;
                build(node<<1, l, mid, data);
                build(node<<1|1, mid+1, r, data);
                tree[node] = Math.max(tree[node<<1], tree[node<<1|1]);
            }
        }
        // find first index in [ql..qr] whose value > x, or return -1
        int queryFirst(int node, int l, int r, int ql, int qr, int x) {
            if (ql>r || qr<l || tree[node] <= x) return -1;
            if (l == r) return l;
            int mid = (l+r)>>>1;
            int res = queryFirst(node<<1, l, mid, ql, qr, x);
            if (res != -1) return res;
            return queryFirst(node<<1|1, mid+1, r, ql, qr, x);
        }
    }

    // Process one test case
    static void solveCase(BufferedReader in, PrintWriter out) throws IOException {
        StringTokenizer st = new StringTokenizer(in.readLine());
        long n = Long.parseLong(st.nextToken());
        long m = Long.parseLong(st.nextToken());
        int k = Integer.parseInt(st.nextToken());

        // Read fountains
        int[] R = new int[k], C = new int[k];
        for (int i = 0; i < k; i++) {
            st = new StringTokenizer(in.readLine());
            R[i] = Integer.parseInt(st.nextToken());
            C[i] = Integer.parseInt(st.nextToken());
        }

        // We will do the "left-start" pass and the "top-start" pass similarly.
        // Returns an object containing:
        //  - bestArea: the maximum region size if we must keep ALL fountains (no removal)
        //  - delta: an array delta[i] = how much region grows if we remove fountain i
        class PassResult {
            long bestArea;
            long[] delta;
        }

        // Build a pass (axis = 0 for columns, 1 for rows)
        PassResult doPass(int axis) {
            // Step 1: group fountains by coordinate (C if axis=0, R if axis=1)
            TreeMap<Integer, ArrayList<Integer>> groups = new TreeMap<>();
            for (int i = 0; i < k; i++) {
                int key = (axis==0 ? C[i] : R[i]);
                int other = (axis==0 ? R[i] : C[i]);
                groups.computeIfAbsent(key, x->new ArrayList<>()).add(other);
            }
            // Extract sorted keys
            int G = groups.size();
            Integer[] keys = groups.keySet().toArray(new Integer[0]);
            // For each group, find the topmost fountain (max other+1) and second topmost
            int[] Lold = new int[G+1], Lnew = new int[G+1];
            // We'll map each fountain to its group index and whether it's "topmost"
            int[] grpIdx = new int[k];
            boolean[] isTopmost = new boolean[k];
            long[] delta = new long[k];

            // Build Lold, Lnew
            for (int gi = 0; gi < G; gi++) {
                ArrayList<Integer> lst = groups.get(keys[gi]);
                Collections.sort(lst, Collections.reverseOrder());
                int max1 = lst.get(0);
                int max2 = (lst.size()>1 ? lst.get(1) : 0);
                Lold[gi+1] = max1+1;     // constraint = maxRow+1
                Lnew[gi+1] = (max2>0 ? max2+1 : 1);
                // Mark which fountain in the input is that topmost
                // We'll do a second pass to label them
            }
            // label each fountain
            {
                // For each group, build a queue of "others", so we can detect topmost by value
                TreeMap<Integer, Queue<Integer>> queueMap = new TreeMap<>();
                for (int gi=0; gi<G; gi++) {
                    ArrayList<Integer> lst = groups.get(keys[gi]);
                    Queue<Integer> q = new LinkedList<>(lst);
                    queueMap.put(keys[gi], q);
                }
                for (int i = 0; i < k; i++) {
                    int key = (axis==0 ? C[i] : R[i]);
                    Queue<Integer> q = queueMap.get(key);
                    // We pop until we match R[i] or C[i]
                    int other = (axis==0 ? R[i] : C[i]);
                    // Since sorted in reverse, the first match is the "topmost"
                    int head;
                    do {
                        head = q.remove();
                        if (head == other) break;
                    } while (true);
                    grpIdx[i] = new ArrayList<>(groups.keySet()).indexOf(key)+1;
                    isTopmost[i] = (head == lstOfThisGroup.get(0));
                    // after that we put it back (so we don't lose it for second-top logic)
                    q.add(head);
                }
            }

            // Build segment widths w[0..G], and prefixW
            long[] w = new long[G+1], prefixW = new long[G+1];
            // segment 0 is columns [1..keys[0]-1]
            w[0] = (keys[0] - 1L);
            for (int i = 1; i < G; i++) {
                w[i] = (keys[i] - keys[i-1]);
            }
            // last segment
            if (axis==0) {
                // columns
                w[G] = (m - keys[G-1] + 1);
            } else {
                // rows
                w[G] = (n - keys[G-1] + 1);
            }
            prefixW[0] = w[0];
            for (int i=1; i<=G; i++) prefixW[i] = prefixW[i-1] + w[i];

            // Build prefix-max F
            int[] F = new int[G+1];
            F[0] = 1; // no fountain forces at segment 0, so it's height=1
            for (int i=1; i<=G; i++) {
                F[i] = Math.max(F[i-1], Lold[i]);
            }
            // Compute totalSum = sum(F[i]*w[i])
            long totalSum = 0;
            for (int i=0; i<=G; i++) {
                totalSum += (long)F[i] * w[i];
            }
            // The best area if we keep ALL fountains:
            long bestArea;
            if (axis==0) {
                // left-start: sum_{c=1..m}(n-f(c)+1) = m*(n+1) - totalSum
                bestArea = m * (n + 1) - totalSum;
            } else {
                // top-start: sum_{r=1..n}(m-g(r)+1) = n*(m+1) - totalSum
                bestArea = n * (m + 1) - totalSum;
            }

            // Build a segment tree on Lold[1..G] so we can find "first index>j where Lold[k]>x"
            SegTree stree = new SegTree(G);
            stree.build(1, 1, G, Lold);

            // Now for each fountain i, if it's the topmost in its column (or row),
            // removing it lowers Lold[j] to Lnew[j], so we recompute the delta.
            for (int i = 0; i < k; i++) {
                int j = grpIdx[i]; 
                if (!isTopmost[i]) {
                    delta[i] = 0; 
                    continue;
                }
                int oldVal = Lold[j], newVal = Lnew[j];
                if (newVal >= oldVal) {
                    delta[i] = 0; 
                    continue;
                }
                // find first p>j with Lold[p]>newVal
                int p = stree.queryFirst(1, 1, G, j+1, G, newVal);
                if (p < 0) p = G+1;
                // sum of segment widths w[j..p-1]:
                long widthSum = prefixW[p-1] - prefixW[j-1];
                // the totalSum is decreased by (oldVal-newVal)*widthSum,
                // so the area = (const - totalSum) is increased by the same
                delta[i] = (long)(oldVal - newVal) * widthSum;
            }

            PassResult pr = new PassResult();
            pr.bestArea = bestArea;
            pr.delta = delta;
            return pr;
        }

        // do left-pass and top-pass
        PassResult leftPass = doPass(0);
        PassResult topPass  = doPass(1);

        // final alpha
        long alpha = Math.max(leftPass.bestArea, topPass.bestArea);

        // for each fountain, compute how much more we get if Bob gives it up
        // newArea_i = max(left+deltaL, top+deltaT)
        // answer_i = newArea_i - alpha
        StringBuilder sb = new StringBuilder();
        sb.append(alpha).append('\n');
        for (int i = 0; i < k; i++) {
            long aL = leftPass.bestArea + leftPass.delta[i];
            long aT = topPass.bestArea  + topPass.delta[i];
            long newA = Math.max(aL, aT);
            sb.append(newA - alpha).append(i+1<k ? ' ' : '\n');
        }
        out.print(sb);
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        int t = Integer.parseInt(in.readLine().trim());
        while (t-- > 0) {
            solveCase(in, out);
        }
        out.flush();
        out.close();
    }
}