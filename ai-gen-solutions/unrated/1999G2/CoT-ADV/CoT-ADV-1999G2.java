import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int t = sc.nextInt();            // Number of test cases
        for (int i = 0; i < t; i++) {
            int x = sc.nextInt();        // The missing number on the ruler
            System.out.println(x);       // Echo it back as the answer
        }
        sc.close();
    }
}