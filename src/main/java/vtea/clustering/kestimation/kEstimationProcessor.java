/*
 * Copyright (C) 2022 SciJava
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
package vtea.clustering.kestimation;

import ij.IJ;
import java.util.ArrayList;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import org.jfree.data.xy.XYSeries;
import smile.clustering.KMeans;
import vtea.featureprocessing.AbstractFeatureProcessing;


/**
 *
 * @author Seth
 */
public class kEstimationProcessor extends AbstractFeatureProcessing {
         
public kEstimationProcessor(){
  
}

@Override
public boolean process(ArrayList al, double[][] feature, boolean val){
        
    int numTrials = 100;
    
        XYSeries distortion = new XYSeries("k-Estimation");
        ArrayList selectData = (ArrayList) al.get(1);
        boolean znorm = (boolean) al.get(0);
        double[][] result = selectColumns(feature, selectData);
        result = normalizeColumns(result, znorm);
   
        long start = System.currentTimeMillis();
        for(int i = 2; i <= 30; i++){
        IJ.log(String.format("PROFILING: k-Estimation using elbow method for a maximum of %d clusters", i));
        
        KMeans km = new KMeans(result, i, numTrials);
        distortion.add(i,km.distortion());
        }
        long finish = System.currentTimeMillis();
        
        IJ.log(String.format("PROFILING: k-Estimation calculated in %d milliseconds", finish-start));
        
        XYPlotkEstimation plot = new XYPlotkEstimation("k-Estimation (Elbow Plot)", 
                 "k", "Distortion", distortion);
        

        
        plot.pack();
        plot.setVisible(true);
        
        return true;
}
}
