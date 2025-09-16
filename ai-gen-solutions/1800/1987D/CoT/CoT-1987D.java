import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(in.readLine().trim());
        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
            int n = Integer.parseInt(in.readLine().trim());
            StringTokenizer st = new StringTokenizer(in.readLine());
            List<Integer> v = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                v.add(Integer.parseInt(st.nextToken()));
            }
            Collections.sort(v);

            int aliceCount = 0;
            int last = 0;  // Alice's current maximum eaten

            while (true) {
                // Alice's turn: pick the smallest > last
                int pickIdx = -1;
                for (int i = 0; i < v.size(); i++) {
                    if (v.get(i) > last) {
                        pickIdx = i;
                        break;
                    }
                }
                if (pickIdx == -1) {
                    // Alice cannot move
                    break;
                }
                last = v.remove(pickIdx);
                aliceCount++;

                // Bob's turn: delete the largest > last
                int delIdx = -1;
                for (int i = v.size() - 1; i >= 0; i--) {
                    if (v.get(i) > last) {
                        delIdx = i;
                        break;
                    }
                }
                if (delIdx == -1) {
                    // Bob cannot move, game ends immediately
                    break;
                }
                v.remove(delIdx);
                // Note: Bob does not change 'last'
            }

            sb.append(aliceCount).append("\n");
        }
        System.out.print(sb);
    }
}