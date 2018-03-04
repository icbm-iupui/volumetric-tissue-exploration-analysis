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
package vtea.exploration.plottools.panels;

import ij.ImagePlus;
import ij.gui.ImageWindow;
import java.awt.Color;
import vtea.exploration.plotgatetools.gates.Gate;
import vtea.exploration.plotgatetools.gates.GateLayer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import vtea.objects.layercake.microVolume;

/**
 *
 * @author vinfrais
 */
public abstract class AbstractExplorationPanel extends JFrame implements ExplorationCenter {

    protected JPanel CenterPanel = new JPanel();
    protected ArrayList<Gate> gates = new ArrayList<Gate>();
    protected ArrayList plotvalues = new ArrayList();
    protected ChartPanel chart;
    protected ArrayList<XYPanels> charts = new ArrayList<XYPanels>();
    protected ArrayList<GateLayer> gatelayers = new ArrayList<GateLayer>();
    protected GateLayer gl = new GateLayer();
    //gates and XYPanels and key for different axes.  First element axes key; second XYPanels; third are the gates

    protected ArrayList<ArrayList> ExplorationItems = new ArrayList<ArrayList>();
    protected HashMap<Integer, String> hm = new HashMap<Integer, String>();
    protected ImagePlus impoverlay;
    protected ImagePlus impoverlayCopy;
    protected boolean imageGate = false;
    
    
    
    protected int currentX;
    protected int currentY;
    protected int currentL; 
    protected int pointsize;
    protected Color imageGateColor = new Color(0,177,76);
    

    public AbstractExplorationPanel() {
        CenterPanel.setOpaque(false);
        CenterPanel.setBackground(vtea._vtea.BACKGROUND);
        
    }

    protected int keyLookUp(int x, int y, int l) {
        ListIterator<ArrayList> itr = ExplorationItems.listIterator();
        Object test;
        String key = x + "_" + y + "_" + l;
        while (itr.hasNext()) {
            //test = itr.next().get(0);
            ArrayList list = itr.next();
            test = list.get(0);
            if (key.equals(test.toString())) {
                return ExplorationItems.indexOf(list);
            }
        }
        return 0;
    }


}
