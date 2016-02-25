/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MicroDeprecated;

import vteaobjects.layercake.LayerCake3D;
import ij.IJ;
import ij.ImageStack;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import vteaexploration.Datasets;

/**
 *
 * @author vinfrais
 */
public class SimpleThresholdDataModel implements Datasets {

    private List Volumes;
    //for keeping track of the analyzed data, [main object, 2nd, etc]
    private ArrayList ResultsPointers;

    public SimpleThresholdDataModel() {
    }

    public SimpleThresholdDataModel(ImageStack[] is, List details) {

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

        LayerCake3D builderRegions;
        LayerCake3D builderVolumes;
        //make builder with all the detectable regions
        
        builderRegions = new LayerCake3D(is[Integer.parseInt(alprimary.get(0).toString())], minConstants, false);
        //new Thread(builderRegions).start();
        //make builder with all the volumes from the detectable regions
        //builderVolumes = new RegionFactory(builderRegions.getRegions(), builderRegions.getRegionsCount(), minConstants, is[0].getSize(), alsecondary);
        builderVolumes = new LayerCake3D(builderRegions.getRegions(), minConstants, new ImageStack());
         //new Thread(builderVolumes).start();
        ListIterator itr = alsecondary.listIterator();

        //derived = new Object[3];
        while (itr.hasNext()) {
            Object[] derived = ((ArrayList) itr.next()).toArray();
            derivedRegionType[(Integer) derived[0]][0] = Integer.parseInt(derived[1].toString());
            derivedRegionType[(Integer) derived[0]][1] = Integer.parseInt(derived[2].toString());
        }

        builderVolumes.makeDerivedRegionsThreading(derivedRegionType, is.length, is, getResultsPointers(details));

        //IJ.log("SimpleThresholdModel, derived regions: " + derivedRegionType);

        //derivedRegionType[][], [Channel][0, type, 1, subtype];
        //int[][] derivedRegionType = new int[3][2];
        Volumes = builderVolumes.getVolumesAsList();
        //get objects as tabulated data
    }

    private int getAnalysisTypeInt(String type) {
        if (type == null) {
            return 2;
        }
        if (type.equals("Mask")) {
            return 0;
        }
        if (type.equals("Grow")) {
            return 1;
        }
        if (type.equals("Fill")) {
            return 2;
        } else {
            return 0;
        }
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
    public List getObjects() {
        return Volumes;
    }

    @Override
    public List getColumn(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
