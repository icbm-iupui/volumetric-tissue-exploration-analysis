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
package vteaobjects.layercake;

import ij.IJ;
import vteaexploration.Datasets;

import vteaobjects.MicroObjectModel;
import ij.ImageStack;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import vteaobjects.floodfill3D.FloodFill3D;
import vteaobjects.layercake.LayerCake3D;

/**
 *
 * @author vinfrais
 */
public class SingleThresholdDataModel implements Datasets{

    private ArrayList Volumes;
    private ArrayList Volumes3D;
    //for keeping track of the analyzed data, [main object, 2nd, etc]
    private ArrayList ResultsPointers;
    private LayerCake3D builderRegions;
    private LayerCake3D builderVolumes;
    private FloodFill3D builder3DVolumes;
    
    public SingleThresholdDataModel() {
    }
    
    public SingleThresholdDataModel(ImageStack[] is, List details, int method, boolean calculate) {
        
        if(method == 1){
            this.processDataLayerCake(is, details, calculate);
        }else if (method == 2){
            this.processData3DFloodFill(is, details, calculate);
        }
        }    
 
    
    public void processDataLayerCake(ImageStack[] is, List details, boolean calculate) {
        //takes a stack and details for object definiton as defined by details
        //Details incluldes:
        //channel, segmentation_key as integer should use a centralized system, Arraylist with fields descriptors, field1, field2, field3...
        //field descriptors acts as a key for the fields.
        int[] minConstants = new int[4];
	// 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold

        //derivedRegionType[][], [Channel][0, type, 1, subtype];
        int[][] derivedRegionType = new int[is.length][2];

        final List alprimary = (ArrayList) details.get(0);

        final List alsecondary = details.subList(1, details.size());

        final List fieldnames = (List) alprimary.get(2);

        minConstants[3] = Integer.parseInt(alprimary.get(fieldnames.indexOf("minThreshold") + 3).toString());
        minConstants[1] = Integer.parseInt(alprimary.get(fieldnames.indexOf("maxObjectSize") + 3).toString());
        minConstants[2] = Integer.parseInt(alprimary.get(fieldnames.indexOf("minOverlap") + 3).toString());
        minConstants[0] = Integer.parseInt(alprimary.get(fieldnames.indexOf("minObjectSize") + 3).toString());

        long start = System.nanoTime();
        
        
        //make builder with all the detectable regions   
        System.out.println("PROFILING: ImageStack size: " + is[Integer.parseInt(alprimary.get(0).toString())].getSize() + " slices.");
        IJ.log("PROFILING: ImageStack size: " + is[Integer.parseInt(alprimary.get(0).toString())].getSize() + " slices.");
        
        builderRegions = new LayerCake3D(is[Integer.parseInt(alprimary.get(0).toString())], minConstants, false);
        long end = System.nanoTime();
        System.out.println("PROFILING: Region find time: " + ((end-start)/1000000) + " ms. " + "Found " + builderRegions.getRegionsCount() + " regions.");
        IJ.log("PROFILING: Region find time: " + ((end-start)/1000000) + " ms. " + "Found " + builderRegions.getRegionsCount() + " regions.");
        //make builder with all the volumes from the detectable regions
        start = System.nanoTime();
        builderVolumes = new LayerCake3D(builderRegions.getRegions(), minConstants, is[Integer.parseInt(alprimary.get(0).toString())]);
        end = System.nanoTime();
        System.out.println("PROFILING: Volume build time: " + ((end-start)/1000000) + " ms. " + "Made " + builderVolumes.getVolumesCount() + " volumes.");
        IJ.log("PROFILING: Volume build time: " + ((end-start)/1000000) + " ms. " + "Made " + builderVolumes.getVolumesCount() + " volumes.");
        
        ListIterator itr = alsecondary.listIterator();
        while (itr.hasNext()) {
            Object[] derived = ((ArrayList) itr.next()).toArray();
            derivedRegionType[(Integer) derived[0]][0] = Integer.parseInt(derived[1].toString());
            derivedRegionType[(Integer) derived[0]][1] = Integer.parseInt(derived[2].toString());
        }
        
        start = System.nanoTime(); 
        //builderVolumes.makeDerivedRegionsThreading(derivedRegionType, is.length, is, getResultsPointers(details));
        if(calculate){
        builderVolumes.makeDerivedRegionsPool(derivedRegionType, is.length, is, getResultsPointers(details));
        }
        end = System.nanoTime();
        System.out.println("PROFILING: Derived region time: " + ((end-start)/1000000) + " ms. " + "Made " + builderVolumes.getRegionsCount() + " regions.");
        IJ.log("PROFILING: Derived region time: " + ((end-start)/1000000) + " ms. " + "Made " + builderVolumes.getRegionsCount() + " regions.");
        Volumes = builderVolumes.getVolumesAsList();


    }
    
