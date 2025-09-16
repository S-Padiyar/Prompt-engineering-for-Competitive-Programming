import java.io.*;
import java.util.*;

public class Main {
  static int N = 1000005;
  static int[] spf = new int[N];
  static int[] nxt, ds, sz;
  static boolean[] wrap;
  static int[] a;
  static int n, k;

  // DSU on the cycle of length n
  static void initDSU(int n) {
    ds = new int[n];
    sz = new int[n];
    wrap = new boolean[n];
    for(int i=0;i<n;i++){
      ds[i]=i; 
      sz[i]=1;
      wrap[i] = false;
    }
  }
  static int findp(int x){
    return ds[x]==x?x: (ds[x]=findp(ds[x]));
  }
  static void union(int x,int y){
    x=findp(x); y=findp(y);
    if(x==y) return;
    if(sz[x]<sz[y]) {int t=x; x=y; y=t;}
    ds[y]=x;
    sz[x]+=sz[y];
  }

  public static void main(String[] args) throws IOException {
    // build smallest-prime-factor sieve
    for(int i=2;i<N;i++){
      if(spf[i]==0){
        for(int j=i;j<N;j+=i){
          if(spf[j]==0) spf[j]=i;
        }
      }
    }

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    int T = Integer.parseInt(br.readLine().trim());
    StringBuilder sb = new StringBuilder();
    while(T-->0){
      StringTokenizer st = new StringTokenizer(br.readLine());
      n = Integer.parseInt(st.nextToken());
      k = Integer.parseInt(st.nextToken());
      st = new StringTokenizer(br.readLine());
      a = new int[n];
      for(int i=0;i<n;i++){
        a[i] = Integer.parseInt(st.nextToken());
      }

      // 1) factor each a[i] into distinct primes
      // 2) build a map from prime -> list of diagonals in which it occurs
      HashMap<Integer,ArrayList<Integer>> byPrime = new HashMap<>();
      for(int i=0;i<n;i++){
        int x = a[i];
        HashSet<Integer> used = new HashSet<>();
        while(x>1){
          int p=spf[x];
          if(!used.contains(p)){
            byPrime.computeIfAbsent(p, zp->new ArrayList<>()).add(i);
            used.add(p);
          }
          x/=p;
        }
      }
      
      // 3) We'll do DSU on the cycle of length n
      initDSU(n);

      // for every prime p, we want to union all d's in the same 'block' of
      // the cycle whose circular gaps are <= k
      for(Map.Entry<Integer,ArrayList<Integer>> ent: byPrime.entrySet()){
        ArrayList<Integer> L = ent.getValue();
        Collections.sort(L);
        int m = L.size();
        if(m<=1) {
          // single diagonal – we mark it as "needs at least two pieces if wrapless"
          wrap[L.get(0)] = true;
          continue;
        }
        // look at the gaps on the cycle
        int prev = L.get(0);
        // always tie everyone in this prime-list into one DSU‐block
        for(int j=1;j<m;j++){
          int cur=L.get(j);
          int diff = cur - prev;
          if(diff<0) diff+=n;
          if(diff<=k) {
            union(prev, cur);
          }
          prev = cur;
        }
        // also the wrap‐around gap
        {
          int first = L.get(0), last=L.get(m-1);
          int diff = first + n - last;
          if(diff<=k){
            union(first,last);
          }
        }
      }

      // 4) now each DSU‐component on {0,...,n−1} is one "stripe‐block"
      //    but each stripe‐block might break further if it contains a
      //    diagonal d with no prime (i.e. a[d]==1), or if it has exactly
      //    one diagonal and k<n (then that diagonal internally is two pieces).
      //
      // We'll count for each leader how many 'required pieces' it has.
      HashMap<Integer,Integer> compCnt = new HashMap<>();
      for(int d=0;d<n;d++){
        int r = findp(d);
        compCnt.put(r, compCnt.getOrDefault(r,0) + 1);
      }
      // now for each leader, base count is 1 piece. Then we add:
      //   +1 if ANY diagonal in that component had a==1 (each such cell is solo)
      //   +1 more if the component size was exactly 1 AND wrap[d]==true AND k<n
      // because that single stripe splits into two if k<n.

      // But we actually need to count cells with a[d]==1 specially:
      // each such diag contributes n isolated singletons.
      long ans = 0;
      for(int d=0; d<n; d++){
        if(a[d]==1){
          ans += n;          // all its n positions are solo
        }
      }
      // any stripe whose gcd-block has >1 diagonals or a[d]>1
      // will contribute at least 1 big piece.
      boolean[] seenLead = new boolean[n];
      for(int d=0; d<n; d++){
        if(a[d]>1){
          int r = findp(d);
          if(!seenLead[r]){
            seenLead[r] = true;
            int size = compCnt.get(r);
            if(size>1){
              // a big 'multi-diagonal' component always becomes exactly 1 piece
              ans += 1L;
            } else {
              // size==1
              // single diagonal but a[d]>1
              // it is either 1 piece if k>=n (so wraps) or otherwise 2 pieces
              if(k < n && wrap[d]) {
                ans += 2L;
              } else {
                ans += 1L;
              }
            }
          }
        }
      }

      sb.append(ans).append('\n');
    }

    System.out.print(sb);
  }
}