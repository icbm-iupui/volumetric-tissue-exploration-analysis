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
package vteaexploration.plotgatetools.gates;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
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
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;
import vteaexploration.plotgatetools.listeners.AddGateListener;
import vteaexploration.plotgatetools.listeners.ImageHighlightSelectionListener;
import vteaexploration.plotgatetools.listeners.PolygonSelectionListener;
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
public class GateLayer implements ActionListener, ItemListener {

    public transient boolean msActive;
    public transient boolean msPolygon;
    public transient boolean msQuadrant;
    public transient boolean msRectangle;
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
    private ArrayList<AddGateListener> addgatelisteners = new ArrayList<AddGateListener>();
    private ArrayList<Gate> gates = new ArrayList<Gate>();

    private JPopupMenu menu = new JPopupMenu();
    
    private Gate selectedGate;
    
    
    public static Gate clipboardGate;
    public static boolean gateInClipboard = false;

    public GateLayer() {

    }

    public ArrayList getGates() {
        return gates;
    }

    public JXLayer createLayer(JPanel chart, ArrayList<Gate> ag) {
        gates = ag;

        // wrap chart component
        JXLayer layer = new JXLayer(chart);

        creatPopUpMenu(layer);

        // create custom LayerUI
        AbstractLayerUI layerUI = new AbstractLayerUI() {

            @Override
            public void paintLayer(Graphics2D g2, JXLayer l) {

                g2.setStroke(new BasicStroke(1));
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
                        g2.setPaint(Color.red);
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
                    }else if (gp.getSelected()) {
                            g2.setPaint(Color.blue);
                            g2.draw(gp.getGateAsShape());
                    } else {
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setPaint(Color.cyan);
                        g2.draw(gp.getGateAsShape());
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
            }

            @Override
            public void processMouseMotionEvent(MouseEvent e, JXLayer l) {

                if (e.getID() == MouseEvent.MOUSE_MOVED) {
                    Point p = e.getPoint();
                    Path2D testp;
                    Ellipse2D test;
                    Gate g;
                    ListIterator<Gate> itr = gates.listIterator();
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
                        points.add(new Point(e.getX(), 31));
                        points.add(new Point(e.getX(), e.getY()));
                        points.add(new Point(46, e.getY()));
                        points.add(new Point(e.getX(), e.getY()));
                        points.add(new Point(e.getX(), 461));
                        points.add(new Point(e.getX(), e.getY()));
                        points.add(new Point(460, e.getY()));

                    }
                }
            }

            @Override
            protected void processMouseEvent(MouseEvent e, JXLayer l) {
                int onmask = e.SHIFT_DOWN_MASK;
                int offmask = e.CTRL_DOWN_MASK;


                if (msPolygon || msRectangle || msQuadrant) {
                    if (msPolygon) {
                        if (e.getClickCount() == 2) {
                            if (!(points.isEmpty())) {
                                try {
                                    drawMicroSelection(e);
                                    makePolygonGate();
                                   
                                } catch (Throwable ex) {
                                    Logger.getLogger(GateLayer.class.getName()).log(Level.SEVERE, null, ex);
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
                                    Logger.getLogger(GateLayer.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                //System.out.println("...making gate for display.");

                            }
                            e.consume();
                            //close polygon add to arraylist
                        }
                    }
                    if (msQuadrant) {
                        if (e.getClickCount() == 1) {
                            if (points.isEmpty()) {
                                points.clear();
                                points.add(new Point((int) e.getX(), (int) e.getY()));
                            } else {
                                try {
                                    drawMicroRectangleSelection(e);
                                    makeQuadrantGate();
                                    //add reset explorer interface here.
                                } catch (Throwable ex) {
                                    Logger.getLogger(GateLayer.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                //System.out.println("...making gate for display.");

                            }
                            e.consume();
                            //close polygon add to arraylist
                        }
                    }

                } else {

                    //after processing gates, if no gates then 
                    
                    if(gateInClipboard){
                        ((JMenuItem)menu.getComponent(4)).setEnabled(true);
                    } else if (!gateInClipboard){
                        ((JMenuItem)menu.getComponent(4)).setEnabled(false);
                    }
                   
                    if (SwingUtilities.isRightMouseButton(e) && !checkForGate(e, gates)) {
                        ((JMenuItem)menu.getComponent(3)).setEnabled(false);
                        ((JMenuItem)menu.getComponent(5)).setEnabled(false);
                        
                        menu.show(e.getComponent(),
                                e.getX(), e.getY());
                        e.consume();

                    } else if (SwingUtilities.isRightMouseButton(e) && checkForGate(e, gates)) {
                         
                        ((JMenuItem)menu.getComponent(3)).setEnabled(true);
                        ((JMenuItem)menu.getComponent(5)).setEnabled(true);
                       
                        menu.show(e.getComponent(),
                                e.getX(), e.getY());
                        e.consume();
                    } else if (e.getClickCount() == 1) {

                        
                        checkForGates(e, gates);
                        e.consume();
                    } else {
                        e.consume();
                    };
                }
                //e.consume();
            }

        };

        //layerUI.add(chart);
        //chart.add(layerUI);
        layer.setUI(layerUI);
        
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

        ArrayList<Point> Q1 = new ArrayList<Point>();
        ArrayList<Point> Q2 = new ArrayList<Point>();
        ArrayList<Point> Q3 = new ArrayList<Point>();
        ArrayList<Point> Q4 = new ArrayList<Point>();

        Q1.add(new Point(46, 31));
        Q1.add(new Point(points.get(0).x, 31));
        Q1.add(points.get(0));
        Q1.add(new Point(46, points.get(0).y));

        Q2.add(new Point(points.get(0).x, 31));
        Q2.add(new Point(460, 31));
        Q2.add(new Point(460, points.get(0).y));
        Q2.add(points.get(0));

        Q3.add(points.get(0));
        Q3.add(new Point(460, points.get(0).y));
        Q3.add(new Point(460, 461));
        Q3.add(new Point(points.get(0).x, 461));

        Q4.add(new Point(46, points.get(0).y));
        Q4.add(points.get(0));
        Q4.add(new Point(points.get(0).x, 461));
        Q4.add(new Point(46, 461));

        notifyPolygonSelectionListeners(Q1);
        notifyPolygonSelectionListeners(Q2);
        notifyPolygonSelectionListeners(Q3);
        notifyPolygonSelectionListeners(Q4);

        this.points.clear();
        this.finalize();
    }

    public void checkForGates(MouseEvent e, ArrayList<Gate> gates) {

        ListIterator<Gate> itr = gates.listIterator();
        Gate gate;
        Point p = new Point(e.getX(), e.getY());
        while (itr.hasNext()) {
            gate = itr.next();
            if(!(e.getModifiersEx() == MouseEvent.SHIFT_DOWN_MASK)){
               gate.setSelected(false); 
            }          
            if (gate.getPath2D().contains(p)) {
                gate.setSelected(true);
                this.notifyImageHighLightSelectionListeners(gates);
            }
        }
        e.consume();
    }

    public boolean checkForGate(MouseEvent e, ArrayList<Gate> gates) {

        ListIterator<Gate> itr = gates.listIterator();
        Gate gate;
        Point p = new Point(e.getX(), e.getY());
        while (itr.hasNext()) {
            gate = itr.next();
            if (gate.getPath2D().contains(p)) {
                this.selectedGate = gate;
                return true;
            }
        }
        e.consume();
        return false;
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
    
    public void addPasteGateListener(AddGateListener listener) {
        addgatelisteners.add(listener);
    }

    public void notifyPasteGateListeners() {
        for (AddGateListener listener : addgatelisteners) {
            listener.onPasteGate();
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

    private void creatPopUpMenu(JXLayer layer) {
        this.menu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Color...");

        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Line...");

        menuItem.addActionListener(this);
        menu.add(menuItem);
      
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

        //Add listener to the text area so the popup menu can come up.
//        MouseListener popupListener = new PopupListener(menu);
//        layer.addMouseListener(popupListener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand());
        if(e.getActionCommand().equals("Delete")){
            
        ListIterator<Gate> gt = gates.listIterator();
            while(gt.hasNext()){
                Gate g = gt.next();
                if(g.getHovering()){
                    gt.remove();
                }
            }
            
        } else if(e.getActionCommand().equals("Copy")){
            clipboardGate = selectedGate;
            gateInClipboard = true;
            menu.getComponent(4).setEnabled(true);
        } else if(e.getActionCommand().equals("Paste")){
            try{
            gates.add(clipboardGate);     
            } catch (NullPointerException n){}
        } else if(e.getActionCommand().equals("Delete All")){
            gates.clear();      
        }
        this.notifyPasteGateListeners();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   

}
