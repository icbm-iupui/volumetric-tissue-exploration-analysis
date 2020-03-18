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
package vtea.workflow;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.scijava.plugin.Plugin;
import vtea.processor.ImageProcessingProcessor;
import vtea.protocol.datastructure.ImageProcessingProtocol;

/**
 *
 * @author vinfrais
 */
@Plugin(type = Workflow.class)
public class ImageProcessingWorkflow extends AbstractWorkflow {

    ImagePlus impOriginal;
    ImagePlus impProcessed;
    ImagePlus impPreview;

    int channelProcess; //-1. 0, 1 etc.  -1 for all.

    /*ImageProcessing steps are kept as fields in an ArrayList 
    These fields are arraylists that include:
    0:Name 1: Channel to operate on 2... Components
     */
    public ImageProcessingWorkflow() {
        VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "New functionality";
        NAME = "Image Processing Workflow";
        KEY = "ImageProcessing";
    }

    public ImageProcessingWorkflow(ImagePlus imp, ImageProcessingProtocol al) {

        VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "New functionality";
        NAME = "Image Processing Workflow";
        KEY = "ImageProcessing";

        impOriginal = imp;
        protocol = al;

    }

    @Override
    public ImagePlus process() {
        impProcessed = new Duplicator().run(impOriginal);
        ListIterator<Object> litr = protocol.listIterator();
        while (litr.hasNext()) {
            ImageProcessingProcessor ipp = new ImageProcessingProcessor(impPreview, protocol);
            ipp.execute();
        }
        return impProcessed;
    }

    @Override
    public ImagePlus processPreview() {
        makePreviewImage();
        ListIterator<Object> litr = protocol.listIterator();
        while (litr.hasNext()) {
            ImageProcessingProcessor ipp = new ImageProcessingProcessor(impPreview, protocol);
            ipp.execute();
        }
        impPreview.resetDisplayRange();
        return impPreview;
    }

    @Override
    public ImagePlus getResult() {
        return impProcessed;
    }

    @Override
    public ImagePlus getPreview() {
        try {
            return impPreview;
        } catch (NullPointerException ex) {
            Logger.getLogger(ImageProcessingWorkflow.class.getName()).log(Level.SEVERE, "Could not find preview image.", ex);
            return processPreview();
        }
    }

    @Override
    public ImageProcessingProtocol getSteps() {
        return (ImageProcessingProtocol) protocol;
    }

    private void makePreviewImage() {

        impOriginal.setZ(impOriginal.getNSlices() / 2);
        impOriginal.setRoi(new Roi(0, 0, 255, 255));
        if (impOriginal.getWidth() < 255 || impOriginal.getHeight() < 255) {
            impOriginal.setRoi(new Roi(0, 0, impOriginal.getWidth(), impOriginal.getHeight()));
        }
        impPreview = new Duplicator().run(impOriginal); //with ROI duplicator only copies ROI
        impPreview.hide();
        impOriginal.deleteRoi();

    }

}
