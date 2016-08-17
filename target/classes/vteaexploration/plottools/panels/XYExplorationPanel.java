/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vteaexploration.plottools.panels;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.RoiListener;
import ij.gui.TextRoi;
import ij.plugin.ChannelSplitter;
import ij.process.StackConverter;
import static ij.plugin.RGBStackMerge.mergeChannels;
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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jdesktop.jxlayer.JXLayer;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import vteaexploration.IJ1Projector;
import vteaexploration.listeners.PlotUpdateListener;
import vteaexploration.listeners.UpdatePlotWindowListener;
import vteaexploration.plotgatetools.listeners.AddGateListener;
import vteaexploration.plotgatetools.listeners.QuadrantSelectionListener;
import vteaobjects.MicroObject;
import vteaobjects.MicroObjectModel;

/**
 *
 * @author vinfrais
 */
public class XYExplorationPanel extends DefaultExplorationPanel implements RoiListener, PlotUpdateListener, PolygonSelectionListener, QuadrantSelectionListener, ImageHighlightSelectionListener, ChangePlotAxesListener, UpdatePlotWindowListener, AddGateListener  {

    XYChartPanel cpd;
    private boolean useGlobal = false;
    int selected = 0;
    int gated = 0;
    
    public XYExplorationPanel(ArrayList li, HashMap<Integer, String> hm) {
        
        super();
        Roi.addRoiListener(this);
        this.plotvalues = li;
        
        this.hm = hm;
        this.pointsize = MicroExplorer.POINTSIZE;
        
        //default plot 
        
        
        
        addPlot(MicroExplorer.XSTART, MicroExplorer.YSTART, MicroExplorer.LUTSTART, MicroExplorer.POINTSIZE, hm.get(1), hm.get(4), hm.get(2)); 
    }

    private XYChartPanel createChartPanel(int x, int y, int l, String xText, String yText, String lText, int size, ImagePlus ip, boolean imageGate, Color imageGateOutline) {
        return new XYChartPanel(plotvalues, x, y, l, xText, yText, lText, size, ip, imageGate, imageGateOutline);
    }
    
