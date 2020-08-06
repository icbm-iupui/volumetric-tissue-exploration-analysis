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
package vtea.spatial;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import vtea.exploration.listeners.AddFeaturesListener;
import vteaobjects.MicroObject;

/**
 *
 * @author Seth
 */
public class densityMap3d {

    ArrayList<ImagePlus> densityMap3d = new ArrayList<ImagePlus>();
    ArrayList<String> map3dName = new ArrayList<String>();

    ArrayList<AddFeaturesListener> addfeaturelistener = new ArrayList<AddFeaturesListener>();

    SettingsDialog sd = new SettingsDialog();

    public void densityMap3d() {

    }

    public void addMap(ImagePlus imp, String name) {

        densityMap3d.add(imp);
        map3dName.add(name);

    }

    public ImagePlus makeMap(ImagePlus imp, ArrayList<MicroObject> al) {

        ArrayList<String> settings = new ArrayList<String>();

        //settings = sd.getSettings();
        sd.showDialog();

        settings = sd.getSettings();

        int radius = Integer.parseInt((String) settings.get(0));
        int weight = Integer.parseInt((String) settings.get(1));

        System.out.println("PROFILING: Generating density map, radius: " + radius + " and weight:" + weight);

        ImagePlus resultImage = IJ.createImage("Segmentation", "8-bit black",
                imp.getWidth(), imp.getHeight(), imp.getNSlices());
        ImageStack resultStack = resultImage.getStack();

        //ArrayList<Integer> xPos = new ArrayList<Integer>();
        //ArrayList<Integer> yPos = new ArrayList<Integer>();
        //ArrayList<Integer> zPos = new ArrayList<Integer>();
        int R = (int) Math.pow(radius, 2);

        int total = al.size();
        int step = 1;

        ListIterator<MicroObject> itr = al.listIterator();
        while (itr.hasNext()) {
            MicroObject vol = (MicroObject) itr.next();
            IJ.showStatus("Calculating distance map...");
            IJ.showProgress(step, total);
            step++;
            try {

                int x0 = (int) vol.getCentroidX();
                int y0 = (int) vol.getCentroidY();
                int z0 = (int) vol.getCentroidZ();

                int xStart = x0 - R - 1;
                int xStop = x0 + R + 1;

                xStart = lowBounds(xStart, 0);
                xStop = highBounds(xStop, imp.getWidth());

                int yStart = y0 - R - 1;
                int yStop = y0 + R + 1;

                yStart = lowBounds(yStart, 0);
                yStop = highBounds(yStop, imp.getHeight());

                int zStart = z0 - R - 1;
                int zStop = z0 + R + 1;

                zStart = lowBounds(zStart, 0);
                zStop = highBounds(zStop, imp.getNSlices());

                for (int x = xStart; x < xStop; x++) {
                    for (int y = yStart; y < yStop; y++) {
                        for (int z = zStart; z < zStop; z++) {
                            if (Math.pow(x - x0, 2) + Math.pow(y - y0, 2) + Math.pow(z - z0, 2) <= R) {
                                resultStack.setVoxel(x, y, z, resultStack.getVoxel(x, y, z) + weight);
                            }
                        }
                    }
                }

            } catch (NullPointerException e) {
            }
        }

        //IJ.run(resultImage,"Invert", "stack");
        IJ.run(resultImage, "Gaussian Blur 3D...", "x=2 y=2 z=2");

        //resultImage.show();
        //IJ.run(resultImage, "Fire", "");
        return resultImage;
    }

    private int lowBounds(int value, int min) {

        if (value < min) {
            return min;
        } else {
            return value;
        }

    }

    private int highBounds(int value, int max) {

        if (value > max) {
            return max;
        } else {
            return value;
        }
    }

    public ImagePlus getMap(int i) {
        return this.densityMap3d.get(i);
    }

    public ArrayList<ArrayList<Number>> getDistance(ArrayList<MicroObject> objects, ImagePlus imp) {

        ArrayList<ArrayList<Number>> al = new ArrayList<ArrayList<Number>>();

        ArrayList<Number> result = new ArrayList<Number>();

        ListIterator<MicroObject> itr = objects.listIterator();

        while (itr.hasNext()) {

            MicroObject object = itr.next();

            //System.out.println("PROFILING: map value: " + imp.getProcessor().getPixel((int)object.getCentroidX(), (int)object.getCentroidY()));
            result.add(imp.getProcessor().getPixel((int) object.getCentroidX(), (int) object.getCentroidY()));

        }
        al.add(result);

        return al;
    }

    public void addFeatureListener(AddFeaturesListener listener) {
        addfeaturelistener.add(listener);
    }

    public void notifyaddFeatureListeners(String name, ArrayList<ArrayList<Number>> al) {
        for (AddFeaturesListener listener : addfeaturelistener) {
            listener.addFeatures(name, al);
        }
    }

    public void addDistanceFeatures() {

    }

    public ArrayList<String> getMapNames() {
        return this.map3dName;
    }

    class SettingsDialog extends JOptionPane {

        ArrayList<String> settings = new ArrayList<String>();

        //size, radius arround centroid for sphere
        //weight,
        JTextArea size = new JTextArea("10");
        JTextArea weight = new JTextArea("5");

        JPanel menu = new JPanel();

        boolean result = false;

        public SettingsDialog() {

            super();

            settings.add("10");
            settings.add("5");

            menu.setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0.2, 1.0,
                    GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
            menu.add(new JLabel("Radius"), gbc);
            gbc = new GridBagConstraints(1, 0, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
            menu.add(size, gbc);

            gbc = new GridBagConstraints(0, 1, 1, 1, 0.2, 1.0,
                    GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
            menu.add(new JLabel("Weight"), gbc);
            gbc = new GridBagConstraints(1, 1, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
            menu.add(weight, gbc);

        }

        public void showDialog() {
            int x = showOptionDialog(null, menu, "Setup Density Map Calculation",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                    null, null);

            if (x == JOptionPane.OK_OPTION) {
                //System.out.println("PROFILING: radius: " + size.getText() + " and weight:" + weight.getText());
                result = true;
                settings.clear();
                settings.add(size.getText());
                settings.add(weight.getText());
            } else {
                result = false;
            }

        }

        public ArrayList<String> getSettings() {

            return settings;
        }
    }

}
