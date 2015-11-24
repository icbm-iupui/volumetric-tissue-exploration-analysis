/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vteaexploration.plotgatetools.gates;

import vteaexploration.plotgatetools.listeners.ImageHighlightSelectionListener;
import vteaexploration.plotgatetools.listeners.PolygonSelectionListener;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.InputMethodEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
//import javax.swing.JLayer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import javax.swing.JLayeredPane;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;
import vteaexploration.plotgatetools.listeners.QuadrantSelectionListener;
//import javax.swing.plaf.LayerUI;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author vinfrais
 */
public class GateLayer {

    public transient boolean msActive;
    public transient boolean msQuadrant;//
    public transient boolean msSelected;
    public transient boolean msFinal;
    public transient boolean msSelecting;
    public transient boolean msOptions;
    public transient boolean kyShift;

    private int mX, mY;
    private ArrayList<Point> points = new ArrayList<Point>();
    private ArrayList<PolygonSelectionListener> polygonlisteners = new ArrayList<PolygonSelectionListener>();
    private ArrayList<ImageHighlightSelectionListener> highlightlisteners = new ArrayList<ImageHighlightSelectionListener>();
    private ArrayList<QuadrantSelectionListener> quadrantlisteners = new ArrayList<QuadrantSelectionListener>();
    private ArrayList<Gate> gates = new ArrayList<Gate>();

    public GateLayer()  {
    }

    public ArrayList getGates() {
        return gates;
    }

