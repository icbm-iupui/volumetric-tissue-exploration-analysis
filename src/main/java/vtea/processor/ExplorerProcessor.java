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
package vtea.processor;

import ij.ImagePlus;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.table.AbstractTableModel;
import org.scijava.plugin.Plugin;
import vtea.exploration.plottools.panels.DefaultPlotPanels;
import vtea.exploration.plottools.panels.XYExplorationPanel;
import vtea.jdbc.H2DatabaseEngine;
import vteaexploration.MicroExplorer;
import vteaobjects.MicroObject;

/**
 *
 * @author sethwinfree
 */
@Plugin(type = Processor.class)
public class ExplorerProcessor extends AbstractProcessor {

    ImagePlus impOriginal;
    ArrayList protocol;  //we may want to turn this into a class...

    private ArrayList descriptions;
    private ArrayList descriptionLabels;
    private ArrayList measurements;
    private ArrayList objects;
    private ArrayList morphologies;

    private String parentKey;

    private ArrayList plotValues;

    public ExplorerProcessor() {

        VERSION = "0.0";
        AUTHOR = "Seth Winfree";
        COMMENT = "Processor for explorer window.";
        NAME = "Explorer Processor";
        KEY = "ExplorerProcessor";

    }

    /*this constructor should change to:
    
    public ExplorerProcessor(ImagePlus imp, ArrayList volumes, ArrayList measurements)
    
    once SegmentationProcessor exists on its own.
    
     */
    public <T extends MicroObject> ExplorerProcessor(String k, String parentk, ImagePlus imp, ArrayList<T> volumes, ArrayList measurements, ArrayList headers, ArrayList headerLabels, ArrayList m) {

        VERSION = "0.0";
        AUTHOR = "Seth Winfree";
        COMMENT = "Processor for explorer window.";
        NAME = "Explorer Processor";
        KEY = "ExplorerProcessor";

        impOriginal = imp;
        objects = volumes;
        this.measurements = measurements;
        descriptions = headers;
        descriptionLabels = headerLabels;
        key = k;
        parentKey = parentk;
        morphologies = m;

    }

    @Override
    protected Void doInBackground() throws Exception {

        int progress = 0;

        if (objects.size() > 0) {

            try {

                firePropertyChange("progress", 0, 5);
                firePropertyChange("comment", "", "Starting explorer processing on " + objects.size() + " objects...");

                HashMap<Integer, String> hm = new HashMap<Integer, String>();

                for (int i = 0; i < descriptions.size(); i++) {
                    hm.put(i, descriptions.get(i).toString());
                }

                Connection connection = H2DatabaseEngine.getDBConnection();
                
                

                System.out.println("PROFILING: Exploring on dataset: " + key);

                String title = "Segmentation_" + (impOriginal.getTitle().replace("DUP_", "")).replace(".tif", "");

                XYExplorationPanel XY = new XYExplorationPanel(key, connection, measurements, descriptions, hm, objects, title);
                DefaultPlotPanels DPP = new DefaultPlotPanels();
                MicroExplorer explorer = new MicroExplorer();

                explorer.setTitle("Explorer: " + title);
                impOriginal.deleteRoi();
                explorer.process(key, impOriginal, title, measurements, XY, DPP, descriptions, descriptionLabels, morphologies);
                XY.updateMenuPositions(explorer.getX(), explorer.getY() + explorer.getHeight());
                setProgress(100);
                firePropertyChange("comment", "", "Done.");
                
//                ArrayList tables = H2DatabaseEngine.getListOfTables(connection);
//                if(tables.size() > 0){
//                    for(int i = 0; i < tables.size(); i++){
//                    System.out.println("PROFILING: Table: " + i + ", name:" + tables.get(i));
//                    }}
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public int process(ArrayList al, String... str) {

        return 1;
    }

    @Override
    public String getChange() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

