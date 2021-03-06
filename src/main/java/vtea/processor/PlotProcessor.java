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

import java.awt.Component;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import org.scijava.plugin.Plugin;
import vtea.jdbc.H2DatabaseEngine;
import vtea.plotprocessing.AbstractPlotMaker;

/**
 *
 * @author sethwinfree
 */
@Plugin(type = Processor.class)
public class PlotProcessor extends AbstractProcessor {

    private ArrayList<String> descriptions;
    private ArrayList<String> descriptionLabels;
    private ArrayList measurements;
    private ArrayList objects;

    private ArrayList<String> features;

    private String parentKey;
    private String plotType;
    private String feature;
    private String group;
    private ArrayList<Component> specialized;
    private boolean exportPDF;
    private String filename;
    private String filenameDestination;

    public PlotProcessor() {

        VERSION = "0.0";
        AUTHOR = "Seth Winfree";
        COMMENT = "Processor for generating plots.";
        NAME = "Plot Processor";
        KEY = "PlotProcessor";

    }

    public <T extends String> PlotProcessor(ArrayList<T> settings) {

        VERSION = "0.0";
        AUTHOR = "Seth Winfree";
        COMMENT = "Processor for generating plots.";
        NAME = "Plot Processor";
        KEY = "PlotProcessor";

    }

    public <T extends String> PlotProcessor(String key, ArrayList<String> settings, ArrayList<String> features) {

        VERSION = "0.0";
        AUTHOR = "Seth Winfree";
        COMMENT = "Processor for generating plots.";
        NAME = "Plot Processor";
        KEY = "PlotProcessor";

        parentKey = key;
        plotType = settings.get(0);
        group = settings.get(1);
        //sortOrder = settings.get(2);
        //fileName = settings.get(3);
        this.features = features;
        this.exportPDF = false;
    }

    public <T extends String> PlotProcessor(String key, ArrayList<String> settings, ArrayList<Component> special, ArrayList<String> features, boolean exportPDF) {

        VERSION = "0.0";
        AUTHOR = "Seth Winfree";
        COMMENT = "Processor for generating plots.";
        NAME = "Plot Processor";
        KEY = "PlotProcessor";

        parentKey = key;
        plotType = settings.get(0);
        group = settings.get(1);
        filename = settings.get(2);
        filenameDestination = settings.get(3);
        //filenameDestination = settings.get(3);
        specialized = special;
        this.features = features;
        this.exportPDF = exportPDF;
    }

    @Override
    protected Void doInBackground() throws Exception {

        if (exportPDF) {
            try {
                firePropertyChange("progress", 0, 5);
                firePropertyChange("comment", "", "Generating "
                        + plotType);

                Class<?> c;
                c = Class.forName("vtea.plotprocessing." + plotType);
                Constructor<?> con;

                Object iImp = new Object();

                con = c.getConstructor();
                iImp = con.newInstance();

                ((AbstractPlotMaker) iImp).exportPlot(filenameDestination, vtea._vtea.PLOT_DIRECTORY,
                        filename.replaceAll(".png", ""), parentKey, features, group, specialized);

                firePropertyChange("comment", "", "Done.");

                firePropertyChange("comment", "", "Done.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

            try {

                firePropertyChange("progress", 0, 5);
                firePropertyChange("comment", "", "Generating "
                        + plotType);

                ArrayList<ArrayList<Double>> al = H2DatabaseEngine.getColumnsnD(
                        vtea._vtea.H2_MEASUREMENTS_TABLE + "_"
                        + parentKey.replace("-", "_"), "Object",
                        group, this.features);
//                
                Class<?> c;
                c = Class.forName(vtea._vtea.PLOTMAKERMAP.get(plotType));
                Constructor<?> con;

                Object iImp = new Object();

                con = c.getConstructor();
                iImp = con.newInstance();

                ((AbstractPlotMaker) iImp).makePlot(vtea._vtea.PLOT_DIRECTORY,
                        parentKey, al, this.features, group);

                firePropertyChange("comment", "", "Done.");

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
