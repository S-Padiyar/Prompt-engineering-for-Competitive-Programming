import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        FastInput in = new FastInput();
        FastOutput out = new FastOutput();
        int n = in.nextInt(), Q = in.nextInt();
        int[] A = new int[n];
        for (int i = 0; i < n; i++) A[i] = in.nextInt();
        final int TH = 64;
        int[] tmp = new int[TH];
        while (Q-- > 0) {
            int l = in.nextInt() - 1, r = in.nextInt() - 1;
            int len = r - l + 1;
            if (len > TH) {
                out.println("YES");
                continue;
            }
            for (int i = 0; i < len; i++) tmp[i] = A[l + i];
            Arrays.sort(tmp, 0, len);
            ArrayList<Long> masks = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                for (int j = i + 1; j < len; j++) {
                    long sum = (long) tmp[i] + tmp[j];
                    for (int k = j + 1; k < len && tmp[k] < sum; k++) {
                        masks.add((1L << i) | (1L << j) | (1L << k));
                    }
                }
            }
            boolean ok = false;
            int M = masks.size();
            for (int i = 0; i < M && !ok; i++) {
                long m1 = masks.get(i);
                for (int j = i + 1; j < M; j++) {
                    if ((m1 & masks.get(j)) == 0) {
                        out.println("YES");
                        ok = true;
                        break;
                    }
                }
            }
            if (!ok) out.println("NO");
        }
        out.flush();
    }

    static class FastInput {
        BufferedInputStream in = new BufferedInputStream(System.in);
        byte[] buf = new byte[1 << 20];
        int pos, len;
        int read() throws IOException {
            if (pos == len) {
                pos = 0;
                len = in.read(buf);
                if (len < 0) return -1;
            }
            return buf[pos++] & 0xFF;
        }
        int nextInt() throws IOException {
            int c, x = 0;
            do { c = read(); if (c < 0) return -1; }
            while (c != '-' && (c < '0' || c > '9'));
            boolean neg = (c == '-');
            if (neg) c = read();
            for (; c >= '0' && c <= '9'; c = read()) x = x * 10 + (c - '0');
            return neg ? -x : x;
        }
    }

    static class FastOutput {
        BufferedOutputStream out = new BufferedOutputStream(System.out);
        void println(String s) throws IOException {
            out.write(s.getBytes());
            out.write('\n');
        }
        void flush() throws IOException {
            out.flush();
        }
    }
}