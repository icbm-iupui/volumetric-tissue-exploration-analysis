/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package vteaimageprocessing;

import ij.ImagePlus;
import java.awt.Component;
import java.util.ArrayList;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import vtea.VTEAModule;



/**
 *
 * @author vinfrais
 */
public interface ImageProcessing<T extends Component, A extends RealType> extends VTEAModule {
    
    public boolean setOptions(ArrayList<T> al);
    
    public ArrayList<T> getOptions();
    
    public boolean process(ArrayList al, ImagePlus imp);
    
    public boolean process(ArrayList al, Img<A> img);
    
    public Img<A> getResult();
    
    public ImagePlus getImpResult();
    
    public Img<A> getPreview();
    
    public String runImageJMacroCommand(String str);
    
    public String getVersion();
    
    public String getAuthor();
    
    public String getComment();
    
    public void sendProgressComment();
    
    public String getProgressComment();
    
}
