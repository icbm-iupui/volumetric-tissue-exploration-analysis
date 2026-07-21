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
package vteaobjects;

import ij.ImageStack;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author vinfrais
 */
public class MicroObject implements Serializable, MicroObjectModel {

    //base pixel positions
    protected int[] x;
    protected int[] y;
    protected int[] z;

    //morphological associations
    protected ArrayList<int[]> derivedX = new ArrayList();
    protected ArrayList<int[]> derivedY = new ArrayList();
    protected ArrayList<int[]> derivedZ = new ArrayList();

    protected HashMap<String, Integer> derivedkey;

    protected int derivedCount;

    ConcurrentHashMap<String, Integer> morphologicalLookup = new ConcurrentHashMap();

    //new way
    ArrayList<ArrayList<Number>> features = new ArrayList();
    ConcurrentHashMap<Integer, String> measurementLookup = new ConcurrentHashMap();

    // Property storage for metadata (used in chunked processing)
    private HashMap<String, Object> properties = new HashMap<String, Object>();

    //private Color[][] Colorized = new Color[4][9];
    private ArrayList ResultsPointer;

    private float centroid_x = 0;
    private float centroid_y = 0;
    private float centroid_z = 0;

    private int max_Z;
    private int min_Z;

    private double serialID;

    private int xLimit;
    private int yLimit;
    private int zLimit;

    private int padding = 3;

    //private int[][][] theBox;
    private boolean gated = false;

    private int color = 0;

    public MicroObject() {
    }

    /**
     * Constructor with pixel coordinates as ArrayList
     * @param pixels ArrayList containing x, y, z coordinate arrays
     * @param maskChannel The mask channel (currently unused, reserved for future use)
     * @param is ImageStack array containing the image data
     * @param serialID Serial identifier for this object
     */
    public MicroObject(ArrayList<int[]> pixels, int maskChannel, ImageStack[] is, int serialID) {
        this.serialID = serialID;
        xLimit = is[0].getWidth();
        yLimit = is[0].getHeight();
        zLimit = is[0].getSize();

        derivedCount = 1;

        setPixels(pixels);

        ArrayList<Number> centroid = new ArrayList<Number>();

        centroid = getCentroid(x, y, z);

        max_Z = this.setMaxZ();
        min_Z = this.setMinZ();

        centroid_x = ((Number) centroid.get(0)).floatValue();
        centroid_y = ((Number) centroid.get(1)).floatValue();
        centroid_z = ((Number) centroid.get(2)).floatValue();

        max_Z = this.setMaxZ();
        min_Z = this.setMinZ();

    }

    /**
     * Constructor with pixel coordinates as separate arrays
     * This constructor is used by chunked segmentation methods.
     * @param x Array of x coordinates
     * @param y Array of y coordinates
     * @param z Array of z coordinates
     * @param channel The channel (currently unused, reserved for future use)
     * @param is ImageStack array containing the image data
     * @param serialID Serial identifier for this object
     */
    public MicroObject(int[] x, int[] y, int[] z, int channel, ImageStack[] is, int serialID) {
        this.serialID = serialID;
        this.x = x;
        this.y = y;
        this.z = z;

        if (is != null && is.length > 0) {
            xLimit = is[0].getWidth();
            yLimit = is[0].getHeight();
            zLimit = is[0].getSize();
        }

        derivedCount = 1;

        ArrayList<Number> centroid = new ArrayList<Number>();
        centroid = getCentroid(x, y, z);

        max_Z = this.setMaxZ();
        min_Z = this.setMinZ();

        centroid_x = ((Number) centroid.get(0)).floatValue();
        centroid_y = ((Number) centroid.get(1)).floatValue();
        centroid_z = ((Number) centroid.get(2)).floatValue();

        max_Z = this.setMaxZ();
        min_Z = this.setMinZ();
    }

    public void setCentroid() {
        ArrayList<Number> centroid = new ArrayList<Number>();

        centroid = getCentroid(x, y, z);

        centroid_x = ((Number) centroid.get(0)).floatValue();
        centroid_y = ((Number) centroid.get(1)).floatValue();
        centroid_z = ((Number) centroid.get(2)).floatValue();
    }

