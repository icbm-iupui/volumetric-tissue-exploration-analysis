from sklearn import cluster, mixture
from sklearn.manifold import TSNE
import numpy as np
from numpy import random
import sys

#   Validation Script, using scikit-learn in python to validate the algorithms
#                      used from SMILE in Java
#   Given a data file, an analysis technique, seed and any additional parameters
#   necessary for the technique you can perform the technique using scikit-learn
#   as similarly as possible to the algorithm in SMILE.

if len(sys.argv) < 3:
    if len(sys.argv) == 2 and sys.argv[1] == 'help':
        print('"python3 validation_script.py [path/to/data/file] [analysis_technique] [seed]  [...additional arguments for technique]"\n Analysis Techniques are:\n KMEANS, TSNE, GAUSSMIX, WARD, COMPLETE')
    else:
        print('"python3 validation_script.py [path/to/data/file] [analysis_technique] [seed] [...additional arguments for technique]"\n or "python3 validation_script.py help" for help')
else:
    ##Set the seed used for the methods based on initial conditions
    seed = int(sys.argv[3])
    ##seed = np.random.RandomState(seed)
        
    ## Generate matrix of values, X, from the 2nd argument in the command line    
    X = np.genfromtxt(sys.argv[1], delimiter=',')
    
    ##Default format for output
    formatt= '%1d'

    #Choose the proper analyis method to perform, based on the 3rd argument
    tech = sys.argv[2]
    if tech == 'KMEANS':        ## Using the K-means clustering method
        n_clust = int(sys.argv[4])  ## number of clusters to find
        n_iter = int(sys.argv[5])   ## number of iterations to perform
        ## Perform the clustering using the above parameters with the 'full' algorithm
        cluster_id = cluster.KMeans(n_clusters=n_clust, init = 'random',n_init=n_iter, random_state=seed, algorithm='full').fit_predict(X)                    
    elif tech == 'TSNE':        ## Using the TSNE dimensional reduction method
        new_dim = int(sys.argv[4])  ## dimensions of the new space
        perplex= int(sys.argv[5])   ## perplexity
        eta = float(sys.argv[6])    ## learning rate
        n_it = int(sys.argv[7])     ## number of iterations
        ## Perform the dimensionality reduction using the above parameters
        tsne_space_coords = TSNE(n_components=new_dim, perplexity=perplex,learning_rate=eta,
                                 n_iter=n_it, random_state=seed).fit_transform(X)
        formatt='%.10f'             ## update the format to work for coordinates with higher precision
    elif tech == 'GAUSSMIX':    ## Using the Gaussian Mixture clustering method
        n_clust = int(sys.argv[4])  ## number of clusters
        ## Perform the clustering using the above parameters
        cluster_id = mixture.GaussianMixture(n_components=n_clust,
                                             random_state=seed).fit(X).predict(X)
    elif tech == 'WARD':        ## Using the Ward Hierarchical clustering method
        n_clust = int(sys.argv[4])  ## number of clusters
        ## Perform the clustering using the above parameters
        cluster_id = cluster.AgglomerativeClustering(n_clusters=n_clust, linkage='ward').fit_predict(X)
    elif tech == 'COMPLETE':    ## Using the Ward Hierarchical clustering method
        n_clust = int(sys.argv[4])  ## number of clusters
        ## Perform the clustering using the above parameters
        cluster_id = cluster.AgglomerativeClustering(n_clusters=n_clust, linkage='complete').fit_predict(X)
    
    ## naming convention for the output file
    file_name = tech + '_validation_file_for_vtea.csv'
    
    ##save the proper matrix to the filename designated above
    if tech == 'TSNE':
        np.savetxt(file_name, tsne_space_coords, fmt=formatt, delimiter=',' )
    else:
        np.savetxt(file_name, cluster_id, fmt=formatt, delimiter=',' )

        
        
        
