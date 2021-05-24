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

import ij.ImagePlus;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author sethwinfree
 */
public class AbstractMorphology implements Morphology {

    protected String VERSION = "0.0";
    protected String AUTHOR = "VTEA Developer";
    protected String COMMENT = "New morphology filter";
    protected String NAME = "ABSTRACT MORPHOLOGY";
    protected String KEY = "ABSTRACT MORPHOLOGY";

    protected List protocol;

    protected String UID;

    /**
     *
     * @param al
     * @param values
     * @return
     */
    @Override
    public ArrayList<ArrayList<Number>> process(int[] x, int[] y, int[] z, List<JComponent> protocol, String operation, String arg) {

        ArrayList<ArrayList<Number>> result = new ArrayList();

        ArrayList<Number> xList = new ArrayList();
        ArrayList<Number> yList = new ArrayList();
        ArrayList<Number> zList = new ArrayList();

        for (int k = 0; k < x.length; k++) {
            xList.add(x[k]);
            yList.add(y[k]);
            zList.add(z[k]);
        }

        return result;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public String getVersion() {
        return VERSION;
    }

    public String getAuthor() {
        return AUTHOR;
    }

    public String getComment() {
        return COMMENT;
    }

    public ImagePlus getExample() {
        return new ImagePlus();
    }

    @Override
    public Image getExamples() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JPanel getMorphologicalTool() {
        return new JPanel();
    }

    @Override
    public ArrayList getOptions() {
        return new ArrayList();
    }

    @Override
    public ArrayList getSettings() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getUID(ArrayList<JComponent> al) {
        return new String();
    }

}
