/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vteaimageprocessing;

import ij.ImagePlus;
import java.awt.Component;
import java.util.ArrayList;
import net.imglib2.img.AbstractImg;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.cell.CellImg;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

/**
 *
 * @author sethwinfree
 */
public class AbstractImageProcessing<T extends Component, A extends RealType>  implements ImageProcessing {
    
    protected String VERSION = "0.0";
    protected String AUTHOR = "VTEA Developer";
    protected String COMMENT = "New functionality";
    protected String NAME = "ND";
    protected String KEY = "ND";

    protected ArrayList<T> protocol= new ArrayList();
    
    protected Img imgResult;
    
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
    public Img getResult() {
        
       return imgResult; 
        
    }
    
        @Override
    public ImagePlus getImpResult() {
        
       return ImageJFunctions.wrapUnsignedShort(imgResult, NAME); 
        
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
    public boolean process(ArrayList al, ImagePlus imp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean process(ArrayList al, Img img) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}
