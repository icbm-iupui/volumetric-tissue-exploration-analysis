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
package vtea.objects.neighborhoodmeasurements;

import java.util.ArrayList;
import java.util.HashMap;
import org.scijava.plugin.Plugin;
import vteaobjects.MicroObject;

/**
 *
 * @author sethwinfree
 */
@Plugin(type = NeighborhoodMeasurements.class)
public class ClassFraction extends AbstractNeighborhoodMeasurement {


    public ClassFraction() {
        VERSION = "1.0";
        AUTHOR = "Seth Winfree";
        COMMENT = "ClassFraction";
        NAME = "Class Fraction";
        KEY = "ClassFraction";
        TYPE = "Tally";
    }



    @Override
    public ArrayList<Double> process(ArrayList<MicroObject> objects, ArrayList classes, HashMap<String, String> values) {
        
 
        
        ArrayList<Double> results = new ArrayList<>();
        
        double[] result = new double[classes.size()];
        int count = 0;
        
        //zero out array
        
        for(int j = 0; j < result.length; j++){
            result[j] = 0;
        }
        
        //add counts by object
        for(int i = 0; i < objects.size(); i++){ 
            MicroObject o = (MicroObject)objects.get(i);
            String a = values.get(String.valueOf(o.getSerialID()));
            double b = Double.parseDouble(a);
            int d = (int)b;

            //System.out.println("PROFILING: getting object: " +o.getSerialID()  
            //        + ", class(Double) " +Double.parseDouble(a)
            //        + ", class(int) " + d);
         
            result[d]++;
            count++;

           
        }
        
        //make arraylist
        for(int k = 0; k < result.length; k++){
           results.add(100*(result[k]/count));
        }
        
//      String str = "PROFILING: class-output: ";
//            for(int l = 0; l < result.length; l++)
//            {
//            str = str + ", " + result[l];
//                    }
//             System.out.println(str);
        
      return results;  
        
    }
    
}
