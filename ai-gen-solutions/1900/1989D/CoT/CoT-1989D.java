import java.io.*;
import java.util.*;

public class Main {
  public static void main(String[] args) throws IOException {
    FastReader in = new FastReader();
    int n = in.nextInt(), m = in.nextInt();
    int[] a = new int[n];
    for(int i = 0; i < n; i++) a[i] = in.nextInt();
    int[] b = new int[n];
    for(int i = 0; i < n; i++) b[i] = in.nextInt();
    // find the class0 with minimal d0 = a[i]-b[i], tie break on smaller a[i]
    long d0 = Long.MAX_VALUE;
    int idx0 = -1;
    for(int i = 0; i < n; i++) {
      long d = (long)a[i] - b[i];
      if (d < d0 || (d == d0 && a[i] < a[idx0])) {
        d0 = d;
        idx0 = i;
      }
    }
    // find class1 with minimal a1
    int a1 = Integer.MAX_VALUE;
    for(int i = 0; i < n; i++) {
      if (a[i] < a1) a1 = a[i];
    }

    // We'll read all c[j] but we also need to know the largest rem ≡ (c - k*d0) < a[idx0].
    long A0 = a[idx0];
    long MAX_REM = A0 - 1; 
    if (MAX_REM < 0) MAX_REM = 0;

    // We'll collect all other classes i!=idx0 that CAN fit in rem: i.e. either d[i]<=MAX_REM or a[i]<=MAX_REM
    // and build a one‐time 0/1 DP up to capacity=MAX_REM
    int cap = (int)MAX_REM;
    List<Item> items = new ArrayList<>();
    for(int i = 0; i < n; i++){
      if (i == idx0) continue;
      int di = a[i] - b[i];
      if (di <= cap) items.add(new Item(di,2));
      if (a[i] <= cap) items.add(new Item(a[i],1));
    }

    // Build the DP[0..cap]
    int[] dp = new int[cap+1];
    // 0/1‐knapsack
    for(Item it : items) {
      int w = it.w, v=it.v;
      for(int x = cap; x >= w; x--){
        int cand = dp[x-w] + v;
        if(cand > dp[x]) dp[x] = cand;
      }
    }

    // now sum up over all metal types
    long answer = 0;
    for(int i = 0; i < m; i++){
      long C = in.nextLong();
      // how many full class0‐cycles can I do?
      long k;
      if (C < A0) {
        k = 0;
      } else {
        // we must always start a cycle with ≥a0 ingots
        k = (C - A0) / d0 + 1;
      }
      long xp = 2 * k;
      long rem = C - k*d0;
      if (rem < 0) rem = 0;
      if (rem <= cap) {
        xp += dp[(int)rem];
      } else {
        // if rem>cap that means d0>cap => no other cycle fits, just do singles
        xp += rem / a1;
      }
      answer += xp;
    }

    System.out.println(answer);
  }

  static class Item {
    int w, v;
    Item(int _w, int _v){ w=_w; v=_v; }
  }

  // fast IO
  static class FastReader {
    BufferedReader br; StringTokenizer st;
    FastReader() { br = new BufferedReader(new InputStreamReader(System.in)); }
    String next() throws IOException {
      while (st==null || !st.hasMoreTokens()) {
        st = new StringTokenizer(br.readLine());
      }
      return st.nextToken();
    }
    int nextInt() throws IOException { return Integer.parseInt(next()); }
    long nextLong() throws IOException { return Long.parseLong(next()); }
  }
}