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

FOr networkanalyzer code adapted below:

MIT License

Copyright (c) 2018 Centre for Science and Technology Studies (CWTS), Leiden University

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package vtea.clustering;

import ij.IJ;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import nl.cwts.networkanalysis.Clustering;
import nl.cwts.networkanalysis.IterativeCPMClusteringAlgorithm;
import nl.cwts.networkanalysis.LeidenAlgorithm;
import nl.cwts.networkanalysis.LouvainAlgorithm;
import nl.cwts.networkanalysis.Network;
import nl.cwts.util.DynamicDoubleArray;
import nl.cwts.util.DynamicIntArray;
import org.la4j.matrix.sparse.CRSMatrix;
import org.scijava.plugin.Plugin;
import smile.classification.KNN;
import smile.clustering.KMeans;
import smile.neighbor.KDTree;
import smile.neighbor.KNNSearch;
import smile.neighbor.Neighbor;
import vtea.featureprocessing.AbstractFeatureProcessing;
import vtea.featureprocessing.FeatureProcessing;

/**
 * KNN graph generation and louvain partitioning.
 *
 * @author winfrees
 */
@Plugin(type = FeatureProcessing.class)
public class KNNCommunity extends AbstractFeatureProcessing {

    public static boolean validate = false;

    /**
     * Creates the Comment Text for the Block GUI.
     *
     * @param comComponents the parameters (Components) selected by the user in
     * the Setup Frame.
     * @return comment text detailing the parameters
     */
    public static String getBlockComment(ArrayList comComponents) {

        DecimalFormat df = new DecimalFormat("#.##");

        String resolution = df.format(getSliderValue(((JSlider) comComponents.get(13)).getValue()));

        String comment = "<html>";
        comment = comment.concat(((JLabel) comComponents.get(4)).getText() + ": ");
        comment = comment.concat(((JSpinner) comComponents.get(5)).getValue().toString());
        comment = comment.concat(", ");
        comment = comment.concat((String) ((JComboBox) comComponents.get(17)).getSelectedItem());
        comment = comment.concat(", ");
        comment = comment.concat("Resolution : ");
        comment = comment.concat(resolution);
        comment = comment.concat("</html>");
        return comment;
    }

    /**
     * Basic Constructor. Sets all protected variables
     */
    public KNNCommunity() {
        VERSION = "0.1";
        AUTHOR = "Seth WInfree";
        COMMENT = "Implements KNN graph generation and "
                + "community detection ";
        NAME = "KNN Community";
        KEY = "KNNcommunity";
        TYPE = "Cluster";
    }

    /**
     * Constructor called for initialization of Setup GUI. When components are
     * added to this, the static method must be altered.
     *
     * @param max the number of objects segmented in the volume
     */
    public KNNCommunity(int max) {
        this();

        protocol = new ArrayList();

        protocol.add(new JLabel("Neighbors"));

        //5
        protocol.add(new JSpinner(new SpinnerNumberModel(30, 2, max, 1)));

        protocol.add(new JLabel("CAUTION unstable"));    
        //protocol.add(new JLabel("    "));
        protocol.add(new JLabel("    "));

        JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, 20, 1);
        JLabelLinked value = new JLabelLinked("");
        slider.addChangeListener(value);

        //8
        protocol.add(new JLabel("Community Clustering:"));
        protocol.add(new JLabel("    "));
        protocol.add(new JLabel("    "));
        protocol.add(new JLabel("    "));

        //12
        protocol.add(new JLabel("Resolution"));
        protocol.add(slider);

        //14
        protocol.add(value);
        protocol.add(new JLabel("    "));

