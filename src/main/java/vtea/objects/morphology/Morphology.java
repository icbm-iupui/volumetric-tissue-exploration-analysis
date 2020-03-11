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
package vtea.objects.morphology;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import vtea.VTEAModule;

/**
 *
 * @author sethwinfree
 */
public interface Morphology extends VTEAModule{
    
    public ArrayList<ArrayList<Number>> process(int[] x, int[] y, int[] z, List<JComponent> protocol, String operation, String arg);
    
    public Image getExamples(); 
    
    public JPanel getMorphologicalTool();
    
    public ArrayList getOptions();
    
    public ArrayList getSettings();
    
    public String getUID(ArrayList<JComponent> al);
}
