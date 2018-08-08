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
package vtea.objects.measurements;

import java.util.ArrayList;
import java.util.ListIterator;
import net.imglib2.RealPoint;
import org.scijava.plugin.Plugin;
import vtea.processor.Processor;

/**
 *
 * @author sethwinfree
 */
@Plugin(type = Measurements.class)
public class Mean extends AbstractMeasurement {
    
    public Mean(){
    VERSION = "1.0";
    AUTHOR = "Seth Winfree";
    COMMENT = "Calculate mean";
    NAME = "Mean";
    KEY = "Mean";
    }

    @Override
    public Number process(ArrayList al, ArrayList values) {
        
        double n = 0;
        ListIterator<Number> itr = values.listIterator();   
    while(itr.hasNext()){
        try{
        Number value = itr.next(); 
        n = n + value.doubleValue();
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }   
    return n/values.size();
    }  
}
