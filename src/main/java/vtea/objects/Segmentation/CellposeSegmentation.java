/*
 * Copyright (C) 2025 Indiana University
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
package vtea.objects.Segmentation;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.scijava.plugin.Plugin;
import vteaobjects.MicroObject;
import vtea.objects.layercake.microVolume;

/**
 * Cellpose segmentation implementation for 3D volumetric data.
 * Integrates Cellpose deep learning-based segmentation into the VTEA framework.
 *
 * @author winfrees
 */
@Plugin(type = Segmentation.class)
public class CellposeSegmentation extends AbstractSegmentation {

    private ImagePlus imageOriginal;
    private ImagePlus imageResult;
    private ImageStack stackOriginal;
    private ImageStack stackResult;

    private List<MicroObject> alVolumes = new ArrayList<>();

    // UI components
    private JComboBox<String> modelComboBox;
    private JTextField diameterField;
    private JTextField flowThresholdField;
    private JTextField cellprobThresholdField;
    private JTextField minVolumeField;
    private JTextField maxVolumeField;
    private JCheckBox useGPUCheckBox;
    private JCheckBox do3DCheckBox;
    private JTextField pythonPathField;

    // Default values
    private static final String[] CELLPOSE_MODELS = {
        "cyto", "cyto2", "cyto3", "nuclei"
    };

    public CellposeSegmentation() {
        VERSION = "1.0";
        AUTHOR = "Seth Winfree";
        COMMENT = "Cellpose deep learning-based segmentation for 3D volumes.";
        NAME = "Cellpose 3D";
        KEY = "Cellpose3D";
        TYPE = "Calculated";
        COMPATIBILITY = "3D";

        // Initialize UI components
        protocol = new ArrayList();

        // Model selection
        modelComboBox = new JComboBox<>(CELLPOSE_MODELS);
        modelComboBox.setSelectedIndex(3); // Default to "nuclei"
        modelComboBox.setPreferredSize(new Dimension(100, 30));

        // Diameter (0 = auto-estimate)
        diameterField = new JTextField("0", 5);
        diameterField.setPreferredSize(new Dimension(50, 30));

        // Flow threshold
        flowThresholdField = new JTextField("0.4", 5);
        flowThresholdField.setPreferredSize(new Dimension(50, 30));

        // Cell probability threshold
        cellprobThresholdField = new JTextField("0.0", 5);
        cellprobThresholdField.setPreferredSize(new Dimension(50, 30));

        // Min/Max volume filters
        minVolumeField = new JTextField("20", 5);
        minVolumeField.setPreferredSize(new Dimension(50, 30));

        maxVolumeField = new JTextField("100000", 7);
        maxVolumeField.setPreferredSize(new Dimension(70, 30));

        // GPU checkbox
        useGPUCheckBox = new JCheckBox("Use GPU", true);

        // 3D processing checkbox
        do3DCheckBox = new JCheckBox("3D Mode", true);

        // Python path (optional)
        pythonPathField = new JTextField("python", 10);
        pythonPathField.setPreferredSize(new Dimension(100, 30));

        // Add to protocol
        protocol.add(new JLabel("Model:"));
        protocol.add(modelComboBox);
        protocol.add(new JLabel("Diameter (px):"));
        protocol.add(diameterField);
        protocol.add(new JLabel("Flow Threshold:"));
        protocol.add(flowThresholdField);
        protocol.add(new JLabel("Cellprob Threshold:"));
        protocol.add(cellprobThresholdField);
        protocol.add(new JLabel("Min Vol (vox):"));
        protocol.add(minVolumeField);
        protocol.add(new JLabel("Max Vol (vox):"));
        protocol.add(maxVolumeField);
        protocol.add(useGPUCheckBox);
        protocol.add(do3DCheckBox);
        protocol.add(new JLabel("Python Path:"));
        protocol.add(pythonPathField);
    }

    @Override
    public void setImage(ImagePlus thresholdPreview) {
        imagePreview = thresholdPreview;
    }

    @Override
    public void updateImage(ImagePlus thresholdPreview) {
        imagePreview = thresholdPreview;
    }

    @Override
    public ArrayList<MicroObject> getObjects() {
        return new ArrayList<>(alVolumes);
    }

    @Override
    public ImagePlus getSegmentation() {
        return this.imageResult;
    }

    @Override
    public JPanel getSegmentationTool() {
        JPanel panel = new JPanel();
        panel.setBackground(vtea._vtea.BACKGROUND);
        // Could add preview or additional controls here
        return panel;
    }

