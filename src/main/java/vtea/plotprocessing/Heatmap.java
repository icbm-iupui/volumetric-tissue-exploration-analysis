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
package vtea.plotprocessing;

import ij.IJ;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.renjin.script.RenjinScriptEngineFactory;
import org.scijava.plugin.Plugin;
import vtea.objects.layercake.LayerCake3D;
import vtea.objects.layercake.microRegion;
import static vtea.renjin.AbstractRenjin.VTEACOLORS;

/**
 *
 * @author sethwinfree
 */
@Plugin(type = PlotMaker.class)
public class Heatmap extends AbstractPlotMaker {

    public Heatmap() {

        VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "Generates heatmaps.";
        NAME = "Heatmap";
        KEY = "Heatmap";

    }
    
    //takes feature list: object, group, features-n

    @Override
    public String makePlot(String location, String key, ArrayList<ArrayList<Double>> al,
            ArrayList<String> featureNames, String group) {

//        0 -> object
//        1 -> group
//        2 -> feature-1...

//want rows by feature, columns averages by group number

        String filename = "Heatmap_" + System.currentTimeMillis();
        
        //IJ.log("PROFILING: Making Heatmap...");

        ArrayList<String> features = featureNames;
        
        //make group list
        ArrayList<Double> toGroup = new ArrayList<>();
        
        toGroup = new ArrayList<>();
        for(int k = 0; k < al.size(); k++){
                toGroup.add(al.get(k).get(1));
        }
        
        //get group numbers

        ArrayList<Integer> groupParsed = getGroups(toGroup);

        //ArrayList to hold calculated means by group number
        
        ArrayList<ArrayList<Double>> means = new ArrayList<>();
        
        //generate an array of rows by feature, columns by group
        
        //by cell data to use in each calculation
        ArrayList<Double> toCalculate = new ArrayList<>();
        toGroup = new ArrayList<>();
        
        
        for (int j = 0; j < features.size(); j++) {
                toCalculate = new ArrayList<>();
                toGroup = new ArrayList<>();
            for(int k = 0; k < al.size(); k++){
                toCalculate.add(al.get(k).get(j+2));
                toGroup.add(al.get(k).get(1));
                }          
            means.add(getMeansByPopulations(toCalculate, toGroup, groupParsed));
        }
        try {
            File file = new File(location + System.getProperty("file.separator")
                    + filename + ".csv");
            PrintWriter pw;

            pw = new PrintWriter(file);

            //these data have a different config, need means...
            StringBuilder sb = new StringBuilder();

            sb.append(group);

            for (int i = 0; i < features.size(); i++) {
                sb.append(',');
                sb.append(features.get(i));
            }

            sb.append('\n');
                
            for(int m = 0; m < groupParsed.size(); m++){
                sb.append(groupParsed.get(m));
                for(int n = 0; n <features.size(); n++){
                    ArrayList<Double> data = means.get(n);
                    sb.append(","); 
                    sb.append(data.get( m));
                }
                sb.append('\n');
            }
      
            pw.write(sb.toString());
            pw.close();
        } catch (FileNotFoundException ex) {
            System.out.println("ERROR: csv generation failed.");
        }
        try {
            RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();

            ScriptEngine engine = factory.getScriptEngine();

            //load libraries
            engine.eval("library(ggplot2)");
            engine.eval("library(gplots)");
     
            engine.eval("library(colorspace)");
            engine.eval("library(RColorBrewer)");
            engine.eval(VTEACOLORS);

            //load data
            engine.eval("plot <- read.csv('" + location
                    + "/" + filename + ".csv')");
            engine.eval("row.names(plot) <- plot$" + group);
            engine.eval("plot <- plot[,-1]");
            engine.eval("plot <- as.matrix(plot)");
            engine.eval("lut <- colorRampPalette(brewer.pal(8, 'PiYG'))(50)");
            engine.eval("png('" + location + "/" + filename + ".png')");
            engine.eval("heatmap.2(plot,  scale='column',col = lut, density.info='none',"
                    + "cexRow=1,cexCol=1,margins=c(12,8),trace='none',srtCol=45)");
            engine.eval("dev.off()");

        } catch (ScriptException ex) {
            System.out.println("ERROR: Renjin interface failed.");
            return null;
        }
        return filename + ".png";
    }

    @Override
    public boolean doesMultiples() {
        return true;
    }

    //makes an array of averages for a features across all groups
    private ArrayList<Double> getMeansByPopulations(ArrayList<Double> values,
            ArrayList<Double> groups, ArrayList<Integer> groupParsed) {

        ArrayList<Double> result = new ArrayList<>();
        
        

        double sum = 0;
        double count = 0;
        int currentGroup = 0;
        
        ArrayList<Double> ordered = new ArrayList<>();
        
        

        for (int i = 0; i < groupParsed.size(); i++) {

            currentGroup = groupParsed.get(i);
            sum = 0;
            count = 0;
            

            for (int j = 0; j < groups.size(); j++) {
                if (groups.get(j) == currentGroup) {
                    sum += values.get(j);
                    count++;
                }
                
            }
            
            result.add(sum / count);

           

        }

        return result;

    }

    private ArrayList<Integer> getGroups(ArrayList<Double> groupData) {
        ArrayList<Integer> groups = new ArrayList<>();

        Collections.sort(groupData, new GroupComparator());
        
        ListIterator<Double> itr = groupData.listIterator();
        


        int group = groupData.get(0).intValue();
        groups.add(group);

        //should run mean here too to avoid double loop
        for (int i = 0; i < groupData.size(); i++) {
            if (groupData.get(i).intValue() != group) {
                group = groupData.get(i).intValue();
                groups.add(group);
            }
        }
        return groups;
    }

    @Override
    public String exportPDFPlot(String location, String key, ArrayList<ArrayList<Double>> al, ArrayList<String> featureNames, String group) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

class GroupComparator implements Comparator<Double> {

    @Override
    public int compare(Double o1, Double o2) {
        if (o1 == o2) {
            return 0;
        } else if (o1 > o2) {
            return 1;
        } else if (o1 < o2) {
            return -1;
        } else {
            return 0;
        }
    }

}
