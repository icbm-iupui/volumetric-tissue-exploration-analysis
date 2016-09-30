/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package vteapreprocessing;

import ij.ImagePlus;
import java.util.ArrayList;

/**
 *
 * @author vinfrais
 */
public interface PreProcessingModule<T> {
    
    public boolean setOptions(ArrayList<T> al);
    
    public ArrayList<T> getOptions();
    
    public ImagePlus getResult();
    
    public ImagePlus getPreview();
    
    public String runImageJMacroCommand(String str);
    
    public void sendProgressComment();
    
}
