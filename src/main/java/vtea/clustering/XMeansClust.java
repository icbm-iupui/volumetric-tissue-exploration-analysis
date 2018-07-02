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
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.scijava.plugin.Plugin;
import vtea.featureprocessing.AbstractFeatureProcessing;
import vtea.featureprocessing.FeatureProcessing;
import smile.clustering.XMeans;

/**
 *
 * @author drewmcnutt
 */
@Plugin (type = FeatureProcessing.class)
public class XMeansClust extends AbstractFeatureProcessing{
    
    public XMeansClust(){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implements the plugin from SMILE";
        NAME = "X-means Clustering";
        KEY = "Xmeans";
        TYPE = "Cluster";
    }
    
    public XMeansClust(int max){
        this();
        
        protocol = new ArrayList();
        
        protocol.add(new JLabel("Maximum number of clusters"));
        protocol.add(new JSpinner(new SpinnerNumberModel(5,2,max,1)));
    }
    
    @Override
    public boolean process(ArrayList al, double[][] feature){
        int maxClust;
        int[] membership;
        
        ArrayList selectData = (ArrayList)al.get(1);
        boolean znorm = (boolean)al.get(0);
        feature = selectColumns(feature, selectData);
        feature = normalizeColumns(feature, znorm);
        dataResult.ensureCapacity(feature.length);
        
        JSpinner clust = (JSpinner)al.get(5);
        maxClust = ((Integer)clust.getValue());
        
        IJ.log("Clustering using XMeans for a maximum of " + maxClust + " clusters");
        long start = System.currentTimeMillis();
        XMeans xm = new XMeans(feature, maxClust);
        membership = xm.getClusterLabel();
        IJ.log("Clustering completed in " + (System.currentTimeMillis()-start)/1000 + " s, " + xm.getNumClusters() + " clusters found");
        
        for(int m: membership)
            dataResult.add(m);
        
        return true;
    }
    
    public static String getBlockComment(ArrayList comComponents){
        String comment = "<html>";
        comment = comment.concat(((JLabel)comComponents.get(4)).getText() + ": ");
        comment = comment.concat(((JSpinner)comComponents.get(5)).getValue().toString());
        comment = comment.concat("</html>");
        return comment;
    }
}