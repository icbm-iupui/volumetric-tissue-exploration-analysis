/* 
 * Copyright (C) 2016-2018 Indiana University
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

import ij.IJ;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import org.scijava.plugin.Plugin;
import vtea.featureprocessing.AbstractFeatureProcessing;
import vtea.featureprocessing.FeatureProcessing;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *K-Means Clustering.
 * @author drewmcnutt
 */
@Plugin (type = FeatureProcessing.class)
public class KMeans extends AbstractFeatureProcessing{
    
    public static boolean validate = true;
    
    Random rand;
    Runtime rt;
    /**
     * Basic Constructor. Sets all protected variables
     */
    public KMeans(){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implementation of K-means";
        NAME = "K-means Clustering";
        KEY = "Kmeans";
        TYPE = "Cluster";
    }
    
    /**
     * Constructor called for initialization of Setup GUI.
     * When components are added to this, the static method must be altered.
     * @param max the number of objects segmented in the volume
     */
    public KMeans(int max){
        this();
        
        protocol = new ArrayList();
        
        protocol.add(new JLabel("Clusters"));
        protocol.add(new JSpinner(new SpinnerNumberModel(5,2,max,1)));
        
        protocol.add(new JLabel("Iterations"));
        protocol.add(new JTextField("10",2));
    }
    
    @Override
    public String getDataDescription(ArrayList params){
        return KEY + '_' + String.valueOf((Integer)((JSpinner)params.get(5)).getValue()) + '_' + getCurrentTime();
    }
    /**
     * Performs the K-Means clustering based on the parameters.
     * @param al contains all of the parameters in the form of JComponents
     * @param feature the full data to be parsed and analyzed
     * @return true when complete
     */
    @Override
    public boolean process(ArrayList al, double[][] feature, boolean val){
        long start;
        int n_clust;
        int numTrials;
        
        long randomSeed = System.nanoTime();
        rand = new Random(randomSeed);
        
        ArrayList selectData = (ArrayList)al.get(1);
        boolean znorm = (boolean)al.get(0);
        feature = selectColumns(feature, selectData);
        feature = normalizeColumns(feature, znorm);
        dataResult.ensureCapacity(feature.length);
        
        JSpinner clust = (JSpinner)al.get(5);
        n_clust = ((Integer)clust.getValue());
        JTextField trial = (JTextField)al.get(7);
        numTrials = (Integer.parseInt(trial.getText()));
        
        
        Centroids best;
        if(val){
            try{
                IJ.log("VALIDATING: Beginning");
                start = System.nanoTime();
                int seed = Math.abs(rand.nextInt() - 1);
                rt = Runtime.getRuntime();
                String s = getPython();
               
                performValidation(feature, n_clust, numTrials, seed);
                
                String randScript = getCWD() + "/src/main/resources/KmeansRandom.py";
                String[] randomGen = new String[]{s, randScript, String.valueOf(seed), String.valueOf(feature.length), String.valueOf(n_clust), String.valueOf(numTrials)};
                Process p = rt.exec(randomGen);
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                p.waitFor();
                String e = null;
                while((e = stdError.readLine()) != null){
                        System.out.println(e);
                }
                long end = System.nanoTime();
                IJ.log("VALIDATING: Completed in " + (end-start)/1000000000 + " seconds" );
                
                IJ.log("VALIDATING: Retrieving random list for the Java Method" );
                int[][] list = getList("random_initial_row_for_kmeans.csv");
                
                IJ.log("PROFILING: Finding KMeans clusters for " + n_clust + " clusters on " + feature[0].length + " features");
                best = performClustering(feature, n_clust, numTrials, list);
                
                deleteFiles(new String[]{"random_initial_row_for_kmeans.csv", "matrix_for_python.csv"});
            }catch(IOException | InterruptedException e){
                e.printStackTrace();
                best = new Centroids();
            }
            
        }else{
            best = performClustering(feature, n_clust, numTrials);
        }
            
        //Exists so that MicroExplorer deals with same data structure for both Clustering and Reduction
        ArrayList holder = new ArrayList();     
        for(int memb: best.getMembership())
            holder.add(memb);
        dataResult.add(holder);
        return true;
    }
    
    private void performValidation(double[][] matrix,int n_clust,int numTrials, int seed){
        makeMatrixCSVFile(matrix);
        String s = getPython();
        String validationScript = getCWD() + String.format("%1$csrc%1$cmain%1$cresources%1$cvalidation_script.py", File.separatorChar);
        try{
            String[] validationGen = new String[]{s, validationScript, "matrix_for_python.csv", "KMEANS", String.valueOf(seed), String.valueOf(n_clust), String.valueOf(numTrials)};
            Process p = rt.exec(validationGen);
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            BufferedReader print = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String e = null;
            
            p.waitFor();
            while((e = stdError.readLine()) != null)
                System.out.println(e);
            while((e = print.readLine()) != null)
                System.out.println(e);
        }catch(IOException | InterruptedException ie){
            ie.printStackTrace();
        }
        
    }
    
    private Centroids performClustering(double[][] feature, int nClust, int nTrials){
        int[][] list = new int[10][5];
        for(int i = 0; i < list.length; i++){
            for(int j = 0; j < list[i].length; j++){
                list[i][j] = rand.nextInt(feature.length);
            }
        }
        
        return performClustering(feature, nClust, nTrials, list);
    }
    
    private Centroids performClustering(double[][] feature, int nClust, int nTrials, int[][] list){
        IJ.log(String.format("PROFILING: Performing clustering: Trial %d of %d", 1,nTrials));
        Centroids best = calculateClusters(nClust,feature, list[0]);
        for(int t = 1; t < nTrials; t++){
            IJ.log(String.format("PROFILING: Performing clustering: Trial %d of %d", t + 1 ,nTrials));
            Centroids C= calculateClusters(nClust,feature, list[t]);
            if(C.getDissimilarity() < best.getDissimilarity())
                best = C;
        }
        IJ.log(String.format("PROFILING: Best clustering found, Dissimilarity of %f", best.getDissimilarity()));
        return best;
    }
    
    /**
     * Creates the Comment Text for the Block GUI.
     * @param comComponents the parameters (Components) selected by the user in 
     * the Setup Frame.
     * @return comment text detailing the parameters
     */
    public static String getBlockComment(ArrayList comComponents){
        String comment = "<html>";
        comment = comment.concat(((JLabel)comComponents.get(4)).getText() + ": ");
        comment = comment.concat(((JSpinner)comComponents.get(5)).getValue().toString() + ", ");
        comment = comment.concat(((JLabel)comComponents.get(6)).getText() + ": ");
        comment = comment.concat(((JTextField)comComponents.get(7)).getText());
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
        
        Centroids(){
            
        }
        
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
    
    private Centroids calculateClusters(int n_clust, double[][] feature, int[] list){
        double dissimilarity = 0;
        int n = feature.length;
        int dim = feature[0].length;
        ArrayList<double[]> clusters = new ArrayList<>(n_clust);
 
        int iterCount = 0;
        for(int i = 0; i < n_clust; i++){
            double[] row = feature[list[i]];
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
