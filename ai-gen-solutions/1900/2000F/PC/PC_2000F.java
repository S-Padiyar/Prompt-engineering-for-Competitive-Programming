ndp[0..k] = +∞
 for j in [0..k]:
   for p in [0..k]:
     new_points = min(k, j + p);
     ndp[new_points] = min(ndp[new_points], dp[j] + rect_dp[p]);
 dp = ndp