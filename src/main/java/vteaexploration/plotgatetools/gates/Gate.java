/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vteaexploration.plotgatetools.gates;

import ij.ImageStack;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import org.jfree.chart.ChartPanel;

/**
 *
 * @author vinfrais
 */
public interface Gate {

    public Shape getGateAsShape();

    public ArrayList getGateAsPoints();

    public ArrayList getGateAsPointsInChart();

    public void createInChartSpace(ChartPanel chart);

    public Path2D createPath2D();

    public Path2D createPath2DInChartSpace();

    public Path2D getPath2D();
    
    public boolean getImageGated();
    
    public void setImageGated(boolean b);

    public boolean getSelected();

    public void setSelected(boolean b);

    public boolean getHovering();

    public void setHovering(boolean b);

    public boolean getKeyStroke();

    public void setKeyStroke(boolean b);

    public Color getColor();

    public void setSelectedColor(Color c);
    
    public void setUnselectedColor(Color c);
    
    public void setGateOverlayStack(ImageStack is);
    
    public void setColorizedGateOverlayStack(ImageStack is);

     public ImageStack getGateOverlayStack();
     
      public ImageStack getColorizedGateOverlayStack();
     
}
