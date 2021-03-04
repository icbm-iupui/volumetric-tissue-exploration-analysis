/*
 * Copyright (C) 2021 SciJava
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
package vtea.gates.math;

import ij.gui.Roi;
import java.util.ArrayList;
import vtea.VTEAModule;
import vtea.exploration.plotgatetools.gates.Gate;
import vtea.exploration.plotgatetools.gates.PolygonGate;
import vteaobjects.MicroObject;
import vteaobjects.MicroObjectModel;

/**
 *
 * @author sethwinfree
 */
public interface GateMath<T extends PolygonGate, A extends Roi, K extends MicroObject> 
extends VTEAModule {
    
    public void settings(ArrayList<K> objects, 
            ArrayList<ArrayList> measurements, 
            ArrayList<String> descriptions,
            String keySQLSafe);
    
    public  ArrayList<ArrayList> process(A g1, T g2);
    
    public  ArrayList<ArrayList> process(T g1, A g2);
    
    public  ArrayList<ArrayList> process(T g1, T g2);
    
    public  ArrayList<ArrayList> process(A g1, A g2);
    
    public  ArrayList<ArrayList> process(ArrayList<ArrayList> gatedResults, A g1);
    
    public  ArrayList<ArrayList> process(ArrayList<ArrayList> gatedResults, T g1);
}

