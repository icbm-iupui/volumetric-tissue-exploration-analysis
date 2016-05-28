    /*
    * To change this license header, choose License Headers in Project Properties.
    * To change this template file, choose Tools | Templates
    * and open the template in the editor.
    */
    package vteaexploration.plotgatetools.gates;

    import java.awt.Color;
    import java.awt.Component;
    import java.awt.Point;
    import java.awt.Shape;
    import java.awt.event.KeyEvent;
    import java.awt.event.KeyListener;
    import java.awt.geom.Path2D;
    import java.awt.geom.Point2D;
    import java.awt.geom.Rectangle2D;
    import java.util.ArrayList;
    import java.util.ListIterator;
    import org.jfree.chart.ChartPanel;
    import org.jfree.chart.plot.XYPlot;

    /**
    *
    * @author vinfrais
    */
    public class PolygonGate extends Component implements Gate{

    private ArrayList<Point2D.Double> vertices = new ArrayList<Point2D.Double>();
    private ArrayList<Point2D.Double> verticesInChartSpace = new ArrayList<Point2D.Double>();
    private Path2D path;
    private Rectangle2D boundingbox;
    private boolean selected = false;
    private boolean mouseover = false;
    private boolean keypressed = false;
    
    public Color selectedColor = new Color(255,0,0);
    public Color unselectedColor;
    

    public PolygonGate(ArrayList<Point2D.Double> points) {
    super();
    vertices = points;
    path = createPath2D();
    boundingbox = path.getBounds2D();
    //System.out.println("Polygon gate points: " + vertices);
    //System.out.println("Bounding box: " + boundingbox);
    //System.out.println("Path: " + path);

    this.setFocusable(true);
    //Point getBoundingAnchor();

    }

    public Point getBoundingAnchor() {

    int x = ((Double) boundingbox.getMaxX()).intValue();
    int y = ((Double) boundingbox.getMaxY()).intValue();

    return new Point(x, y);

    }

    @Override
    public Path2D createPath2D() {

    Point2D p;
    Path2D.Double polygon = new Path2D.Double();

    ListIterator<Point2D.Double> itr = vertices.listIterator();

    p = (Point2D) vertices.get(0);
    polygon.moveTo(p.getX(), p.getY());
    while (itr.hasNext()) {
        p = (Point2D) itr.next();
        polygon.lineTo(p.getX(), p.getY());
    }
    polygon.closePath();
    return polygon;

    }

    @Override
    public void createInChartSpace(ChartPanel chart) {

    int[] x1Points = new int[vertices.size()];
    int[] y1Points = new int[vertices.size()];
    double xChartPoint;
    double yChartPoint;

    for (int i = 0; i <= vertices.size() - 1; i++) {
        x1Points[i] = (int) ((Point2D) vertices.get(i)).getX();
        y1Points[i] = (int) ((Point2D) vertices.get(i)).getY();
    }

    for (int index = 0; index < x1Points.length; index++) {

        Rectangle2D plotArea = chart.getScreenDataArea();
        XYPlot plot = (XYPlot) chart.getChart().getPlot();
        xChartPoint = plot.getDomainAxis().java2DToValue(x1Points[index], plotArea, plot.getDomainAxisEdge());
        yChartPoint = plot.getRangeAxis().java2DToValue(y1Points[index], plotArea, plot.getRangeAxisEdge());

        this.verticesInChartSpace.add(new Point2D.Double(xChartPoint, yChartPoint));
    }

    }

    @Override
    public Shape getGateAsShape() {
    return this.path;
    }

    @Override
    public ArrayList getGateAsPoints() {
    return this.vertices;
    }

    @Override
    public Path2D getPath2D() {
    return this.path;
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
    public Path2D createPath2DInChartSpace() {

    Point2D p;
    Path2D.Double polygon = new Path2D.Double();

    ListIterator<Point2D.Double> itr = verticesInChartSpace.listIterator();

    p = (Point2D) verticesInChartSpace.get(0);
    //System.out.println(verticesInChartSpace.size() + " Gate points");
    //System.out.println("First Point: " + p);
    polygon.moveTo(p.getX(), p.getY());
    while (itr.hasNext()) {
        p = (Point2D) itr.next();
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

    }
