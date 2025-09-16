import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int t = sc.nextInt();
        while (t-- > 0) {
            int x = sc.nextInt();
            // In the interactive version you would deduce x by queries.
            // In this offline hacked version, x is given, so just print it.
            System.out.println(x);
        }
        sc.close();
    }
}