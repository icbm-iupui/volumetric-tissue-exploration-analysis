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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.scijava.plugin.Plugin;
import smile.stat.distribution.MultivariateGaussianDistribution;
import smile.stat.distribution.MultivariateGaussianMixture;
import smile.stat.distribution.MultivariateMixture;
import vtea.featureprocessing.AbstractFeatureProcessing;
import vtea.featureprocessing.FeatureProcessing;

/**
 *
 * @author drewmcnutt
 */
@Plugin (type = FeatureProcessing.class)
public class GaussianMix extends AbstractFeatureProcessing{
    
    public GaussianMix(){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Gaussian Mixture";
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
        
        ArrayList selectData = (ArrayList)al.get(0);
        feature = selectColumns(feature, selectData);
        dataResult.ensureCapacity(feature.length);
        
        auto_clust = (JCheckBox)al.get(5);
        if(!auto_clust.isSelected()){
            JSpinner clust = (JSpinner)al.get(4);
            n_clust = ((Integer)clust.getValue()); 
            IJ.log("PROFILING: Finding Gaussian Mixture Model for " + n_clust + " clusters on " + feature[0].length + " features");
            start = System.nanoTime();
            mgm = new MultivariateGaussianMixture(feature, n_clust);
            
        }
        else{
            //Larger BIC is prefered
//            n_clust = 2;
//            double bic;
//            double b = Double.NEGATIVE_INFINITY;
//            start = System.nanoTime();
//            IJ.log("PROFILING: Finding Gaussian Mixture Model on " + feature[0].length + " features with lowest BIC");
//            mgm = new MultivariateGaussianMixture(feature, n_clust);
//            bic = getBic(mgm, feature);
//            System.out.printf("%d guassian distributions with a BIC of %f%n", n_clust, bic);
//            while(n_clust < 20){
//                n_clust++;
//                b = bic;
//                mgm = new MultivariateGaussianMixture(feature, n_clust);
//                bic = getBic(mgm,feature);
//                System.out.printf("%d guassian distributions with a BIC of %f%n", n_clust, bic);
//            }
//            IJ.log("PROFILING: Lowest BIC is " + bic);
            start = System.nanoTime();
            IJ.log("PROFILING: Finding Gaussian Mixture Model on " + feature[0].length + " features with lowest BIC");
            mgm = new MultivariateGaussianMixture(feature);
        }
        IJ.log("PROFILING: Extracting membership of clusters");
        getMembership(mgm,feature);
        long end = System.nanoTime();
        IJ.log("PROFILING: Gaussian Mixture Model completed in " + (end-start)/1000000 + " ms" );
        return true;
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
    private double getBic(MultivariateGaussianMixture gmm,double[][] feature){
            List components = gmm.getComponents();
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
    
    private void getMembership(MultivariateGaussianMixture mgm, double[][] feature){
        List components = mgm.getComponents();
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
    
    @Override
    public ArrayList getBlockCommentLocation(){
        ArrayList commentLocat =  new ArrayList();
        
        commentLocat.add(3);    //3 for JCheckBox
        commentLocat.add(2);    //Location in ArrayList
        commentLocat.add(false);    //keep going through list if enabled
        commentLocat.add(0);    //0 for JLabel
        commentLocat.add(0);    //Location in ArrayList
        commentLocat.add(1);    //1 for JSpinner
        commentLocat.add(1);    //Location in ArrayList
        
        return commentLocat;
    }

}
