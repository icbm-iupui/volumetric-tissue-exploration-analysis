/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vtea.services;

import vtea.VTEAModule;
import vteaobjects.Segmentation.Segmentation;

/**
 *
 * @author sethwinfree
 */
public class SegmentationService extends AbstractService< Segmentation > {
    
    public SegmentationService() {
        super(Segmentation.class);
    }
    
}
