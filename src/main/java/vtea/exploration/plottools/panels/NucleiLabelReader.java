/*
 * Copyright (C) 2019 SciJava
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
package vtea.exploration.plottools.panels;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageRoi;
import ij.gui.Roi;
import ij.gui.Overlay;
import ij.gui.TextRoi;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import vtea.exploration.plotgatetools.gates.Gate;
import vtea.jdbc.H2DatabaseEngine;
import vteaobjects.MicroObject;
import vteaobjects.MicroObjectModel;

/**
 *
 * @author andre
 */
public class NucleiLabelReader {
    ImagePlus image;
    protected ArrayList<MicroObject> objects = new ArrayList();
    protected ArrayList<ArrayList<Number>> measurements = new ArrayList();
    int size;
    int depth;
    int[] info;
    File csv;
    String csvpath; 
    double threshold;
    
    NucleiLabelReader(ImagePlus image, ArrayList objects , ArrayList measurements, File csv, ArrayList options){
        this.image = image;
        this.objects = objects;
        this.measurements = measurements;
        this.csv = csv;
        this.threshold = (double) options.get(0);
        
        
    }
    
    public void run(){
        List<String[]> allData = null;
        try { 
            
            // Create an object of filereader 
            // class with CSV file as a parameter. 
            FileReader filereader = new FileReader(csv); 
            
            // create csvReader object passing 
            // file reader as a parameter 
            CSVReader csvReader = new CSVReaderBuilder(filereader) 
                                  //.withSkipLines(1) //skip header
                                  .build(); 
            allData = csvReader.readAll();
            //String[] = code, percent class 1, percent class 2, etc.
            } 
        catch (Exception e) { 
            e.printStackTrace(); 
        } 
        
        ArrayList<MicroObject> volumes = (ArrayList) objects;
        ArrayList<Double> ids = new ArrayList<Double>();
        ArrayList<Integer> ids_int = new ArrayList<Integer>();
        for (MicroObject vol : volumes){
            ids.add(vol.getSerialID());
            int tp = (int) vol.getSerialID();
            ids_int.add(Integer.valueOf(tp));
        }
        System.out.println("Total objects in image: " + ids.size());
        //System.out.println("string serialID, int id, found index, found class");
        ArrayList<Color> mycolors = new ArrayList<Color>();
        mycolors.add(Color.magenta);
        mycolors.add(Color.cyan);
        ArrayList<MicroObject> matches = new ArrayList<MicroObject>();
        ArrayList<Integer> ids_pred = new ArrayList<Integer>();
        Overlay overlay1 = new Overlay();
        Overlay overlay2 = new Overlay();
        
        for (String[] s : allData){
            String id_s = s[0];
            double id_d = Double.parseDouble(id_s);
            Integer id_i = Integer.valueOf((int) id_d);
            double[] class_percent = {Double.parseDouble(s[1]),  Double.parseDouble(s[2])};
            //int idx = ids.indexOf(id_d); //use double serial id
            int idx = ids_int.indexOf(id_i); //use integers to find
            if (idx != -1){
                MicroObject vol_interest = volumes.get(idx);
              
                double max = 0;
                int max_idx = 0;
                for (int item = 0; item < class_percent.length; item++){
                    if (max < class_percent[item]){
                        max = class_percent[item];
                        max_idx = item;
                    }
                }
                if (max > threshold){
                   //System.out.println(id_s+ "      " + id_i + "     "+ vol_interest.getSerialID() + "    " + max_idx);
                   ids_pred.add(max_idx); 
                   matches.add(vol_interest); 
                }
            }    
        }
        System.out.println("Number of matches found: " + matches.size());
        System.out.println("Number of predictions in csv: " + allData.size());
        makeOverlayImageCNN(matches, ids_pred, mycolors);
        //image.setOverlay(overlay1);
        //image.setOverlay(overlay2);
        
    }
    
    public void makeOverlayImageCNN(ArrayList<MicroObject> volumes, ArrayList<Integer> ids_pred, ArrayList<Color> mycolors) {
        //convert gate to chart x,y path
                ArrayList<MicroObject> result = volumes;

                Overlay overlay = new Overlay();

                int count = 0;

                BufferedImage placeholder = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

                ImageStack gateOverlay = new ImageStack(image.getWidth(), image.getHeight());

                int selected = result.size();

                int total = volumes.size();

                int gated = selected;
                int gatedSelected = selected;

                Collections.sort(result, new ZComparator());

                for (int i = 0; i <= image.getNSlices(); i++) {

                    BufferedImage selections = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

                    Graphics2D g2 = selections.createGraphics();

                    ImageRoi ir = new ImageRoi(0, 0, placeholder);

                    ListIterator<MicroObject> vitr = result.listIterator();
                    ListIterator<Integer> intitr = ids_pred.listIterator();

                    boolean inZ = true;

                    while (vitr.hasNext() && inZ) {

                        MicroObject vol = (MicroObject) vitr.next();
                        int class_id = intitr.next();
                        inZ = true;

                        //
                        if (i >= vol.getMinZ() && i <= vol.getMaxZ()) {
                            inZ = false;
                        }
                        //

                        try {
                            int[] x_pixels = vol.getXPixelsInRegion(i);
                            int[] y_pixels = vol.getYPixelsInRegion(i);

                            for (int c = 0; c < x_pixels.length; c++) {
                                g2.setColor(mycolors.get(class_id));
//                                if (class_id == 0){ //endo
//                                    g2.setColor(Color.CYAN);
//                                }
//                                if (class_id == 1){ //pct
//                                    g2.setColor(Color.magenta);
//                                }
//                                if (class_id ==0){
//                                    g2.drawRect(x_pixels[c], y_pixels[c], 1, 1);
//                                }    
                                g2.drawRect(x_pixels[c], y_pixels[c], 1, 1);
                            }
                            ir = new ImageRoi(0, 0, selections);
                            count++;

                        } catch (NullPointerException e) {
                        }
                    }

                    ir.setPosition(0, i + 1, 0);

                    //old setPosition not functional as of imageJ 1.5m
                    ir.setOpacity(0.4);
                    overlay.selectable(false);
                    overlay.add(ir);

                    gateOverlay.addSlice(ir.getProcessor());
                    java.awt.Font f = new Font("Arial", Font.BOLD, 12);
                    TextRoi textTotal = new TextRoi(5, 10, "Endothelium: cyan, PCT: magenta");
                    overlay.add(textTotal);

                    
                image.setOverlay(overlay);

                //gate.setGateOverlayStack(gateOverlay);

            }

            image.draw();

            if (image.getDisplayMode() != IJ.COMPOSITE) {
                image.setDisplayMode(IJ.COMPOSITE);
            }

            if (image.getSlice() == 1) {
                image.setZ(Math.round(image.getNSlices() / 2));
            } else {
                image.setSlice(image.getSlice());
            }
            image.show();
        }
}
    
