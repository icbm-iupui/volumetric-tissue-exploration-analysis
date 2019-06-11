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
package vtea.imageprocessing.builtin;

import ij.IJ;
import ij.ImagePlus;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import net.imglib2.img.Img;
import org.scijava.plugin.Plugin;
import vtea.imageprocessing.AbstractImageProcessing;
import vtea.imageprocessing.ImageProcessing;

/**
 *
 * @author sethwinfree
 */
@Plugin (type = ImageProcessing.class)
public class EnhanceContrast extends AbstractImageProcessing {

String[] labels = {"normalize","equalize","process_all","use"};
    
   public EnhanceContrast(){
    VERSION = "0.1";
    AUTHOR = "Seth Winfree";
    COMMENT = "Implements the plugin from ImageJ";
    NAME = "Enhance Contrast";
    KEY = "EnhanceContrast";  
    
    protocol = new ArrayList();

    protocol.add(new JLabel("saturation (%)"));
    protocol.add(new JTextField("0.1", 5));
    protocol.add(new JRadioButton(labels[0], true));
    protocol.add(new JRadioButton(labels[1], false));
    protocol.add(new JRadioButton(labels[2], true));
    protocol.add(new JRadioButton(labels[3], false));
 
    }
   
    @Override
    public boolean process(ArrayList al, ImagePlus imp) {
        JTextField fractionsaturated;
        JRadioButton normalize, stack, equalize, stackhistogram;

        fractionsaturated = (JTextField) al.get(3);
        normalize = (JRadioButton) al.get(4);
        equalize = (JRadioButton) al.get(5);
        stack = (JRadioButton) al.get(6);
        stackhistogram = (JRadioButton) al.get(7);

        String norm, equal, stackall, stackhisto;

        if (normalize.isSelected()) {
            norm = "normalize";
        } else {
            norm = "";
        }
        if (equalize.isSelected()) {
            equal = "equalize";
        } else {
            equal = "";
        }
        if (stack.isSelected()) {
            stackall = "process_all";
        } else {
            stackall = "";
        }
        if (stackhistogram.isSelected()) {
            stackhisto = "use";
        } else {
            stackhisto = "";
        }
        IJ.run(imp, "Enhance Contrast...", "saturated=" + fractionsaturated.getText() + " " + norm + " " + equal + " " + stackall + " " + stackhisto);
        
        //imgResult = ImageJFunctions.wrapReal(imp);
        
        return true;
    }

    @Override
    public boolean process(ArrayList al, Img img) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Img getResult() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Img getPreview() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    public boolean copyComponentParameter(String version, ArrayList dComponents, ArrayList sComponents) {
    
        try{
            dComponents.clear();

            JTextField fractionsaturated = (JTextField) sComponents.get(1);
            JRadioButton normalize = (JRadioButton) sComponents.get(2);
            JRadioButton equalize = (JRadioButton) sComponents.get(3);
            JRadioButton stack = (JRadioButton) sComponents.get(4);
            JRadioButton stackhistogram = (JRadioButton) sComponents.get(5);
            
            dComponents.add(new JLabel("saturation (%)"));
       
            dComponents.add(new JTextField(fractionsaturated.getText(), 3)); 
            dComponents.add(new JRadioButton(labels[0], normalize.isSelected())); 
            dComponents.add(new JRadioButton(labels[1], equalize.isSelected())); 
            dComponents.add(new JRadioButton(labels[2], stack.isSelected())); 
            dComponents.add(new JRadioButton(labels[3], stackhistogram.isSelected())); 
        
        return true;
        } catch(Exception e){
            System.out.println("ERROR: Could not copy parameter(s) for " + NAME);
            return false;
        }
    }
    
    @Override
    public boolean loadComponentParameter(String version, ArrayList dComponents, ArrayList fields) {
             try{
                  dComponents.clear();
            
            String fSaturated = (String)fields.get(0);
       
            dComponents.add(new JLabel("saturation (%)"));
       
            dComponents.add(new JTextField(fSaturated, 3)); 
            dComponents.add(new JRadioButton(labels[0], (boolean)fields.get(1))); 
            dComponents.add(new JRadioButton(labels[1], (boolean)fields.get(2))); 
            dComponents.add(new JRadioButton(labels[2], (boolean)fields.get(3))); 
            dComponents.add(new JRadioButton(labels[3], (boolean)fields.get(4))); 
            
        return true;
        
        } catch(Exception e){
            
            System.out.println("ERROR: Could not copy parameter(s) for " + NAME);
            
            return false;
        }   
    }
    
    @Override
    public boolean saveComponentParameter(String version, ArrayList dComponents, ArrayList fields) {
            try{
                  fields.clear();
                  fields.add(((JTextField)(dComponents.get(1))).getText());
                  fields.add(((JRadioButton)(dComponents.get(2))).isSelected());
                  fields.add(((JRadioButton)(dComponents.get(3))).isSelected());
                  fields.add(((JRadioButton)(dComponents.get(4))).isSelected());
                  fields.add(((JRadioButton)(dComponents.get(5))).isSelected());
        return true;
        
        } catch(Exception e){
            
            System.out.println("ERROR: Could not copy parameter(s) for " + NAME);
            
            return false;
        }   
    }
}
