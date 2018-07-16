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
package vtea.exploration.plottools.panels;

import ij.ImagePlus;
import java.awt.Component;
import java.awt.image.BufferedImage;
import vtea.exploration.plotgatetools.gates.Gate;
import vtea.exploration.plotgatetools.gates.MicroSelection;
import vtea.exploration.plotgatetools.listeners.MakeImageOverlayListener;
import vtea.exploration.plotgatetools.listeners.ResetSelectionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import vtea.objects.layercake.microVolume;
import vteaobjects.MicroObject;

/**
 *
 * @author vinfrais
 */
public interface ExplorationCenter {
    
    static int PANELWIDTH = 630;
    
    static int PANELHEIGHT = 600;
    
   

    ArrayList<MakeImageOverlayListener> overlaylisteners = new ArrayList<MakeImageOverlayListener>();
    
    ArrayList<ResetSelectionListener> resetselectionlisteners = new ArrayList<ResetSelectionListener>();

    //public JPanel createPanel(List li);
    public JPanel getPanel();
    
    public ImagePlus getZProjection();

    public JPanel addPolygonToPlot();
    
    public JPanel addQuadrantToPlot();
    
    public JPanel addRectangleToPlot();

    public JPanel addPlot(int x, int y, int l, int size, String xText, String yText, String LUTText);

    public void showPlot(int x, int y, int l, int size, String xText, String yText, String lText);

    public void updatePlot(int x, int y, int l, int size);
    
   // public void changeImageGate(ArrayList<microVolume> al, int x, int y, int l, int size);
    
    //public JPanel addImageGatedPlot(ArrayList<microVolume> al, int x, int y, int l, int size, String xText, String yText, String lText);
    
    public void updatePlotPointSize(int size);

    public boolean isMade(int x, int y, int l, int size);

    public void addExplorationGroup();
    
    public XYChartPanel getXYChartPanel();

    public XYChartPanel getPanel(int x, int y, int l, int size, String xText, String yText, String lText);

    public Gate getGates(int x, int y, int l, int size);
    
    public int getSelectedObjects();
    
    public int getGatedObjects(ImagePlus ip);
    
    public int getGatedSelected(ImagePlus ip);
    
    public ArrayList<MicroObject> getObjects();
    
    public ArrayList<ArrayList<Number>> getMeasurments();
    
    public void setGatedOverlay(ImagePlus ip);
    
    public void setAxesToCurrent();
    
    public void setAxesTo(ArrayList<Double> al, boolean x, boolean y);
    
    public void setCustomRange(boolean state);
    
    public void setGlobalAxes(boolean state);
    
    public ArrayList<Component> getSettingsContent();
    
    public boolean getGlobalAxes();
    
    public void stopGateSelection();

    public void addMakeImageOverlayListener(MakeImageOverlayListener listener);

    public void notifyMakeImageOverlayListeners(ArrayList gates);
    
    public void addResetSelectionListener(ResetSelectionListener listener);

    public void notifyResetSelectionListeners();
    
    public BufferedImage getBufferedImage();
    
    public void exportGates();
    
    public void importGates();
}
