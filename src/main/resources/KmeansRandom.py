import numpy as np
import sys

##  Provides the same initialization for K-means as scikit-learn does. Outputs 
##  the rows from the initial data to use as the starting centroids.

if len(sys.argv) < 4:
    print('python3 KmeansRandom.py [RandomState seed] [n_samples] [n_clusters] [n_init = 10]')
else:
    seed = int(sys.argv[1])
    n_samples = int(sys.argv[2])
    n_clusters = int(sys.argv[3])
    if len(sys.argv) == 5:
        n_init = int(sys.argv[4])
    else:
        n_init = 10
    rs = np.random.RandomState(seed)
    matrix = (rs.permutation(n_samples)[:n_clusters]).reshape(1,n_clusters)
    for i in range(n_init - 1):
        matrix = np.append(matrix, rs.permutation(n_samples)[:n_clusters].reshape(1,n_clusters), axis = 0)
    np.savetxt('random_initial_row_for_kmeans.csv', matrix, fmt='%1d',delimiter=',')
