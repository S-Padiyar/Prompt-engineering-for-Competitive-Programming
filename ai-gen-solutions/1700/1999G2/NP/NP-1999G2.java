import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class Main {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        StringTokenizer st;
        
        int t = Integer.parseInt(br.readLine().trim());
        for (int i = 0; i < t; i++) {
            // Read the missing number x
            int x = Integer.parseInt(br.readLine().trim());
            // Directly output it
            out.println(x);
        }
        
        out.flush();
    }
}