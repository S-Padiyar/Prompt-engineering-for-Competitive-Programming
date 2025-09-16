for (j=LOG-1 down to 0) {
  if (parent[cur][j] <= R) {
    acc += fullSum[cur][j]; 
    cur = parent[cur][j];
  }
}