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
import java.awt.FlowLayout;
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
import java.util.Random;
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
import vtea.exploration.listeners.SpatialListener;
import vtea.exploration.listeners.SubGateExplorerListener;
import vtea.exploration.listeners.SubGateListener;
import vtea.exploration.listeners.UpdateExplorerGuiListener;
import vtea.exploration.listeners.UpdateFeaturesListener;
import vtea.exploration.listeners.UpdatePlotWindowListener;
import vtea.exploration.listeners.colorUpdateListener;
import vtea.exploration.listeners.remapOverlayListener;
import vtea.exploration.plotgatetools.gates.GateLayer;
import vtea.exploration.plotgatetools.gates.PolygonGate;
import vtea.exploration.gallery.GalleryViewDataProvider;
import vtea.exploration.gallery.GallerySelectionListener;
import vtea.exploration.gallery.GalleryViewWindow;
import vtea.exploration.plotgatetools.listeners.AddGateListener;
import vtea.exploration.plotgatetools.listeners.ChangePlotAxesListener;
import vtea.exploration.plotgatetools.listeners.DeleteGateListener;
import vtea.exploration.plotgatetools.listeners.GateColorListener;
import vtea.exploration.plotgatetools.listeners.ImageHighlightSelectionListener;
import vtea.exploration.plotgatetools.listeners.MakeImageOverlayListener;
import vtea.exploration.plotgatetools.listeners.PolygonSelectionListener;
import vtea.exploration.plotgatetools.listeners.QuadrantSelectionListener;
import vtea.exploration.plotgatetools.listeners.RandomizationListener;
import vtea.exploration.plotgatetools.listeners.ResetSelectionListener;
import vtea.exploration.util.ClassRandomization;
import vtea.exploration.util.PositionRandomization;
import vtea.gui.ComboboxToolTipRenderer;
import vtea.jdbc.H2DatabaseEngine;
import vtea.lut.Black;
import vtea.morphology.ImageFeatureAddFrame;
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
import vtea.exploration.listeners.GatePlotListener;
import vtea.exploration.plotgatetools.listeners.DatasetUtilitiesListener;
import vtea.exploration.util.DatasetUtilities;
import vtea.util.PerformanceProfiler;

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
        GateManagerActionListener, AddClassByMathListener, GateMathObjectListener,
        RandomizationListener, SpatialListener, GatePlotListener, DatasetUtilitiesListener,
        GalleryViewDataProvider{

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

    DatasetUtilities DataUtilities;

    // BufferedImage cache for overlay rendering to avoid recreating images on every call
    private BufferedImage cachedPlaceholder;
    private BufferedImage cachedSelections;
    private boolean overlayCacheDirty = true;
    private int cachedImageWidth = -1;
    private int cachedImageHeight = -1;

    // Cache for database query results to avoid blocking EDT
    private HashMap<String, ArrayList<ArrayList>> gateQueryCache = new HashMap<>();
    private boolean useQueryCache = true;

    // Gallery view fields
    private HashMap<String, GalleryViewWindow> galleryWindows = new HashMap<>();
    private ImageStack[] imageStacks;
    private GateLayer gateLayer;
    private MicroObject currentGallerySelection;

    public XYExplorationPanel(String key, Connection connection,
            ArrayList measurements, ArrayList<String> descriptions,
            HashMap<Integer, String> hm, ArrayList<MicroObject> objects,
            String title) {

        super();

        gm = new TableWindow(title);
        gm.addGatePlotListener(this);
        
        
        configureListeners();
        
        this.key = key;
        this.objects = objects;
        this.measurements = measurements;
        this.descriptions = descriptions;
        this.connection = connection;

        keySQLSafe = key.replace("-", "_");
        
        DataUtilities = new DatasetUtilities(vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe, this.objects);
        

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
        
        //DataUtilities.addResetIDListener(this);

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
        gl.addRandomizationListener(this);
        gl.addSpatialListener(this);
        gl.addDatasetUtilitiesListener(this);

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
            //Connection cn = _vtea.connection;
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

            // Profile this critical rendering path
            long startTime = PerformanceProfiler.startTiming("MakeGateOverlayImage");

            PolygonGate gate;
            ListIterator<PolygonGate> gate_itr = gates.listIterator();

            int total = 0;
            int gated = 0;
            int selected = 0;
            int gatedSelected = 0;
            int gatecount = gates.size();

            // Use cached BufferedImages if available and size hasn't changed
            int currentWidth = impoverlay.getWidth();
            int currentHeight = impoverlay.getHeight();

            if (cachedPlaceholder == null || overlayCacheDirty ||
                cachedImageWidth != currentWidth || cachedImageHeight != currentHeight) {
                // Create new images only when necessary
                cachedPlaceholder = new BufferedImage(currentWidth, currentHeight, BufferedImage.TYPE_INT_ARGB);
                cachedSelections = new BufferedImage(currentWidth, currentHeight, BufferedImage.TYPE_INT_ARGB);
                cachedImageWidth = currentWidth;
                cachedImageHeight = currentHeight;
                overlayCacheDirty = false;
            } else {
                // Clear existing image for reuse
                Graphics2D g2clear = cachedSelections.createGraphics();
                g2clear.setBackground(new Color(0, 0, 0, 0));
                g2clear.clearRect(0, 0, currentWidth, currentHeight);
                g2clear.dispose();
            }

            BufferedImage placeholder = cachedPlaceholder;
            BufferedImage selections = cachedSelections;

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

                    // Create cache key for this gate query
                    String cacheKey = gate.getXAxis() + "_" + gate.getYAxis() + "_" +
                                     path.getBounds2D().getX() + "_" + path.getBounds2D().getY() + "_" +
                                     path.getBounds2D().getWidth() + "_" + path.getBounds2D().getHeight();

                    ArrayList<ArrayList> resultKey;

                    // Check cache first to avoid database query on EDT
                    if (useQueryCache && gateQueryCache.containsKey(cacheKey)) {
                        resultKey = gateQueryCache.get(cacheKey);
                    } else {
                        // Query database - this should ideally be done in background
                        // For now, we cache the result to avoid repeated queries
                        resultKey = H2DatabaseEngine.getObjectsInRange2D(path,
                                        vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe,
                                        gate.getXAxis(), path.getBounds2D().getX(),
                                        path.getBounds2D().getX() + path.getBounds2D().getWidth(),
                                        gate.getYAxis(), path.getBounds2D().getY(),
                                        path.getBounds2D().getY() + path.getBounds2D().getHeight());

                        // Cache the result
                        if (useQueryCache) {
                            gateQueryCache.put(cacheKey, resultKey);
                        }
                    }

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
                //System.gc();

                // End profiling for this rendering path
                PerformanceProfiler.endTiming("MakeGateOverlayImage", startTime);
            }
        } else {
            impoverlay.getOverlay().clear();
            //System.gc();
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
                    
                    //make a Hashmap 
                    
//                    HashMap<Integer, Integer> lookupPosition = new HashMap();
//                    
//                    
//                    
//                    for(int k = 0; k < resultKey.size(); k++) {
//                        ArrayList al = resultKey.get(k);
//                        int object = ((Double)al.get(0)).intValue();
//                        lookupPosition.put(object, k);
//                    }

                    while (itr.hasNext()) {
                        ArrayList al = itr.next();
                        int object = ((Double)(al.get(3))).intValue();
                        result.add(volumes.get(object));
                        //result.add(volumes.get((lookupPosition.get(object))));
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
                            //add morphology, need to add selectable gui v2
//                            if(vol.getMorphologicalCount() > 0){
//                            int[] x_pixels_morph = vol.getMorphPixelsX(0);
//                            int[] y_pixels_morph = vol.getMorphPixelsY(0);
//                            int[] z_pixels_morph = vol.getMorphPixelsZ(0);
//                            for (int c = 0; c < x_pixels_morph.length; c++) {
//                                if(z_pixels_morph[c] == i){
//                                g2.setColor(Color.GREEN);
//                                g2.drawRect(x_pixels_morph[c], y_pixels_morph[c], 1, 1);
//                                }
//                            }
//                            }
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
                    percentage = percentage.divide(totalBD, 3, BigDecimal.ROUND_HALF_UP);

                    BigDecimal percentageGated = new BigDecimal(gated);
                    BigDecimal totalGatedBD = new BigDecimal(total);
                    percentageGated = percentageGated.divide(totalGatedBD, 3,
                            BigDecimal.ROUND_HALF_UP);

                    BigDecimal percentageGatedSelected = new BigDecimal(gatedSelected);
                    BigDecimal totalGatedSelectedBD = new BigDecimal(total);
                    percentageGatedSelected
                            = percentageGatedSelected.divide(totalGatedSelectedBD,
                                    3, BigDecimal.ROUND_HALF_UP);
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
            if (mapGates || impoverlay.getOverlay() != null || impoverlay != null) {
                impoverlay.getOverlay().clear();
            }
            gm.setMeasurementsText("No gate selected...");
        }
        this.updateimage = true;

    }

    public ArrayList<ImageStack> makeOverlayVolume(ArrayList<PolygonGate> gates, int x, int y,
            int xAxis, int yAxis) {

        ArrayList<ImageStack> alIs = new ArrayList<>();
        
        HashMap<Double, Integer> objPositions = new HashMap();
        
        objPositions = getSerialIDHashMap(objects);

        PolygonGate gate;
        ListIterator<PolygonGate> gate_itr = gates.listIterator();

        int gatedSelected = 0;
        int gatecount = gates.size();

        while (gate_itr.hasNext()) {
            gate = gate_itr.next();

            if (gate.getSelected()) {

                Path2D.Double path = gate.createPath2DInChartSpace();

                ArrayList<MicroObject> result = new ArrayList<MicroObject>();
              ///  ArrayList<MicroObject> volumes = (ArrayList) objects;
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
                    double object = (double) (al.get(0));

                    MicroObject obj = objects.get(objPositions.get(object));

                    if (impoverlay.getRoi() != null) {
                        if (impoverlay.getRoi().contains((int) obj.getCentroidX(),
                                (int) obj.getCentroidY())) {
                            result.add(objects.get(objPositions.get(object)));
                        }
                    } else {
                        result.add(objects.get(objPositions.get(object)));
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
                alIs.add(gateOverlay);
                gatecount++;
            }
        }
        return alIs;
    }
    
    @Override
        public ArrayList<ImageStack> makeGateOverlayVolume(ArrayList<PolygonGate> gates) {

        ArrayList<ImageStack> alIs = new ArrayList<>();

        HashMap<Double, Integer> objPositions = new HashMap();

        objPositions = getSerialIDHashMap(objects);

        PolygonGate gate;
        ListIterator<PolygonGate> gate_itr = gates.listIterator();

        int gatedSelected = 0;
        int gatecount = gates.size();

        while (gate_itr.hasNext()) {
            gate = gate_itr.next();

            if (gate.getSelected()) {

                Path2D.Double path = gate.createPath2DInChartSpace();

                ArrayList<MicroObject> result = new ArrayList<MicroObject>();
                ///  ArrayList<MicroObject> volumes = (ArrayList) objects;
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
                    double object = (double) (al.get(0));

                    MicroObject obj = objects.get(objPositions.get(object));

                    if (impoverlay.getRoi() != null) {
                        if (impoverlay.getRoi().contains((int) obj.getCentroidX(),
                                (int) obj.getCentroidY())) {
                            result.add(objects.get(objPositions.get(object)));
                        }
                    } else {
                        result.add(objects.get(objPositions.get(object)));
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
                alIs.add(gateOverlay);
                gatecount++;
            }
        }
        return alIs;
    }
    
    @Override
    public ImageStack makeSelectedGateOverlayVolume(PolygonGate gate) {

        
        HashMap<Double, Integer> objPositions = new HashMap();

        objPositions = getSerialIDHashMap(objects);

        Path2D.Double path = gate.createPath2DInChartSpace();

        ArrayList<MicroObject> result = new ArrayList<MicroObject>();
        
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
        
        //System.out.println("PROFILING: Gate mask, objects found: " + resultKey.size());
        //System.out.println("PROFILING: Gate mask, lookup hashmap: " + objPositions.size());

        int offsetx = 0;
        int offsety = 0;

        if (impoverlay.getRoi() != null) {
            Roi r = impoverlay.getRoi();
            offsetx = r.getBounds().x;
            offsety = r.getBounds().y;
        }

        while (itr.hasNext()) {
            ArrayList al = itr.next();
            double object = (double) (al.get(0));

            MicroObject obj = objects.get(objPositions.get(object));

            if (impoverlay.getRoi() != null) {
                if (impoverlay.getRoi().contains((int) obj.getCentroidX(),
                        (int) obj.getCentroidY())) {
                    result.add(objects.get(objPositions.get(object)));
                }
            } else {
                result.add(objects.get(objPositions.get(object)));
            }
        }

        Overlay overlay = new Overlay();

        int count = 0;

        ImageStack gateOverlay = new ImageStack(impoverlay.getWidth(),
                impoverlay.getHeight());

        //gated = getGatedObjects(impoverlay);
        

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
        return gateOverlay;
    }

    @Override
    public ImageStack makeObjectsOverlayVolume(ArrayList<MicroObject> objects) {

        HashMap<Double, Integer> objPositions = new HashMap();
        objPositions = getSerialIDHashMap(objects);

        int offsetx = 0;
        int offsety = 0;

        if (impoverlay.getRoi() != null) {
            Roi r = impoverlay.getRoi();
            offsetx = r.getBounds().x;
            offsety = r.getBounds().y;
        }

        Overlay overlay = new Overlay();

        int count = 0;

        ImageStack imageOverlay = new ImageStack(impoverlay.getWidth(),
                impoverlay.getHeight());

        Collections.sort(objects, new ZComparator());

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
            ListIterator<MicroObject> vitr = objects.listIterator();
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
                        g2.setColor(Color.RED);
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

            imageOverlay.addSlice(ir.getProcessor());
        }
        return imageOverlay;
    }

    @Override
    public int getGatedObjects(ImagePlus ip) {
        ArrayList<MicroObject> ImageGatedObjects = new ArrayList<MicroObject>();
        try {
            ListIterator<MicroObject> itr = objects.listIterator();
            while (itr.hasNext()) {
                MicroObject m = itr.next();
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
        gl.addRandomizationListener(this);
        gl.addSpatialListener(this);
        gl.addDatasetUtilitiesListener(this);

        gl.msActive = false;

        JXLayer<JComponent> gjlayer = gl.createLayer(chart, gates, hm.get(currentX), hm.get(currentY));

        gjlayer.setLocation(0, 0);
        CenterPanel.removeAll();
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
    public void updatePlot(int x, int y, int l, int size, boolean gatePlot) {
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
    public PolygonGate getGate(int x, int y, int l, int size) {
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
        gl.addRandomizationListener(this);
        gl.addSpatialListener(this);
        gl.addDatasetUtilitiesListener(this);

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
        
        //boolean includeImage = false;
        
       ImageStack[] isAll = new ImageStack[gates.size()];
        
//        if(includeImage){
//
//        isAll = new ImageStack[impoverlay.getNChannels()
//                + gates.size()];
//
//        //get channels from original image
//        for (int i = 1; i <= impoverlay.getNChannels(); i++) {
//
//            if (impoverlay.getRoi() != null) {
//                isAll[i - 1] = ChannelSplitter.getChannel(new Duplicator().run(impoverlay), i);
//            } else {
//                isAll[i - 1] = ChannelSplitter.getChannel(impoverlay, i);
//            }
//        } 
//        }

        if (gates.size() > 0) {
            for (int i = 0; i < gates.size(); i++) {
                //isAll[i + impoverlay.getNChannels()]
                isAll[i]= alIs.get(i);
            }
        }
        ImagePlus[] images = new ImagePlus[isAll.length];

//        ij.process.LUT[] oldLUT = impoverlay.getLuts();
//
        for (int i = 0; i < isAll.length; i++) {
            images[i] = new ImagePlus("Channel_" + i, isAll[i]);
            if (impoverlay.getSlice() > 1) {
                StackConverter sc = new StackConverter(images[i]);
//                if (i < impoverlay.getNChannels()) {
//                    images[i].setLut(oldLUT[i]);
//                }
                sc.convertToGray8();
            } else {
                ImageConverter ic = new ImageConverter(images[i]);
//                if (i < impoverlay.getNChannels()) {
//                    images[i].setLut(oldLUT[i]);
//                }
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
    public void onPasteGate(PolygonGate gt) {
        //gates = gt;
        //gm.updateTable(gates);
        //gt.setSelected(true);
        gl.importGate(gt);
        gm.updateTable(gates, mapGates);
        gm.setVisible(true);
//        gm.addGateToTable(gates.get(gates.size() - 1));
//        gm.pack();
//        gm.repaint();
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
                this.explorerLutIndex, this.explorerPointSizeIndex, false);

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

                //position Serial ID connection FIXED

                ArrayList<ArrayList> al = cloneGatedObjectsMeasurements(false);
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
                                path.getBounds2D().getY() + path.getBounds2D().getHeight());

                ListIterator<ArrayList> itr = resultKey.listIterator();

                while (itr.hasNext()) {
                    ArrayList al = itr.next();
                    int object = ((Number) (al.get(3))).intValue();
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

                                    measurementsFinal.add(
                                            cloneMeasurements(
                                                    measurementsGated.get(i)));
                                    position++;
                                }

                            } else {

                                if (renumber) {
                                    object.setSerialID(objectsFinal.size());
                                }

                                objectsFinal.add(object);

                                measurementsFinal.add(
                                        cloneMeasurements(
                                                measurementsGated.get(i)));
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
        JCheckBox position = new JCheckBox("Use Position", false);
        //JCheckBox parentID = new JCheckBox("Use ParentID", false);

        ProgressTracker tracker = new ProgressTracker();

        double progress;
        
        jf.setDialogTitle("Select CSV file");

//        JPanel panel1 = (JPanel) jf.getComponent(1);
//        JPanel panel2 = (JPanel) panel1.getComponent(2);
//        JPanel panel3 = (JPanel) panel2.getComponent(2);
//
//        panel3.add(zero);
//        panel3.add(position);

        int returnVal = jf.showOpenDialog(CenterPanel);
        File file = jf.getSelectedFile();

        boolean zeroOrder = zero.isSelected();
        boolean positionImport = position.isSelected();

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
                FeatureColumns.add(new ArrayList<>());
            }
            
            
            //ArrayList<String> parentIDs = 
            

            //build data by column
            for (int c = 0; c < this.objects.size(); c++) {
                
                String positionText = objects.get(c).getCentroidX() + "_" +
                        objects.get(c).getCentroidY() + "_" +
                        objects.get(c).getCentroidZ();      
                
                ArrayList<Number> data = this.getObjectDataAll(zeroOrder, 
                        positionImport, csvData, blank, (int)objects.get(c).getSerialID(), positionText);
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
                this.notifyAddFeatureListener(
                        (columnTitles[m].replace(".", "_")).replace("/", "_"),
                        (columnTitles[m].replace(".", "_")).replace("/", "_"), 
                        result);
                System.gc();
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
                this.notifyAddFeatureListener((columnTitles[m + 1].replace(".", "_")).replace("/", "_"),(columnTitles[m + 1].replace(".", "_")).replace("/", "_"), result);

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
            String name = file.getName();
            name = name.replace(".", "_");
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

    private ArrayList<Number> getObjectDataAll(boolean zeroOrder, boolean positionImport,
            ArrayList<ArrayList<Number>> data, ArrayList<Number> blank, float id, String position) {
        ArrayList<Number> result = new ArrayList<>();
        boolean elementFound = false;
        for (int x = 0; x < data.size(); x++) {

            ArrayList<Number> test = data.get(x);
            
            
            if(positionImport){
                String positionText = (Float)(test.get(1)) + "_" +
                        (Float)(test.get(2)) + "_" +
                        (Float)(test.get(3)); 
                if(position.equals(positionText)){
                    result.addAll(test);
                    elementFound = true;
                    data.remove(x);
                    return result;
                }
            } else {

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
        }
        result.addAll(blank);
        return result;
    }

    @Override
    public void addDensityMapFromGate(String s) {

//position Serial ID connection FIXED

        ArrayList<ArrayList> al = cloneGatedObjectsMeasurements(false);
        ArrayList<MicroObject> objectsTemp = new ArrayList<MicroObject>();
        ArrayList<ArrayList<Number>> measurementsFinal = new ArrayList<ArrayList<Number>>();

        objectsTemp = al.get(0);

        ImagePlus map = densityMaps3D.makeMap(impoverlay, objectsTemp);
        ImagePlus mapRandom = densityMaps3D.makeRandomMap(impoverlay, objectsTemp);

        densityMaps3D.addMap(map, s);

        measurementsFinal = densityMaps3D.getDistance(objects, map);

        this.notifyAddFeatureListener(s, "3D density map", measurementsFinal);

        densityMaps3D.addMap(mapRandom, s + "_Random");

        measurementsFinal = densityMaps3D.getDistance(objects, mapRandom);

        this.notifyAddFeatureListener(s + "_Random", "Random 3D density map",measurementsFinal);
    }
    
    @Override
    public void addSpatialFromGate(String name){
        
       SpatialSettingsDialog settingsDialog = new SpatialSettingsDialog();
        settingsDialog.setFocusable(false);

        if (settingsDialog.showDialog()) {

            ArrayList<String> settings = settingsDialog.getSettings();

            String zScale = settings.get(1);
            String interval = settings.get(2);
            ProgressTracker pt = new ProgressTracker();
            
            pt.createandshowGUI("Calculating Spatial Feature", explorerXposition, explorerYposition);
            addPropertyChangeListener(pt);
            
            firePropertyChange("method", "", "Ripley's K");
            firePropertyChange("indeterminant", " setup", "");

            //1) get gated cells x,y,z, include rois (use px)
            
            ArrayList<ArrayList> al = cloneGatedObjectsMeasurements(false);
            
            HashMap<Double, Integer> objPositions = new HashMap();
        
            objPositions = getSerialIDHashMap(objects);

            //start factory
            ArrayList<MicroObject> objectsTemp = new ArrayList<MicroObject>();
            objectsTemp = al.get(0);
            
            if(objectsTemp.size() > 0){

            ListIterator itrName = objectsTemp.listIterator();

            double[][] key = new double[objectsTemp.size()][1];
            double[][] data = new double[objectsTemp.size()][3];
            
            //2) get randomized x,y,z in roi if exists (use px)
            

            
            //3) calculate area of roi or whole image (use px)
            
            double A = 0;
            double n = objectsTemp.size();
            int distance_max = 0;
             int distance_inc = 5;
            
            if(impoverlay.getRoi() != null){
                A = impoverlay.getRoi().size();
                Roi r = impoverlay.getRoi();
                distance_max = 
                        (int)Math.sqrt(Math.pow(r.getBounds().getHeight(), n) + 
                        Math.pow(r.getBounds().getWidth(), n));
            } else {
                A = impoverlay.getWidth() * impoverlay.getHeight();
                                distance_max = 
                        (int)Math.sqrt(Math.pow(impoverlay.getHeight(), n) + 
                        Math.pow(impoverlay.getWidth(), n));
            }
            
            //4) build knn test and random
            
            KDTree kd_test = new KDTree(data,key);
            KDTree[] kd_MC = new KDTree[10];
            
            //Monte Carlo of random for 95 CI estimation
            
            for(int i = 0; i < 9; i++){
                
                double progress = 100 * ((double) (i+1)/ (double) (10));
                firePropertyChange("method", "", "Running Monte Carlo...");
                firePropertyChange("progress", " Running Monte Carlo...", (int) progress);

                    double[][] keyMC = new double[objectsTemp.size()][1];
                    double[][] dataMC = new double[objectsTemp.size()][3];

                    ArrayList<ArrayList<Integer>> randomized = this.randomize(objectsTemp.size());
                    
                for(int j = 0; j < objectsTemp.size(); j++){
                    

                    MicroObject obj = objectsTemp.get(j);
                    
                    double[] k = new double[1];
                    double[] d = new double[3];
                      
                    k[0] = obj.getSerialID();
                    key[j] = k;
                    
                    d[0] = (double)randomized.get(0).get(j);
                    d[1] = (double)randomized.get(1).get(j);
                    d[2] = (double)randomized.get(2).get(j)*(Double.parseDouble(zScale));
                    
                    data[j] = d;

                }
                
                kd_MC[i] = new KDTree(dataMC,keyMC);
  
            }
            //each entry is an interval
            ArrayList<Float> K_test = new ArrayList<Float>();
            
            //each arraylist is a imulation, each entry is an interval
            ArrayList<ArrayList<Float>> K_MC = new ArrayList<ArrayList<Float>>();
            
            
            //5) loop for distance up to max for test,
            // 
            //   calculate K, K = A/n * Sum of (distance/n)
            
            int step = Integer.parseInt(interval);
            
            int max_distance = impoverlay.getWidth();
            if(max_distance > impoverlay.getHeight()){
                max_distance = impoverlay.getHeight();
            }
            
            //////////KDTree kd_test = new KDTree(data,key);
            //KDTree[] kd_MC = new KDTree[10];
            
            for(int i = step; i < max_distance; i =+ step){
                
                //parse all objects
                
                for(int j = 0; j < kd_MC.length; j++){
                    
                }
                
            }
           
            // distance max = distance_max;
            // distance step = interval;
            
            //6) loop through randomized and for distance up to max for random, calculate K
            //  Calculatea aK = A/n * Sum distance/n
            // 
               
            
          
            
            

            ArrayList<Integer> classes = new ArrayList<>();


        }
        } else {
            
        }
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
            String zScale = settings.get(3);
            String kNeighbors = settings.get(4);
            String interval = settings.get(5);
            String randomize = settings.get(6);
            
            boolean random = false;
            
            if(randomize.equals("Y")){random = true;}

            ProgressTracker pt = new ProgressTracker();
            pt.createandshowGUI("Neighborhood Analysis", explorerXposition, explorerYposition);
            addPropertyChangeListener(pt);
            firePropertyChange("method", "", method);
            firePropertyChange("indeterminant", " setup", "");

            ArrayList<Number> classes = new ArrayList<>();

            ArrayList<ArrayList<Number>> features
                    = H2DatabaseEngine.getColumn(vtea._vtea.H2_MEASUREMENTS_TABLE + "_"
                            + keySQLSafe, this.descriptions.get(feature));

            double maxClass = this.getMaximumOfData(features, 0);
            double minClass = this.getMinimumOfData(features, 0);
            
            if(minClass > 0){
                minClass = 0;
            }
            
          
            for (int i = (int) minClass; i <= (int) maxClass; i++) {
                classes.add(i);        
            }

            HashMap<String, String> objFeature = new HashMap<>();

            for (int c = 0; c < this.objects.size(); c++) {
                objFeature.put(String.valueOf((objects.get(c)).getSerialID()), String.valueOf(features.get(c).get(0)));
            }
            //position Serial ID connection FIXED
            ArrayList<ArrayList> al = cloneGatedObjectsMeasurements(false); //erroring out on 2 channel image

            //start factory
            ArrayList<MicroObject> objectsTemp = new ArrayList<MicroObject>();
            ArrayList<ArrayList<Number>> measurementsFinal = new ArrayList<ArrayList<Number>>();
            objectsTemp = al.get(0);
            measurementsFinal = al.get(1);

            ListIterator itrName = objectsTemp.listIterator();
            
            HashMap<Double, Integer> objIDLookUp = new HashMap<>();

            double[][] key = new double[objectsTemp.size()][1];
            double[][] data = new double[objectsTemp.size()][3];

            int i = 0;
            
            ArrayList<ArrayList<Integer>> randomized = this.randomize(objectsTemp.size());

            while (itrName.hasNext()) {
                MicroObject obj = (MicroObject) itrName.next();
                double[] k = new double[1];
                k[0] = obj.getSerialID();
                
                objIDLookUp.put(k[0], i);
                
                //System.out.println("PROFILING: key, " + k[0] + ", position: " + i);
           
                key[i] = k;
                double[] d = new double[3];
                if(random){
                   
                d[0] = (double)randomized.get(0).get(i);
                d[1] = (double)randomized.get(1).get(i);
                d[2] = (double)randomized.get(2).get(i)*(Double.parseDouble(zScale));
                    
                data[i] = d;
                i++;
                } else {
                d[0] = obj.getCentroidX();
                d[1] = obj.getCentroidY();
                d[2] = obj.getCentroidZ()*(Double.parseDouble(zScale));
                data[i] = d;
                i++;
            }
            }
     
            ArrayList<ArrayList<Neighbor>> neighborhoods = new ArrayList<ArrayList<Neighbor>>();

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


                    ArrayList<Double> neighborhoodObjects = new ArrayList<>();

                    neighborhoodObjects.add(key[objectKey][0]);

                    ArrayList<MicroObject> localObjects = new ArrayList<>();

                    localObjects.add(objectsTemp.get((objIDLookUp.get(key[objectKey][0]))));


                    for (int l = 0; l < neighbors.size(); l++) {
                        double[] id = (double[]) ((Neighbor) neighbors.get(l)).value;
                        // str = str + "," + id[0];
                        neighborhoodObjects.add(id[0]);
                        MicroObject objectAdd = (objectsTemp.get(objIDLookUp.get(id[0])));
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
                    
                    //System.out.println("PROFILING: lookingup: " + key[objectKey][0]);

                    localObjects.add(objectsTemp.get(objIDLookUp.get(key[objectKey][0])));

                    for (int l = 0; l < neighbors.size(); l++) {
                        double[] id = (double[]) ((Neighbor) neighbors.get(l)).value;
                        neighborhoodObjects.add(id[0]);
                        MicroObject objectAdd = (objectsTemp.get(objIDLookUp.get(id[0])));
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

                HashMap<Double, Integer> objectPosition = this.getSerialIDHashMap(objectsTemp);
                double maxSerialID = this.getSerialIDMax(objectsTemp);

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
                int referenceKey = (int) this.getSerialIDMax(objectsTemp) + 1;

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

                firePropertyChange("method", "", "Spatial by point..");
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

                            MicroObject objectAdd = objectsTemp.get(objIDLookUp.get(id[0]));

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
    
    public ArrayList<ArrayList<Integer>> randomize(int count){
          
        Roi pr = impoverlay.getRoi();
        boolean roi = false;
        int maxX = impoverlay.getWidth();
        int maxY = impoverlay.getHeight();
        int maxZ = impoverlay.getNSlices();
        
        
        if(pr == null){
            roi = false;
        } else {
            roi = true;
        }
        
        ProgressTracker tracker = new ProgressTracker();
        
        double progress = 0;
        
        //System.out.println("PROFILING: Object positions to randomize:  " + count);
        
        tracker.createandshowGUI("Randomizing positions...", 0, 0);
        addPropertyChangeListener(tracker);
        
        
        Random randX = new Random();
        Random randY = new Random();
        Random randZ = new Random();
        
        int nextRandomX = 0;
        int nextRandomY = 0;
        int nextRandomZ = 0;
        
        ArrayList<Integer> randomizedX = new ArrayList<Integer>();
        ArrayList<Integer> randomizedY = new ArrayList<Integer>();
        ArrayList<Integer> randomizedZ = new ArrayList<Integer>();

        for (int i = 0; i < count; i++) {
            
            progress = 100 * ((double) i / (double) count);

            firePropertyChange("method", "", "Randomizing positions");
            firePropertyChange("progress", "Randomizing...", (int) progress);

            boolean repeat = true;

            while (repeat) {
                nextRandomX = randX.nextInt(maxX);
                nextRandomY = randY.nextInt(maxY);
                nextRandomZ = randZ.nextInt(maxZ);
                if (roi) {
                    if (pr.contains(nextRandomX, nextRandomY)) {
                        randomizedX.add(nextRandomX);
                        randomizedY.add(nextRandomY);
                        randomizedZ.add(nextRandomZ);
                        repeat = false;
                    }
                } else {
                    randomizedX.add(nextRandomX);
                    randomizedY.add(nextRandomY);
                    randomizedZ.add(nextRandomZ);
                    repeat = false;
                }
            }
        }
        ArrayList<ArrayList<Integer>> result = new ArrayList<>();
        
        result.add(randomizedX);
        result.add(randomizedY);
        result.add(randomizedZ);
        tracker.setVisible(false);
        
        return result;
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
        
        //System.out.println("PROFILING: calculating distance.");

        ArrayList<ArrayList> al = cloneGatedObjectsMeasurements(false);
        ArrayList<MicroObject> objectsTemp = new ArrayList<MicroObject>();
        ArrayList<ArrayList<Number>> measurementsFinal
                = new ArrayList<ArrayList<Number>>();

        objectsTemp = al.get(0);

        ImagePlus map = distanceMaps2D.makeMap(impoverlay, objectsTemp);
        ImagePlus mapRandom = distanceMaps2D.makeRandomMap(impoverlay, objectsTemp);

        distanceMaps2D.addMap(map, s);
        distanceMaps2D.addMap(mapRandom, s + "_Random");

        measurementsFinal = distanceMaps2D.getDistance(objects, map);

        this.notifyAddFeatureListener(s, "2D distance map", measurementsFinal);

        measurementsFinal
                = new ArrayList<ArrayList<Number>>();

        measurementsFinal = distanceMaps2D.getDistance(objects, mapRandom);

        this.notifyAddFeatureListener(s + "_Random", "Random 2D distance map", measurementsFinal);
    }

    @Override
    public void addFeatureListener(AddFeaturesListener listener) {
        addfeaturelisteners.add(listener);
    }

    @Override
    public void notifyAddFeatureListener(String description, String descriptionLabel,
            ArrayList<ArrayList<Number>> feature) {
        for (AddFeaturesListener listener : addfeaturelisteners) {
            listener.addFeatures(description,descriptionLabel, feature);
        }
    }

    @Override
    public void addupdateExplorerGUIListener(UpdateExplorerGuiListener listener) {
        updateexlporerguilisteners.add(listener);
    }

    @Override
    public void notifyUpdateExplorerGUIListener() {
        for (UpdateExplorerGuiListener listener : updateexlporerguilisteners) {
            listener.rebuildExplorerGUI(descriptions.get(currentX), 
                    descriptions.get(currentY), descriptions.get(currentL), "15", false);
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
            max = Math.round(getMaximumOfData(H2DatabaseEngine.getColumn(
                    vtea._vtea.H2_MEASUREMENTS_TABLE + "_" 
                            + keySQLSafe, selectedIndexText), 0));
            min = Math.round(getMinimumOfData(H2DatabaseEngine.getColumn(
                    vtea._vtea.H2_MEASUREMENTS_TABLE + "_" 
                            + keySQLSafe, selectedIndexText), 0));

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

        if (ip.getID() == impoverlay.getID() && updateimage && 
                impZ != impoverlay.getCurrentSlice()) {
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
    public void addFeatures(String description, String descriptionLabel, 
            ArrayList<ArrayList<Number>> al) {
        this.notifyAddFeatureListener(description, descriptionLabel, al);
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
        DataUtilities.setLocation(xPos, yPos);
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
            //position Serial ID connection FIXED
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

    @Override
    public void importImageFeatures() {
                   ArrayList<String> Channels = new ArrayList<String>();
            for (int i = 0; i <= impoverlay.getNChannels() - 1; i++) {
                Channels.add("Channel_" + (i + 1));
            }  
            ImageFeatureAddFrame mf = new ImageFeatureAddFrame(key, Channels, 
                    impoverlay, objects, connection);
            mf.addListener(this);
            mf.setVisible(true);
    }

    @Override
    public void addRandomization(String type) {
        if(type.equals("position")){
            PositionRandomization pr = new PositionRandomization(objects.size(), 
                    impoverlay);
            pr.addFeatureListener(this);
            pr.process();
        } else if(type.equals("class")){
            ClassRandomization cr = new ClassRandomization(H2DatabaseEngine.getColumnInt(
                    vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe, "Assigned"));
            cr.addFeatureListener(this);
            cr.process();
        }
    }

    @Override
    public void onGatePlot(String StringX, String StringY) {
        
        ListIterator<String> itr = descriptions.listIterator();
        
        int xPos = this.currentX;
        int yPos = this.currentY;
        int count = 0;
        
        while(itr.hasNext()){
            String str = itr.next();
            if(str.equals(StringX)){
                xPos = count;
            }
            if(str.equals(StringY)){
                yPos = count;
            }
            count++;
        }
        //System.out.println("PROFILING: " + xPos +", " + yPos + ".");
      updatePlot(xPos, yPos, this.currentL, 4, true);
    }


    @Override
    public void dataSetUtilities() {
        DataUtilities.setVisible(true);    
    }

    @Override
    public ArrayList<PolygonGate> getGates() {
        return gl.getGates();
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
        
        double[] pixelSize = new double[3];

        String[] methods = {"Spatial by cell", "Spatial by position", "Nearest-k"};

        String[] methodsDetail = {"Uses a spatial kernel with a fixed radius centered on a cell.", "Uses a spherical spatial kernel with a fixed radius.",
            "Selects the k-nearest neighbors of a cell."};

        JComboBox method = new JComboBox(methods);

        JLabel radiusLabel = new JLabel("Radius");
        JTextField radius = new JTextField("50", 5);
        JLabel zLabel = new JLabel("Voxel Z scale");
        JTextField zScale = new JTextField("2", 5);
        JLabel kNeighborsLabel = new JLabel("Neighbors");
        JTextField kNeighbors = new JTextField("10", 5);
        JLabel intervalLabel = new JLabel("Interval");
        JTextField interval = new JTextField(radius.getText(), 5);
        JLabel warning = new JLabel("CAUTION: Use a discrete feature.");
        JLabel randomize = new JLabel("Randomize");
        String[] choice = {"N", "Y"};
        JComboBox random = new JComboBox(choice);

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
            gbc = new GridBagConstraints(0, 3, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
            menu.add(randomize, gbc);
            gbc = new GridBagConstraints(0, 4, 1, 1, 1, 1.0, GridBagConstraints.WEST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
            menu.add(random, gbc);

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
                submenu.add(zLabel, gbc);
                gbc = new GridBagConstraints(1, 1, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(zScale, gbc);
                
                gbc = new GridBagConstraints(0, 2, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(randomize, gbc);
                gbc = new GridBagConstraints(1, 2, 1, 1, 1, 1.0, GridBagConstraints.WEST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(random, gbc);
                

                gbc = new GridBagConstraints(0, 3, 1, 1, 0.2, 1.0,
                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(new JLabel("     "), gbc);
                gbc = new GridBagConstraints(1, 3, 1, 1, 1, 1.0, GridBagConstraints.EAST,
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
                submenu.add(zLabel, gbc);
                gbc = new GridBagConstraints(1, 1, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(zScale, gbc);
                gbc = new GridBagConstraints(0, 2, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(randomize, gbc);
                gbc = new GridBagConstraints(1, 2, 1, 1, 1, 1.0, GridBagConstraints.WEST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(random, gbc);
                gbc = new GridBagConstraints(0, 3, 1, 1, 0.2, 1.0,
                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(intervalLabel, gbc);
                gbc = new GridBagConstraints(1, 3, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(interval, gbc);

            } else {
                GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0.2, 1.0,
                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(kNeighborsLabel, gbc);
                gbc = new GridBagConstraints(1, 0, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(kNeighbors, gbc);
                gbc = new GridBagConstraints(0, 1, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(randomize, gbc);
                gbc = new GridBagConstraints(1, 1, 1, 1, 1, 1.0, GridBagConstraints.WEST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(random, gbc);    
                gbc = new GridBagConstraints(0, 2, 1, 1, 0.2, 1.0,
                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(new JLabel("     "), gbc);
                gbc = new GridBagConstraints(1, 2, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(new JLabel("     "), gbc);
                gbc = new GridBagConstraints(0, 3, 1, 1, 0.2, 1.0,
                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
                submenu.add(new JLabel("     "), gbc);
                gbc = new GridBagConstraints(1, 3, 1, 1, 1, 1.0, GridBagConstraints.EAST,
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
                settings.add(zScale.getText());
                settings.add(kNeighbors.getText());
                settings.add(interval.getText());
                settings.add((String)random.getSelectedItem());
            } else {
                return false;
            }
            return true;
        }

        public ArrayList<String> getSettings() {

            return settings;
        }
    }
    
    class SpatialSettingsDialog extends JOptionPane {

        ArrayList<String> settings = new ArrayList<String>();

//        JComboBox classification = new JComboBox(descriptions.toArray());
        
        double[] pixelSize = new double[3];

        String[] methods = {"Ripley's K"};

        String[] methodsDetail = {"Calculate self-association with distance.",};
        JLabel intervalLabel = new JLabel("Step");
        JTextField interval = new JTextField("5", 5);
        JComboBox method = new JComboBox(methods);
        JLabel zLabel = new JLabel("Voxel Z scale");
        JTextField zScale = new JTextField("2", 5);
        //JLabel randomize = new JLabel("Randomize");
        //String[] choice = {"N", "Y"};
        //JComboBox random = new JComboBox(choice);

        JPanel menu = new JPanel();
        JPanel submenu = new JPanel();

        boolean result = false;

        public SpatialSettingsDialog() {

            super();

//            classification.setSelectedIndex(descriptions.size() - 1);
//
//            classification.addActionListener(new java.awt.event.ActionListener() {
//                public void actionPerformed(java.awt.event.ActionEvent evt) {
//                    CheckRangeOfValues(classification.getSelectedIndex());
//                }
//            });
    

            ArrayList<String> tips = new ArrayList<String>();

            tips.add(methodsDetail[0]);

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
            menu.add(intervalLabel, gbc);

            gbc = new GridBagConstraints(1, 0, 1, 1, 0.2, 1.0,
                    GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
            menu.add(interval, gbc);

            gbc = new GridBagConstraints(0,1, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
            menu.add(new JLabel("    "), gbc);
            gbc = new GridBagConstraints(1,1, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
            menu.add(method, gbc);
//            gbc = new GridBagConstraints(0, 2, 1, 1, 1, 1.0, GridBagConstraints.EAST,
//                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
//            menu.add(randomize, gbc);
//            gbc = new GridBagConstraints(1, 2, 1, 1, 1, 1.0, GridBagConstraints.WEST,
//                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
//            menu.add(random, gbc);

            method.setSelectedIndex(0);

        }

        private void ProcessSelectComboBoxActionPerformed(java.awt.event.ActionEvent evt) {

            submenu.setVisible(false);
            submenu.removeAll();

            submenu.setLayout(new GridBagLayout());

            String methodString = (String) method.getSelectedItem();

//            if (methodString.equals("Ripley's K")) {
//
//                GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0.2, 1.0,
//                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
//
////                gbc = new GridBagConstraints(0, 0, 1, 1, 0.2, 1.0,
////                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
////                submenu.add(radiusLabel);
////                gbc = new GridBagConstraints(1, 0, 1, 1, 1, 1.0, GridBagConstraints.EAST,
////                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
////                submenu.add(radius, gbc);
//                gbc = new GridBagConstraints(0, 3, 1, 1, 0.2, 1.0,
//                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
//                submenu.add(intervalLabel, gbc);
//                
//                gbc = new GridBagConstraints(0, 1, 1, 1, 0.2, 1.0,
//                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
//                submenu.add(zLabel, gbc);
//                gbc = new GridBagConstraints(1, 1, 1, 1, 1, 1.0, GridBagConstraints.EAST,
//                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
//                submenu.add(zScale, gbc);
//                
//                gbc = new GridBagConstraints(0, 2, 1, 1, 1, 1.0, GridBagConstraints.EAST,
//                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
//                submenu.add(randomize, gbc);
//                gbc = new GridBagConstraints(1, 2, 1, 1, 1, 1.0, GridBagConstraints.WEST,
//                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
//                submenu.add(random, gbc);
//                
//
//                gbc = new GridBagConstraints(0, 3, 1, 1, 0.2, 1.0,
//                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
//                submenu.add(new JLabel("     "), gbc);
//                gbc = new GridBagConstraints(1, 3, 1, 1, 1, 1.0, GridBagConstraints.EAST,
//                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
//                submenu.add(new JLabel("     "), gbc);
//
////            } else if (methodString.equals("Spatial by position")) {
////                GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0.2, 1.0,
////                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
////                submenu.add(radiusLabel);
////                gbc = new GridBagConstraints(1, 0, 1, 1, 1, 1.0, GridBagConstraints.EAST,
////                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
////                submenu.add(radius, gbc);
////                gbc = new GridBagConstraints(0, 1, 1, 1, 0.2, 1.0,
////                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
////                submenu.add(zLabel, gbc);
////                gbc = new GridBagConstraints(1, 1, 1, 1, 1, 1.0, GridBagConstraints.EAST,
////                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
////                submenu.add(zScale, gbc);
////                gbc = new GridBagConstraints(0, 2, 1, 1, 1, 1.0, GridBagConstraints.EAST,
////                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
////                submenu.add(randomize, gbc);
////                gbc = new GridBagConstraints(1, 2, 1, 1, 1, 1.0, GridBagConstraints.WEST,
////                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
////                submenu.add(random, gbc);
////                gbc = new GridBagConstraints(0, 3, 1, 1, 0.2, 1.0,
////                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
////                submenu.add(intervalLabel, gbc);
////                gbc = new GridBagConstraints(1, 3, 1, 1, 1, 1.0, GridBagConstraints.EAST,
////                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
////                submenu.add(interval, gbc);
////
////            } else {
////                GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0.2, 1.0,
////                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
////                submenu.add(kNeighborsLabel, gbc);
////                gbc = new GridBagConstraints(1, 0, 1, 1, 1, 1.0, GridBagConstraints.EAST,
////                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
////                submenu.add(kNeighbors, gbc);
////                gbc = new GridBagConstraints(0, 1, 1, 1, 1, 1.0, GridBagConstraints.EAST,
////                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
////                submenu.add(randomize, gbc);
////                gbc = new GridBagConstraints(1, 1, 1, 1, 1, 1.0, GridBagConstraints.WEST,
////                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
////                submenu.add(random, gbc);    
////                gbc = new GridBagConstraints(0, 2, 1, 1, 0.2, 1.0,
////                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
////                submenu.add(new JLabel("     "), gbc);
////                gbc = new GridBagConstraints(1, 2, 1, 1, 1, 1.0, GridBagConstraints.EAST,
////                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
////                submenu.add(new JLabel("     "), gbc);
////                gbc = new GridBagConstraints(0, 3, 1, 1, 0.2, 1.0,
////                        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
////                submenu.add(new JLabel("     "), gbc);
////                gbc = new GridBagConstraints(1, 3, 1, 1, 1, 1.0, GridBagConstraints.EAST,
////                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
////                submenu.add(new JLabel("     "), gbc);
////
//           }

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
                //settings.add(Integer.toString(classification.getSelectedIndex()));
                settings.add(methods[method.getSelectedIndex()]);
                //settings.add(radius.getText());
                settings.add(zScale.getText());
                //settings.add(kNeighbors.getText());
                settings.add(interval.getText());
                //settings.add((String)random.getSelectedItem());
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

    /**
     * Invalidate the BufferedImage cache to force recreation on next overlay render.
     * Call this when gates are added/removed/modified or when the image size changes.
     */
    public void invalidateOverlayCache() {
        overlayCacheDirty = true;
    }

    /**
     * Clear the database query cache.
     * Call this when gates are modified or when the underlying data changes.
     */
    public void clearQueryCache() {
        if (gateQueryCache != null) {
            gateQueryCache.clear();
        }
    }

    /**
     * Clear a specific query from the cache.
     * @param gate The gate whose query should be removed from cache
     */
    public void clearQueryCacheForGate(PolygonGate gate) {
        if (gateQueryCache != null && gate != null) {
            Path2D.Double path = gate.createPath2DInChartSpace();
            String cacheKey = gate.getXAxis() + "_" + gate.getYAxis() + "_" +
                             path.getBounds2D().getX() + "_" + path.getBounds2D().getY() + "_" +
                             path.getBounds2D().getWidth() + "_" + path.getBounds2D().getHeight();
            gateQueryCache.remove(cacheKey);
        }
    }

    /**
     * Override dispose to cleanup XYExplorationPanel-specific resources
     */
    @Override
    public void dispose() {
        // Remove ROI listener that was added in configureListeners
        Roi.removeRoiListener(this);

        // Cleanup database connection
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                // Log or ignore
            }
            connection = null;
        }

        // Cleanup gate manager
        if (gm != null) {
            gm.dispose();
            gm = null;
        }

        // Cleanup axes manager
        if (AxesManager != null) {
            AxesManager = null;
        }

        // Cleanup chart panel
        if (cpd != null) {
            cpd = null;
        }

        // Clear cached BufferedImages
        cachedPlaceholder = null;
        cachedSelections = null;

        // Clear query cache
        clearQueryCache();
        gateQueryCache = null;

        // Call parent cleanup
        super.dispose();
    }

    // ========== Gallery View Implementation ==========

    /**
     * Implementation of GalleryViewDataProvider interface.
     * Opens a gallery view window for the specified gate.
     */
    @Override
    public void openGalleryView(PolygonGate gate) {
        // Get cells in this gate
        ArrayList<MicroObject> gatedCells = getObjectsInGate(gate);

        if (gatedCells.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No cells found in gate \"" + gate.getName() + "\"",
                    "Empty Gate",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        // Check if we have image stacks
        if (imageStacks == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Image data not available for gallery view.\nPlease ensure image stacks are loaded.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Determine which channels to display
        int[] channels = getDefaultChannels();

        // Create or reuse gallery window
        String gateKey = gate.getName();
        GalleryViewWindow galleryWindow = galleryWindows.get(gateKey);

        if (galleryWindow == null || !galleryWindow.isVisible()) {
            galleryWindow = new GalleryViewWindow(
                    gate.getName(),
                    gatedCells,
                    imageStacks,
                    channels
            );

            // Listen for gallery selections
            final GalleryViewWindow finalGalleryWindow = galleryWindow;
            galleryWindow.addGallerySelectionListener(new GallerySelectionListener() {
                @Override
                public void gallerySelectionChanged(MicroObject selectedCell) {
                    handleGallerySelection(selectedCell);
                }
            });

            galleryWindows.put(gateKey, galleryWindow);
            galleryWindow.setVisible(true);
        } else {
            // Bring existing window to front
            galleryWindow.toFront();
            galleryWindow.requestFocus();
        }
    }

    /**
     * Get all objects within a specific gate.
     */
    private ArrayList<MicroObject> getObjectsInGate(PolygonGate gate) {
        ArrayList<MicroObject> result = new ArrayList<>();

        try {
            // Get gate bounds in data coordinates
            Path2D.Float path = gate.createPath2DInChartSpace();

            // Get axis names
            String xAxisName = gate.getXAxis();
            String yAxisName = gate.getYAxis();

            // Test each object
            for (MicroObject obj : objects) {
                double xValue = obj.getChannelTagFloat(xAxisName);
                double yValue = obj.getChannelTagFloat(yAxisName);

                if (path.contains(xValue, yValue)) {
                    result.add(obj);
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting objects in gate: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Get default channels for display (up to 3 channels for RGB).
     */
    private int[] getDefaultChannels() {
        if (imageStacks != null) {
            int numChannels = Math.min(3, imageStacks.length);
            int[] channels = new int[numChannels];
            for (int i = 0; i < numChannels; i++) {
                channels[i] = i;
            }
            return channels;
        }
        return new int[]{0};
    }

    /**
     * Handle selection from gallery view.
     */
    private void handleGallerySelection(MicroObject selectedCell) {
        if (selectedCell == null) {
            // Clear highlighting
            clearGalleryHighlight();
            return;
        }

        currentGallerySelection = selectedCell;

        // Highlight cell on XY chart
        if (cpd != null) {
            highlightCellOnChart(selectedCell);
        }

        // Highlight cell on image overlay via GateLayer
        if (gateLayer != null) {
            highlightCellOnImage(selectedCell);
        }
    }

    /**
     * Highlight a specific cell on the XY chart.
     */
    private void highlightCellOnChart(MicroObject cell) {
        if (cpd != null) {
            cpd.highlightCell(cell);
        }
    }

    /**
     * Highlight cell on image overlay.
     */
    private void highlightCellOnImage(MicroObject cell) {
        if (gateLayer != null) {
            gateLayer.highlightCell(cell);
        }
    }

    /**
     * Clear gallery highlight.
     */
    private void clearGalleryHighlight() {
        currentGallerySelection = null;

        // Clear chart highlighting
        if (cpd != null) {
            cpd.clearCellHighlight();
        }

        // Clear image highlighting
        if (gateLayer != null) {
            gateLayer.clearCellHighlight();
        }
    }

    /**
     * Set the image stacks for gallery view.
     * @param imageStacks Array of ImageStacks (one per channel)
     */
    public void setImageStacks(ImageStack[] imageStacks) {
        this.imageStacks = imageStacks;
    }

    /**
     * Set the GateLayer reference for highlighting.
     * @param layer The GateLayer instance
     */
    public void setGateLayer(GateLayer layer) {
        this.gateLayer = layer;
        // Register as data provider
        if (layer != null) {
            layer.setGalleryViewDataProvider(this);
        }
    }

    /**
     * Get the currently selected cell from gallery.
     * @return Current gallery selection, or null
     */
    public MicroObject getCurrentGallerySelection() {
        return currentGallerySelection;
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