    private ArrayList<Number> getCentroid(int[] x, int[] y, int[] z) {

        int maxX = 0;
        int maxY = 0;
        int maxZ = 0;

        for (int i = 0; i < x.length; i++) {

            if (x[i] > maxX) {
                maxX = x[i];
            }
            if (y[i] > maxY) {
                maxY = y[i];
            }
            if (z[i] > maxZ) {
                maxZ = z[i];
            }

        }

        int minX = maxX;
        int minY = maxY;
        int minZ = maxZ;

        for (int i = 0; i < x.length; i++) {

            if (x[i] < minX) {
                minX = x[i];
            }
            if (y[i] < minY) {
                minY = y[i];
            }
            if (z[i] < minZ) {
                minZ = z[i];
            }
        }

        double centX = ((maxX - minX) / 2.0) + minX;
        double centY = ((maxY - minY) / 2.0) + minY;
        double centZ = ((maxZ - minZ) / 2.0) + minZ;

        ArrayList<Number> result = new ArrayList<Number>();

        result.add(centX);
        result.add(centY);
        result.add(centZ);

        return result;
    }

    private boolean containsPixel(int[] xVal, int[] yVal, int[] zVal, int x, int y, int z) {
        //System.out.println("Checking object voxels!");
        for (int i = 0; i < xVal.length; i++) {
            if (xVal[i] == x) {
                for (int j = 0; j < yVal.length; j++) {
                    if (yVal[i] == y) {
                        for (int k = 0; k < zVal.length; k++) {
                            if (xVal[i] == x && yVal[j] == y && zVal[k] == z) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private ArrayList<ArrayList> dilatefill3D(ArrayList<ArrayList> al, int x, int y, int z, int width, int height, int size) {

        if ((containsPixel(this.x, this.y, this.z, x, y, z))) {
            return al;
        }
        al.get(0).add(x);
        al.get(1).add(y);
        al.get(2).add(z);
        return al;
    }

    private void setPixels(ArrayList<int[]> pixels) {
        x = pixels.get(0);
        y = pixels.get(1);
        z = pixels.get(2);
    }

    private int getMax(int[] vals) {
        int max = 0;
        for (int i = 0; i < vals.length; i++) {
            if (vals[i] > max) {
                max = vals[i];
            }
        }
        return max;
    }

    private int getMin(int[] vals) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < vals.length; i++) {
            if (vals[i] < min) {
                min = vals[i];
            }
        }
        return min;
    }

    public int getRange(int dim) {
        int[] vals;
        switch (dim) {
            case 0:
                vals = x;
                break;
            case 1:
                vals = y;
                break;
            case 2:
                vals = z;
            default:
                vals = new int[0];

        }

        int min = getMin(vals);
        int max = getMax(vals);
        return max - min;
    }

    @Override
    public int[] getPixelsX() {
        return this.x;
    }

    @Override
    public int[] getPixelsY() {
        return this.y;
    }

    @Override
    public int[] getPixelsZ() {
        return this.z;
    }

    public void setPixelsX(int[] d) {
        this.x = new int[d.length];
        this.x = d;
    }

    public void setPixelsY(int[] d) {
        this.y = new int[d.length];
        this.y = d;
    }

    public void setPixelsZ(int[] d) {
        this.z = new int[d.length];
        this.z = d;
    }

    @Override
    public float getCentroidX() {
        return this.centroid_x;
    }

    @Override
    public float getCentroidY() {
        return this.centroid_y;
    }

    @Override
    public float getCentroidZ() {
        return this.centroid_z;
    }

    public int setMinZ() {
        int min = 70000;
        for (int i = 0; i < z.length; i++) {
            if (z[i] < min) {
                min = z[i];
            }
        }
        min_Z = min;
        return min;
    }

    public int setMaxZ() {
        int max = 0;
        for (int i = 0; i < z.length; i++) {
            if (z[i] > max) {
                max = z[i];
            }
        }
        max_Z = max;
        return max;
    }

    public int getMinZ() {
        return min_Z;
    }

    public int getMaxZ() {
        return max_Z;
    }

    public int getEquivalentMorphology(String str) {

        return -1;
    }

    @Override
    public ArrayList getObjectPixels() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList getResultPointer() {
        return this.ResultsPointer;
    }

    @Override
    public double getSerialID() {
        return this.serialID;
    }

    @Override
    public void setSerialID(int i) {
        this.serialID = i;
    }

    @Override
    public List getRegions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int[] getXPixelsInRegion(int i) {
        int[] al_x = new int[z.length];
        int counter = 0;
        for (int j = 0; j < z.length; j++) {
            if (z[j] == i) {
                al_x[counter] = x[j];
                counter++;
            }
        }
        int[] export_x = new int[counter];
        for (int k = 0; k < counter; k++) {
            export_x[k] = al_x[k];
        }
        return export_x;
    }

    @Override
    public int[] getYPixelsInRegion(int i) {
        int[] al_y = new int[z.length];
        int counter = 0;
        for (int j = 0; j < z.length; j++) {
            if (z[j] == i) {
                al_y[counter] = y[j];
                counter++;
            }
        }
        int[] export_y = new int[counter];
        for (int k = 0; k < counter; k++) {
            export_y[k] = al_y[k];
        }
        return export_y;
    }

    @Override
    public void setGated(boolean b) {
        gated = b;
    }

    @Override
    public boolean getGated() {
        return gated;
    }

    @Override
    public void setColor(int c) {
        color = c;
    }

    @Override
    public int getColor() {
        return color;
    }

    public int checkMorphological(String UID) {

        if (morphologicalLookup.containsKey(UID)) {

            return morphologicalLookup.get(UID);
        } else {
            return -1;
        }
    }

    public void setMorphological(String method_UID, int[] x, int[] y, int[] z) {

        morphologicalLookup.put(method_UID, derivedX.size());

        derivedX.add(x);
        derivedY.add(y);
        derivedZ.add(z);

    }

    public int getMorphologicalCount() {
        return derivedX.size();
    }

    @Override
    public ArrayList<int[]> getMorphological(int index) {

        ArrayList<int[]> al = new ArrayList();

        al.add(derivedX.get(index));
        al.add(derivedY.get(index));
        al.add(derivedZ.get(index));

        return al;
    }

    @Override
    public int[] getMorphPixelsX(int index) {

        return derivedX.get(index);

    }

    @Override
    public int[] getMorphPixelsY(int index) {

        return derivedY.get(index);

    }

    @Override
    public int[] getMorphPixelsZ(int index) {

        return derivedZ.get(index);

    }

    @Override
    public int getPixelCount() {
        if (x == null) {
            return 0;
        }
        return x.length;
    }

    /**
     * Get the number of voxels/pixels in this object
     * @return The number of voxels
     */
    public int getVoxelCount() {
        if (x == null) {
            return 0;
        }
        return x.length;
    }

    @Override
    public double getThresholdedIntegratedIntensity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getThresholdedMeanIntensity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setThreshold(double threshold) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    

    @Override
    public Rectangle getBoundingRectangle() {

    int x_max = 0;
    for (int i = 0; i < x.length; i++) {
        if (x[i] > x_max) {
            x_max = x[i];
        }
    }
    int y_max = 0;
    for (int i = 0; i < y.length; i++) {
        if (y[i] > y_max) {
            y_max = y[i];
        }
    }

    int x_min = x_max;
    for (int i = 0; i < x.length; i++) {
        if (x[i] < x_min) {
            x_min = x[i];
        }
    }
    int y_min = y_max;
    for (int i = 0; i < y.length; i++) {
        if (y[i] < y_min) {
            y_min = y[i];
        }
    }

    int width = x_max-x_min;
    int height = y_max-y_min;

    int xStart = (int)this.getCentroidX()-(width/2);
    int yStart = (int)this.getCentroidY()-(height/2);

    return new Rectangle(xStart,yStart,width,height);

    }

    /**
     * Get centroid coordinates as a double array
     * Used for coordinate transformations in chunked processing
     * @return Array containing [x, y, z] centroid coordinates as doubles
     */
    public double[] getCentroidXYZ_AsDbl() {
        return new double[]{(double) centroid_x, (double) centroid_y, (double) centroid_z};
    }

    /**
     * Set centroid coordinates from a double array
     * Used for coordinate transformations in chunked processing
     * @param centroid Array containing [x, y, z] centroid coordinates
     */
    public void setCentroidXYZ(double[] centroid) {
        if (centroid != null && centroid.length >= 3) {
            this.centroid_x = (float) centroid[0];
            this.centroid_y = (float) centroid[1];
            this.centroid_z = (float) centroid[2];
        }
    }

    /**
     * Set a property value for this object
     * Properties are used to store metadata such as chunk information, boundary status, etc.
     * @param key Property name
     * @param value Property value
     */
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    /**
     * Get a property value for this object
     * @param key Property name
     * @return Property value, or null if not found
     */
    public Object getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Check if a property exists
     * @param key Property name
     * @return true if the property exists
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    /**
     * Remove a property
     * @param key Property name
     * @return The previous value associated with the key, or null
     */
    public Object removeProperty(String key) {
        return properties.remove(key);
    }

}
