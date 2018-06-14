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
import org.scijava.plugin.Plugin;
import smile.clustering.HierarchicalClustering;
import smile.clustering.linkage.SingleLinkage;
import vtea.featureprocessing.FeatureProcessing;

/**
 *
 * @author drewmcnutt
 */
@Plugin (type= FeatureProcessing.class)
public class SingleCluster extends AbstractHierarchical{
    
    public SingleCluster(){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implements the plugin from SMILE";
        NAME = "Single-Link Hierarchical Clustering";
        KEY = "SingleLinkHierarchicalClustering";
        TYPE = "Cluster";
    }
    
    public SingleCluster(int max){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implements the plugin from SMILE";
        NAME = "Single-Link Hierarchical Clustering";
        KEY = "SingleLinkHierarchicalClustering";
        TYPE = "Cluster";
        
        protocol = new ArrayList();

        protocol.add(new JLabel("Amount of clusters:"));

        protocol.add(new JSpinner(new SpinnerNumberModel(5,0,max,1)));
    }
    
    /*Calculates the proximity matrix of the features and using Single-Link Hierarchical Clustering returns true when complete*/
    @Override
    public boolean process(ArrayList al, double[][] feature){
        progress = 0;
        int nclusters;
        double[][] proximity;
        
        ArrayList selectData = (ArrayList)al.get(0);
        feature = selectColumns(feature, selectData);
        
        dataResult.ensureCapacity(feature.length);
        JSpinner clust = (JSpinner)al.get(4);
        nclusters = ((Integer)clust.getValue());
        
        IJ.log("PROFILING: Calculating Proximity Matrix for " + feature[1].length + " features");
        proximity = calculateProximity(feature);
        IJ.log("PROFILING: Creating Single-Link Linkage");
        SingleLinkage sl = new SingleLinkage(proximity);
        calculateClusters(sl, nclusters);

        return true;
    }
}
