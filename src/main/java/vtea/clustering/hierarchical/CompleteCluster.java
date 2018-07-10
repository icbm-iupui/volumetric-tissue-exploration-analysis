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
import org.scijava.plugin.Plugin;
import smile.clustering.linkage.CompleteLinkage;
import vtea.featureprocessing.FeatureProcessing;

/**
 * Hierarchical Clustering using Complete-Link. After merging two clusters the
 * new cluster is given a dissimilarity value based on the maximum distance
 * between elements of this cluster with another cluster.
 * @author drewmcnutt
 */
@Plugin (type =FeatureProcessing.class)
public class CompleteCluster extends AbstractHierarchical{
    
    /**
     * Basic Constructor. Sets all protected variables
     */
    public CompleteCluster(){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implements the plugin from SMILE";
        NAME = "Complete-Link Hierarchical Clustering";
        KEY = "CompleteLinkHierarchicalClustering";
        TYPE = "Cluster";
    }
    
    /**
     * Constructor. Calls the super constructor and sets all protected variables
     * of the class.
     * @param max the number of objects in the volume
     */
    public CompleteCluster(int max){
        super(max);
        
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implements the plugin from SMILE";
        NAME = "Complete-Link Hierarchical Clustering";
        KEY = "CompleteLinkHierarchicalClustering";
        TYPE = "Cluster";
    }
    
    /**
     * Calculates the hierarchical tree using Complete-Link Clustering.
     * @param al holds the settings for the clustering
     * @param feature the feature array
     * @return true.
     */
    @Override
    public boolean process(ArrayList al, double[][] feature){
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
        
        IJ.log("PROFILING: Calculating Proximity Matrix for " + feature[1].length + " features");
        proximity = calculateProximity(feature);
        IJ.log("PROFILING: Creating Complete-Link Linkage");
        CompleteLinkage cl = new CompleteLinkage(proximity);
        calculateClusters(cl, nclusters);

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
