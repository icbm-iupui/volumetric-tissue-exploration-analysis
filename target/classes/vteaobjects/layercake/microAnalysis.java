package vteaobjects.layercake;

import vteaobjects.layercake.microVolume;
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
