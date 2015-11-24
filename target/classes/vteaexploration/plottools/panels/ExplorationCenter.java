/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vteaexploration.plottools.panels;

import ij.ImagePlus;
import vteaexploration.plotgatetools.gates.Gate;
import vteaexploration.plotgatetools.gates.MicroSelection;
import vteaexploration.plotgatetools.listeners.MakeImageOverlayListener;
import vteaexploration.plotgatetools.listeners.ResetSelectionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import vteaobjects.layercake.microVolume;

/**
 *
 * @author vinfrais
 */
public interface ExplorationCenter {

    ArrayList<MakeImageOverlayListener> overlaylisteners = new ArrayList<MakeImageOverlayListener>();
    
    ArrayList<ResetSelectionListener> resetselectionlisteners = new ArrayList<ResetSelectionListener>();

    //public JPanel createPanel(List li);
    public JPanel getPanel();

    public JPanel addSelectionToPlot();
    
    public JPanel addQuadrantPointToPlot();

    public JPanel addPlot(int x, int y, int l, int size, String xText, String yText, String LUTText);

    public void showPlot(int x, int y, int l, int size, String xText, String yText, String lText);

    public void updatePlot(int x, int y, int l, int size);
    
    //public void changeImageGate(ArrayList<microVolume> al, int x, int y, int l, int size);
    
    //public JPanel addImageGatedPlot(ArrayList<microVolume> al, int x, int y, int l, int size, String xText, String yText, String lText);
    
    public void updatePlotPointSize(int size);

    public boolean isMade(int x, int y, int l, int size);

    public void addExplorationGroup();
    
    public XYChartPanel getXYChartPanel();

    public XYChartPanel getPanel(int x, int y, int l, int size, String xText, String yText, String lText);

    public Gate getGates(int x, int y, int l, int size);
    
    public void setGatedOverlay(ImagePlus ip);
    
    public void stopGateSelection();

    public void addMakeImageOverlayListener(MakeImageOverlayListener listener);

    public void notifyMakeImageOverlayListeners(ArrayList gates);
    
    public void addResetSelectionListener(ResetSelectionListener listener);

    public void notifyResetSelectionListeners();
    
    
    
    

}
