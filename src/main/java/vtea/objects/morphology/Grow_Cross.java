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
 * @author sethwinfree
 * Disabled
 */
//@Plugin(type = Morphology.class)
public class Grow_Cross extends AbstractMorphology {

    JTextField Distance = new JTextField("1", 5);

    public Grow_Cross() {
        VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "Cross 3D probe 6C";
        NAME = "Cross";
        KEY = "CROSS";
    }

    //Allowed operations: 6C, 8C
    //Allowed arguments:  in String arg.
    @Override
    public ArrayList<ArrayList<Number>> process(int[] x, int[] y, int[] z, List<JComponent> protocol, String operation, String arg) {

        JTextField distance = (JTextField) protocol.get(1);

        ArrayList<Number> centroid = getCentroid(x, y, z);

        return makeCross6C(centroid.get(0).intValue(), centroid.get(1).intValue(), centroid.get(2).intValue(), Integer.parseInt(distance.getText()));
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

    private ArrayList<Number> getCentroid(int[] x, int[] y, int[] z) {

        int maxX = 0;
        int maxY = 0;
        int maxZ = 0;

        for (int i = 0; i < x.length; i++) {

            if (x[i] > maxX) {
                maxX = x[i];
            }
            if (y[i] > maxY) {
                maxY = y[i];
            }
            if (z[i] > maxZ) {
                maxZ = z[i];
            }

        }

        int minX = maxX;
        int minY = maxY;
        int minZ = maxZ;

        for (int i = 0; i < x.length; i++) {

            if (x[i] < minX) {
                minX = x[i];
            }
            if (y[i] < minY) {
                minY = y[i];
            }
            if (z[i] < minZ) {
                minZ = z[i];
            }

        }

        int centX = maxX - minX / 2;
        int centY = maxY - minY / 2;
        int centZ = maxZ - minZ / 2;

        ArrayList<Number> result = new ArrayList<Number>();

        result.add(centX);
        result.add(centY);
        result.add(centZ);

        return result;
    }

    private ArrayList<ArrayList<Number>> makeCross6C(int x, int y, int z, int times) {

        //System.out.println("PROFILING");
        ArrayList<Number> xArr = new ArrayList();
        ArrayList<Number> yArr = new ArrayList();
        ArrayList<Number> zArr = new ArrayList();

        ArrayList<Number> xList = new ArrayList();
        ArrayList<Number> yList = new ArrayList();
        ArrayList<Number> zList = new ArrayList();

        ArrayList<ArrayList<Number>> noDups = new ArrayList();

        //to determine how many expansions
        for (int j = 1; j <= times; j++) {

            xArr.add(x - j);
            yArr.add(y);
            zArr.add(z);

            xArr.add(x + j);
            yArr.add(y);
            zArr.add(z);

            xArr.add(x);
            yArr.add(y - j);
            zArr.add(z);

            xArr.add(x);
            yArr.add(y + j);
            zArr.add(z);

            //z below
            xArr.add(x);
            yArr.add(y);
            zArr.add(z - j);

            //z above
            xArr.add(x);
            yArr.add(y);
            zArr.add(z + j);

        }
        //reassign

        noDups = removeDuplicates(xArr, yArr, zArr);
        noDups = removeOverlapPixels(xList, yList, zList, noDups.get(0), noDups.get(1), noDups.get(2));

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
