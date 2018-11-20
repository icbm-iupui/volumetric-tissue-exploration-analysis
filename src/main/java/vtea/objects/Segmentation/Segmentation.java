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
package vtea.objects.Segmentation;

import ij.ImagePlus;
import ij.ImageStack;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import vtea.VTEAModule;
import vtea.processor.listeners.SegmentationListener;
import vteaobjects.MicroObject;

/**
 *
 * @author winfrees
 */
public interface Segmentation extends VTEAModule {
    
    public ImagePlus getSegmentation();
    
    public ArrayList<MicroObject> getObjects();
    
    public boolean process(ImageStack[] is, List details, boolean calculate);
    
    public JPanel getOptionsPanel();
    
    public String runImageJMacroCommand(String str);
    
    public String getVersion();
    
    public String getAuthor();
    
    public String getComment();
   
    public void sendProgressComment();
    
    public String getProgressComment();

    public boolean setOptions(ArrayList al);
    
    public void setSegmentationTool(ArrayList al);
     
    public JPanel getSegmentationTool();
    
    public ArrayList getSegmentationToolOptions();
    
    public ArrayList getOptions();
    
    public ArrayList getDefaultValues();
    
    public void addSegmentationListener(SegmentationListener sl);
    
    public void notifySegmentationListener(String str, Double dbl);
    
    public void setImage(ImagePlus thresholdPreview);
    
    public void updateImage(ImagePlus thresholdPreview);
    
}
