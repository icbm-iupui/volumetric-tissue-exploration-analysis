/*
 * Copyright (C) 2025 University of Nebraska
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
import ij.gui.Roi;
import ij.io.DirectoryChooser;
import ij.process.ImageProcessor;
import io.bioimage.modelrunner.model.Model;
import io.bioimage.modelrunner.tensor.Tensor;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.scijava.plugin.Plugin;
import vtea.protocol.setup.MicroThresholdAdjuster;
import vteaobjects.MicroObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * DeepImageJ-based segmentation using JDLL (Java Deep Learning Library)
 * Supports TensorFlow, PyTorch, and ONNX models via the BioImage.IO model zoo
 *
 * @author winfrees
 */
@Plugin(type = Segmentation.class)
public class DeepImageJSegmentation extends AbstractSegmentation {

    private ImagePlus imageOriginal;
    private ImagePlus imageResult;
    private ImageStack stackOriginal;
    protected ImageStack stackResult;

    private ArrayList<MicroObject> segmentedObjects = new ArrayList<MicroObject>();

    // UI components
    private JTextAreaModelPath modelPath;
    private JTextField confidenceThreshold;
    private JComboBox<String> postProcessing;
    private JLabel statusLabel;

    // Model and engine management
    private Model deepLearningModel;
    private String currentModelPath = "";

    public DeepImageJSegmentation() {
        VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "Deep learning segmentation using DeepImageJ/JDLL";
        NAME = "DeepImageJ Segmentation";
        KEY = "DeepImageJSegmentation";
        TYPE = "Calculated";
        COMPATIBILITY = "3D";

        protocol = new ArrayList();

        // Model path selector
        modelPath = new JTextAreaModelPath("Click to select model directory");
        modelPath.setPreferredSize(new Dimension(250, 30));
        modelPath.setMinimumSize(new Dimension(250, 30));

        // Confidence threshold
        confidenceThreshold = new JTextField("0.5", 10);
        confidenceThreshold.setPreferredSize(new Dimension(50, 25));

        // Post-processing options
        String[] postProcOptions = {"None", "Watershed", "Fill Holes"};
        postProcessing = new JComboBox<>(postProcOptions);
        postProcessing.setPreferredSize(new Dimension(120, 25));

        // Status label
        statusLabel = new JLabel("No model loaded");
        statusLabel.setForeground(Color.ORANGE);

        protocol.add(modelPath);
        protocol.add(new JLabel("Confidence:"));
        protocol.add(confidenceThreshold);
        protocol.add(new JLabel("Post-processing:"));
        protocol.add(postProcessing);
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
        return segmentedObjects;
    }

    @Override
    public ImagePlus getSegmentation() {
        return this.imageResult;
    }

    @Override
    public JPanel getSegmentationTool() {
        JPanel container = new JPanel();
        container.setBackground(vtea._vtea.BACKGROUND);
        container.setPreferredSize(new Dimension(300, 200));

        JPanel panel = new JPanel();
        panel.setBackground(vtea._vtea.BACKGROUND);
        panel.setLayout(new GridBagLayout());

        GridBagConstraints layoutConstraints = new GridBagConstraints();
        layoutConstraints.insets = new Insets(5, 5, 5, 5);

        // Model path label
        layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 0;
        layoutConstraints.gridwidth = 2;
        panel.add(new JLabel("BioImage.IO Model Directory:"), layoutConstraints);

        // Model path text area
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 1;
        layoutConstraints.gridwidth = 2;
        panel.add(modelPath, layoutConstraints);

        // Status label
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 2;
        layoutConstraints.gridwidth = 2;
        panel.add(statusLabel, layoutConstraints);

        // Confidence threshold
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 3;
        layoutConstraints.gridwidth = 1;
        layoutConstraints.fill = GridBagConstraints.NONE;
        layoutConstraints.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Confidence:"), layoutConstraints);

        layoutConstraints.gridx = 1;
        layoutConstraints.gridy = 3;
        layoutConstraints.anchor = GridBagConstraints.WEST;
        panel.add(confidenceThreshold, layoutConstraints);

        // Post-processing
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 4;
        layoutConstraints.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Post-processing:"), layoutConstraints);

        layoutConstraints.gridx = 1;
        layoutConstraints.gridy = 4;
        layoutConstraints.anchor = GridBagConstraints.WEST;
        panel.add(postProcessing, layoutConstraints);

        container.add(panel);
        return container;
    }

