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
package vtea.reduction;

import com.jujutsu.tsne.TSneConfiguration;
import ij.IJ;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.scijava.plugin.Plugin;
import com.jujutsu.tsne.barneshut.BHTSne;
import com.jujutsu.tsne.barneshut.BarnesHutTSne;
import com.jujutsu.utils.TSneUtils;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import vtea.featureprocessing.AbstractFeatureProcessing;
import vtea.featureprocessing.FeatureProcessing;

/**
 *
 * @author drewmcnutt
 */
@Plugin (type = FeatureProcessing.class)
public class TSNEReduction extends AbstractFeatureProcessing{
    
    public TSNEReduction(){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implements the plugin from SMILE";
        NAME = "tSNE";
        KEY = "tSNE";
        TYPE = "Reduction";
    }
    
    public TSNEReduction(int n){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implements the plugin from SMILE";
        NAME = "tSNE";
        KEY = "tSNE";
        TYPE = "Reduction";
        
        protocol = new ArrayList();
        
        protocol.add(new JLabel("New Dimension"));
        
        protocol.add(new JTextField("2",2));
        
        protocol.add( new JLabel("Iterations"));
        
        protocol.add( new JTextField("1000",1000));
        
        //protocol.add(new JLabel("Learning Rate"));
        
        //protocol.add(new JTextField("200",200));
        
        protocol.add(new JLabel("Perplexity"));
        
        protocol.add(new JTextField("20",20));
        
        protocol.add(new JLabel("PCA Preprocessing"));
        
        protocol.add(new JCheckBox());
    }
    
    @Override
    public boolean process(ArrayList al, double[][] feature){
        //double[][] dissimilarity;
        double[][] results;
        
        //dissimilarity = calculateProximity(feature);
        int dim = Integer.parseInt(((JTextField)al.get(4)).getText());
        int itr = Integer.parseInt(((JTextField)al.get(6)).getText());
        int perpl = Integer.parseInt(((JTextField)al.get(8)).getText());
        boolean pca = ((JCheckBox)al.get(10)).isSelected();
        
        ArrayList selectData = (ArrayList)al.get(0);
        feature = selectColumns(feature, selectData);
        
        double[][] fortsne = new double[feature.length][feature[0].length-1];
        int i = 0;
        for(double[] r: feature){
            for(int j = 1; j < r.length; j++){
                fortsne[i][j-1] = feature[i][j];
            }
            i++;
        }
        
        IJ.log("PROFILING: Training tSNE for " + itr + " iterations");
        long start = System.nanoTime();
        BarnesHutTSne tsne = new BHTSne();
        TSneConfiguration config = TSneUtils.buildConfig(fortsne,dim,fortsne[0].length,perpl,itr, pca, 0.5, false);
        double[][] Y = tsne.tsne(config);
        
        
        IJ.log("PROFILING: Extracting results");
        
        for(i = 0; i < Y.length; i++){
            ArrayList obj = new ArrayList();
            for(int j = 0; j < Y[i].length; j++){
                obj.add(Y[i][j]);
            }
            dataResult.add(obj);
        }
        
        long end = System.nanoTime();
        IJ.log("PROFILING: tSNE completed in " + (end-start)/1000000 + " ms" );
        return true;
    }
    
//    private double[][] calculateProximity(double[][] feature){
//        int n = feature.length;
//        int feat = feature[0].length;
//        
//        double[][] proximity = new double[n][n];
//        
//        int i = 0;
//        for(double[] sample : feature){
//            for(int j = 0; j <= i; j++){
//                if(i == j){
//                    proximity[i][j] = 0;
//                    continue;
//                }
//                double d = calcDistance(feat,sample,feature[j]);
//                proximity[i][j] = d;
//                proximity[j][i] = d;
//            }
//            i++;
//            //System.out.println(i + "objects done");
//        }
//        return proximity;
//    }
//    
//    /*Calculates the distance between two objects based on the amount of
//    dimensions given as dim*/
//    private double calcDistance(int dim, double[] n, double[] m){
//        double d = 0;
//        for(int i = 1; i < dim; i++){
//            double val = n[i] - m[i];
//            d += val * val;
//        }
//        d = Math.sqrt(d);
//        return d;
//    }
}
