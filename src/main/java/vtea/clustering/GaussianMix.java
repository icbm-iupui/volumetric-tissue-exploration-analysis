/* 
 * Copyright (C) 2020 Indiana University
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.scijava.plugin.Plugin;
import smile.math.Math;
import smile.stat.distribution.MultivariateGaussianDistribution;
import smile.stat.distribution.MultivariateGaussianMixture;
import smile.stat.distribution.MultivariateMixture;
import vtea.featureprocessing.AbstractFeatureProcessing;
import vtea.featureprocessing.FeatureProcessing;

/**
 *Gaussian Mixture Model Clustering. Clusters the data assuming the data is a mixture of 
 * Gaussian Distributions. There are two different modes, one where the number
 * of clusters is selected by the user and one where the number of clusters is
 * selected based on an information criterion.
 * @author drewmcnutt
 */
@Plugin (type = FeatureProcessing.class)
public class GaussianMix extends AbstractFeatureProcessing{
    public static boolean validate = true;
    /**
     * Creates the Comment Text for the Block GUI.
     * @param comComponents the parameters (Components) selected by the user in
     * the Setup Frame.
     * @return comment text detailing the parameters
     */
    public static String getBlockComment(ArrayList comComponents){
        String comment = "<html>";
        JCheckBox jcb = (JCheckBox)comComponents.get(6);
        if(jcb.isSelected())
            comment = comment.concat(jcb.getText() + ": Enabled");
        else{
            comment = comment.concat(((JLabel)comComponents.get(4)).getText() + ": ");
            comment = comment.concat(((JSpinner)comComponents.get(5)).getValue().toString());
            comment = comment.concat("</html>");
        }
        return comment;
    }
    List<MultivariateMixture.Component> components;
    Runtime rt;
    Random rand;
    
    /**
     * Basic Constructor. Sets all protected variables
     */
    public GaussianMix(){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implements the plugin from SMILE";
        NAME = "Gaussian Mixture";
        KEY = "GaussianMixture";
        TYPE = "Cluster";
    }
    
    /**
     * Constructor called for initialization of Setup GUI.
     * When components are added to this, the static method must be altered.
     * @param max the number of objects segmented in the volume
     */
    public GaussianMix(int max){
        this();
        
        protocol = new ArrayList();
        
        JLabel clust = new JLabel("Number of clusters");
        protocol.add(clust);
       
        JSpinner n_clust = new JSpinner(new SpinnerNumberModel(5,2,max,1));
        protocol.add(n_clust);
        JCheckBox auto = new JCheckBox("Select Automatically");
        JComboBox infoCrit = new JComboBox(new String[] {"BIC", "AIC"});
        infoCrit.setVisible(false);
        auto.addItemListener(new ItemListener(){
                @Override
                public void itemStateChanged(ItemEvent evt) {
                    if(auto.isSelected()){
                        infoCrit.setVisible(true);
                        n_clust.setVisible(false);
                        clust.setVisible(false);
                    }else{
                        infoCrit.setVisible(false);
                        n_clust.setVisible(true);
                        clust.setVisible(true);
                    }
                }
        });
        protocol.add(auto);
        protocol.add(infoCrit);
    }
    
    
    @Override
    public String getDataDescription(ArrayList params){
        if(((JCheckBox)params.get(6)).isSelected())
            return KEY + "_autoCluster" + '_' + getCurrentTime();
        else{
           int clusters = (Integer)((JSpinner)params.get(5)).getValue();
            return KEY + '_' + String.valueOf(clusters) + '_' + getCurrentTime();
        } 
    }
    