    @Override
    public void doUpdateOfTool() {
        // Update UI if needed
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

    /**
     * Main processing method for 3D segmentation
     */
    @Override
    public boolean process(ImageStack[] is, List protocol, boolean count) {

        System.out.println("PROFILING: Starting DeepImageJ segmentation...");

        try {
            // Extract parameters from protocol
            ArrayList al = (ArrayList) protocol.get(3);

            JTextAreaModelPath modelPathField = (JTextAreaModelPath) al.get(0);
            // Skip labels at indices 1 and 3
            JTextField confidenceField = (JTextField) al.get(2);
            JComboBox postProcField = (JComboBox) al.get(4);

            String modelDir = modelPathField.getText();
            double confidence = Double.parseDouble(confidenceField.getText());
            String postProc = (String) postProcField.getSelectedItem();

            int segmentationChannel = (int) protocol.get(2);

            if (modelDir == null || modelDir.isEmpty() || modelDir.equals("Click to select model directory")) {
                System.out.println("ERROR: No model directory selected.");
                notifyProgressListeners("ERROR: No model selected", 0.0);
                return false;
            }

            // Prepare input image
            stackOriginal = is[segmentationChannel];
            imageOriginal = new ImagePlus("Original", stackOriginal);

            notifyProgressListeners("Loading deep learning model...", 5.0);

            // Load model if not already loaded or if model path changed
            if (!modelDir.equals(currentModelPath)) {
                loadModel(modelDir);
                currentModelPath = modelDir;
            }

            if (deepLearningModel == null) {
                System.out.println("ERROR: Failed to load model.");
                notifyProgressListeners("ERROR: Model loading failed", 0.0);
                return false;
            }

            notifyProgressListeners("Running model inference...", 20.0);

            // Run inference
            ImagePlus prediction = runModelInference(imageOriginal);

            if (prediction == null) {
                System.out.println("ERROR: Model inference failed.");
                notifyProgressListeners("ERROR: Inference failed", 0.0);
                return false;
            }

            notifyProgressListeners("Applying confidence threshold...", 60.0);

            // Apply confidence threshold
            imageResult = applyConfidenceThreshold(prediction, confidence);

            notifyProgressListeners("Post-processing...", 70.0);

            // Apply post-processing
            imageResult = applyPostProcessing(imageResult, postProc);

            notifyProgressListeners("Extracting objects...", 85.0);

            // Convert segmentation mask to MicroObject list
            segmentedObjects = extractObjectsFromMask(imageResult, segmentationChannel, is);

            System.out.println("PROFILING: Found " + segmentedObjects.size() + " objects.");
            notifyProgressListeners("Complete - found " + segmentedObjects.size() + " objects", 100.0);

            return true;

        } catch (Exception ex) {
            System.out.println("ERROR: DeepImageJ segmentation failed: " + ex.getMessage());
            ex.printStackTrace();
            notifyProgressListeners("ERROR: " + ex.getMessage(), 0.0);
            return false;
        }
    }

    @Override
    public boolean process(ImagePlus imp, List details, boolean calculate) {
        // Convert ImagePlus to ImageStack array and call 3D version
        ImageStack[] stacks = new ImageStack[1];
        stacks[0] = imp.getImageStack();
        return process(stacks, details, calculate);
    }

    /**
     * Load a BioImage.IO model from directory
     */
    private void loadModel(String modelDirectory) {
        try {
            File modelDir = new File(modelDirectory);
            if (!modelDir.exists() || !modelDir.isDirectory()) {
                System.out.println("ERROR: Invalid model directory: " + modelDirectory);
                statusLabel.setText("Invalid model directory");
                statusLabel.setForeground(Color.RED);
                return;
            }

            // Check for rdf.yaml (BioImage.IO model descriptor)
            File rdfFile = new File(modelDir, "rdf.yaml");
            if (!rdfFile.exists()) {
                System.out.println("WARNING: No rdf.yaml found, looking for model.yaml...");
                rdfFile = new File(modelDir, "model.yaml");
            }

            if (!rdfFile.exists()) {
                System.out.println("ERROR: No model descriptor found (rdf.yaml or model.yaml)");
                statusLabel.setText("No model descriptor found");
                statusLabel.setForeground(Color.RED);
                return;
            }

            System.out.println("PROFILING: Loading model from: " + modelDirectory);

            // Create JDLL model
            deepLearningModel = Model.createBioimageioModel(modelDirectory);
            deepLearningModel.loadModel();

            System.out.println("PROFILING: Model loaded successfully");
            statusLabel.setText("Model loaded: " + modelDir.getName());
            statusLabel.setForeground(Color.GREEN);

        } catch (Exception e) {
            System.out.println("ERROR: Failed to load model: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("Model loading failed");
            statusLabel.setForeground(Color.RED);
            deepLearningModel = null;
        }
    }

    /**
     * Run model inference on the input image
     */
    private ImagePlus runModelInference(ImagePlus input) {
        try {
            // Convert ImagePlus to ImgLib2 Img
            Img<FloatType> inputImg = ImageJFunctions.convertFloat(input);

            // Create input tensor
            Tensor<FloatType> inputTensor = Tensor.build("input", "XYZC", inputImg);

            // Create output tensor placeholder
            Tensor<FloatType> outputTensor = Tensor.buildEmptyTensor("output", "XYZC");

            // Prepare input/output lists
            List<Tensor<?>> inputs = new ArrayList<>();
            List<Tensor<?>> outputs = new ArrayList<>();
            inputs.add(inputTensor);
            outputs.add(outputTensor);

            // Run model
            deepLearningModel.runModel(inputs, outputs);

            // Get output tensor
            Tensor<?> result = outputs.get(0);
            RandomAccessibleInterval<?> resultImg = result.getData();

            // Convert back to ImagePlus
            ImagePlus resultImp = ImageJFunctions.wrap((RandomAccessibleInterval) resultImg, "prediction");

            return resultImp;

        } catch (Exception e) {
            System.out.println("ERROR: Model inference failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Apply confidence threshold to prediction
     */
    private ImagePlus applyConfidenceThreshold(ImagePlus prediction, double threshold) {
        ImageStack stack = prediction.getImageStack();
        ImageStack resultStack = stack.duplicate();

        for (int z = 0; z < resultStack.getSize(); z++) {
            ImageProcessor ip = resultStack.getProcessor(z + 1);
            for (int x = 0; x < ip.getWidth(); x++) {
                for (int y = 0; y < ip.getHeight(); y++) {
                    float value = ip.getPixelValue(x, y);
                    if (value >= threshold) {
                        ip.setf(x, y, 255);
                    } else {
                        ip.setf(x, y, 0);
                    }
                }
            }
        }

        ImagePlus result = new ImagePlus("Thresholded", resultStack);
        IJ.run(result, "8-bit", "");
        return result;
    }

    /**
     * Apply post-processing to segmentation result
     */
    private ImagePlus applyPostProcessing(ImagePlus mask, String method) {
        ImagePlus result = mask.duplicate();

        if (method == null || method.equals("None")) {
            return result;
        }

        switch (method) {
            case "Watershed":
                notifyProgressListeners("Applying watershed...", 75.0);
                IJ.run(result, "Watershed", "stack");
                break;
            case "Fill Holes":
                notifyProgressListeners("Filling holes...", 75.0);
                IJ.run(result, "Fill Holes", "stack");
                break;
            default:
                break;
        }

        return result;
    }

    /**
     * Extract MicroObject instances from segmentation mask
     */
    private ArrayList<MicroObject> extractObjectsFromMask(ImagePlus maskImage, int channel, ImageStack[] originalStacks) {
        ArrayList<MicroObject> objects = new ArrayList<>();
        ImageStack maskStack = maskImage.getImageStack();

        // Find unique object labels (for labeled masks)
        HashMap<Double, ArrayList<int[]>> objectPixels = new HashMap<>();

        double max = maskStack.getSize() * maskStack.getWidth() * maskStack.getHeight();
        double count = 0;

        for (int z = 0; z < maskStack.getSize(); z++) {
            for (int x = 0; x < maskStack.getWidth(); x++) {
                for (int y = 0; y < maskStack.getHeight(); y++) {

                    count++;
                    if (count % 10000 == 0) {
                        double progress = 85.0 + (10.0 * count / max);
                        notifyProgressListeners("Parsing pixels...", progress);
                    }

                    double value = maskStack.getVoxel(x, y, z);

                    if (value > 0) {
                        // For binary masks, treat all non-zero as single object
                        // For labeled masks, treat each unique value as separate object
                        if (!objectPixels.containsKey(value)) {
                            objectPixels.put(value, new ArrayList<>());
                        }
                        int[] pixel = {x, y, z};
                        objectPixels.get(value).add(pixel);
                    }
                }
            }
        }

        // Convert pixel lists to MicroObjects
        int objectIndex = 0;
        for (Double label : objectPixels.keySet()) {
            ArrayList<int[]> pixels = objectPixels.get(label);

            if (pixels.size() == 0) continue;

            int[] xArray = new int[pixels.size()];
            int[] yArray = new int[pixels.size()];
            int[] zArray = new int[pixels.size()];

            for (int i = 0; i < pixels.size(); i++) {
                int[] pixel = pixels.get(i);
                xArray[i] = pixel[0];
                yArray[i] = pixel[1];
                zArray[i] = pixel[2];
            }

            ArrayList<int[]> pixelArrays = new ArrayList<>();
            pixelArrays.add(xArray);
            pixelArrays.add(yArray);
            pixelArrays.add(zArray);

            MicroObject obj = new MicroObject(pixelArrays, channel, originalStacks, objectIndex);
            objects.add(obj);
            objectIndex++;
        }

        return objects;
    }

    /**
     * Custom text area for model path selection
     */
    class JTextAreaModelPath extends JTextArea {

        private File modelDirectory;

        public JTextAreaModelPath(String s) {
            super(s);
            this.setEditable(false);
            this.setLineWrap(true);
            this.setWrapStyleWord(true);

            // Add mouse listener for directory selection
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() >= 1) {
                        selectModelDirectory();
                    }
                }
            });
        }

        private void selectModelDirectory() {
            DirectoryChooser dc = new DirectoryChooser("Select BioImage.IO Model Directory");
            String directory = dc.getDirectory();

            if (directory != null) {
                modelDirectory = new File(directory);
                setText(directory);

                // Try to load the model immediately
                loadModel(directory);
            }
        }

        public File getModelDirectory() {
            return modelDirectory;
        }
    }

    /**
     * Copy component parameters for protocol saving
     */
    @Override
    public boolean copyComponentParameter(String version, ArrayList dComponents, ArrayList sComponents) {
        try {
            dComponents.clear();
            JTextAreaModelPath f1 = (JTextAreaModelPath) sComponents.get(0);
            JLabel l1 = (JLabel) sComponents.get(1);
            JTextField f2 = (JTextField) sComponents.get(2);
            JLabel l2 = (JLabel) sComponents.get(3);
            JComboBox f3 = (JComboBox) sComponents.get(4);

            dComponents.add(f1);
            dComponents.add(l1);
            dComponents.add(f2);
            dComponents.add(l2);
            dComponents.add(f3);

            return true;
        } catch (Exception e) {
            System.out.println("ERROR: Could not copy parameter(s) for " + NAME);
            return false;
        }
    }

    /**
     * Load component parameters from saved protocol
     */
    @Override
    public boolean loadComponentParameter(String version, ArrayList dComponents, ArrayList fields) {
        try {
            dComponents.clear();
            JTextAreaModelPath n1 = (JTextAreaModelPath) fields.get(0);
            JLabel l1 = (JLabel) fields.get(1);
            JTextField n2 = (JTextField) fields.get(2);
            JLabel l2 = (JLabel) fields.get(3);
            JComboBox n3 = (JComboBox) fields.get(4);

            dComponents.add(n1);
            dComponents.add(l1);
            dComponents.add(n2);
            dComponents.add(l2);
            dComponents.add(n3);

            return true;
        } catch (Exception e) {
            System.out.println("ERROR: Could not load parameter(s) for " + NAME);
            return false;
        }
    }

    /**
     * Save component parameters to protocol
     */
    @Override
    public boolean saveComponentParameter(String version, ArrayList fields, ArrayList sComponents) {
        try {
            fields.add((JTextAreaModelPath) sComponents.get(0));
            fields.add((JLabel) sComponents.get(1));
            fields.add((JTextField) sComponents.get(2));
            fields.add((JLabel) sComponents.get(3));
            fields.add((JComboBox) sComponents.get(4));

            return true;
        } catch (Exception e) {
            System.out.println("ERROR: Could not save parameter(s) for " + NAME + "\n" + e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Cleanup resources
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            if (deepLearningModel != null) {
                deepLearningModel.closeModel();
            }
        } finally {
            super.finalize();
        }
    }
}
