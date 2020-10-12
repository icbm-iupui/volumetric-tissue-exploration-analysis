/* 
 * Copyright (C) 2020 Indiana University
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package vtea.objects.Segmentation;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JPanel;
import org.scijava.plugin.Plugin;
import vtea.objects.layercake.microRegion;
import vtea.protocol.setup.IJRoiManagerClone;
import vtea.protocol.setup.MicroThresholdAdjuster;
import vteaobjects.MicroObject;

/**
 *
 * @author winfrees
 */
@Plugin(type = Segmentation.class)

public class ImageJROIBased extends AbstractSegmentation {

    private int[] minConstants = new int[4]; // 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold
    private ImagePlus imageOriginal;
    private ImagePlus imageResult;
    private ImageStack stackOriginal;
    protected ImageStack stackResult;

    private double[][] distance;

    private boolean watershedImageJ = true;

    private ArrayList<MicroObject> alVolumes = new ArrayList<MicroObject>();
    private List<microRegion> alRegions = Collections.synchronizedList(new ArrayList<microRegion>());
    private List<microRegion> alRegionsProcessed = Collections.synchronizedList(new ArrayList<microRegion>());

    Roi[] f1;

    

    IJRoiManagerClone r;

    public ImageJROIBased() {
        VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "Import existing 2D ImageJ ROI";
        NAME = "ImageJ ROIs";
        KEY = "ImageJROI";
        TYPE = "Import";
        

        protocol = new ArrayList();
        r = new IJRoiManagerClone();
        r.makeWindow();
        protocol.add(r);

    }

    @Override
    public void setImage(ImagePlus thresholdPreview) {
        imagePreview = thresholdPreview;
    }

    @Override
    public void updateImage(ImagePlus thresholdPreview) {
        imagePreview = thresholdPreview;
      
    }

    @Override
    public ArrayList<MicroObject> getObjects() {
        return alVolumes;
    }

    @Override
    public ImagePlus getSegmentation() {
        return this.imageResult;
    }

    @Override
    public JPanel getSegmentationTool() {

        return r.getPanel();
    }

    @Override
    public void doUpdateOfTool() {
        //f1.setText(String.valueOf(mta.getMin()));
        //mta.doUpdate();
    }

    /**
     * Copies components between an source and destination arraylist
     *
     * @param version
     * @param dComponents
     * @param sComponents
     * @return
     */
    @Override
    public boolean copyComponentParameter(String version, ArrayList dComponents, ArrayList sComponents) {
        try {
            dComponents.clear();
            IJRoiManagerClone f1 = (IJRoiManagerClone) sComponents.get(0);
            dComponents.add(f1);

            return true;
        } catch (Exception e) {
            System.out.println("ERROR: Could not copy parameter(s) for " + NAME);
            return false;
        }
    }

    /**
     * Takes a set of values from 'fields' and populates the components , as
     * defined herein
     *
     * @param version
     * @param dComponents
     * @param fields
     * @return
     */
    @Override
    public boolean loadComponentParameter(String version, ArrayList dComponents, ArrayList fields) {
        try {

            dComponents.clear();
            IJRoiManagerClone n1 = (IJRoiManagerClone) fields.get(0);
            dComponents.add(n1);

            return true;
        } catch (Exception e) {
            System.out.println("ERROR: Could not copy parameter(s) for " + NAME);
            return false;
        }
    }

    /**
     * Takes the a set of components, as defined herein and populates the fields
     * ArrayList for serialization.
     *
     * @param version in case multiple versions need support
     * @param sComponents
     * @param fields
     * @return
     */
    @Override
    public boolean saveComponentParameter(String version, ArrayList fields, ArrayList sComponents) {

        try {

            fields.add(((IJRoiManagerClone) sComponents.get(0)));

            return true;
        } catch (Exception e) {
            System.out.println("ERROR: Could not save parameter(s) for " + NAME + "\n" + e.getLocalizedMessage());
            return false;
        }
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

    /**
     *
     * @param is
     * @param protocol
     * @param count
     * @return
     */
    @Override
    public boolean process(ImageStack[] is, List protocol, boolean count) {

        /**
         * segmentation and measurement protocol redefining. 0: title text, 1:
         * method (as String), 2: channel, 3: ArrayList of JComponents used for
         * analysis 3: ArrayList of Arraylist for morphology determination
         */
        // 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold
        ArrayList al = (ArrayList) protocol.get(3);

        /**
         * PLugin JComponents starts at 1
         */
        IJRoiManagerClone man = (IJRoiManagerClone) al.get(0);
        Roi[] ijRois = man.getRoisAsArray();

        for (int i = 0; i < ijRois.length; i++) {
            Roi r = ijRois[i];
            MicroObject obj = new MicroObject();
            Point[] p = r.getContainedPoints();
            int[] x = new int[p.length];
            int[] y = new int[p.length];
            int[] z = new int[p.length];
            for (int j = 0; j < p.length; j++) {
                x[j] = p[j].x;
                y[j] = p[j].y;
                z[j] = r.getZPosition();
            }

            obj.setPixelsX(x);
            obj.setPixelsY(y);
            obj.setPixelsZ(z);
            obj.setCentroid();
            //System.out.println("PROFILING:  Found a anothe object, size: " +  obj.getPixelCount());
            obj.setSerialID(alVolumes.size());
            alVolumes.add(obj);

        }

        System.out.println("PROFILING:  Found " + alVolumes.size() + " volumes.");
        return true;
    }

}