    /**
     * Performs the Gaussian Mixture Model based on the parameters.
     * @param al contains all of the parameters in the form of JComponents
     * @param feature the full data to be parsed and analyzed
     * @return true when complete
     */
    @Override
    public boolean process(ArrayList al, double[][] feature, boolean val){
        long start;
        int n_clust;
        
        long randSeed = System.nanoTime();
        rand = new Random(randSeed);
        
        MultivariateGaussianMixture mgm;
        
        JCheckBox auto_clust;
        components = new ArrayList<>();
        
        ArrayList selectData = (ArrayList)al.get(1);
        boolean znorm = (boolean)al.get(0);
        feature = selectColumns(feature, selectData);
        feature = normalizeColumns(feature, znorm);
        dataResult.ensureCapacity(feature.length);
        
        auto_clust = (JCheckBox)al.get(6);
        if(!auto_clust.isSelected()){
            JSpinner clust = (JSpinner)al.get(5);
            n_clust = ((Integer)clust.getValue()); 
           
            int seed = java.lang.Math.abs(rand.nextInt() - 1);
            if(val){
                start = System.nanoTime();
                try{
                    IJ.log("VALIDATING: Beginning");
                    start = System.nanoTime();
                    rt = Runtime.getRuntime();
                    

                    performValidation(feature, n_clust, seed);

                    String s = getPython();
                    String randScript = getCWD() + String.format("%1$csrc%1$cmain%1$cresources%1$cGaussianRandom.py", File.separatorChar);
                    String[] randomGen = new String[]{s, randScript, String.valueOf(seed), String.valueOf(n_clust)};
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
                    int[] list = getIntList("random_initial_values_for_GM.csv");
                    
                    IJ.log("PROFILING: Finding Gaussian Mixture Model for " + n_clust + " clusters on " + feature[0].length + " features");
                    run(feature, n_clust, list);
                    
                    deleteFiles(new String[]{"random_initial_values_for_GM.csv", "matrix_for_python.csv"});
                }catch(IOException | InterruptedException ie){
                    ie.printStackTrace();
                }
            }else{
                start = System.nanoTime();
                IJ.log("PROFILING: Finding Gaussian Mixture Model for " + n_clust + " clusters on " + feature[0].length + " features");
                int[] list = getRandom(feature, n_clust, seed);
                run(feature, n_clust, list);
            }
        }else{
            if(val){IJ.log("VALIDATING: *Cannot be validated when using Automatic*");}
            
            start = System.nanoTime();
            IJ.log("PROFILING: Finding Gaussian Mixture Model on " + feature[0].length + " features with lowest BIC");
            run(feature, ((JComboBox)al.get(7)).getSelectedIndex());
        }
        IJ.log("PROFILING: Extracting membership of clusters");
        getMembership(feature);
        long end = System.nanoTime();
        IJ.log("PROFILING: Gaussian Mixture Model completed in " + (end-start)/1000000 + " ms" );
//        auto_clust = (JCheckBox)al.get(6);
//        if(!auto_clust.isSelected()){
//            JSpinner clust = (JSpinner)al.get(5);
//            n_clust = ((Integer)clust.getValue()); 
//            IJ.log("PROFILING: Finding Gaussian Mixture Model for " + n_clust + " clusters on " + feature[0].length + " features");
//            start = System.nanoTime();
//            run(feature, n_clust, 5);       //last value is the random seed
//            
//        }
//        else{
//            start = System.nanoTime();
//            IJ.log("PROFILING: Finding Gaussian Mixture Model on " + feature[0].length + " features with lowest BIC");
//            run(feature, ((JComboBox)al.get(7)).getSelectedIndex());
//        }
//        IJ.log("PROFILING: Extracting membership of clusters");
//        getMembership(feature);
//        long end = System.nanoTime();
//        IJ.log("PROFILING: Gaussian Mixture Model completed in " + (end-start)/1000000 + " ms" );
        return true;
    }
    private void performValidation(double[][] matrix, int n_clust, int seed){
        makeMatrixCSVFile(matrix);
        String s = getPython();
        String pathAddOn = String.format("%1$csrc%1$cmain%1$cresources%1$cvalidation_script.py", File.separatorChar);
        String validationScript = getCWD() + pathAddOn;
        try{
            String[] validationGen = new String[]{s, validationScript, "matrix_for_python.csv", "GAUSSMIX", String.valueOf(seed), String.valueOf(n_clust)};
            Process p = rt.exec(validationGen);
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            p.waitFor();
            
            String e = null;
            while((e = stdError.readLine()) != null)
                System.out.println(e);
        }catch(IOException | InterruptedException ie){
            ie.printStackTrace();
        }
    }
    
    private int[] getRandom(double[][] data, int n_clust, int seed){
        int[] list = new int[n_clust];
        int l = 0;
        
        int n = data.length;
        int d = data[0].length;
        
        Random randGen = new Random(seed);
        int val = randGen.nextInt(n);
        double[] centroid = data[val];
        list[l] = val;
        l++;
        double[] D = new double[n];
        for (int i = 0; i < n; i++) {
            D[i] = Double.MAX_VALUE;
        }
        
        for (int i = 1; i < n_clust; i++) {
            // Loop over the samples and compare them to the most recent center.  Store
            // the distance from each sample to its closest center in scores.
            for (int j = 0; j < n; j++) {
                // compute the distance between this sample and the current center
                double dist = Math.squaredDistance(data[j], centroid);
                if (dist < D[j]) {
                    D[j] = dist;
                }
            }

            double cutoff = randGen.nextDouble() * Math.sum(D);
            double cost = 0.0;
            int index = 0;
            for (; index < n; index++) {
                cost += D[index];
                if (cost >= cutoff)
                    break;
            }

            centroid = data[index];
            list[l] = index;
        }
        
        return list;
    }
    private void run(double[][] data, int n_clust, int[] list){
        if (n_clust < 2)
            throw new IllegalArgumentException("Invalid number of components in the mixture.");

        int n = data.length;
        int d = data[0].length;
        double[] mu = new double[d];
        double[][] sigma = new double[d][d];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < d; j++) {
                mu[j] += data[i][j];
            }
        }

        for (int j = 0; j < d; j++) {
            mu[j] /= n;
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < d; j++) {
                for (int l = 0; l <= j; l++) {
                    sigma[j][l] += (data[i][j] - mu[j]) * (data[i][l] - mu[l]);
                }
            }
        }

        for (int j = 0; j < d; j++) {
            for (int l = 0; l <= j; l++) {
                sigma[j][l] /= (n - 1);
                sigma[l][j] = sigma[j][l];
            }
        }