    public void processData3DFloodFill(ImageStack[] is, List details, boolean calculate) {
        //takes a stack and details for object definiton as defined by details
        //Details incluldes:
        //channel, segmentation_key as integer should use a centralized system, Arraylist with fields descriptors, field1, field2, field3...
        //field descriptors acts as a key for the fields.
        int[] minConstants = new int[4];
	// 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold

        //derivedRegionType[][], [Channel][0, type, 1, subtype];
        int[][] derivedRegionType = new int[is.length][2];

        final List alprimary = (ArrayList) details.get(0);

        final List alsecondary = details.subList(1, details.size());

        final List fieldnames = (List) alprimary.get(2);

        minConstants[3] = Integer.parseInt(alprimary.get(fieldnames.indexOf("minThreshold") + 3).toString());
        minConstants[1] = Integer.parseInt(alprimary.get(fieldnames.indexOf("maxObjectSize") + 3).toString());
        minConstants[2] = Integer.parseInt(alprimary.get(fieldnames.indexOf("minOverlap") + 3).toString());
        minConstants[0] = Integer.parseInt(alprimary.get(fieldnames.indexOf("minObjectSize") + 3).toString());

        long start = System.nanoTime();
        long end = System.nanoTime();
        
        //make builder with all the detectable regions   
        
        
        System.out.println("PROFILING: ImageStack size: " + is[Integer.parseInt(alprimary.get(0).toString())].getSize() + " slices.");
        IJ.log("PROFILING: ImageStack size: " + is[Integer.parseInt(alprimary.get(0).toString())].getSize() + " slices.");
        

        ListIterator itr = alsecondary.listIterator();
        while (itr.hasNext()) {
            Object[] derived = ((ArrayList) itr.next()).toArray();
            derivedRegionType[(Integer) derived[0]][0] = Integer.parseInt(derived[1].toString());
            derivedRegionType[(Integer) derived[0]][1] = Integer.parseInt(derived[2].toString());
        }


        start = System.nanoTime(); 
        builder3DVolumes = new FloodFill3D(is, Integer.parseInt(alprimary.get(0).toString()), minConstants, false);
        if(calculate){
        builder3DVolumes.makeDerivedRegionsPool(derivedRegionType, is.length, is, getResultsPointers(details));
        }
        end = System.nanoTime();
        System.out.println("PROFILING: 3D Floodfill time: " + ((end-start)/1000000) + " ms. ");
        IJ.log("PROFILING: 3D Floodfill time: " + ((end-start)/1000000) + " ms. ");
        
        Volumes = builder3DVolumes.getVolumesAsList();
        
        
    
    }

    private ArrayList getResultsPointers(List details) {

        ArrayList<Integer> result = new ArrayList();

        List alprimary = (ArrayList) details.get(0);
        List alsecondary = details.subList(1, details.size());

        result.add((Integer) alprimary.get(0));

        ListIterator itr = alsecondary.listIterator();

        while (itr.hasNext()) {
            result.add((Integer) ((ArrayList) itr.next()).get(0));
        }
        return result;
    }

    @Override
    public int getObjectCount() {
        return Volumes.size();
    }

    @Override
    public int getColumnCount() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] getColumnTitles() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList getObjects() {
        return Volumes;
    }

    @Override
    public List getColumn(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

        public ArrayList getObjects3D() {
        return Volumes3D;
    }


}
