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
public class ClassSums extends AbstractNeighborhoodMeasurement {

    public ClassSums() {
        VERSION = "1.0";
        AUTHOR = "Seth Winfree";
        COMMENT = "Class Sums";
        NAME = "Class Sums";
        KEY = "ClassSums";
        TYPE = "Tally";
    }

    @Override
    public ArrayList<Double> process(ArrayList<MicroObject> objects, ArrayList classes, HashMap<String, String> values) {
        
        ArrayList<Double> results = new ArrayList<>();
        
        double[] result = new double[classes.size()];

        for(int j = 0; j < result.length; j++){
            result[j] = 0;
        }
        
        for(int i = 0; i < objects.size(); i++){  
              MicroObject o = (MicroObject)objects.get(i);
            String a = values.get(String.valueOf(o.getSerialID()));
            double b = Double.parseDouble(a);
            int d = (int)b;
            result[d]++;
        }   

        
        for(int k = 0; k < result.length; k++){
            results.add(result[k]);
        }
        
      return results;  
        
    }
    

}
