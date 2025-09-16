import java.io.*;
import java.util.*;

public class Main {
    static class Pair {
        int coord, idx;
        Pair(int c, int i) { coord = c; idx = i; }
    }
    // find first position in list L where L[pos].coord >= value
    static int lowerBound(List<Pair> L, int value) {
        int lo = 0, hi = L.size();
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (L.get(mid).coord < value) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }

    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader();
        PrintWriter out = new PrintWriter(System.out);

        int t = in.nextInt();
        while (t-- > 0) {
            int n = in.nextInt();
            int d = in.nextInt();

            // mapU: u0 -> list of (v, index), to handle "vertical extremes share u"
            HashMap<Integer, List<Pair>> mapU = new HashMap<>();
            // mapV: v0 -> list of (u, index), to handle "horizontal extremes share v"
            HashMap<Integer, List<Pair>> mapV = new HashMap<>();

            int[] U = new int[n];
            int[] V = new int[n];
            for (int i = 0; i < n; i++) {
                int x = in.nextInt();
                int y = in.nextInt();
                int u = x + y;
                int v = x - y;
                U[i] = u;
                V[i] = v;
                mapU.computeIfAbsent(u, z -> new ArrayList<>()).add(new Pair(v, i + 1));
                mapV.computeIfAbsent(v, z -> new ArrayList<>()).add(new Pair(u, i + 1));
            }

            // Sort all the lists
            for (List<Pair> lst : mapU.values()) {
                lst.sort(Comparator.comparingInt(a -> a.coord));
            }
            for (List<Pair> lst : mapV.values()) {
                lst.sort(Comparator.comparingInt(a -> a.coord));
            }

            boolean found = false;

            // ========== Pattern 1: vertical extremes share the same u ==========
            outer1:
            for (Map.Entry<Integer, List<Pair>> e : mapU.entrySet()) {
                if (found) break;
                int u0 = e.getKey();
                List<Pair> list0 = e.getValue();
                int sz0 = list0.size();

                for (int pi = 0; pi < sz0; pi++) {
                    Pair P = list0.get(pi);
                    int vP = P.coord;
                    int idxP = P.idx;

                    // look for Q with v = vP + d in the same list
                    int qpos = lowerBound(list0, vP + d);
                    if (qpos < sz0 && list0.get(qpos).coord == vP + d) {
                        int idxQ = list0.get(qpos).idx;

                        // try the two possible opposite us: u0 + d and u0 - d
                        for (int sign : new int[]{+1, -1}) {
                            int u1 = u0 + sign * d;
                            List<Pair> list1 = mapU.get(u1);
                            if (list1 == null) continue;

                            // we need any R with v in [vP, vP+d]
                            int rpos = lowerBound(list1, vP);
                            if (rpos < list1.size() &&
                                list1.get(rpos).coord <= vP + d) {
                                int idxR = list1.get(rpos).idx;
                                out.printf("%d %d %d\n", idxP, idxQ, idxR);
                                found = true;
                                break outer1;
                            }
                        }
                    }
                }
            }

            // ========== Pattern 2: horizontal extremes share the same v ==========
            if (!found) {
                outer2:
                for (Map.Entry<Integer, List<Pair>> e : mapV.entrySet()) {
                    if (found) break;
                    int v0 = e.getKey();
                    List<Pair> list0 = e.getValue();
                    int sz0 = list0.size();

                    for (int pi = 0; pi < sz0; pi++) {
                        Pair P = list0.get(pi);
                        int uP = P.coord;
                        int idxP = P.idx;

                        // look for Q with u = uP + d in same list
                        int qpos = lowerBound(list0, uP + d);
                        if (qpos < sz0 && list0.get(qpos).coord == uP + d) {
                            int idxQ = list0.get(qpos).idx;

                            // try the two possible opposite vs: v0 + d and v0 - d
                            for (int sign : new int[]{+1, -1}) {
                                int v1 = v0 + sign * d;
                                List<Pair> list1 = mapV.get(v1);
                                if (list1 == null) continue;

                                // we need any R with u in [uP, uP+d]
                                int rpos = lowerBound(list1, uP);
                                if (rpos < list1.size() &&
                                    list1.get(rpos).coord <= uP + d) {
                                    int idxR = list1.get(rpos).idx;
                                    out.printf("%d %d %d\n", idxP, idxQ, idxR);
                                    found = true;
                                    break outer2;
                                }
                            }
                        }
                    }
                }
            }

            if (!found) {
                out.println("0 0 0");
            }
        }

        out.flush();
        out.close();
    }

    // FastReader for speedy input
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
        FastReader() {
            br = new BufferedReader(new InputStreamReader(System.in));
        }
        String next() {
            while (st == null || !st.hasMoreTokens()) {
                try {
                    String line = br.readLine();
                    if (line == null) return null;
                    st = new StringTokenizer(line);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return st.nextToken();
        }
        int nextInt() {
            return Integer.parseInt(next());
        }
    }
}