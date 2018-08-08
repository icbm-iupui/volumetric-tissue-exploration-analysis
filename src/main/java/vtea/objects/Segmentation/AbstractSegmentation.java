/*
 * Copyright (C) 2017 SciJava
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
import java.awt.Component;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import vteaobjects.MicroObject;

/**
 *
 * @author sethwinfree
 * @param <T>
 * @param <K>
 */
public class AbstractSegmentation<T extends Component, K extends Object> implements Segmentation {
    
    protected String VERSION = "0.0";
    protected String AUTHOR = "VTEA Developer";
    protected String COMMENT = "New functionality";
    protected String NAME = "ND";
    protected String KEY = "ND";
    
    protected ArrayList<T> protocol= new ArrayList();
    
    protected ArrayList<Integer> defaultValues = new ArrayList();
    
            /**protocol is arraylist of segementation settings.
             * (0) is the channel the segmentation is based on, as a zero order integer
             * (1) is the name of the segmentation protocol
             * (2) is an Arraylist of field names (this should be changed to a hashmap
             * (3) is an ArrayList of the segmentation settings per the ArrayList in 2
            **/
    
    //protected ArrayList<T> protocol = new ArrayList();
    
    protected ArrayList buildtool = new ArrayList();  //right this is where I am...

    
    @Override
    public ImagePlus getSegmentation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList<MicroObject> getObjects() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getAuthor() {
        return AUTHOR;
    }

    @Override
    public String getComment() {
        return COMMENT;
    }

    @Override
    public String runImageJMacroCommand(String str) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void sendProgressComment() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getProgressComment() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean process(ImageStack[] is, List details, boolean calculate) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private <K> void propertyChange(String name, K previousValue, K lastChange ) {
        PropertyChangeSupport p = new PropertyChangeSupport(this);
        p.firePropertyChange(name, previousValue, lastChange);
    } 
    
    @Override
    public void setBuildTool(ArrayList al) {
        
    }
    
    @Override
    public boolean setOptions(ArrayList al) {
        protocol = al;
        return true;
    }

    @Override
    public ArrayList getOptions() {
        return protocol;
    }

    @Override
    public ArrayList getBuildTool() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList getBuildOptions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList getDefaultValues() {
        return defaultValues;
    }

    @Override
    public JPanel getOptionsPanel() {
        return new JPanel();
    }


}
