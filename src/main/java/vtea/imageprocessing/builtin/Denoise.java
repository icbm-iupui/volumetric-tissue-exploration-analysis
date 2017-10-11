/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vtea.imageprocessing.builtin;

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
public class Denoise extends AbstractImageProcessing {

    public Denoise() {
        
        VERSION = "0.1";
        
        AUTHOR = "Seth Winfree";
        
        COMMENT = "Implements the Median plugin from ImageJ";
        
        NAME = "Denoise";
        
        KEY = "Denoise";

        protocol = new ArrayList();
        
        protocol.add(new JLabel("Radius (pixels):"));
        
        protocol.add(new JTextField("1", 3));

        
    }
    
    @Override
    public boolean process(ArrayList al, ImagePlus imp) {

        JTextField radius = (JTextField) al.get(3);

        ImageStack is;

        is = imp.getImageStack();

        IJ.run(new ImagePlus("", is), "Median...", "radius=" + radius.getText() + " stack");

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