    public void makeOverlayImage(ArrayList gates, int x, int y, int xAxis, int yAxis) {
        //convert gate to chart x,y path
        
        
        
        Gate gate;
        ListIterator<Gate> gate_itr = gates.listIterator();

        //.get

        int total = 0;
        int gated = 0;
        int selected = 0;
        int gatedSelected = 0;
       

        int gatecount = gates.size();

        while (gate_itr.hasNext()) {
            gate = gate_itr.next();
            if (gate.getSelected()) {
                Path2D path = gate.createPath2DInChartSpace();

                ArrayList<MicroObject> result = new ArrayList<MicroObject>();
                
                ArrayList<MicroObject> volumes = (ArrayList) this.plotvalues.get(1);
                MicroObjectModel volume;

                double xValue = 0;
                double yValue = 0;

                ListIterator<MicroObject> it = volumes.listIterator();
                try {
                    while (it.hasNext()) {
                        volume = it.next();
                        if (volume != null) {
                            xValue = ((Number) processPosition(xAxis, (MicroObject) volume)).doubleValue();
                            yValue = ((Number) processPosition(yAxis, (MicroObject) volume)).doubleValue();
                            if (path.contains(xValue, yValue)) {
                                result.add((MicroObject) volume);
                            }
                        }
                    }
                } catch (NullPointerException e) {
                };

                Overlay overlay = new Overlay();

                int count = 0;
                BufferedImage placeholder = new BufferedImage(impoverlay.getWidth(), impoverlay.getHeight(), BufferedImage.TYPE_INT_ARGB);
                
                ImageStack gateOverlay = new ImageStack(impoverlay.getWidth(), impoverlay.getHeight());

                selected = getSelectedObjects();

                total = volumes.size();
                
                gated = getGatedObjects(impoverlay);
                
                gatedSelected = getGatedSelected(impoverlay);

                for (int i = 0; i <= impoverlay.getNSlices(); i++) {
                    BufferedImage selections = new BufferedImage(impoverlay.getWidth(), impoverlay.getHeight(), BufferedImage.TYPE_INT_ARGB);

                    Graphics2D g2 = selections.createGraphics();

                    ImageRoi ir = new ImageRoi(0, 0, placeholder);
                    ListIterator<MicroObject> vitr = result.listIterator();

                    while (vitr.hasNext()) {
                        try {
                            MicroObject vol = (MicroObject) vitr.next();

                            int[] x_pixels = vol.getXPixelsInRegion(i);
                            int[] y_pixels = vol.getYPixelsInRegion(i);

                            for (int c = 0; c < x_pixels.length; c++) {

                                g2.setColor(gate.getColor());
                                g2.drawRect(x_pixels[c], y_pixels[c], 1, 1);
                            }
                            ir = new ImageRoi(0, 0, selections);
                            count++;

                        } catch (NullPointerException e) {
                        }
                    }

                    ir.setPosition(i);
                    ir.setOpacity(0.4);
                    overlay.add(ir);

                    gateOverlay.addSlice(ir.getProcessor());

                    java.awt.Font f = new Font("Arial", Font.BOLD, 12);
                    BigDecimal percentage = new BigDecimal(selected);
                    BigDecimal totalBD = new BigDecimal(total);
                    percentage = percentage.divide(totalBD, 4, BigDecimal.ROUND_UP);
                    
                    BigDecimal percentageGated = new BigDecimal(gated);
                    BigDecimal totalGatedBD = new BigDecimal(total);
                    percentageGated = percentageGated.divide(totalGatedBD, 4, BigDecimal.ROUND_UP);
                    
                    BigDecimal percentageGatedSelected = new BigDecimal(gatedSelected);
                    BigDecimal totalGatedSelectedBD = new BigDecimal(total);
                    percentageGatedSelected = percentageGatedSelected.divide(totalGatedSelectedBD, 4, BigDecimal.ROUND_UP);

                    // System.out.println("PROFILING: gate fraction: " + percentage.toString());
                    if (impoverlay.getWidth() > 256) {
                        
                        TextRoi textTotal = new TextRoi(5, 10, selected + "/" + total + " total (" + 100 * percentage.doubleValue() + "%)");
                        
                        if(gated > 0){
                            textTotal = new TextRoi(5, 10, selected + "/" + total + " total (" + 100 * percentage.doubleValue() + "%)" +
                            "; " + gated + "/" + total + " gated (" + 100 * percentageGated.doubleValue() + "%)" +
                            "; " + gatedSelected + "/" + total + " overlap (" + 100 * percentageGatedSelected.doubleValue() + "%)" , f);   
                        }
                        //TextRoi textImageGated = new TextRoi(5, 18, selected + "/" + total + " gated objects (" + 100 * percentage.doubleValue() + "%)", f);
                        textTotal.setPosition(i);
                        //textImageGated.setPosition(i);
                        overlay.add(textTotal);
                    } else {
                        f = new Font("Arial", Font.PLAIN, 10);
                        TextRoi line1 = new TextRoi(5, 5, selected + "/" + total + " objects" + "(" + 100 * percentage.doubleValue() + "%)", f);
                        overlay.add(line1);
                        if(gated > 0){
                        f = new Font("Arial", Font.PLAIN, 10);
                        TextRoi line2 = new TextRoi(5, 18, gated + "/" + total + " gated (" + 100 * percentageGated.doubleValue() + "%)", f);
                        overlay.add(line2);
                        TextRoi line3 = new TextRoi(5, 31, gatedSelected + "/" + total + " overlap (" + 100 * percentageGatedSelected.doubleValue() + "%)", f);
                        overlay.add(line3);
                        }
                        line1.setPosition(i);
    
                    }
                }
                impoverlay.setOverlay(overlay);
                    
                    //ImagePlus gateMaskImage = new ImagePlus("gates", gateOverlay);
                    
                    //gateMaskImage.show();
                     
                    gate.setGateOverlayStack(gateOverlay);

            }
           
            
            impoverlay.draw();
            impoverlay.setTitle(this.getTitle());

            if (impoverlay.getDisplayMode() != IJ.COMPOSITE) {
                impoverlay.setDisplayMode(IJ.COMPOSITE);
            }

            if (impoverlay.getSlice() == 1) {
                impoverlay.setZ(Math.round(impoverlay.getNSlices() / 2));
            } else {
                impoverlay.setSlice(impoverlay.getSlice());
            }
            impoverlay.show();
        }
    }

   
    

    
    @Override
    public int getGatedObjects(ImagePlus ip){
                    ArrayList<MicroObject> ImageGatedObjects = new ArrayList<MicroObject>();
        try{
                    
                    ArrayList<MicroObject> volumes = (ArrayList) plotvalues.get(1);
                    ListIterator<MicroObject> itr = volumes.listIterator();
                    while (itr.hasNext()) {
                        MicroObject m = itr.next();
                        int[] c = new int[2];
                        c = m.getBoundsCenter();

                        if (ip.getRoi().contains(c[0], c[1])) {
                               ImageGatedObjects.add(m);
                               m.setGated(true);
                        }
                    }
        }catch(NullPointerException e){ return 0;}
        return  ImageGatedObjects.size();           
    }
    
    
    
