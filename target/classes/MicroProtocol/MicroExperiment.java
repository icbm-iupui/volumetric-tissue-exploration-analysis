/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MicroProtocol;

import ij.IJ;
import vteaexploration.MicroExplorer;
import vteaexploration.plottools.panels.DefaultPlotPanels;
import vteaexploration.plottools.panels.XYExplorationPanel;
import ij.ImagePlus;
import java.util.ArrayList;
import java.util.HashMap;
import vteaobjects.MicroObjectModel;
import vteaobjects.layercake.microDerivedRegion;
import vteaobjects.layercake.microVolume;
import vteapreprocessing.MicroProtocolPreProcessing;

/**
 *
 * @author vinfrais
 *
 * Class for organizing both the folder classes-> data source and processing and
 * the explorer classes-> connected exploration classes.
 */
public class MicroExperiment implements Runnable {

    ArrayList FolderDrawer = new ArrayList();
    ArrayList ExploreDrawer = new ArrayList();
    MicroProtocolPreProcessing Process;
    
    private Thread t;
    private String threadName = "microExperiment_" + System.nanoTime();
    
    ImagePlus image;
    ArrayList protocol;
    int position;
    

    MicroExperiment() {
    }

    private void addFolder(ImagePlus imp, ArrayList<ArrayList> details) {
        //System.out.println("PROFILING: Adding " + details.size() + " folders to 'FolderDrawer'.");
        for (int i = 0; i <= details.size() - 1; i++) {
            MicroFolder mf = new MicroFolder(imp, (ArrayList) details.get(i));
            mf.start();
            //mf.process();
            FolderDrawer.add(mf);
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
    
    public void addProcessing(ImagePlus imp, String title, MicroProtocolPreProcessing protocol){
        
        Process = new MicroProtocolPreProcessing(imp, protocol.getSteps());
        
    }

    public void addExplore(ImagePlus imp, String title, ArrayList<MicroObjectModel> alvolumes, ArrayList AvailableData) {

        int[] plotDataReference = new int[5];

        plotDataReference[0] = 0;
        plotDataReference[1] = 0;
        plotDataReference[2] = 0;
        plotDataReference[3] = 0;
        plotDataReference[4] = 0;

        ArrayList plotvalues = new ArrayList();

        plotvalues.add(imp);
        plotvalues.add(alvolumes);
        plotvalues.add(0.0);
        plotvalues.add(0.0);
        plotvalues.add("x_axis");
        plotvalues.add("y_axis");
        plotvalues.add(imp.getTitle());
        plotvalues.add(plotDataReference);

        //imageplus
        //volumes per microvolumes
        //float x, deprecated
        //float y, deprecated
        //x title, deprecated
        //y title, deprecated
        //imageplus title
        //plot data reference
        
        HashMap<Integer, String> hm = new HashMap<Integer,String>();
        
        for(int i = 0; i <= AvailableData.size()-1; i++){hm.put(i, AvailableData.get(i).toString());}
       
        XYExplorationPanel XY = new XYExplorationPanel(plotvalues, hm);
    
        
        DefaultPlotPanels DPP = new DefaultPlotPanels();
        MicroExplorer me = new MicroExplorer();
        me.setTitle(imp.getTitle().replace("DUP_", ""));
        me.setTitle(me.getTitle().replace(".tif", ""));
        me.setTitle(me.getTitle().concat("_"+title));
        
        
        
        
        me.process(imp, title, plotvalues, XY, DPP, AvailableData);
        
        //System.out.println("Add explorer for " + alvolumes.size() + " volumes.");
       
        ExploreDrawer.add(me);
    }

    public ArrayList getVolumes(int i) {
        MicroFolder mf;
        mf = (MicroFolder) FolderDrawer.get(i);
        return mf.getVolumes();
    }
    
        public ArrayList getVolumes3D(int i) {
        MicroFolder mf;
        mf = (MicroFolder) FolderDrawer.get(i);
        return mf.getVolumes3D();
    }

    public ArrayList getAvailableData(int i) {
        MicroFolder mf;
        mf = (MicroFolder) FolderDrawer.get(i);
        return mf.getAvailableData();
    }
    
       public ArrayList getAvailableData3D(int i) {
        MicroFolder mf;
        mf = (MicroFolder) FolderDrawer.get(i);
        return mf.getAvailableData3D();
    }
    
    public ArrayList getProcess() {
        return this.Process.getSteps();
    }

    @Override
    public void run() {
        System.out.println("PROFILING: Starting MicroExperiment thread: " + threadName);
        IJ.log("PROFILING: Starting MicroExperiment thread: " + threadName);
        addFolder(image, protocol);
    }
    
    public void start(ImagePlus ProcessedImage, ArrayList p){
                    image = ProcessedImage;
                    protocol = p;
                     t = new Thread (this, threadName);
                     t.start ();
            try {
                t.join();
            } catch (InterruptedException ex) {     
            }
            System.out.println("PROFILING: Exiting MicroExperiment thread: " + threadName); 
            IJ.log("PROFILING: Exiting MicroExperiment thread: " + threadName); 
        }
}
