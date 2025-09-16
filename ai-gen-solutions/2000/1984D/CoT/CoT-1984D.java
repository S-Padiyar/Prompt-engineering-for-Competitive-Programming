import java.io.*;
import java.util.*;

public class Main {
    static final long HASH_BASE = 1315423911L;
    static final int MAXN = 300_000 + 5;
    // For rolling hash
    static long[] powBase = new long[MAXN];

    public static void main(String[] args) throws IOException {
        // Precompute powers of HASH_BASE
        powBase[0] = 1;
        for (int i = 1; i < MAXN; i++) {
            powBase[i] = powBase[i - 1] * HASH_BASE;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int T = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();
        while (T-- > 0) {
            String s = br.readLine().trim();
            int n = s.length();

            // 1) Collect positions of non-'a'
            ArrayList<Integer> p = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if (s.charAt(i) != 'a') {
                    p.add(i);
                }
            }
            int K = p.size();
            // If all 'a', answer = n-1
            if (K == 0) {
                sb.append(n - 1).append('\n');
                continue;
            }

            // Build rolling hash
            long[] h = new long[n];
            h[0] = (s.charAt(0) - 'a' + 1);
            for (int i = 1; i < n; i++) {
                h[i] = h[i - 1] * HASH_BASE + (s.charAt(i) - 'a' + 1);
            }
            // substring hash of s[l..r] inclusive
            // H(l,r) = h[r] - h[l-1]*powBase[r-l+1]
            class HashUtil {
                long get(int l, int r) {
                    long res = h[r];
                    if (l > 0) res -= h[l - 1] * powBase[r - l + 1];
                    return res;
                }
            }
            HashUtil HU = new HashUtil();

            // Case A: many small t's
            long caseA = 0;
            if (K >= 2) {
                // M = min gap between consecutive p's
                int M = n;
                for (int i = 1; i < K; i++) {
                    M = Math.min(M, p.get(i) - p.get(i - 1));
                }
                // Compute minimal LCP between suffix at p[0] and at p[i]
                int L = n;  // large
                for (int i = 1; i < K; i++) {
                    int x = p.get(0), y = p.get(i);
                    // binary search lcp
                    int low = 0, high = Math.min(n - x, n - y);
                    while (low < high) {
                        int mid = (low + high + 1) >>> 1;
                        if (HU.get(x, x + mid - 1) == HU.get(y, y + mid - 1)) {
                            low = mid;
                        } else {
                            high = mid - 1;
                        }
                    }
                    L = Math.min(L, low);
                }
                // also cannot exceed n - p[K-1]
                int maxLen2 = n - p.get(K - 1);
                int up = Math.min(Math.min(M, L), maxLen2);
                if (up > 0) caseA = up;
            }
            // Case B: one big t covering [l..r]
            int l = p.get(0), rpos = p.get(K - 1);
            int D = rpos - l + 1;

            // Build suffix‐automaton
            SuffixAutomaton sam = new SuffixAutomaton(n * 2);
            for (int i = 0; i < n; i++) {
                sam.extend(s.charAt(i) - 'a', i);
            }
            sam.propagateMaxPos();

            long caseB = 0;
            for (int v = 1; v < sam.size; v++) {
                if (sam.mxPos[v] >= rpos) {
                    int linkLen = sam.linkOf(v) >= 0 ? sam.lenOf(sam.linkOf(v)) : 0;
                    int lb = Math.max(Math.max(linkLen + 1, D),
                                      sam.mxPos[v] - l + 1);
                    int rb = sam.lenOf(v);
                    if (lb <= rb) {
                        caseB += (rb - lb + 1);
                    }
                }
            }

            sb.append(caseA + caseB).append('\n');
        }

        System.out.print(sb);
    }

    // --- Suffix Automaton with max‐endpos propagation ---
    static class SuffixAutomaton {
        static class State {
            int len, link;
            int[] next = new int[26];
            int maxPos;  // for propagating the maximum end‐position
            State() {
                len = 0; link = -1;
                Arrays.fill(next, -1);
                maxPos = -1;
            }
        }

        State[] st;
        int size, last;

        SuffixAutomaton(int maxStates) {
            st = new State[maxStates];
            for (int i = 0; i < maxStates; i++) st[i] = new State();
            st[0].len = 0; st[0].link = -1;
            size = 1; last = 0;
        }

        void extend(int c, int pos) {
            int cur = size++;
            st[cur].len = st[last].len + 1;
            st[cur].maxPos = pos;
            int p = last;
            for (; p != -1 && st[p].next[c] == -1; p = st[p].link) {
                st[p].next[c] = cur;
            }
            if (p == -1) {
                st[cur].link = 0;
            } else {
                int q = st[p].next[c];
                if (st[p].len + 1 == st[q].len) {
                    st[cur].link = q;
                } else {
                    int clone = size++;
                    st[clone].len = st[p].len + 1;
                    st[clone].link = st[q].link;
                    System.arraycopy(st[q].next, 0, st[clone].next, 0, 26);
                    st[clone].maxPos = st[q].maxPos;
                    for (; p != -1 && st[p].next[c] == q; p = st[p].link) {
                        st[p].next[c] = clone;
                    }
                    st[q].link = st[cur].link = clone;
                }
            }
            last = cur;
        }

        // After building, propagate each state's maxPos up the link‐tree
        void propagateMaxPos() {
            // Bucket states by length
            int maxLen = 0;
            for (int i = 0; i < size; i++) {
                maxLen = Math.max(maxLen, st[i].len);
            }
            int[] cnt = new int[maxLen + 1];
            for (int i = 0; i < size; i++) cnt[st[i].len]++;
            for (int i = 1; i <= maxLen; i++) cnt[i] += cnt[i - 1];
            int[] order = new int[size];
            for (int i = size - 1; i >= 0; i--) {
                order[--cnt[st[i].len]] = i;
            }
            // Propagate in decreasing order of len
            for (int i = size - 1; i > 0; i--) {
                int v = order[i];
                int p = st[v].link;
                if (p >= 0) {
                    st[p].maxPos = Math.max(st[p].maxPos, st[v].maxPos);
                }
            }
        }

        int lenOf(int v) { return st[v].len; }
        int linkOf(int v) { return st[v].link; }
        int getMaxPos(int v) { return st[v].maxPos; }
    }
}