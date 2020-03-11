/* 
 * Copyright (C) 2020 Indiana University
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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import smile.clustering.linkage.WardLinkage;
import org.scijava.plugin.Plugin;
import smile.clustering.linkage.CompleteLinkage;
import vtea.featureprocessing.FeatureProcessing;

/**
 * Hierarchical Clustering using Ward. After merging two clusters the
 * new cluster is given a dissimilarity value based on the error sum of squares
 * between elements of this cluster with another cluster.
 * @author drewmcnutt
 */
@Plugin (type = FeatureProcessing.class)
public class WardCluster extends AbstractHierarchical{
    
    public static boolean validate = true;
    /**
     * Basic Constructor. Sets all protected variables
     */
    public WardCluster(){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implements the plugin from SMILE";
        NAME = "Ward Hierarchical Clustering";
        KEY = "WardHierarch";
    }
    
   /**
     * Constructor. Calls the super constructor and sets all protected variables
     * of the class.
     * @param max the number of objects in the volume
     */
    public WardCluster(int max){
        super(max);
        
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implements the plugin from SMILE";
        NAME = "Ward Hierarchical Clustering";
        KEY = "WardHierarchicalClustering";
        TYPE = "Cluster";
    }
    
    @Override
    public String getDataDescription(ArrayList params){
        return KEY + '_' + String.valueOf((Integer)((JSpinner)params.get(5)).getValue()) + '_' + getCurrentTime();
    }
    
    /**
     * Calculates the proximity matrix of the features and using 
     * Ward Hierarchical Clustering returns true when complete
     * @return true when complete
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
        
        if(val){
            start = System.nanoTime();
            IJ.log("VALIDATING: Beginning Python");
            start = System.nanoTime();
            rt = Runtime.getRuntime();


            performValidation(feature, nclusters, 1);

            long end = System.nanoTime();
            IJ.log("VALIDATING: Completed Python in " + (end-start)/1000000000 + " seconds" );

            start = System.nanoTime();
            IJ.log("PROFILING: Calculating Proximity Matrix for " + feature.length + " volumes in "+ (feature[1].length) + "-D space" );
            proximity = calculateProximity(feature);
            IJ.log("PROFILING: Creating Ward Linkage");
            WardLinkage cl = new WardLinkage(proximity);
            calculateClusters(cl, nclusters, NAME);
        }else{
            start = System.nanoTime();
            IJ.log("PROFILING: Calculating Proximity Matrix for " + feature.length + " volumes in "+ (feature[1].length) + "-D space" );
            proximity = calculateProximity(feature);
            IJ.log("PROFILING: Creating Ward Linkage");
            WardLinkage cl = new WardLinkage(proximity);
            calculateClusters(cl, nclusters, NAME);
        }
        long end = System.nanoTime();
        IJ.log("PROFILING: Ward Tree completed in " + (end-start)/1000000 + " ms" );
//        IJ.log("PROFILING: Calculating Proximity Matrix for " + feature.length + " volumes in "+ (feature[1].length - 1) + "-D space" );
//        
//        proximity = calculateProximity(feature);
//        IJ.log("PROFILING: Creating Ward Linkage");
//        WardLinkage wl = new WardLinkage(proximity);
//        calculateClusters(wl, nclusters, NAME);
        
        return true;
    }
    
    private void performValidation(double[][] matrix, int n_clust, int seed){
        makeMatrixCSVFile(matrix);
        String s = getPython();
        String pathAddOn = String.format("%1$csrc%1$cmain%1$cresources%1$cvalidation_script.py", File.separatorChar);
        String validationScript = getCWD() + pathAddOn;
        try{
            String[] validationGen = new String[]{s, validationScript, "matrix_for_python.csv", "WARD", String.valueOf(seed), String.valueOf(n_clust)};
            Process p = rt.exec(validationGen);
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            p.waitFor();
            
            String e = null;
            while((e = stdError.readLine()) != null)
                System.out.println(e);
        }catch(IOException | InterruptedException ie){
            ie.printStackTrace();
        }
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
