/*
 * Copyright (C) 2020 Indiana University and 2023 University of Nebraska
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
package vtea.deeplearning;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Parameters for Cellpose segmentation.
 * Uses builder pattern for flexible configuration.
 *
 * @author sethwinfree
 */
public class CellposeParams implements Serializable {

    private static final long serialVersionUID = 1L;

    // Model configuration
    private CellposeModel model = CellposeModel.CYTO2;
    private String customModelPath = null;

    // Segmentation parameters
    private int diameter = 30;          // Expected cell diameter in pixels (0 = auto-detect)
    private double cellprobThreshold = 0.0;  // Cell probability threshold (-6 to 6)
    private double flowThreshold = 0.4;      // Flow error threshold (0.0 to 1.0)

    // Channel configuration
    private int[] channels = new int[]{0, 0}; // [cyto channel, nucleus channel] (0=None)

    // Processing mode
    private boolean do3D = false;       // True 3D segmentation
    private boolean stitch3D = true;    // Stitch 2D masks into 3D (for do3D=false)

    // Preprocessing
    private boolean normalize = true;   // Normalize intensity percentiles
    private double[] normalizeParams = null; // Custom normalization [lower_pct, upper_pct]

    // Hardware
    private boolean useGPU = true;
    private String device = "auto";     // "auto", "cpu", "cuda", "mps"

    // Advanced
    private boolean resample = true;    // Resample dynamics
    private double augment = false;     // Test-time augmentation (not yet implemented)
    private int netAvg = true;          // Use network averaging

    /**
     * Default constructor with sensible defaults
     */
    public CellposeParams() {
    }

    /**
     * Copy constructor
     * @param other params to copy
     */
    public CellposeParams(CellposeParams other) {
        this.model = other.model;
        this.customModelPath = other.customModelPath;
        this.diameter = other.diameter;
        this.cellprobThreshold = other.cellprobThreshold;
        this.flowThreshold = other.flowThreshold;
        this.channels = other.channels.clone();
        this.do3D = other.do3D;
        this.stitch3D = other.stitch3D;
        this.normalize = other.normalize;
        this.normalizeParams = other.normalizeParams != null ? other.normalizeParams.clone() : null;
        this.useGPU = other.useGPU;
        this.device = other.device;
        this.resample = other.resample;
        this.augment = other.augment;
        this.netAvg = other.netAvg;
    }

    // Getters
    public CellposeModel getModel() { return model; }
    public String getCustomModelPath() { return customModelPath; }
    public int getDiameter() { return diameter; }
    public double getCellprobThreshold() { return cellprobThreshold; }
    public double getFlowThreshold() { return flowThreshold; }
    public int[] getChannels() { return channels.clone(); }
    public boolean isDo3D() { return do3D; }
    public boolean isStitch3D() { return stitch3D; }
    public boolean isNormalize() { return normalize; }
    public double[] getNormalizeParams() { return normalizeParams != null ? normalizeParams.clone() : null; }
    public boolean isUseGPU() { return useGPU; }
    public String getDevice() { return device; }
    public boolean isResample() { return resample; }
    public boolean isAugment() { return augment; }
    public boolean isNetAvg() { return netAvg; }

    // Setters
    public void setModel(CellposeModel model) { this.model = model; }
    public void setCustomModelPath(String path) { this.customModelPath = path; }
    public void setDiameter(int diameter) { this.diameter = diameter; }
    public void setCellprobThreshold(double threshold) { this.cellprobThreshold = threshold; }
    public void setFlowThreshold(double threshold) { this.flowThreshold = threshold; }
    public void setChannels(int[] channels) { this.channels = channels.clone(); }
    public void setDo3D(boolean do3D) { this.do3D = do3D; }
    public void setStitch3D(boolean stitch3D) { this.stitch3D = stitch3D; }
    public void setNormalize(boolean normalize) { this.normalize = normalize; }
    public void setNormalizeParams(double[] params) { this.normalizeParams = params != null ? params.clone() : null; }
    public void setUseGPU(boolean useGPU) { this.useGPU = useGPU; }
    public void setDevice(String device) { this.device = device; }
    public void setResample(boolean resample) { this.resample = resample; }
    public void setAugment(boolean augment) { this.augment = augment; }
    public void setNetAvg(boolean netAvg) { this.netAvg = netAvg; }

