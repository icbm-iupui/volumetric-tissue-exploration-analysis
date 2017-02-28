/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vteaobjects.Segmentation;

import ij.ImagePlus;
import java.util.ArrayList;
import vtea.VTEAModule;
import vteaobjects.MicroObject;

/**
 *
 * @author winfrees
 */
public interface Segmentation extends VTEAModule {
    
    public ImagePlus getSegmentation();
    //need to generalize for IJ2
    public ArrayList<MicroObject> getObjects();
    
    
}
