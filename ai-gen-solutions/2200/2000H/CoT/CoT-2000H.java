import java.io.*;
import java.util.*;

public class Main {
  static final int MAXX = 2000000;
  static final int SENTINEL = MAXX + 1;
  static Random rnd = new Random();

  // Treap node
  static class Node {
    int key;     // gap start
    int val;     // gap length
    int mx;      // subtree maximum of val
    int pri;     // heap priority
    Node left, right;
    Node(int k, int v){
      key = k;
      val = v;
      mx = v;
      pri = rnd.nextInt();
    }
  }

  // recalc mx from children
  static void update(Node t){
    if(t == null) return;
    t.mx = t.val;
    if(t.left != null && t.left.mx > t.mx) t.mx = t.left.mx;
    if(t.right!= null && t.right.mx > t.mx) t.mx = t.right.mx;
  }

  // split treap t into [< key] and [>= key]
  static Node[] split(Node t, int key){
    if(t == null) return new Node[]{null,null};
    if(t.key < key){
      Node[] sr = split(t.right, key);
      t.right = sr[0];
      update(t);
      return new Node[]{t, sr[1]};
    } else {
      Node[] sl = split(t.left, key);
      t.left = sl[1];
      update(t);
      return new Node[]{sl[0], t};
    }
  }

  // merge two treaps a (all keys < keys in b) and b
  static Node merge(Node a, Node b){
    if(a == null) return b;
    if(b == null) return a;
    if(a.pri > b.pri){
      a.right = merge(a.right, b);
      update(a);
      return a;
    } else {
      b.left = merge(a, b.left);
      update(b);
      return b;
    }
  }

  // insert node nd (with unique key) into treap t
  static Node insert(Node t, Node nd){
    if(t == null) return nd;
    if(nd.pri > t.pri){
      Node[] sp = split(t, nd.key);
      nd.left = sp[0];
      nd.right = sp[1];
      update(nd);
      return nd;
    }
    else if(nd.key < t.key){
      t.left = insert(t.left, nd);
      update(t);
      return t;
    } else {
      t.right = insert(t.right, nd);
      update(t);
      return t;
    }
  }

  // erase the node with exactly key from t (it must exist)
  static Node erase(Node t, int key){
    // split into <key and >=key
    Node[] a = split(t, key);
    // split the >=key into =key and >key
    Node[] b = split(a[1], key+1);
    // drop b[0], which is the single-node treap with key
    return merge(a[0], b[1]);
  }

  // find the minimum key in t whose val >= k, or return -1 if none
  static int findFirst(Node t, int k){
    if(t == null || t.mx < k) return -1;
    if(t.left != null && t.left.mx >= k){
      return findFirst(t.left, k);
    }
    if(t.val >= k) {
      return t.key;
    }
    return findFirst(t.right, k);
  }

  public static void main(String[] args) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    StringBuilder out = new StringBuilder();
    int T = Integer.parseInt(in.readLine().trim());
    while(T-- > 0){
      int n = Integer.parseInt(in.readLine().trim());
      StringTokenizer st = new StringTokenizer(in.readLine());
      TreeSet<Integer> S = new TreeSet<>();
      // sentinel 0 and MAXX+1
      S.add(0);
      S.add(SENTINEL);
      for(int i=0;i<n;i++){
        S.add(Integer.parseInt(st.nextToken()));
      }
      // build initial treap of gaps
      Node root = null;
      {
        int prev = -1;
        for(int x : S){
          if(prev != -1){
            int L = x - prev - 1;
            if(L > 0){
              int d = prev + 1;
              root = insert(root, new Node(d, L));
            }
          }
          prev = x;
        }
      }
      int m = Integer.parseInt(in.readLine().trim());
      for(int i=0;i<m;i++){
        String line = in.readLine();
        char type = line.charAt(0);
        int v = Integer.parseInt(line.substring(2).trim());
        if(type == '+'){
          // insert v into S
          int prev = S.lower(v);
          int next = S.higher(v);
          // remove old gap (prev, next)
          int oldLen = next - prev - 1;
          if(oldLen > 0){
            root = erase(root, prev+1);
          }
          // add gap (prev, v)
          int L1 = v - prev - 1;
          if(L1 > 0){
            root = insert(root, new Node(prev+1, L1));
          }
          // add gap (v, next)
          int L2 = next - v - 1;
          if(L2 > 0){
            root = insert(root, new Node(v+1, L2));
          }
          S.add(v);
        }
        else if(type == '-'){
          // remove v from S
          int prev = S.lower(v);
          int next = S.higher(v);
          // remove gaps (prev,v) and (v,next)
          int L1 = v - prev - 1;
          if(L1 > 0){
            root = erase(root, prev+1);
          }
          int L2 = next - v - 1;
          if(L2 > 0){
            root = erase(root, v+1);
          }
          // add gap (prev,next)
          int L = next - prev - 1;
          if(L > 0){
            root = insert(root, new Node(prev+1, L));
          }
          S.remove(v);
        }
        else {
          // query "? v" : find minimal d so that [d..d+v-1] is all missing
          int ans = findFirst(root, v);
          if(ans < 0) ans = SENTINEL;
          out.append(ans).append('\n');
        }
      }
    }
    System.out.print(out);
  }
}