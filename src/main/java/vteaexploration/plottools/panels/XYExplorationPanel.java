/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vteaexploration.plottools.panels;

import ij.ImagePlus;
import vteaexploration.MicroExplorer;
import vteaexploration.plotgatetools.gates.Gate;
import vteaexploration.plotgatetools.gates.GateLayer;
import vteaexploration.plotgatetools.gates.PolygonGate;
import vteaexploration.plotgatetools.listeners.ChangePlotAxesListener;
import vteaexploration.plotgatetools.listeners.ImageHighlightSelectionListener;
import vteaexploration.plotgatetools.listeners.PolygonSelectionListener;
import vteaexploration.plotgatetools.listeners.MakeImageOverlayListener;
import vteaexploration.plotgatetools.listeners.ResetSelectionListener;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jdesktop.jxlayer.JXLayer;
import org.jfree.chart.ChartPanel;
import vteaexploration.listeners.UpdatePlotWindowListener;
import vteaexploration.plotgatetools.listeners.QuadrantSelectionListener;
import vteaobjects.layercake.microVolume;

/**
 *
 * @author vinfrais
 */
public class XYExplorationPanel extends DefaultExplorationPanel implements PolygonSelectionListener, QuadrantSelectionListener, ImageHighlightSelectionListener, ChangePlotAxesListener, UpdatePlotWindowListener  {

    XYChartPanel cpd;
    
    public XYExplorationPanel(ArrayList li, HashMap<Integer, String> hm) {
        super();
        this.plotvalues = li;

        this.hm = hm;
        this.pointsize = MicroExplorer.POINTSIZE;
        //default plot 
        addPlot(MicroExplorer.XSTART, MicroExplorer.YSTART, MicroExplorer.LUTSTART, MicroExplorer.POINTSIZE, hm.get(1), hm.get(4), hm.get(2)); 
    }

    private XYChartPanel createChartPanel(int x, int y, int l, String xText, String yText, String lText, int size) {
        return new XYChartPanel(plotvalues, x, y, l, xText, yText, lText, size);
    }

    @Override
    public void addMakeImageOverlayListener(MakeImageOverlayListener listener) {
        overlaylisteners.add(listener);
    }

    @Override
    public void notifyMakeImageOverlayListeners(ArrayList gates) {
        for (MakeImageOverlayListener listener : overlaylisteners) {
            listener.makeOverlayImage(gates, currentX, currentY);
        }
    }

    @Override
    public JPanel getPanel() {
        return CenterPanel;
    }
    
    @Override
    public XYChartPanel getXYChartPanel() {
        return cpd;
    }

    @Override
    public JPanel addPlot(int x, int y, int l, int size, String xText, String yText, String lText) {

        currentX = x;
        currentY = y;
        currentL = l;       
        pointsize = size;
        CenterPanel.removeAll();

        //setup chart values
        cpd = new XYChartPanel(plotvalues, x, y, l, xText, yText, lText, pointsize);
        cpd.addUpdatePlotWindowListener(this);
        chart = cpd.getChartPanel();
        chart.setOpaque(false);
        

        //setup chart layer
        CenterPanel.setOpaque(false);
        CenterPanel.setBackground(new Color(0, 0, 0, 0));
        CenterPanel.setPreferredSize(chart.getPreferredSize());

        //add overlay 
        this.gl = new GateLayer();
        gl.addPolygonSelectionListener(this);
        gl.addImageHighLightSelectionListener(this);
        gl.msActive = false;
        this.gates = new ArrayList();
        JXLayer<JComponent> gjlayer = gl.createLayer(chart, gates);
        gjlayer.setLocation(0, 0);
        CenterPanel.add(gjlayer);
        validate();
        repaint();
        pack();
        return CenterPanel;
    }
    
    @Override
    public void addExplorationGroup() {
        ArrayList al = new ArrayList();
        al.add(currentX + "_" + currentY + "_" + currentL);
        al.add(this.chart);
        al.add(gl.getGates());
        ExplorationItems.add(al);
    }

    @Override
    public void updatePlot(int x, int y, int l, int size) {
        if (!(isMade(currentX, currentY, currentL, size))) {
            addExplorationGroup();
        }
        if (!(isMade(x, y, l, size))) {
            addPlot(x, y, l, size, hm.get(x), hm.get(y), hm.get(l));
        } else { 
            showPlot(x, y, l, size, hm.get(x), hm.get(y), hm.get(l));  
        }
    }

