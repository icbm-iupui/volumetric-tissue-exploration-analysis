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
package vtea.clustering.hierarchical;

import ij.IJ;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import smile.clustering.HierarchicalClustering;
import smile.clustering.linkage.Linkage;
import vtea.featureprocessing.AbstractFeatureProcessing;
import smile.plot.Dendrogram;
import smile.plot.PlotCanvas;

/**
 * Clusters using Hierarchical clustering with ambiguous linkage method. 
 * @author drewmcnutt
 */
public abstract class AbstractHierarchical extends AbstractFeatureProcessing{
    
    /**
     * Basic Constructor. Sets all protected variables
     */
    public AbstractHierarchical(){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Abstract Class";
        NAME = "Abstract of Hierarchical Clustering";
        KEY = "AbstractHierarchicalClustering";
        TYPE = "Cluster";
    }
    
    /**
     * Constructor called for initialization of Setup GUI.
     * When components are added to this, the static method must be altered.
     * @param max the number of objects segmented in the volume
     */
    public AbstractHierarchical(int max){
        this();
        
        protocol = new ArrayList();

        protocol.add(new JLabel("Amount of clusters"));
        protocol.add(new JSpinner(new SpinnerNumberModel(5,0,max,1)));
        
    }
    
    /**
     * Calculates the membership of all the segmented objects
     * @param l linkage method for merging of clusters
     * @param n number of clusters
     */
    protected void calculateClusters(Linkage l, int n){
        int[] membership;
        IJ.log("PROFILING: Creating Hierarchical Tree");
        HierarchicalClustering hc = new HierarchicalClustering(l);
        
        IJ.log("PROFILING: Finding membership of volumes in " + n + " clusters");
        membership = hc.partition(n);
        
        for(int i = 0; i < membership.length; i++){
            dataResult.add(membership[i] * 1.0);
        }
        createDendrogram(hc);
        
    }
    
    /**
     * Calculates the proximity matrix of the objects. Using Euclidean space
     * @param feature feature array(rows are objects and columns are features)
     * @return proximity matrix
     */
    protected double[][] calculateProximity(double[][] feature){
        int n = feature.length; 
        int feat = feature[0].length;   
        
        double[][] proximity = new double[n][n];
        
        int i = 0;
        for(double[] sample : feature){
            for(int j = 0; j <= i; j++){
                if(i == j){
                    proximity[i][j] = 0;
                    continue;
                }
                double d = calcDistance(feat,sample,feature[j]);
                proximity[i][j] = d;
                proximity[j][i] = d;
            }
            i++;
        }
        //System.out.println("finished proximity");
        return proximity;
        
    }
    
    /**
     * Calculates the distance between two objects.
     * @param dim dimension in which to calculate distance
     * @param n object1
     * @param m object2
     * @return 
     */
    private double calcDistance(int dim, double[] n, double[] m){
        double d = 0;
        for(int i = 0; i < dim; i++){   //starting at i=1 does not include the ID in the calculation
            double val = n[i] - m[i];
            d += val * val;
        }
        d = Math.sqrt(d);
        return d;
    }
    
    /**
     * Constructs a JFrame containing the dendrogram of the hierarchical 
     * clustering.
     * @param hc the hierarchical clustering to make the dendrogram with
     */
    public void createDendrogram(HierarchicalClustering hc){
        JFrame dendroFrame = new JFrame();
        dendroFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dendroFrame.setVisible(false);
        dendroFrame.setSize(500, 1000);
        PlotCanvas plot = Dendrogram.plot(hc.getTree(), hc.getHeight());
        plot.setAxisLabel(0, "");
        plot.setAxisLabel(1, "Height");
        plot.setTitle("Dendrogram");
        dendroFrame.getContentPane().add(plot);
        dendroFrame.setVisible(true);
    }
}
