This total work sums over all components of \(|R_i|\times|C_i|\), which in the worst case is still \(O(nm)\) or a few times \(nm\).  Since \(nm\le10^6\), it runs in time.

Overall time per test is
     – one DSU pass: \(O(nm)\).  
     – computing \(s_r,s_c,\text{dotRow},\text{dotCol}\): \(O(nm)\).  
     – sorting the raw “row‐pairs” and “col‐pairs”: each of size up to \(3\cdot(\#\text{black cells})\le3nm\), so \(O(nm\log(nm))\).  
     – filling the cross array by “per‐component double loops” \(\sum_i|R_i|\cdot|C_i|\), again \(O(nm)\).  
     – final scan of all \((r,c)\) in \(O(nm)\).  

Since \(\sum nm\le10^6\) over all test cases, this comfortably fits in 2 s in Java.

---