//        int seed = java.lang.Math.abs(rand.nextInt() - 1);
//        Random randGen = new Random(seed);
        
        double[] centroid = data[list[0]];
        MultivariateMixture.Component c = new MultivariateMixture.Component();
        c.priori = 1.0 / n_clust;
        MultivariateGaussianDistribution gaussian = new MultivariateGaussianDistribution(centroid, sigma);
        c.distribution = gaussian;
        components.add(c);

        // We use a the kmeans++ algorithm to find the initial centers.
        // Initially, all components have same covariance matrix.
//        double[] D = new double[n];
//        for (int i = 0; i < n; i++) {
//            D[i] = Double.MAX_VALUE;
//        }

        // pick the next center
        for (int i = 1; i < n_clust; i++) {
//            // Loop over the samples and compare them to the most recent center.  Store
//            // the distance from each sample to its closest center in scores.
//            for (int j = 0; j < n; j++) {
//                // compute the distance between this sample and the current center
//                double dist = Math.squaredDistance(data[j], centroid);
//                if (dist < D[j]) {
//                    D[j] = dist;
//                }
//            }
//
//            double cutoff = randGen.nextDouble() * Math.sum(D);
//            double cost = 0.0;
//            int index = 0;
//            for (; index < n; index++) {
//                cost += D[index];
//                if (cost >= cutoff)
//                    break;
//            }

            centroid = data[list[i]];
            c = new MultivariateMixture.Component();
            c.priori = 1.0 / n_clust;
            gaussian = new MultivariateGaussianDistribution(centroid, sigma);
            c.distribution = gaussian;
            components.add(c);
        }

        EM(components, data, 0.2, Integer.MAX_VALUE);
    }
    
    private void run(double[][] data, int infoC){
        if (data.length < 20)
            throw new IllegalArgumentException("Too few samples.");

        ArrayList<MultivariateMixture.Component> mixture = new ArrayList<>();
        MultivariateMixture.Component c = new MultivariateMixture.Component();
        c.priori = 1.0;
        c.distribution = new MultivariateGaussianDistribution(data, false);
        mixture.add(c);

        int freedom = 0;
        for (int i = 0; i < mixture.size(); i++)
            freedom += mixture.get(i).distribution.npara();

        double ic = 0.0;
        for (double[] x : data) {
            double p = c.distribution.p(x);
            if (p > 0) ic += Math.log(p);
        }
        ic -= ((infoC) * freedom) + ((1 - infoC) * 0.5 * Math.log(data.length) * freedom);

        double b = Double.NEGATIVE_INFINITY;
        while (ic > b) {
            b = ic;
            components = (ArrayList<MultivariateMixture.Component>) mixture.clone();

            split(mixture);
            ic = EM(mixture, data, 0.2, Integer.MAX_VALUE);

            freedom = 0;
            for (int i = 0; i < mixture.size(); i++)
                freedom += mixture.get(i).distribution.npara();

            ic -= ((infoC) * freedom) + ((1 - infoC) * 0.5 * Math.log(data.length) * freedom);
        }
        
        IJ.log(components.size() + " cluster(s) found automatically using BIC");
    }
    
    private int findMax(double[] probabilities){
        double max_prob = 0;
        int max_col = 0;
        for(int i = 0; i < probabilities.length; i++){
            if(probabilities[i] > max_prob){
                max_col = i;
                max_prob = probabilities[i];
            }
        }
        
        return max_col;
    }

    private void getMembership(double[][] feature){
        double[][] probabilities = new double[feature.length][components.size()];
        for(int i = 0; i < components.size(); i++){
            MultivariateGaussianDistribution mgd = (MultivariateGaussianDistribution)((MultivariateMixture.Component)components.get(i)).distribution;
            for(int j = 0; j < feature.length; j++){
                //System.out.println(mgd.p(feature[j]));
                probabilities[j][i] = mgd.p(feature[j]);
            }

        }
        //Exists so that MicroExplorer deals with same data structure for both Clustering and Reduction
        ArrayList holder = new ArrayList();
        for (double[] probability : probabilities) {
            int j = findMax(probability);
            holder.add(j);
        }
        dataResult.add(holder);
    }
    
    double EM(List<MultivariateMixture.Component> components, double[][] x , double gamma, int maxIter) {
        if (x.length < components.size() / 2)
                throw new IllegalArgumentException("Too many components");

        if (gamma < 0.0 || gamma > 0.2)
            throw new IllegalArgumentException("Invalid regularization factor gamma.");

        int n = x.length;
        int m = components.size();

        double[][] posteriori = new double[m][n];

        // Log Likelihood
        double L = 0.0;
        for (double[] xi : x) {
            double p = 0.0;
            for (MultivariateMixture.Component c : components)
                p += c.priori * c.distribution.p(xi);
            if (p > 0) L += Math.log(p);
        }

        // EM loop until convergence
        int iter = 0;
        for (; iter < maxIter; iter++) {

            // Expectation step
            for (int i = 0; i < m; i++) {
                MultivariateMixture.Component c = components.get(i);

                for (int j = 0; j < n; j++) {
                    posteriori[i][j] = c.priori * c.distribution.p(x[j]);
                }
            }

            // Normalize posteriori probability.
            for (int j = 0; j < n; j++) {
                double p = 0.0;

                for (int i = 0; i < m; i++) {
                    p += posteriori[i][j];
                }

                for (int i = 0; i < m; i++) {
                    posteriori[i][j] /= p;
                }

                // Adjust posterior probabilites based on Regularized EM algorithm.
                if (gamma > 0) {
                    for (int i = 0; i < m; i++) {
                        posteriori[i][j] *= (1 + gamma * Math.log2(posteriori[i][j]));
                        if (Double.isNaN(posteriori[i][j]) || posteriori[i][j] < 0.0) {
                            posteriori[i][j] = 0.0;
                        }
                    }
                }
            }

            // Maximization step
            ArrayList<MultivariateMixture.Component> newConfig = new ArrayList<>();
            for (int i = 0; i < m; i++)
                newConfig.add(((MultivariateGaussianDistribution)components.get(i).distribution).M(x, posteriori[i]));

            double sumAlpha = 0.0;
            for (int i = 0; i < m; i++)
                sumAlpha += newConfig.get(i).priori;

            for (int i = 0; i < m; i++)
                newConfig.get(i).priori /= sumAlpha;

            double newL = 0.0;
            for (double[] xi : x) {
                double p = 0.0;
                for (MultivariateMixture.Component c : newConfig) {
                    p += c.priori * c.distribution.p(xi);
                }
                if (p > 0) newL += Math.log(p);
            }

            if (newL > L) {
                L = newL;
                components.clear();
                components.addAll(newConfig);
            } else {
                break;
            }
        }

        return L;
    }
    
    private void split(List<MultivariateMixture.Component> mixture) {
        // Find most dispersive cluster (biggest sigma)
        MultivariateMixture.Component componentToSplit = new MultivariateMixture.Component();

        double maxSigma = 0.0;
        for (MultivariateMixture.Component c : mixture) {
            double sigma = ((MultivariateGaussianDistribution) c.distribution).scatter();
            if (sigma > maxSigma) {
                maxSigma = sigma;
                componentToSplit = c;
            }
        }

        // Splits the component
        double[][] delta = ((MultivariateGaussianDistribution) componentToSplit.distribution).cov();
        double[] mu = ((MultivariateGaussianDistribution) componentToSplit.distribution).mean();

        MultivariateMixture.Component c = new MultivariateMixture.Component();
        c.priori = componentToSplit.priori / 2;
        double[] mu1 = new double[mu.length];
        for (int i = 0; i < mu.length; i++)
            mu1[i] = mu[i] + Math.sqrt(delta[i][i])/2;
        c.distribution = new MultivariateGaussianDistribution(mu1, delta);
        mixture.add(c);

        c = new MultivariateMixture.Component();
        c.priori = componentToSplit.priori / 2;
        double[] mu2 = new double[mu.length];
        for (int i = 0; i < mu.length; i++)
            mu2[i] = mu[i] - Math.sqrt(delta[i][i])/2;
        c.distribution = new MultivariateGaussianDistribution(mu2, delta);
        mixture.add(c);

        mixture.remove(componentToSplit);
    }
}
