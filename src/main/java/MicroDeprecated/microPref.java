/* 
 * Copyright (C) 2016 Indiana University
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
package MicroDeprecated;

import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import ij.plugin.frame.*;

@Deprecated

public class microPref extends Object implements Cloneable, java.io.Serializable {

    /**
     * Variables*
     */
    private static final int GROW_1 = 0;
    private static final int GROW_2 = 1;
    private static final int ANALYZED = 1;
    private static final int SETUP = 0;
    private static final int INTERLEAVED = 0;
    private static final int HYPERSTACK = 1;
    private static final int CHANNEL = 2;
    private static final int MASK = 0;
    private static final int EXPERIMENTAL = 1;
    private static final int NOT_USED = 2;

    private String[] values = {"Y", "N"};
    private String[] derivedExperimental = {"GROW", "ERODE"};
    private int derivedExperimentalSize = 1;
    private String[] derivedMask = {"FILL volume"};
    private String[] imageTypeText = {"Interleaved", "Hyperstack", "Channels"};
    private String[] channels = {"MASK", "EXPERIMENTAL", "EMPTY"};

    private String[] channelNames = new String[10];
    private int channelCount = 3;
    private int[] channelType = new int[4];
    private ImagePlus[] impArray = new ImagePlus[3];

    private int imageCount = 1;

    private int maskAnalysis;
    private int exp1Analysis;
    private int exp2Analysis;

    private int imageType;

    private int x_dim;
    private int y_dim;
    private int height;

    private int image1;
    private int image2;
    private int image3;

    private int interleavedHeight;

    private int minObjectSize = 50;
    private int maxObjectSize = 500;
    private double minOverlap = 5;
    private int minThreshold = 1000;

    private float[] minConstants = new float[4];

    private boolean valid = false;
    private int status;

    /**
     * Constructor*
     */
    public microPref() {
        return;
    }

    public microPref(int x_dim, int y_dim, int height, int channels) {
        status = SETUP;
        return;
    }

    /**
     * Methods*
     */
    public boolean getValid() {
        return valid;
    }

    public void showDialog(ImagePlus imp) {

        int[] windowList = WindowManager.getIDList();
//		if (windowList==null || windowList.length<1) {
//			//error();
//			IJ.showMessage("Error", "No open images");
//			return;
//		}
//		String[] titles = new String[windowList.length];
//		for (int i=0; i<windowList.length; i++) {
//			ImagePlus imp_temp = WindowManager.getImage(windowList[i]);
//			titles[i] = imp_temp!=null?imp_temp.getTitle():"";
//		}

        GenericDialog gd = new GenericDialog("MicroFLOW v0.25");
//		gd.addChoice("Image 1:", titles, titles[0]);
//		
//		if(windowList.length > 1) {gd.addChoice("Image 2:", titles, titles[1]);}
//		if(windowList.length > 2) {gd.addChoice("Image 3:", titles, titles[2]);}
        gd.addChoice("Type:", imageTypeText, imageTypeText[0]);
        gd.addNumericField("Channels:", channelCount, 0, 4, "");

        gd.addChoice("Channel 1:", channels, channels[0]);
        gd.addStringField("Channel 1 name:", channelNames[0]);
        gd.addChoice("Channel 2:", channels, channels[1]);
        gd.addStringField("Channel 2 name:", channelNames[1]);
        gd.addChoice("Channel 3:", channels, channels[2]);
        gd.addStringField("Channel 3 name:", channelNames[2]);

        gd.addChoice("Mask Analysis:", derivedMask, derivedMask[0]);
        gd.addChoice("Experimental 1 Analysis:", derivedExperimental, derivedExperimental[0]);
        gd.addNumericField("Pixels:", derivedExperimentalSize, derivedExperimentalSize);

        gd.addNumericField("Minimum object size:", minObjectSize, 0, 4, "px");
        gd.addNumericField("Maximum object size:", maxObjectSize, 0, 4, "px");
        gd.addNumericField("Overlap distance:", minOverlap, 0, 4, "px");
        gd.addNumericField("Object intensity threshold:", minThreshold, 0, 4, "");
        gd.addMessage("___________________________________________");

        gd.addMessage("Author: Seth Winfree Indiana University   05/21/2013");
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }

        //IJ.log("microPref::showDialog           Displayed dialog panel.");
