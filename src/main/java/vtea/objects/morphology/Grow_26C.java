/* 
 * Copyright (C) 2016-2018 Indiana University
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
package vtea.objects.morphology;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.scijava.plugin.Plugin;

/**
 *
 * @author sethwinfree
 *
 */
@Plugin(type = Morphology.class)
public class Grow_26C extends AbstractMorphology {
    
    
    JTextField Distance = new JTextField("1", 5);

    public Grow_26C() {
        VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "Basic dilation";
        NAME = "Grow 26C";
        KEY = "GR26C";
    }
    
    //Allowed operations: 6C, 8C
    //Allowed arguments:  in String arg.

    @Override
    public ArrayList<ArrayList<Number>> process(int[] x, int[] y, int[] z, List<JComponent> protocol, String operation, String arg) {
       
        JTextField distance = (JTextField)protocol.get(1); 
       
       return growRegion26C(x, y, z, Integer.parseInt(distance.getText()));
    }
    
    @Override
    public String getUID(){
        return this.NAME + "_" + Distance.getText();
    }
    
    @Override
    public ArrayList getOptions(){ 
         ArrayList<JComponent> al = new ArrayList<JComponent>();
         al.add(new JLabel("Dilation distance: "));
         al.add(Distance);
         return al;   
    }
    
