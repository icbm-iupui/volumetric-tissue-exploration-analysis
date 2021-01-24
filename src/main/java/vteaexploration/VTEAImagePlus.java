/*
 * Copyright (C) 2021 SciJava
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
package vteaexploration;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.ImageWindow;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.gui.StackWindow;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *
 * @author Seth
 */
public class VTEAImagePlus extends CompositeImage {

    boolean activated;
    private static int currentID = -1;
    private int ID;

    public VTEAImagePlus(String title, ImagePlus imp) {
        super(imp);
        setID();
    }

    private void setID() {
        ID = --currentID;
    }

    @Override

    public void show(String statusMessage) {
        if (isVisible()) {
            return;
        }
        win = null;
//        if ((IJ.isMacro() && ij==null) || Interpreter.isBatchMode()) {
//            if (isComposite()) ((CompositeImage)this).reset();
//            ImagePlus imp = WindowManager.getCurrentImage();
//            if (imp!=null) imp.saveRoi();
//            WindowManager.setTempCurrentImage(this);
//            Interpreter.addBatchModeImage(this);
//            return;
//        }
        if (Prefs.useInvertingLut && getBitDepth() == 8 && ip != null && !ip.isInvertedLut() && !ip.isColorLut()) {
            invertLookupTable();
        }
        img = getImage();
        if ((img != null) && (width >= 0) && (height >= 0)) {
            activated = false;
            int stackSize = getStackSize();
            if (stackSize > 1) {
                win = new StackWindow(this);
            } else if (getProperty(Plot.PROPERTY_KEY) != null) {
                win = new PlotWindow(this, (Plot) (getProperty(Plot.PROPERTY_KEY)));
            } else {
                win = new ImageWindow(this);
            }
            if (roi != null) {
                roi.setImage(this);
            }
            if (this.getOverlay() != null && getCanvas() != null) {
                getCanvas().setOverlay(this.getOverlay());
            }
            IJ.showStatus(statusMessage);
//            if (IJ.isMacro()) { // wait for window to be activated
//                long start = System.currentTimeMillis();
//                while (!activated) {
//                    IJ.wait(5);
//                    if ((System.currentTimeMillis()-start)>2000) {
//                        WindowManager.setTempCurrentImage(this);
//                        break; // 2 second timeout
//                    }
//                }
//            }

            //if (imageType==GRAY16 && default16bitDisplayRange!=0) {
            resetDisplayRange();
            updateAndDraw();
            //}
            if (stackSize > 1) {
                int c = getChannel();
                int z = getSlice();
                int t = getFrame();
                if (c > 1 || z > 1 || t > 1) {
                    setPosition(c, z, t);
                }
            }
            if (setIJMenuBar) {
                IJ.wait(25);
            }
            win.addWindowListener(
                    new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    //IJ.log("DONT Close!!!");
//                    // Only let user close after three clicks (on fourth)!
//                    if ((count % 4) == 0) {
//                        win.dispose();
//                    }
//                    ++count;
                    win.setVisible(true);
                }
            });
            notifyListeners(OPENED);
        }
    }

    void invertLookupTable() {
        int nImages = getStackSize();
        ip.invertLut();
        if (nImages == 1) {
            ip.invert();
        } else {
            ImageStack stack2 = getStack();
            for (int i = 1; i <= nImages; i++) {
                stack2.getProcessor(i).invert();
            }
            stack2.setColorModel(ip.getColorModel());
        }
    }

}
