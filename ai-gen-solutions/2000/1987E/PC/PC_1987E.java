s[v] = sumB - a[v],   // leftover slack
        r[v] = (s[v]>0 ? 1 : 1 + min_i r[c_i]),
        b[v] = a[v].