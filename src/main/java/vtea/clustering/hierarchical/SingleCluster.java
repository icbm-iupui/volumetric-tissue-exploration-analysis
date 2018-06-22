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
 * Hierarchical Clustering using Single-Link. After merging two clusters the
 * new cluster is given a dissimilarity value based on the minimum distance
 * between elements of this cluster with another cluster.
 * @author drewmcnutt
 */
@Plugin (type= FeatureProcessing.class)
public class SingleCluster extends AbstractHierarchical{
    
    /**
     * Constructor. Sets basic information of the class.
     */
    public SingleCluster(){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implements the plugin from SMILE";
        NAME = "Single-Link Hierarchical Clustering";
        KEY = "SingleLinkHierarchicalClustering";
        TYPE = "Cluster";
    }
    
    /**
     * Constructor. Calls the super constructor and sets basic information of
     * the class.
     * @param max the number of objects in the volume
     */
    public SingleCluster(int max){
        super(max);
        
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implements the plugin from SMILE";
        NAME = "Single-Link Hierarchical Clustering";
        KEY = "SingleLinkHierarchicalClustering";
        TYPE = "Cluster";
    }
    
    /**
     * Using Single-Link Hierarchical Clustering calculates the hierarchical tree.
     * @param al contains the parameters of the clustering
     * @param feature the 2D feature array
     * @return true
     */
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
