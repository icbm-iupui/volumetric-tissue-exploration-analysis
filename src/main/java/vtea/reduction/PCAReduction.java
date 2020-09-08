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
import javax.swing.JTextField;
import org.scijava.plugin.Plugin;
import smile.projection.PCA;
import vtea.featureprocessing.AbstractFeatureProcessing;
import vtea.featureprocessing.FeatureProcessing;

/**
 * Principal Component Analysis.
 *
 * @author drewmcnutt
 */
@Plugin(type = FeatureProcessing.class)
public class PCAReduction extends AbstractFeatureProcessing {

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
        comment = comment.concat(((JComboBox) comComponents.get(4)).getSelectedItem() + ": ");
        comment = comment.concat(((JTextField) comComponents.get(5)).getText());
        comment = comment.concat("</html>");
        return comment;
    }

    /**
     * Basic Constructor. Sets all protected variables
     */
    public PCAReduction() {
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implements the plugin from SMILE";
        NAME = "Principal Component Analysis";
        KEY = "PCA";
        TYPE = "Reduction";
    }

    /**
     * Constructor called for initialization of Setup GUI. When components are
     * added to this, the static method must be altered.
     *
     * @param max the number of objects segmented in the volume
     */
    public PCAReduction(int max) {
        this();

        protocol = new ArrayList();
        String[] combobox = {"New Dimension", "Desired Variance"};
        JComboBox jcb = new JComboBox(combobox);
        JTextField jtf = new JTextField("2", 3);
        jcb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (jcb.getSelectedIndex() == 0) {
                    jtf.setText("2");
                } else {
                    jtf.setText("0.95");
                }
            }
        });
        protocol.add(jcb);
        protocol.add(jtf);
    }

    @Override
    public String getDataDescription(ArrayList params) {
        
        String keyRoot = ((JTextField) params.get(5)).getText();
        keyRoot = keyRoot.replace(".", "");
        if (((JComboBox) params.get(4)).getSelectedIndex() == 0) {
            return KEY + "_D" + keyRoot + '_' + getCurrentTime();
        } else {
            return KEY + "_V" + keyRoot + '_' + getCurrentTime();
        }

    }

    /**
     * Performs the Principal Component Analysis based on the parameters.
     *
     * @param al contains all of the parameters in the form of JComponents
     * @param feature the full data to be parsed and analyzed
     * @return true when complete
     */
    @Override
    public boolean process(ArrayList al, double[][] feature, boolean valid) {
        double variance;
        int newdim;
        double[][] output;

        ArrayList selectData = (ArrayList) al.get(1);
        boolean znorm = (boolean) al.get(0);
        feature = selectColumns(feature, selectData);
        feature = normalizeColumns(feature, znorm);

        JComboBox selection = (JComboBox) al.get(4);
        JTextField input = (JTextField) al.get(5);

        IJ.log(String.format("PROFILING: Finding principal components of the data"));
        PCA pca = new PCA(feature);

        if (selection.getSelectedIndex() == 0) {
            newdim = Integer.parseInt(input.getText());
            pca.setProjection(newdim);
            double[] cumvar = pca.getCumulativeVarianceProportion();
            IJ.log(String.format("PROFILING: Projecting the data onto %d dimensions covering %f%% of the variance", newdim, cumvar[newdim - 1] * 100));
        } else {
            variance = Double.parseDouble(input.getText());
            pca.setProjection(variance);
            double[] cumvar = pca.getCumulativeVarianceProportion();
            newdim = 0;
            for (double cum : cumvar) {
                newdim++;
                if (cum >= variance) {
                    variance = cum;
                    break;
                }
            }
            IJ.log("PROFILING: Projecting the data onto " + newdim + " dimensions covering " + IJ.d2s(variance * 100) + "% of the variance");
        }

        output = pca.project(feature);
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
