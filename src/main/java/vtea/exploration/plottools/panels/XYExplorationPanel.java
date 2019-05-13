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

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.RoiListener;
import ij.gui.TextRoi;
import ij.plugin.ChannelSplitter;
import static ij.plugin.RGBStackMerge.mergeChannels;
import ij.process.StackConverter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ListIterator;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.io.FilenameUtils;
import org.jdesktop.jxlayer.JXLayer;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.plot.XYPlot;
import vtea._vtea;
import static vtea._vtea.LUTMAP;
import static vtea._vtea.LUTOPTIONS;
import vtea.exploration.listeners.PlotUpdateListener;
import vtea.exploration.listeners.UpdatePlotWindowListener;
import vtea.exploration.plotgatetools.gates.Gate;
import vtea.exploration.plotgatetools.gates.GateImporter;
import vtea.exploration.plotgatetools.gates.GateLayer;
import vtea.exploration.plotgatetools.gates.PolygonGate;
import vtea.exploration.plotgatetools.listeners.AddGateListener;
import vtea.exploration.plotgatetools.listeners.ChangePlotAxesListener;
import vtea.exploration.plotgatetools.listeners.ImageHighlightSelectionListener;
import vtea.exploration.plotgatetools.listeners.MakeImageOverlayListener;
import vtea.exploration.plotgatetools.listeners.PolygonSelectionListener;
import vtea.exploration.plotgatetools.listeners.QuadrantSelectionListener;
import vtea.exploration.plotgatetools.listeners.ResetSelectionListener;
import vtea.jdbc.H2DatabaseEngine;
import vtea.lut.AbstractLUT;
import vteaexploration.MicroExplorer;
import vteaobjects.MicroObject;
import vteaobjects.MicroObjectModel;

/**
 *
 * @author vinfrais
 */
public class XYExplorationPanel extends AbstractExplorationPanel implements WindowListener, RoiListener, PlotUpdateListener, PolygonSelectionListener, QuadrantSelectionListener, ImageHighlightSelectionListener, ChangePlotAxesListener, UpdatePlotWindowListener, AddGateListener {

    XYChartPanel cpd;
    private boolean useGlobal = false;
    private boolean useCustom = false;
    int selected = 0;
    int gated = 0;
    String key = "";
    String keySQLSafe = "";

    private Connection connection;

    public XYExplorationPanel(String key, Connection connection, ArrayList measurements, ArrayList<String> descriptions, HashMap<Integer, String> hm, ArrayList<MicroObject> objects) {

        super();
        Roi.addRoiListener(this);
        this.key = key;
        this.objects = objects;
        this.measurements = measurements;
        this.descriptions = descriptions;
        this.connection = connection;

        keySQLSafe = key.replace("-", "_");

        writeCSV(ij.Prefs.getImageJDir() + key);
        startH2Database(ij.Prefs.getImageJDir() + key + ".csv", vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe);

        this.LUT = 0;
        this.hm = hm;
        this.pointsize = MicroExplorer.POINTSIZE;

        //default plot 
        addPlot(MicroExplorer.XSTART, MicroExplorer.YSTART, MicroExplorer.LUTSTART, MicroExplorer.POINTSIZE, 0, hm.get(1), hm.get(4), hm.get(2));
    }

    private XYChartPanel createChartPanel(int x, int y, int l, String xText, String yText, String lText, int size, ImagePlus ip, boolean imageGate, Color imageGateOutline) {
        return new XYChartPanel(objects, measurements, x, y, l, xText, yText, lText, size, ip, imageGate, new Color(0, 177, 76));
    }

    private void startH2Database(String file, String table) {

        try {
            Connection cn = H2DatabaseEngine.getDBConnection();

            H2DatabaseEngine.insertFromCSV(new File(file), cn, table);
            cn.commit();

        } catch (SQLException e) {

        }
    }

