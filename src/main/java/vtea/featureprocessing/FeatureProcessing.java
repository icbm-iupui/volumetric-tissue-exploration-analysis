/*
 * Copyright (C) 2018 SciJava
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
package vtea.featureprocessing;

import java.awt.Component;
import java.util.ArrayList;
import net.imglib2.type.numeric.RealType;
import vtea.VTEAModule;

/**
 *
 * @author drewmcnutt
 * 
 */
public interface FeatureProcessing<T extends Component, A extends RealType> extends VTEAModule {
    
    public boolean setOptions(ArrayList<T> al, double[][] feature);
    
    public ArrayList<T> getOptions();
    
    public boolean process(ArrayList al, double[][] feature);
    
    public String getType();
    
    //public boolean process(ArrayList al);
    
    public ArrayList getResult();
    
    //public double[][] getImpResult();
    
    //public double[] getPreview();
    
//    public String getImageJMacroCommand();
//    
//    public String runImageJMacroCommand(String str);
    
    public String getVersion();
    
    public String getAuthor();
    
    public String getComment();
    
    public void sendProgressComment();
    
    public String getProgressComment();
    
    public boolean copyComponentParameter(int index, ArrayList<T> dComponents, ArrayList<T> sComponents);
    
}
