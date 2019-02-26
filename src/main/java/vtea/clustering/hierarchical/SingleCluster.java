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
import javax.swing.JLabel;
import javax.swing.JSpinner;
import org.scijava.plugin.Plugin;
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
    public static boolean validate = false;
    /**
     * Basic Constructor. Sets all protected variables
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
     * Constructor. Calls the super constructor and sets all protected variables
     * of the class.
     * @param max the number of objects in the volume
     */
    public SingleCluster(int max){
        super(max);
        
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implements the plugin from SMILE";
        NAME = "Single-Link Hierarchical Clustering";
        KEY = "SingleLinkHierarch";
        TYPE = "Cluster";
    }
    
    @Override
    public String getDataDescription(ArrayList params){
        return KEY + '_' + String.valueOf((Integer)((JSpinner)params.get(5)).getValue()) + '_' + getCurrentTime();
    }
    
    /**
     * Using Single-Link Hierarchical Clustering calculates the hierarchical tree.
     * @param al contains the parameters of the clustering
     * @param feature the 2D feature array
     * @return true
     */
    @Override
    public boolean process(ArrayList al, double[][] feature, boolean val){
        long start;
        progress = 0;
        int nclusters;
        double[][] proximity;
        
        ArrayList selectData = (ArrayList)al.get(1);
        boolean znorm = (boolean)al.get(0);
        feature = selectColumns(feature, selectData);
        feature = normalizeColumns(feature, znorm);
        
        dataResult.ensureCapacity(feature.length);
        JSpinner clust = (JSpinner)al.get(5);
        nclusters = ((Integer)clust.getValue());
        
        IJ.log("PROFILING: Calculating Proximity Matrix for " + feature.length + " volumes in "+ (feature[1].length - 1) + "-D space" );
        start = System.nanoTime();
        proximity = calculateProximity(feature);
        IJ.log("PROFILING: Creating Single-Link Linkage");
        SingleLinkage sl = new SingleLinkage(proximity);
        calculateClusters(sl, nclusters, NAME);
        long end = System.nanoTime();
        IJ.log("PROFILING: Single-Link Tree completed in " + (end-start)/1000000 + " ms" );

        return true;
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
        comment = comment.concat(((JSpinner)comComponents.get(5)).getValue().toString());
        comment = comment.concat("</html>");
        return comment;
    }
}
