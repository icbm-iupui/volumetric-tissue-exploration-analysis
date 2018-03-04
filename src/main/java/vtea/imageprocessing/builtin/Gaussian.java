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
package vtea.imageprocessing.builtin;

/**
 *
 * @author sethwinfree
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTextField;
import net.imglib2.img.Img;
import org.scijava.plugin.Plugin;
import vtea.imageprocessing.AbstractImageProcessing;
import vtea.imageprocessing.ImageProcessing;

/**
 *
 * @author sethwinfree
 */
@Plugin(type = ImageProcessing.class)
public class Gaussian extends AbstractImageProcessing {

    public Gaussian() {
        
        VERSION = "0.1";
        
        AUTHOR = "Seth Winfree";
        
        COMMENT = "Implements the Gaussian plugin from ImageJ";
        
        NAME = "Gaussian";
        
        KEY = "Gaussian";

        protocol = new ArrayList();
        
        protocol.add(new JLabel("Sigma (pixels):"));
        
        protocol.add(new JTextField("1", 3));

        
    }
    
    @Override
    public boolean process(ArrayList al, ImagePlus imp) {

        JTextField radius = (JTextField) al.get(3);

        ImageStack is;

        is = imp.getImageStack();

        IJ.run(new ImagePlus("", is), "Gaussian Blur...", "sigma=" + radius.getText() + " stack");

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
    public boolean copyComponentParameter(int index, ArrayList dComponents, ArrayList sComponents) {
    
        try{
            
            JTextField sRadius = (JTextField) sComponents.get(3);
       
            dComponents.set(3, (new JTextField(sRadius.getText(), 3))); 
        
        return true;
        } catch(Exception e){
            System.out.println("ERROR: Could not copy parameter(s) for " + NAME);
            return false;
        }
    }

}
