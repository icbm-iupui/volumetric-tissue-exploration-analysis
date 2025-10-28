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

/**
 * Enumeration of available Cellpose segmentation models.
 * Each model is pre-trained for specific cell/tissue types.
 *
 * @author sethwinfree
 */
public enum CellposeModel {

    /**
     * Generalized cytoplasm model (original)
     */
    CYTO("cyto", "Cytoplasm (original)", "Generalized cytoplasm segmentation"),

    /**
     * Updated cytoplasm model (recommended for most use cases)
     */
    CYTO2("cyto2", "Cytoplasm 2", "Updated cytoplasm model (recommended)"),

    /**
     * Nuclear segmentation model
     */
    NUCLEI("nuclei", "Nuclei", "Nuclear segmentation"),

    /**
     * Model trained on tissue sections
     */
    TISSUENET("tissuenet", "TissueNet", "Tissue section segmentation"),

    /**
     * Model trained on live cell imaging
     */
    LIVECELL("livecell", "LiveCell", "Live cell imaging segmentation"),

    /**
     * Custom user-trained model
     */
    CUSTOM("custom", "Custom Model", "User-trained custom model");

    private final String pythonName;
    private final String displayName;
    private final String description;

    /**
     * Constructor
     * @param pythonName name used in Python Cellpose API
     * @param displayName name shown in UI
     * @param description model description
     */
    CellposeModel(String pythonName, String displayName, String description) {
        this.pythonName = pythonName;
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Get the Python API name for this model
     * @return Python model name
     */
    public String getPythonName() {
        return pythonName;
    }

    /**
     * Get display name for UI
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get model description
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get model from Python name
     * @param pythonName Python model name
     * @return CellposeModel or null if not found
     */
    public static CellposeModel fromPythonName(String pythonName) {
        if (pythonName == null) {
            return null;
        }
        for (CellposeModel model : values()) {
            if (model.pythonName.equals(pythonName)) {
                return model;
            }
        }
        return null;
    }

    /**
     * Get model from display name
     * @param displayName display name
     * @return CellposeModel or null if not found
     */
    public static CellposeModel fromDisplayName(String displayName) {
        if (displayName == null) {
            return null;
        }
        for (CellposeModel model : values()) {
            if (model.displayName.equals(displayName)) {
                return model;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
