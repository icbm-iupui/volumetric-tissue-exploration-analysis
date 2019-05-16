/*
 * Copyright (C) 2019 SciJava
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
import ij.ImageStack;
import ij.process.StackProcessor;
import ij.process.ImageProcessor;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTextField;
import net.imglib2.img.Img;
import org.scijava.plugin.Plugin;
import vtea.imageprocessing.AbstractImageProcessing;
import vtea.imageprocessing.ImageProcessing;

/**
 *
 * @author sukhoc
 */
@Plugin(type = ImageProcessing.class)
public class Median3D extends AbstractImageProcessing{
    
    public Median3D() {
        
        VERSION = "0.1";
        
        AUTHOR = "Suraj Khochare";
        
        COMMENT = "Implements the 3D Median from StackProcessor in ImageJ";
        
        NAME = "Median3D";
        
        KEY = "Median3D";

        protocol = new ArrayList();
        
        protocol.add(new JLabel("X radius (pixels):"));
        
        protocol.add(new JTextField("1", 3));
        
        protocol.add(new JLabel("Y radius (pixels):"));
        
        protocol.add(new JTextField("1", 3));
        
        protocol.add(new JLabel("Z radius (pixels):"));
        
        protocol.add(new JTextField("1", 3));

        
    }
    
    @Override
    public boolean process(ArrayList al, ImagePlus imp) {

        JTextField Xradius = (JTextField) al.get(3);
        JTextField Yradius = (JTextField) al.get(5);
        JTextField Zradius = (JTextField) al.get(7);        
               
        ImageStack is;
        is = imp.getImageStack();
        
        ImageProcessor ip;
        ip = imp.getProcessor();
        
        StackProcessor sp = new StackProcessor(is, ip);
        
        float xRad = Float.parseFloat(Xradius.getText());
        float yRad = Float.parseFloat(Yradius.getText());
        float zRad = Float.parseFloat(Zradius.getText());
        int firstImagePos = 0;
        sp.filter3D(is, xRad, yRad, zRad, firstImagePos, imp.getNSlices(), StackProcessor.FILTER_MEDIAN);

        return true;
    }

    @Override
    public boolean process(ArrayList al, Img img) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Img getPreview() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public String getImageJMacroCommand(){
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
            
            JTextField xRadius = (JTextField) sComponents.get(1);
            JTextField yRadius = (JTextField) sComponents.get(3);
            JTextField zRadius = (JTextField) sComponents.get(5);
            
            dComponents.add(new JLabel("X radius (pixels):"));
            dComponents.add((new JTextField(xRadius.getText(), 3))); 
            dComponents.add(new JLabel("Y radius (pixels):"));
            dComponents.add((new JTextField(yRadius.getText(), 3))); 
            dComponents.add(new JLabel("Z radius (pixels):"));
            dComponents.add((new JTextField(zRadius.getText(), 3))); 
        
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
            
            String xRadius = (String)fields.get(0);
            String yRadius = (String)fields.get(1);
            String zRadius = (String)fields.get(2);
       
            dComponents.add(new JLabel("X radius (pixels):"));
            dComponents.add((new JTextField(xRadius, 3))); 
            dComponents.add(new JLabel("Y radius (pixels):"));
            dComponents.add((new JTextField(yRadius, 3)));
            dComponents.add(new JLabel("Z radius (pixels):"));
            dComponents.add((new JTextField(zRadius, 3)));
            
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
                  fields.add(((JTextField)(dComponents.get(3))).getText());
                  fields.add(((JTextField)(dComponents.get(5))).getText());
        return true;
        
        } catch(Exception e){
            
            System.out.println("ERROR: Could not copy parameter(s) for " + NAME);
            
            return false;
        }   
    }
    
}
