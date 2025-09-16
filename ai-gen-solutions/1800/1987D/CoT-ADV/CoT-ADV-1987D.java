import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int t = sc.nextInt();
        while (t-- > 0) {
            int n = sc.nextInt();
            // Build multiset of cake tastiness values
            TreeMap<Integer,Integer> multiset = new TreeMap<>();
            for (int i = 0; i < n; i++) {
                int v = sc.nextInt();
                multiset.put(v, multiset.getOrDefault(v, 0) + 1);
            }
            
            int ans = 0;        // number of cakes Alice eats
            int last = 0;       // Alice's last eaten tastiness
            
            while (true) {
                // Alice's turn: pick smallest key > last
                Integer aliceChoice = multiset.higherKey(last);
                if (aliceChoice == null) {
                    // No suitable cake for Alice -- game ends
                    break;
                }
                // Remove one occurrence of aliceChoice
                decrementCount(multiset, aliceChoice);
                ans++;
                last = aliceChoice;
                
                // Bob's turn: pick largest remaining cake (if any)
                if (multiset.isEmpty()) {
                    // No cake left for Bob, game ends
                    break;
                }
                Integer bobChoice = multiset.lastKey();
                decrementCount(multiset, bobChoice);
                // Bob's pick does not affect 'last'
            }
            
            System.out.println(ans);
        }
        sc.close();
    }
    
    // Helper to decrement a key's count in the multiset, removing if it hits zero
    private static void decrementCount(TreeMap<Integer,Integer> map, int key) {
        int cnt = map.get(key);
        if (cnt == 1) {
            map.remove(key);
        } else {
            map.put(key, cnt - 1);
        }
    }
}