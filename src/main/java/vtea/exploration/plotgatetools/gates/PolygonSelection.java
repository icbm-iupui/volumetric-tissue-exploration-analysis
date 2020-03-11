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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import vtea.exploration.plotgatetools.listeners.PolygonSelectionListener;

/**
 *
 * @author vinfrais
 *
 * http://www.coderanch.com/t/344218/GUI/java/Polygon-lasso-drawing Craig Wood
 *
 */
public class PolygonSelection extends MicroSelection {

    List<Point> points = new ArrayList<Point>();
    ArrayList<PolygonSelectionListener> gateListener = new ArrayList();
    public MouseListener ml = new MouseAdapter() {
        int size = 5;
        int PROXIMAL = 5;

        public void mousePressed(MouseEvent e) {
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
            } else if (e.getClickCount() == 2) {
                points.clear();
            }
            repaint();
        }
    };

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(Color.blue);
        for (int j = 0; j < points.size() - 1; j++) {
            Point p = points.get(j);
            Point next = points.get(j + 1);
            g2.draw(new Line2D.Double(p, next));
        }
        g2.setPaint(Color.red);
        for (int j = 0; j < points.size(); j++) {
            Point p = points.get(j);
            g2.fill(new Ellipse2D.Double(p.x - 2, p.y - 2, 4, 4));
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(512, 512);
    }

}
