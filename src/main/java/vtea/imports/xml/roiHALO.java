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
package vtea.imports.xml;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import static ij.gui.Roi.POLYGON;
import ij.plugin.frame.RoiManager;
import java.io.File;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import vtea._vtea;
import vtea.imports.xml.Annotations.Annotation;
import vtea.imports.xml.Annotations.Annotation.Regions;
import vtea.imports.xml.Annotations.Annotation.Regions.Region;
import vtea.imports.xml.Annotations.Annotation.Regions.Region.Vertices;
import vtea.imports.xml.Annotations.Annotation.Regions.Region.Vertices.V;

/**
 *
 * @author Seth
 */
public class roiHALO {

    RoiManager manager = RoiManager.getRoiManager();
    Annotations an = new Annotations();
    List annotations;
    
   

    public roiHALO(File source) {

        
        Persister persister = new Persister();
        
        
        
        try {
            an = persister.read(Annotations.class, source);
            annotationsParser();
        } catch (Exception ex) {
            System.out.println("ERROR: Parsing failed." + ex.getLocalizedMessage());
            
            StackTraceElement[] error = ex.getStackTrace();
            
            for(int i = 0; i < error.length; i++){
                System.out.println("    trace: " + error[i].toString());
            }
            
            
        }

    }
    


    private void annotationsParser() {
        List annotations = an.getAnnotations();
        ListIterator<Annotation> itr = annotations.listIterator();
        
        while(itr.hasNext()){
            Annotation annotation = itr.next();
            parseRegions(annotation);
        }
    }
    
    
    private void parseRegions(Annotation annotation) {
        Regions regions = annotation.getRegions();
        List regionList = regions.getRegions();
        ListIterator<Region> itr = regionList.listIterator();
        int i = 0;
        
        while(itr.hasNext()){
            
            Region region = itr.next();
            parseVertices(annotation.getName()+"_"+i, region.getVertices());

            i++;
        }
    }
    
    private void parseVertices(String name, Vertices v) {

        List vertices = v.getVertices();
        
        float[] pointX = new float[vertices.size()];
        float[] pointY = new float[vertices.size()];
        
        ListIterator<V> itr = vertices.listIterator();
        V point;
        int i = 0;
        
        while(itr.hasNext()){
            point = itr.next();
            pointX[i] = Float.parseFloat(point.getX());
            pointY[i] = Float.parseFloat(point.getY());
            i++;
        }
        
        if(pointX.length > 0){
            PolygonRoi pr = new PolygonRoi(pointX, pointY, Roi.POLYGON);
            pr.setName(name);
            manager = RoiManager.getRoiManager();
            manager.addRoi(pr);
        } else {
            System.out.println("ERROR: Vertices empty...  Skipping...");
        }
    }
    
    private void parseRectangle(Vertices v) {
        System.out.println("ERROR: Importing rectangles is not supported...  Skipping...");
    }
    
    private void parseEllipse(Vertices v) {
        System.out.println("ERROR: Importing ellipses is not supported...  Skipping...");
    }
} 
    

