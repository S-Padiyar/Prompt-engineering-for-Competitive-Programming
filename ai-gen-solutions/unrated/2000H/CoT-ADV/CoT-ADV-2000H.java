import java.io.*;
import java.util.*;

public class Main {
    static final int INF = Integer.MAX_VALUE;
    static Random rand = new Random();

    // A Treap node to store a gap of length `len`, starting at `start`.
    // We order by (len, start), and maintain subtree minimum of `start`.
    static class Node {
        int len, start, mnStart;
        int pr;        // priority
        Node left, right;
        Node(int l, int s) {
            len = l; start = s;
            mnStart = s;
            pr = rand.nextInt();
        }
    }

    // Update mnStart from children and self
    static void pull(Node t) {
        if (t == null) return;
        t.mnStart = t.start;
        if (t.left != null && t.left.mnStart < t.mnStart)
            t.mnStart = t.left.mnStart;
        if (t.right != null && t.right.mnStart < t.mnStart)
            t.mnStart = t.right.mnStart;
    }

    // Compare two keys (l1,s1) vs (l2,s2)
    // return negative if (l1,s1) < (l2,s2), zero if equal, positive if greater
    static int cmp(int l1, int s1, int l2, int s2) {
        if (l1 != l2) return l1 - l2;
        return s1 - s2;
    }

    // Merge two treaps l and r, all keys in l < keys in r
    static Node merge(Node l, Node r) {
        if (l == null) return r;
        if (r == null) return l;
        if (l.pr > r.pr) {
            l.right = merge(l.right, r);
            pull(l);
            return l;
        } else {
            r.left = merge(l, r.left);
            pull(r);
            return r;
        }
    }

    // Insert a new node `it` into treap `t`
    static Node insert(Node t, Node it) {
        if (t == null) return it;
        if (it.pr > t.pr) {
            // split t by it.key
            Node[] sp = split(t, it.len, it.start);
            it.left = sp[0];
            it.right = sp[1];
            pull(it);
            return it;
        } else {
            if (cmp(it.len, it.start, t.len, t.start) < 0) {
                t.left = insert(t.left, it);
            } else {
                t.right = insert(t.right, it);
            }
            pull(t);
            return t;
        }
    }

    // Delete the node with key (len, start) from t
    static Node delete(Node t, int len, int start) {
        if (t == null) return null;
        int c = cmp(len, start, t.len, t.start);
        if (c == 0) {
            // remove this node, merge children
            return merge(t.left, t.right);
        } else if (c < 0) {
            t.left = delete(t.left, len, start);
            pull(t);
            return t;
        } else {
            t.right = delete(t.right, len, start);
            pull(t);
            return t;
        }
    }

    // Split treap t into { keys < (len,start) } and { keys >= (len,start) }
    static Node[] split(Node t, int len, int start) {
        if (t == null) return new Node[]{null, null};
        if (cmp(t.len, t.start, len, start) < 0) {
            // t.key < (len,start): goes to left part
            Node[] sp = split(t.right, len, start);
            t.right = sp[0];
            pull(t);
            sp[0] = t;
            return sp;
        } else {
            // t.key >= (len,start): goes to right part
            Node[] sp = split(t.left, len, start);
            t.left = sp[1];
            pull(t);
            sp[1] = t;
            return sp;
        }
    }

    // Query the Treap for the minimum `start` among nodes with len >= k
    static int query(Node t, int k) {
        if (t == null) return INF;
        if (t.len < k) {
            // discard this node and left subtree
            return query(t.right, k);
        } else {
            // this node qualifies, so do: min(this.start,
            //                                 right.mnStart,
            //                                 query(left, k))
            int ans = t.start;
            if (t.right != null && t.right.mnStart < ans)
                ans = t.right.mnStart;
            int leftAns = query(t.left, k);
            return Math.min(ans, leftAns);
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int t = Integer.parseInt(br.readLine());
        while (t-- > 0) {
            // Read initial set
            int n = Integer.parseInt(br.readLine());
            StringTokenizer st = new StringTokenizer(br.readLine());
            TreeSet<Integer> xs = new TreeSet<>();
            for (int i = 0; i < n; i++) {
                xs.add(Integer.parseInt(st.nextToken()));
            }

            // Build Treap of finite gaps
            Node root = null;
            int prev = 0;  // treat predecessor of first as 0
            for (int x : xs) {
                int len = x - prev - 1;
                if (len >= 1) {
                    // gap [prev+1, x-1]
                    root = insert(root, new Node(len, prev+1));
                }
                prev = x;
            }
            // after last element, we do NOT insert an infinite gap
            // it will be handled in queries

            // Process operations
            int m = Integer.parseInt(br.readLine());
            st = null;
            StringBuilder ansBuf = new StringBuilder();
            for (int i = 0; i < m; i++) {
                if (st == null || !st.hasMoreTokens()) {
                    st = new StringTokenizer(br.readLine());
                }
                String op = st.nextToken();
                if (op.equals("+")) {
                    int x = Integer.parseInt(st.nextToken());
                    // find neighbors
                    Integer p0 = xs.lower(x), s0 = xs.higher(x);
                    int p = (p0 == null ? 0 : p0);
                    // remove old gap [p+1, s0-1] if s0 != null
                    if (s0 != null) {
                        int oldLen = s0 - p - 1;
                        if (oldLen >= 1) {
                            root = delete(root, oldLen, p+1);
                        }
                    }
                    // insert the two new gaps around x
                    int leftLen = x - p - 1;
                    if (leftLen >= 1) {
                        root = insert(root, new Node(leftLen, p+1));
                    }
                    if (s0 != null) {
                        int rightLen = s0 - x - 1;
                        if (rightLen >= 1) {
                            root = insert(root, new Node(rightLen, x+1));
                        }
                    }
                    xs.add(x);

                } else if (op.equals("-")) {
                    int x = Integer.parseInt(st.nextToken());
                    // find neighbors before removal
                    Integer p0 = xs.lower(x), s0 = xs.higher(x);
                    int p = (p0 == null ? 0 : p0);
                    // remove the two gaps adjacent to x
                    int leftLen = x - p - 1;
                    if (leftLen >= 1) {
                        root = delete(root, leftLen, p+1);
                    }
                    if (s0 != null) {
                        int rightLen = s0 - x - 1;
                        if (rightLen >= 1) {
                            root = delete(root, rightLen, x+1);
                        }
                    }
                    // insert the merged gap [p+1, s0-1] (if s0!=null)
                    if (s0 != null) {
                        int newLen = s0 - p - 1;
                        if (newLen >= 1) {
                            root = insert(root, new Node(newLen, p+1));
                        }
                    }
                    xs.remove(x);

                } else { // "?" query
                    int kq = Integer.parseInt(st.nextToken());
                    // first check finite gaps in Treap
                    int best = query(root, kq);
                    if (best < INF) {
                        ansBuf.append(best).append(' ');
                    } else {
                        // no finite gap of length>=k, answer is last+1 (or 1 if empty)
                        if (xs.isEmpty()) {
                            ansBuf.append(1).append(' ');
                        } else {
                            ansBuf.append(xs.last()+1).append(' ');
                        }
                    }
                }
            }
            out.println(ansBuf.toString().trim());
        }

        out.flush();
    }
}