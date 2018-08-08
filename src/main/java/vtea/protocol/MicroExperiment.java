/* 
 * Copyright (C) 2016 Indiana University
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
package vtea.protocol;

import ij.IJ;
import vteaexploration.MicroExplorer;
import vtea.exploration.plottools.panels.DefaultPlotPanels;
import vtea.exploration.plottools.panels.XYExplorationPanel;
import ij.ImagePlus;
import java.util.ArrayList;
import java.util.HashMap;
import vteaobjects.MicroObjectModel;
import vtea.workflow.ImageProcessingWorkflow;

import java.lang.IllegalArgumentException;
import vteaobjects.MicroObject;

/**
 *
 * @author vinfrais
 *
 * Class for organizing both the folder classes-> data source and processing and
 * the explorer classes-> connected exploration classes.
 */
public class MicroExperiment implements Runnable{

    ArrayList FolderDrawer = new ArrayList();
    ArrayList ExploreDrawer = new ArrayList();
    ImageProcessingWorkflow Process;
    
    private Thread t;
    private String threadName = "microExperiment_" + System.nanoTime();
    
    ImagePlus image;
    ArrayList protocol;
    int position;
    boolean calculate;
    

    public MicroExperiment() {
    }

    
    //every segmentation setup is added to the microexperiment object as a microfolder.
    
    //will add folder unless the folder already exists...
    private void addUpdateFolder(ImagePlus imp, ArrayList<ArrayList> details, boolean calculate) {
        
        this.calculate = calculate;
        for (int i = 0; i <= details.size()-1; i++) {
            if(FolderDrawer.size() > i+1){
            MicroFolder mf = (MicroFolder)FolderDrawer.get(i);
                if(mf.getImageUpdate() || mf.getProtocolUpdate()){
                    //System.out.println("PROFILING: Updating folder at postion: " + i);
                    mf.start();
                    mf.setProcessedFlags(false);
                } 
            }else{
                MicroFolder mf = new MicroFolder(imp, (ArrayList) details.get(i), calculate);
                FolderDrawer.add(mf);
                //System.out.println("PROFILING: Adding folder at postion: " + i);
                mf.start();
            }
            
        }
    }
    
    public boolean updateMicroFolderImage(int i, ImagePlus imp){
        if(FolderDrawer.size() > 0){
        MicroFolder mf = (MicroFolder)FolderDrawer.get(i);
        mf.setNewImageData(imp);
        return true;
        } else {
        return false;
        }
    }
    
    public boolean updateMicroFolderProtocol(int i, ArrayList al){
        if(FolderDrawer.size() > 0){
        MicroFolder mf = (MicroFolder)FolderDrawer.get(i);
        mf.setNewProtocol(al);
        return true;
        } else {
        return false;
        }
    }
    
    public void emptyFolderDrawer(){
        this.FolderDrawer.clear();
    }
    
    public void emptyExplorerDrawer(){
        this.ExploreDrawer.clear();
    }
    
    public int getFolderDrawerSize(){
        return this.FolderDrawer.size();
    }
    
    public void addProcessing(ImagePlus imp, String title, ImageProcessingWorkflow protocol){
        
        Process = new ImageProcessingWorkflow(imp, protocol.getSteps());
        
    }

    public void addExplore(ImagePlus imp, String title, ArrayList<MicroObjectModel> alvolumes, ArrayList AvailableData) {


        ArrayList plotvalues = new ArrayList();

        plotvalues.add("");
        plotvalues.add(alvolumes);



       
        //removing
        HashMap<Integer, String> hm = new HashMap<Integer,String>();
        for(int i = 0; i <= AvailableData.size()-1; i++){hm.put(i, AvailableData.get(i).toString());}
        //XYExplorationPanel XY = new XYExplorationPanel(plotvalues, hm);
        DefaultPlotPanels DPP = new DefaultPlotPanels();
        
        MicroExplorer mex = new MicroExplorer();
        mex.setTitle(imp.getTitle().replace("DUP_", ""));
        mex.setTitle(mex.getTitle().replace(".tif", ""));
        mex.setTitle(mex.getTitle().concat("_"+title));
        //mex.process(imp, title, plotvalues, XY, DPP, AvailableData);

        ExploreDrawer.add(mex);
    }

    public ArrayList getFolderVolumes(int i) {
        MicroFolder mf;
        mf = (MicroFolder) FolderDrawer.get(i);
        return mf.getVolumes();
    }
    
//        public ArrayList getVolumes3D(int i) {
//        MicroFolder mf;
//        mf = (MicroFolder) FolderDrawer.get(i);
//        return mf.getVolumes3D();
//    }

    public ArrayList getAvailableFolderData(int i) {
        MicroFolder mf;
        mf = (MicroFolder) FolderDrawer.get(i);
        return mf.getAvailableData();
    }
    
//       public ArrayList getAvailableData3D(int i) {
//        MicroFolder mf;
//        mf = (MicroFolder) FolderDrawer.get(i);
//        return mf.getAvailableData3D();
//    }
    
    public ArrayList getProcess() {
        return this.Process.getSteps();
    }

    @Override
    public void run() {
        System.out.println("PROFILING: Starting MicroExperiment thread: " + threadName);
        IJ.log("PROFILING: Starting MicroExperiment thread: " + threadName);
        addUpdateFolder(image, protocol, calculate);
    }
    
    public void start(ImagePlus ProcessedImage, ArrayList p, boolean calculate){
                    this.calculate = calculate;
                    image = ProcessedImage;
                    protocol = p;
                     //t = new Thread (this, threadName);
                     //t.start ();
                     
                     Thread t = new Thread() {
            public void run() {
    
               addUpdateFolder(image, protocol, calculate);
                
            }

        };
        t.start();
                     
                     
            try {
                t.join();
            } catch (IllegalArgumentException iae) {  
                
            } catch (InterruptedException ex) {
            
        }
            System.out.println("PROFILING: Exiting MicroExperiment thread: " + threadName); 
            IJ.log("PROFILING: Exiting MicroExperiment thread: " + threadName); 
        }
}


