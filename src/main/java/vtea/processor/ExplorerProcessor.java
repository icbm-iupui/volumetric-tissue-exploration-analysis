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
package vtea.processor;

import ij.ImagePlus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import org.scijava.plugin.Plugin;
import vtea.exploration.plottools.panels.DefaultPlotPanels;
import vtea.exploration.plottools.panels.XYExplorationPanel;
import vteaexploration.MicroExplorer;

/**
 *
 * @author sethwinfree
 */

@Plugin(type = Processor.class)
public class ExplorerProcessor extends AbstractProcessor {
    
    ImagePlus impOriginal;
    ArrayList protocol;  //we may want to turn this into a class...
    
    private ArrayList descriptions; 
    private ArrayList measurements;
    private ArrayList objects;
    
    private ArrayList plotValues;
   
    
    public ExplorerProcessor(){
        
    VERSION = "0.0";
    AUTHOR = "Seth Winfree";
    COMMENT = "Processor for explorer window.";
    NAME = "Explorer Processor";
    KEY = "ExplorerProcessor";
    
    
    
    }
    
    /*this constructor should change to:
    
    public ExplorerProcessor(ImagePlus imp, ArrayList volumes, ArrayList measurements)
    
    once SegmentationProcessor exists on its own.
    
    */
    
    public ExplorerProcessor(String k, ImagePlus imp, ArrayList volumes, ArrayList measurements, ArrayList headers){
        
    VERSION = "0.0";
    AUTHOR = "Seth Winfree";
    COMMENT = "Processor for explorer window.";
    NAME = "Explorer Processor";
    KEY = "ExplorerProcessor";
    
    impOriginal = imp;
    objects = volumes;
    this.measurements = measurements;
    descriptions = headers;
    key = k; 
   
    }
    

    @Override
    protected Void doInBackground() throws Exception {
        
                int progress = 0;
   
        try{       
            
            firePropertyChange("progress", 0, 5);
            firePropertyChange("comment", "", "Starting explorer processing...");
            //ListIterator<Object> litr = this.protocol.listIterator();
            

                    
//        while (litr.hasNext()) {
//            setProgress(progress);
//            //ProcessManager((ArrayList) litr.next(), impOriginal);
//            progress += step;
//        }
        HashMap<Integer, String> hm = new HashMap<Integer,String>();
        for(int i = 0; i <= descriptions.size()-1; i++){hm.put(i, descriptions.get(i).toString());}
        XYExplorationPanel XY = new XYExplorationPanel(measurements, hm, objects);
        DefaultPlotPanels DPP = new DefaultPlotPanels();
        
        String title = "Object_?";
        
        MicroExplorer mex = new MicroExplorer();
        mex.setTitle(impOriginal.getTitle().replace("DUP_", ""));
        mex.setTitle(mex.getTitle().replace(".tif", ""));
        mex.setTitle(mex.getTitle().concat("_"+title));
        mex.process(impOriginal, title, measurements, XY, DPP, descriptions);
                
        setProgress(100);
        firePropertyChange("comment", "", "Done.");
        }catch(Exception e){
        e.printStackTrace();
        }

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
