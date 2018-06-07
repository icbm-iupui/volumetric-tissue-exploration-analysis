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
import javax.swing.JLabel;
import javax.swing.JTextField;
import smile.clustering.HierarchicalClustering;
import smile.clustering.linkage.WardLinkage;
import vtea.featureprocessing.AbstractFeatureProcessing;
import org.scijava.plugin.Plugin;
import vtea.featureprocessing.FeatureProcessing;

/**
 *
 * @author drewmcnutt
 */
@Plugin (type = FeatureProcessing.class)
public class WardCluster extends AbstractFeatureProcessing{
    
    public WardCluster(){
        VERSION = "0.1";
        AUTHOR = "Drew McNutt";
        COMMENT = "Implements the plugin from SMILE";
        NAME = "Ward Hierarchical Clustering";
        KEY = "WardHierarchicalClustering";
        TYPE = "Cluster";
        
        protocol = new ArrayList();

        protocol.add(new JLabel("Amount of clusters:"));
        
        protocol.add(new JTextField("5", 5));
    }
    
    @Override
    public boolean process(ArrayList al, double[][] feature){
        int nclusters;
        double[][] proximity;
        int[] membership;
        
        proximity = calculateProximity(feature);
        nclusters = (int)al.get(1);
        WardLinkage wl = new WardLinkage(proximity);
        HierarchicalClustering hc = new HierarchicalClustering(wl);
        membership = hc.partition(nclusters);
        
        for(int i = 0; i < membership.length; i++){
            dataResult[i] = (double)membership[i];
        }
        
        return true;
    }
    
    
    /*Calculates the proximity matrix based on all of the features of the 
    objects*/
    
    private double[][] calculateProximity(double[][] feature){
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
        return proximity;
        
    }
    
    /*Calculates the distance between two objects based on the amount of
    dimensions given as dim*/
    
    private double calcDistance(int dim, double[] n, double[] m){
        double d = 0;
        for(int i = 1; i < dim; i++){
            double val = n[i] - m[i];
            d += val * val;
        }
        d = Math.sqrt(d);
        return d;
    }


}
