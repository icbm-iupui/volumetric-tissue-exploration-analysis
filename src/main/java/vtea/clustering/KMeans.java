/*
 * Copyright (C) 2018 SciJava
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package vtea.clustering;

import java.util.ArrayList;
import java.util.Random;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.scijava.plugin.Plugin;
import vtea.featureprocessing.AbstractFeatureProcessing;
import vtea.featureprocessing.FeatureProcessing;

/**
 *
 * @author drewmcnutt
 */
@Plugin (type = FeatureProcessing.class)
public class KMeans extends AbstractFeatureProcessing{
    public KMeans(){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implementation of K-means";
        NAME = "K-means Clustering";
        KEY = "Kmeans";
        TYPE = "Cluster";
    }
    
    public KMeans(int max){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implementation of K-means";
        NAME = "K-means Clustering";
        KEY = "Kmeans";
        TYPE = "Cluster";
        
        protocol = new ArrayList();
        
        protocol.add(new JLabel("Number of clusters"));
       
        protocol.add(new JSpinner(new SpinnerNumberModel(5,2,max,1)));
    }
    
    @Override
    public boolean process(ArrayList al, double[][] feature){
        int n_clust;
        int numTrials = 20;
        
        ArrayList selectData = (ArrayList)al.get(1);
        boolean znorm = (boolean)al.get(0);
        feature = selectColumns(feature, selectData);
        feature = normalizeColumns(feature, znorm);
        dataResult.ensureCapacity(feature.length);
        
        JSpinner clust = (JSpinner)al.get(5);
        n_clust = ((Integer)clust.getValue());
        
        Centroids best = calculateClusters(n_clust,feature);
        for(int t = 0; t < numTrials; t++){
            Centroids C= calculateClusters(n_clust,feature);
            if(C.getDissimilarity() < best.getDissimilarity())
                best = C;
        }
        for(int memb: best.getMembership())
            dataResult.add(memb);
        return true;
    }
    
    public static String getBlockComment(ArrayList comComponents){
        String comment = "<html>";
        comment = comment.concat(((JLabel)comComponents.get(4)).getText() + ": ");
        comment = comment.concat(((JSpinner)comComponents.get(5)).getValue().toString());
        comment = comment.concat("</html>");
        return comment;
    }
    
    
    double calculateDistanceSq(double[] n1, double[] n2){
        double dist = 0;
        if(n1.length != n2.length)
            throw new RuntimeException("The dimensions of the two vectors are not the same");
        
        int dim = n1.length;
        
        for(int i = 0; i < dim; i++){
            dist += (n1[i] - n2[i]) * (n1[i] - n2[i]);
        }
        
        return dist;
    }
    
    class Centroids{
        ArrayList<double[]> centers;
        int[] membership;
        double dissimilarity; 
        Centroids(ArrayList<double[]> cents, int[] memb){
            centers = cents;
            membership = memb;
        }
        
        public double getDissimilarity(){return dissimilarity;}
        public int[] getMembership(){return membership;}
        public void setDissimilarity(double[][] feature){
            dissimilarity = 0;
            for(int i = 0; i < membership.length; i++){
                dissimilarity += calculateDistanceSq(centers.get(membership[i]),feature[i]);
            }
        }
                
        
    }
    
    private Centroids calculateClusters(int n_clust, double[][] feature){
        double dissimilarity = 0;
        int n = feature.length;
        int dim = feature[0].length;
        ArrayList<double[]> clusters = new ArrayList<>(n_clust);
        long randomSeed = System.nanoTime();
        Random randomGen = new Random(randomSeed);
        
        int iterCount = 0;
        for(int i = 0; i < n_clust; i++){
            double[] row = feature[randomGen.nextInt(n)];
            clusters.add(row);
        }
        int[] membership = new int[n];
        while(true){
            for(int i = 0; i < n; i++){
                double distSq = Double.MAX_VALUE;
                for(int j = 0; j < n_clust; j++){
                    double d = calculateDistanceSq(feature[i],clusters.get(j));
                    if(d < distSq){
                        membership[i] = j;
                        distSq = d;
                    }

                }
                dissimilarity += distSq;
            }
            double clustDif = 0;
            for(int i = 0; i < n_clust; i++){
                int count = 0;
                ArrayList center = new ArrayList(dim);
                for(int cent = 0; cent < dim; cent++)
                    center.add((double)0.0);
                for(int j = 0; j < n; j++){
                    if(membership[j] == i){
                        count++;
                        for(int k = 0; k < dim; k++){
                            center.set(k, (double)center.get(k) + feature[j][k]);
                        }
                    }
                }
                double[] newCentroid = new double[dim];
                double[] oldCentroid = clusters.get(i);
                for(int k = 0; k < dim; k++){
                    newCentroid[k] = (double)center.get(k)/count;
                    clustDif += (oldCentroid[k] - newCentroid[k]) * (oldCentroid[k] - newCentroid[k]);
                }

                clusters.set(i, newCentroid);

            }
            if(clustDif == 0)
                break;
            iterCount++;
            //System.out.printf("%d Iteration(s) complete, Centroids moved by %f%n", iterCount, clustDif);
        }
        
        Centroids c = new Centroids(clusters,membership);
        c.setDissimilarity(feature);
        return c;
    }
    
}