    @Override
    public void doUpdateOfTool() {
        // Optional: Update preview if needed
    }

    /**
     * Main processing method that runs Cellpose segmentation
     */
    @Override
    public boolean process(ImageStack[] is, List protocol, boolean count) {

        System.out.println("PROFILING: Processing with Cellpose 3D segmentation...");

        try {
            // Extract parameters from protocol
            ArrayList al = (ArrayList) protocol.get(3);

            String model = (String) ((JComboBox) al.get(1)).getSelectedItem();
            double diameter = Double.parseDouble(((JTextField) al.get(3)).getText());
            double flowThreshold = Double.parseDouble(((JTextField) al.get(5)).getText());
            double cellprobThreshold = Double.parseDouble(((JTextField) al.get(7)).getText());
            int minVolume = Integer.parseInt(((JTextField) al.get(9)).getText());
            int maxVolume = Integer.parseInt(((JTextField) al.get(11)).getText());
            boolean useGPU = ((JCheckBox) al.get(12)).isSelected();
            boolean do3D = ((JCheckBox) al.get(13)).isSelected();
            String pythonPath = ((JTextField) al.get(15)).getText();

            int segmentationChannel = (int) protocol.get(2);

            System.out.println("PROFILING: Segmentation channel: " + segmentationChannel);
            System.out.println("PROFILING: Model: " + model + ", Diameter: " + diameter +
                             ", 3D: " + do3D + ", GPU: " + useGPU);

            // Get the image stack for the selected channel
            stackOriginal = is[segmentationChannel];
            imageOriginal = new ImagePlus("Cellpose_Input", stackOriginal);

            notifyProgressListeners("Preparing image data...", 5.0);

            // Save image to temporary file for Cellpose
            File tempDir = new File(System.getProperty("java.io.tmpdir"));
            File tempInputFile = File.createTempFile("cellpose_input_", ".tif", tempDir);
            File tempOutputFile = File.createTempFile("cellpose_output_", ".tif", tempDir);

            FileSaver saver = new FileSaver(imageOriginal);
            if (imageOriginal.getStackSize() > 1) {
                saver.saveAsTiffStack(tempInputFile.getAbsolutePath());
            } else {
                saver.saveAsTiff(tempInputFile.getAbsolutePath());
            }

            System.out.println("PROFILING: Saved input to: " + tempInputFile.getAbsolutePath());

            notifyProgressListeners("Running Cellpose...", 10.0);

            // Run Cellpose via Python subprocess
            boolean success = runCellpose(
                tempInputFile.getAbsolutePath(),
                tempOutputFile.getAbsolutePath(),
                model,
                diameter,
                flowThreshold,
                cellprobThreshold,
                useGPU,
                do3D,
                pythonPath
            );

            if (!success) {
                System.err.println("ERROR: Cellpose segmentation failed!");
                tempInputFile.delete();
                tempOutputFile.delete();
                return false;
            }

            notifyProgressListeners("Loading segmentation results...", 60.0);

            // Load the segmentation result
            imageResult = IJ.openImage(tempOutputFile.getAbsolutePath());
            if (imageResult == null) {
                System.err.println("ERROR: Could not load Cellpose output from: " +
                                 tempOutputFile.getAbsolutePath());
                tempInputFile.delete();
                tempOutputFile.delete();
                return false;
            }

            stackResult = imageResult.getStack();

            System.out.println("PROFILING: Loaded segmentation result: " +
                             stackResult.getWidth() + "x" +
                             stackResult.getHeight() + "x" +
                             stackResult.getSize());

            notifyProgressListeners("Parsing objects...", 70.0);

            // Convert labeled image to MicroObjects
            parseLabelsToObjects(stackResult, stackOriginal, minVolume, maxVolume);

            // Clean up temporary files
            tempInputFile.delete();
            tempOutputFile.delete();

            notifyProgressListeners("Complete", 100.0);
            System.out.println("PROFILING: Found " + alVolumes.size() + " objects.");

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ERROR: Exception during Cellpose processing: " +
                             e.getMessage());
            return false;
        }
    }

    /**
     * Runs Cellpose via Python subprocess
     */
    private boolean runCellpose(String inputPath, String outputPath, String model,
                                double diameter, double flowThreshold,
                                double cellprobThreshold, boolean useGPU,
                                boolean do3D, String pythonPath) {
        try {
            // Build Cellpose command
            List<String> command = new ArrayList<>();
            command.add(pythonPath);
            command.add("-m");
            command.add("cellpose");
            command.add("--image_path");
            command.add(inputPath);
            command.add("--pretrained_model");
            command.add(model);
            command.add("--save_tif");
            command.add("--savedir");
            command.add(new File(outputPath).getParent());
            command.add("--flow_threshold");
            command.add(String.valueOf(flowThreshold));
            command.add("--cellprob_threshold");
            command.add(String.valueOf(cellprobThreshold));

            if (diameter > 0) {
                command.add("--diameter");
                command.add(String.valueOf(diameter));
            }

            if (do3D) {
                command.add("--do_3D");
            }

            if (!useGPU) {
                command.add("--use_gpu");
                command.add("false");
            }

            // Add no_npy flag to avoid saving npy files
            command.add("--no_npy");

            System.out.println("PROFILING: Running command: " + String.join(" ", command));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read output
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("CELLPOSE: " + line);
            }

            int exitCode = process.waitFor();
            System.out.println("PROFILING: Cellpose exit code: " + exitCode);

            if (exitCode != 0) {
                return false;
            }

            // Cellpose saves with _cp_masks suffix, rename to expected output
            String parentDir = new File(outputPath).getParent();
            String inputName = new File(inputPath).getName().replace(".tif", "");
            File cellposeOutput = new File(parentDir, inputName + "_cp_masks.tif");

            if (cellposeOutput.exists()) {
                cellposeOutput.renameTo(new File(outputPath));
                return true;
            } else {
                System.err.println("ERROR: Expected output not found: " +
                                 cellposeOutput.getAbsolutePath());
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Converts labeled image stack to MicroObjects
     */
    private void parseLabelsToObjects(ImageStack labels, ImageStack original,
                                      int minVolume, int maxVolume) {

        alVolumes.clear();

        // Find max label value
        int maxLabel = 0;
        for (int z = 0; z < labels.getSize(); z++) {
            ImageProcessor ip = labels.getProcessor(z + 1);
            for (int x = 0; x < labels.getWidth(); x++) {
                for (int y = 0; y < labels.getHeight(); y++) {
                    int value = (int) ip.getPixelValue(x, y);
                    if (value > maxLabel) {
                        maxLabel = value;
                    }
                }
            }
        }

        System.out.println("PROFILING: Max label: " + maxLabel);

        // Create a map to store pixels for each label
        Map<Integer, ArrayList<int[]>> labelPixels = new HashMap<>();

        // Collect all pixels for each label
        for (int z = 0; z < labels.getSize(); z++) {
            ImageProcessor ip = labels.getProcessor(z + 1);

            double progress = 70.0 + (20.0 * z / labels.getSize());
            notifyProgressListeners("Collecting pixels...", progress);

            for (int x = 0; x < labels.getWidth(); x++) {
                for (int y = 0; y < labels.getHeight(); y++) {
                    int label = (int) ip.getPixelValue(x, y);

                    if (label > 0) {
                        if (!labelPixels.containsKey(label)) {
                            labelPixels.put(label, new ArrayList<>());
                        }
                        labelPixels.get(label).add(new int[]{x, y, z});
                    }
                }
            }
        }

        System.out.println("PROFILING: Found " + labelPixels.size() + " unique labels");

        // Convert each label to a MicroObject
        int objectIndex = 0;
        for (Map.Entry<Integer, ArrayList<int[]>> entry : labelPixels.entrySet()) {
            ArrayList<int[]> pixels = entry.getValue();
            int numPixels = pixels.size();

            // Filter by volume
            if (numPixels >= minVolume && numPixels <= maxVolume) {

                // Create arrays for x, y, z coordinates
                int[] xCoords = new int[numPixels];
                int[] yCoords = new int[numPixels];
                int[] zCoords = new int[numPixels];

                for (int i = 0; i < numPixels; i++) {
                    int[] pixel = pixels.get(i);
                    xCoords[i] = pixel[0];
                    yCoords[i] = pixel[1];
                    zCoords[i] = pixel[2];
                }

                // Create MicroObject (using microVolume which extends MicroObject)
                microVolume volume = new microVolume();
                volume.setPixelsX(xCoords);
                volume.setPixelsY(yCoords);
                volume.setPixelsZ(zCoords);
                volume.setCentroid();
                volume.setSerialID(objectIndex);

                alVolumes.add(volume);
                objectIndex++;
            }
        }

        notifyProgressListeners("Finalizing objects...", 95.0);
    }

    @Override
    public boolean copyComponentParameter(String version, ArrayList dComponents,
                                         ArrayList sComponents) {
        try {
            dComponents.clear();

            JComboBox modelBox = (JComboBox) sComponents.get(1);
            JTextField diamField = (JTextField) sComponents.get(3);
            JTextField flowField = (JTextField) sComponents.get(5);
            JTextField cellprobField = (JTextField) sComponents.get(7);
            JTextField minVolField = (JTextField) sComponents.get(9);
            JTextField maxVolField = (JTextField) sComponents.get(11);
            JCheckBox gpuBox = (JCheckBox) sComponents.get(12);
            JCheckBox do3DBox = (JCheckBox) sComponents.get(13);
            JTextField pythonField = (JTextField) sComponents.get(15);

            dComponents.add(new JLabel("Model:"));
            dComponents.add(modelBox);
            dComponents.add(new JLabel("Diameter (px):"));
            dComponents.add(diamField);
            dComponents.add(new JLabel("Flow Threshold:"));
            dComponents.add(flowField);
            dComponents.add(new JLabel("Cellprob Threshold:"));
            dComponents.add(cellprobField);
            dComponents.add(new JLabel("Min Vol (vox):"));
            dComponents.add(minVolField);
            dComponents.add(new JLabel("Max Vol (vox):"));
            dComponents.add(maxVolField);
            dComponents.add(gpuBox);
            dComponents.add(do3DBox);
            dComponents.add(new JLabel("Python Path:"));
            dComponents.add(pythonField);

            return true;
        } catch (Exception e) {
            System.out.println("ERROR: Could not copy parameter(s) for " + NAME);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean loadComponentParameter(String version, ArrayList dComponents,
                                         ArrayList fields) {
        try {
            JComboBox modelBox = (JComboBox) dComponents.get(1);
            JTextField diamField = (JTextField) dComponents.get(3);
            JTextField flowField = (JTextField) dComponents.get(5);
            JTextField cellprobField = (JTextField) dComponents.get(7);
            JTextField minVolField = (JTextField) dComponents.get(9);
            JTextField maxVolField = (JTextField) dComponents.get(11);
            JCheckBox gpuBox = (JCheckBox) dComponents.get(12);
            JCheckBox do3DBox = (JCheckBox) dComponents.get(13);
            JTextField pythonField = (JTextField) dComponents.get(15);

            modelBox.setSelectedItem((String) fields.get(0));
            diamField.setText((String) fields.get(1));
            flowField.setText((String) fields.get(2));
            cellprobField.setText((String) fields.get(3));
            minVolField.setText((String) fields.get(4));
            maxVolField.setText((String) fields.get(5));
            gpuBox.setSelected((Boolean) fields.get(6));
            do3DBox.setSelected((Boolean) fields.get(7));
            pythonField.setText((String) fields.get(8));

            return true;
        } catch (Exception e) {
            System.out.println("ERROR: Could not load parameter(s) for " + NAME);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean saveComponentParameter(String version, ArrayList fields,
                                         ArrayList sComponents) {
        try {
            JComboBox modelBox = (JComboBox) sComponents.get(1);
            JTextField diamField = (JTextField) sComponents.get(3);
            JTextField flowField = (JTextField) sComponents.get(5);
            JTextField cellprobField = (JTextField) sComponents.get(7);
            JTextField minVolField = (JTextField) sComponents.get(9);
            JTextField maxVolField = (JTextField) sComponents.get(11);
            JCheckBox gpuBox = (JCheckBox) sComponents.get(12);
            JCheckBox do3DBox = (JCheckBox) sComponents.get(13);
            JTextField pythonField = (JTextField) sComponents.get(15);

            fields.add(modelBox.getSelectedItem());
            fields.add(diamField.getText());
            fields.add(flowField.getText());
            fields.add(cellprobField.getText());
            fields.add(minVolField.getText());
            fields.add(maxVolField.getText());
            fields.add(gpuBox.isSelected());
            fields.add(do3DBox.isSelected());
            fields.add(pythonField.getText());

            return true;
        } catch (Exception e) {
            System.out.println("ERROR: Could not save parameter(s) for " + NAME +
                             "\n" + e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String runImageJMacroCommand(String str) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sendProgressComment() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getProgressComment() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
