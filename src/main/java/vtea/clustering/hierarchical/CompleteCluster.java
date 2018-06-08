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

import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.scijava.plugin.Plugin;
import smile.clustering.linkage.CompleteLinkage;
import vtea.featureprocessing.FeatureProcessing;

/**
 *
 * @author drewmcnutt
 */
@Plugin (type =FeatureProcessing.class)
public class CompleteCluster extends AbstractHierarchical{
    
    public CompleteCluster(){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implements the plugin from SMILE";
        NAME = "Complete Link Hierarchical Clustering";
        KEY = "CompleteLinkHierarchicalClustering";
        TYPE = "Cluster";
        
        protocol = new ArrayList();

        protocol.add(new JLabel("Amount of clusters:"));

        protocol.add(new JTextField("5", 5));
    
    }
    
    /*Calculates the proximity matrix of the features and using Complete-Link Hierarchical Clustering returns true when complete*/
    @Override
    public boolean process(ArrayList al, double[][] feature){
        progress = 0;
        int nclusters;
        double[][] proximity;

        
        dataResult.ensureCapacity(feature.length);
        JTextField clust = (JTextField)al.get(3);
        nclusters = Integer.parseInt(clust.getText());
        
        proximity = calculateProximity(feature);
        CompleteLinkage cl = new CompleteLinkage(proximity);
        calculateClusters(cl, nclusters);

        return true;
    }
    
}