//		image1 = gd.getNextChoiceIndex();
//		if(windowList.length > 1) {image2 = gd.getNextChoiceIndex();}
//		if(windowList.length > 2) {image3 = gd.getNextChoiceIndex();}
//	
        impArray[0] = WindowManager.getImage(windowList[image1]);
        if (windowList.length > 1) {
            impArray[1] = WindowManager.getImage(windowList[image2]);
        }
        if (windowList.length > 2) {
            impArray[2] = WindowManager.getImage(windowList[image3]);
        }

        imageType = gd.getNextChoiceIndex();
        channelCount = (int) gd.getNextNumber();

        channelType[0] = gd.getNextChoiceIndex();
        channelNames[0] = gd.getNextString();
        channelType[1] = gd.getNextChoiceIndex();
        channelNames[1] = gd.getNextString();
        channelType[2] = gd.getNextChoiceIndex();
        channelNames[2] = gd.getNextString();

        maskAnalysis = gd.getNextChoiceIndex();
        exp1Analysis = gd.getNextChoiceIndex();
        derivedExperimentalSize = (int) gd.getNextNumber();

        minObjectSize = (int) gd.getNextNumber();
        maxObjectSize = (int) gd.getNextNumber();
        minOverlap = (double) gd.getNextNumber();
        minThreshold = (int) gd.getNextNumber();

        IJ.log("microPref::showDialog           Extracted variables.");
        IJ.log("Image name:  " + imp.getTitle());
        IJ.log("        Imagetype:  " + imageType);
        IJ.log("        Channel count:  " + channelCount);
        IJ.log("        Channel 1 type:  " + channelType[0]);
        IJ.log("        Channel 2 type:  " + channelType[1]);
        IJ.log("        Channel 3 type:  " + channelType[2]);
	//min constants

        //IJ.log("microPref::showDialog           Setup constants.");
        minConstants[0] = (float) minObjectSize;
        minConstants[1] = (float) maxObjectSize;
        minConstants[2] = (float) minOverlap;
        minConstants[3] = (float) minThreshold;

        //IJ.log("microPref::showDialog           Defined variables for methods.");
        y_dim = imp.getHeight();
        x_dim = imp.getWidth();
        height = imp.getStackSize();

        interleavedHeight = height / channelCount;
        //FIX   need an exception trap here or accomadation for none interleaved images...
        return;
    }

    //valid = validDialog(minThreshold, minOverlap, imp);}
    /**
     * dialog validation function
     */
//needs to be simplified for existing plugin...
    private boolean validDialog(int minThreshold, double minDistance, ImagePlus imp) {
        ImageStatistics stats = imp.getStatistics();
        double max = stats.max;
        ImageStack stack = imp.getStack();
        int stackSize = stack.getSize();
        double sizeFlag = stackSize / minDistance;
        if (sizeFlag > 3) {
            IJ.showMessage("Warning", "Distance threshold large");
            return true;
        } else {
            return true;
        }
    }

    /**
     * Additional methods for dialog results*
     */
    public ImagePlus[] getImages() {
        return impArray;
    }
//returns all the imps

    public ImageStack[] getStacks() {
        ImageStack[] stacks = new ImageStack[3];
        stacks[0] = impArray[0].getStack();
        stacks[1] = impArray[1].getStack();
        stacks[2] = impArray[2].getStack();
        //IJ.log("microPref::getStacks           Generated stack array.");
        //IJ.log("        Stack height:  " + stacks[0].getSize());
        return stacks;
    }
//returns the stacks from three individual channel dataset

    public ImageStack[] getInterleavedStacks(ImagePlus imp) {
        ImageStack[] stacks = new ImageStack[channelCount];
        ImageStack stack = imp.getImageStack();

        for (int m = 0; m <= channelCount - 1; m++) {
            stacks[m] = new ImageStack(x_dim, y_dim);
            for (int n = m; n <= impArray[0].getStackSize() - 1; n += channelCount) {
                stacks[m].addSlice(stack.getProcessor(n + 1));
            }
        }
        IJ.log("microPref::getInterleavedStacks           Generated stack array.");
        IJ.log("        ImagePlus height:  " + impArray[0].getStackSize());
        IJ.log("        Interleaved height:  " + interleavedHeight);
        //IJ.log("        Channel count:  " + channelCount);
        //IJ.log("        Stack height:  " + stacks[0].getSize());

        return stacks;
    }
//returns stacks from a delinterleaved stack-not a hyperstack

//returns stacks from a hyper stack
    public int getImageType() {
        return imageType;
    }

    public String[] getTitles() {
        return channelNames;
    }

    public String getImageName() {
        return impArray[0].getTitle();
    }

    ;
public int getChannelCount() {
        return channelCount;
    }

    public int[] getChannelTypes() {
        return channelType;
    }

    public int getMaskAnalysis() {
        return maskAnalysis;
    }

    public int getExpAnalysis() {
        return exp1Analysis;
    }

    public int getDerivedExperimentalSize() {
        return derivedExperimentalSize;
    }

    public float[] getMinConstants() {
        return minConstants;
    }

    public int getStackHeight() {
        return interleavedHeight;
    }

}
