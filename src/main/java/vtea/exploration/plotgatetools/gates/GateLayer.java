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
package vtea.exploration.plotgatetools.gates;

import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import vtea.exploration.listeners.ManualClassListener;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;
import org.jfree.chart.ChartPanel;
import vtea._vtea;
import vtea.exploration.listeners.AssignmentListener;
import vtea.exploration.listeners.DensityMapListener;
import vtea.exploration.listeners.DistanceMapListener;
import vtea.exploration.listeners.NeighborhoodListener;
import vtea.exploration.listeners.SaveGatedImagesListener;
import vtea.exploration.listeners.SpatialListener;
import vtea.exploration.listeners.SubGateListener;
import vtea.exploration.plotgatetools.listeners.AddGateListener;
import vtea.exploration.plotgatetools.listeners.DatasetUtilitiesListener;
import vtea.exploration.plotgatetools.listeners.DeleteGateListener;
import vtea.exploration.plotgatetools.listeners.GateColorListener;
import vtea.exploration.plotgatetools.listeners.ImageHighlightSelectionListener;
import vtea.exploration.plotgatetools.listeners.PolygonSelectionListener;
import vtea.exploration.plotgatetools.listeners.QuadrantSelectionListener;
import vtea.exploration.plotgatetools.listeners.RandomizationListener;
import vtea.exploration.plottools.panels.XYExplorationPanel;
import vtea.imports.xml.roiHALO;
import vteaexploration.GateMathWindow;
import vtea.exploration.gallery.GalleryViewDataProvider;
import vteaobjects.MicroObject;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author vinfrais
 */
public class GateLayer implements ActionListener, ItemListener {

    public static PolygonGate clipboardGate;
    public static boolean gateInClipboard = false;

    public transient boolean msActive;
    public transient boolean msPolygon;
    public transient boolean msQuadrant;
    public transient boolean msRectangle;
    public transient boolean msSelected;
    public transient boolean msFinal;
    public transient boolean msSelecting;
    public transient boolean msOptions;
    public transient boolean kyShift;

    private String xAxis, yAxis;

    private ChartPanel chart;

    private ArrayList<Point> points = new ArrayList<Point>();
    private ArrayList<PolygonSelectionListener> polygonlisteners = new ArrayList<PolygonSelectionListener>();
    private ArrayList<ImageHighlightSelectionListener> highlightlisteners = new ArrayList<ImageHighlightSelectionListener>();
    private ArrayList<QuadrantSelectionListener> quadrantlisteners = new ArrayList<QuadrantSelectionListener>();
    private ArrayList<AddGateListener> addgatelisteners = new ArrayList<AddGateListener>();
    private ArrayList<DeleteGateListener> deletegatelisteners = new ArrayList<DeleteGateListener>();
    private ArrayList<GateColorListener> gatecolorlisteners = new ArrayList<GateColorListener>();
    private ArrayList<SaveGatedImagesListener> saveImageListeners = new ArrayList<SaveGatedImagesListener>();
    private ArrayList<ManualClassListener> manualClassListeners = new ArrayList<ManualClassListener>();
    private ArrayList<AssignmentListener> assignmentListeners = new ArrayList<AssignmentListener>();
    private ArrayList<SubGateListener> subGateListeners = new ArrayList<SubGateListener>();
    private ArrayList<DistanceMapListener> distanceMapListeners = new ArrayList<>();
    private ArrayList<RandomizationListener> randomizationListeners = new ArrayList<>();
    private ArrayList<DensityMapListener> densityMapListeners = new ArrayList<>();
    private ArrayList<NeighborhoodListener> neighborhoodListeners = new ArrayList<>();
    private ArrayList<SpatialListener> spatialListeners = new ArrayList<>();
    private ArrayList<DatasetUtilitiesListener> DatasetUtilitiesListeners = new ArrayList<>();

    // Gallery view data provider
    private GalleryViewDataProvider galleryViewDataProvider;

    // Gallery view cell highlighting
    private MicroObject highlightedCell;
    private JXLayer layer;  // Store layer reference for repainting
    private static final Color HIGHLIGHT_COLOR = Color.RED;
    private static final Stroke HIGHLIGHT_STROKE = new BasicStroke(
            2.0f,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND,
            10.0f,
            new float[]{5.0f, 5.0f},
            0.0f
    );

    private ArrayList<PolygonGate> gates = new ArrayList<PolygonGate>();

    private JPopupMenu menu = new JPopupMenu();

    private PolygonGate selectedGate;

    String[] colors = {"red", "green", "blue", "yellow", "orange", "yellow green",
        "light green", "cyan", "light blue", "dark blue", "purple", "pink",
        "salmon"
    };

    Color[] colorsRGB = {
        new Color(255, 0, 0), new Color(0, 255, 0), new Color(0, 0, 255),
        new Color(255, 255, 0), new Color(255, 153, 51),
        new Color(153, 255, 51), new Color(51, 255, 153),
        new Color(51, 255, 255), new Color(102, 178, 255),
        new Color(102, 102, 255), new Color(178, 102, 255),
        new Color(255, 102, 255), new Color(255, 102, 178)
    };

    public GateLayer() {

    }

