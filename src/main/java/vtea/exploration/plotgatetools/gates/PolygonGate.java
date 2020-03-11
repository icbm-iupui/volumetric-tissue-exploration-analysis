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

import ij.ImageStack;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.UUID;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleEdge;
import static vtea.exploration.plotgatetools.gates.GateLayer.clipboardGate;

/**
 *
 * @author vinfrais
 */
public class PolygonGate extends Component implements Gate, Serializable {

    private ArrayList<Point2D.Double> vertices = new ArrayList<>();
    private ArrayList<Point2D.Double> verticesInChartSpace = new ArrayList<>();
    private Path2D.Double path;
    private Rectangle2D.Double boundingbox;
    private boolean selected = false;
    private boolean imagegated = false;
    private boolean mouseover = false;
    private boolean keypressed = false;

    private String name;

    private int objectsInGate;

    private int objectsTotal;

    private String xAxis;
    private String yAxis;

    private String uidGate;

    public Color selectedColor = new Color(255, 0, 0);
    public Color unselectedColor;

    private ImageStack gateoverlay;
    private ImageStack colorizedgateoverlay;

    public PolygonGate(ArrayList<Point2D.Double> points) {
        super();
        vertices.addAll(points);
        //System.out.println("PROFILING: Adding gate with vertices 1: " + vertices.size());
        path = createPath2D();
        boundingbox = (Rectangle2D.Double) path.getBounds2D();
        setFocusable(true);
        name = "Untitled";
        uidGate = makeUID();
    }

    private static String makeUID() {
        UUID id = UUID.randomUUID();
        String st = id.toString();
        return st.replaceAll("-", "");
    }

    public Point getBoundingAnchor() {

        int x = ((Double) boundingbox.getMaxX()).intValue();
        int y = ((Double) boundingbox.getMaxY()).intValue();

        return new Point(x, y);

    }

    @Override
    public Path2D.Double createPath2D() {

        Point2D p;
        Path2D.Double polygon = new Path2D.Double();

        ListIterator<Point2D.Double> itr = vertices.listIterator();

        p = (Point2D) vertices.get(0);
        //System.out.println("PROFILING: Adding gate with vertices 2: " + vertices.size());
        polygon.moveTo(p.getX(), p.getY());
        while (itr.hasNext()) {
            p = (Point2D) itr.next();
            polygon.lineTo(p.getX(), p.getY());
        }
        polygon.closePath();
        //System.out.println("PROFILING: Adding gate with vertices 3: " + vertices.size());
        return polygon;

    }

    @Override
    public void createInChartSpace(ChartPanel chart) {

        double[] x1Points = new double[vertices.size()];
        double[] y1Points = new double[vertices.size()];
        double xChartPoint;
        double yChartPoint;
        //System.out.println("PROFILING: Adding gate with vertices 4: " + vertices.size());

        for (int i = 0; i <= vertices.size() - 1; i++) {
            x1Points[i] = (double) ((Point2D) vertices.get(i)).getX();
            y1Points[i] = (double) ((Point2D) vertices.get(i)).getY();
        }
        //System.out.println("PROFILING: Adding gate with vertices 5: " + vertices.size());
        
        Rectangle2D plotArea = chart.getScreenDataArea();
        XYPlot plot = (XYPlot) chart.getChart().getPlot();

        for (int index = 0; index < x1Points.length; index++) {

            xChartPoint = plot.getDomainAxis().java2DToValue(x1Points[index],
                    plotArea, plot.getDomainAxisEdge());
            yChartPoint = plot.getRangeAxis().java2DToValue(y1Points[index],
                    plotArea, plot.getRangeAxisEdge());
            this.verticesInChartSpace.add(new Point2D.Double(xChartPoint, yChartPoint));
        }

    }
    
    public void updatePanelPositions(ChartPanel chart){
        
        ArrayList<Point2D.Double> result = createInPanelSpace(chart);
        vertices.clear();
        vertices.addAll(result);
        //createInChartSpace(chart);
        path = createPath2D();
    }

