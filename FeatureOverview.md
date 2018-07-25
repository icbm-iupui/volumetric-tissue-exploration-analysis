# Feature Overview

### Table of Contents

#### [Clustering](#Clustering)
  1. ##### [Hierarchical Clustering](#Hierarchical)
      + #####   [Ward](#Ward)
      + #####   [Single-Link](#Single)
      + #####   [Complete-Link](#Complete)
  2. #####  [K-means](#kmeans)
  3. #####  [X-Means](#xmeans)
  4. #####  [Gaussian Mixture](#gm)
#### [Dimensionality Reduction](#dr)
  1. #####  [t-Distributed Stochastic Neighborhood Embedding (t-SNE)](#tsne)
  2. #####  [Principal Component Analysis (PCA)](#pca)
#### [Criteria for Model Selection](#infocriterion)
  1. ##### [Bayesian Information Criterion (BIC)](#bic)
  2. ##### [Akaike Information Criterion (AIC)](#aic)
  
  ***
  
<a name="Clustering"></a>
## Clustering
&emsp;Using a marker for similarity the data points are combined into different clusters. Some clustering methods include the cluster number as a parameter while other methods calculate the cluster number as part of the method.

<a name="Hierarchical"></a>
### Hierarchical Clustering
&emsp;An exclusive and intrinsic method for forming the data into clusters also called agglomerative clustering. The algorithm starts with every data point as its own clusters and combines clusters with the smallest dissimilarity until there is the selected number of clusters left. The measure of dissimilarity can be any form of distance (e.g. Euclidean, Manhattan). Hierarchical clustering is deterministic because it is directly based on the measurements of the points. Every hierarchical clustering method uses a proximity matrix to determine the hierarchy, with each value of the matrix delineating the dissimilarity between the cluster indicated by the row and column.\
&emsp;The standard algorithm has a time complexity of O(n<sup>3</sup>) and requires O(n<sup>2</sup>) memory.\
&emsp;The number of clusters must be provided to the method, however, a dendrogram (a tree structure) can be made from the data to show how points are clustered and to show when the distance between clusters jumps to indicate an idea of the correct number of clusters.\
&emsp;When using a Hierarchical clustering method, it is best to use normalized data so distance is not skewed by larger numbers.
  
  <a name="hierarchicalalgorithm"></a>
  __Algorithm__
  1. Compute dissimilarity matrix (proximity matrix) based on chosen form of distance
  2. Find the least dissimilar pair of clusters (row and column of the smallest number in the matrix)
  3. Increment the sequence number, merge the two clusters (**r** & **s**) into a single cluster(**t**), set the level of the clustering as the distance between **r** and **s**
  4. Update the proximity matrix by deleting rows and columns corresponding to **r** and **s**
  5. Add a row/column for the new cluster, **t**, using the equation below where k is any other cluster
     
     proximity[k,**t**] = α<sub>r</sub> * dist[k,**r**] + α<sub>s</sub> * dist[k,**r**] + β * dist[**r**,**s**] + ɣ * abs(dist[k,**r**] - dist[k,**s**])
     
     where α<sub>r</sub>, α<sub>s</sub>, β, and ɣ are dependent on the linkage method used (e.g. Ward, Single-Link, etc.)
  6. If there is one cluster left, then complete, otherwise  go back to **2**
  
  |Pros|Cons|
  |---|---|
  |Deterministic|Must choose number of clusters|
  |Level of cluster can identify proper number of clusters| Each linkage method is slightly different|
  | |Not necessarily global optimal solution|
  | |Very greedy algorithm|
  
  <a name="Ward"></a>
  #### Ward's Hierarchical Clustering
 &emsp;This hierarchical clustering method, also called minimum variance method, picks the clusters to merge based on the optimal error sum of squares. This optimization ends up minimizing the total within-cluster variance.\
  &emsp;When using the Ward method, only squared Euclidean distance can be used as the similarity measure.\
  &emsp;Using the [algorithm provided for Hierarchical clustering](#hierarchicalalgorithm), the values for α<sub>r</sub>, α<sub>s</sub>, β, and ɣ in step 5 are as follows
  
  |α<sub>r</sub>|α<sub>s</sub>|β| ɣ|
  ---|---|---|---|
  ![(n<sub>r</sub> + n<sub>k</sub>) / (n<sub>r</sub> + n<sub>k</sub> + n<sub>s</sub>)][wardalphar]|![(n<sub>s</sub> + n<sub>k</sub>) / (n<sub>r</sub> + n<sub>k</sub> + n<sub>s</sub>)][wardalphas]|![(-n<sub>k</sub>) / (n<sub>r</sub> + n<sub>k</sub> + n<sub>s</sub>)][wardbeta]|![0][zero]
  
  where n<sub>r</sub> is the number of objects in cluster **r**, n<sub>s</sub> is the number of objects in cluster **s**, and n<sub>k</sub> is the number of objects in cluster k
  
  <a name="Single"></a>
  #### Single-Link Hierarchical Clustering
  &emsp;This hierarchical clustering method, also called the minimum method, sets the proximities of a new cluster as the minimum distance between one of its objects and an object from a different cluster. This tends to create chains with little homogeneity.\
  &emsp;Using the [algorithm provided for Hierarchical clustering](#hierarchicalalgorithm), the values for α<sub>r</sub>, α<sub>s</sub>, β, and ɣ in step 5 are as follows
  
  |α<sub>r</sub>|α<sub>s</sub>|β| ɣ|
  ---|---|---|---|
  ![1/2][onehalf]|![1/2][onehalf]|![0][zero]|![-1/2][minushalf]
  
  <a name="Complete"></a>
  #### Complete-Link Hierarchical Clustering
  &emsp;This hierarchical clustering method, also called the maximum method, sets the proximities of a new cluster as the maximum distance between one of its objects and an object from a different cluster. This tends to create clusters that may not be well separated.\
  &emsp;Using the [algorithm provided for Hierarchical clustering](#hierarchicalalgorithm), the values for α<sub>r</sub>, α<sub>s</sub>, β, and ɣ in step 5 are as follows
  
  |α<sub>r</sub>|α<sub>s</sub>|β| ɣ|
  ---|---|---|---|
  ![1/2][onehalf]|![1/2][onehalf]|![0][zero]|![1/2][onehalf]
  
  <a name="kmeans"></a>
  ### K-means
 &emsp;Iterative algorithm that separates n objects into k clusters. Every object is assigned to the cluster with the nearest mean. K-means can be viewed as a special case of [Gaussian Mixture](#gm) that uses an Expectation Maximization(EM) approach. When viewed as an EM approach [step 2](#kmeansalgorithm) is the expectation step and [step 3](#kmeansalgorithm) is seen as the maximization step.\
    &emsp;K-means tends to produce clusters of equal size.\
    &emsp;Given a fixed dimension,d, and number of clusters,k, the problem can be solved in O(n<sup>kd + 1</sup>) time.
    
<a name="kmeansalgorithm"></a>
__Algorithm__
1. Assign the initial centroids from k randomly selected objects
2. Create k clusters by assigning every object to the closest centroid
3. Compute the new centroids based on the mean of the objects in each cluster
4. Repeat **2** and **3** until centroids do not change or cluster assignment does not change
    
|Pros|Cons|
---|---|
Performs well on spherical data|Prone to converge on local minima|
Fast|k is an input parameter|
| |Non-deterministic|
| |Scales poorly computationally|
  
  <a name="xmeans"></a>
  ### X-means
  &emsp;A variant of [K-means](#kmeans) that increases speed and uses an information criterion to determine the number of clusters.<sup>[1](#xmeansfoot)<a name="xmeansfoo"></a></sup> The speed is increased by using a kd-tree architecture and blacklisting to refine the search for objects in the cluster. The algorithm is given an upper bound for the number of clusters, k, and determines the proper number of clusters for the data in the range 2 to k.\
  &emsp;For large problems X-means scales much better in comparison to K-means which is over 2x slower. However, may not be better for a large amount of dimensions.
  
__Algorithm__
1. Run the conventional [k-means algorithm](#kmeansalgorithm) until convergence
2. Split all centroids (parents) into two centroids (children)
3. Run a local [k-means algorithm](#kmeansalgorithm) on the two children in the parent region until convergence 
4. Perform a model selection test (e.g. [BIC, AIC](#infocriterion)) to determine if the children or the parent represent the structure better
5. Kill the parent or children, according to the test
6. Repeat steps **1** through **5** until the upper bound for k is met
7. Provide the model with the best score on the given selection test

Pros|Cons|
---|---|
Determines the number of clusters|Not proven on high dimensional data|
Avoids local minima|Non-deterministic(but more consistent than [K-means](#kmeans))|
Faster than k-means| |

  
<a name="gm"></a>
### Gaussian Mixture
  &emsp;A probabilistic model that assumes all of the data is a combination of a finite number of gaussian distributions. This can be thought of as a generalized [K-means](#kmeansalgorithm), where the clusters can have different sizes and shapes based on their covariance's. It employs an Expectation Maximization(EM) algorithm wherein [step 5](#gmalgorithmk) is is the expectation and [step 7](#gmalgorithmk) is the maximization.\
  &emsp;The algorithm has the ability to use a [model selection test](#infocriterion) to choose the number of clusters, k, or to provide a number of clusters, k.

<a name="gmalgorithmk"></a>  
__Algorithm (given k clusters)__
1. Calculate the mean of all of the objects for every dimension
2. Calculate the covariance matrix (multi-dimensional standard deviation)
3. Setup Gaussian Distributions
  1. Centroid is set as random object in data
  2. All given same covariance matrix and priori
4. Use [k-means algorithm](#kmeansalgorithm) to find centers of clusters
5. Calculate posterior probabilities of each object
6. Using probabilities assign each object to a distribution
7. Reassign parameters of distribution based on the objects in the distribution
8. Repeat steps **5** through **7** until convergence

__Algorithm (automatically select k)__
1. Create gaussian distribution to fit the data
2. Calculate and record model selection test (e.g. [BIC, AIC](#infocriterion))
3. Split the distribution with the largest covariance
4. Calculate posterior probabilities of each object
5. Using probabilities assign each object to a distribution
6. Reassign parameters of distribution based on the objects in the distribution
7. Repeat steps **4** through **6** until convergence
8. Repeat steps **2** through **7** until the model selection test increases

|Pros|Cons|
---|---|
Probabilistic Clustering| EM tends to find local minima|
| |Data may not be normally distributed|
| |Non-deterministic|

***
<a name="Dimensionality Reduction"></a>
## Dimensionality Reduction
  &emsp;Reducing the amount of variables in a dataset in order to better handle the data while still conveying the same information. Helpful in visualizing datasets with many dimensions because anything over 3 dimensions is difficult to understand. It is useful in removing redundant features and noise.\
  &emsp;Dimensionality Reduction is affected by the curse of dimensionality.

<a name="tsne"></a>
### t-Distributed Stochastic Neighborhood Embedding (t-SNE)
  &emsp;A visualization tool to plot high dimensional data onto a two or three dimensional space. A variation of Stochastic Neighborhood Embedding that is both faster and avoids crowding the points in the center. t-SNE uses a symmetric cost function known as the Kullback-Liebler(KL) Divergence<sup name="backkl">[2](#kldivergence)</sup>, which maps the similarities from the high dimensional space onto the low dimensional space. To prevent crowding there is a uniform background model that prevents the distance between low dimensional points from being below a certain value. Addional crowding prevention is provided by the use of a student t-Distribution instead of a Gaussian distribution in the low dimensional space, to ensure that a moderate distance in the high dimensional space is modeled to a greater distance in the low dimensional space. t-SNE uses a gradient descent algorithm to optimize the KL Divergence and create the mapping.\
  &emsp;A number of parameters are given to the t-SNE algorithm including perplexity, number of iterations, and learning rate. Perplexity is a measure of how well attention is balanced between local and global aspects of the data, it usually has a value between 5 and 50. The number of iterations is the number of times the gradient descent algorithm will iterate, this should be greater than 250. The learning rate is the size of the step taken during the gradient descent, with values typically ranging from 1 to 1000. A website discussing the effects of the parameters can be found [here](https://distill.pub/2016/misread-tsne/).\
  &emsp;t-SNE has a time and memory complexity of O(n<sup>2</sup>). This complexity can be reduced to to O(nlog(n)) by using a method developed by astrophysicists, called Barnes-Hut. Barnes-Hut works by approximating similar interactions as one object located at the center of mass of all of the similar objects and the force that virtual object has is multiplied by the number of objects it represents.\
  &emsp;The cost function of t-SNE is non-convex, therefore it is prone to find local minima. However, if t-SNE is run multiple times and the value of the cost function is recorded, a lower minima (or even the global minima) can be found. Since t-SNE initializes the solution randomly, each run will be different, meaning that some runs will find different local minima than others. The value of the cost function can be compared over multiple runs of t-SNE to find the best value of the cost function obtained over all of the runs.
  

__Algorithm__
1. Normalize input data by dividing by the maximum
2. Compute the affinities of the data points in the high dimensional space
3. Randomly initialize solution in the low dimensional space
4. Compute the low dimensional affinities
5. Compute the gradient of the KL Divergence
5. Update points in the low dimensional space
6. Repeat steps **5** and **6** for the given number of iterations

|Pros|Cons|
---|---|
|Good at visualizing data |t-SNE has not been evaluated for reducing dimensions to d>3|
|Represents local structure well|Global structure does not mean anything|
| |Often finds local minima|
| |Non-deterministic|
| |Bad at visualizing swiss roll type problems|

<a name="pca"></a>
### Principal Component Analysis (PCA)
&emsp;Principal Component Analysis finds linearly independent dimensions which can losslessly represent the data. The first new dimension, called the first principal component, contains the greatest variance by some projection of the data. With subsequent principal components containing the greatest variance of the remaining dimensions. The values of the datapoints on the principal components are referred to as the score while the loadings are the weight by which each standardized original variable should be multiplied to get the score. The principal components are actually the eigenvectors of the covariance matrix of the data such that the eigenvectors are ordered by decreasing eigenvalue. Singular Value Decomposition(SVD) proves to be a faster method than eigenvalue decomposition. SVD calculates a value for U, Σ, and V such that **X=UΣV<sup>*</sup>**. The score matrix, **T** , can be easily calculated via **T=UΣ**. The dimension of the score matrix **T** can be reduced to dimension **p** by deleting columns of **Σ** such that it becomes a **n**x**p** matrix where **n** is the number of observations.\
&emsp;When using PCA it is necessary to preprocess the data such that all data is normalized to be in the same range. Otherwise one variable may dominate and the first principal component will essentially lay along the axis of that variable.

<a name="pcaalgorithm"></a>
__Algorithm__
1. Normalize all the variables of the data to create matrix <em>X</em>
2. Compute the Singular Value Decomposition of <em>X</em> to get <em>X=UΣV</em>
3. Delete the columns in <em>Σ</em> after column <em>p</em>, the desired new dimension.
4. Calculate <em>T=UΣ</em> to get the scores
(Alternatively the column <em>p</em> can be selected via the desired variance, <em>v</em>. The cumulative variance of each principal component can be quickly calculated by summing all of the singular values up to and including the column of that principal component)

Pros|Cons|
---|---|
|Deterministic|May lose low variance deviations between neighbors|
|Reversible|Heavily influenced by outliers |
|Preserves global properties| |

---
<a name="infocriterion"></a>
### Criteria for Model Selection
<a name="bic"></a>
#### Bayesian Information Criterion (BIC)
&emsp;Criterion for model selection from a finite number of models. Introduces a penalty term for the number of parameters in the model so as to avoid overfitting. The model with the lowest BIC is preferred. Tends to be more asymptotically correct, however, it often chooses models that are too small. Does not indicate anything about the absolute quality of the model, only its quality relative to other models.\
&emsp;`BIC = -2 * L + k * ln(n)`; where L is the maximum log-likelihood of the model, k is the number of parameters of the model, and n is the number of observations.

<a name="aic"></a>
#### Akaike Information Criterion (AIC)
&emsp;Criterion for model selection from a finite number of models. Similar to [BIC](#bic), in that it includes a penalty term for the number of parameters. However, the penalty is less severe than [BIC](#bic) when the model has more than 8 observations. The model with the lowest AIC is preferred. Does not indicate anything about the absolute quality of the model, only its quality relative to other models.\
&emsp;`AIC = -L + k`; where L is the maximum log-likelihood of the model and k is the number of parameters of the model.

---  
##### Footnotes
<a name="xmeansfoot"></a>
1: For more information see: Dan Pelleg, Andrew W. Moore: X-means: Extending K-means with Efficient Estimation of the Number of Clusters. In: Seventeenth International Conference on Machine Learning, 727-734, 2000[↩](#xmeansfoo)

<a name="kldivergence"></a>
2:![KL Divergence][kl] where p is the high dimensional space and q is the low dimensional space\
The similarity for the higher dimensional space, p, is calculated via\
  ![similarity calculation high dimension][similaritiesp] \
and the low dimensional space, q, calculation is calculated via\
  ![similarity calculation low dimension][similaritiesq]\
  To get rid of outliers the high dimensional space is subject to the constraint ![fix outliers][outlie] [↩](#backkl)

[wardalphar]:http://www.sciweavers.org/tex2img.php?eq=%5Cfrac%7Bn_%7Br%7D%2Bn_%7Bk%7D%7D%7Bn_%7Br%7D%2Bn_%7Bk%7D%2Bn%7Bs%7D%7D&bc=White&fc=Black&im=jpg&fs=12&ff=arev&edit=0
[wardalphas]:http://www.sciweavers.org/tex2img.php?eq=%5Cfrac%7Bn_%7Bs%7D%2Bn_%7Bk%7D%7D%7Bn_%7Br%7D%2Bn_%7Bk%7D%2Bn%7Bs%7D%7D&bc=White&fc=Black&im=jpg&fs=12&ff=arev&edit=0
[wardbeta]:http://www.sciweavers.org/tex2img.php?eq=%5Cfrac%7B-n_%7Bk%7D%7D%7Bn_%7Br%7D%2Bn_%7Bk%7D%2Bn%7Bs%7D%7D&bc=White&fc=Black&im=jpg&fs=12&ff=arev&edit=0
[zero]:http://www.sciweavers.org/tex2img.php?eq=0&bc=White&fc=Black&im=jpg&fs=12&ff=arev&edit=0
[onehalf]:http://www.sciweavers.org/tex2img.php?eq=%5Cfrac%7B1%7D%7B2%7D&bc=White&fc=Black&im=jpg&fs=12&ff=arev&edit=0
[minushalf]:http://www.sciweavers.org/tex2img.php?eq=-%5Cfrac%7B1%7D%7B2%7D&bc=White&fc=Black&im=jpg&fs=12&ff=arev&edit=0
[kl]:
[similaritiesp]:
[outlie]:
[similaritiesq]:
