import numpy as np
import scipy.sparse as sp
from sklearn.metrics.pairwise import euclidean_distances
from sklearn.utils.extmath import stable_cumsum,row_norms
import sys

##  Performs the same initialization as scikit-learn does for a standard 
##  Gaussian Mixture. Uses a K-means clustering as the initial setup of the 
##  centroids. Outputs the row of the data that should be used as the starting 
##  centroids of the Gaussian Mixture method

if len(sys.argv) < 3:
    print('python3 GaussianRandom.py [seed] [n_clusters]')
else:
    seed = int(sys.argv[1])
    random_state = np.random.RandomState(seed)
    n_clusters = int(sys.argv[2])

    X = np.genfromtxt('matrix_for_python.csv', delimiter=',')
    x_squared_norms = row_norms(X, squared=True)
    n_samples, n_features = X.shape
    first = random_state.randint(n_samples)

    centers = np.empty((n_clusters, n_features), dtype=X.dtype)
    centers_row = np.empty((n_clusters,1), dtype=np.int32)
    
    n_local_trials = 2 + int(np.log(n_clusters))

    center_id = random_state.randint(n_samples)
    if sp.issparse(X):
        centers[0] = X[center_id].toarray()
        centers_row[0] = center_id
    else:
        centers[0] = X[center_id]
        centers_row[0] = center_id

    closest_dist_sq = euclidean_distances(
            centers[0, np.newaxis], X, Y_norm_squared=x_squared_norms,
            squared=True)
    current_pot = closest_dist_sq.sum()

    for c in range(1, n_clusters):
        rand_vals = random_state.random_sample(n_local_trials) * current_pot
        candidate_ids = np.searchsorted(stable_cumsum(closest_dist_sq),
                                        rand_vals)

        distance_to_candidates = euclidean_distances(
            X[candidate_ids], X, Y_norm_squared=x_squared_norms, squared=True)

        # Decide which candidate is the best
        best_candidate = None
        best_pot = None
        best_dist_sq = None
        for trial in range(n_local_trials):
            # Compute potential when including center candidate
            new_dist_sq = np.minimum(closest_dist_sq,
                                     distance_to_candidates[trial])
            new_pot = new_dist_sq.sum()

            # Store result if it is the best local trial so far
            if (best_candidate is None) or (new_pot < best_pot):
                best_candidate = candidate_ids[trial]
                best_pot = new_pot
                best_dist_sq = new_dist_sq

        # Permanently add best center candidate found in local tries
        if sp.issparse(X):
            centers_row[c] = best_candidate
            centers[c] = X[best_candidate].toarray()
        else:
            centers_row[c] = best_candidate
            centers[c] = X[best_candidate]
            
        current_pot = best_pot
        closest_dist_sq = best_dist_sq
    np.savetxt('random_initial_values_for_GM.csv', centers_row, fmt='%1d', delimiter=',')
