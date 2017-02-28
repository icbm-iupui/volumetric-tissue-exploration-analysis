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

import vteaprotocol.ProtocolManagerMulti;
import ij.ImageJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.lang.Runnable;
import java.lang.Thread;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import vtea.services.ImageProcessingService;
import vtea.services.SegmentationService;

public class _vtea implements PlugIn, ImageListener, ActionListener {

    public static Color BACKGROUND = new Color(204, 204, 204);
    public static Color BUTTONBACKGROUND = new Color(200, 200, 200);
    public static Color ACTIONPANELBACKGROUND = new Color(240, 240, 240);
    public static Color INACTIVETEXT = new Color(153,153,153);
    public static Color ACTIVETEXT = new Color(0,0,0);
    public static Dimension SMALLBUTTONSIZE = new Dimension(32, 32);
    public static Dimension BLOCKSETUP = new Dimension(370, 350);
    public static Dimension BLOCKSETUPPANEL = new Dimension(340, 100);
    public static String VERSION = new String("0.3.9");

    public static String[] PROCESSOPTIONS = {"LayerCake 3D", "FloodFill 3D"};
    
    public static String[] SEGMENTATIONOPTIONS;
    public static String[] PROCESSINGOPTIONS;
    
    public static ConcurrentHashMap<String, String> PROCESSINGMAP;
    
    //public static Color ButtonBackground = new java.awt.Color(102, 102, 102);

    public static void main(String[] args) {
         //set the plugins.dir property to make the plugin appear in the Plugins menu
        Class<?> clazz = _vtea.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
        System.setProperty("plugins.dir", pluginsDir);
        
        //MicroJNI.HelloJNI jni = new MicroJNI.HelloJNI();

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {new ImageJ(); }
        });
    }
    


    public ProtocolManagerMulti protocolWindow;


    public void setup(String arg, ImagePlus imp1) {
      
    }

    
    @Override
public void run(String arg) {
    ImagePlus.addImageListener(this);
      
    Thread VTEA = new Thread(new Runnable(){
        
        @Override
        public void run() {
              protocolWindow = new ProtocolManagerMulti(); 
              protocolWindow.setVisible(true);
              
              PROCESSINGMAP = new ConcurrentHashMap<String, String>();
              
              SegmentationService ss = new SegmentationService();
              ImageProcessingService ips = new ImageProcessingService();
              
              //ObjectAnalysisService oas = new ObjectAnalysisService();
              //ObjectMeasurementService oms = new ObjectMeasurementService();
              //GroupAnalysisService gas = new GroupAnalysisService();
              //ObjectMeasurementService oms = new ObjectMeasurementService();
              //VisualizationService vs = new VisualizationService();
              //ExplorationService es = new ExplorationService();
              
              List<String> ips_names = ips.getNames();
              List<String> ips_qualifiedNames = ips.getQualifiedName();
              
              List<String> ss_names = ss.getNames();
              //List<String> ss_namesames = ss.getQualifiedName();
              
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
              
              PROCESSINGOPTIONS = ips_names.toArray(new String[ips_names.size()]);
              
              for(int i = 0; i < ips_names.size(); i++){                 
                  try {
                      Object o = Class.forName(ips_qualifiedNames.get(i)).newInstance();
                      System.out.println("Loaded: " + o.getClass().getName());
                      PROCESSINGMAP.put(PROCESSINGOPTIONS[i], o.getClass().getName());
                  } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                      Logger.getLogger(_vtea.class.getName()).log(Level.SEVERE, null, ex);
                  }
              }

              
              System.out.println("Loading Segmentation Plugins: ");
              for(int i = 0; i < ss_names.size(); i++){                 
                  System.out.println(ss_names.get(i));
              }
              
              SEGMENTATIONOPTIONS = ss_names.toArray(new String[ss_names.size()]);
              
        }
    });

    VTEA.setPriority(8);
    System.out.println("New VTEA thread in: "+VTEA.getThreadGroup()+ " at priority: " + VTEA.getPriority());
    VTEA.start();
    

  
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
}