    public ArrayList getGates() {
        return gates;
    }

    public JXLayer createLayer(ChartPanel chart, ArrayList<PolygonGate> ag, String x, String y) {
        gates = ag;
        xAxis = x;
        yAxis = y;
        this.chart = chart;

        // wrap chart component
        this.layer = new JXLayer(chart);

        createPopUpMenu(this.layer);

        // create custom LayerUI
        AbstractLayerUI layerUI = new AbstractLayerUI() {

            @Override
            public void paintLayer(Graphics2D g2, JXLayer l) {

                g2.setStroke(new BasicStroke(1));
                super.paintLayer(g2, l);

                PolygonGate gp;
                //for gate drawing
                Point p;
                Point next;

                Point2D pPrevious = new Point();
                Point2D pCurrent = new Point();
                Point2D p0 = new Point();

                double x, y;

                x = 0.0;
                y = 0.0;

                //draw existing gates
                ListIterator<PolygonGate> itr = gates.listIterator();
                while (itr.hasNext()) {
                    gp = itr.next();
                    if (((PolygonGate) gp).getXAxis().equals(xAxis)
                            && ((PolygonGate) gp).getYAxis().equals(yAxis)) {
                        //if a selected gate is found change paint
                        rescaleGates(chart);
                        Stroke thick = new BasicStroke(2);
                        Stroke thin = new BasicStroke(1);
                        Stroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
                        if (gp.getHovering()) {

                            Path2D polygon = gp.getPath2D();
                            PathIterator pi = polygon.getPathIterator(null);
                            g2.setPaint(gp.getColor());
                            g2.setStroke(dashed);
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);

                            double coords[] = new double[6];

                            ArrayList<Point2D> topaint = new ArrayList<Point2D>();

                            while (!pi.isDone()) {
                                if (pi.currentSegment(coords) == PathIterator.SEG_LINETO) {
                                    topaint.add(new Point2D.Double(coords[0], coords[1]));
                                }
                                pi.next();
                            }

                            ListIterator<Point2D> pli = topaint.listIterator();

                            while (pli.hasNext()) {
                                if (pli.nextIndex() == 0) {
                                    p0 = pli.next();

                                    //g2.fill(new Ellipse2D.Double(p0.getX() - 4, p0.getY() - 4, 8, 8));
                                    pPrevious = p0;
                                } else {
                                    pCurrent = pli.next();
                                    //g2.fill(new Ellipse2D.Double(pCurrent.getX() - 4, pCurrent.getY() - 4, 8, 8));
                                    g2.draw(new Line2D.Double(pPrevious, pCurrent));
                                    pPrevious = pCurrent;
                                }
                            }
                            g2.draw(new Line2D.Double(pCurrent, p0));
                        } else if (gp.getSelected()) {
                            g2.setPaint(gp.getColor());
                            g2.setStroke(thick);
                            g2.draw(gp.getGateAsShape());
                        } else {
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setPaint(gp.getColor());
                            g2.setStroke(thin);
                            g2.draw(gp.getGateAsShape());
                        }
                    }
                }

                if (msPolygon) {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setPaint(Color.blue);
                    for (int j = 0; j < points.size() - 1; j++) {
                        p = points.get(j);
                        next = points.get(j + 1);
                        g2.draw(new Line2D.Double(p, next));
                    }
                    g2.setPaint(Color.red);
                    for (int j = 0; j < points.size(); j++) {
                        p = points.get(j);
                        g2.fill(new Ellipse2D.Double(p.x - 4, p.y - 4, 8, 8));
                    }
                    if (msFinal) {
                        g2.draw(new Line2D.Double(points.get(points.size()), points.get(0)));
                    }
                }

                if (msRectangle && points.size() > 0) {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setPaint(Color.blue);
                    for (int j = 0; j < points.size() - 1; j++) {
                        p = points.get(j);
                        next = points.get(j + 1);
                        g2.draw(new Line2D.Double(p, next));
                    }
                    g2.draw(new Line2D.Double(points.get(points.size() - 1), points.get(0)));
                    g2.setPaint(Color.red);
                    for (int j = 0; j < points.size(); j++) {
                        p = points.get(j);
                        g2.fill(new Ellipse2D.Double(p.x - 4, p.y - 4, 8, 8));
                    }
                    if (msFinal) {
                        g2.draw(new Line2D.Double(points.get(points.size() - 1), points.get(0)));
                    }
                }

                if (msQuadrant && points.size() > 0) {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setPaint(Color.blue);
                    g2.setStroke(new BasicStroke(3));
                    for (int j = 0; j < points.size() - 1; j++) {
                        p = points.get(j);
                        next = points.get(j + 1);
                        g2.draw(new Line2D.Double(p, next));
                    }
                    g2.draw(new Line2D.Double(points.get(points.size() - 1), points.get(0)));
                    g2.setPaint(Color.red);
                    for (int j = 0; j < points.size(); j++) {
                        p = points.get(j);
                        //g2.fill(new Ellipse2D.Double(p.x - 4, p.y - 4, 8, 8));
                    }
                    if (msFinal) {
                        g2.draw(new Line2D.Double(points.get(points.size() - 1), points.get(0)));
                    }
                }

                // Draw gallery view cell highlight
                if (highlightedCell != null) {
                    drawHighlightedCell(g2);
                }
            }

            @Override
            public void processMouseMotionEvent(MouseEvent e, JXLayer l) {

                if (e.getID() == MouseEvent.MOUSE_MOVED) {
                    Point p = e.getPoint();
                    Path2D testp;
                    Ellipse2D test;
                    PolygonGate g;
                    ListIterator<PolygonGate> itr = gates.listIterator();
                    if (gates.size() > 0) {
                        while (itr.hasNext()) {
                            g = itr.next();
                            testp = g.getPath2D();
                            if (testp.contains(p)) {

                                g.setHovering(true);
                            } else {
                                g.setHovering(false);
                            }
                        }
                    }
                }
                if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
                    if (msRectangle && points.size() > 0) {
                        Point p = points.get(0);
                        points.clear();
                        points.add(p);
                        points.add(new Point(e.getX(), (int) p.getY()));
                        points.add(new Point(e.getX(), e.getY()));
                        points.add(new Point((int) p.getX(), e.getY()));
                    }
                    if (msQuadrant && points.size() > 0) {
                        points.clear();
                        points.add(new Point(e.getX(), e.getY()));
                        points.add(new Point(e.getX(), 32));
                        points.add(new Point(e.getX(), e.getY()));
                        points.add(new Point(42, e.getY()));
                        points.add(new Point(e.getX(), e.getY()));
                        points.add(new Point(e.getX(), 460));
                        points.add(new Point(e.getX(), e.getY()));
                        points.add(new Point(480, e.getY()));

                    }
                }

            }

