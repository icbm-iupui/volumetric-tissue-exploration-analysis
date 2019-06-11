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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import javax.swing.JComponent;
import org.jfree.chart.ChartPanel;

/**
 *
 * @author vinfrais
 */
public class FreeFormGate extends Path2D.Float implements Gate {

    public FreeFormGate() {
        super();
    }

    @Override
    public Shape getGateAsShape() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList getGateAsPoints() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Path2D createPath2D() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }



    @Override
    public boolean getSelected() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setSelected(boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList getGateAsPointsInChart() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean getHovering() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setHovering(boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void createInChartSpace(ChartPanel chart) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

 

    @Override
    public boolean getKeyStroke() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setKeyStroke(boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setSelectedColor(Color c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setUnselectedColor(Color c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Color getColor() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setGateOverlayStack(ImageStack is) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ImageStack getGateOverlayStack() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean getImageGated() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setImageGated(boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setColorizedGateOverlayStack(ImageStack is) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ImageStack getColorizedGateOverlayStack() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Double createPath2DInChartSpace() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Double getPath2D() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    class PadDraw extends JComponent {

        Image image;
        //this is gonna be your image that you draw on
        Graphics2D graphics2D;
        //this is what we'll be using to draw on
        int currentX, currentY, oldX, oldY;
	//these are gonna hold our mouse coordinates

        //Now for the constructors
        public PadDraw() {
            setDoubleBuffered(false);
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    oldX = e.getX();
                    oldY = e.getY();
                }
            });
            //if the mouse is pressed it sets the oldX & oldY
            //coordinates as the mouses x & y coordinates
            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    currentX = e.getX();
                    currentY = e.getY();
                    if (graphics2D != null) {
                        graphics2D.drawLine(oldX, oldY, currentX, currentY);
                    }
                    repaint();
                    oldX = currentX;
                    oldY = currentY;
                }

            });
            //while the mouse is dragged it sets currentX & currentY as the mouses x and y
            //then it draws a line at the coordinates
            //it repaints it and sets oldX and oldY as currentX and currentY
        }

        public void paintComponent(Graphics g) {
            if (image == null) {
                image = createImage(getSize().width, getSize().height);
                graphics2D = (Graphics2D) image.getGraphics();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                clear();

            }
            g.drawImage(image, 0, 0, null);
        }
        //this is the painting bit
        //if it has nothing on it then
        //it creates an image the size of the window
        //sets the value of Graphics as the image
        //sets the rendering
        //runs the clear() method
        //then it draws the image

        public void clear() {
            graphics2D.setPaint(Color.white);
            graphics2D.fillRect(0, 0, getSize().width, getSize().height);
            graphics2D.setPaint(Color.black);
            repaint();
        }

        //this is the clear
        //it sets the colors as white
        //then it fills the window with white
        //thin it sets the color back to black
        public void red() {
            graphics2D.setPaint(Color.red);
            repaint();
        }

        //this is the red paint
        public void black() {
            graphics2D.setPaint(Color.black);
            repaint();
        }

        //black paint
        public void magenta() {
            graphics2D.setPaint(Color.magenta);
            repaint();
        }

        //magenta paint
        public void blue() {
            graphics2D.setPaint(Color.blue);
            repaint();
        }

        //blue paint
        public void green() {
            graphics2D.setPaint(Color.green);
            repaint();
        }
        //green paint

    }

}
