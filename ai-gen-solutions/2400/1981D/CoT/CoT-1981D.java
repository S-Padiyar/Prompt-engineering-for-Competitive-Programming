import java.io.*;
import java.util.*;

public class Main {
    static int K;               // alphabet size
    static int DBN = 2;         // de Bruijn order = 2
    static int[] a;             // work array for de Bruijn
    static ArrayList<Integer> seq;  // holds the de Bruijn sequence (0..K-1)

    // Hierholzer‐style recursive de Bruijn construction for order=2
    static void db(int t, int p) {
        if (t > DBN) {
            if (DBN % p == 0) {
                // output a[1..p]
                for (int i = 1; i <= p; i++) {
                    seq.add(a[i]);
                }
            }
        } else {
            a[t] = a[t - p];
            db(t + 1, p);
            for (int j = a[t - p] + 1; j < K; j++) {
                a[t] = j;
                db(t + 1, t);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));

        int t = Integer.parseInt(in.readLine().trim());
        int[] ns = new int[t];
        int maxN = 0;
        for (int i = 0; i < t; i++) {
            ns[i] = Integer.parseInt(in.readLine().trim());
            if (ns[i] > maxN) maxN = ns[i];
        }

        // Choose K = ceil(sqrt(maxN)) so that K^2 >= maxN
        K = (int)Math.floor(Math.sqrt(maxN));
        if ((long)K * K < maxN) {
            K++;
        }

        // Build de Bruijn( K, order=2 )
        // The resulting seq.size() == K^2
        a = new int[K * DBN + 1];  // need indices up to a[DBN], a[0]
        seq = new ArrayList<>(K * K);
        db(1, 1);

        // seq now length K^2; we will use seq[0..n-1], shifting each by +1
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < t; i++) {
            int n = ns[i];
            sb.setLength(0);
            for (int j = 0; j < n; j++) {
                // +1 to make symbols in [1..K] instead of [0..K-1]
                sb.append(seq.get(j) + 1);
                if (j + 1 < n) sb.append(' ');
            }
            out.println(sb);
        }

        out.flush();
    }
}