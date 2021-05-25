/* 
 * Copyright (C) 2020 Indiana University
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
import ij.ImageListener;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.RoiListener;
import ij.gui.TextRoi;
import ij.plugin.ChannelSplitter;
import ij.plugin.Duplicator;
import static ij.plugin.RGBStackMerge.mergeChannels;
import ij.process.ImageConverter;
import ij.process.StackConverter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.showOptionDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.io.FilenameUtils;
import org.jdesktop.jxlayer.JXLayer;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import smile.neighbor.KDTree;
import smile.neighbor.Neighbor;
import vtea._vtea;
import vtea.exploration.listeners.AddClassByMathListener;
import vtea.exploration.listeners.AddFeaturesListener;
import vtea.exploration.listeners.AssignmentListener;
import vtea.exploration.listeners.AxesSetupExplorerPlotUpdateListener;
import vtea.exploration.listeners.DensityMapListener;
import vtea.exploration.listeners.DistanceMapListener;
import vtea.exploration.listeners.GateManagerActionListener;
import vtea.exploration.listeners.GateMathObjectListener;
import vtea.exploration.listeners.LinkedKeyListener;
import vtea.exploration.listeners.ManualClassListener;
import vtea.exploration.listeners.NameUpdateListener;
import vtea.exploration.listeners.NeighborhoodListener;
import vtea.exploration.listeners.PlotAxesPreviewButtonListener;
import vtea.exploration.listeners.PlotUpdateListener;
import vtea.exploration.listeners.SaveGatedImagesListener;
import vtea.exploration.listeners.SubGateExplorerListener;
import vtea.exploration.listeners.SubGateListener;
import vtea.exploration.listeners.UpdateExplorerGuiListener;
import vtea.exploration.listeners.UpdateFeaturesListener;
import vtea.exploration.listeners.UpdatePlotWindowListener;
import vtea.exploration.listeners.colorUpdateListener;
import vtea.exploration.listeners.remapOverlayListener;
import vtea.exploration.plotgatetools.gates.GateLayer;
import vtea.exploration.plotgatetools.gates.PolygonGate;
import vtea.exploration.plotgatetools.listeners.AddGateListener;
import vtea.exploration.plotgatetools.listeners.ChangePlotAxesListener;
import vtea.exploration.plotgatetools.listeners.DeleteGateListener;
import vtea.exploration.plotgatetools.listeners.GateColorListener;
import vtea.exploration.plotgatetools.listeners.ImageHighlightSelectionListener;
import vtea.exploration.plotgatetools.listeners.MakeImageOverlayListener;
import vtea.exploration.plotgatetools.listeners.PolygonSelectionListener;
import vtea.exploration.plotgatetools.listeners.QuadrantSelectionListener;
import vtea.exploration.plotgatetools.listeners.ResetSelectionListener;
import vtea.gui.ComboboxToolTipRenderer;
import vtea.jdbc.H2DatabaseEngine;
import vtea.lut.Black;
import vtea.neighbors.NeighborhoodFactory;
import vtea.processor.GateMathProcessor;
import vtea.spatial.densityMap3d;
import vtea.spatial.distanceMaps2d;
import vteaexploration.GateMathWindow;
import vteaexploration.MicroExplorer;
import vteaexploration.PlotAxesManager;
import vteaexploration.ProgressTracker;
import vteaexploration.TableWindow;
import vteaobjects.MicroNeighborhoodObject;
import vteaobjects.MicroObject;
import vteaobjects.MicroObjectModel;

/**
 *
 * @author vinfrais
 */
