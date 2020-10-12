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

import vtea.objects.neighborhoodmeasurements.*;
import java.util.ArrayList;
import java.util.HashMap;
import vteaobjects.MicroObject;

/**
 *
 * @author sethwinfree
 */
public abstract class AbstractNeighborhoodMeasurement implements NeighborhoodMeasurements {
    protected String VERSION = "0.0";
    protected String AUTHOR = "VTEA Developer";
    protected String COMMENT = "New functionality";
    protected String NAME = "ND";
    protected String KEY = "ND";
    protected String TYPE = "Abstract";
       

    /**
     *
     * @param objects
     * @param classes
     * @param values
     * @return
     */
    
    @Override
    public ArrayList<Double> process(ArrayList<MicroObject> objects, ArrayList classes, HashMap<String, String> values) {
        return new ArrayList<Double>();
     }



    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public String getVersion() {
        return VERSION;
    }

    public String getAuthor() {
        return AUTHOR;
    }

    public String getComment() {
        return COMMENT;
    }

    public String getType() {
        return TYPE;
    }
    
}