    @Override
    public int getSelectedObjects(){
        Gate gate;
        ListIterator<Gate> gate_itr = gates.listIterator();

        //.get
        int selected = 0;
        int gated = 0;
        int total = 0;


        ArrayList<MicroObject> result = new ArrayList<MicroObject>();
        
        while (gate_itr.hasNext()) {
            gate = gate_itr.next();
            if (gate.getSelected()) {
                Path2D path = gate.createPath2DInChartSpace();

                ArrayList<MicroObject> volumes = (ArrayList) this.plotvalues.get(1);
                MicroObjectModel volume;

                double xValue = 0;
                double yValue = 0;

                ListIterator<MicroObject> it = volumes.listIterator();
                try {
                    while (it.hasNext()) {
                        volume = it.next();
                        if (volume != null) {
                            xValue = ((Number) processPosition(currentX, (MicroObject) volume)).doubleValue();
                            yValue = ((Number) processPosition(currentY, (MicroObject) volume)).doubleValue();
                            if (path.contains(xValue, yValue)) {
                                result.add((MicroObject) volume);
                            }
                        }
                    }
                } catch (NullPointerException e) {
                    return 0;
                }
            }
        }
       

        //System.out.println("RESULT: total gates " + gates.size() + ": " + this.getTitle() + ", Gated: " + selected + ", Total: " + total + ", for: " + 100 * (new Double(selected).doubleValue() / (new Double(total)).doubleValue()) + "%");
        return result.size();
    }
    
    private Number processPosition(int a, MicroObject volume) {
        if (a <= 10) {
            return (Number) volume.getAnalysisMaskVolume()[a];
        } else {
            int row = ((a) / 11) - 1;
            int column = a % 11;
            return (Number) volume.getAnalysisResultsVolume()[row][column];
        }
    }
    
    @Override
    public void addMakeImageOverlayListener(MakeImageOverlayListener listener) {
        overlaylisteners.add(listener);
    }