            @Override
            protected void processMouseEvent(MouseEvent e, JXLayer l) {

                if (msPolygon || msRectangle || msQuadrant) {
                    if (msPolygon) {
                        if (e.getClickCount() == 2) {
                            if (!(points.isEmpty())) {
                                try {
                                    drawMicroSelection(e);
                                    makePolygonGate();

                                } catch (Throwable ex) {
                                    Logger.getLogger(GateLayer.class.getName()).
                                            log(Level.SEVERE, null, ex);
                                }

                            }
                            e.consume();
                            //close polygon add to arraylist
                        } else if (e.getID() == MouseEvent.MOUSE_CLICKED) {
                            drawMicroSelection(e);
                        }
                    }
                    if (msRectangle) {
                        if (e.getClickCount() == 1) {
                            if (points.isEmpty()) {
                                points.add(new Point((int) e.getX(), (int) e.getY()));
                            } else {
                                try {
                                    drawMicroRectangleSelection(e);
                                    makeRectangleGate();
                                    //add reset explorer interface here.
                                } catch (Throwable ex) {
                                    Logger.getLogger(GateLayer.class.getName()).
                                            log(Level.SEVERE, null, ex);
                                }
                            }
                            e.consume();
                        }
                    }
//                    if (msQuadrant) {
//                        if (e.getClickCount() == 1) {
//                            if (points.isEmpty()) {
//                                points.clear();
//                                points.add(new Point((int) e.getX(), (int) e.getY()));
//                            } else {
//                                try {
//                                    drawMicroRectangleSelection(e);
//                                    makeQuadrantGate();
//                                    //add reset explorer interface here.
//                                } catch (Throwable ex) {
//                                    Logger.getLogger(GateLayer.class.getName()).log(Level.SEVERE, null, ex);
//                                }
//                            }
//                            e.consume();
//
//                        }
//                    }

                } else {

                    if (gateInClipboard) {
                        ((JMenuItem) menu.getComponent(5)).setEnabled(true);
                    } else if (!gateInClipboard) {
                        ((JMenuItem) menu.getComponent(5)).setEnabled(false);
                    }

                    if (SwingUtilities.isRightMouseButton(e) && !checkForGate(e, gates)) {
                        ((JMenuItem) menu.getComponent(4)).setEnabled(false);
                        ((JMenuItem) menu.getComponent(6)).setEnabled(false);

                        menu.show(e.getComponent(),
                                e.getX(), e.getY());
                        e.consume();

                    } else if (SwingUtilities.isRightMouseButton(e) && checkForGate(e, gates)) {

                        ((JMenuItem) menu.getComponent(4)).setEnabled(true);
                        ((JMenuItem) menu.getComponent(6)).setEnabled(true);

                        menu.show(e.getComponent(),
                                e.getX(), e.getY());
                        e.consume();
                    } else if (e.getClickCount() == 1) {
                        checkForGates(e, gates);

                        e.consume();
                    } else {
                        e.consume();
                    };
                    e.consume();
                }
                e.consume();
            }

        };

        layer.setUI(layerUI);
