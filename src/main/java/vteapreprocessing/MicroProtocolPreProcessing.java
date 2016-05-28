/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vteapreprocessing;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.ChannelSplitter;
import ij.plugin.Duplicator;
import ij.plugin.filter.BackgroundSubtracter;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 *
 * @author vinfrais
 */
public class MicroProtocolPreProcessing extends java.lang.Object {

    ImagePlus impOriginal;
    ImagePlus impPreview;
    ArrayList protocol;

    public MicroProtocolPreProcessing(ImagePlus imp, ArrayList protocol) {

        
        
        impOriginal = imp;
        impPreview = imp.duplicate();
        this.protocol = protocol;
    }
    
    private void makePreviewImage() {
        
        impOriginal.resetStack();
        impOriginal.setRoi(new Roi(0,0,255,255));
        if(impOriginal.getWidth() < 255 || impOriginal.getHeight() < 255){
            impOriginal.setRoi(new Roi(0,0,impOriginal.getWidth(),impOriginal.getHeight()));
        }
        impPreview = new Duplicator().run(impOriginal);
        impPreview.hide();
        impOriginal.deleteRoi();
        
    }

    public ImagePlus ProcessImage() {
        makePreviewImage();
        ListIterator<Object> litr = this.protocol.listIterator();
        while (litr.hasNext()) {
            ProcessManager((ArrayList) litr.next(), impPreview);
        }
        return impPreview;
    }
    
    public ImagePlus ProcessPreviewImage() {
        makePreviewImage();
        ListIterator<Object> litr = this.protocol.listIterator();
        while (litr.hasNext()) {
            ProcessManager((ArrayList) litr.next(), impPreview);
        }
       
        return impPreview;
    }
    
    private void ProcessManager(ArrayList steps, ImagePlus imp) {
        
        int testcase = 0;
        
        //System.out.println(steps);
        
        if(steps.get(0).toString().equals("Background Subtraction")) {testcase = 0;};
        if(steps.get(0).toString().equals("Enhance Contrast")) {testcase = 1;};
        if(steps.get(0).toString().equals("Reduce Noise")) {testcase = 2;};      
        
        switch (testcase) {
            case 0:
                SubtractBackground(imp,steps);
                //System.out.println("PROFILING: Running Background Subtraction...");
                break;
            case 1:
                EnhanceContrast(imp,steps);
               // System.out.println("PROFILING: Running Enhance Contrast...");
                break;
            case 2: ;
                break;
            default: ;
                break;
        }
    }

    private void SubtractBackground(ImagePlus imp, ArrayList variables) {

        JTextField radius;
        int channel;
        JRadioButton slidingparabaloid, stack;

        channel = (Integer)variables.get(1);
        radius = (JTextField) variables.get(3);
        //slidingparabaloid = (JRadioButton) variables.get(4);
        //stack = (JRadioButton) variables.get(5);
        

//        String paraboloid, all;
//
//        if (slidingparabaloid.isSelected()) {
//            paraboloid = "paraboloid";
//        } else {
//            paraboloid = "";
//        }
//        if (stack.isSelected()) {
//            all = "stack";
//        } else {
//            all = "";
//        }
        
        BackgroundSubtracter rbb = new BackgroundSubtracter();
        
        //rollingBallBackground(ImageProcessor ip, double radius, boolean createBackground, boolean lightBackground, boolean useParaboloid, boolean doPresmooth, boolean correctCorners)
        
        ChannelSplitter cs = new ChannelSplitter();
        
        ImageStack is;
        
        is = cs.getChannel(imp, channel+1);
        
        for(int n = 1; n <= is.getSize(); n++){
        rbb.rollingBallBackground(is.getProcessor(n), Integer.parseInt(radius.getText()), false, false, false, true, true);
        //System.out.println("PROFILING: Running Background Subtraction... slice " + n + ", radius: " + radius.getText());
        }
        //ImagePlus result = new ImagePlus("processed", is);
        //result.show();
    }

    private void EnhanceContrast(ImagePlus imp, ArrayList variables) {

        JTextField fractionsaturated;
        JRadioButton normalize, stack, equalize, stackhistogram;

        fractionsaturated = (JTextField) variables.get(3);
        normalize = (JRadioButton) variables.get(4);
        equalize = (JRadioButton) variables.get(5);
        stack = (JRadioButton) variables.get(6);
        stackhistogram = (JRadioButton) variables.get(7);

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
    }

    public ImagePlus getResult() {
        return this.impOriginal;
    }

    public ImagePlus getPreview() {
        return this.impPreview;
    }
    
    public ArrayList getSteps() {
        return this.protocol;
    }

}
