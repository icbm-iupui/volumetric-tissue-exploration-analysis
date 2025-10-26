/* 
 * Copyright (C) 2016-2018 Indiana University
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
package vtea.exploration.plottools.panels;

import ij.ImagePlus;
import ij.ImageStack;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import vtea.exploration.listeners.AddFeaturesListener;
import vtea.exploration.listeners.SubGateExplorerListener;

import vtea.exploration.listeners.AxesSetupExplorerPlotUpdateListener;
import vtea.exploration.listeners.LinkedKeyListener;
import vtea.exploration.listeners.UpdateExplorerGuiListener;
import vtea.exploration.plotgatetools.gates.Gate;

import vtea.exploration.plotgatetools.gates.GateLayer;
import vtea.exploration.plotgatetools.gates.PolygonGate;

import vtea.exploration.plotgatetools.listeners.MakeImageOverlayListener;
import vtea.exploration.plotgatetools.listeners.ResetSelectionListener;
import vtea.spatial.densityMap3d;
import vtea.spatial.distanceMaps2d;
import vteaobjects.MicroObject;

/**
 *
 * @author vinfrais
 */
public abstract class AbstractExplorationPanel extends JFrame implements ExplorationCenter {

    ArrayList<MakeImageOverlayListener> overlaylisteners = new ArrayList<MakeImageOverlayListener>();
    
    ArrayList<ResetSelectionListener> resetselectionlisteners = new ArrayList<ResetSelectionListener>();
    
    ArrayList<SubGateExplorerListener> subgatelisteners = new ArrayList<SubGateExplorerListener>();
    
    ArrayList<LinkedKeyListener> linkedKeyListeners = new ArrayList<LinkedKeyListener>();
    
    ArrayList<AddFeaturesListener> addfeaturelisteners = new ArrayList<AddFeaturesListener>();

    ArrayList<AxesSetupExplorerPlotUpdateListener> axesSetupExplorerUpdateListeners = new ArrayList<AxesSetupExplorerPlotUpdateListener>();

    ArrayList<UpdateExplorerGuiListener> updateexlporerguilisteners = new ArrayList<UpdateExplorerGuiListener>();  
    
    protected JPanel CenterPanel = new JPanel();
    protected ArrayList<PolygonGate> gates = new ArrayList<>();
    protected ArrayList<ArrayList<Number>> measurements = new ArrayList<ArrayList<Number>>();
    protected ArrayList<MicroObject> objects = new ArrayList<MicroObject>();
    protected ArrayList<MicroObject> gatemathobjects = new ArrayList<MicroObject>();
    protected ArrayList<String> descriptions = new ArrayList();
    protected ChartPanel chart;
    protected ArrayList<XYPanels> charts = new ArrayList<>();
    protected ArrayList<SubGateExplorerListener> SubGateListeners = new ArrayList<>();
    protected ArrayList<GateLayer> gatelayers = new ArrayList<>();
    protected distanceMaps2d distanceMaps2D = new distanceMaps2d();
    protected densityMap3d densityMaps3D = new densityMap3d();
    protected GateLayer gl = new GateLayer();
    //gates and XYPanels and key for different axes.  First element axes key; second XYPanels; third are the gates

    protected ArrayList<ArrayList> ExplorationItems = new ArrayList<>();
    protected HashMap<Integer, String> hm = new HashMap<>();
    protected ImagePlus impoverlay;
    protected ArrayList<ImageStack> GateOverlays;
    protected boolean imageGate = false;
    protected boolean mapGates = true;
    
    protected ArrayList<Double> AxesLimits;
    
    protected boolean xScaleLinear = true;
    protected boolean yScaleLinear = true;
    
    protected int LUT = 0;

    protected int currentX;
    protected int currentY;
    protected int currentL; 
    protected int pointsize;
    protected static Color imageGateColor = new Color(0,177,76);   

    public AbstractExplorationPanel() {
        CenterPanel.setOpaque(false);
        CenterPanel.setBackground(new Color(255, 255, 255, 255));
        
        
    }

    protected int keyLookUp(int x, int y, int l) {
        ListIterator<ArrayList> itr = ExplorationItems.listIterator();
        Object test;
        String key = x + "_" + y + "_" + l;
        while (itr.hasNext()) {
            //test = itr.next().get(0);
            ArrayList list = itr.next();
            test = list.get(0);
            if (key.equals(test.toString())) {
                return ExplorationItems.indexOf(list);
            }
        }
        return 0;
    }
    @Override
    public ArrayList<PolygonGate> getGates(){
        return gates;
    }
   
    
    @Override
    public ArrayList<Component>  getSettingsContent() {
        ArrayList<Component> al = new ArrayList();
        
//        al.add(new JLabel("X axis minimum: "));
//        al.add(new JTextField());
//        al.add(new JLabel("X axis maximum: "));
//        al.add(new JTextField());
//        al.add(new JPopupMenu());
//        al.add(new JLabel("Y axis minimum: "));
//        al.add(new JTextField());
//        al.add(new JLabel("Y axis maximum: "));
//        al.add(new JTextField());
        
        
        return al;
    }
    
    @Override 
      public ArrayList<MicroObject> getObjects(){
          return objects;
      }
      
    @Override 
      public ArrayList<ArrayList<Number>> getMeasurments(){
          return measurements;
      }
      
      @Override
       public void addFromCSV(String s){}
       
       @Override
       public void setMapping(boolean map){
           this.mapGates = map;
       }

       /**
        * Properly cleanup resources and remove all listeners to prevent memory leaks
        */
       @Override
       public void dispose() {
           // Clear all listener lists
           if (overlaylisteners != null) {
               overlaylisteners.clear();
           }
           if (resetselectionlisteners != null) {
               resetselectionlisteners.clear();
           }
           if (subgatelisteners != null) {
               subgatelisteners.clear();
           }
           if (linkedKeyListeners != null) {
               linkedKeyListeners.clear();
           }
           if (addfeaturelisteners != null) {
               addfeaturelisteners.clear();
           }
           if (axesSetupExplorerUpdateListeners != null) {
               axesSetupExplorerUpdateListeners.clear();
           }
           if (updateexlporerguilisteners != null) {
               updateexlporerguilisteners.clear();
           }
           if (SubGateListeners != null) {
               SubGateListeners.clear();
           }

           // Clear data structures
           if (gates != null) {
               gates.clear();
           }
           if (measurements != null) {
               measurements.clear();
           }
           if (objects != null) {
               objects.clear();
           }
           if (gatemathobjects != null) {
               gatemathobjects.clear();
           }
           if (charts != null) {
               charts.clear();
           }
           if (gatelayers != null) {
               gatelayers.clear();
           }
           if (ExplorationItems != null) {
               ExplorationItems.clear();
           }

           // Nullify references
           impoverlay = null;
           chart = null;
           GateOverlays = null;

           super.dispose();
       }

}
