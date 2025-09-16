import java.io.*;
import java.util.*;

public class Main {
    static int Ntot;
    static ArrayList<Integer>[] ch;
    static int[] sub;
    static int[] parent;
    static int[] treeSize;          // N for each tree
    static int[] treeOf;            // which tree each node belongs to
    static int[] noCutMask;         // noCutMask[t] = size of tree t
    // we will store for each node v oneCutMask[v] = sub[v] | (treeSize[t] - sub[v])
    static int[] oneCutMask;
    static int nodePtr;             // global index over all nodes
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        
        int tcs = Integer.parseInt(in.readLine().trim());
        Ntot = 0;
        // We will do everything forest‐wide in one big array of size up to 1e6.
        // As we read each test case we fill in parent[], ch[], etc.
        // Finally we do the greedy bit‐by‐bit over all trees in this case.
        // Then print and move on.
        
        StringBuilder sb = new StringBuilder();
        while (tcs-- > 0) {
            int k = Integer.parseInt(in.readLine().trim());
            // first pass to count total nodes
            int sumN = 0;
            int[] ni = new int[k];
            for (int i = 0; i < k; i++) {
                ni[i] = Integer.parseInt(in.readLine().trim());
                sumN += ni[i];
                // skip blank line if present
                if (ni[i]==1) {
                  // nothing to skip
                } else {
                  // next line to be read
                }
            }
            // now build all structures for this case
            parent = new int[sumN+1];
            ch = new ArrayList[sumN+1];
            for (int i = 1; i <= sumN; i++) ch[i] = new ArrayList<>();
            treeOf = new int[sumN+1];
            treeSize = new int[k];
            noCutMask = new int[k];
            oneCutMask = new int[sumN+1];
            
            // read parents and build trees
            int curNode = 1;
            for (int ti = 0; ti < k; ti++) {
                int n = ni[ti];
                treeSize[ti] = n;
                noCutMask[ti] = n;
                
                // root is curNode+0
                parent[curNode] = 0;
                treeOf[curNode] = ti;
                for (int j = 1; j < n; j++) {
                    st = new StringTokenizer(in.readLine());
                    int p = Integer.parseInt(st.nextToken());
                    // global index of p is (curNode-1)+p
                    int gp = (curNode - 1) + p;
                    parent[curNode + j] = gp;
                    treeOf[curNode + j] = ti;
                    ch[gp].add(curNode + j);
                }
                curNode += n;
            }
            
            // compute all subtree‐sizes by one DFS per tree
            sub = new int[sumN+1];
            for (int i = 1; i <= sumN; i++) {
                if (parent[i] == 0) dfsSub(i);
            }
            
            // build oneCutMask[v]
            for (int v = 1; v <= sumN; v++) {
                int t = treeOf[v];
                int N = treeSize[t];
                int sv = sub[v];
                oneCutMask[v] = (sv | (N - sv));
            }
            
            // Now do the bit‐by‐bit greedy:
            // We'll keep for each tree which mask we are “committing to” as soon
            // as we decide a bit, but in reality we just need to know whether
            // a tree has some candidate that covers our current res|bit.
            // So for quick queries we will precompute for each tree its two
            // candidate masks: "noCut" and the **best** oneCut that has all the
            // bits we need so far.  Actually we just need to test existence.
            
            // We will store for each tree an integer "bestMask" which we will
            // update whenever we successfully assign a new bit to that tree.
            // Initially bestMask[t] = noCutMask[t].
            int[] bestMask = new int[k];
            for (int t = 0; t < k; t++) {
                bestMask[t] = noCutMask[t];
            }
            
            int res = 0;
            // for b = 20..0
            for (int b = 20; b >= 0; b--) {
                int want = res | (1 << b);
                // can we find some tree t, and some candidate (either noCutMask[t]
                // or oneCutMask[u] for a u in that tree) that has all bits of 'want'?
                boolean ok = false;
                int whichTree = -1;
                
                // We’ll do two passes.  First check for each tree whether its
                // *current* bestMask[t] already covers 'want'.  If so we take it.
                for (int t = 0; t < k; t++) {
                    if ((bestMask[t] & want) == want) {
                        ok = true;
                        whichTree = t;
                        break;
                    }
                }
                
                if (!ok) {
                    // If no tree's current bestMask covers 'want' out of the box,
                    // we try upgrading one tree by seeing if ANY single‐cut
                    // mask in that tree covers 'want'.
                    // We just scan all nodes once per tree (sum is O(N)) but
                    // we break early if we succeed.
                    
                    for (int t = 0; t < k && !ok; t++) {
                        int N = treeSize[t];
                        int offset = 0; // we need the starting global‐index of tree t
                        // find it by earlier prefix sums or store it.  For simplicity,
                        // we can store "start[t]" when reading, but assume we have it.
                        // Then scan global nodes v= start[t]..start[t]+N-1
                        // here I'll just loop v=1..Ntot but check treeOf[v]==t
                        for (int v = 1; v <= sumN; v++) {
                            if (treeOf[v] != t) continue;
                            int om = oneCutMask[v];
                            if ((om & want) == want) {
                                // we can upgrade tree t's bestMask to om
                                bestMask[t] = om;
                                ok = true;
                                whichTree = t;
                                break;
                            }
                        }
                    }
                }
                
                if (ok) {
                    // commit this bit
                    res |= (1 << b);
                }
            }
            
            sb.append(res).append('\n');
        }
        
        System.out.print(sb);
    }
    
    // compute sub[v] by DFS
    static int dfsSub(int v) {
        int s = 1;
        for (int c : ch[v]) {
            s += dfsSub(c);
        }
        sub[v] = s;
        return s;
    }
}