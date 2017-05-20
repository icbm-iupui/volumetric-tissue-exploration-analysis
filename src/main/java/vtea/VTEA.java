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
package vtea;

import ij.ImageJ;
import vteaprotocol.ProtocolManagerMulti;
import ij.ImageListener;
import ij.ImagePlus;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RserveException;
import org.scijava.Context;
import org.scijava.Prioritized;
import org.scijava.Priority;
import org.scijava.log.LogService;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.plugin.RichPlugin;
import vtea.services.ImageProcessingService;
import vtea.services.SegmentationService;
import vtear.RServe;
import vtear.Renjin;

@Plugin(type= RichPlugin.class, priority=Priority.HIGH_PRIORITY, menuPath = "IU_Tools > VTEA")
public class VTEA implements Runnable, RichPlugin, ImageListener, ActionListener {

    public static Color BACKGROUND = new Color(204, 204, 204);
    public static Color BUTTONBACKGROUND = new Color(200, 200, 200);
    public static Color ACTIONPANELBACKGROUND = new Color(240, 240, 240);
    public static Color INACTIVETEXT = new Color(153,153,153);
    public static Color ACTIVETEXT = new Color(0,0,0);
    public static Dimension SMALLBUTTONSIZE = new Dimension(32, 32);
    public static Dimension BLOCKSETUP = new Dimension(370, 350);
    public static Dimension BLOCKSETUPPANEL = new Dimension(340, 100);
    public static String VERSION = new String("0.4");

    public static String[] PROCESSOPTIONS = {"LayerCake 3D", "FloodFill 3D"};
    
    public static String[] SEGMENTATIONOPTIONS;
    public static String[] PROCESSINGOPTIONS;
    
    public static ConcurrentHashMap<String, String> PROCESSINGMAP;
    public static ConcurrentHashMap<String, String> SEGMENTATIONMAP;
    
    public ProtocolManagerMulti protocolWindow;
    
    private Context context;
    private double priority = Priority.HIGH_PRIORITY;
    

    public static void main(String... args){
        
		//ImageJ.main( args );
                ImageJ ij = new ImageJ();
		new VTEA().run();

}

    @Override
    public void run(){

                context = new Context( LogService.class, PluginService.class );

                ImagePlus.addImageListener(this);
                
                protocolWindow = new ProtocolManagerMulti();
                protocolWindow.setVisible(true);
                
                PROCESSINGMAP = new ConcurrentHashMap<String, String>();
                SEGMENTATIONMAP = new ConcurrentHashMap<String, String>();

                SegmentationService ss = new SegmentationService(context);                
                ImageProcessingService ips = new ImageProcessingService(context);
               
                //ObjectAnalysisService oas = new ObjectAnalysisService();
                //ObjectMeasurementService oms = new ObjectMeasurementService();                
                //GroupAnalysisService gas = new GroupAnalysisService();
                //ObjectMeasurementService oms = new ObjectMeasurementService();
                //VisualizationService vs = new VisualizationService();
                //ExplorationService es = new ExplorationService();             
                
                List<String> ips_names = ips.getNames();
                List<String> ips_qualifiedNames = ips.getQualifiedName();
                
                List<String> ss_names = ss.getNames();
                List<String> ss_qualifiedNames = ss.getQualifiedName();
                
                //List<String> oas_names = oas.getNames();
                //List<String> oas_qualifiedNames = oas.getQualifiedName();
                
                //List<String> oas_names = oas.getNames();
                //List<String> oas_qualifiedNames = oas.getQualifiedName();    
                
                //List<String> ss_names = ss.getNames();
                //List<String> ss_namesames = ss.getQualifiedName();
                
                //List<String> ss_names = ss.getNames();
                //List<String> ss_namesames = ss.getQualifiedName();
                
                //List<String> ss_names = ss.getNames();
                //List<String> ss_namesames = ss.getQualifiedName();
                
                //List<String> ss_names = ss.getNames();
                //List<String> ss_namesames = ss.getQualifiedName();
                
                
                System.out.println("Loading Image Processing Plugins: ");
                //Logger.getAnonymousLogger().log(Level.INFO, "Loading Image Processing Plugins: ");
                
                PROCESSINGOPTIONS = ips_names.toArray(new String[ips_names.size()]);
                
                for(int i = 0; i < ips_names.size(); i++){
                    try {
                        Object o = Class.forName(ips_qualifiedNames.get(i)).newInstance();
                        System.out.println("Loaded: " + o.getClass().getName());
                        //Logger.getLogger(VTEAService.class.getName()).log(Level.INFO, "Loaded: " + o.getClass().getName());
                        PROCESSINGMAP.put(PROCESSINGOPTIONS[i], o.getClass().getName());    
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                        Logger.getLogger(VTEA.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                System.out.println("Loading Segmentation Plugins: ");
                //Logger.getAnonymousLogger().log(Level.INFO, "Loading Segmentation Plugins: ");
                
                SEGMENTATIONOPTIONS = ss_names.toArray(new String[ss_names.size()]);
                
                for(int i = 0; i < ss_names.size(); i++){
                    try {
                        Object o = Class.forName(ss_qualifiedNames.get(i)).newInstance();
                        System.out.println("Loaded: " + o.getClass().getName()); 
                        //Logger.getLogger(VTEAService.class.getName()).log(Level.INFO, "Loaded: " + o.getClass().getName());
                        SEGMENTATIONMAP.put(SEGMENTATIONOPTIONS[i], o.getClass().getName());
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                        Logger.getLogger(VTEA.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
              Renjin r = new Renjin();
                
            try {
                RServe rS = new RServe();
            } catch (RserveException ex) {
               
            } catch (REXPMismatchException ex) {
                
            }
    }


    @Override
    public void actionPerformed(java.awt.event.ActionEvent evt) {

    }

    @Override
    public void imageOpened(ImagePlus imp) {
        protocolWindow.UpdateImageList();   
    }

    @Override
    public void imageClosed(ImagePlus imp) {
        protocolWindow.UpdateImageList();  
    }

    @Override
    public void imageUpdated(ImagePlus imp) {
        protocolWindow.UpdateImageList();    
    }

    @Override
    public Context context() {
       return context;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void setContext(Context cntxt) {
        context = cntxt;
    }

    @Override
    public double getPriority() {
        return priority;
    }

    @Override
    public void setPriority(double d) {
        priority = d;
    }

    @Override
    public int compareTo(Prioritized o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PluginInfo<?> getInfo() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setInfo(PluginInfo<?> pi) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    }