public class XYExplorationPanel extends AbstractExplorationPanel implements
        NeighborhoodListener, DensityMapListener, DistanceMapListener,
        WindowListener, RoiListener, PlotUpdateListener, PolygonSelectionListener,
        QuadrantSelectionListener, ImageHighlightSelectionListener,
        ChangePlotAxesListener, AddFeaturesListener, UpdatePlotWindowListener,
        AddGateListener, DeleteGateListener, GateColorListener, SaveGatedImagesListener,
        SubGateListener, PlotAxesPreviewButtonListener, ImageListener,
        NameUpdateListener, colorUpdateListener, remapOverlayListener,
        ManualClassListener, AssignmentListener, UpdateFeaturesListener,
        GateManagerActionListener, AddClassByMathListener, GateMathObjectListener {

    static String printResult = "";
    static public int testCounter = 0;
    int impZ;
    GateMathProcessor gmp;

    public static double getMaximumOfData(ArrayList measurements, int l) {

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

    public static double getMinimumOfData(ArrayList measurements, int l) {

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

    XYChartPanel cpd;
    TableWindow gm;
    private boolean useGlobal = false;
    private boolean useCustomX = false;
    private boolean useCustomY = false;
    int selected = 0;
    int gated = 0;
    String key = "";
    String keySQLSafe = "";
    boolean updateimage = true;
    private Connection connection;

    PlotAxesManager AxesManager;
    int explorerXaxisIndex = 0;
    int explorerYaxisIndex = 0;
    int explorerLutIndex = 0;
    int explorerXposition = 0;
    int explorerYposition = 0;
    int explorerPointSizeIndex = 0;
    LookupPaintScale lps;

    public XYExplorationPanel(String key, Connection connection,
            ArrayList measurements, ArrayList<String> descriptions,
            HashMap<Integer, String> hm, ArrayList<MicroObject> objects,
            String title) {

        super();

        gm = new TableWindow(title);

        configureListeners();

        this.key = key;
        this.objects = objects;
        this.measurements = measurements;
        this.descriptions = descriptions;
        this.connection = connection;

        keySQLSafe = key.replace("-", "_");

        writeCSV(_vtea.DATABASE_DIRECTORY + System.getProperty("file.separator") + key);
        startH2Database(_vtea.DATABASE_DIRECTORY + System.getProperty("file.separator") + key + ".csv",
                vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe);

        this.LUT = 0;
        this.hm = hm;
        this.pointsize = MicroExplorer.POINTSIZE;

        distanceMaps2D = new distanceMaps2d();
        densityMaps3D = new densityMap3d();

        AxesManager = new PlotAxesManager(key, connection, title, hm);

        //set current LUT to LUTDefault.class
        String lText = "";
        if (MicroExplorer.LUTSTART < 0) {
            lText = "";
        } else {
            lText = hm.get(MicroExplorer.LUTSTART);
        }

        double max = 0;
        double min = 0;
        if (MicroExplorer.LUTSTART >= 0) {
            max = Math.round(getMaximumOfData(H2DatabaseEngine.getColumn(
                    vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe, lText), 0));
            min = Math.round(getMinimumOfData(H2DatabaseEngine.getColumn(
                    vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe, lText), 0));
            double range = max - min;
            if (max == 0) {
                max = 1;
            }
        }

        Black defaultLUT = new Black();
        lps = defaultLUT.getPaintScale(min, max);

        //default plot 
        addPlot(MicroExplorer.XSTART, MicroExplorer.YSTART, MicroExplorer.LUTSTART,
                MicroExplorer.POINTSIZE, 0, hm.get(1), hm.get(4), hm.get(2));

        invokeAxesSettingsDialog(this.getX(), this.getY() + this.getHeight());

    }

    private void configureListeners() {

        Roi.addRoiListener(this);

        gm.addUpdateNameListener(this);
        gm.addUpdateColorListener(this);
        gm.addRemapOverlayListener(this);
        gm.addGateActionListener(this);

        gl.addPolygonSelectionListener(this);
        gl.addQuadrantSelectionListener(this);
        gl.addPasteGateListener(this);
        gl.addDeleteGateListener(this);
        gl.addGateColorListener(this);
        gl.addImageHighLightSelectionListener(this);
        gl.addDistanceMapListener(this);
        gl.addDensityMapListener(this);
        gl.addNeighborhoodListener(this);
        gl.addClassificationListener(this);
        gl.addAssignmentListener(this);

    }

    private XYChartPanel createChartPanel(int x, int y, int l, String xText,
            String yText, String lText, int size, ImagePlus ip, boolean imageGate,
            Color imageGateOutline) {
        return new XYChartPanel(objects, measurements, x, y, l, xText, yText,
                lText, size, ip, imageGate, imageGateOutline);
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

                //MicroObject vol = (MicroObject) objects.get(i);
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

    public void makeGateOverlayImage() {
        
        if(this.mapGates){

        if (gates.size() > 0) {

            PolygonGate gate;
            ListIterator<PolygonGate> gate_itr = gates.listIterator();

            int total = 0;
            int gated = 0;
            int selected = 0;
            int gatedSelected = 0;
            int gatecount = gates.size();

            BufferedImage placeholder = new BufferedImage(impoverlay.getWidth(),
                    impoverlay.getHeight(), BufferedImage.TYPE_INT_ARGB);

            BufferedImage selections = new BufferedImage(impoverlay.getWidth(),
                    impoverlay.getHeight(), BufferedImage.TYPE_INT_ARGB);

            Overlay overlay = new Overlay();

            ImageRoi ir = new ImageRoi(0, 0, placeholder);

            int i = impoverlay.getZ() - 1;

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

                    ArrayList<ArrayList> resultKey
                            = H2DatabaseEngine.getObjectsInRange2D(path,
                                    vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe,
                                    gate.getXAxis(), path.getBounds2D().getX(),
                                    path.getBounds2D().getX() + path.getBounds2D().getWidth(),
                                    gate.getYAxis(), path.getBounds2D().getY(),
                                    path.getBounds2D().getY() + path.getBounds2D().getHeight());

                    ListIterator<ArrayList> itr = resultKey.listIterator();

                    while (itr.hasNext()) {
                        ArrayList al = itr.next();
                        int object = ((Number) (al.get(0))).intValue();
                        result.add(volumes.get(object));
                    }

                    int count = 0;

                    selected = result.size();

                    total = volumes.size();

                    gated = getGatedObjects(impoverlay);
                    gatedSelected = getGatedSelected(impoverlay);

                    Collections.sort(result, new ZComparator());

                    Graphics2D g2 = selections.createGraphics();

                    ListIterator<MicroObject> vitr = result.listIterator();
                    boolean inZ = true;
                    while (vitr.hasNext()) {
                        MicroObject vol = (MicroObject) vitr.next();
                        inZ = true;
                        if (i >= vol.getMinZ() && i <= vol.getMaxZ()) {
                            inZ = false;
                        }
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

//                    java.awt.Font f = new Font("Arial", Font.BOLD, 12);
//
//                    BigDecimal percentage = new BigDecimal(selected);
//                    BigDecimal totalBD = new BigDecimal(total);
//                    percentage = percentage.divide(totalBD, 3, BigDecimal.ROUND_CEILING);
//
//                    BigDecimal percentageGated = new BigDecimal(gated);
//                    BigDecimal totalGatedBD = new BigDecimal(total);
//                    percentageGated = percentageGated.divide(totalGatedBD, 3,
//                            BigDecimal.ROUND_CEILING);
//
//                    BigDecimal percentageGatedSelected = new BigDecimal(gatedSelected);
//                    BigDecimal totalGatedSelectedBD = new BigDecimal(total);
//                    percentageGatedSelected
//                            = percentageGatedSelected.divide(totalGatedSelectedBD,
//                                    3, BigDecimal.ROUND_CEILING);
//
//                    TextRoi textTotal = new TextRoi(5, 10, selected
//                            + "/" + total + " gated ("
//                            + 100 * percentage.floatValue() + "%)");
//                    printResult = textTotal.getText();
//                    if (gated > 0) {
//                        textTotal = new TextRoi(5, 10, selected + "/" + total
//                                + " total (" + 100 * percentage.floatValue()
//                                + "%)" + "; " + gated + "/" + total + " roi ("
//                                + 100 * percentageGated.floatValue() + "%)"
//                                + "; " + gatedSelected + "/" + total + " overlap ("
//                                + 100 * percentageGatedSelected.floatValue() + "%)", f);
//                        printResult = textTotal.getText();
//                    }
                    gate.setObjectsInGate(selected);
                    gate.setTotalObjects(total);

                }
                ir.setPosition(0, i + 1, 0);
                ir.setOpacity(0.4);
                overlay.selectable(false);
                overlay.add(ir);

                impoverlay.setOverlay(overlay);
                impoverlay.draw();

                if (impoverlay.getDisplayMode() != IJ.COMPOSITE) {
                    impoverlay.setDisplayMode(IJ.COMPOSITE);
                }
                impoverlay.show();
                System.gc();

            }
        } else {
            impoverlay.getOverlay().clear();
            System.gc();
        }
    }
    } 
//    
    public void makeOverlayImageAndCalculate(ArrayList<PolygonGate> gates, int x, int y,
            int xAxis, int yAxis) {

        this.updateimage = false;

        //System.out.println("PROFILING: Mapping cells...");
        if (!gm.isVisible()) {
            gm.setVisible(true);
        }

        if (gates.size() > 0) {

            PolygonGate gate;
            ListIterator<PolygonGate> gate_itr = gates.listIterator();

            int total = 0;
            int gated = 0;
            int selected = 0;
            int gatedSelected = 0;
            int gatecount = gates.size();

            BufferedImage placeholder = new BufferedImage(impoverlay.getWidth(),
                    impoverlay.getHeight(), BufferedImage.TYPE_INT_ARGB);
//        ImageStack gateOverlay = new ImageStack(impoverlay.getWidth(),
//                impoverlay.getHeight());

            BufferedImage selections = new BufferedImage(impoverlay.getWidth(),
                    impoverlay.getHeight(), BufferedImage.TYPE_INT_ARGB);

            Overlay overlay = new Overlay();

            ImageRoi ir = new ImageRoi(0, 0, placeholder);

            int i = impoverlay.getZ() - 1;

            //
            while (gate_itr.hasNext()) {
                gate = gate_itr.next();

                if (gate.getSelected()) {

                    gate.getXAxis().equals(descriptions.get(this.currentX));

                    Path2D.Double path = gate.createPath2DInChartSpace();

                    ArrayList<MicroObject> result = new ArrayList<MicroObject>();
                    ArrayList<MicroObject> resultFinal = new ArrayList<MicroObject>();

                    ArrayList<MicroObject> volumes = (ArrayList) objects;
                    MicroObjectModel volume;

                    double xValue = 0;
                    double yValue = 0;

                    gated = getGatedObjects(impoverlay);
                    gatedSelected = getGatedSelected(impoverlay);

                    ArrayList<ArrayList> resultKey
                            = H2DatabaseEngine.getObjectsInRange2D(path,
                                    vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe,
                                    gate.getXAxis(), path.getBounds2D().getX(),
                                    path.getBounds2D().getX() + path.getBounds2D().getWidth(),
                                    gate.getYAxis(), path.getBounds2D().getY(),
                                    path.getBounds2D().getY() + path.getBounds2D().getHeight());

                    ListIterator<ArrayList> itr = resultKey.listIterator();

                    //System.out.println("PROFILING: Returned object count: " + resultKey.size());
                    while (itr.hasNext()) {
                        ArrayList al = itr.next();
                        int object = ((Number) (al.get(0))).intValue();
                        result.add(volumes.get(object));
                    }
                    try {
                        for (int j = 0; j < result.size(); j++) {
                            ArrayList<Number> measured = resultKey.get(j);
                            xValue = measured.get(1).doubleValue();
                            yValue = measured.get(2).doubleValue();
                            if (path.contains(xValue, yValue)) {
                                resultFinal.add((MicroObject) result.get(j));
                            }
                        }
                    } catch (NullPointerException e) {
                    }

                    int count = 0;

                    selected = result.size();

                    total = volumes.size();

                    Collections.sort(result, new ZComparator());

                    Graphics2D g2 = selections.createGraphics();

                    if(this.mapGates){
                    
                    ListIterator<MicroObject> vitr = result.listIterator();
                    boolean inZ = true;
                    while (vitr.hasNext()) {
                        MicroObject vol = (MicroObject) vitr.next();
                        inZ = true;
                        if (i >= vol.getMinZ() && i <= vol.getMaxZ()) {
                            inZ = false;
                        }
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
                    }

                    //text for overlay
                    java.awt.Font f = new Font("Arial", Font.BOLD, 12);

                    BigDecimal percentage = new BigDecimal(selected);
                    BigDecimal totalBD = new BigDecimal(total);
                    percentage = percentage.divide(totalBD, 3, BigDecimal.ROUND_CEILING);

                    BigDecimal percentageGated = new BigDecimal(gated);
                    BigDecimal totalGatedBD = new BigDecimal(total);
                    percentageGated = percentageGated.divide(totalGatedBD, 3,
                            BigDecimal.ROUND_CEILING);

                    BigDecimal percentageGatedSelected = new BigDecimal(gatedSelected);
                    BigDecimal totalGatedSelectedBD = new BigDecimal(total);
                    percentageGatedSelected
                            = percentageGatedSelected.divide(totalGatedSelectedBD,
                                    3, BigDecimal.ROUND_CEILING);
                    TextRoi textTotal = new TextRoi(5, 10, selected
                            + "/" + total + " gated ("
                            + 100 * percentage.floatValue() + "%)");
                    printResult = textTotal.getText();
                    if (gated > 0) {
                        textTotal = new TextRoi(5, 10, selected + "/" + total
                                + " total (" + 100 * percentage.floatValue()
                                + "%)" + "; " + gated + "/" + total + " roi ("
                                + 100 * percentageGated.floatValue() + "%)"
                                + "; " + gatedSelected + "/" + total + " overlap ("
                                + 100 * percentageGatedSelected.floatValue() + "%)", f);
                        printResult = textTotal.getText();
                    }

                    gate.setObjectsInGate(selected);
                    gate.setTotalObjects(total);
                }
                
                if(this.mapGates){

                ir.setPosition(0, i + 1, 0);

                //old setPosition not functional as of imageJ 1.5m
                ir.setOpacity(0.4);
                overlay.selectable(false);
                overlay.add(ir);

                impoverlay.setOverlay(overlay);
                impoverlay.draw();

                if (impoverlay.getDisplayMode() != IJ.COMPOSITE) {
                    impoverlay.setDisplayMode(IJ.COMPOSITE);
                }
                impoverlay.show();
                }
                gm.setMeasurementsText(printResult);

                gm.updateTable(gates, mapGates);

            }
        } else {
            if (mapGates || impoverlay.getOverlay() != null) {
                impoverlay.getOverlay().clear();
            }
            gm.setMeasurementsText("No gate selected...");
        }
        this.updateimage = true;

    }
    

    public ArrayList<ImageStack> makeOverlayVolume(ArrayList<PolygonGate> gates, int x, int y,
            int xAxis, int yAxis) {

        ArrayList<ImageStack> alIs = new ArrayList<>();

        PolygonGate gate;
        ListIterator<PolygonGate> gate_itr = gates.listIterator();

//        int total = 0;
//        int gated = 0;
//        int selected = 0;
        int gatedSelected = 0;
        int gatecount = gates.size();

        while (gate_itr.hasNext()) {
            gate = gate_itr.next();

            if (gate.getSelected()) {

                Path2D.Double path = gate.createPath2DInChartSpace();

                ArrayList<MicroObject> result = new ArrayList<MicroObject>();
                ArrayList<MicroObject> volumes = (ArrayList) objects;
                MicroObjectModel volume;

                double xValue = 0;
                double yValue = 0;

                ArrayList<ArrayList> resultKey
                        = H2DatabaseEngine.getObjectsInRange2D(path,
                                vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe,
                                gate.getXAxis(), path.getBounds2D().getX(),
                                path.getBounds2D().getX() + path.getBounds2D().getWidth(),
                                gate.getYAxis(), path.getBounds2D().getY(),
                                path.getBounds2D().getY() + path.getBounds2D().getHeight());

                ListIterator<ArrayList> itr = resultKey.listIterator();

                int offsetx = 0;
                int offsety = 0;

                if (impoverlay.getRoi() != null) {
                    Roi r = impoverlay.getRoi();
                    offsetx = r.getBounds().x;
                    offsety = r.getBounds().y;
                }

                while (itr.hasNext()) {
                    ArrayList al = itr.next();
                    int object = ((Number) (al.get(0))).intValue();
                    MicroObject obj = volumes.get(object);

                    if (impoverlay.getRoi() != null) {
                        if (impoverlay.getRoi().contains((int) obj.getCentroidX(),
                                (int) obj.getCentroidY())) {
                            result.add(volumes.get(object));

                        }
                    } else {
                        result.add(volumes.get(object));
                    }
                }

                Overlay overlay = new Overlay();

                int count = 0;

                ImageStack gateOverlay = new ImageStack(impoverlay.getWidth(),
                        impoverlay.getHeight());

                gated = getGatedObjects(impoverlay);
                gatedSelected = getGatedSelected(impoverlay);

                Collections.sort(result, new ZComparator());

                for (int i = 0; i <= impoverlay.getNSlices(); i++) {

                    BufferedImage selections = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_GRAY);

                    if (impoverlay.getRoi() != null) {
                        Roi r = impoverlay.getRoi();
                        selections = new BufferedImage(r.getBounds().width,
                                r.getBounds().height, BufferedImage.TYPE_BYTE_GRAY);
                    } else {

                        selections = new BufferedImage(impoverlay.getWidth(),
                                impoverlay.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
                    }

                    Graphics2D g2 = selections.createGraphics();
                    ImageRoi ir = new ImageRoi(0, 0, selections);
                    ListIterator<MicroObject> vitr = result.listIterator();
                    boolean inZ = true;
                    while (vitr.hasNext()) {
                        MicroObject vol = (MicroObject) vitr.next();
                        inZ = true;
                        if (i >= vol.getMinZ() && i <= vol.getMaxZ()) {
                            inZ = false;
                        }
                        try {
                            int[] x_pixels = vol.getXPixelsInRegion(i);
                            int[] y_pixels = vol.getYPixelsInRegion(i);
                            for (int c = 0; c < x_pixels.length; c++) {
                                g2.setColor(gate.getColor());
                                g2.drawRect(x_pixels[c] - offsetx, y_pixels[c] - offsety, 1, 1);
                            }
                            ir = new ImageRoi(0, 0, selections);
                            count++;
                        } catch (NullPointerException e) {
                        }
                    }

                    ir.setPosition(0, i, 0);
                    ir.setOpacity(0.4);
                    overlay.selectable(false);
                    overlay.add(ir);

                    gateOverlay.addSlice(ir.getProcessor());
                }

                //impoverlay.setOverlay(overlay);
                //gate.setGateOverlayStack(gateOverlay);
                alIs.add(gateOverlay);
                gatecount++;
            }
            //impoverlay.draw();
//            if (impoverlay.getDisplayMode() != IJ.COMPOSITE) {
//                impoverlay.setDisplayMode(IJ.COMPOSITE);
//            } 
        }
//        impoverlay.show();
        return alIs;
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
        PolygonGate gate;
        ListIterator<PolygonGate> gate_itr = gates.listIterator();

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
        return result.size();
    }

    @Override
    public void addMakeImageOverlayListener(MakeImageOverlayListener listener) {
        overlaylisteners.add(listener);
    }

    @Override
    public void notifyMakeImageOverlayListeners(ArrayList<PolygonGate> gates) {
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
    public JPanel addPlot(int x, int y, int l, int size, int LUT, String xText,
            String yText, String lText) {
        currentX = x;
        currentY = y;
        currentL = l;
        pointsize = size;
        CenterPanel.removeAll();
        this.LUT = LUT;

        if (imageGate) {
            imageGateColor = impoverlay.getRoi().getStrokeColor();
        }

        //System.out.println("XYExplorationPanel, addplot:" + System.currentTimeMillis());
        cpd = new XYChartPanel(keySQLSafe, objects, x, y, l, xText, yText, lText, pointsize, impoverlay, imageGate, imageGateColor, lps);

        cpd.addUpdatePlotWindowListener(this);

        chart = cpd.getChartPanel();
        chart.setOpaque(false);

        //XYPlot plot = (XYPlot) cpd.getChartPanel().getChart().getPlot();
        if (useGlobal) {
            cpd.setChartPanelRanges(XYChartPanel.XAXIS, XYChartPanel.xMin, XYChartPanel.xMax, xScaleLinear);
            cpd.setChartPanelRanges(XYChartPanel.YAXIS, XYChartPanel.yMin, XYChartPanel.yMax, yScaleLinear);
        }

        if (useCustomX) {
            cpd.setChartPanelRanges(XYChartPanel.XAXIS, AxesLimits.get(0), AxesLimits.get(1), xScaleLinear);
        }
        if (useCustomY) {
            cpd.setChartPanelRanges(XYChartPanel.YAXIS, AxesLimits.get(2), AxesLimits.get(3), yScaleLinear);
        }

        //setup chart layer
        CenterPanel.setOpaque(false);
        CenterPanel.setBackground(new Color(255, 255, 255, 255));
        CenterPanel.setPreferredSize(chart.getPreferredSize());

        //add overlay 
        this.gl = new GateLayer();

        gl.addImagesListener(this);
        gl.addSubGateListener(this);
        gl.addPolygonSelectionListener(this);
        gl.addPasteGateListener(this);
        gl.addDeleteGateListener(this);
        gl.addGateColorListener(this);
        gl.addImageHighLightSelectionListener(this);
        gl.addDistanceMapListener(this);
        gl.addDensityMapListener(this);
        gl.addNeighborhoodListener(this);
        gl.addClassificationListener(this);
        gl.addAssignmentListener(this);

        gl.msActive = false;

        JXLayer<JComponent> gjlayer = gl.createLayer(chart, gates, hm.get(currentX), hm.get(currentY));

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
        al.add(this.cpd);
        al.add(gl.getGates());
        ExplorationItems.add(al);
    }

    @Override
    public void updatePlot(int x, int y, int l, int size) {
        this.explorerXaxisIndex = x;
        this.explorerYaxisIndex = y;
        this.explorerLutIndex = l;

        this.explorerPointSizeIndex = size;

        String lText = "";
        if (l < 0) {
            lText = "";
        } else {
            lText = hm.get(l);
        }

        lps = AxesManager.getLookupPaintScale(l);

        addPlot(x, y, l, size, LUT, hm.get(x), hm.get(y), lText);

//                if (this.useGlobal) {
//            cpd.setChartPanelRanges(XYChartPanel.XAXIS, XYChartPanel.xMin, XYChartPanel.xMax, xScaleLinear);
//            cpd.setChartPanelRanges(XYChartPanel.YAXIS, XYChartPanel.yMin, XYChartPanel.yMax, yScaleLinear);
//        } else {
//
//            if (useCustomX) {
//                cpd.setChartPanelRanges(XYChartPanel.XAXIS, AxesLimits.get(0), AxesLimits.get(1), xScaleLinear);
//            } else {
//                HashMap<String, Integer> xAxisLimits = getLimits(x);
//                cpd.setChartPanelRanges(XYChartPanel.XAXIS, xAxisLimits.get("min"), xAxisLimits.get("max"), xScaleLinear);
//            }
//            if (useCustomY) {
//                cpd.setChartPanelRanges(XYChartPanel.YAXIS, AxesLimits.get(2), AxesLimits.get(3), yScaleLinear);
//            } else {
//                HashMap<String, Integer> yAxisLimits = getLimits(y);
//                cpd.setChartPanelRanges(XYChartPanel.YAXIS, yAxisLimits.get("min"), yAxisLimits.get("max"), yScaleLinear);
//
//            }
//        }
//        if(explorerLutIndex == l){
//
//        } else {
        //  }
        AxesManager.setAxesSetupAxisLimits(this.getSettingsContent());

        this.notifyUpdateExplorerGUIListener();

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
    public XYChartPanel getPanel(int x, int y, int l, int size, String xText,
            String yText, String lText) {
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

        if (imageGate) {
            imageGateColor = impoverlay.getRoi().getStrokeColor();
        }

        return createChartPanel(x, y, l, xText, yText, lText, pointsize,
                impoverlay, imageGate, imageGateColor);
    }

    @Override
    public PolygonGate getGates(int x, int y, int l, int size) {
        String key = x + "_" + y;
        if (isMade(x, y, l, size)) {
            ListIterator<ArrayList> itr = ExplorationItems.listIterator();
            String test;
            while (itr.hasNext()) {
                test = itr.next().get(0).toString();
                if (key.equals(test)) {
                    return (PolygonGate) itr.next().get(1);
                }
            }
        }
        return null;
    }

    @Override
    public void showPlot(int x, int y, int l, int size, String xText,
            String yText, String lText) {

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
            cpd.setChartPanelRanges(XYChartPanel.XAXIS, XYChartPanel.xMin, XYChartPanel.xMax, xScaleLinear);
            cpd.setChartPanelRanges(XYChartPanel.YAXIS, XYChartPanel.yMin, XYChartPanel.yMax, yScaleLinear);
        }

        if (useCustomX) {
            cpd.setChartPanelRanges(XYChartPanel.XAXIS, AxesLimits.get(0), AxesLimits.get(1), xScaleLinear);
        }
        if (useCustomY) {
            cpd.setChartPanelRanges(XYChartPanel.YAXIS, AxesLimits.get(2), AxesLimits.get(3), yScaleLinear);
        }

        CenterPanel.setOpaque(false);
        CenterPanel.setBackground(new Color(255, 255, 255, 255));
        CenterPanel.setPreferredSize(chart.getPreferredSize());
        JXLayer<JComponent> gjlayer = gl.createLayer(chart, gates, hm.get(currentX), hm.get(currentY));
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

        }
        //Need to do something about this

        pg.setXAxis(hm.get(this.currentX));
        pg.setYAxis(hm.get(this.currentY));
        pg.setName("Untitled");
        gates.add(pg);
        notifyResetSelectionListeners();

    }

    @Override
    public void addQuadrantGate(ArrayList<PolygonGate> al) {
        setMultipleGates(al);
    }

    @Override
    public void imageHighLightSelection(ArrayList gates) {
        this.gates = gates;
        testCounter++;
        //System.out.println("PROFILING: highlightlistener called by image highlight: " + testCounter);
        makeOverlayImageAndCalculate(gates, 0, 0, currentX, currentY);
    }

    @Override
    public void onChangeAxes(int x, int y, int l, int size, boolean imagegate) {
        addPlot(x, y, l, size, LUT, hm.get(x), hm.get(y), hm.get(l));
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
    public void addSubgateListener(SubGateExplorerListener listener) {
        SubGateListeners.add(listener);
    }

    @Override
    public void notifySubgateListener(ArrayList<MicroObject> objects,
            ArrayList<ArrayList<Number>> measurements) {
        for (SubGateExplorerListener listener : SubGateListeners) {
            listener.makeSubGateExplorer(objects, measurements);
        }
    }

    @Override
    public void addLinkedKeyListener(LinkedKeyListener listener) {
        linkedKeyListeners.add(listener);
    }

    @Override
    public void notifyLinkedKeyListener(String linkedKey) {
        for (LinkedKeyListener listener : linkedKeyListeners) {
            listener.addLinkedKey(linkedKey);
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
        gl.addDeleteGateListener(this);
        gl.addGateColorListener(this);
        gl.addImageHighLightSelectionListener(this);
        gl.addDistanceMapListener(this);
        gl.addDensityMapListener(this);
        gl.addNeighborhoodListener(this);
        gl.addClassificationListener(this);
        gl.addAssignmentListener(this);

        gl.msActive = false;
        JXLayer<JComponent> gjlayer = gl.createLayer(chart, gates, hm.get(currentX), hm.get(currentY));
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
        impoverlay.addImageListener(this);
    }

    @Override
    public void onPlotUpdateListener() {
        this.getParent().validate();
        this.getParent().repaint();
    }

    @Override
    public void roiModified(ImagePlus ip, int i) {

        ArrayList<PolygonGate> gates = new ArrayList<PolygonGate>();
        try {
            if (ip.getID() == impoverlay.getID()) {
                switch (i) {
                    case RoiListener.COMPLETED:
                        imageGate = true;
                        addPlot(currentX, currentY, currentL, pointsize, LUT,
                                hm.get(currentX), hm.get(currentY),
                                hm.get(currentL));
                        makeOverlayImageAndCalculate(this.gates, 0, 0, currentX, currentY);
                        break;
                    case RoiListener.MOVED:
                        imageGate = true;
                        addPlot(currentX, currentY, currentL, pointsize, LUT,
                                hm.get(currentX), hm.get(currentY),
                                hm.get(currentL));
                        makeOverlayImageAndCalculate(this.gates, 0, 0, currentX, currentY);
                        break;
                    case RoiListener.DELETED:
                        imageGate = false;
                        addPlot(currentX, currentY, currentL, pointsize, LUT,
                                hm.get(currentX), hm.get(currentY), hm.get(currentL));
                        makeOverlayImageAndCalculate(this.gates, 0, 0, currentX, currentY);
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

        Overlay over = impoverlay.getOverlay();

        while (itr.hasNext()) {
            PolygonGate gate = (PolygonGate) itr.next();
            gate.setSelected(true);
        }

        ArrayList<ImageStack> alIs = makeOverlayVolume(gates, 0, 0,
                currentX, currentY);

        while (itr.hasNext()) {
            PolygonGate gate = (PolygonGate) itr.next();
            gate.setSelected(false);
        }

        ImageStack[] isAll = new ImageStack[impoverlay.getNChannels()
                + gates.size()];

        //get channels from original image
        for (int i = 1; i <= impoverlay.getNChannels(); i++) {

            if (impoverlay.getRoi() != null) {
                isAll[i - 1] = ChannelSplitter.getChannel(new Duplicator().run(impoverlay), i);
            } else {
                isAll[i - 1] = ChannelSplitter.getChannel(impoverlay, i);
            }
        }

        if (gates.size() > 0) {
            for (int i = 0; i < gates.size(); i++) {
                isAll[i + impoverlay.getNChannels()]
                        = alIs.get(i);
                //= this.GateOverlays.get(i);
                //= gates.get(i).getGateOverlayStack();
            }
        }
        ImagePlus[] images = new ImagePlus[isAll.length];

        ij.process.LUT[] oldLUT = impoverlay.getLuts();

        for (int i = 0; i < isAll.length; i++) {
            images[i] = new ImagePlus("Channel_" + i, isAll[i]);
            if (impoverlay.getSlice() > 1) {
                StackConverter sc = new StackConverter(images[i]);
                if (i < impoverlay.getNChannels()) {
                    images[i].setLut(oldLUT[i]);
                }
                sc.convertToGray8();
            } else {
                ImageConverter ic = new ImageConverter(images[i]);
                if (i < impoverlay.getNChannels()) {
                    images[i].setLut(oldLUT[i]);
                }
                ic.convertToGray8();
            }
        }

        ImagePlus merged = mergeChannels(images, true);

        merged.setDisplayMode(IJ.COMPOSITE);
        merged.show();
        merged.setTitle("Gates_" + impoverlay.getTitle());
        //makeOverlayImageAndCalculate(gates, 0, 0, currentX, currentY);
        //impoverlay.getOverlay().clear();
        //impoverlay.setOverlay(over);
        return merged;
    }

    @Override
    public void onDeleteGate(ArrayList<PolygonGate> gt) {
        gates = gt;
        gm.updateTable(gates, mapGates);
        gm.pack();
        gm.repaint();
        makeOverlayImageAndCalculate(gates, 0, 0, currentX, currentY);

    }

    @Override
    public void onPasteGate(ArrayList<PolygonGate> gt) {
        gates = gt;
        //gm.updateTable(gates);
        gm.addGateToTable(gates.get(gates.size() - 1));
        gm.pack();
        gm.repaint();
        makeOverlayImageAndCalculate(gates, 0, 0, currentX, currentY);
    }

    @Override
    public void setAxesToCurrent() {
        //useCustom = false;
        XYPlot plot = (XYPlot) cpd.getChartPanel().getChart().getPlot();
        XYChartPanel.xMin = plot.getDomainAxis().getLowerBound();
        XYChartPanel.xMax = plot.getDomainAxis().getUpperBound();
        XYChartPanel.yMin = plot.getRangeAxis().getLowerBound();
        XYChartPanel.yMax = plot.getRangeAxis().getUpperBound();
        XYChartPanel.xLinear = xScaleLinear;
        XYChartPanel.yLinear = yScaleLinear;
    }

    @Override
    public void setCustomRange(int Axis, boolean state) {
        if (Axis == MicroExplorer.XAXIS) {
            useCustomX = state;
        }
        if (Axis == MicroExplorer.YAXIS) {
            useCustomY = state;
        }

    }

    @Override
    public void setAxesTo(ArrayList al, boolean x, boolean y, int lutTable) {
        AxesLimits = al;
        xScaleLinear = x;
        yScaleLinear = y;
        LUT = lutTable;
        useCustomX = true;
        useCustomY = true;

        // Imitate updatePlot() functionality as MicroExplorer is no more 
        // communicating with PlotAxesSetup.
        String lText = "";
        if (this.LUT < 0) {
            lText = "";
        } else {
            lText = hm.get(LUT);
        }

        lps = AxesManager.getLookupPaintScale(LUT);

        updatePlot(this.explorerXaxisIndex, this.explorerYaxisIndex,
                this.explorerLutIndex, this.explorerPointSizeIndex);

        notifyAxesSetupExplorerPlotUpdateListener(this.explorerXaxisIndex,
                this.explorerYaxisIndex, this.explorerLutIndex,
                this.explorerPointSizeIndex);
    }

    @Override
    public void setGlobalAxes(boolean state) {
        useGlobal = state;
        useCustomX = false;
        useCustomY = false;
    }

    @Override
    public boolean getGlobalAxes() {
        return useGlobal;
    }

    @Override
    public int getGatedSelected(ImagePlus ip) {
        ArrayList<ArrayList<Number>> ImageGatedObjects
                = new ArrayList<ArrayList<Number>>();
        int index = 0;
        ListIterator<MicroObject> itr = objects.listIterator();
        while (itr.hasNext()) {
            MicroObject m = itr.next();
            try {
                if (ip.getRoi().contains((int) m.getCentroidX(),
                        (int) m.getCentroidY())) {
                    ImageGatedObjects.add(measurements.get(index));
                }
                index++;
            } catch (NullPointerException e) {
                return 0;
            }
        }

        PolygonGate gate;
        ListIterator<PolygonGate> gate_itr = gates.listIterator();
        int result = 0;

        while (gate_itr.hasNext()) {
            gate = gate_itr.next();
            if (gate.getSelected()) {
                Path2D path = gate.createPath2DInChartSpace();

                double xValue = 0;
                double yValue = 0;

                ListIterator<ArrayList<Number>> it
                        = ImageGatedObjects.listIterator();
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

        JComboBox x = new JComboBox(new DefaultComboBoxModel(cOptions));
        JComboBox y = new JComboBox(new DefaultComboBoxModel(cOptions));

        if (this.xScaleLinear) {
            x.setSelectedIndex(0);
        } else {
            x.setSelectedIndex(1);
        }

        if (this.yScaleLinear) {
            y.setSelectedIndex(0);
        } else {
            y.setSelectedIndex(1);
        }

        al.add(new JLabel("X min"));
        al.add(new JTextField(String.valueOf(Math.round(plot.getDomainAxis().getLowerBound()))));
        al.add(new JLabel("X max"));
        al.add(new JTextField(String.valueOf(Math.round(plot.getDomainAxis().getUpperBound()))));
        al.add(x);
        al.add(new JLabel("Y min"));
        al.add(new JTextField(String.valueOf(Math.round(plot.getRangeAxis().getLowerBound()))));
        al.add(new JLabel("Y max"));
        al.add(new JTextField(String.valueOf(Math.round(plot.getRangeAxis().getUpperBound()))));
        al.add(y);

        return al;

    }

    @Override
    public BufferedImage getBufferedImage() {

        BufferedImage image = new BufferedImage(cpd.getChartPanel().getWidth(), cpd.getChartPanel().getHeight(), BufferedImage.TYPE_INT_ARGB);
        cpd.getChartPanel().paint(image.getGraphics());

        File file;
        int choice = JOptionPane.OK_OPTION;
        int returnVal = JFileChooser.CANCEL_OPTION;
        do {
            JFileChooser jf = new JFileChooser(_vtea.LASTDIRECTORY);
            returnVal = jf.showSaveDialog(cpd.getChartPanel());
            file = jf.getSelectedFile();

            try {

                if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("png")) {

                } else {
                    file = new File(file.toString() + ".png");
                }
            } catch (Exception e) {
            }

            if (file.exists()) {
                String message = String.format("%s already exists\nOverwrite it?",
                        file.getName());
                choice = JOptionPane.showConfirmDialog(CenterPanel, message,
                        "Overwrite File", JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE);

            }
        } while (choice != JOptionPane.OK_OPTION);

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
        eg.export(gates);
    }

    @Override
    public void importGates() {
        ImportGates ig = new ImportGates();
        ArrayList<PolygonGate> al = ig.importGates();
        ListIterator itr = al.listIterator();

        while (itr.hasNext()) {
            PolygonGate pg = (PolygonGate) itr.next();
            if (pg.getGateAsPoints().size() < 3) {
            } else {
                gl.importGate(pg);
            }
        }
        gm.updateTable(gates, mapGates);
        gm.setVisible(true);
    }

    public void setMultipleGates(ArrayList<PolygonGate> al) {
        ListIterator itr = al.listIterator();
        //int c = 1;
        while (itr.hasNext()) {
            PolygonGate pg = (PolygonGate) itr.next();
            pg.createInChartSpace(chart);
            pg.setXAxis(hm.get(this.currentX));
            pg.setYAxis(hm.get(this.currentY));
            pg.setName("Untitled");
            gl.importGate(pg);
        }
        notifyResetSelectionListeners();
        gm.updateTable(gates, mapGates);
        gm.setVisible(true);
    }

    public void removeSinglePointGates() {

    }

    @Override
    public void updateFeatureSpace(HashMap<Integer, String> descriptions, ArrayList<ArrayList<Number>> measurements) {

        this.hm = descriptions;
        this.measurements = measurements;

        H2DatabaseEngine.dropTable(vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe);
        writeCSV(_vtea.DATABASE_DIRECTORY + System.getProperty("file.separator") + key);
        startH2Database(_vtea.DATABASE_DIRECTORY + System.getProperty("file.separator") + key + ".csv",
                vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe);

        AxesManager.updateFeatureSpace(hm);

    }

    @Override
    public void saveGated(Path2D path) {

        new Thread(() -> {
            try {

                NucleiExportation exportnuclei = new NucleiExportation(impoverlay, objects, measurements, key);
                try {

                    exportnuclei.saveImages(path, currentX, currentY);
                } catch (IOException ex) {

                    System.out.println("ERROR: " + ex.getLocalizedMessage());
                }

            } catch (Exception e) {
                System.out.println("ERROR: " + e.getLocalizedMessage());
            }
        }).start();

    }

    @Override
    public void subGate() {

        new Thread(() -> {
            try {

                ArrayList<ArrayList> al = cloneGatedObjectsMeasurements(true);
                if (al.size() > 0) {
                    ArrayList<MicroObject> objectsTemp = new ArrayList<MicroObject>();
                    ArrayList<ArrayList<Number>> measurementsFinal = new ArrayList<ArrayList<Number>>();

                    objectsTemp = al.get(0);
                    measurementsFinal = al.get(1);
                    System.out.println("Launching new explorer window... \n with "
                            + objectsTemp.size() + " objects and " + measurementsFinal.size()
                            + " measurements.");
                    notifySubgateListener(objectsTemp, measurementsFinal);
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getLocalizedMessage());
            }

        }).start();

    }

    private ArrayList<ArrayList> calculateGatingStrategy(ArrayList<String> gates, ArrayList<String> operators, boolean renumber) {

        //convert strings to gates
        //handoff gates as gat, objects, measurements and descriptions to GateMathProcessor
        PolygonGate gate;
        ListIterator<PolygonGate> gate_itr = this.gates.listIterator();

        int total = 0;
        int gated = 0;
        int selected = 0;
        int gatedSelected = 0;
        int gatecount = gates.size();

        ArrayList<ArrayList> result = new ArrayList<>();

        while (gate_itr.hasNext()) {
            gate = gate_itr.next();

            if (gate.getSelected()) {

                Path2D.Double path = gate.createPath2DInChartSpace();

                ArrayList<MicroObject> objectsTemp = new ArrayList<MicroObject>();
                ArrayList<MicroObject> objectsGated = new ArrayList<MicroObject>();
                ArrayList<MicroObject> objectsFinal = new ArrayList<MicroObject>();

                ArrayList<ArrayList<Number>> sortTemp
                        = new ArrayList<ArrayList<Number>>();

                ArrayList<ArrayList<Number>> measurementsTemp
                        = new ArrayList<ArrayList<Number>>();
                ArrayList<ArrayList<Number>> measurementsFinal
                        = new ArrayList<ArrayList<Number>>();
                ArrayList<ArrayList<Number>> measurementsGated
                        = new ArrayList<ArrayList<Number>>();

                //ArrayList<String> description = new ArrayList<String>();
                double xValue = 0;
                double yValue = 0;

                //description.add(this.descriptions.get(currentX));
                //description.add(this.descriptions.get(currentY));
                //description.add(this.descriptions.get(currentL));
                //System.out.println("PROFILING: Measurements length, " + measurements.size());
                //this is where we need to add logic for polygons...  this is tripping up things
                ArrayList<ArrayList> resultKey
                        = H2DatabaseEngine.getObjectsInRange2D(path,
                                vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe,
                                this.descriptions.get(currentX), path.getBounds2D().getX(),
                                path.getBounds2D().getX() + path.getBounds2D().getWidth(),
                                this.descriptions.get(currentY), path.getBounds2D().getY(),
                                path.getBounds2D().getY() + path.getBounds2D().getHeight(),
                                this.descriptions.get(currentL));

                ListIterator<ArrayList> itr = resultKey.listIterator();

                while (itr.hasNext()) {
                    ArrayList al = itr.next();
                    int object = ((Number) (al.get(0))).intValue();
                    objectsTemp.add(objects.get(object));
                    measurementsTemp.add(this.measurements.get(object));
                    sortTemp.add(al);
                }

                measurementsGated = measurementsTemp;
                objectsGated = objectsTemp;

                try {
                    int position = 0;
                    for (int i = 0; i < objectsGated.size(); i++) {

                        MicroObject object = ((MicroObject) objectsGated.get(i));

                        ArrayList<Number> sorted = (ArrayList<Number>) sortTemp.get(i);

                        xValue = sorted.get(1).doubleValue();
                        yValue = sorted.get(2).doubleValue();

                        if (path.contains(xValue, yValue)) {

                            if (this.imageGate) {

                                float PosX = object.getCentroidX();
                                float PosY = object.getCentroidY();

                                Roi r = impoverlay.getRoi();

                                if (r.containsPoint((double) PosX, (double) PosY)) {

                                    if (renumber) {
                                        object.setSerialID(objectsFinal.size());
                                    }

                                    objectsFinal.add(object);

                                    measurementsFinal.add(cloneMeasurements(measurementsGated.get(i)));
                                    position++;
                                }

                            } else {

                                if (renumber) {
                                    object.setSerialID(objectsFinal.size());
                                }

                                objectsFinal.add(object);

                                measurementsFinal.add(cloneMeasurements(measurementsGated.get(i)));
                                position++;
                            }
                        }

                    }
                } catch (NullPointerException e) {
                }
                result.add(objectsFinal);
                result.add(measurementsFinal);
            }

        }
        return result;
    }

    private ArrayList<ArrayList> cloneGatedObjectsMeasurements(boolean renumber) {

        PolygonGate gate;
        ListIterator<PolygonGate> gate_itr = gates.listIterator();

        int total = 0;
        int gated = 0;
        int selected = 0;
        int gatedSelected = 0;
        int gatecount = gates.size();

        ArrayList<ArrayList> result = new ArrayList<>();

        while (gate_itr.hasNext()) {
            gate = gate_itr.next();

            if (gate.getSelected()) {

                Path2D.Double path = gate.createPath2DInChartSpace();

                ArrayList<MicroObject> objectsTemp = new ArrayList<MicroObject>();
                ArrayList<MicroObject> objectsGated = new ArrayList<MicroObject>();
                ArrayList<MicroObject> objectsFinal = new ArrayList<MicroObject>();

                ArrayList<ArrayList<Number>> sortTemp
                        = new ArrayList<ArrayList<Number>>();

                ArrayList<ArrayList<Number>> measurementsTemp
                        = new ArrayList<ArrayList<Number>>();
                ArrayList<ArrayList<Number>> measurementsFinal
                        = new ArrayList<ArrayList<Number>>();
                ArrayList<ArrayList<Number>> measurementsGated
                        = new ArrayList<ArrayList<Number>>();

                ArrayList<String> description = new ArrayList<String>();

                double xValue = 0;
                double yValue = 0;

                description.add(this.descriptions.get(currentX));
                description.add(this.descriptions.get(currentY));
                description.add(this.descriptions.get(currentL));

                //System.out.println("PROFILING: Measurements length, " + measurements.size());
                //this is where we need to add logic for polygons...  this is tripping up things
                ArrayList<ArrayList> resultKey
                        = H2DatabaseEngine.getObjectsInRange2D(path,
                                vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe,
                                this.descriptions.get(currentX), path.getBounds2D().getX(),
                                path.getBounds2D().getX() + path.getBounds2D().getWidth(),
                                this.descriptions.get(currentY), path.getBounds2D().getY(),
                                path.getBounds2D().getY() + path.getBounds2D().getHeight(),
                                this.descriptions.get(currentL));

                ListIterator<ArrayList> itr = resultKey.listIterator();

                while (itr.hasNext()) {
                    ArrayList al = itr.next();
                    int object = ((Number) (al.get(0))).intValue();
                    objectsTemp.add(objects.get(object));
                    measurementsTemp.add(this.measurements.get(object));
                    sortTemp.add(al);
                }

                measurementsGated = measurementsTemp;
                objectsGated = objectsTemp;

                try {
                    int position = 0;
                    for (int i = 0; i < objectsGated.size(); i++) {

                        MicroObject object = ((MicroObject) objectsGated.get(i));

                        ArrayList<Number> sorted = (ArrayList<Number>) sortTemp.get(i);

                        xValue = sorted.get(1).doubleValue();
                        yValue = sorted.get(2).doubleValue();

                        if (path.contains(xValue, yValue)) {

                            if (this.imageGate) {

                                float PosX = object.getCentroidX();
                                float PosY = object.getCentroidY();

                                Roi r = impoverlay.getRoi();

                                if (r.containsPoint((double) PosX, (double) PosY)) {

//                                    if (renumber) {
//                                        object.setSerialID(objectsFinal.size());
//                                    }
                                    objectsFinal.add(object);

                                    measurementsFinal.add(cloneMeasurements(measurementsGated.get(i)));
                                    position++;
                                }

                            } else {

                                if (renumber) {
                                    object.setSerialID(objectsFinal.size());
                                }

                                objectsFinal.add(object);

                                measurementsFinal.add(cloneMeasurements(measurementsGated.get(i)));
                                position++;
                            }
                        }

                    }
                } catch (NullPointerException e) {
                }
                result.add(objectsFinal);
                result.add(measurementsFinal);

            }

        }
        return result;
    }

    private ArrayList<Number> cloneMeasurements(ArrayList<Number> al) {
        ArrayList<Number> result = new ArrayList<Number>();

        for (int i = 0; i < al.size(); i++) {
            float f = al.get(i).floatValue();
            result.add(Float.valueOf(f));
        }

        return result;
    }

    @Deprecated
    private ArrayList<ArrayList> getGatedObjectsMeasurements() {

        PolygonGate gate;
        ListIterator<PolygonGate> gate_itr = gates.listIterator();

        int total = 0;
        int gated = 0;
        int selected = 0;
        int gatedSelected = 0;
        int gatecount = gates.size();

        ArrayList<ArrayList> result = new ArrayList<ArrayList>();

        while (gate_itr.hasNext()) {
            gate = gate_itr.next();

            if (gate.getSelected()) {

                Path2D.Double path = gate.createPath2DInChartSpace();

                ArrayList<MicroObject> objectsTemp = new ArrayList<MicroObject>();

                ArrayList<MicroObject> objectsFinal = new ArrayList<MicroObject>();

                ArrayList<ArrayList<Number>> sortTemp
                        = new ArrayList<ArrayList<Number>>();

                ArrayList<ArrayList<Number>> measurementsTemp
                        = new ArrayList<ArrayList<Number>>();

                ArrayList<ArrayList<Number>> measurementsFinal
                        = new ArrayList<ArrayList<Number>>();

                ArrayList<String> description = new ArrayList<String>();

                double xValue = 0;
                double yValue = 0;

                description.add(this.descriptions.get(currentX));
                description.add(this.descriptions.get(currentY));
                description.add(this.descriptions.get(currentL));

                //this is where we need to add logic for polygons...  this is tripping up things
                //System.out.println("PROFILING: Menu selections, " + currentX + ", " + currentY + ", " + currentL);
                ArrayList<ArrayList> resultKey
                        = H2DatabaseEngine.getObjectsInRange2D(path,
                                vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe,
                                this.descriptions.get(currentX), path.getBounds2D().getX(),
                                path.getBounds2D().getX() + path.getBounds2D().getWidth(),
                                this.descriptions.get(currentY), path.getBounds2D().getY(),
                                path.getBounds2D().getY() + path.getBounds2D().getHeight(),
                                this.descriptions.get(currentL));

                ListIterator<ArrayList> itr = resultKey.listIterator();

                while (itr.hasNext()) {
                    ArrayList al = itr.next();
                    int object = ((Number) (al.get(0))).intValue();
                    objectsTemp.add(objects.get(object));
                    measurementsTemp.add(this.measurements.get(object));
                    sortTemp.add(al);
                }

                try {
                    int position = 0;
                    for (int i = 0; i < objectsTemp.size(); i++) {

                        MicroObject object = ((MicroObject) objectsTemp.get(i));

                        ArrayList<Number> sorted = (ArrayList<Number>) sortTemp.get(i);

                        xValue = sorted.get(1).doubleValue();
                        yValue = sorted.get(2).doubleValue();
                        if (path.contains(xValue, yValue)) {

                            objectsFinal.add(object);

                            measurementsFinal.add(measurementsTemp.get(i));

                            System.out.println("Added...");

                            position++;
                        }

                    }
                } catch (NullPointerException e) {
                }
                result.add(objectsFinal);
                result.add(measurementsFinal);

            }

        }
        return result;
    }

    @Override
    public void addFromCSV(String s) {

        int dataColumns = 0;

        JFileChooser jf = new JFileChooser(_vtea.LASTDIRECTORY);
        JCheckBox zero = new JCheckBox("Zero Order", true);

        ProgressTracker tracker = new ProgressTracker();

        double progress;

        JPanel panel1 = (JPanel) jf.getComponent(3);
        JPanel panel2 = (JPanel) panel1.getComponent(3);
        panel2.add(zero);

        int returnVal = jf.showOpenDialog(CenterPanel);
        File file = jf.getSelectedFile();

        boolean zeroOrder = zero.isSelected();

        ArrayList<ArrayList<Number>> csvData = new ArrayList();

        ArrayList<Number> blank = new ArrayList<Number>();

        String header = "";

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedReader csvReader = new BufferedReader(new FileReader(file));
                String row;
                boolean firstRow = true;

                tracker.createandshowGUI("Import CSV features...", 0, 0);
                addPropertyChangeListener(tracker);

                while ((row = csvReader.readLine()) != null) {

                    firePropertyChange("method", "", "Importing CSV");
                    firePropertyChange("indeterminant", "Parsing CSV", "");

                    if (firstRow) {
                        header = row;
                        firstRow = false;
                    } else {
                        String[] data = row.split(",");

                        dataColumns = data.length;

                        ArrayList<Number> dataList = new ArrayList<Number>();

                        for (int j = 0; j < data.length; j++) {
                            dataList.add(Float.parseFloat(data[j]));
                        }

                        csvData.add(dataList);

                    }
                }
                csvReader.close();

            } catch (IOException e) {
                System.out.println("ERROR: Could not open the file.");
            }

            //csvData is an ArrayList of ArrayList<Number> where each 
            //ArrayList<Number> is a segmented nucleus and columns are features
            //Features can be added easily into VTEA by column 
            //imported CSVs may not have values for all objects, enter a -1 for 
            //now
            //Old implementation parsed each feature of each object, On2 atleast
            //parse the csvData once and build each column at the same time.
            //generate empty ArrayList for objects without data
            ArrayList<ArrayList<Number>> FeatureColumns = new ArrayList<>();

            for (int k = 0; k < dataColumns; k++) {
                blank.add(-1);
                FeatureColumns.add(new ArrayList<Number>());
            }

            //build data by column
            for (int c = 0; c < this.objects.size(); c++) {
                ArrayList<Number> data = this.getObjectDataAll(zeroOrder, csvData, blank, c);
                for (int b = 0; b < dataColumns; b++) {
                    ArrayList<Number> al = FeatureColumns.get(b);
                    al.add(data.get(b));
                }
                progress = 100 * ((double) c / (double) objects.size());

                firePropertyChange("method", "", "Importing CSV");
                firePropertyChange("progress", "Building table", (int) progress);
            }

            String[] columnTitles = header.split(",");

            for (int m = 0; m < FeatureColumns.size(); m++) {

                ArrayList<ArrayList<Number>> result = new ArrayList<ArrayList<Number>>();

                progress = 100 * ((double) m / (double) FeatureColumns.size());

                firePropertyChange("method", "", "Importing CSV");
                firePropertyChange("progress", "Importing table...", (int) progress);

                result.add(FeatureColumns.get(m));
                this.notifyAddFeatureListener((columnTitles[m].replace(".", "_")).replace("/", "_"), result);

            }
        }
        tracker.setVisible(false);
    }

    @Deprecated
    public void addFromCSV_v1(String s) {

        int countObjects = this.objects.size();
        int dataColumns = 0;

        JFileChooser jf = new JFileChooser(_vtea.LASTDIRECTORY);
        JCheckBox zero = new JCheckBox("Zero Order", true);

        ProgressTracker tracker = new ProgressTracker();

        double progress;

        JPanel panel1 = (JPanel) jf.getComponent(3);
        JPanel panel2 = (JPanel) panel1.getComponent(3);
        panel2.add(zero);

        int returnVal = jf.showOpenDialog(CenterPanel);
        File file = jf.getSelectedFile();

        boolean zeroOrder = zero.isSelected();

        ArrayList<ArrayList<Number>> csvData = new ArrayList();
        ArrayList<ArrayList<Number>> paddedTable = new ArrayList();

        ArrayList<Number> blank = new ArrayList<Number>();

        String header = "";

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedReader csvReader = new BufferedReader(new FileReader(file));
                String row;
                boolean firstRow = true;

                tracker.createandshowGUI("Import CSV features...", 0, 0);
                addPropertyChangeListener(tracker);

                while ((row = csvReader.readLine()) != null) {

                    firePropertyChange("method", "", "Importing CSV");
                    firePropertyChange("indeterminant", "Parsing CSV...", 0);

                    if (firstRow) {
                        header = row;
                        firstRow = false;
                    } else {
                        String[] data = row.split(",");

                        dataColumns = data.length;

                        ArrayList<Number> dataList = new ArrayList<Number>();

                        for (int j = 0; j < data.length; j++) {
                            dataList.add(Float.parseFloat(data[j]));
                        }

                        csvData.add(dataList);

                    }
                }
                csvReader.close();

            } catch (IOException e) {
                System.out.println("ERROR: Could not open the file.");
            }

            //generate empty ArrayList for objects without data
            for (int k = 0; k < dataColumns; k++) {
                blank.add(-1);
            }

            //grab data by column
            for (int i = 1; i < dataColumns; i++) {

                ArrayList<Number> data = getData(zeroOrder, i, csvData);

                progress = 100 * ((double) i / (double) dataColumns);

                firePropertyChange("method", "", "Importing CSV");
                firePropertyChange("progress", "Processing CSV...", (int) progress);

                if (data.size() > 0) {
                    paddedTable.add(data);
                } else {
                    paddedTable.add(blank);
                }

            }

            ArrayList<Number> paddedTableColumn = paddedTable.get(0);
            dataColumns = paddedTableColumn.size();
            String[] columnTitles = header.split(",");

            for (int m = 0; m < paddedTable.size(); m++) {

                ArrayList<ArrayList<Number>> result = new ArrayList<ArrayList<Number>>();

                progress = 100 * ((double) m / (double) paddedTable.size());

                firePropertyChange("method", "", "Importing CSV");
                firePropertyChange("progress", "Importing table...", (int) progress);

                result.add(paddedTable.get(m));
                this.notifyAddFeatureListener((columnTitles[m + 1].replace(".", "_")).replace("/", "_"), result);

            }
        }
        tracker.setVisible(false);
    }

    //*Original addFromCSV used in NephNet work DO NOT delete//
    @Deprecated
    public void addFromCSV_old(String s) {
//        //this method does not assume that all objects get a value
        int countObjects = this.objects.size();
        int dataColumns = 0;

        JFileChooser jf = new JFileChooser(_vtea.LASTDIRECTORY);
        int returnVal = jf.showOpenDialog(CenterPanel);
        File file = jf.getSelectedFile();

        ArrayList<ArrayList<Number>> csvData = new ArrayList();
        ArrayList<ArrayList<Number>> paddedTable = new ArrayList();

        ArrayList<Number> blank = new ArrayList<Number>();

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedReader csvReader = new BufferedReader(new FileReader(file));
                String row;
                int objectID = 0;
                while ((row = csvReader.readLine()) != null) {
                    String[] data = row.split(",");

                    dataColumns = data.length;

                    ArrayList<Number> dataList = new ArrayList<Number>();

                    for (int j = 0; j < data.length; j++) {
                        dataList.add(Float.parseFloat(data[j]));
                    }

                    csvData.add(dataList);

                }
                csvReader.close();

            } catch (IOException e) {
                System.out.println("ERROR: Could not open the file.");
            }

            for (int k = 0; k < dataColumns; k++) {
                blank.add(-1);
            }

//            for (int i = 1; i < dataColumns; i++) {
//                //ArrayList<Number> data = getData(i, csvData);
//
////                if (data.size() > 0) {
////                    paddedTable.add(data);
////                } else {
////                    paddedTable.add(blank);
////                }
//            }
            String name = file.getName();
            name = name.replace(".", "_");
            this.notifyAddFeatureListener(name, paddedTable);
        }
    }

    private ArrayList<Number> getData(boolean zeroOrder, int columnIndex,
            ArrayList<ArrayList<Number>> data) {
        ArrayList<Number> result = new ArrayList<Number>();

        for (int objectID = 0; objectID < objects.size(); objectID++) {
            ListIterator<ArrayList<Number>> itr = data.listIterator();
            boolean elementFound = false;
            while (itr.hasNext()) {
                ArrayList<Number> test = itr.next();
                if (zeroOrder) {
                    if ((Float) (test.get(0)) == (float) objectID) {
                        result.add(test.get(columnIndex));
                        elementFound = true;
                        break;
                    }
                } else {
                    if ((Float) (test.get(0)) - 1 == (float) objectID) {
                        result.add(test.get(columnIndex));
                        elementFound = true;
                        break;
                    }
                }
            }
            if (elementFound == false) {
                result.add(-1);
            }
        }
        return result;
    }

    private ArrayList<Number> getObjectDataAll(boolean zeroOrder,
            ArrayList<ArrayList<Number>> data, ArrayList<Number> blank, float id) {
        ArrayList<Number> result = new ArrayList<Number>();
        boolean elementFound = false;
        //for (int objectID = 0; objectID < objects.size(); objectID++) {
        for (int x = 0; x < data.size(); x++) {

            ArrayList<Number> test = data.get(x);
            if (zeroOrder) {
                if ((Float) (test.get(0)) == (float) id) {
                    result.addAll(test);
                    elementFound = true;
                    data.remove(x);
                    return result;
                }
            } else {
                if ((Float) (test.get(0)) - 1 == (float) id) {
                    result.addAll(test);
                    elementFound = true;
                    data.remove(x);
                    return result;
                }
            }

        }

        result.addAll(blank);
        return result;
    }

    @Override
    public void addDensityMapFromGate(String s) {

        ArrayList<ArrayList> al = cloneGatedObjectsMeasurements(true);
        ArrayList<MicroObject> objectsTemp = new ArrayList<MicroObject>();
        ArrayList<ArrayList<Number>> measurementsFinal = new ArrayList<ArrayList<Number>>();

        objectsTemp = al.get(0);

        ImagePlus map = densityMaps3D.makeMap(impoverlay, objectsTemp);
        ImagePlus mapRandom = densityMaps3D.makeRandomMap(impoverlay, objectsTemp);

        densityMaps3D.addMap(map, s);

        measurementsFinal = densityMaps3D.getDistance(objects, map);

        this.notifyAddFeatureListener(s, measurementsFinal);

        densityMaps3D.addMap(mapRandom, s + "_Random");

        measurementsFinal = densityMaps3D.getDistance(objects, mapRandom);

        this.notifyAddFeatureListener(s + "_Random", measurementsFinal);
    }

    @Override
    public void addNeighborhoodFromGate(String name) {

        NeighborhoodSettingsDialog settingsDialog = new NeighborhoodSettingsDialog();
        settingsDialog.setFocusable(false);

        if (settingsDialog.showDialog()) {

            ArrayList<String> settings = settingsDialog.getSettings();

            Integer feature = Integer.parseInt(settings.get(0));
            String method = settings.get(1);
            String radius = settings.get(2);
            String kNeighbors = settings.get(3);
            String interval = settings.get(4);

            ProgressTracker pt = new ProgressTracker();
            pt.createandshowGUI("Neighborhood Analysis", explorerXposition, explorerYposition);
            addPropertyChangeListener(pt);
            firePropertyChange("method", "", method);

            firePropertyChange("indeterminant", " setup", "");

            //determine classes
            ArrayList<Integer> classes = new ArrayList<>();

            ArrayList<ArrayList<Number>> features
                    = H2DatabaseEngine.getColumn(vtea._vtea.H2_MEASUREMENTS_TABLE + "_"
                            + keySQLSafe, this.descriptions.get(feature));

            double maxClass = this.getMaximumOfData(features, 0);
            double minClass = this.getMinimumOfData(features, 0);

            for (int i = (int) minClass; i <= (int) maxClass; i++) {
                classes.add(i);
                //System.out.println("PROFILING: Adding class: " + i);
            }

            HashMap<String, String> objFeature = new HashMap<>();

            for (int c = 0; c < this.objects.size(); c++) {
                //System.out.println("PROFILING: Adding hash: " + (objects.get(c)).getSerialID() +
                //        ", " + features.get(c).get(0));
                objFeature.put(String.valueOf((objects.get(c)).getSerialID()), String.valueOf(features.get(c).get(0)));

            }

            ArrayList<ArrayList> al = cloneGatedObjectsMeasurements(false);

            //start factory
            ArrayList<MicroObject> objectsTemp = new ArrayList<MicroObject>();
            ArrayList<ArrayList<Number>> measurementsFinal = new ArrayList<ArrayList<Number>>();
            objectsTemp = al.get(0);
            measurementsFinal = al.get(1);

            ListIterator itrName = objectsTemp.listIterator();

            double[][] key = new double[objectsTemp.size()][1];

            double[][] data = new double[objectsTemp.size()][3];

            int i = 0;

            while (itrName.hasNext()) {
                MicroObject obj = (MicroObject) itrName.next();
                double[] k = new double[1];
                k[0] = obj.getSerialID();
                //System.out.println("PROFILING: Adding object(node): " + k[0]);
                key[i] = k;

                double[] d = new double[3];
                d[0] = obj.getCentroidX();
                d[1] = obj.getCentroidY();
                d[2] = obj.getCentroidZ();

                data[i] = d;
                i++;
            }

            //String str = "";
            //int randomKey = Math.abs(Random.nextInt(objectsTemp.size()-1));
            ArrayList<ArrayList<Neighbor>> neighborhoods = new ArrayList<ArrayList<Neighbor>>();

            //final result
            ArrayList<MicroNeighborhoodObject> n_objs = new ArrayList<>();

            if (method.equals("Nearest-k")) {

                firePropertyChange("method", "Nearest-k.", "Nearest-k.");
                firePropertyChange("indeterminant", "Making kD tree...", "");

                KDTree tree = new KDTree(data, key);

                for (int objectKey = 0; objectKey < objectsTemp.size(); objectKey++) {

                    Neighbor n = tree.nearest(data[objectKey]);

                    double[] d = data[objectKey];
                    double[] k = (double[]) n.value;

                    Neighbor[] neighborsArray = tree.knn(data[objectKey], Integer.parseInt(kNeighbors));

                    ArrayList<Neighbor> neighbors = new ArrayList<Neighbor>();
                    for (int j = 0; j < neighborsArray.length; j++) {
                        neighbors.add(neighborsArray[j]);
                    }
                    neighborhoods.add(neighbors);

                    //System.out.println("Query object : " + key[objectKey][0] + ", " + neighbors.size() + " objects.");
                    //str = "   Objects: ";
                    ArrayList<Double> neighborhoodObjects = new ArrayList<>();

                    neighborhoodObjects.add(key[objectKey][0]);

                    ArrayList<MicroObject> localObjects = new ArrayList<>();

                    localObjects.add(objects.get(((int) key[objectKey][0])));
                    //str = str + (int) key[objectKey][0];
                    //str = str + "(" + objects.get(((int) key[objectKey][0])).getPixelsX().length + ")";

                    for (int l = 0; l < neighbors.size(); l++) {
                        double[] id = (double[]) ((Neighbor) neighbors.get(l)).value;
                        // str = str + "," + id[0];
                        neighborhoodObjects.add(id[0]);
                        MicroObject objectAdd = (objects.get((int) id[0]));
                        localObjects.add(objectAdd);
                        // str = str + "(" + ((objectAdd.getPixelsX()).length) + ")";
                    }
                    // System.out.println(str);
                    MicroNeighborhoodObject mno = new MicroNeighborhoodObject(localObjects, this.key);
                    mno.setCentroid();
                    mno.setSerialID(objectKey);
                    n_objs.add(mno);

                    double progress = 100 * ((double) objectKey / (double) objectsTemp.size());

                    firePropertyChange("comment", "", "Building neighborhood:");
                    firePropertyChange("progress", "Building neighborhood:", (int) progress);

                }

                //convert neighborhood into list of microobjects
                //add center object
            } else if (method.equals("Spatial by cell")) {

                firePropertyChange("method", "Spatial by cell.", "Spatial by cell.");
                //firePropertyChange("progress", "Making kD tree...", 5);
                firePropertyChange("indeterminant", " setup", "");

                KDTree tree = new KDTree(data, key);

                for (int objectKey = 0; objectKey < objectsTemp.size(); objectKey++) {

                    Neighbor n = tree.nearest(data[objectKey]);

                    double[] d = data[objectKey];
                    double[] k = (double[]) n.value;

                    ArrayList<Neighbor> neighbors = new ArrayList<Neighbor>();
                    tree.range(data[objectKey], Double.parseDouble(radius), neighbors);
                    neighborhoods.add(neighbors);

                    ArrayList<Double> neighborhoodObjects = new ArrayList<>();

                    neighborhoodObjects.add(key[objectKey][0]);

                    ArrayList<MicroObject> localObjects = new ArrayList<>();

                    localObjects.add(objects.get(((int) key[objectKey][0])));

                    for (int l = 0; l < neighbors.size(); l++) {
                        double[] id = (double[]) ((Neighbor) neighbors.get(l)).value;
                        neighborhoodObjects.add(id[0]);
                        MicroObject objectAdd = (objects.get((int) id[0]));
                        localObjects.add(objectAdd);
                    }
                    //System.out.println(str);
                    MicroNeighborhoodObject mno = new MicroNeighborhoodObject(localObjects, this.key);
                    mno.setCentroid();
                    mno.setSerialID(objectKey);
                    n_objs.add(mno);

                    double progress = 100 * ((double) objectKey / (double) objectsTemp.size());

                    firePropertyChange("comment", "", "Building neighborhood:");
                    firePropertyChange("progress", "Building neighborhood:", (int) progress);

                }

                //convert neighborhood into list of microobjects
                //add center object
            } else { //spatial by point

                firePropertyChange("method", "Spatial by point.", "Spatial by point.");
                firePropertyChange("indeterminant", " setup", "");

                HashMap<Double, Integer> objectPosition = this.getSerialIDHashMap(objects);
                double maxSerialID = this.getSerialIDMax(objects);

                int width = impoverlay.getWidth();
                int height = impoverlay.getHeight();
                int depth = impoverlay.getNSlices();

                int intervalInt = Integer.parseInt(interval);
                //add pseudo points based on spacing.
                int x;
                int y;
                int z;

                int[] xPos = new int[1];
                int[] yPos = new int[1];
                int[] zPos = new int[1];

                //firePropertyChange("comment", "", "Making kD tree...");
                //firePropertyChange("progress", "Making kD tree...", 5);
                if (width > 2 * intervalInt) {
                    x = Math.round(width / intervalInt);
                    xPos = new int[x - 1];
                    xPos[0] = intervalInt;
                    for (int k = 1; k < xPos.length; k++) {
                        xPos[k] = xPos[k - 1] + intervalInt;
                    }
                } else {
                    xPos = new int[1];
                    xPos[0] = Math.round(width / 2);
                }
                if (height > 2 * intervalInt) {
                    y = Math.round(height / intervalInt);
                    yPos = new int[y - 1];
                    yPos[0] = intervalInt;
                    for (int k = 1; k < yPos.length; k++) {
                        yPos[k] = yPos[k - 1] + intervalInt;
                    }
                } else {
                    yPos = new int[1];
                    yPos[0] = Math.round(height / 2);
                }
                if (depth > 2 * intervalInt) {
                    z = Math.round(depth / intervalInt);
                    zPos = new int[z - 1];
                    zPos[0] = intervalInt;
                    for (int k = 1; k < zPos.length; k++) {
                        zPos[k] = zPos[k - 1] + intervalInt;
                    }
                } else {
                    zPos = new int[1];
                    zPos[0] = Math.round(depth / 2);
                }

                double[][] dataReference = new double[xPos.length * yPos.length * zPos.length + data.length][3];
                key = new double[xPos.length * yPos.length * zPos.length + data.length][1];

                int c = 0;
                int referenceKey = (int) this.getSerialIDMax(objects) + 1;

                ImagePlus resultImage = IJ.createImage("Sampling Volumes", "8-bit black", impoverlay.getWidth(), impoverlay.getHeight(), impoverlay.getNSlices());
                ImageStack resultStack = resultImage.getStack();

                //System.out.println("PROFILING: Neighborhood Analysis Adding : " + xPos.length * yPos.length * zPos.length + " reference points.");
                boolean makeImage = true;
                if (makeImage) {
                    //firePropertyChange("comment", "", "Making image...");
                    firePropertyChange("progress", "Making image...", 30);
                    for (int l = 0; l < xPos.length; l++) {
                        for (int m = 0; m < yPos.length; m++) {
                            for (int n = 0; n < zPos.length; n++) {
                                double[] d = new double[3];
                                d[0] = xPos[l];
                                d[1] = yPos[m];
                                d[2] = zPos[n];

                                resultStack.drawSphere(5, (int) d[0], (int) d[1], (int) d[2]);

                                dataReference[c] = d;
                                double[] k = new double[1];
                                k[0] = referenceKey;
                                key[c] = k;
                                //System.out.println("PROFILING: Adding reference of: " + k[0] + " at position: " + d[0] +","+ d[1] +"," + d[2]);

                                referenceKey++;
                                c++;

                                double length = xPos.length * yPos.length * zPos.length;
                                double position = l + m + n;
                                //firePropertyChange("comment", "", "Making image...");
                                firePropertyChange("progress", "Making image...", (int) (100 * position / length));

                            }
                        }
                    }

                    resultImage.show();
                }
                //add segmented objects to end of references
                ListIterator itrObject = objectsTemp.listIterator();

                while (itrObject.hasNext()) {
                    MicroObject obj = (MicroObject) itrObject.next();
                    double[] k = new double[1];
                    //System.out.println("PROFILING: Adding object for tree building: " + obj.getSerialID() + " at position: " + key[c]);
                    k[0] = obj.getSerialID();
                    key[c] = k;

                    double[] d = new double[3];
                    d[0] = obj.getCentroidX();
                    d[1] = obj.getCentroidY();
                    d[2] = obj.getCentroidZ();

                    dataReference[c] = d;

                    //System.out.println("PROFILING: Adding object: " + k[0] + " at position: " + d[0] +","+ d[1] +"," + d[2]);
                    //
                    c++;
                }

                //key needs to be same length,  set a non
                firePropertyChange("method", "", "Spatial by point..");
                //firePropertyChange("comment", "", "Making kD tree...");
                firePropertyChange("indeterminant", " setup", "");

                KDTree tree = new KDTree(dataReference, key);

                //firePropertyChange("comment", "", "Generating neighborhoods...");
                int refLength = xPos.length * yPos.length * zPos.length;

                for (int objectKey = 0; objectKey < refLength; objectKey++) {

                    double[] d = dataReference[objectKey];

                    ArrayList<Neighbor> neighbors = new ArrayList<Neighbor>();
                    tree.range(dataReference[objectKey], Double.parseDouble(radius), neighbors);
                    neighborhoods.add(neighbors);
//                    System.out.println("PROFILING: Neighborhood: " + objectKey);
//                    System.out.println("Query position : " + dataReference[objectKey][0] + ", " + neighbors.size() + " objects within " + radius + " px");
//                    String  str = "   Objects: ";

                    ArrayList<MicroObject> localObjects = new ArrayList<>();

                    for (int l = 0; l < neighbors.size(); l++) {
                        double[] id = (double[]) ((Neighbor) neighbors.get(l)).value;
                        // System.out.println("PROFILING: Is testing object: " + (int)id[0] + " at postion: " + objectPosition.get(id[0]));

                        if ((int) id[0] <= maxSerialID) {

                            MicroObject objectAdd = objects.get((int) id[0]);

//System.out.println("PROFILING: Adding object: " + objectAdd.getSerialID());
                            //str = str +(id[0] - refLength - 1) + ",";
                            // System.out.println( + objectAdd.getPixelsX().length);
                            localObjects.add(objectAdd);

                        }
                    }
                    //System.out.println(str);
                    if (localObjects.size() > 0) {
                        MicroNeighborhoodObject mno = new MicroNeighborhoodObject(localObjects, this.key);
                        mno.setCentroid();
                        mno.setSerialID(n_objs.size());

                        // System.out.println("PROFILING: Adding neighborhood of size: " + mno.getPixelsX().length);
                        n_objs.add(mno);
                    }

                    double progress = 100 * ((double) objectKey / (double) refLength);

                    firePropertyChange("method", "", "Spatial by position");
                    firePropertyChange("progress", "Generating neighborhoods...", (int) progress);

                }
            }
            if (n_objs.size() > 0) {
                firePropertyChange("comment", "", "Calculating...");
                String newKey = this.key + "_" + System.currentTimeMillis();
                NeighborhoodFactory nf = new NeighborhoodFactory();
                nf.makeNeighborhoodAnalysis(impoverlay, newKey, this.key, n_objs, classes, objFeature, pt);
            }
            pt.setVisible(false);

        }

    }

    private HashMap<Double, Integer> getSerialIDHashMap(ArrayList<MicroObject> objs) {

        HashMap<Double, Integer> lookup = new HashMap();

        int position = 0;

        ListIterator<MicroObject> itr = objs.listIterator();

        while (itr.hasNext()) {
            MicroObject obj = itr.next();
            lookup.put(obj.getSerialID(), position);
            position++;
        }

        return lookup;

    }

    private double getSerialIDMax(ArrayList<MicroObject> objs) {

        double max = 0;

        ListIterator<MicroObject> itr = objs.listIterator();

        while (itr.hasNext()) {
            MicroObject obj = itr.next();
            if (obj.getSerialID() > max) {
                max = obj.getSerialID();
            }

        }

        return max;

    }

    private MicroObject getObjectFromList(double ID, ArrayList<MicroObject> objects) {
        MicroObject obj = new MicroObject();
        ListIterator itr = objects.listIterator();
        while (itr.hasNext()) {
            obj = (MicroObject) itr.next();
            if (obj.getSerialID() == ID) {
                return obj;
            }
        }
        return obj;
    }

    @Override
    public void addDistanceMapFromGate(String s) {

        ArrayList<ArrayList> al = cloneGatedObjectsMeasurements(true);
        ArrayList<MicroObject> objectsTemp = new ArrayList<MicroObject>();
        ArrayList<ArrayList<Number>> measurementsFinal
                = new ArrayList<ArrayList<Number>>();

        objectsTemp = al.get(0);

        ImagePlus map = distanceMaps2D.makeMap(impoverlay, objectsTemp);
        ImagePlus mapRandom = distanceMaps2D.makeRandomMap(impoverlay, objectsTemp);

        distanceMaps2D.addMap(map, s);
        distanceMaps2D.addMap(mapRandom, s + "_Random");

        measurementsFinal = distanceMaps2D.getDistance(objects, map);

        this.notifyAddFeatureListener(s, measurementsFinal);

        measurementsFinal
                = new ArrayList<ArrayList<Number>>();

        measurementsFinal = distanceMaps2D.getDistance(objects, mapRandom);

        this.notifyAddFeatureListener(s + "_Random", measurementsFinal);
    }

    @Override
    public void addFeatureListener(AddFeaturesListener listener) {
        addfeaturelisteners.add(listener);
    }

    @Override
    public void notifyAddFeatureListener(String name,
            ArrayList<ArrayList<Number>> feature) {
        for (AddFeaturesListener listener : addfeaturelisteners) {
            listener.addFeatures(name, feature);
        }
    }

    @Override
    public void addupdateExplorerGUIListener(UpdateExplorerGuiListener listener) {
        updateexlporerguilisteners.add(listener);
    }

    @Override
    public void notifyUpdateExplorerGUIListener() {
        for (UpdateExplorerGuiListener listener : updateexlporerguilisteners) {
            listener.rebuildExplorerGUI();
        }
    }

    @Override
    public void addAxesSetpExplorerPlotUpdateListener(AxesSetupExplorerPlotUpdateListener listener) {
        axesSetupExplorerUpdateListeners.clear();
        axesSetupExplorerUpdateListeners.add(listener);
    }

    @Override
    public void notifyAxesSetupExplorerPlotUpdateListener(int x, int y, int l, int pointsize) {
        for (AxesSetupExplorerPlotUpdateListener listener : axesSetupExplorerUpdateListeners) {
            listener.axesSetupExplorerPlotUpdate(x, y, l, pointsize);
            //System.out.println("PROFILING: notifyPlotAxesPreviewBtnListeners");
        }
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void invokeAxesSettingsDialog(int xPos, int yPos) {
        AxesManager.createAxesSettingsDialog(this.getSettingsContent(), xPos, yPos);
        AxesManager.addPlotAxesPreviewButtonListener(this);
    }

    @Deprecated
    private HashMap<String, Integer> getLimits(int selectedIndex) {

        HashMap<String, Integer> minAndMax = new HashMap<>();

        String selectedIndexText;
        if (MicroExplorer.LUTSTART < 0) {
            selectedIndexText = "";
        } else {
            selectedIndexText = hm.get(selectedIndex);
        }

        double max = 0;
        double min = 0;
        if (MicroExplorer.LUTSTART >= 0) {
            max = Math.round(getMaximumOfData(H2DatabaseEngine.getColumn(vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe, selectedIndexText), 0));
            min = Math.round(getMinimumOfData(H2DatabaseEngine.getColumn(vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe, selectedIndexText), 0));

            if (max == 0) {
                max = 1;
            }
        }

        int padding = (int) (0.05 * (max - min));

        minAndMax.put("min", (int) Math.round(min) - padding);
        minAndMax.put("max", (int) Math.round(max) + padding);

        return minAndMax;
    }

    @Override
    public void imageOpened(ImagePlus ip) {
        if (ip.getID() == impoverlay.getID() && updateimage) {
            impZ = impoverlay.getCurrentSlice();
        }
    }

    @Override
    public void imageClosed(ImagePlus ip) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void imageUpdated(ImagePlus ip) {

        if (ip.getID() == impoverlay.getID() && updateimage && impZ != impoverlay.getCurrentSlice()) {
            impZ = impoverlay.getCurrentSlice();
            makeOverlayImageAndCalculate(gates, 0, 0, currentX, currentY);
        }
    }

    @Override
    public void onUpdateName(String st, int row) {
        gl.updateGateName(st, row);
    }

    @Override
    public void onColorUpdate(Color color, int row) {

    }

    @Override
    public void onRemapOverlay(Boolean b, int row) {
        gl.setGateOverlay(b, row);
        makeOverlayImageAndCalculate(gates, 0, 0, currentX, currentY);
    }

    @Override
    public void startManualClassListener() {
        ManualClassification mc
                = new ManualClassification(impoverlay, objects, measurements, key);
        mc.addFeatureListener(this);
        mc.process();
    }

    @Override
    public void addFeatures(String name, ArrayList<ArrayList<Number>> al) {
        this.notifyAddFeatureListener(name, al);
    }

    @Override
    public void onGateColor(ArrayList<PolygonGate> gt) {
        gates = gt;
        gm.updateTable(gates, mapGates);
        gm.pack();
        gm.repaint();
    }

    @Override
    public void updateMenuPositions(int xPos, int yPos) {
        explorerXposition = xPos;
        explorerYposition = yPos;
        AxesManager.updateMenuPosition(xPos, yPos);
        gm.setLocation(xPos, yPos + 96);
        gm.toFront();
        gm.pack();
    }
    
    @Override
    public void updateMenuVisible(boolean visible){
        AxesManager.setVisible(visible);
        if(gm.getNumberOfGates() > 0 && !gm.isVisible()){
        gm.setVisible(visible);}
    }
    
    
    @Override
    public void closeMenu() {
        AxesManager.close();
        
        gm.setVisible(false);
    }

    @Override
    public void assignClassification(String cmd) {

        ArrayList<ArrayList> al = new ArrayList<>();

        if (cmd.equals("gate")) {
            al = cloneGatedObjectsMeasurements(false);
            AssignClassification ac
                    = new AssignClassification(al.get(0), objects, keySQLSafe, descriptions);
            ac.addFeatureListener(this);
            ac.process();
        } else if (cmd.equals("gatemath")) {

            GateMathWindow gatemath = new GateMathWindow(gates);
            gatemath.addMathListener(this);
            gatemath.setVisible(true);

        }

    }

    @Override
    public void updateFeatures(String name, ArrayList<ArrayList<Number>> al) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void doGates(String st) {
        if (st.equals("import")) {
            this.importGates();
        }
        if (st.equals("export")) {
            this.exportGates();
        }
    }

    @Override
    public void addClassByMath(ArrayList<String> gatesString,
            ArrayList<String> operatorsString, int classAssigned) {

        new Thread(() -> {
            try {
                gmp = new GateMathProcessor(
                        gatesString, operatorsString, gates,
                        objects, measurements, descriptions, 
                        keySQLSafe, classAssigned);
                gmp.addFeatureListener(this);
                gmp.execute();
 
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getLocalizedMessage());
            }
        }).start();
    }



    @Override
    public void addGateMathObjects(ArrayList<MicroObject> gatedObjects, int classAssigned) {
       
    }

    class ExportGates {

        public ExportGates() {
        }

        public void export(ArrayList<PolygonGate> al) {
            File file;
            int returnVal = JFileChooser.CANCEL_OPTION;
            int choice = JOptionPane.OK_OPTION;
            do {
                JFileChooser jf = new JFileChooser(_vtea.LASTDIRECTORY);
                returnVal = jf.showSaveDialog(CenterPanel);
                file = jf.getSelectedFile();

                _vtea.LASTDIRECTORY = file.getPath();

                file = jf.getSelectedFile();

                if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("vtg")) {

                } else {
                    file = new File(file.toString() + ".vtg");
                }

                if (file.exists()) {
                    String message = String.format("%s already exists\nOverwrite it?",
                            file.getName());
                    choice = JOptionPane.showConfirmDialog(CenterPanel, message,
                            "Overwrite File", JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                }
            } while (choice != JOptionPane.OK_OPTION);

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

    class NeighborhoodSettingsDialog extends JOptionPane {

        ArrayList<String> settings = new ArrayList<String>();

        JComboBox classification = new JComboBox(descriptions.toArray());

        String[] methods = {"Spatial by cell", "Spatial by position", "Nearest-k"};

        String[] methodsDetail = {"Uses a spatial kernel with a fixed radius centered on a cell.", "Uses a spherical spatial kernel with a fixed radius.",
            "Selects the k-nearest neighbors of a cell."};

        JComboBox method = new JComboBox(methods);

        JLabel radiusLabel = new JLabel("Radius");
        JTextField radius = new JTextField("30", 5);
        JLabel kNeighborsLabel = new JLabel("Neighbors");
        JTextField kNeighbors = new JTextField("10", 5);
        JLabel intervalLabel = new JLabel("Interval");
        JTextField interval = new JTextField(radius.getText(), 5);
        JLabel warning = new JLabel("CAUTION: Use a discrete feature.");

        JPanel menu = new JPanel();
        JPanel submenu = new JPanel();

        boolean result = false;

        public NeighborhoodSettingsDialog() {

            super();

            classification.setSelectedIndex(descriptions.size() - 1);

            classification.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    CheckRangeOfValues(classification.getSelectedIndex());
                }
            });

            ArrayList<String> tips = new ArrayList<String>();

            tips.add(methodsDetail[0]);
            tips.add(methodsDetail[1]);
            tips.add(methodsDetail[2]);

            ComboboxToolTipRenderer renderer = new ComboboxToolTipRenderer();
            renderer.setTooltips(tips);

            method.setRenderer(renderer);

            method.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    ProcessSelectComboBoxActionPerformed(evt);
                }
            });

            menu.setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0.2, 1.0,
                    GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
            menu.add(new JLabel("Base classifcation on:"), gbc);
            gbc = new GridBagConstraints(1, 0, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
            menu.add(classification, gbc);

            gbc = new GridBagConstraints(0, 1, 1, 1, 0.2, 1.0,
                    GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
            menu.add(warning, gbc);
            gbc = new GridBagConstraints(1, 1, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
            menu.add(method, gbc);
            gbc = new GridBagConstraints(1, 2, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
            menu.add(submenu, gbc);

            method.setSelectedIndex(0);

        }

        private void CheckRangeOfValues(int selectedIndex) {

        }

        private void ProcessSelectComboBoxActionPerformed(java.awt.event.ActionEvent evt) {

            submenu.setVisible(false);
            submenu.removeAll();

            submenu.setLayout(new GridBagLayout());

            String methodString = (String) method.getSelectedItem();

            if (methodString.equals("Spatial by cell")) {

                GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0.2, 1.0,
                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);

                gbc = new GridBagConstraints(0, 0, 1, 1, 0.2, 1.0,
                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(radiusLabel);
                gbc = new GridBagConstraints(1, 0, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(radius, gbc);
                gbc = new GridBagConstraints(0, 1, 1, 1, 0.2, 1.0,
                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(new JLabel("     "), gbc);
                gbc = new GridBagConstraints(1, 1, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(new JLabel("     "), gbc);

            } else if (methodString.equals("Spatial by position")) {
                GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0.2, 1.0,
                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(radiusLabel);
                gbc = new GridBagConstraints(1, 0, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(radius, gbc);
                gbc = new GridBagConstraints(0, 1, 1, 1, 0.2, 1.0,
                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(intervalLabel, gbc);
                gbc = new GridBagConstraints(1, 1, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(interval, gbc);

            } else {
                GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0.2, 1.0,
                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(kNeighborsLabel, gbc);
                gbc = new GridBagConstraints(1, 0, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(kNeighbors, gbc);
                gbc = new GridBagConstraints(0, 1, 1, 1, 0.2, 1.0,
                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(new JLabel("     "), gbc);
                gbc = new GridBagConstraints(1, 1, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(new JLabel("     "), gbc);

            }

            submenu.revalidate();
            submenu.repaint();
            submenu.setVisible(true);

        }

        public boolean showDialog() {
            int x = showOptionDialog(null, menu, "Setup Neighborhoods",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                    null, null);

            if (x == JOptionPane.OK_OPTION) {
                //System.out.println("PROFILING: radius: " + size.getText() + " and weight:" + weight.getText());
                result = true;
                settings.clear();
                settings.add(Integer.toString(classification.getSelectedIndex()));
                settings.add(methods[method.getSelectedIndex()]);
                settings.add(radius.getText());
                settings.add(kNeighbors.getText());
                settings.add(interval.getText());
            } else {
                return false;
            }
            return true;
        }

        public ArrayList<String> getSettings() {

            return settings;
        }
    }

    class ImportGates {

        public ImportGates() {
        }

        protected ArrayList<PolygonGate> importGates() {

            JFileChooser jf = new JFileChooser(_vtea.LASTDIRECTORY);
            int returnVal = jf.showOpenDialog(CenterPanel);
            File file = jf.getSelectedFile();

            ArrayList<PolygonGate> result = new ArrayList();

            Object obj = new Object();

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        ObjectInputStream ois = new ObjectInputStream(fis);
                        result = (ArrayList) ois.readObject();
                        ois.close();
                        _vtea.LASTDIRECTORY = file.getAbsolutePath();
                    } catch (IOException e) {
                        System.out.println("ERROR: Could not open the file.");
                        System.out.println(e.getMessage());
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
