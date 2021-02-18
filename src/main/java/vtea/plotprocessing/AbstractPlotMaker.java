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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import static vtea.exploration.plottools.panels.XYExplorationPanel.getMaximumOfData;
import static vtea.exploration.plottools.panels.XYExplorationPanel.getMinimumOfData;
import vtea.jdbc.H2DatabaseEngine;

/**
 *
 * @author sethwinfree
 */
abstract public class AbstractPlotMaker implements PlotMaker {

    protected String VERSION = "0.0";
    protected String AUTHOR = "VTEA developer";
    protected String COMMENT = "Classes for generating plots.";
    protected String NAME = "Abstract PlotMaker";
    protected String KEY = "AbstractPlotMaker";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getAuthor() {
        return AUTHOR;
    }

    @Override
    public String getComment() {
        return COMMENT;
    }

    @Override
    public String makePlot(String location, String key, ArrayList<ArrayList<Double>> al,
            ArrayList<String> featureNames, String group) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
      public void exportPlot(String destination, String location, String filename, String key,
            ArrayList<String> featureNames, String group, ArrayList<Component> secondarySettings){
          
          //secondarySettings include:
          //0: X
          //1: Y
          //2: custom, plot defined
        
          throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
      }
      
      @Override
    public String getGroup(File file){
       throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public ArrayList<String> getFeatures(File file){
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public boolean doesMultiples(){
        return false;
    }
    
     public static String getCSVHeader(File file){
        String header = "";
         try {
                    int dataColumns = 0;
                BufferedReader csvReader = new BufferedReader(new FileReader(file));
                String row;
                boolean firstRow = true;
                
                


                while ((row = csvReader.readLine()) != null) {

                    if (firstRow) {
                        header = row;
                        firstRow = false;
                    }
                }
                csvReader.close();

            } catch (IOException e) {
                System.out.println("ERROR: Could not open the file.");
            }
         
         
         
         return header;
         
         
    }
     
        static public String getGroupSortString(String keySQLSafe, String group) {
            
        double max = Math.round(getMaximumOfData(H2DatabaseEngine.getColumn(vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe, group), 0));
        double min = Math.round(getMinimumOfData(H2DatabaseEngine.getColumn(vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe, group), 0));

        String sort = new String();

        sort = sort + "'" + (int) min;

        for (int i = (int) min + 1; i <= max; i++) {
            sort = sort + ",'" + i + "'";
        }

        //sort = sort + ")";
        return sort;

    }

}
