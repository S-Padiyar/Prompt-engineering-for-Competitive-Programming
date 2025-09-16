import java.io.*;
import java.util.*;

public class Main {
    static class DSU {
        int[] p, r;
        DSU(int n) {
            p = new int[n+1];
            r = new int[n+1];
            for(int i = 1; i <= n; i++) {
                p[i] = i;
                r[i] = 0;
            }
        }
        int find(int x) {
            if (p[x] == x) return x;
            return p[x] = find(p[x]);
        }
        boolean union(int a, int b) {
            a = find(a);
            b = find(b);
            if (a == b) return false;
            if (r[a] < r[b]) {
                p[a] = b;
            } else if (r[b] < r[a]) {
                p[b] = a;
            } else {
                p[b] = a;
                r[a]++;
            }
            return true;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(br.readLine().trim());
        StringTokenizer st;

        while (t-- > 0) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());

            DSU dsu = new DSU(n);
            boolean[] involved = new boolean[n+1];

            // Read friendships and union in DSU
            List<int[]> edges = new ArrayList<>(m);
            for (int i = 0; i < m; i++) {
                st = new StringTokenizer(br.readLine());
                int a = Integer.parseInt(st.nextToken());
                int b = Integer.parseInt(st.nextToken());
                dsu.union(a, b);
                involved[a] = true;
                involved[b] = true;
                edges.add(new int[]{a, b});
            }

            // Gather vertices of each DSU component
            List<List<Integer>> comp = new ArrayList<>(n+1);
            for(int i = 0; i <= n; i++) {
                comp.add(new ArrayList<>());
            }
            for (int v = 1; v <= n; v++) {
                if (!involved[v]) continue;
                int r = dsu.find(v);
                comp.get(r).add(v);
            }

            // Build the list of edge-intervals on the cycle we must keep
            List<int[]> intervals = new ArrayList<>();

            for (int root = 1; root <= n; root++) {
                List<Integer> verts = comp.get(root);
                if (verts.size() < 2) continue; // no friendships in this component

                Collections.sort(verts);
                int k = verts.size();

                // Compute the k gaps around the cycle
                int maxGap = -1, maxIdx = -1;
                for (int i = 0; i < k-1; i++) {
                    int gap = verts.get(i+1) - verts.get(i);
                    if (gap > maxGap) {
                        maxGap = gap;
                        maxIdx = i;
                    }
                }
                // wrapping gap from last back to first
                int wrapGap = (verts.get(0) + n) - verts.get(k-1);
                if (wrapGap > maxGap) {
                    maxGap = wrapGap;
                    maxIdx = k-1;
                }

                // The minimal connecting arc is the complement of that largest gap:
                // it starts at verts[(maxIdx+1)%k], ends at verts[maxIdx], length = n - maxGap
                int startVertex = verts.get((maxIdx+1) % k);
                int endVertex   = verts.get(maxIdx);
                int length = (endVertex - startVertex + n) % n;
                if (length == 0) {
                    // Actually for a component of size >=2, length must >=1
                    continue;
                }
                // The edges in that arc are numbered startEdge..(startEdge+length-1) mod n,
                // where edge i is between i and i+1 (and edge n is between n and 1).
                int startEdge = startVertex;
                int endEdge   = (startEdge + length - 1) % n;
                if (endEdge == 0) endEdge = n;

                // If this interval wraps, split it into two
                if (startEdge <= endEdge) {
                    intervals.add(new int[]{startEdge, endEdge});
                } else {
                    intervals.add(new int[]{startEdge, n});
                    intervals.add(new int[]{1, endEdge});
                }
            }

            // Sort all intervals by their left endpoint
            intervals.sort(Comparator.comparingInt(a -> a[0]));

            // Sweep to compute the total union length
            long ans = 0;
            int curL = -1, curR = -1;
            for (int[] iv : intervals) {
                int L = iv[0], R = iv[1];
                if (curR < 0) {
                    // first interval
                    curL = L;
                    curR = R;
                    ans += (R - L + 1);
                } else if (L > curR + 1) {
                    // disjoint
                    curL = L;
                    curR = R;
                    ans += (R - L + 1);
                } else {
                    // overlapping or just touching
                    if (R > curR) {
                        ans += (R - curR);
                        curR = R;
                    }
                }
            }

            System.out.println(ans);
        }
    }
}