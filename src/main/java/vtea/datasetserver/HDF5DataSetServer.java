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
package vtea.datasetserver;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5SimpleReader;
import ch.systemsx.cisd.hdf5.IHDF5SimpleWriter;
import org.scijava.plugin.Plugin;
import vtea.featureprocessing.FeatureProcessing;

/**
 *
 * @author sethwinfree
 */

@Plugin(type = DataSetServer.class)
public class HDF5DataSetServer extends vteaDataSetServer {

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getKey() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void writeH5() {
        float[] mydata = new float[1000];
IHDF5SimpleWriter writer = HDF5Factory.open("myfile.h5");
writer.writeFloatArray("mydata", mydata);
writer.close();
    }

public void readH5(String location){
IHDF5SimpleReader reader = HDF5Factory.openForReading("myfile.h5");
float[] mydata = reader.readFloatArray("mydata");
reader.close();
    }

 
    
}

/**solutions
 * 
 * Chunk like a pyrimidal tiff file
 *  
 *  have to have image and mapping, although mapping could be points below 
 * 1:1...
 * 
 * move obxs to objects and store in h2?  
 * 
 * 
 * 
 */