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
package vtea.featureprocessing;

import java.awt.Component;
import java.util.ArrayList;
import net.imglib2.type.numeric.RealType;
import vtea.VTEAModule;

/**
 * Basic Feature Interface. Sets up all of the methods that all features must
 * employ.
 *
 * @author drewmcnutt
 */
public interface FeatureProcessing<T extends Component, A extends RealType> extends VTEAModule {

    /**
     * Sets the parameters to the specified values.
     *
     * @param al ArrayList of parameters of the feature
     * @return whether completed or not
     */
    public boolean setOptions(ArrayList<T> al);

    /**
     * Retrieves the currently set parameters.
     *
     * @return value of the parameters
     */
    public ArrayList<T> getOptions();

    /**
     * Completes the feature computation.
     *
     * @param al contains all of the parameters of the feature
     * @param feature 2D array of features
     */
    public boolean process(ArrayList al, double[][] feature, boolean validate);

    /**
     * Retrieves the feature type.
     *
     * @return "cluster", "Reduction" or "Other"
     */
    public String getType();

    /**
     * Retrieves the results of the feature
     *
     * @return newly calculated results of the feature
     */
    public ArrayList getResult();

    /**
     * Retrieves the version of the feature
     *
     * @return version
     */
    public String getVersion();

    /**
     * Retrieves the author of the feature
     *
     * @return author name
     */
    public String getAuthor();

    /**
     * Retrieves the comment about the feature
     *
     * @return feature comment
     */
    public String getComment();

    /**
     * Retrieves a descriptive title for the new feature variable. Used for
     * displaying in MicroExplorer
     *
     * @return brief description of the analysis that was performed
     */
    public String getDataDescription(ArrayList params);

    public void sendProgressComment();

    public String getProgressComment();

    public boolean copyComponentParameter(int index, ArrayList<T> dComponents, ArrayList<T> sComponents);

    public String getCurrentTime();
}
