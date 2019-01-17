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
package vtea.exploration.plotgatetools.gates;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;

/**
 *
 * @author sethwinfree
 */
public abstract class GateImporter {
    
public static ArrayList<Point2D.Double> importGates(ArrayList<Point2D.Double> original, ChartPanel chart){
    
    //ArrayList<Point2D.Double> original = gate.getGateAsPointsInChart();
    ArrayList<Point2D.Double> vertices = new ArrayList();
 
    int[] x1Points = new int[original.size()];
    int[] y1Points = new int[original.size()];
    double xJava2DPoint;
    double yJava2DPoint;

    for (int i = 0; i <= original.size() - 1; i++) {
        x1Points[i] = (int) ((Point2D) original.get(i)).getX();
        y1Points[i] = (int) ((Point2D) original.get(i)).getY();
    }
            
       for (int index = 0; index < x1Points.length; index++) {

        Rectangle2D plotArea = chart.getScreenDataArea();
        XYPlot plot = (XYPlot) chart.getChart().getPlot();
        xJava2DPoint = plot.getDomainAxis().valueToJava2D(x1Points[index], plotArea, plot.getDomainAxisEdge());
        yJava2DPoint = plot.getRangeAxis().valueToJava2D(y1Points[index], plotArea, plot.getRangeAxisEdge());

        
        vertices.add(new Point2D.Double(xJava2DPoint, yJava2DPoint));
    }

    return vertices;

}   
}
