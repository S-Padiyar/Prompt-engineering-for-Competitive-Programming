term_s = Σ_s * (floor(m/2)+1) % MOD * inv[m+1] % MOD
   term_n = Σ_n * ceil(m/2)       % MOD * inv[m]   % MOD   (if m>0)
   E_A    = (term_s + term_n) % MOD
   E_B    = (Σ_s+Σ_n - E_A + MOD) % MOD