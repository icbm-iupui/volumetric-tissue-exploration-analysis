from sklearn import cluster, mixture
from sklearn.manifold import TSNE
import numpy as np
from numpy import random
import sys

if len(sys.argv) < 3:
    if len(sys.argv) == 2 and sys.argv[1] == 'help':
        print('"python3 validation_script.py [path/to/data/file] [analysis_technique] [seed]  [...additional arguments for technique]"\n Analysis Techniques are:\n KMEANS, TSNE, GAUSSMIX, WARD, COMPLETE')
    else:
        print('"python3 validation_script.py [path/to/data/file] [analysis_technique] [seed] [...additional arguments for technique]"\n or "python3 validation_script.py help" for help')
else:
    ##Set the seed
    seed = int(sys.argv[3])
    ##seed = np.random.RandomState(seed)
        
    ## Generate matrix of values, X, with no header column and no object #    
    X = np.genfromtxt(sys.argv[1], delimiter=',', skip_header=1)
    X = np.delete(X,0,axis=1)

    formatt= '%1d'

    #Choose the proper analyis method to perform
    tech = sys.argv[2]
    if tech == 'KMEANS':
        n_clust = int(sys.argv[4])
        n_iter = int(sys.argv[5])
        cluster_id = cluster.KMeans(n_clusters=n_clust, init = 'random',
                                n_init=n_iter, random_state=seed, algorithm='full').fit_predict(X)
    elif tech == 'TSNE':
        new_dim = int(sys.argv[4])
        perplex= int(sys.argv[5])
        eta = int(sys.argv[6])
        n_it = int(sys.argv[7])
        tsne_space_coords = TSNE(n_components=new_dim, perplexity=perplex,learning_rate=eta,
                                 n_iter=n_it, random_state=seed).fit_transform(X)
        formatt='%.10f'
    elif tech == 'GAUSSMIX':
        n_clust = int(sys.argv[4])
        cluster_id = mixture.GaussianMixture(n_components=n_clust,
                                             random_state=rseed).fit(X).predict(X)
    elif tech == 'WARD':
        n_clust = int(sys.argv[4])
        cluster_id = cluster.AgglomerativeClustering(n_clusters=n_clust, linkage='ward').fit_predict(X)
    elif tech == 'COMPLETE':
        n_clust = int(sys.argv[4])
        cluster_id = cluster.AgglomerativeClustering(n_clusters=n_clust, linkage='complete').fit_predict(X)

    file_name = tech + '_validation_file_for_vtea.csv'
    np.savetxt(filename, X, fmt=formatt, delimiter=',' )

        
        
        
