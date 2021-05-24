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
 * @author sethwinfree  This impleents the original apporach iused for the KPMP.
It is flawed in the analysis and may contain duplicate values diluting means.
 *
 */
@Plugin(type = Morphology.class)
public class Grow_6C_inclusive extends AbstractMorphology {
    
    JTextField Distance = new JTextField("1", 5);

    public Grow_6C_inclusive() {
        VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "Basic dilation inclusive KPMP";
        NAME = "Grow 6C";
        KEY = "GR6C";
    }

    //Allowed operations: 6C, 8C
    //Allowed arguments:  in String arg.
    @Override
    public ArrayList<ArrayList<Number>> process(int[] x, int[] y, int[] z, 
List<JComponent> protocol, String operation, String arg) {

        JTextField distance = (JTextField) protocol.get(1);

        return growRegion6C(x, y, z, Integer.parseInt(distance.getText()));
    }

    @Override
    public String getUID(ArrayList<JComponent> al) {
        JTextField distance = (JTextField) al.get(1);
        return this.NAME + "_" + distance;
    }

    @Override
    public ArrayList getOptions() {
        ArrayList<JComponent> al = new ArrayList<JComponent>();
        al.add(new JLabel("Dilation distance: "));
        al.add(Distance);
        return al;
    }

    @Override
    public JPanel getMorphologicalTool() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(300, 300));
        panel.add(new JLabel("Coming soon, demostration space..."));
        return panel;
    }

    private ArrayList<ArrayList<Number>> growRegion6C(int[] x, int[] y, int[] z, int times) {

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

                //z below
                xArr.add(x[i]);
                yArr.add(y[i]);
                zArr.add(z[i] - 1);

                //z above
                xArr.add(x[i]);
                yArr.add(y[i]);
                zArr.add(z[i] + 1);

            }

        }
        noDups = removeDuplicates(xArr, yArr, zArr);

        return noDups;
    }

private ArrayList<ArrayList<Number>> removeDuplicates(ArrayList<Number> x, ArrayList<Number> y, ArrayList<Number> z) {

        Number xPos;
        Number yPos;
        Number zPos;

        int count = 0;

        for (int i = 0; i < x.size(); i++) {
            xPos = x.get(i);
            yPos = y.get(i);
            zPos = z.get(i);

            for (int j = 0; j < x.size(); j++) {
                if (((Integer)(xPos)).equals(x.get(j)) && ((Integer)(yPos)).equals(y.get(j)) && ((Integer)(zPos)).equals(z.get(j))) {
                    x.remove(j);
                    y.remove(j);
                    z.remove(j);
                    j--;
                }
            }
        }
        ArrayList<ArrayList<Number>> result = new ArrayList();

        result.add(x);
        result.add(y);
        result.add(z);

        return result;
    }



}