    public ArrayList<Point2D.Double> createInPanelSpace(ChartPanel chart) {

        double[] x1Points = new double[verticesInChartSpace.size()];
        double[] y1Points = new double[verticesInChartSpace.size()];
        
        double xPanelPoint;
        double yPanelPoint;

        Rectangle2D plotArea = chart.getScreenDataArea();
        XYPlot plot = (XYPlot) chart.getChart().getPlot();
        
        for (int i = 0; i <= verticesInChartSpace.size() - 1; i++) {
            x1Points[i] = (double) ((Point2D) verticesInChartSpace.get(i)).getX();
            y1Points[i] = (double) ((Point2D) verticesInChartSpace.get(i)).getY();
        }

        ArrayList<Point2D.Double> result = new ArrayList<>();
        
        //System.out.println("PROFILING: Adding gate with verticesinchartspace 8: " + verticesInChartSpace.size());

        for (int index = 0; index < x1Points.length; index++) {
             xPanelPoint = plot.getDomainAxis().valueToJava2D(x1Points[index],
                    plotArea, plot.getDomainAxisEdge());
            
            yPanelPoint = plot.getRangeAxis().valueToJava2D(y1Points[index],
                    plotArea, plot.getRangeAxisEdge());

           //System.out.println("PROFILING: Input chart position: " + x1Points[index] +"," + y1Points[index]);
           //System.out.println("PROFILING: Input panel position: " + vertices.get(index));
           //System.out.println("PROFILING: Output panel position: " + xPanelPoint +"," + yPanelPoint);
            result.add(new Point2D.Double(xPanelPoint, yPanelPoint));
        }
        
        //System.out.println("PROFILING: Adding gate with verticesinchartspace 9: " + verticesInChartSpace.size());
        return result;
    }

    @Override
    public Shape getGateAsShape() {
        return path;
    }

    @Override
    public ArrayList<Point2D.Double> getGateAsPoints() {
        //System.out.println("PROFILING: Adding gate with vertices 10: " + vertices.size());
        return vertices;
    }

    @Override
    public Path2D.Double getPath2D() {
        return path;
    }

    @Override
    public boolean getSelected() {
        return this.selected;
    }

    @Override
    public void setSelected(boolean b) {
        this.selected = b;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String st) {
        this.name = st;
    }

    public String getUID() {
        return this.uidGate;
    }

    public String getXAxis() {
        return this.xAxis;
    }

    public String getYAxis() {
        return this.yAxis;
    }

    public int getObjectsInGate() {
        return this.objectsInGate;
    }

    @Override
    public void setObjectsInGate(int count) {
        this.objectsInGate = count;
    }

    @Override
    public void setTotalObjects(int count) {
        this.objectsTotal = count;
    }

    @Override
    public int getTotalObjects() {
        return this.objectsTotal;
    }

    @Override
    public ArrayList getGateAsPointsInChart() {
        return this.verticesInChartSpace;
    }

    @Override
    public boolean getHovering() {
        return this.mouseover;
    }

    @Override
    public void setHovering(boolean b) {
        this.mouseover = b;
    }

    @Override
    public Path2D.Double createPath2DInChartSpace() {

        Point2D p;
        Path2D.Double polygon = new Path2D.Double();

        ListIterator<Point2D.Double> itr = verticesInChartSpace.listIterator(1);

        p = (Point2D.Double) verticesInChartSpace.get(0);
        //System.out.println(verticesInChartSpace.size() + " Gate points");
        // System.out.println("First Point: " + p);
        polygon.moveTo(p.getX(), p.getY());
        while (itr.hasNext()) {
            p = (Point2D.Double) itr.next();
            //System.out.println("Next Point: " + p);
            polygon.lineTo(p.getX(), p.getY());
        }
        polygon.closePath();
        return polygon;

    }

    @Override
    public boolean getKeyStroke() {
        return this.keypressed;
    }

    @Override
    public void setKeyStroke(boolean b) {
        this.keypressed = b;
    }

    @Override
    protected void processKeyEvent(KeyEvent e) {
        super.processKeyEvent(e); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Color getColor() {
        return this.selectedColor;
    }

    @Override
    public void setSelectedColor(Color c) {
        selectedColor = c;
    }

    @Override
    public void setUnselectedColor(Color c) {
        unselectedColor = c;
    }

    @Override
    public void setGateOverlayStack(ImageStack is) {
        gateoverlay = is;
    }

    @Override
    public ImageStack getGateOverlayStack() {
        return gateoverlay;
    }

    @Override
    public boolean getImageGated() {
        return imagegated;
    }

    @Override
    public void setImageGated(boolean b) {
        imagegated = b;
    }

    @Override
    public void setColorizedGateOverlayStack(ImageStack is) {
        colorizedgateoverlay = is;
    }

    @Override
    public ImageStack getColorizedGateOverlayStack() {
        return colorizedgateoverlay;
    }

    @Override
    public void setXAxis(String s) {
        this.xAxis = s;
    }

    @Override
    public void setYAxis(String s) {
        this.yAxis = s;
    }

}
