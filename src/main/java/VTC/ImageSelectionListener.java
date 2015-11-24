/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package VTC;

import ij.ImagePlus;

/**
 *
 * @author vinfrais
 */
public interface ImageSelectionListener {
     public void onSelect(ImagePlus imp, int tab);
}
