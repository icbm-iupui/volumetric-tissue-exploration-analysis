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
package vteaexploration;

import java.util.HashMap;
import vtea.exploration.plottools.panels.XYChartPanel;

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
