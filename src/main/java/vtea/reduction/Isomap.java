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
package vtea.reduction;

import ij.IJ;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.scijava.plugin.Plugin;
import smile.manifold.IsoMap;
import vtea.featureprocessing.AbstractFeatureProcessing;
import vtea.featureprocessing.FeatureProcessing;

/**
 * Isomap manifold determinations.
 *
 * @author swinfree
 */
@Plugin(type = FeatureProcessing.class)
public class Isomap extends AbstractFeatureProcessing {

    public static boolean validate = false;

    /**
     * Creates the Comment Text for the Block GUI.
     *
     * @param comComponents the parameters (Components) selected by the user in
     * the Setup Frame.
     * @return comment text detailing the parameters
     */
    public static String getBlockComment(ArrayList comComponents) {
        String comment = "<html>";
        comment = comment.concat(((JTextField) comComponents.get(5)).getText());
        comment = comment.concat("</html>");
        return comment;
    }

    /**
     * Basic Constructor. Sets all protected variables
     */
    public Isomap() {
        VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "Implements the plugin from SMILE";
        NAME = "IsoMap Embedding";
        KEY = "IsoMap";
        TYPE = "Reduction";
    }

    /**
     * Constructor called for initialization of Setup GUI. When components are
     * added to this, the static method must be altered.
     *
     * @param max the number of objects segmented in the volume
     */
    public Isomap(int max) {
        this();

        protocol = new ArrayList();
        JLabel k = new JLabel("neighbors");
        JTextField jtfK = new JTextField("10", 3);
        JLabel d = new JLabel("dimensions");
        JTextField jtfD = new JTextField("1", 3);
        protocol.add(d);
        protocol.add(jtfD);
         protocol.add(k);
        protocol.add(jtfK);
    }

    @Override
    public String getDataDescription(ArrayList params) {       
        String keyRoot = ((JTextField) params.get(7)).getText();
        keyRoot = keyRoot.replace(".", "");
        return KEY + "_" + keyRoot + '_' + getCurrentTime();
    }

    /**
     * Performs the IsoMap embedding based on the parameters.
     *
     * @param al contains all of the parameters in the form of JComponents
     * @param feature the full data to be parsed and analyzed
     * @return true when complete
     */
    @Override
    public boolean process(ArrayList al, double[][] feature, boolean valid) {
        double[][] output;

        ArrayList selectData = (ArrayList) al.get(1);
        boolean znorm = (boolean) al.get(0);
        feature = selectColumns(feature, selectData);
        feature = normalizeColumns(feature, znorm);

        JTextField inputK = (JTextField) al.get(5);
        JTextField inputD = (JTextField) al.get(7);

        IJ.log(String.format("PROFILING: Emdbedding data in isomap."));
        IsoMap isomap = new IsoMap(feature, Integer.parseInt(inputK.getText()), Integer.parseInt(inputD.getText()), true);
        IJ.log("PROFILING: Projecting the data onto " + Integer.parseInt(inputD.getText()) + " dimensions");
        
        output = isomap.getCoordinates();
        
        for (int j = 0; j < output[0].length; j++) {
            ArrayList dimension = new ArrayList();
            for (double[] row : output) {
                dimension.add(row[j]);
            }
            dataResult.add(dimension);
        }

        return true;
    }

}
