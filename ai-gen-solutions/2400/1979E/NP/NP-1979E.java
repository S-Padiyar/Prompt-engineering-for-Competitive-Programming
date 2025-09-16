import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        int t = Integer.parseInt(in.readLine().trim());

        while (t-- > 0) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(st.nextToken());
            int d = Integer.parseInt(st.nextToken());

            int[] U = new int[n];
            int[] V = new int[n];
            int[] X = new int[n];
            int[] Y = new int[n];

            // Read points, compute (u,v)
            for (int i = 0; i < n; i++) {
                st = new StringTokenizer(in.readLine());
                int x = Integer.parseInt(st.nextToken());
                int y = Integer.parseInt(st.nextToken());
                X[i] = x;
                Y[i] = y;
                U[i] = x + y;
                V[i] = x - y;
            }

            // Build mapU: u -> list of (v, index)
            //      mapV: v -> list of (u, index)
            HashMap<Integer, ArrayList<int[]>> mapU = new HashMap<>();
            HashMap<Integer, ArrayList<int[]>> mapV = new HashMap<>();

            for (int i = 0; i < n; i++) {
                mapU.computeIfAbsent(U[i], k -> new ArrayList<>()).add(new int[]{V[i], i});
                mapV.computeIfAbsent(V[i], k -> new ArrayList<>()).add(new int[]{U[i], i});
            }

            // Sort each bucket by the other coordinate
            for (ArrayList<int[]> lst : mapU.values()) {
                lst.sort(Comparator.comparingInt(a -> a[0]));
            }
            for (ArrayList<int[]> lst : mapV.values()) {
                lst.sort(Comparator.comparingInt(a -> a[0]));
            }

            boolean found = false;

            // 1) Scan vertical lines in (u,v)-space => same u, v differs by d
            outer:
            for (Map.Entry<Integer, ArrayList<int[]>> entry : mapU.entrySet()) {
                int u = entry.getKey();
                ArrayList<int[]> lst = entry.getValue();
                for (int j = 0; j + 1 < lst.size(); j++) {
                    int v1 = lst.get(j)[0], i1 = lst.get(j)[1];
                    int v2 = lst.get(j+1)[0], i2 = lst.get(j+1)[1];
                    if (v2 - v1 == d) {
                        // these two are at Chebyshev distance d
                        // look for any third point at u ± d
                        if (mapU.containsKey(u + d)) {
                            int i3 = mapU.get(u + d).get(0)[1];
                            sb.append((i1+1)).append(' ')
                              .append((i2+1)).append(' ')
                              .append((i3+1)).append('\n');
                            found = true;
                            break outer;
                        }
                        if (mapU.containsKey(u - d)) {
                            int i3 = mapU.get(u - d).get(0)[1];
                            sb.append((i1+1)).append(' ')
                              .append((i2+1)).append(' ')
                              .append((i3+1)).append('\n');
                            found = true;
                            break outer;
                        }
                    }
                }
            }

            // 2) If not found, scan horizontal lines => same v, u differs by d
            if (!found) {
                outer:
                for (Map.Entry<Integer, ArrayList<int[]>> entry : mapV.entrySet()) {
                    int v = entry.getKey();
                    ArrayList<int[]> lst = entry.getValue();
                    for (int j = 0; j + 1 < lst.size(); j++) {
                        int u1 = lst.get(j)[0], i1 = lst.get(j)[1];
                        int u2 = lst.get(j+1)[0], i2 = lst.get(j+1)[1];
                        if (u2 - u1 == d) {
                            // these two are at Chebyshev distance d
                            // look for any third at v ± d
                            if (mapV.containsKey(v + d)) {
                                int i3 = mapV.get(v + d).get(0)[1];
                                sb.append((i1+1)).append(' ')
                                  .append((i2+1)).append(' ')
                                  .append((i3+1)).append('\n');
                                found = true;
                                break outer;
                            }
                            if (mapV.containsKey(v - d)) {
                                int i3 = mapV.get(v - d).get(0)[1];
                                sb.append((i1+1)).append(' ')
                                  .append((i2+1)).append(' ')
                                  .append((i3+1)).append('\n');
                                found = true;
                                break outer;
                            }
                        }
                    }
                }
            }

            // If still not found, no such triangle
            if (!found) {
                sb.append("0 0 0\n");
            }
        }

        System.out.print(sb.toString());
    }
}