//        System.gc();
        return layer;
    }

    public void addToGateSelection(MouseEvent e) {

    }

    public void drawMicroSelection(MouseEvent e) {
        int size = 30;
        int PROXIMAL = 5;
        Point p = e.getPoint();

        if (points.size() == size - 1) {
            if (points.get(0).distance(p) <= PROXIMAL) {
                points.add((Point) points.get(0).clone());
            } else {
                int retVal = JOptionPane.showConfirmDialog(null,
                        "Do you want to keep this?", "Confirm",
                        JOptionPane.YES_NO_OPTION);
                if (retVal == JOptionPane.YES_OPTION) {
                    points.add((Point) points.get(0).clone());

                } else {
                    points.clear();
                }
            }
        } else if (points.size() < size) {
            points.add(p);
        }

    }

    public void drawMicroRectangleSelection(MouseEvent e) {

    }

    public void drawMicroQuandrantSelection(MouseEvent e) {

    }

    public void cancelSelection() {
        this.msActive = false;
        this.msPolygon = false;
        this.msRectangle = false;
        this.msQuadrant = false;
        this.points.clear();
    }

    public void makePolygonGate() throws Throwable {
        this.msPolygon = false;
        points.remove(points.size() - 1);
        notifyPolygonSelectionListeners(points);
        this.points.clear();
        this.finalize();
    }

    public void makeRectangleGate() throws Throwable {
        this.msRectangle = false;
        notifyPolygonSelectionListeners(points);
        this.points.clear();
        this.finalize();
    }

    public void makeQuadrantGate() throws Throwable {
        this.msQuadrant = false;

        ArrayList<PolygonGate> multipleGates = new ArrayList<>();

        ArrayList<Point2D.Double> Q1 = new ArrayList<>();
        ArrayList<Point2D.Double> Q2 = new ArrayList<>();
        ArrayList<Point2D.Double> Q3 = new ArrayList<>();
        ArrayList<Point2D.Double> Q4 = new ArrayList<>();

        Q1.add(new Point2D.Double(42, 30));
        Q1.add(new Point2D.Double(points.get(0).x, 30));
        Q1.add(new Point2D.Double(points.get(0).x, points.get(0).y));
        Q1.add(new Point2D.Double(42, points.get(0).y));

        Q2.add(new Point2D.Double(points.get(0).x, 30));
        Q2.add(new Point2D.Double(490, 30));
        Q2.add(new Point2D.Double(490, points.get(0).y));
        Q2.add(new Point2D.Double(points.get(0).x, points.get(0).y));

        Q3.add(new Point2D.Double(points.get(0).x, points.get(0).y));
        Q3.add(new Point2D.Double(490, points.get(0).y));
        Q3.add(new Point2D.Double(490, 465));
        Q3.add(new Point2D.Double(points.get(0).x, 465));

        Q4.add(new Point2D.Double(42, points.get(0).y));
        Q4.add(new Point2D.Double(points.get(0).x, points.get(0).y));
        Q4.add(new Point2D.Double(points.get(0).x, 465));
        Q4.add(new Point2D.Double(42, 465));

//        notifyPolygonSelectionListeners(Q1);
//        this.finalize();
//        notifyPolygonSelectionListeners(Q2);
//        this.finalize();
//        notifyPolygonSelectionListeners(Q3);
//        this.finalize();
//        notifyPolygonSelectionListeners(Q4);
        PolygonGate pg1 = new PolygonGate(Q1);
        multipleGates.add(pg1);
        PolygonGate pg2 = new PolygonGate(Q2);
        multipleGates.add(pg2);
        PolygonGate pg3 = new PolygonGate(Q3);
        multipleGates.add(pg3);
        PolygonGate pg4 = new PolygonGate(Q4);
        multipleGates.add(pg4);

        this.notifyQuadrantSelectionListeners(multipleGates);

        this.points.clear();
        this.finalize();
    }

    public void checkForGates(MouseEvent e, ArrayList<PolygonGate> gates) {

        ListIterator<PolygonGate> itr = gates.listIterator();
        PolygonGate gate;
        Point p = new Point(e.getX(), e.getY());

        boolean highlight = false;

        while (itr.hasNext()) {

            gate = itr.next();
            if (!(e.getModifiersEx() == MouseEvent.SHIFT_DOWN_MASK)) {
                gate.setSelected(false);
            }
            if (gate.getPath2D().contains(p) && (gate.getXAxis().equals(xAxis)
                    && gate.getYAxis().equals(yAxis))) {
                gate.setSelected(true);
                highlight = true;
            }
        }
        if (highlight) {
            this.notifyImageHighLightSelectionListeners(gates);
        }
        e.consume();
    }

    public boolean checkForGate(MouseEvent e, ArrayList<PolygonGate> gates) {

        ListIterator<PolygonGate> itr = gates.listIterator();
        PolygonGate gate;
        Point p = new Point(e.getX(), e.getY());
        while (itr.hasNext()) {
            gate = itr.next();
            if (gate.getPath2D().contains(p) && (((PolygonGate) gate).getXAxis().equals(xAxis)
                    && ((PolygonGate) gate).getYAxis().equals(yAxis))) {
                selectedGate = gate;
                //System.out.println("PROFILING: GateLayer CheckForGate.");
                e.consume();
                return true;
            }
        }
        e.consume();
        return false;
    }

    private ArrayList makeHighLightSelection() {
        ArrayList<PolygonGate> result = new ArrayList<PolygonGate>();
        ListIterator<PolygonGate> itr = gates.listIterator();
        if (gates.size() > 0) {
            PolygonGate gate = itr.next();
            if (gate.getSelected()) {
                result.add(gate);
            };
        }
        return result;
    }

    public void addPolygonSelectionListener(PolygonSelectionListener listener) {
        polygonlisteners.add(listener);
    }

    public void notifyPolygonSelectionListeners(ArrayList points) {
        for (PolygonSelectionListener listener : polygonlisteners) {
            listener.polygonGate(points);
        }
    }

    public void addQuadrantSelectionListener(QuadrantSelectionListener listener) {
        quadrantlisteners.add(listener);
    }

    public void notifyQuadrantSelectionListeners(ArrayList<PolygonGate> g) {
        for (QuadrantSelectionListener listener : quadrantlisteners) {
            listener.addQuadrantGate(g);
        }
    }

    public void addDeleteGateListener(DeleteGateListener listener) {
        //System.out.println("Adding Deleting gate .");
        deletegatelisteners.add(listener);
    }

    public void notifyDeleteGateListeners() {
        for (DeleteGateListener listener : deletegatelisteners) {
            listener.onDeleteGate(gates);
        }
    }

    public void addPasteGateListener(AddGateListener listener) {
        addgatelisteners.add(listener);
    }

    public void notifyPasteGateListeners(PolygonGate gate) {
        for (AddGateListener listener : addgatelisteners) {
            listener.onPasteGate(gate);
        }
    }
    
    //DatasetUtilitiesListeners
    
    
    public void addDatasetUtilitiesListener(DatasetUtilitiesListener listener) {
        DatasetUtilitiesListeners.add(listener);
    }

    public void notifyDatasetUtilitiesListeners() {
        for (DatasetUtilitiesListener listener : DatasetUtilitiesListeners) {
            listener.dataSetUtilities();
        }
    }
    

    public void addGateColorListener(GateColorListener listener) {
        gatecolorlisteners.add(listener);
    }

    public void notifyGateColorListeners() {
        for (GateColorListener listener : gatecolorlisteners) {
            listener.onGateColor(gates);
        }
    }

    public void addImageHighLightSelectionListener(ImageHighlightSelectionListener listener) {
        highlightlisteners.add(listener);
    }

    public void notifyImageHighLightSelectionListeners(ArrayList<PolygonGate> gates) {
        for (ImageHighlightSelectionListener listener : highlightlisteners) {
            XYExplorationPanel.testCounter++;
            listener.imageHighLightSelection(gates);
        }
    }

    /**
     * Set the data provider for gallery view.
     * @param provider The GalleryViewDataProvider to use
     */
    public void setGalleryViewDataProvider(GalleryViewDataProvider provider) {
        this.galleryViewDataProvider = provider;
    }

    private void createPopUpMenu(JXLayer layer) {

        this.menu = new JPopupMenu();
        //JMenuItem menuItem = new JMenuItem("Color...");
//        menuItem.addActionListener(this);
//        menu.add(menuItem);

        JMenuItem menuItem = new JMenuItem("");

        menuItem = new JMenuItem("Add gate...");
        menuItem.addActionListener(this);
        menu.add(menuItem);

      
        menu.add(new JSeparator());

        JMenu jm = new JMenu("Color...");

        for (int i = 0; i < colors.length; i++) {
            menuItem = new JMenuItem(colors[i]);
            menuItem.addActionListener(this);
            jm.add(menuItem);
        }

        menu.add(jm);

//        menuItem = new JMenuItem("Line...");
//        menuItem.addActionListener(this);
//        menu.add(menuItem);
        menu.add(new JSeparator());

        menuItem = new JMenuItem("Copy");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Paste");
        menuItem.setEnabled(gateInClipboard);

        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Delete");

        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.add(new JSeparator());

        menuItem = new JMenuItem("Delete All");

        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.add(new JSeparator());

        menuItem = new JMenuItem("Make Ground Truth...");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.add(new JSeparator());

        menuItem = new JMenuItem("Classify by Image...");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Classify by Gate...");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Classify by Gate Math...");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menu.add(new JSeparator());

        menuItem = new JMenuItem("Import ROI...");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.add(new JSeparator());

        menuItem = new JMenuItem("Subgate Selection...");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Gallery View...");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Dataset utilities...");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.add(new JSeparator());

        menuItem = new JMenuItem("Add Distance Map...");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Add Density Map...");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Calculate K...");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
