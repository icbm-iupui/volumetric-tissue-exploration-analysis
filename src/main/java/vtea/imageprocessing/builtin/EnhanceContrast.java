/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    public boolean copyComponentParameter(int index, ArrayList dComponents, ArrayList sComponents) {
    
        try{
    
            JTextField fractionsaturated = (JTextField) sComponents.get(3);
            JRadioButton normalize = (JRadioButton) sComponents.get(4);
            JRadioButton equalize = (JRadioButton) sComponents.get(5);
            JRadioButton stack = (JRadioButton) sComponents.get(6);
            JRadioButton stackhistogram = (JRadioButton) sComponents.get(7);
       
            dComponents.set(3, (new JTextField(fractionsaturated.getText(), 3))); 
            dComponents.set(4, (new JRadioButton(labels[0], normalize.isSelected()))); 
            dComponents.set(5, (new JRadioButton(labels[1], equalize.isSelected()))); 
            dComponents.set(6, (new JRadioButton(labels[2], stack.isSelected()))); 
            dComponents.set(7, (new JRadioButton(labels[3], stackhistogram.isSelected()))); 
        
        return true;
        } catch(Exception e){
            System.out.println("ERROR: Could not copy parameter(s) for " + NAME);
            return false;
        }
    }
}
