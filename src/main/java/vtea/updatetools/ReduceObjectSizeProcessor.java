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
package vtea.updatetools;

import java.util.ArrayList;
import vtea.processor.AbstractProcessor;
import vteaobjects.MicroObject;

/**
 *
 * @author sethwinfree
 */
public class ReduceObjectSizeProcessor extends AbstractProcessor {
    
    private ArrayList<MicroObject> objects;
    private ArrayList morphologies;
    private ArrayList descriptionLabels;
    
    ReduceObjectSizeProcessor(ArrayList<MicroObject> obj, ArrayList morph,  ArrayList desc){
        objects = obj;
        morphologies = morph;
        descriptionLabels = desc;
    }

    @Override
    protected Void doInBackground() throws Exception {
        
        //get first object
        //get number of morphologies
        //generate list of unique UIDs
        //find lowest number of each UID
            //reset identical UID to this lowest number
            //record lowest number in keep list
        //generate morphologie ArrayList  
            // ArrayList for morphology: 0: method(as String), 1: channel,
            // 2: ArrayList of JComponents for method 3: UID
            // Set 0: "See labels", Set 1: -1; Set 2: null; set 3: UID.
  
        //for all objects
        //parse keep list and set derived regions NOT @ keeplist to null
        
        
        
        
        return null;
    }

    @Override
    public int process(ArrayList al, String... str) {
        return 1;
    }

    @Override
    public String getChange() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