//        menuItem = new JMenuItem("Get Moran's I...");
//        menuItem.addActionListener(this);
//        menu.add(menuItem);

        menu.add(new JSeparator());
        
        menuItem = new JMenuItem("Randomize classes...");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Randomize positions...");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menu.add(new JSeparator());

        menuItem = new JMenuItem("Generate Neighborhoods...");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        //Add listener to the text area so the popup menu can come up.
//        MouseListener popupListener = new PopupListener(menu);
//        layer.addMouseListener(popupListener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //System.out.println(e.getActionCommand());

        if (e.getActionCommand().equals("Delete")) {

            ListIterator<PolygonGate> gt = gates.listIterator();
            while (gt.hasNext()) {
                PolygonGate g = gt.next();
                if (g.getHovering() && (((PolygonGate) g).getXAxis().equals(xAxis)
                        && ((PolygonGate) g).getYAxis().equals(yAxis))) {
                    gt.remove();
                }
            }
            notifyDeleteGateListeners();
        } else if (e.getActionCommand().equals("Copy")) {
            clipboardGate = selectedGate;
            gateInClipboard = true;
            menu.getComponent(4).setEnabled(true);
        } else if (e.getActionCommand().equals("Import ROI...")) {

            importROI();

        } else if (e.getActionCommand().equals("Paste")) {
            try {
                PolygonGate newgate = new PolygonGate(clipboardGate.createInPanelSpace(chart));
                newgate.setXAxis(xAxis);
                newgate.setYAxis(yAxis);
                newgate.createInChartSpace(chart);
                newgate.createPath2D();
                //gates.add(newgate);
                notifyPasteGateListeners(newgate);
            } catch (NullPointerException n) {
            }
        } else if (e.getActionCommand().equals("Delete All")) {
            ListIterator<PolygonGate> gt = gates.listIterator();
            while (gt.hasNext()) {
                PolygonGate g = gt.next();
                if ((((PolygonGate) g).getXAxis().equals(xAxis)
                        && ((PolygonGate) g).getYAxis().equals(yAxis))) {
                    gt.remove();
                }
            }
            notifyDeleteGateListeners();
        } else if (e.getActionCommand().equals("Make Ground Truth...")) {
            //Used to export individual images of each segmented nuclei

            ListIterator<PolygonGate> gt = gates.listIterator();
            Path2D path = null;
            while (gt.hasNext()) {
                PolygonGate g = gt.next();
                if (g.getSelected() && (((PolygonGate) g).getXAxis().equals(xAxis)
                        && ((PolygonGate) g).getYAxis().equals(yAxis))) {
                    path = g.createPath2DInChartSpace();
                }
            }
            if (path != null) {
                notifyImageListeners(path);
            }
        } else if (e.getActionCommand().equals("Classify by Image...")) {

            notifyClassificationListener();

        } else if (e.getActionCommand().equals("Subgate Selection...")) {
            //Used to subgate to a new MicroExplorer

            ListIterator<PolygonGate> gt = gates.listIterator();
            Path2D path = null;
            while (gt.hasNext()) {
                PolygonGate g = gt.next();
                if (g.getSelected() && (((PolygonGate) g).getXAxis().equals(xAxis)
                        && ((PolygonGate) g).getYAxis().equals(yAxis))) {
                    path = g.createPath2DInChartSpace();
                }
            }

            notifySubgateListeners();

        } else if (e.getActionCommand().equals("Gallery View...")) {
            // Open gallery view for selected gate
            if (selectedGate == null) {
                JOptionPane.showMessageDialog(
                        chart,
                        "Please select a gate first.",
                        "No Gate Selected",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (galleryViewDataProvider != null) {
                galleryViewDataProvider.openGalleryView(selectedGate);
            } else {
                System.err.println("GalleryViewDataProvider not set in GateLayer!");
            }

        } else if (e.getActionCommand().equals("Classify by Gate...")) {

            notifyAssignmentListeners("gate");
            
        } else if (e.getActionCommand().equals("Dataset utilities...")) {
            //System.out.println("fired event, daatset, listners: " + DatasetUtilitiesListeners.size());
           notifyDatasetUtilitiesListeners();

        }else if (e.getActionCommand().equals("Randomize classes...")) {
                           new Thread(() -> {
                    try {

                        notifyRandomizationListener("class");

                    } catch (Exception ex) {
                        System.out.println("ERROR: " + ex.getLocalizedMessage());
                    }
                }).start(); 
            

        }else if (e.getActionCommand().equals("Randomize positions...")) {
                           new Thread(() -> {
                    try {

                        notifyRandomizationListener("position");


                    } catch (Exception ex) {
                        System.out.println("ERROR: " + ex.getLocalizedMessage());
                    }
                }).start(); 
           
        } else if (e.getActionCommand().equals("Classify by Gate Math...")) {

            notifyAssignmentListeners("gatemath");
            
        } else if (e.getActionCommand().equals("Calculate K...")) { 
            
            notifySpatialListeners("Ripley's K");
 
        } else if (e.getActionCommand().equals("Get Moran's I...")) { 
            
            notifySpatialListeners("Moran's I");
            

        } else if (e.getActionCommand().equals("Add Distance Map...")) {
            //Used to subgate to a new MicroExplorer

            ListIterator<PolygonGate> gt = gates.listIterator();
            Path2D path = null;
            while (gt.hasNext()) {
                PolygonGate g = gt.next();
                if (g.getSelected() && (((PolygonGate) g).getXAxis().equals(xAxis)
                        && ((PolygonGate) g).getYAxis().equals(yAxis))) {
                    path = g.createPath2DInChartSpace();
                }
            }
            if (path != null) {
                


                String s = (String) JOptionPane.showInputDialog(
                        null,
                        "Please enter the group name",
                        "Distance Map Group",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        "Distance_");

                new Thread(() -> {
                    try {

                        notifyDistanceMapListeners(s);

                    } catch (Exception ex) {
                        System.out.println("ERROR: " + ex.getLocalizedMessage());
                    }
                }).start();

            }
        } else if (e.getActionCommand().equals("Generate Neighborhoods...")) {
            //Used to subgate to a new MicroExplorer

            ListIterator<PolygonGate> gt = gates.listIterator();
            Path2D path = null;
            while (gt.hasNext()) {
                PolygonGate g = gt.next();
                if (g.getSelected() && (((PolygonGate) g).getXAxis().equals(xAxis)
                        && ((PolygonGate) g).getYAxis().equals(yAxis))) {
                    path = g.createPath2DInChartSpace();
                }
            }
            if (path != null) {

                new Thread(() -> {
                    try {

                        this.notifyNeighborhoodListeners("Neighborhood");

                    } catch (Exception ex) {
                        StackTraceElement[] error = ex.getStackTrace();
                        System.out.println("ERROR: " + ex.getMessage());
                        for (int i = 0; i < error.length; i++) {
                            System.out.println(error[i]);

                        }
                    }
                }).start();

            }
        } else if (e.getActionCommand().equals("Add Density Map...")) {
            //Used to subgate to a new MicroExplorer

            ListIterator<PolygonGate> gt = gates.listIterator();
            Path2D path = null;
            while (gt.hasNext()) {
                PolygonGate g = gt.next();
                if (g.getSelected() && (((PolygonGate) g).getXAxis().equals(xAxis)
                        && ((PolygonGate) g).getYAxis().equals(yAxis))) {
                    path = g.createPath2DInChartSpace();
                }
            }
            if (path != null) {

                String s = (String) JOptionPane.showInputDialog(
                        null,
                        "Please enter the group name",
                        "Distance Map Group",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        "Density_");

                new Thread(() -> {
                    try {

                        notifyDensityMapListeners(s);

                    } catch (Exception ex) {
                        System.out.println("ERROR: " + ex.getLocalizedMessage());
                    }
                }).start();

            }
        } else {

            for (int i = 0; i < colors.length; i++) {
                if (e.getActionCommand().equals(colors[i])) {
                    PolygonGate gp;
                    ListIterator<PolygonGate> itr = gates.listIterator();
                    while (itr.hasNext()) {
                        gp = itr.next();
                        if (gp.getUID() == selectedGate.getUID()) {
                            gp.setSelectedColor(colorsRGB[i]);
                        }
                    }
                }
            }
            notifyGateColorListeners();
        }
    }

    private void importROI() {

        new Thread(() -> {
            try {

                JFileChooser jf = new JFileChooser(_vtea.LASTDIRECTORY);

                FileNameExtensionFilter filter2
                        = new FileNameExtensionFilter("HALO roi file.", ".annotations", "annotations");
                jf.addChoosableFileFilter(filter2);
                jf.setFileFilter(filter2);

                jf.setDialogTitle("Select HALO annotation file");

                JPanel panel1 = (JPanel) jf.getComponent(3);
                JPanel panel2 = (JPanel) panel1.getComponent(3);

                int returnVal = jf.showOpenDialog(menu);
                File file = jf.getSelectedFile();
                
                _vtea.LASTDIRECTORY = file.getPath(); 

                roiHALO importHALO = new roiHALO(file);
                
            } catch (Exception ex) {
                System.out.println("ERROR: " + ex.getLocalizedMessage());
            }
        }).start();
    }


    @Override
    public void itemStateChanged(ItemEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Adds a SaveGatedImagesListener
     *
     * @param listener the listener to add to the ArrayList saveImageListeners
     */
    public void addImagesListener(SaveGatedImagesListener listener) {
        saveImageListeners.add(listener);
    }

    /**
     * Notify the SaveGatedImagesListeners to save the gated nuclei
     *
     * @param path
     */
    private void notifyImageListeners(Path2D path) {
        for (SaveGatedImagesListener listener : saveImageListeners) {
            listener.saveGated(path);
        }
    }

    /**
     *
     *
     * @param listener the listener to add to the ArrayList ManualClassListener
     */
    public void addClassificationListener(ManualClassListener listener) {
        manualClassListeners.add(listener);
        //System.out.println("PROFILING: Manual Classifcation Listener Added: " + listener.getClass());
    }

    /**
     *
     *
     *
     */
    private void notifyClassificationListener() {

        //System.out.println("PROFILING: Notifying Manual Classifcation Listeners Total: " + manualClassListeners.size());
        for (ManualClassListener listener : manualClassListeners) {

            listener.startManualClassListener();
        }
    }

    /**
     *
     *
     * @param listener the listener to add to the ArrayList ManualClassListener
     */
    public void addAssignmentListener(AssignmentListener listener) {
        assignmentListeners.add(listener);
        //System.out.println("PROFILING: Manual Classifcation Listener Added: " + listener.getClass());
    }

    /**
     *
     *
     *
     */
    private void notifyAssignmentListeners(String cmd) {

        //System.out.println("PROFILING: Notifying Manual Classifcation Listeners Total: " + manualClassListeners.size());
        for (AssignmentListener listener : assignmentListeners) {

            listener.assignClassification(cmd);
        }
    }

    /**
     * Adds a SaveGatedImagesListener
     *
     * @param listener the listener to add to the ArrayList saveImageListeners
     */
    public void addSubGateListener(SubGateListener listener) {
        subGateListeners.add(listener);
    }

    /**
     * Notify the SaveGatedImagesListeners to save the gated nuclei
     *
     * @param path
     */
    private void notifySubgateListeners() {
        for (SubGateListener listener : subGateListeners) {
            listener.subGate();
        }
    }

    public void addDistanceMapListener(DistanceMapListener listener) {
        distanceMapListeners.add(listener);
    }

    public void addNeighborhoodListener(NeighborhoodListener listener) {
        neighborhoodListeners.add(listener);
    }

    /**
     * Notify the SaveGatedImagesListeners to save the gated nuclei
     *
     * @param path
     */
    private void notifyDistanceMapListeners(String name) {
        for (DistanceMapListener listener : distanceMapListeners) {
            listener.addDistanceMapFromGate(name);
        }
    }

    /**
     * Notify the SaveGatedImagesListeners to save the gated nuclei
     *
     * @param path
     */
    private void notifyNeighborhoodListeners(String name) {
        for (NeighborhoodListener listener : neighborhoodListeners) {
            listener.addNeighborhoodFromGate("neighborhood");
        }
    }

    public void addDensityMapListener(DensityMapListener listener) {
        densityMapListeners.add(listener);
    }
    
        /**
     * Notify the SaveGatedImagesListeners to save the gated nuclei
     *
     * @param path
     */
    private void notifySpatialListeners(String name) {
        for (SpatialListener listener : spatialListeners) {
            listener.addSpatialFromGate(name);
        }
    }
    
    public void addSpatialListener(SpatialListener listener) {
        spatialListeners.add(listener);
    }


 

    /**
     * Notify the SaveGatedImagesListeners to save the gated nuclei
     *
     * @param path
     */
    private void notifyDensityMapListeners(String name) {
        for (DensityMapListener listener : densityMapListeners) {
            listener.addDensityMapFromGate(name);

        }
    }
    
    public void addRandomizationListener(RandomizationListener listener) {
        randomizationListeners.add(listener);
    }


    /**
     * Notify the SaveGatedImagesListeners to save the gated nuclei
     *
     * @param path
     */
    private void notifyRandomizationListener(String type) {
        for (RandomizationListener listener : randomizationListeners) {
            listener.addRandomization(type);
        }
    }

    public void rescaleGates(ChartPanel newchart) {
        //ArrayList<PolygonGate> result = new ArrayList<>();
        ListIterator<PolygonGate> gt = gates.listIterator();
        while (gt.hasNext()) {
            PolygonGate g = gt.next();
            if ((g.getXAxis().equals(xAxis)
                    && g.getYAxis().equals(yAxis))) {
                g.updatePanelPositions(newchart);
            }
        }
    }

    public void updateGateName(String name, int row) {
        PolygonGate pg = gates.get(row);
        pg.setName(name);
    }

    public void updateGateColor(Color color, int row) {
        PolygonGate pg = gates.get(row);
        pg.setSelectedColor(color);
    }

    public void setGateOverlay(boolean b, int row) {
        //System.out.println("PROFILING:  row: " + row + ", " + b);
        PolygonGate pg = gates.get(row);
        pg.setSelected(b);
    }

    public void importGate(PolygonGate pg) {
        gates.add(pg);
        //notifyPasteGateListeners();

    }

    // ========== Gallery View Cell Highlighting ==========

    /**
     * Highlight a specific cell on the image overlay with a red dashed circle.
     * @param cell The MicroObject to highlight
     */
    public void highlightCell(MicroObject cell) {
        this.highlightedCell = cell;
        if (layer != null) {
            layer.repaint();
        }
    }

    /**
     * Clear the highlighted cell.
     */
    public void clearCellHighlight() {
        this.highlightedCell = null;
        if (layer != null) {
            layer.repaint();
        }
    }

    /**
     * Draw the highlighted cell as a red dashed circle on the chart.
     * @param g2 Graphics context
     */
    private void drawHighlightedCell(Graphics2D g2) {
        if (highlightedCell == null || chart == null) {
            return;
        }

        try {
            // Get cell coordinates in data space
            double xValue = highlightedCell.getChannelTagFloat(xAxis);
            double yValue = highlightedCell.getChannelTagFloat(yAxis);

            // Convert to screen coordinates
            Rectangle2D dataArea = chart.getChartRenderingInfo()
                    .getPlotInfo()
                    .getDataArea();

            XYPlot plot = (XYPlot) chart.getChart().getPlot();
            ValueAxis domainAxis = plot.getDomainAxis();
            ValueAxis rangeAxis = plot.getRangeAxis();

            double screenX = domainAxis.valueToJava2D(
                    xValue, dataArea, plot.getDomainAxisEdge());
            double screenY = rangeAxis.valueToJava2D(
                    yValue, dataArea, plot.getRangeAxisEdge());

            // Draw circle outline
            g2.setColor(HIGHLIGHT_COLOR);
            g2.setStroke(HIGHLIGHT_STROKE);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int radius = 10;  // Pixels
            g2.drawOval(
                    (int) (screenX - radius),
                    (int) (screenY - radius),
                    radius * 2,
                    radius * 2
            );

        } catch (Exception e) {
            System.err.println("Error drawing highlighted cell: " + e.getMessage());
            // Don't print stack trace - this may fail if axes don't match, which is expected
        }
    }
}
