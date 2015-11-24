/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package VTC;

import ij.gui.Roi;

/**
 *
 * @author vinfrais
 */
public interface IJ1RoiModifiedListener {
    
    public void roiCreated(Roi r);
    
    public void roiMoved(Roi r);
    
    public void roiDeleted(Roi r);
    
}
