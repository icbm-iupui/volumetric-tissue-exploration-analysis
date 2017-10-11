/* 
 * Copyright (C) 2016 Indiana University
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
package vtea.objects.layercake;

import vtea.objects.layercake.microVolume;
import ij.*;

//new class for defining a region object-reference by volume class
public class microAnalysis extends Object implements Cloneable, java.io.Serializable {

    /**
     * Constants
     */
    /**
     * Variables
     */
    /**
     * Constructors
     */
    public microAnalysis() {
    }

    public float[] analyze(microVolume[] Volumes, int nVolumes, int Channel, int Analytic, int MaskChannel) {

        float[] result = new float[nVolumes];
        Object[][] analysisResultsVolume;
        Object[] analysisResultsMask;

        IJ.log("microAnalysis::analyze                                 Total volumes found:" + (nVolumes));

        for (int n = 0; n <= nVolumes - 1; n++) {

            analysisResultsVolume = Volumes[n].getAnalysisResultsVolume();
            analysisResultsMask = Volumes[n].getAnalysisMaskVolume();

            if (!(analysisResultsVolume[Channel][Analytic] == null)) {
                result[n] = Float.valueOf(analysisResultsVolume[Channel][Analytic].toString());
            }
            if (Channel == MaskChannel) {
                result[n] = Float.valueOf(analysisResultsMask[Analytic].toString());
            }

        }
        return result;
    }

    /**
     * Methods
     */
}
