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

    public Path2D.Double createPath2DInChartSpace();

    public Path2D.Double getPath2D();
    
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
