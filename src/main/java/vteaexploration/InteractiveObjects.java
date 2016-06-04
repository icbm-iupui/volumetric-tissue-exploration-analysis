/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vteaexploration;

import java.util.HashMap;
import vteaexploration.plottools.panels.XYChartPanel;

/**
 *
 * @author winfrees
 */
public class InteractiveObjects {
    
    protected HashMap<Integer, double[]> plotDimensionsHM;
    protected HashMap<Integer, String> AvailableDataHM;
    protected HashMap<Integer, XYChartPanel> plotPanelsHM;

    
    
    public void setDimensionHashMap(HashMap<Integer, double[]> hm){
    this.plotDimensionsHM = hm;    
    }
    
    public double[] getDimensionHashMap(Integer key)throws NullPointerException{  
        return plotDimensionsHM.get(key);
    }
    
    public void setPlotDimensionHashMap(Integer key, double[] value) throws NullPointerException{
        plotDimensionsHM.replace(key, value);
    }
    
    public XYChartPanel getPlotPanelHashMap(Integer key)throws NullPointerException{  
        return plotPanelsHM.get(key);
    }
    
    public void setPlotPanelHashMap(Integer key, XYChartPanel value) throws NullPointerException{
        plotPanelsHM.replace(key, value);
    }
    
    
}
