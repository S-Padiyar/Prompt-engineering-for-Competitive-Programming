import java.io.*;
import java.util.*;

public class Main {
  static final int MAXN = 300_000;
  static int n;
  static ArrayList<Integer>[] adj = new ArrayList[MAXN+1];

  // dpDown[u] = f(C(u,parent)), i.e. best leaves in the component at u when we cut u-inputEdge
  // dpUp[u]   = f(C(parent,u)),   i.e. best leaves in the component at parent when we cut parent-u  
  static int[] dpDown = new int[MAXN+1];
  static int[] dpUp   = new int[MAXN+1];
  // deg[u] = original degree of u
  static int[] deg = new int[MAXN+1];
  // sumSub[u] = sum of dpDown[v] for all v children of u in the first root-1 DFS
  static long[] sumSub = new long[MAXN+1];

  public static void main(String[] args) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
    int t = Integer.parseInt(in.readLine());
    while (t-- > 0) {
      n = Integer.parseInt(in.readLine());
      for (int i = 1; i <= n; i++) {
        if (adj[i] == null) adj[i] = new ArrayList<>();
        else           adj[i].clear();
        deg[i] = 0;
      }
      for (int i = 0; i < n - 1; i++) {
        StringTokenizer tk = new StringTokenizer(in.readLine());
        int u = Integer.parseInt(tk.nextToken());
        int v = Integer.parseInt(tk.nextToken());
        adj[u].add(v);
        adj[v].add(u);
        deg[u]++; 
        deg[v]++;
      }
      // 1) run dfs1 from node 1 to compute dpDown[u] for every u
      dfs1(1, 0);

      // 2) run dfs2 from node 1 to compute dpUp[u] for every u
      //    dpUp[1]=0 since the “parent-component” of 1 is empty
      dpUp[1] = 0;
      dfs2(1, 0);

      // 3) now for each node `r` we can write
      //    f(T) if we chose r as the very top‐root = (deg[r]==1?1:0) + sum_{all neigh u} f(C(u,r))
      //                                  = (deg[r]==1?1:0) + sumDownChildren + dpUp[r]
      long ans = 0;
      for (int r = 1; r <= n; r++) {
        long cur = dpUp[r] + sumSub[r];
        if (deg[r] == 1) cur += 1;
        ans = Math.max(ans, cur);
      }
      out.println(ans);
    }
    out.flush();
  }

  // dfs1(u,p) builds dpDown[u] = f(C(u,p))
  // we first compute dpDown[v] for all children v, then
  // sumSub[u] = sum(dpDown[v]) over all children v
  // finally
  //   if u is a leaf in that component (i.e. children_count==0)  dpDown[u]=1;
  //   else                                                    dpDown[u]=sumSub[u];
  // You can show by induction that this is exactly the f(C(u,p)) we want.
  static void dfs1(int u, int p) {
    sumSub[u] = 0;
    for (int v : adj[u]) {
      if (v == p) continue;
      dfs1(v, u);
      sumSub[u] += dpDown[v];
    }
    if (adj[u].size() - (p==0 ? 0 : 1) == 0) {
      // no children => a single node component => when attached to parent it becomes a leaf
      dpDown[u] = 1;
    } else {
      dpDown[u] = (int)sumSub[u];
    }
  }

  // dfs2(u,p) builds dpUp[*] for each child from the parent's dpUp and siblings
  // dpUp[v] = f(C(u,v)) where C(u,v) is the component you get if you cut the edge u-v at u
  // we already know dpUp[u] = f(C(p,u)), now we pass that info down to each v:
  //
  // Let total = dpUp[u] + sum( dpDown[w] for all w children of u ) = sum_{all neighbors x of u} dpSub[x->u]
  // then for each child v we want:
  //    dpUp[v] = f( component at u when we cut u-v ) 
  //            = sum_{all neighbors x of u except x=v} dpSub[x->u]
  //            + bonus if that component has u as leaf
  //
  // So we simply do:
  //    withoutV = dpUp[u] + (sumSub[u] - dpDown[v])
  //    degWithout = deg[u] - 1  (we removed one neighbor v)
  //    bonus = (degWithout == 0 ? 1 : 0)
  //    dpUp[v] = withoutV + bonus
  //
  // It turns out that exactly matches the original f‐recurrence, and gives the correct dpUp[v].
  static void dfs2(int u, int p) {
    long totalAll = dpUp[u] + sumSub[u];
    for (int v : adj[u]) {
      if (v == p) continue;
      // remove v's branch from totalAll
      long withoutV = totalAll - dpDown[v];
      int degWithout = deg[u] - 1; 
      // if after removing v the degree of u in that component is zero => it will become a leaf
      dpUp[v] = (int)( withoutV + (degWithout == 0 ? 1 : 0) );
      dfs2(v, u);
    }
  }
}