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

import java.awt.Component;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.renjin.script.RenjinScriptEngineFactory;
import org.scijava.plugin.Plugin;
import vtea.jdbc.H2DatabaseEngine;
import static vtea.renjin.AbstractRenjin.VTEACOLORS;

/**
 *
 * @author sethwinfree
 */
@Plugin(type = PlotMaker.class)
public class ViolinPlot extends AbstractPlotMaker {

    public ViolinPlot() {

        VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "Generates violin plots.";
        NAME = "Violin Plot";
        KEY = "ViolinPlot";

    }

    @Override
    public String makePlot(String location, String key, ArrayList<ArrayList<Double>> al,
            ArrayList<String> featureNames, String group) {

        String filename = KEY + "_" + System.currentTimeMillis();

        try {
            File file = new File(location + System.getProperty("file.separator") + filename + ".csv");
            PrintWriter pw;

            pw = new PrintWriter(file);

            StringBuilder sb = new StringBuilder();

            sb.append("Object");
            sb.append(',');
            sb.append(group);
            sb.append(',');
            sb.append(featureNames.get(0));
            sb.append('\n');

            ListIterator<ArrayList<Double>> itr = al.listIterator();

            while (itr.hasNext()) {

                ArrayList<Double> objectData = itr.next();

                sb.append((Double) objectData.get(0));
                sb.append(",");
                sb.append((Double) objectData.get(1));
                sb.append(",");
                sb.append((Double) objectData.get(2));
                sb.append('\n');

            }

            pw.write(sb.toString());
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ViolinPlot.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {

            RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();

            ScriptEngine engine = factory.getScriptEngine();

            engine.eval("library(ggplot2)");
            engine.eval("library(colorspace)");

            String platform = System.getProperty("os.name");
            if (platform.startsWith("Windows")) {
                location = location.replace("\\", "//");
                engine.eval("plot <- read.csv('" + location + "//" + filename + ".csv')");
            } else {
                engine.eval("plot <- read.csv('" + location + "/" + filename + ".csv')");
            }
            engine.eval(VTEACOLORS);
            //engine.eval("plot$" + group + " <- factor(plot, " + "plot$" + group  + ")");
            engine.eval("out <- ggplot(plot, aes(factor(plot$" + group + "),"
                    + " plot$" + featureNames.get(0) + ", fill = as.factor(plot$" + group + ")))");
            engine.eval("out <- out + geom_violin(scale = 'width', alpha = 0.8) "
                    + "+ theme_bw() + theme(legend.position = 'none') + "
                    + "scale_fill_manual(values = plot_colors)");

            engine.eval("png('" + location + "/" + filename + ".png')");
            engine.eval("print(out)");
            engine.eval("dev.off()");

        } catch (ScriptException ex) {
            Logger.getLogger(ViolinPlot.class.getName()).log(Level.SEVERE, null, ex);
        }
        return filename + ".png";
    }

    @Override
    public void exportPlot(String destination, String location, String filename, String key,
            ArrayList<String> featureNames, String group, ArrayList<Component> secondarySettings) {
        

                
        try {

            RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();

            ScriptEngine engine = factory.getScriptEngine();

            engine.eval("library(ggplot2)");
            engine.eval("library(colorspace)");

            String platform = System.getProperty("os.name");
            if (platform.startsWith("Windows")) {
                location = location.replace("\\", "//");
                engine.eval("plot <- read.csv('" + location + "//" + filename + ".csv')");
            } else {
                engine.eval("plot <- read.csv('" + location + "/" + filename + ".csv')");
            }
            engine.eval(VTEACOLORS);
            
            
            engine.eval("sortorder <- c(" + ((JTextField)secondarySettings.get(3)).getText() + ")");
            engine.eval("plot$" + group + " <- factor(" + "plot$" + group + ", levels = c(" + ((JTextField)secondarySettings.get(3)).getText() + "))");
            engine.eval("out <- ggplot(plot, aes(factor(plot$" + group + "),"
                    + " plot$" + featureNames.get(0) + ", fill = as.factor(plot$" + group + ")))");
            engine.eval("out <- out + geom_violin(scale = 'width', alpha = 0.8) "
                    + "+ theme_bw() + theme(legend.position = 'none') + "
                    + "scale_fill_manual(values = plot_colors)");
            
            if (platform.startsWith("Windows")) {
                destination = destination.replace("\\", "//");
                engine.eval("ggsave('" + destination + ".pdf', height = " + ((JTextField)secondarySettings.get(1)).getText() + ", width = " +((JTextField)secondarySettings.get(0)).getText()+")");
            } else {
                engine.eval("ggsave('" + destination + ".pdf', height = " + ((JTextField)secondarySettings.get(1)).getText() + ", width = " +((JTextField)secondarySettings.get(0)).getText()+")");
            }
            
//            engine.eval("ggsave('" + location + "/" + filename + ".pdf'");
            
//            engine.eval("pdf('" + location + "/" + filename + ".pdf', height = " + ((JTextField)secondarySettings.get(1)).getText() + ", width = " +((JTextField)secondarySettings.get(0)).getText() + ")");
//            engine.eval("print(out)");
//            engine.eval("dev.off()");

        } catch (ScriptException ex) {
            Logger.getLogger(ViolinPlot.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public boolean doesMultiples() {
        return false;
    }

    @Override
    public String getGroup(File file) {
        String header = AbstractPlotMaker.getCSVHeader(file);
        String[] data = header.split(",");
        return data[1];
    }

    @Override
    public ArrayList<String> getFeatures(File file) {
        ArrayList<String> features = new ArrayList<>();
        String header = AbstractPlotMaker.getCSVHeader(file);
        String[] data = header.split(",");
        
        features.add(data[2]);
       //System.out.println("PROFILING: feature, " + data[2]);
        return features;
    }

    @Override
    public boolean hasSecondarySettings() {
        return true;
    }

    @Override
    public ArrayList<Component> getSecondarySettings(ArrayList<String> features, String group, File file) {
        //System.out.println("PROFILING: file, " + file.getPath());
        
 
        String csvFilename = file.getPath().replace(".png", ".csv");        
        ArrayList<Component> secondaryComponents = new ArrayList<>();
        JLabel label = new JLabel("Sort: ");
        label.setPreferredSize(new Dimension(30, 20));
        secondaryComponents.add(label);
        JTextField sort = new JTextField(getSortOrderString(new File(vtea._vtea.PLOT_DIRECTORY
                + System.getProperty("file.separator")
                + csvFilename)));
        sort.setPreferredSize(new Dimension(140, 20));
        secondaryComponents.add(sort);
        return secondaryComponents;
    }

    private String getSortOrderString(File file) {

        String result = "";
         ArrayList<ArrayList<Number>> groupData = new ArrayList();
         String header = "";

       try {
                BufferedReader csvReader = new BufferedReader(new FileReader(file));
                String row;
                boolean firstRow = true;


                while ((row = csvReader.readLine()) != null) {



                    if (firstRow) {
                        header = row;
                        firstRow = false;
                    } else {
                        String[] data = row.split(",");

                       // dataColumns = data.length;

                        ArrayList<Number> dataList = new ArrayList<Number>();

                        //for (int j = 0; j < data.length; j++) {
                            dataList.add(Float.parseFloat(data[1]));
                        //}

                       groupData.add(dataList);

                    }
                }
                csvReader.close();

            } catch (IOException e) {
                System.out.println("ERROR: Could not open the file.");
            }
       
       double max = getMaximumOfData(groupData, 0);
       double min = getMinimumOfData(groupData, 0);
       
       result = result + "'" + (int)min + "'";
       
       for(int j = (int)min+1; j <= max; j++){
           
           result = result + ", '" + j + "'";
           
       }

        return result;
    }

    private double getMaximumOfData(ArrayList measurements, int l) {

        ListIterator<ArrayList> litr = measurements.listIterator();

        Number high = 0;

        while (litr.hasNext()) {
            try {

                ArrayList<Number> al = litr.next();

                if (al.get(l).floatValue() > high.floatValue()) {
                    high = al.get(l).floatValue();
                }
            } catch (NullPointerException e) {
            }
        }
        return high.longValue();

    }

    private double getMinimumOfData(ArrayList measurements, int l) {

        ListIterator<ArrayList> litr = measurements.listIterator();

        //ArrayList<Number> al = new ArrayList<Number>();
        Number low = getMaximumOfData(measurements, l);

        while (litr.hasNext()) {
            try {
                ArrayList<Number> al = litr.next();
                if (al.get(l).floatValue() < low.floatValue()) {
                    low = al.get(l).floatValue();
                }
            } catch (NullPointerException e) {
            }
        }
        //System.out.println("PROFILING: The low value is: " + low);
        return low.longValue();
        //return low.doubleValue();
    }

}