    @Override
    public boolean isMade(int x, int y, int l, int size) {
        ListIterator<ArrayList> itr = ExplorationItems.listIterator();
        String test;
        String key = x + "_" + y + "_" + l + "_" + size;
        while (itr.hasNext()) {
            test = itr.next().get(0).toString();
            if (key.equals(test)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public XYChartPanel getPanel(int x, int y, int l, int size, String xText, String yText, String lText) {
        String key = x + "_" + y + "_" + l;
        if (isMade(x, y, l, size)) {
            ListIterator<ArrayList> itr = ExplorationItems.listIterator();
            String test;
            while (itr.hasNext()) {
                test = itr.next().get(0).toString();
                if (key.equals(test)) {
                    return (XYChartPanel) itr.next().get(1);
                }
            }
        }
        return this.createChartPanel(x, y, l, xText, yText, lText, pointsize);
    }

    @Override
    public Gate getGates(int x, int y, int l, int size) {
        String key = x + "_" + y;
        if (isMade(x, y, l, size)) {
            ListIterator<ArrayList> itr = ExplorationItems.listIterator();
            String test;
            while (itr.hasNext()) {
                test = itr.next().get(0).toString();
                if (key.equals(test)) {
                    return (Gate) itr.next().get(1);
                }
            }
        }
        return null;
    }

    @Override
    public void showPlot(int x, int y, int l, int size, String xText, String yText, String lText) {

        currentX = x;
        currentY = y;
        currentL = l;
        CenterPanel.removeAll();
        ArrayList current = this.ExplorationItems.get(keyLookUp(x, y, l));
        
        this.gates = new ArrayList();
        this.chart = (ChartPanel) current.get(1);
        this.gates = (ArrayList) current.get(2);
        this.chart.setOpaque(false);
        CenterPanel.setOpaque(false);
        CenterPanel.setBackground(new Color(0, 0, 0, 0));
        CenterPanel.setPreferredSize(chart.getPreferredSize());

        JXLayer<JComponent> gjlayer = gl.createLayer(chart, gates);
        gjlayer.setLocation(0, 0);
        CenterPanel.add(gjlayer);
        validate();
        repaint();
        pack();
    }

    @Override
    public JPanel addSelectionToPlot() {
        gl.msActive = true;
        validate();
        repaint();
        pack();
        return CenterPanel;
    }
    
    @Override
    public JPanel addQuadrantPointToPlot() {
        gl.msQuadrant = true;
        validate();
        repaint();
        pack();
        return CenterPanel;
    }
    //Listener overrides

    @Override
    public void polygonGate(ArrayList points) {
        PolygonGate pg = new PolygonGate(points);
        pg.createInChartSpace(chart);
        gates.add(pg);
        this.notifyResetSelectionListeners();
    }
    
    @Override
    public void quadrantSelection(float x, float y) {
       
    }

    @Override
    public void imageHighLightSelection(ArrayList gates) {
        this.notifyMakeImageOverlayListeners(gates);
    }

    @Override
    public void onChangeAxes(int x, int y, int l, int size) {

        if (!(isMade(currentX, currentY, currentL, size))) {
            addExplorationGroup();
        }
        if (!(isMade(x, y, l, size))) {
            addPlot(x, y, l, size, hm.get(x), hm.get(y), hm.get(l));
            System.out.println("Change Axes, new plot: " + x + ", " + y + ", " + l);
        } else {
            showPlot(x, y, l, size,  hm.get(x), hm.get(y), hm.get(l));
            System.out.println("Change Axes: " + x + ", " + y + ", " + l);
        }
    }

    @Override
    public void addResetSelectionListener(ResetSelectionListener listener) {
       resetselectionlisteners.add(listener);
    }

    @Override
    public void notifyResetSelectionListeners() {
        for (ResetSelectionListener listener : resetselectionlisteners) {
            listener.resetGateSelection();
        }     
    }

    @Override
    public void stopGateSelection() {
        gl.cancelSelection();
    }

    @Override
    public void onChangePointSize(int size) {
        
    }

    @Override
    public void updatePlotPointSize(int size) {
        this.pointsize = size;
    }

    @Override
    public void onUpdatePlotWindow() {     
        
        
        CenterPanel.removeAll();

        //setup chart values
        chart = cpd.getUpdatedChartPanel();
        chart.setOpaque(false);
        
        

        //setup chart layer
        CenterPanel.setOpaque(false);
        CenterPanel.setBackground(new Color(0, 0, 0, 0));
        CenterPanel.setPreferredSize(chart.getPreferredSize());

        //add overlay 
        this.gl = new GateLayer();
        gl.addPolygonSelectionListener(this);
        gl.addImageHighLightSelectionListener(this);
        gl.msActive = false;
        this.gates = new ArrayList();
        JXLayer<JComponent> gjlayer = gl.createLayer(chart, gates);
        gjlayer.setLocation(0, 0);
        CenterPanel.add(gjlayer);
        validate();
        repaint();
        pack();
    }

    @Override
    public void setGatedOverlay(ImagePlus ip) {
        impoverlay = ip;
        cpd.setOverlayImage(impoverlay);
    }




}