    /**
     * Validate parameters
     * @throws IllegalArgumentException if invalid
     */
    public void validate() throws IllegalArgumentException {
        if (diameter < 0) {
            throw new IllegalArgumentException("Diameter must be >= 0 (0 = auto-detect)");
        }
        if (cellprobThreshold < -6 || cellprobThreshold > 6) {
            throw new IllegalArgumentException("Cell probability threshold must be between -6 and 6");
        }
        if (flowThreshold < 0 || flowThreshold > 1) {
            throw new IllegalArgumentException("Flow threshold must be between 0 and 1");
        }
        if (channels == null || channels.length != 2) {
            throw new IllegalArgumentException("Channels must be array of length 2");
        }
        if (model == CellposeModel.CUSTOM && (customModelPath == null || customModelPath.isEmpty())) {
            throw new IllegalArgumentException("Custom model path required for CUSTOM model type");
        }
    }

    /**
     * Convert to JSON string for Python communication
     * @return JSON representation
     */
    public String toJSON() {
        StringBuilder json = new StringBuilder("{");
        json.append("\"model\":\"").append(model.getPythonName()).append("\",");
        if (customModelPath != null) {
            json.append("\"model_path\":\"").append(customModelPath.replace("\\", "\\\\")).append("\",");
        }
        json.append("\"diameter\":").append(diameter).append(",");
        json.append("\"cellprob_threshold\":").append(cellprobThreshold).append(",");
        json.append("\"flow_threshold\":").append(flowThreshold).append(",");
        json.append("\"channels\":[").append(channels[0]).append(",").append(channels[1]).append("],");
        json.append("\"do_3D\":").append(do3D).append(",");
        json.append("\"stitch_threshold\":").append(stitch3D ? 0.0 : -1.0).append(",");
        json.append("\"normalize\":").append(normalize).append(",");
        if (normalizeParams != null) {
            json.append("\"normalize_params\":[").append(normalizeParams[0]).append(",").append(normalizeParams[1]).append("],");
        }
        json.append("\"use_gpu\":").append(useGPU).append(",");
        json.append("\"device\":\"").append(device).append("\",");
        json.append("\"resample\":").append(resample).append(",");
        json.append("\"augment\":").append(augment).append(",");
        json.append("\"net_avg\":").append(netAvg);
        json.append("}");
        return json.toString();
    }

    @Override
    public String toString() {
        return String.format("CellposeParams[model=%s, diameter=%d, do3D=%b, GPU=%b]",
                           model, diameter, do3D, useGPU);
    }

    /**
     * Builder for CellposeParams
     */
    public static class Builder {
        private CellposeParams params;

        public Builder() {
            params = new CellposeParams();
        }

        public Builder model(CellposeModel model) {
            params.model = model;
            return this;
        }

        public Builder customModelPath(String path) {
            params.customModelPath = path;
            return this;
        }

        public Builder diameter(int diameter) {
            params.diameter = diameter;
            return this;
        }

        public Builder cellprobThreshold(double threshold) {
            params.cellprobThreshold = threshold;
            return this;
        }

        public Builder flowThreshold(double threshold) {
            params.flowThreshold = threshold;
            return this;
        }

        public Builder channels(int cytoChannel, int nucleusChannel) {
            params.channels = new int[]{cytoChannel, nucleusChannel};
            return this;
        }

        public Builder do3D(boolean do3D) {
            params.do3D = do3D;
            return this;
        }

        public Builder stitch3D(boolean stitch) {
            params.stitch3D = stitch;
            return this;
        }

        public Builder normalize(boolean normalize) {
            params.normalize = normalize;
            return this;
        }

        public Builder normalizeParams(double lower, double upper) {
            params.normalizeParams = new double[]{lower, upper};
            return this;
        }

        public Builder useGPU(boolean useGPU) {
            params.useGPU = useGPU;
            return this;
        }

        public Builder device(String device) {
            params.device = device;
            return this;
        }

        public Builder resample(boolean resample) {
            params.resample = resample;
            return this;
        }

        public Builder augment(boolean augment) {
            params.augment = augment;
            return this;
        }

        public Builder netAvg(boolean netAvg) {
            params.netAvg = netAvg;
            return this;
        }

        public CellposeParams build() {
            params.validate();
            return params;
        }
    }

    /**
     * Create default parameters for cytoplasm segmentation
     * @return default cyto params
     */
    public static CellposeParams createCytoDefault() {
        return new Builder()
                .model(CellposeModel.CYTO2)
                .diameter(30)
                .channels(0, 0)
                .do3D(false)
                .build();
    }

    /**
     * Create default parameters for nuclear segmentation
     * @return default nucleus params
     */
    public static CellposeParams createNucleiDefault() {
        return new Builder()
                .model(CellposeModel.NUCLEI)
                .diameter(17)
                .channels(0, 0)
                .do3D(false)
                .build();
    }

    /**
     * Create 3D segmentation parameters
     * @param diameter cell diameter
     * @return 3D params
     */
    public static CellposeParams create3D(int diameter) {
        return new Builder()
                .model(CellposeModel.CYTO2)
                .diameter(diameter)
                .do3D(true)
                .stitch3D(false)
                .build();
    }
}
