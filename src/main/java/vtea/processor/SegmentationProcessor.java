/*
 * Copyright (C) 2017 SciJava
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
package vtea.processor;

import ij.ImagePlus;
import java.util.ArrayList;
import org.scijava.plugin.Plugin;

/**
 *
 * @author sethwinfree
 * 
 * 
 */
@Plugin(type = Processor.class)
public class SegmentationProcessor extends AbstractProcessor {
    
    ImagePlus impOriginal;
    ImagePlus impPreview;
    ArrayList protocol;
    int channelProcess;
    
    public SegmentationProcessor(){
        
    VERSION = "0.0";
    AUTHOR = "Seth Winfree";
    COMMENT = "Processor for segmentation processing";
    NAME = "Segmentation Processor";
    KEY = "SegmentationProcessor";
    
    }

    @Override
    protected Void doInBackground() throws Exception {
        return null;
    }

    @Override
    public int process(ArrayList al, String... str) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getChange() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
