/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vteaexploration.plottools.panels;

import ij.ImagePlus;
import java.awt.Color;
import vteaexploration.plotgatetools.gates.Gate;
import vteaexploration.plotgatetools.gates.GateLayer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import vteaobjects.layercake.microVolume;

/**
 *
 * @author vinfrais
 */
public abstract class DefaultExplorationPanel extends JFrame implements ExplorationCenter {

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
    protected boolean imageGate = false;
    
    
    
    protected int currentX;
    protected int currentY;
    protected int currentL; 
    protected int pointsize;
    protected Color imageGateColor = new Color(0,177,76);
    

    public DefaultExplorationPanel() {
        CenterPanel.setOpaque(false);
        CenterPanel.setBackground(VTC._VTC.BACKGROUND);
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
