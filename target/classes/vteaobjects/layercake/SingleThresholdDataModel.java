/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vteaobjects.layercake;

import ij.IJ;
import vteaexploration.Datasets;

import vteaobjects.MicroObjectModel;
import ij.ImageStack;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author vinfrais
 */
public class SingleThresholdDataModel implements Datasets, MicroObjectModel{

    private ArrayList Volumes;
    //for keeping track of the analyzed data, [main object, 2nd, etc]
    private ArrayList ResultsPointers;
    private RegionFactory builderRegions;
    private RegionFactory builderVolumes;
    
    public SingleThresholdDataModel() {
    }
    
    public SingleThresholdDataModel(ImageStack[] is, List details) {
        
        //takes a stack and details for object definiton as defined by details
        //Details incluldes:
        //channel, segmentation_key as integer should use a centralized system, Arraylist with fields descriptors, field1, field2, field3...
        //field descriptors acts as a key for the fields.
        int[] minConstants = new int[4];
	// 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold

        //derivedRegionType[][], [Channel][0, type, 1, subtype];
        int[][] derivedRegionType = new int[is.length][2];

        final List alprimary = (ArrayList) details.get(0);

        //IJ.log("SimpleThresholdDataModel, primary: " + alprimary);

        final List alsecondary = details.subList(1, details.size());

        //IJ.log("SimpleThresholdDataModel, secondary: " + alsecondary);
        

        //make ResultsPointer
        //ResultsPointers = 
        final List fieldnames = (List) alprimary.get(2);
        //IJ.log("SimpleThresholdDataModel, field names: " + fieldnames);

        //IJ.log("SimpleThresholdModel, minthreshold: " + alprimary.get((Integer) fieldnames.indexOf("minObjectSize") + 3));
        
       

        minConstants[3] = Integer.parseInt(alprimary.get(fieldnames.indexOf("minThreshold") + 3).toString());
        minConstants[1] = Integer.parseInt(alprimary.get(fieldnames.indexOf("maxObjectSize") + 3).toString());
        minConstants[2] = Integer.parseInt(alprimary.get(fieldnames.indexOf("minOverlap") + 3).toString());
        minConstants[0] = Integer.parseInt(alprimary.get(fieldnames.indexOf("minObjectSize") + 3).toString());

        long start = System.nanoTime();
        //make builder with all the detectable regions   
        System.out.println("PROFILING: ImageStack size: " + is[Integer.parseInt(alprimary.get(0).toString())].getSize() + " slices.");
        IJ.log("PROFILING: ImageStack size: " + is[Integer.parseInt(alprimary.get(0).toString())].getSize() + " slices.");
        
        builderRegions = new RegionFactory(is[Integer.parseInt(alprimary.get(0).toString())], minConstants, false);
        long end = System.nanoTime();
        System.out.println("PROFILING: Region find time: " + ((end-start)/1000000) + " ms. " + "Found " + builderRegions.getRegionsCount() + " regions.");
        IJ.log("PROFILING: Region find time: " + ((end-start)/1000000) + " ms. " + "Found " + builderRegions.getRegionsCount() + " regions.");
        //make builder with all the volumes from the detectable regions
        start = System.nanoTime();
        builderVolumes = new RegionFactory(builderRegions.getRegions(), minConstants, is[Integer.parseInt(alprimary.get(0).toString())]);
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
        builderVolumes.makeDerivedRegionsPool(derivedRegionType, is.length, is, getResultsPointers(details));
        end = System.nanoTime();
        System.out.println("PROFILING: Derived region time: " + ((end-start)/1000000) + " ms. " + "Made " + builderVolumes.getRegionsCount() + " regions.");
        IJ.log("PROFILING: Derived region time: " + ((end-start)/1000000) + " ms. " + "Made " + builderVolumes.getRegionsCount() + " regions.");
        Volumes = builderVolumes.getVolumesAsList();
    
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

}