    @Override
    public void notifyMakeImageOverlayListeners(ArrayList gates) {
        for (MakeImageOverlayListener listener : overlaylisteners) {
            listener.makeOverlayImage(gates, 0, 0, currentX, currentY);
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
        
        System.out.println("PROFILING: Selected objects: " +  getSelectedObjects() + " and " + getGatedObjects(this.impoverlay) + " gated.");
        
        cpd = new XYChartPanel(plotvalues, x, y, l, xText, yText, lText, pointsize, impoverlay, imageGate, imageGateColor);
        //if(imageGate){cpd.roiCreated(impoverlay);}
        cpd.addUpdatePlotWindowListener(this);
        //cpd.setOverlayImage(impoverlay);
        chart = cpd.getChartPanel();
        chart.setOpaque(false);
        
        if(this.useGlobal){
            cpd.setChartPanelRanges(XYChartPanel.XAXIS, cpd.xMin, cpd.xMax);
            cpd.setChartPanelRanges(XYChartPanel.YAXIS, cpd.yMin, cpd.yMax);
        } 
        

        //setup chart layer
        CenterPanel.setOpaque(false);
        CenterPanel.setBackground(new Color(0, 0, 0, 0));
        CenterPanel.setPreferredSize(chart.getPreferredSize());

        //add overlay 
        this.gl = new GateLayer();
        gl.addPolygonSelectionListener(this);
        gl.addImageHighLightSelectionListener(this);
        gl.msActive = false;

       // this.gates = new ArrayList();
        JXLayer<JComponent> gjlayer = gl.createLayer(chart, gates);
        gjlayer.setLocation(0, 0);
        CenterPanel.add(gjlayer);
        validate();
        repaint();
        pack();
        
        makeOverlayImage(gates, 0, 0, currentX, currentY);
        //makeColorizedOverlayImage(gates, 0, 0, currentX, currentY);
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
        return createChartPanel(x, y, l, xText, yText, lText, pointsize, impoverlay, imageGate, imageGateColor);
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
    public JPanel addPolygonToPlot() {
        gl.msPolygon = true;
        validate();
        repaint();
        pack();
        return CenterPanel;
    }
    
    @Override
    public JPanel addQuadrantToPlot() {
        gl.msQuadrant = true;
        validate();
        repaint();
        pack();
        return CenterPanel;
    }
    
        @Override
    public JPanel addRectangleToPlot() {
        gl.msRectangle = true;
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
        this.gates = gates;
        
        makeOverlayImage(gates, 0, 0, currentX, currentY);
        
        //this.notifyMakeImageOverlayListeners(gates);
    }

    @Override
    public void onChangeAxes(int x, int y, int l, int size, boolean imagegate) {
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
    public void onChangePointSize(int size, boolean imagegate) {
        
    }

    @Override
    public void updatePlotPointSize(int size) {
        this.pointsize = size;
    }

    @Override
    public void onUpdatePlotWindow() {     
        
        System.out.println("PROFILING: Image gating, plot updated...  refresh.");
        
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
        gl.addPasteGateListener(this);
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

    @Override
    public void onPlotUpdateListener() {
        this.getParent().validate();
        this.getParent().repaint();
        System.out.println("PROFILING: Plot updated...");
    }

    @Override
    public void roiModified(ImagePlus ip, int i) {
        ArrayList<Gate> gates = new ArrayList<Gate>();
       try{ 
        if(ip.getID() == impoverlay.getID()){
     switch(i)
            {
                case RoiListener.COMPLETED:
                    imageGate = true;
                    //System.out.println("PROFILING: XYChartPanel, Roi modified... Completed. Imagegate: " + imageGate);    
                    addPlot(currentX, currentY, currentL, pointsize, hm.get(currentX), hm.get(currentY), hm.get(currentL));           
                    break;
                case RoiListener.MOVED:
                    imageGate = true;
                    //System.out.println("PROFILING: XYChartPanel, roiListener, Roi modified... Moved. Imagegate: " + imageGate);                   
                    addPlot(currentX, currentY, currentL, pointsize, hm.get(currentX), hm.get(currentY), hm.get(currentL));                  
                    break;
               case RoiListener.DELETED:
                   imageGate = false;
                    //System.out.println("PROFILING: XYChartPanel, roiListener, Roi modified... Deleted. Imagegate: " + imageGate);  
                    addPlot(currentX, currentY, currentL, pointsize, hm.get(currentX), hm.get(currentY), hm.get(currentL));                  
                    break;
                default:
                    break;
            }
        }}
       catch(NullPointerException e){}
    }
   
    private void roiCreated(ImagePlus ip){
        System.out.println("PROFILING: XYChartPanel... image gate processed and updated.");     
    }

    private void roiDeleted(){ 
    }

    @Override
    public ImagePlus getZProjection() {
        ImageStack is = impoverlay.createImagePlus().getImageStack(); 
        ImageStack[] isAll = new ImageStack[impoverlay.getNChannels()+gates.size()];
 
        for(int i = 1; i <= impoverlay.getNChannels(); i++){
            isAll[i-1] = ChannelSplitter.getChannel(impoverlay, i);
        }
        for(int i = 0; i < gates.size(); i++){
            isAll[i+impoverlay.getNChannels()] = gates.get(i).getGateOverlayStack();
        }
        
        ImagePlus[] images = new ImagePlus[isAll.length];
        
       for(int i = 0; i < isAll.length; i++){
         images[i] = new ImagePlus("Channel_" + i, isAll[i]);
         StackConverter sc = new StackConverter(images[i]);
         sc.convertToGray8();
       }
        
       ImagePlus merged = mergeChannels(images, true);  
       merged.setDisplayMode(IJ.COMPOSITE);
        merged.show();
        IJ1Projector projection = new IJ1Projector(merged);
        return projection.getProjection();  
    }

    @Override
    public void onPasteGate() {
               this.getParent().validate();
        this.getParent().repaint();
    }

    @Override
    public void setAxesToCurrent() {   
        XYPlot plot = (XYPlot)cpd.getChartPanel().getChart().getPlot();
        XYChartPanel.xMin = plot.getDomainAxis().getLowerBound();
        XYChartPanel.xMax = plot.getDomainAxis().getUpperBound();
        XYChartPanel.yMin = plot.getRangeAxis().getLowerBound();
        XYChartPanel.yMax = plot.getRangeAxis().getUpperBound();     
    }

    @Override
    public void setGlobalAxes(boolean state) {
        this.useGlobal = state;
    }

    @Override
    public boolean getGlobalAxes() {
        return this.useGlobal;
    }

    @Override
    public int getGatedSelected(ImagePlus ip) {
                     ArrayList<MicroObject> ImageGatedObjects = new ArrayList<MicroObject>();
                     ArrayList<MicroObject> volumes = (ArrayList) plotvalues.get(1);
        try{
                    ListIterator<MicroObject> itr = volumes.listIterator();
                    while (itr.hasNext()) {
                        MicroObject m = itr.next();
                        int[] c = new int[2];
                        c = m.getBoundsCenter();
                        if (ip.getRoi().contains(c[0], c[1])) {
                               ImageGatedObjects.add(m);
                        }
                    }
        }catch(NullPointerException e){ return 0;}
        
        Gate gate;
        ListIterator<Gate> gate_itr = gates.listIterator();
        ArrayList<MicroObject> result = new ArrayList<MicroObject>();
        
        while (gate_itr.hasNext()) {
            gate = gate_itr.next();
            if (gate.getSelected()) {
                Path2D path = gate.createPath2DInChartSpace();

                MicroObjectModel volume;
                double xValue = 0;
                double yValue = 0;

                ListIterator<MicroObject> it = ImageGatedObjects.listIterator();
                try {
                    while (it.hasNext()) {
                        volume = it.next();
                        if (volume != null) {
                            xValue = ((Number) processPosition(currentX, (MicroObject) volume)).doubleValue();
                            yValue = ((Number) processPosition(currentY, (MicroObject) volume)).doubleValue();
                            if (path.contains(xValue, yValue)) {
                                result.add((MicroObject) volume);
                            }
                        }
                    }
                } catch (NullPointerException e) {
                    return 0;
                }
            }
        }
       return result.size();    
    }
}