    @Override
    public JPanel getMorphologicalTool() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(300,300));
        panel.add(new JLabel("Coming soon, demostration space..."));
        return panel;
    }
    
    private ArrayList<ArrayList<Number>> growRegion26C(int[] x, int[] y, int[] z, int times) {

        //System.out.println("PROFILING:                       Starting object size: " + x.length + ".");
        ArrayList<Number> xArr = new ArrayList();
        ArrayList<Number> yArr = new ArrayList();
        ArrayList<Number> zArr = new ArrayList();

        ArrayList<Number> xList = new ArrayList();
        ArrayList<Number> yList = new ArrayList();
        ArrayList<Number> zList = new ArrayList();

        for (int k = 0; k < x.length; k++) {
            xList.add(x[k]);
            yList.add(y[k]);
            zList.add(z[k]);
        }
        
        ArrayList<ArrayList<Number>> noDups = new ArrayList();

        //to determine how many expansions
        
        for (int j = 1; j <= times; j++) {

            int n = x.length;
            
            //System.out.println("PROFILING:             Expansion time: " + times + ".");
            
            //8 connected.
            for (int i = 0; i < n; i++) {

                //same z
                xArr.add(x[i]);
                yArr.add(y[i]);
                zArr.add(z[i]);

                xArr.add(x[i] - 1);
                yArr.add(y[i]);
                zArr.add(z[i]);

                xArr.add(x[i] + 1);
                yArr.add(y[i]);
                zArr.add(z[i]);

                xArr.add(x[i]);
                yArr.add(y[i] - 1);
                zArr.add(z[i]);

                xArr.add(x[i]);
                yArr.add(y[i] + 1);
                zArr.add(z[i]);

                xArr.add(x[i] + 1);
                yArr.add(y[i] + 1);
                zArr.add(z[i]);

                xArr.add(x[i] + 1);
                yArr.add(y[i] - 1);
                zArr.add(z[i]);

                xArr.add(x[i] - 1);
                yArr.add(y[i] + 1);
                zArr.add(z[i]);

                xArr.add(x[i] - 1);
                yArr.add(y[i] - 1);
                zArr.add(z[i]);

                //z below
                xArr.add(x[i]);
                yArr.add(y[i]);
                zArr.add(z[i] - 1);

                xArr.add(x[i] - 1);
                yArr.add(y[i]);
                zArr.add(z[i] - 1);

                xArr.add(x[i] + 1);
                yArr.add(y[i]);
                zArr.add(z[i] - 1);

                xArr.add(x[i]);
                yArr.add(y[i] - 1);
                zArr.add(z[i] - 1);

                xArr.add(x[i]);
                yArr.add(y[i] + 1);
                zArr.add(z[i] - 1);

                xArr.add(x[i] + 1);
                yArr.add(y[i] + 1);
                zArr.add(z[i] - 1);

                xArr.add(x[i] + 1);
                yArr.add(y[i] - 1);
                zArr.add(z[i] - 1);

                xArr.add(x[i] - 1);
                yArr.add(y[i] + 1);
                zArr.add(z[i] - 1);

                xArr.add(x[i] - 1);
                yArr.add(y[i] - 1);
                zArr.add(z[i] - 1);

                //z above
                xArr.add(x[i]);
                yArr.add(y[i]);
                zArr.add(z[i] + 1);

                xArr.add(x[i] + 1);
                yArr.add(y[i]);
                zArr.add(z[i] + 1);

                xArr.add(x[i] - 1);
                yArr.add(y[i]);
                zArr.add(z[i] + 1);

                xArr.add(x[i]);
                yArr.add(y[i] - 1);
                zArr.add(z[i] + 1);

                xArr.add(x[i]);
                yArr.add(y[i] + 1);
                zArr.add(z[i] + 1);

                xArr.add(x[i] + 1);
                yArr.add(y[i] + 1);
                zArr.add(z[i] + 1);

                xArr.add(x[i] + 1);
                yArr.add(y[i] - 1);
                zArr.add(z[i] + 1);

                xArr.add(x[i] - 1);
                yArr.add(y[i] + 1);
                zArr.add(z[i] + 1);

                xArr.add(x[i] - 1);
                yArr.add(y[i] - 1);
                zArr.add(z[i] + 1);

               
            }
             //reassign

            noDups = removeDuplicates(xArr, yArr, zArr); 
            
            noDups = removeOverlapPixels(xList, yList, zList, noDups.get(0), noDups.get(1), noDups.get(2));
            
            if (times > 1) {
                
                    xArr = noDups.get(0);
                    yArr = noDups.get(1);
                    zArr = noDups.get(2);

                    x = new int[xArr.size()];
                    y = new int[xArr.size()];
                    z = new int[xArr.size()];

                    for (int k = 0; k < xArr.size(); k++) {
                        x[k] = (Integer) xArr.get(k);
                        y[k] = (Integer) yArr.get(k);
                        z[k] = (Integer) zArr.get(k);
                    }
            }
        }
        return noDups;
    }

    private ArrayList<ArrayList<Number>> removeDuplicates(ArrayList<Number> x, ArrayList<Number> y, ArrayList<Number> z) {

        Number xPos;
        Number yPos;
        Number zPos;

        ArrayList<Number> x2 = new ArrayList();
        ArrayList<Number> y2 = new ArrayList();
        ArrayList<Number> z2 = new ArrayList();

        int count = 0;

        for (int i = 0; i < x.size(); i++) {
            xPos = x.get(i);
            yPos = y.get(i);
            zPos = z.get(i);

            x2.add(xPos);
            y2.add(yPos);
            z2.add(zPos);

            for (int j = 0; j < x.size(); j++) {
                if ((xPos == x.get(j)) && (yPos == y.get(j)) && ((zPos == z.get(j)))) {
                    x.remove(j);
                    y.remove(j);
                    z.remove(j);
                }

            }

        }
        ArrayList<ArrayList<Number>> result = new ArrayList();

        result.add(x2);
        result.add(y2);
        result.add(z2);

        return result;

    }

    private ArrayList<ArrayList<Number>> removeOverlapPixels(ArrayList<Number> x1, ArrayList<Number> y1, ArrayList<Number> z1, ArrayList<Number> x2, ArrayList<Number> y2, ArrayList<Number> z2) {

        for (int i = 0; i < x1.size(); i++) {
            for (int j = 0; j < x2.size(); j++) {
                if ((x1.get(i) == x2.get(j)) && (y1.get(i) == y2.get(j)) && ((z1.get(i) == z2.get(j)))) {
                    x2.remove(j);
                    y2.remove(j);
                    z2.remove(j);
                }
            }
        }
        ArrayList<ArrayList<Number>> result = new ArrayList();

        result.add(x2);
        result.add(y2);
        result.add(z2);

        // System.out.println("PROFILING:  Final object size: " + x2.size() + ".");
        return result;
    }

}