        //16
        protocol.add(new JLabel("Method"));
        String[] methods = {"Louvain", "Leiden"};
        protocol.add(new JComboBox(methods));
        //18
        protocol.add(new JLabel("Starts"));
        protocol.add(new JTextField("500"));
        //20
        protocol.add(new JLabel("Iterations"));
        protocol.add(new JTextField("100"));
        //22
        protocol.add(new JLabel("Min. Size"));
        protocol.add(new JTextField("20"));

    }

    private static double getSliderValue(int position) {

        HashMap<Integer, JLabel> labels = getLabels();
        return Double.valueOf(((JLabel) labels.get(position)).getText());

    }

    private static HashMap getLabels() {
        HashMap<Integer, JLabel> labels = new HashMap();

        for (int i = 1; i < 21; i++) {
            labels.put(new Integer(i), new JLabel(String.valueOf(i * (0.1))));
        }

        return labels;
    }

    @Override
    public String getDataDescription(ArrayList params) {

        DecimalFormat df = new DecimalFormat("#.##");

        return KEY + '_'
                + String.valueOf((Integer) ((JSpinner) params.get(5)).getValue()) + '_'
                + (String) ((JComboBox) params.get(17)).getSelectedItem() + "_"
                + df.format(getSliderValue(((JSlider) params.get(13)).getValue())) + '_'
                + getCurrentTime();
    }

    /**
     * Performs the KNN and Louvain community detection/clustering based on the
     * parameters.
     *
     * @param al contains all of the parameters in the form of JComponents
     * @param feature the full data to be parsed and analyzed
     * @return true when complete
     */
    @Override
    public boolean process(ArrayList al, double[][] feature, boolean val) {

//                //15
//        protocol.add(new JLabel("Method"));
//        String[] methods = {"Louvain", "Leiden"};
//        protocol.add(new JComboBox(methods)); 
//        //17
//        protocol.add(new JLabel("Starts"));
//        protocol.add(new JTextField("500"));
//        //19
//        protocol.add(new JLabel("Iterations"));
//        protocol.add(new JTextField("100"));
//        //21
//        protocol.add(new JLabel("Min. Size"));
//        protocol.add(new JTextField("20"));
        //make KNN graph
        // --> generate KDTree
        //--> use euclidean distance between cells with n features to define kNN
        int kNeighbors = (Integer) ((JSpinner) al.get(5)).getValue();
        double resolution = (Double) getSliderValue(((JSlider) al.get(13)).getValue());
        String community = (String) ((JComboBox) al.get(17)).getSelectedItem();
        int nRandomStarts = Integer.valueOf(((JTextField) al.get(19)).getText());
        int nIterations = Integer.valueOf(((JTextField) al.get(21)).getText());
        int minClusterSize = Integer.valueOf(((JTextField) al.get(23)).getText());

        int db = 0;

        double[] test = feature[0];

        double[][] data = new double[feature.length][test.length];
        double[][] key = new double[feature.length][1];
        double[] d = new double[test.length];
        //System.out.println("Building kD tree...");

        for (int i = 0; i < feature.length; i++) {
            d = new double[test.length];
            test = feature[i];

            for (int j = 0; j < test.length; j++) {
                d[j] = test[j];
                System.out.println("PROFILING: making data array: " + test[j]);
            }

            data[i] = d;

            double[] k = new double[1];
            k[0] = i;
            key[i] = k;
            //object[i] = i;

        }

        CRSMatrix clusters = CRSMatrix.zero(feature.length, feature.length);

        KDTree tree = new KDTree(data, key);

        double matrixMean = 0;

        for (int k = 0; k < feature.length; k++) {
            Neighbor[] neighborsArray = tree.knn(data[k], kNeighbors);
            double mean = getMeanDistance(neighborsArray);
            matrixMean = matrixMean + mean;
            for (int m = 0; m < kNeighbors; m++) {
                //make list of edges
                Neighbor neighbor = neighborsArray[m];
                if(neighbor.distance <= mean & neighbor.distance > 0){
                double[] id = (double[]) neighbor.value;
                if (clusters.get(k, (int) id[0]) == 0) {
                    clusters.set(k, (int) id[0], mean);
                    clusters.set((int) id[0], k, mean);
                } 
                }
            }
        }

        
        //Adapted from networkanalyzer, copyrights as above.

        double[] nodeWeights = new double[feature.length];

        DynamicDoubleArray[] clustered = new DynamicDoubleArray[100];

        DynamicIntArray[] edges = new DynamicIntArray[2];
        edges[0] = new DynamicIntArray(100);
        edges[1] = new DynamicIntArray(100);
        DynamicDoubleArray edgeWeights = new DynamicDoubleArray(100);
        System.out.println("PROFILING: Building edge table: ");

        int a = 0;
        for (int i = 0; i < feature.length; i++) {
            for (int j = 0; j < feature.length; j++) {
                if (clusters.get(i, j) > 0) {
                    edges[0].append(i);
                    edges[1].append(j);
                    edgeWeights.append(clusters.get(i, j));

                    System.out.println("PROFILING: Edge: " + a + ", " + i + "<---->" + j + " with weight: " + clusters.get(i, j));
                    //} 
                    a++;
                }
            }
        }
            int[][] edges2 = new int[2][];
            edges2[0] = edges[0].toArray();
            edges2[1] = edges[1].toArray();

            Network network = new Network(nodeWeights, edges2,
                    edgeWeights.toArray(), true, true);

            try {
                network.checkIntegrity();
            } catch (java.lang.IllegalArgumentException e) {
                System.out.println("ERROR: KNNLouvain network building error.");
            }

            Clustering initialClustering = new Clustering(network.getNNodes());

            long startTimeAlgorithm = System.currentTimeMillis();

            IterativeCPMClusteringAlgorithm algorithm;

            double resolution2 = resolution / (2 * network.getTotalEdgeWeight() + network.getTotalEdgeWeightSelfLinks());
            Random random = new Random();
            //partition useing community approach
            if (community.equals("Louvain")) {
                algorithm = new LouvainAlgorithm(resolution2, nIterations, random);
            } else {
                algorithm = new LeidenAlgorithm(resolution2, nIterations, LeidenAlgorithm.DEFAULT_RANDOMNESS, random);
            }
            Clustering finalClustering = null;
            double maxQuality = Double.NEGATIVE_INFINITY;
            for (int k = 0; k < nRandomStarts; k++) {
                Clustering clustering = initialClustering.clone();
                algorithm.improveClustering(network, clustering);
                double quality = algorithm.calcQuality(network, clustering);
                if (nRandomStarts > 1) {
                    System.err.println("Quality function in random start " + (k + 1) + " equals " + quality + ".");
                }
                if (quality > maxQuality) {
                    finalClustering = clustering;
                    maxQuality = quality;
                }
            }
            finalClustering.orderClustersByNNodes();
            System.err.println("Running algorithm took " + (System.currentTimeMillis() - startTimeAlgorithm) / 1000 + "s.");
            if (nRandomStarts > 1) {
                System.err.println("Maximum value of quality function in " + nRandomStarts + " random starts equals " + maxQuality + ".");
            } else {
                System.err.println("Quality function equals " + maxQuality + ".");
            }
            if (minClusterSize > 1) {
                System.err.println("Clustering consists of " + finalClustering.getNClusters() + " clusters.");
                System.err.println("Removing clusters consisting of fewer than " + minClusterSize + " nodes.");
                algorithm.removeSmallClustersBasedOnNNodes(network, finalClustering, minClusterSize);
            }
            System.err.println("Final clustering consists of " + finalClustering.getNClusters() + " clusters.");

            int[] membership = finalClustering.getClusters();

            //IJ.log(String.format("PROFILING: Clustering completed in %d s, %d clusters found", (System.currentTimeMillis() - start) / 1000, xm.getNumClusters()));
//
//        //Exists so that MicroExplorer deals with same data structure for both Clustering and Reduction
            ArrayList holder = new ArrayList();
            for (int m : membership) {
                holder.add(m);
            }
            dataResult.add(holder);

            //add good to return VOC as well
            return true;
        }
    
    

    private double getMeanDistance(Neighbor[] neighbors) {
        double result = 0;
        int count = 0;

        for (int i = 0; i < neighbors.length; i++) {
            result = result + neighbors[i].distance;
            count++;
        }

        return result / count;
    }

    private class JLabelLinked extends JLabel implements ChangeListener {

        public JLabelLinked(String text) {
            super(text);

        }

        @Override
        public void stateChanged(ChangeEvent e) {

            int position = ((JSlider) e.getSource()).getValue();

            DecimalFormat df = new DecimalFormat("#.##");
            String lookup = df.format(KNNCommunity.getSliderValue(position));

            this.setText(lookup);
        }

    }

}
