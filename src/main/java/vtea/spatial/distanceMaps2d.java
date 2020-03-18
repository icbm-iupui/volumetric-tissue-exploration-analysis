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
package vtea.spatial;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.ZProjector;
import java.util.ArrayList;
import java.util.ListIterator;
import vtea.exploration.listeners.AddFeaturesListener;
import vteaobjects.MicroObject;

/**
 *
 * @author Seth
 */
public class distanceMaps2d {

    ArrayList<ImagePlus> distanceMap2d = new ArrayList<ImagePlus>();
    ArrayList<String> map2dName = new ArrayList<String>();

    ArrayList<AddFeaturesListener> addfeaturelistener = new ArrayList<AddFeaturesListener>();

    public void distanceMaps2d() {

    }

    public void addMap(ImagePlus imp, String name) {

        distanceMap2d.add(imp);
        map2dName.add(name);

    }

    public ImagePlus makeMap(ImagePlus imp, ArrayList<MicroObject> al) {

        ImagePlus resultImage = IJ.createImage("Segmentation", "8-bit black", imp.getWidth(), imp.getHeight(), imp.getNSlices());
        ImageStack resultStack = resultImage.getStack();
        int value = 1;

        for (int i = 0; i <= imp.getNSlices(); i++) {
            ListIterator<MicroObject> itr = al.listIterator();
            while (itr.hasNext()) {
                try {
                    MicroObject vol = (MicroObject) itr.next();
                    int[] x_pixels = vol.getXPixelsInRegion(i);
                    int[] y_pixels = vol.getYPixelsInRegion(i);
                    for (int c = 0; c < x_pixels.length; c++) {
                        resultStack.setVoxel(x_pixels[c], y_pixels[c], i, 255);
                    }

                } catch (NullPointerException e) {
                }
            }
        }

        resultImage = ZProjector.run(resultImage, "max");

        IJ.run(resultImage, "Distance Map", "");

        //resultImage.show();
        //IJ.run(resultImage, "3-3-2 RGB", "");
        return resultImage;
    }

    public ImagePlus getMap(int i) {
        return this.distanceMap2d.get(i);
    }

    public ArrayList<ArrayList<Number>> getDistance(ArrayList<MicroObject> objects, ImagePlus imp) {

        ArrayList<ArrayList<Number>> al = new ArrayList<ArrayList<Number>>();

        ArrayList<Number> result = new ArrayList<Number>();

        ListIterator<MicroObject> itr = objects.listIterator();

        while (itr.hasNext()) {

            MicroObject object = itr.next();

            //System.out.println("PROFILING: map value: " + imp.getProcessor().getPixel((int)object.getCentroidX(), (int)object.getCentroidY()));
            result.add(imp.getProcessor().getPixel((int) object.getCentroidX(), (int) object.getCentroidY()));

        }
        al.add(result);

        return al;
    }

//    public void process(ArrayList<MicroObject> objects){
//        
//        ArrayList<Number> result = new ArrayList<Number>();
//        
//        ListIterator<ImagePlus> itr = distanceMap2d.listIterator();
//        int i = 0;
//        while(itr.hasNext()){         
//            ImagePlus imp = itr.next();
//            getDistance(objects, imp, map2dName.get(i));
//            i++;
//        }
//       
//    }
    public void addFeatureListener(AddFeaturesListener listener) {
        addfeaturelistener.add(listener);
    }

    public void notifyaddFeatureListeners(String name, ArrayList<ArrayList<Number>> al) {
        for (AddFeaturesListener listener : addfeaturelistener) {
            listener.addFeatures(name, al);
        }
    }

    public void addDistanceFeatures() {

    }

    public ArrayList<String> getMapNames() {
        return this.map2dName;
    }

}