    private void writeCSV(String file) {
        try {
            PrintWriter pw = new PrintWriter(new File(file + ".csv"));
            StringBuilder sb = new StringBuilder();

            ListIterator itr = descriptions.listIterator();

            sb.append("Object");
            sb.append(',');
            sb.append("PosX,PosY,PosZ,");
            while (itr.hasNext()) {
                sb.append((String) itr.next());
                if (itr.hasNext()) {
                    sb.append(',');
                }
            }

            sb.append('\n');

            for (int i = 0; i < objects.size(); i++) {

                MicroObject volume = objects.get(i);
                ArrayList<Number> measured = measurements.get(i);

                sb.append(volume.getSerialID());
                sb.append(',');
                sb.append(volume.getCentroidX());
                sb.append(',');
                sb.append(volume.getCentroidY());
                sb.append(',');
                sb.append(volume.getCentroidZ());

                ListIterator<Number> itr_mes = measured.listIterator();

                while (itr_mes.hasNext()) {

                    sb.append(",");
                    sb.append(itr_mes.next());
                }
                sb.append('\n');
            }

            pw.write(sb.toString());
            pw.close();

        } catch (FileNotFoundException e) {
        }

    }

//    
    public void makeOverlayImage(ArrayList gates, int x, int y, int xAxis, int yAxis) {
        //convert gate to chart x,y path

        Gate gate;
        ListIterator<Gate> gate_itr = gates.listIterator();

        int total = 0;
        int gated = 0;
        int selected = 0;
        int gatedSelected = 0;
        int gatecount = gates.size();

        while (gate_itr.hasNext()) {
            gate = gate_itr.next();

            if (gate.getSelected()) {

                Path2D.Double path = gate.createPath2DInChartSpace();

                ArrayList<MicroObject> result = new ArrayList<MicroObject>();
                ArrayList<MicroObject> resultFinal = new ArrayList<MicroObject>();

                ArrayList<MicroObject> volumes = (ArrayList) objects;
                MicroObjectModel volume;

                double xValue = 0;
                double yValue = 0;

                ArrayList<ArrayList> resultKey = H2DatabaseEngine.getObjectsInRange2D(vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe,
                        this.descriptions.get(xAxis), path.getBounds2D().getX(),
                        path.getBounds2D().getX() + path.getBounds2D().getWidth(),
                        this.descriptions.get(yAxis), path.getBounds2D().getY(),
                        path.getBounds2D().getY() + path.getBounds2D().getHeight());

                ListIterator<ArrayList> itr = resultKey.listIterator();

                while (itr.hasNext()) {
                    ArrayList al = itr.next();
                    int object = ((Number) (al.get(0))).intValue();
                    result.add(volumes.get(object));
                }

                try {
                    for (int i = 0; i < result.size(); i++) {

                        ArrayList<Number> measured = resultKey.get(i);

                        xValue = measured.get(1).doubleValue();
                        yValue = measured.get(2).doubleValue();

                        if (path.contains(xValue, yValue)) {
                            resultFinal.add((MicroObject) result.get(i));
                        }
                    }
                } catch (NullPointerException e) {
                }

                Overlay overlay = new Overlay();

                int count = 0;

                BufferedImage placeholder = new BufferedImage(impoverlay.getWidth(), impoverlay.getHeight(), BufferedImage.TYPE_INT_ARGB);

                ImageStack gateOverlay = new ImageStack(impoverlay.getWidth(), impoverlay.getHeight());

                selected = result.size();

                total = volumes.size();

                gated = getGatedObjects(impoverlay);
                gatedSelected = getGatedSelected(impoverlay);

                Collections.sort(result, new ZComparator());

                for (int i = 0; i <= impoverlay.getNSlices(); i++) {

                    BufferedImage selections = new BufferedImage(impoverlay.getWidth(), impoverlay.getHeight(), BufferedImage.TYPE_INT_ARGB);

                    Graphics2D g2 = selections.createGraphics();

                    ImageRoi ir = new ImageRoi(0, 0, placeholder);

                    ListIterator<MicroObject> vitr = result.listIterator();

                    boolean inZ = true;

                    while (vitr.hasNext() && inZ) {

                        MicroObject vol = (MicroObject) vitr.next();

                        inZ = true;

                        //
                        if (i >= vol.getMinZ() && i <= vol.getMaxZ()) {
                            inZ = false;
                        }
                        //

                        try {
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

                    ir.setPosition(0, i + 1, 0);

                    //old setPosition not functional as of imageJ 1.5m
                    ir.setOpacity(0.4);
                    overlay.selectable(false);
                    overlay.add(ir);

                    gateOverlay.addSlice(ir.getProcessor());

                    //text for overlay
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

                    if (impoverlay.getWidth() > 512) {

                        //f = new Font("Arial", Font.PLAIN, 100);
                        TextRoi textTotal = new TextRoi(5, 10, selected + "/" + total + " gated (" + 100 * percentage.floatValue() + "%)");

                        if (gated > 0) {
                            textTotal = new TextRoi(5, 10, selected + "/" + total + " total (" + 100 * percentage.floatValue() + "%)"
                                    + "; " + gated + "/" + total + " roi (" + 100 * percentageGated.floatValue() + "%)"
                                    + "; " + gatedSelected + "/" + total + " overlap (" + 100 * percentageGatedSelected.floatValue() + "%)", f);
                        }
                        textTotal.setPosition(i);
                        overlay.add(textTotal);
                    } else {
                        f = new Font("Arial", Font.PLAIN, 10);
                        TextRoi line1 = new TextRoi(5, 5, selected + "/" + total + " gated" + "(" + 100 * percentage.floatValue() + "%)", f);
                        line1.setPosition(i);
                        overlay.add(line1);
                        if (gated > 0) {
                            f = new Font("Arial", Font.PLAIN, 10);
                            TextRoi line2 = new TextRoi(5, 18, gated + "/" + total + " roi (" + 100 * percentageGated.floatValue() + "%)", f);
                            line2.setPosition(i);
                            overlay.add(line2);
                            TextRoi line3 = new TextRoi(5, 31, gatedSelected + "/" + total + " overlap (" + 100 * percentageGatedSelected.floatValue() + "%)", f);
                            line3.setPosition(i);
                            overlay.add(line3);
                        }

                    }
                }
                impoverlay.setOverlay(overlay);

                gate.setGateOverlayStack(gateOverlay);

            }

            impoverlay.draw();

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
    public int getGatedObjects(ImagePlus ip) {
        ArrayList<MicroObject> ImageGatedObjects = new ArrayList<MicroObject>();
        try {
            ListIterator<MicroObject> itr = objects.listIterator();
            while (itr.hasNext()) {
                MicroObject m = itr.next();
//                int[] c = new int[2];
//                c = m.getBoundsCenter();

                if (ip.getRoi().contains((int) m.getCentroidX(), (int) m.getCentroidY())) {
                    ImageGatedObjects.add(m);
                    m.setGated(true);
                }
            }
        } catch (NullPointerException e) {
            return 0;
        }
        return ImageGatedObjects.size();
    }

    @Override
    public int getSelectedObjects() {
        Gate gate;
        ListIterator<Gate> gate_itr = gates.listIterator();

        //.get
        int selected = 0;
        int gated = 0;
        int total = objects.size();

        ArrayList<MicroObject> result = new ArrayList<MicroObject>();

        while (gate_itr.hasNext()) {
            gate = gate_itr.next();
            if (gate.getSelected()) {
                Path2D path = gate.createPath2DInChartSpace();

                ArrayList<Number> measured;

                double xValue = 0;
                double yValue = 0;

                for (int i = 0; i < objects.size(); i++) {

                    ListIterator<ArrayList<Number>> it = measurements.listIterator();
                    try {
                        while (it.hasNext()) {
                            measured = it.next();
                            if (measured != null) {
                                xValue = measured.get(currentX).floatValue();
                                yValue = measured.get(currentY).floatValue();
                                if (path.contains(xValue, yValue)) {
                                    result.add(objects.get(i));
                                }
                            }

                        }
                    } catch (NullPointerException e) {
                        return 0;
                    }
                }
            }
        }

        //System.out.println("RESULT: total gates " + gates.size() + ": " + this.getTitle() + ", Gated: " + result.size() + ", Total: " + total + ", for: " + 100 * (new Double(selected).doubleValue() / (new Double(total)).doubleValue()) + "%");
        return result.size();
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
    public JPanel addPlot(int x, int y, int l, int size, int LUT, String xText, String yText, String lText) {
        currentX = x;
        currentY = y;
        currentL = l;
        pointsize = size;
        CenterPanel.removeAll();

        cpd = new XYChartPanel(keySQLSafe, objects, x, y, l, xText, yText, lText, pointsize, impoverlay, imageGate, imageGateColor);

        cpd.addUpdatePlotWindowListener(this);

        chart = cpd.getChartPanel();
        chart.setOpaque(false);

        XYPlot plot = (XYPlot) cpd.getChartPanel().getChart().getPlot();

        if (useGlobal) {
            cpd.setChartPanelRanges(XYChartPanel.XAXIS, cpd.xMin, cpd.xMax);
            cpd.setChartPanelRanges(XYChartPanel.YAXIS, cpd.yMin, cpd.yMax);

            if (!XYChartPanel.xLinear) {
                LogAxis xcLog = new LogAxis();
                xcLog.setRange(cpd.xMin, cpd.xMax);
                xcLog.setMinorTickCount(9);
                plot.setDomainAxis(xcLog);
            }
            if (!XYChartPanel.yLinear) {
                LogAxis ycLog = new LogAxis();
                ycLog.setRange(cpd.yMin, cpd.yMax);
                ycLog.setMinorTickCount(9);
                plot.setRangeAxis(ycLog);
            }

        }

        if (useCustom) {

            cpd.setChartPanelRanges(XYChartPanel.XAXIS, AxesLimits.get(0), AxesLimits.get(1));
            cpd.setChartPanelRanges(XYChartPanel.YAXIS, AxesLimits.get(2), AxesLimits.get(3));

            if (!xScaleLinear) {
                LogAxis xcLog = new LogAxis();

                xcLog.setMinorTickCount(9);
                plot.setDomainAxis(xcLog);
            }
            if (!yScaleLinear) {
                LogAxis ycLog = new LogAxis();

                ycLog.setMinorTickCount(9);
                plot.setRangeAxis(ycLog);
            }

        }

        //setup LUTs
        try {
            Class<?> c;

            String str = LUTMAP.get(LUTOPTIONS[this.LUT]);

            //System.out.println("PROFILING: The loaded lut is: " + str);
            c = Class.forName(str);
            Constructor<?> con;

            Object iImp = new Object();

            try {

                con = c.getConstructor();
                iImp = con.newInstance();

                HashMap lutTable = ((AbstractLUT) iImp).getLUTMAP();

                XYChartPanel.TENPERCENT = ((AbstractLUT) iImp).getColor(10);
                XYChartPanel.TWENTYPERCENT = ((AbstractLUT) iImp).getColor(20);
                XYChartPanel.THIRTYPERCENT = ((AbstractLUT) iImp).getColor(30);
                XYChartPanel.FORTYPERCENT = ((AbstractLUT) iImp).getColor(40);
                XYChartPanel.FIFTYPERCENT = ((AbstractLUT) iImp).getColor(50);
                XYChartPanel.SIXTYPERCENT = ((AbstractLUT) iImp).getColor(60);
                XYChartPanel.SEVENTYPERCENT = ((AbstractLUT) iImp).getColor(70);
                XYChartPanel.EIGHTYPERCENT = ((AbstractLUT) iImp).getColor(80);
                XYChartPanel.NINETYPERCENT = ((AbstractLUT) iImp).getColor(90);
                XYChartPanel.ALLPERCENT = ((AbstractLUT) iImp).getColor(100);

//        
            } catch (NullPointerException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                System.out.println("EXCEPTION: new instance decleration error... NPE etc.");
            }
        } catch (NullPointerException | ClassNotFoundException ex) {
            System.out.println("EXCEPTION: new class decleration error... Class not found.");
        }

        //setup chart layer
        CenterPanel.setOpaque(false);
        CenterPanel.setBackground(new Color(255, 255, 255, 255));
        CenterPanel.setPreferredSize(chart.getPreferredSize());

        //add overlay 
        this.gl = new GateLayer();
        gl.addPolygonSelectionListener(this);
        gl.addImageHighLightSelectionListener(this);

        gl.msActive = false;

        JXLayer<JComponent> gjlayer = gl.createLayer(chart, gates);
        gjlayer.setLocation(0, 0);
        CenterPanel.add(gjlayer);
        validate();
        repaint();
        pack();

        //addExplorationGroup();
        return CenterPanel;
    }

    private double getMaximumOfData(ArrayList measurements, int l) {

        ListIterator<ArrayList> litr = measurements.listIterator();

        Number high = 0;

        while (litr.hasNext()) {
            try {

                ArrayList<Number> al = litr.next();

                if (al.get(l).floatValue() > high.floatValue()) {
                    high = al.get(l).floatValue();
                }
            } catch (NullPointerException e) {
            }
        }
        return high.longValue();

    }

    private double getMinimumOfData(ArrayList measurements, int l) {

        ListIterator<ArrayList> litr = measurements.listIterator();

        //ArrayList<Number> al = new ArrayList<Number>();
        Number low = getMaximumOfData(measurements, l);

        while (litr.hasNext()) {
            try {
                ArrayList<Number> al = litr.next();
                if (al.get(l).floatValue() < low.floatValue()) {
                    low = al.get(l).floatValue();
                }
            } catch (NullPointerException e) {
            }
        }
        return low.longValue();
    }

    @Override
    public void addExplorationGroup() {
        ArrayList al = new ArrayList();
        al.add(currentX + "_" + currentY + "_" + currentL);
        al.add(this.cpd);
        al.add(gl.getGates());
        ExplorationItems.add(al);
    }

    @Override
    public void updatePlot(int x, int y, int l, int size) {
//        if (!(isMade(currentX, currentY, currentL, size))) {
//            addExplorationGroup();
//        }

        String lText = "";
        if (l < 0) {
            lText = "";
        } else {
            lText = hm.get(l);
        }
//        if (!(isMade(x, y, l, size))) {
        addPlot(x, y, l, size, LUT, hm.get(x), hm.get(y), lText);
//        } else { 
//            showPlot(x, y, l, size, hm.get(x), hm.get(y), lText); 
//            System.out.println("Showing not adding!");
//        }
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

        if (this.useGlobal) {
            cpd.setChartPanelRanges(XYChartPanel.XAXIS, cpd.xMin, cpd.xMax);
            cpd.setChartPanelRanges(XYChartPanel.YAXIS, cpd.yMin, cpd.yMax);
        }

        if (this.useCustom) {
            cpd.setChartPanelRanges(XYChartPanel.XAXIS, AxesLimits.get(0), AxesLimits.get(1));
            cpd.setChartPanelRanges(XYChartPanel.YAXIS, AxesLimits.get(2), AxesLimits.get(3));
        }

        CenterPanel.setOpaque(false);
        CenterPanel.setBackground(new Color(255, 255, 255, 255));
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
        if (pg.getGateAsPoints().size() == 0) {
            //System.out.println("PROFILING: What happened to my points?");
        }
        gates.add(pg);
        //System.out.println("PROFILING: in gate arraylist: " + gates.get(gates.lastIndexOf(pg)).getGateAsPoints());

        this.notifyResetSelectionListeners();
    }

    @Override
    public void quadrantSelection(float x, float y) {

    }

    @Override
    public void imageHighLightSelection(ArrayList gates) {
        this.gates = gates;
        makeOverlayImage(gates, 0, 0, currentX, currentY);
    }

    @Override
    public void onChangeAxes(int x, int y, int l, int size, boolean imagegate) {
//        if (!(isMade(currentX, currentY, currentL, size))) {
//            addExplorationGroup();
//        }
//        if (!(isMade(x, y, l, size))) {
        addPlot(x, y, l, size, LUT, hm.get(x), hm.get(y), hm.get(l));
        //System.out.println("Change Axes, new plot: " + x + ", " + y + ", " + l);
//        } else {
//            showPlot(x, y, l, size,  hm.get(x), hm.get(y), hm.get(l));
//            //System.out.println("Change Axes: " + x + ", " + y + ", " + l);
//        }
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

        CenterPanel.removeAll();

        //setup chart values
        chart = cpd.getUpdatedChartPanel();
        chart.setOpaque(false);

        //setup chart layer
        CenterPanel.setOpaque(false);
        CenterPanel.setBackground(new Color(255, 255, 255, 255));
        CenterPanel.setPreferredSize(chart.getPreferredSize());

        //add overlay 
        this.gl = new GateLayer();
        gl.addPolygonSelectionListener(this);
        gl.addPasteGateListener(this);
        gl.addImageHighLightSelectionListener(this);
        gl.msActive = false;
        //this.gates = new ArrayList();
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
        //System.out.println("PROFILING: Plot updated...");
    }

    @Override
    public void roiModified(ImagePlus ip, int i) {

        ArrayList<Gate> gates = new ArrayList<Gate>();
        try {
            if (ip.getID() == impoverlay.getID()) {
                switch (i) {
                    case RoiListener.COMPLETED:
                        imageGate = true;
                        //System.out.println("PROFILING: XYChartPanel, Roi modified... Completed. Imagegate: " + imageGate);    
                        addPlot(currentX, currentY, currentL, pointsize, LUT, hm.get(currentX), hm.get(currentY), hm.get(currentL));
                        makeOverlayImage(this.gates, 0, 0, currentX, currentY);
                        break;
                    case RoiListener.MOVED:
                        imageGate = true;
                        //System.out.println("PROFILING: XYChartPanel, roiListener, Roi modified... Moved. Imagegate: " + imageGate);                   
                        addPlot(currentX, currentY, currentL, pointsize, LUT, hm.get(currentX), hm.get(currentY), hm.get(currentL));
                        makeOverlayImage(this.gates, 0, 0, currentX, currentY);
                        break;
                    case RoiListener.DELETED:
                        imageGate = false;
                        //System.out.println("PROFILING: XYChartPanel, roiListener, Roi modified... Deleted. Imagegate: " + imageGate);  
                        addPlot(currentX, currentY, currentL, pointsize, LUT, hm.get(currentX), hm.get(currentY), hm.get(currentL));
                        makeOverlayImage(this.gates, 0, 0, currentX, currentY);
                        break;
                    default:
                        break;
                }
            }
        } catch (NullPointerException e) {
        }
    }

    @Override
    public ImagePlus getZProjection() {

        ListIterator itr = gates.listIterator();

        while (itr.hasNext()) {
            Gate gate = (Gate) itr.next();
            gate.setSelected(true);
        }

        makeOverlayImage(gates, 0, 0, currentX, currentY);

        while (itr.hasNext()) {
            Gate gate = (Gate) itr.next();
            gate.setSelected(false);
        }

        //System.out.println("PROFILING: Number of gates: " + gates.size());
        //public void updatePlot(int x, int y, int l, int size) 
        //updatePlot(currentX, currentY, currentL, pointsize);
        ImageStack[] isAll = new ImageStack[impoverlay.getNChannels() + gates.size()];

        for (int i = 1; i <= impoverlay.getNChannels(); i++) {
            isAll[i - 1] = ChannelSplitter.getChannel(impoverlay, i);
        }

        if (gates.size() > 0) {
            for (int i = 0; i < gates.size(); i++) {
                isAll[i + impoverlay.getNChannels()] = gates.get(i).getGateOverlayStack();
            }
        }

        ImagePlus[] images = new ImagePlus[isAll.length];

        ij.process.LUT[] oldLUT = impoverlay.getLuts();

        for (int i = 0; i < isAll.length; i++) {
            images[i] = new ImagePlus("Channel_" + i, isAll[i]);
            StackConverter sc = new StackConverter(images[i]);
            if (i < impoverlay.getNChannels()) {
                images[i].setLut(oldLUT[i]);
            }
            sc.convertToGray8();
        }

        ImagePlus merged = mergeChannels(images, true);

        merged.setDisplayMode(IJ.COMPOSITE);
        merged.show();
        merged.setTitle("Gates_" + impoverlay.getTitle());
        return merged;
    }

    @Override
    public void onPasteGate() {
        this.getParent().validate();
        this.getParent().repaint();
    }

    @Override
    public void setAxesToCurrent() {
        useCustom = false;
        XYPlot plot = (XYPlot) cpd.getChartPanel().getChart().getPlot();
        XYChartPanel.xMin = plot.getDomainAxis().getLowerBound();
        XYChartPanel.xMax = plot.getDomainAxis().getUpperBound();
        XYChartPanel.yMin = plot.getRangeAxis().getLowerBound();
        XYChartPanel.yMax = plot.getRangeAxis().getUpperBound();

        XYChartPanel.xLinear = xScaleLinear;
        XYChartPanel.yLinear = yScaleLinear;
    }

    @Override
    public void setCustomRange(boolean state) {
        useCustom = state;
    }

    @Override
    public void setAxesTo(ArrayList al, boolean x, boolean y, int lutTable) {
        AxesLimits = al;
        xScaleLinear = x;
        yScaleLinear = y;
        LUT = lutTable;
        useCustom = true;
    }

    @Override
    public void setGlobalAxes(boolean state) {
        useGlobal = state;
        useCustom = false;
    }

    @Override
    public boolean getGlobalAxes() {
        return useGlobal;
    }

    @Override
    public int getGatedSelected(ImagePlus ip) {
        ArrayList<ArrayList<Number>> ImageGatedObjects = new ArrayList<ArrayList<Number>>();
        int index = 0;
        ListIterator<MicroObject> itr = objects.listIterator();
        while (itr.hasNext()) {
            MicroObject m = itr.next();
            try {

                if (ip.getRoi().contains((int) m.getCentroidX(), (int) m.getCentroidY())) {
                    ImageGatedObjects.add(measurements.get(index));
                }
                index++;
            } catch (NullPointerException e) {
                return 0;
            }
        }

        Gate gate;
        ListIterator<Gate> gate_itr = gates.listIterator();
        int result = 0;

        while (gate_itr.hasNext()) {
            gate = gate_itr.next();
            if (gate.getSelected()) {
                Path2D path = gate.createPath2DInChartSpace();

                double xValue = 0;
                double yValue = 0;

                ListIterator<ArrayList<Number>> it = ImageGatedObjects.listIterator();
                ArrayList<Number> measured;

                try {
                    while (it.hasNext()) {
                        measured = it.next();
                        if (measured != null) {
                            xValue = measured.get(currentX).floatValue();
                            yValue = measured.get(currentY).floatValue();
                            if (path.contains(xValue, yValue)) {
                                result++;
                            }
                        }

                    }
                } catch (NullPointerException e) {
                    return 0;
                }
            }
        }

        return result;
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public ArrayList<Component> getSettingsContent() {
        ArrayList<Component> al = new ArrayList();

        XYPlot plot = (XYPlot) cpd.getChartPanel().getChart().getPlot();

        String[] cOptions = new String[2];

        cOptions[0] = "Lin";
        cOptions[1] = "Log";

        JComboBox j = new JComboBox(new DefaultComboBoxModel(cOptions));

        al.add(new JLabel("X min"));
        al.add(new JTextField(String.valueOf(Math.round(plot.getDomainAxis().getLowerBound()))));
        al.add(new JLabel("X max"));
        al.add(new JTextField(String.valueOf(Math.round(plot.getDomainAxis().getUpperBound()))));
        al.add(new JComboBox(new DefaultComboBoxModel(cOptions)));
        al.add(new JLabel("Y min"));
        al.add(new JTextField(String.valueOf(Math.round(plot.getRangeAxis().getLowerBound()))));
        al.add(new JLabel("Y max"));
        al.add(new JTextField(String.valueOf(Math.round(plot.getRangeAxis().getUpperBound()))));
        al.add(new JComboBox(new DefaultComboBoxModel(cOptions)));

        return al;

    }

    @Override
    public BufferedImage getBufferedImage() {
        Color color = CenterPanel.getBackground();

        BufferedImage image = new BufferedImage(CenterPanel.getWidth(), CenterPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
        CenterPanel.paint(image.getGraphics());

        JFrame j = new JFrame();

        JFileChooser jf = new JFileChooser(_vtea.LASTDIRECTORY);
        int returnVal = jf.showSaveDialog(CenterPanel);
        File file = jf.getSelectedFile();

        try {

            if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("png")) {

            } else {
                file = new File(file.toString() + ".png");
            }
        } catch (Exception e) {
        }

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            try {

                try {
                    ImageIO.write(image, "png", file);
                    _vtea.LASTDIRECTORY = file.getPath();
                } catch (IOException e) {
                }
            } catch (NullPointerException ne) {
            }

        } else {

        }

        return image;
    }

    @Override
    public void exportGates() {
        ExportGates eg = new ExportGates();
        ArrayList<ArrayList<Point2D.Double>> al = new ArrayList();
        for (int i = 0; i < gates.size(); i++) {
            al.add(gates.get(i).getGateAsPointsInChart());
        }
        eg.export(al);
    }

    @Override
    public void importGates() {
        ImportGates ig = new ImportGates();
        ArrayList<ArrayList<Point2D.Double>> al = ig.importGates();
        ListIterator itr = al.listIterator();

        while (itr.hasNext()) {
            ArrayList<Point2D.Double> al1 = new ArrayList();
            al1 = (ArrayList<Point2D.Double>) itr.next();
            gl.notifyPolygonSelectionListeners(GateImporter.importGates(al1, chart));
        }
    }

    @Override
    public void updateFeatureSpace(HashMap<Integer, String> descriptions, ArrayList<ArrayList<Number>> measurements) {

        this.hm = descriptions;
        this.measurements = measurements;

        H2DatabaseEngine.dropTable(vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe);
        writeCSV(ij.Prefs.getImageJDir() + key);
        startH2Database(ij.Prefs.getImageJDir() + key + ".csv", vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe);

    }

    class ExportGates {

        public ExportGates() {
        }

        public void export(ArrayList<ArrayList<Point2D.Double>> al) {

            JFileChooser jf = new JFileChooser(_vtea.LASTDIRECTORY);
            int returnVal = jf.showSaveDialog(CenterPanel);
            File file = jf.getSelectedFile();

            _vtea.LASTDIRECTORY = file.getPath();

            file = jf.getSelectedFile();
            if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("vtg")) {

            } else {
                file = new File(file.toString() + ".vtg");
            }

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    try {
                        FileOutputStream fos = new FileOutputStream(file);
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        oos.writeObject(al);
                        oos.close();
                    } catch (IOException e) {
                        System.out.println("ERROR: Could not save the file" + e);
                    }
                } catch (NullPointerException ne) {
                    System.out.println("ERROR: NPE in Gate Export");
                }
            } else {
            }
        }

    }

    class ImportGates {

        public ImportGates() {
        }

        protected ArrayList<ArrayList<Point2D.Double>> importGates() {

            JFileChooser jf = new JFileChooser(_vtea.LASTDIRECTORY);
            int returnVal = jf.showOpenDialog(CenterPanel);
            File file = jf.getSelectedFile();

            ArrayList<ArrayList<Point2D.Double>> result = new ArrayList();

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        ObjectInputStream ois = new ObjectInputStream(fis);
                        result = (ArrayList<ArrayList<Point2D.Double>>) ois.readObject();
                        ois.close();
                    } catch (IOException e) {
                        System.out.println("ERROR: Could not open the file.");
                    }
                } catch (ClassNotFoundException ne) {
                    System.out.println("ERROR: Not Found in Gate Export");
                }
            } else {
            }
            return result;
        }

    }

}

class ZComparator implements Comparator<MicroObject> {

    @Override
    public int compare(MicroObject o1, MicroObject o2) {
        if (o1.getCentroidZ() == o2.getCentroidZ()) {
            return 0;
        } else if (o1.getCentroidZ() > o2.getCentroidZ()) {
            return 1;
        } else if (o1.getCentroidZ() < o2.getCentroidZ()) {
            return -1;
        } else {
            return 0;
        }
    }

}