    public JXLayer createLayer(JPanel chart, ArrayList<Gate> ag) {
        gates = ag;

        // wrap chart component
        JXLayer layer = new JXLayer(chart);

        // create custom LayerUI
        AbstractLayerUI layerUI = new AbstractLayerUI() {

//            @Override
//            public void setOpaque(boolean bln) {
//                super.setOpaque(bln); //To change body of generated methods, choose Tools | Templates.
//            }
                    
            @Override
            public void paintLayer(Graphics2D g2, JXLayer l) {
                // paint the layer as is
//                Graphics2D g2 = (Graphics2D) g.create();
                super.paintLayer(g2, l);

                Gate gp;
                //for gate drawing
                Point p = new Point();
                Point next = new Point();

                Point2D pPrevious = new Point();
                Point2D pCurrent = new Point();
                Point2D p0 = new Point();

                double x, y;

                x = 0.0;
                y = 0.0;

                //draw existing gates
                ListIterator<Gate> itr = gates.listIterator();
                while (itr.hasNext()) {
                    gp = itr.next();

                    //if a selected gate is found change paint
                    if (gp.getHovering()) {
                        Path2D polygon = gp.getPath2D();
                        PathIterator pi = polygon.getPathIterator(null);

                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
                        if (gp.getSelected()) {
                            g2.setPaint(Color.blue);
                        } else {
                            g2.setPaint(gp.getColor());
                        }

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

                                g2.fill(new Ellipse2D.Double(p0.getX() - 4, p0.getY() - 4, 8, 8));
                                pPrevious = p0;
                            } else {
                                pCurrent = pli.next();
                                g2.fill(new Ellipse2D.Double(pCurrent.getX() - 4, pCurrent.getY() - 4, 8, 8));
                                g2.draw(new Line2D.Double(pPrevious, pCurrent));
                                pPrevious = pCurrent;
                            }
                        }
                        g2.draw(new Line2D.Double(pCurrent, p0));

                    } else {
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setPaint(Color.cyan);
                        g2.draw(gp.getGateAsShape());
                   }
                }

                if (msActive) {
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
                if (msQuadrant) {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                }
            }

            @Override
            public void processMouseMotionEvent(MouseEvent e, JXLayer l) {

                if (e.getID() == MouseEvent.MOUSE_MOVED) {
                 Point p = e.getPoint();
                    Path2D testp;
                    Ellipse2D test;
                    Gate g;
                    //System.out.println("Gate Layer, Mouse moved: " + e.getPoint());
                    ListIterator<Gate> itr = gates.listIterator();
                    if (gates.size() > 0) {
                        while (itr.hasNext()) {
                            g = itr.next();
                            testp = g.getPath2D();
                            //test = new Ellipse2D.Double(testp.x-4, testp.y-4, 8, 8);
                            if (testp.contains(p)) {
                                //System.out.println("Gate Layer, Mouse over gate: " + testp);
                                g.setHovering(true);
                            } else {
                                g.setHovering(false);
                                g.setSelected(false);
                            }
                        }
                    }
                }
                if (e.getID() == MouseEvent.MOUSE_DRAGGED) {

                }
            }
            
            @Override
            protected void processMouseEvent(MouseEvent e, JXLayer l) {
                int onmask = e.SHIFT_DOWN_MASK;
                int offmask = e.CTRL_DOWN_MASK;

                //System.out.println("Gate Layer, Mouse clicked: " + e.getPoint());
                //System.out.println("Gate Layer, Selection Active: " + msActive);
                if (msActive) {
                    if (e.getClickCount() == 2) {
                        if (!(points.isEmpty())) {
                            try {
                                drawMicroSelection(e);
                                makePolygonGate();
                                //add reset explorer interface here.
                            } catch (Throwable ex) {
                                Logger.getLogger(GateLayer.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            //System.out.println("...making gate for display.");
                        }
                        e.consume();
                        //close polygon add to arraylist
                    } else if (e.getID() == MouseEvent.MOUSE_CLICKED) {
                        drawMicroSelection(e);
                    }
                
                } else if((e.getModifiersEx() & (onmask)) == onmask) {
                    
                    //System.out.println("Gate Layer, SHIFT key down: ");
                    
                    checkForGates(e, gates);
                   addToGateSelection(e);
                } else if(e.getClickCount() == 1) {
                    
                    //clearGateSelection();
                    checkForGates(e, gates); 
                    //e.consume();
                } else {
                    checkForGates(e, gates);
                    msSelecting = true;
                    if (e.getClickCount() == 2) {//msActive = true;
                    }
                    //select underlying gate, wait for drag event
                };
            }
        };
        
        //layerUI.add(chart);
        //chart.add(layerUI);
        
        layer.setUI(layerUI);
        return layer;
    }
    
    public void addToGateSelection(MouseEvent e) {
        
//        
//        while (itr.hasNext()) {
//            gate = itr.next();
//            if (gate.getPath2D().contains(p)) {
//                gate.setSelected(true);
//                System.out.println("Gate present gui space: " + gate.getGateAsPoints());
//                System.out.println("Gate present chart space: " + gate.getGateAsPointsInChart());
//                this.notifyImageHighLightSelectionListeners(gate);
//            }
//        }
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
    
    public void cancelSelection() {  
        this.msActive = false;
        this.points.clear(); 
    }

    public void makePolygonGate() throws Throwable {
        this.msActive = false;
        notifyPolygonSelectionListeners(points);
        this.points.clear();
        this.finalize();
    }

    public void checkForGates(MouseEvent e, ArrayList<Gate> gates) {

        ListIterator<Gate> itr = gates.listIterator();
        Gate gate;
        Point p = new Point(e.getX(), e.getY());
        while (itr.hasNext()) {
            gate = itr.next();
            if (gate.getPath2D().contains(p)) {
                gate.setSelected(true);
                //System.out.println("Gate present gui space: " + gate.getGateAsPoints());
                System.out.println("Gate present chart space: " + gate.getGateAsPointsInChart()); 
                this.notifyImageHighLightSelectionListeners(gates);
            }
        }
    }

    private ArrayList makeHighLightSelection() {
        ArrayList<Gate> result = new ArrayList<Gate>();
        ListIterator<Gate> itr = gates.listIterator();
        if (gates.size() > 0) {
            Gate gate = itr.next();
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

    public void addImageHighLightSelectionListener(ImageHighlightSelectionListener listener) {
        highlightlisteners.add(listener);
    }

    public void notifyImageHighLightSelectionListeners(ArrayList gates) {
        for (ImageHighlightSelectionListener listener : highlightlisteners) {
            listener.imageHighLightSelection(gates);
        }
    }

//    @Override
//    public void keyTyped(KeyEvent ke) {
//        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public void keyPressed(KeyEvent ke) {
//       if(ke.isShiftDown()){this.kyShift = true;}
//    }
//
//    @Override
//    public void keyReleased(KeyEvent ke) {
//        this.kyShift = false;
//    }

}
