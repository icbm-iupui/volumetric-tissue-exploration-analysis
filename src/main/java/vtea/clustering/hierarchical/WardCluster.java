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
package vtea.clustering.hierarchical;

import ij.IJ;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import smile.clustering.linkage.WardLinkage;
import org.scijava.plugin.Plugin;
import vtea.featureprocessing.FeatureProcessing;

/**
 *
 * @author drewmcnutt
 */
@Plugin (type = FeatureProcessing.class)
public class WardCluster extends AbstractHierarchical{
    
    public WardCluster(){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implements the plugin from SMILE";
        NAME = "Ward Hierarchical Clustering";
        KEY = "WardHierarchicalClustering";
        TYPE = "Cluster";
    }
    
    public WardCluster(int max){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implements the plugin from SMILE";
        NAME = "Ward Hierarchical Clustering";
        KEY = "WardHierarchicalClustering";
        TYPE = "Cluster";
        
        protocol = new ArrayList();

        protocol.add(new JLabel("Amount of clusters:"));

        protocol.add(new JSpinner(new SpinnerNumberModel(5,0,max,1)));
    }
    
    /*Calculates the proximity matrix of the features and using Ward Hierarchical Clustering returns true when complete*/
    @Override
    public boolean process(ArrayList al, double[][] feature){
        dataResult.ensureCapacity(feature.length);
        progress = 0;
        int nclusters;
        double[][] proximity;
        
        JSpinner clust = (JSpinner)al.get(3);
        nclusters = ((Integer)clust.getValue());
        IJ.log("PROFILING: Calculating Proximity Matrix for " + feature.length + " volumes in "+ feature[1].length + "-D space" );
        proximity = calculateProximity(feature);
        IJ.log("PROFILING: Creating Ward Linkage");
        WardLinkage wl = new WardLinkage(proximity);
        calculateClusters(wl, nclusters);
        
        return true;
    }
    
    
    @Override
    public void sendProgressComment(){
        
    }

}
