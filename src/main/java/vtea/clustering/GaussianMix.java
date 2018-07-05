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

import ij.IJ;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.scijava.plugin.Plugin;
import smile.stat.distribution.MultivariateGaussianDistribution;
import smile.stat.distribution.MultivariateGaussianMixture;
import smile.stat.distribution.MultivariateMixture;
import smile.math.Math;
import vtea.featureprocessing.AbstractFeatureProcessing;
import vtea.featureprocessing.FeatureProcessing;

/**
 *
 * @author drewmcnutt
 */
@Plugin (type = FeatureProcessing.class)
public class GaussianMix extends AbstractFeatureProcessing{
    List<MultivariateMixture.Component> components;
    
    public GaussianMix(){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implements the plugin from SMILE";
        NAME = "Gaussian Mixture";
        KEY = "GaussianMixture";
        TYPE = "Cluster";
    }
    
    public GaussianMix(int max){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Gaussian Mixture";
        NAME = "Gaussian Mixture";
        KEY = "GaussianMixture";
        TYPE = "Cluster";
        
        protocol = new ArrayList();
        
        protocol.add(new JLabel("Number of clusters"));
       
        JSpinner n_clust = new JSpinner(new SpinnerNumberModel(5,2,max,1));
        protocol.add(n_clust);
        JCheckBox auto = new JCheckBox("Select Automatically");
        auto.addItemListener(new ItemListener(){
                @Override
                public void itemStateChanged(ItemEvent evt) {
                    if(auto.isSelected())
                        n_clust.setEnabled(false);
                    else
                        n_clust.setEnabled(true);
                }
        });
        protocol.add(auto);
    }
    
    @Override
    public boolean process(ArrayList al, double[][] feature){
        int n_clust;
        MultivariateGaussianMixture mgm;
        JCheckBox auto_clust;
        long start;
        
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
            IJ.log("PROFILING: Finding Gaussian Mixture Model for " + n_clust + " clusters on " + feature[0].length + " features");
            start = System.nanoTime();
            run(feature, n_clust, 5);       //last value is the random seed
            
        }
        else{
            start = System.nanoTime();
            IJ.log("PROFILING: Finding Gaussian Mixture Model on " + feature[0].length + " features with lowest BIC");
            run(feature, 5);
        }
        IJ.log("PROFILING: Extracting membership of clusters");
        getMembership(feature);
        long end = System.nanoTime();
        IJ.log("PROFILING: Gaussian Mixture Model completed in " + (end-start)/1000000 + " ms" );
        return true;
    }
    
    private void run(double[][] data, int k, int randomSeed){
        if (k < 2)
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
        Random randGen = new Random(randomSeed);
        
        double[] centroid = data[randGen.nextInt(n)];
        MultivariateMixture.Component c = new MultivariateMixture.Component();
        c.priori = 1.0 / k;
        MultivariateGaussianDistribution gaussian = new MultivariateGaussianDistribution(centroid, sigma);
        c.distribution = gaussian;
        components.add(c);

        // We use a the kmeans++ algorithm to find the initial centers.
        // Initially, all components have same covariance matrix.
        double[] D = new double[n];
        for (int i = 0; i < n; i++) {
            D[i] = Double.MAX_VALUE;
        }

        // pick the next center
        for (int i = 1; i < k; i++) {
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
            c = new MultivariateMixture.Component();
            c.priori = 1.0 / k;
            gaussian = new MultivariateGaussianDistribution(centroid, sigma);
            c.distribution = gaussian;
            components.add(c);
        }

        EM(components, data, 0.2, Integer.MAX_VALUE);
    }
    
    private void run(double[][] data, int randomSeed){
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

        double bic = 0.0;
        for (double[] x : data) {
            double p = c.distribution.p(x);
            if (p > 0) bic += Math.log(p);
        }
        bic -= 0.5 * freedom * Math.log(data.length);

        double b = Double.NEGATIVE_INFINITY;
        while (bic > b) {
            b = bic;
            components = (ArrayList<MultivariateMixture.Component>) mixture.clone();

            split(mixture);
            bic = EM(mixture, data, 0.2, Integer.MAX_VALUE);

            freedom = 0;
            for (int i = 0; i < mixture.size(); i++)
                freedom += mixture.get(i).distribution.npara();

            bic -= 0.5 * freedom * Math.log(data.length);
        }

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
   
    /**
     * Calculates BIC. The formula for BIC is given as BIC = MaximumLogLikelihood - 0.5 * number_of_parameters * log(number_of_objects).
     * This form of the BIC must be maximized.
     * @param gmm the model to calculate BIC for
     * @param feature
     * @return 
     */
    private double getBic(double[][] feature){
            int freedom = 0;
            double newbic = 0.0;
            
            for(Object component: components){
                freedom += ((MultivariateMixture.Component)component).distribution.npara();
            }
            
            for(Object component: components){
                for (double[] x : feature) {
                    double p = ((MultivariateMixture.Component)component).distribution.p(x);
                    if (p > 0) newbic += Math.log(p);
                }
            }
            
            newbic -= 0.5 * freedom * Math.log(feature.length);
            
            return newbic;
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
        for (double[] probability : probabilities) {
            int j = findMax(probability);
            dataResult.add(j);
        }
    }

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
