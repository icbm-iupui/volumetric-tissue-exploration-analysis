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
package vtea.partition;

import smile.neighbor.KDTree;
import smile.neighbor.Neighbor;
import vteaobjects.MicroObject;

import java.util.*;

/**
 * Stitches objects from multiple chunks by merging objects that span chunk boundaries.
 * Uses spatial indexing (kDTree) for efficient nearest-neighbor searches.
 *
 * @author sethwinfree
 */
public class ObjectStitcher {

    private double overlapThreshold = 0.1;  // Minimum overlap to merge (10%)
    private double distanceThreshold = 10.0; // Maximum centroid distance to consider merging
    private boolean useIntensityCorrelation = true;
    private double intensityCorrelationThreshold = 0.7;

    /**
     * Default constructor
     */
    public ObjectStitcher() {
    }

    /**
     * Constructor with custom thresholds
     * @param overlapThreshold minimum overlap percentage (0.0 to 1.0)
     * @param distanceThreshold maximum centroid distance
     */
    public ObjectStitcher(double overlapThreshold, double distanceThreshold) {
        this.overlapThreshold = overlapThreshold;
        this.distanceThreshold = distanceThreshold;
    }

    /**
     * Set overlap threshold
     * @param threshold overlap percentage (0.0 to 1.0)
     */
    public void setOverlapThreshold(double threshold) {
        this.overlapThreshold = Math.max(0.0, Math.min(1.0, threshold));
    }

    /**
     * Set distance threshold
     * @param threshold maximum distance in pixels
     */
    public void setDistanceThreshold(double threshold) {
        this.distanceThreshold = threshold;
    }

    /**
     * Set whether to use intensity correlation
     * @param use true to use intensity correlation
     */
    public void setUseIntensityCorrelation(boolean use) {
        this.useIntensityCorrelation = use;
    }

    /**
     * Stitch objects from multiple chunks
     * @param objects list of all objects from all chunks
     * @return merged list of objects
     */
    public List<MicroObject> stitchObjects(List<MicroObject> objects) {
        if (objects == null || objects.isEmpty()) {
            return new ArrayList<>();
        }

        // Separate boundary objects from core objects
        List<MicroObject> boundaryObjects = new ArrayList<>();
        List<MicroObject> coreObjects = new ArrayList<>();

        for (MicroObject obj : objects) {
            Object boundaryProp = obj.getProperty("chunkBoundary");
            boolean isBoundary = boundaryProp != null && (Boolean) boundaryProp;

            if (isBoundary || isNearChunkBoundary(obj)) {
                boundaryObjects.add(obj);
            } else {
                coreObjects.add(obj);
            }
        }

        // Build spatial index for boundary objects
        if (boundaryObjects.isEmpty()) {
            return new ArrayList<>(objects);
        }

        // Merge boundary objects
        List<MicroObject> mergedBoundary = mergeBoundaryObjects(boundaryObjects);

        // Combine core and merged boundary objects
        List<MicroObject> result = new ArrayList<>(coreObjects);
        result.addAll(mergedBoundary);

        return result;
    }

    /**
     * Check if object is near a chunk boundary
     * @param obj MicroObject to check
     * @return true if near boundary
     */
    private boolean isNearChunkBoundary(MicroObject obj) {
        Object chunkBoundaryProp = obj.getProperty("chunkBoundary");
        if (chunkBoundaryProp != null) {
            return (Boolean) chunkBoundaryProp;
        }
        return false;
    }

    /**
     * Merge boundary objects using kDTree spatial indexing
     * @param boundaryObjects list of boundary objects
     * @return merged list
     */
    private List<MicroObject> mergeBoundaryObjects(List<MicroObject> boundaryObjects) {
        if (boundaryObjects.size() < 2) {
            return boundaryObjects;
        }

        // Build kDTree with object centroids
        int n = boundaryObjects.size();
        double[][] centroids = new double[n][3];

        for (int i = 0; i < n; i++) {
            MicroObject obj = boundaryObjects.get(i);
            double[] centroid = obj.getCentroidXYZ_AsDbl();
            if (centroid != null && centroid.length >= 3) {
                centroids[i][0] = centroid[0];
                centroids[i][1] = centroid[1];
                centroids[i][2] = centroid[2];
            }
        }

        KDTree<MicroObject> kdTree = new KDTree<>(centroids, boundaryObjects.toArray(new MicroObject[0]));

        // Track which objects have been merged
        Set<Integer> merged = new HashSet<>();
        List<MicroObject> result = new ArrayList<>();

        // Find and merge overlapping objects
        for (int i = 0; i < boundaryObjects.size(); i++) {
            if (merged.contains(i)) {
                continue;
            }

            MicroObject obj1 = boundaryObjects.get(i);
            double[] centroid1 = obj1.getCentroidXYZ_AsDbl();

            if (centroid1 == null || centroid1.length < 3) {
                result.add(obj1);
                continue;
            }

            // Search for nearby objects
            Neighbor<double[], MicroObject>[] neighbors = kdTree.range(centroid1, distanceThreshold);

            List<MicroObject> mergeGroup = new ArrayList<>();
            mergeGroup.add(obj1);
            merged.add(i);

            // Check each neighbor for merging criteria
            for (Neighbor<double[], MicroObject> neighbor : neighbors) {
                MicroObject obj2 = neighbor.value;
                int j = boundaryObjects.indexOf(obj2);

                if (j != i && !merged.contains(j) && shouldMerge(obj1, obj2)) {
                    mergeGroup.add(obj2);
                    merged.add(j);
                }
            }

            // Merge the group into a single object
            if (mergeGroup.size() > 1) {
                MicroObject mergedObj = mergeObjectGroup(mergeGroup);
                result.add(mergedObj);
            } else {
                result.add(obj1);
            }
        }

        return result;
    }

    /**
     * Determine if two objects should be merged
     * @param obj1 first object
     * @param obj2 second object
     * @return true if should merge
     */
    private boolean shouldMerge(MicroObject obj1, MicroObject obj2) {
        // Check from different chunks
        Object chunk1 = obj1.getProperty("chunkId");
        Object chunk2 = obj2.getProperty("chunkId");

        if (chunk1 != null && chunk2 != null && chunk1.equals(chunk2)) {
            return false; // Same chunk, don't merge
        }

        // Check centroid distance
        double[] c1 = obj1.getCentroidXYZ_AsDbl();
        double[] c2 = obj2.getCentroidXYZ_AsDbl();

        if (c1 == null || c2 == null || c1.length < 3 || c2.length < 3) {
            return false;
        }

        double distance = Math.sqrt(
            Math.pow(c1[0] - c2[0], 2) +
            Math.pow(c1[1] - c2[1], 2) +
            Math.pow(c1[2] - c2[2], 2)
        );

        if (distance > distanceThreshold) {
            return false;
        }

        // Check bounding box overlap
        double overlap = calculateOverlap(obj1, obj2);
        if (overlap < overlapThreshold) {
            return false;
        }

        // Optional: Check intensity correlation
        if (useIntensityCorrelation) {
            double correlation = calculateIntensityCorrelation(obj1, obj2);
            if (correlation < intensityCorrelationThreshold) {
                return false;
            }
        }

        return true;
    }

    /**
     * Calculate bounding box overlap between two objects
     * @param obj1 first object
     * @param obj2 second object
     * @return overlap ratio (0.0 to 1.0)
     */
    private double calculateOverlap(MicroObject obj1, MicroObject obj2) {
        // Get bounding boxes
        double[] bb1 = getBoundingBox(obj1);
        double[] bb2 = getBoundingBox(obj2);

        if (bb1 == null || bb2 == null) {
            return 0.0;
        }

        // Calculate intersection volume
        double x1 = Math.max(bb1[0], bb2[0]);
        double y1 = Math.max(bb1[1], bb2[1]);
        double z1 = Math.max(bb1[2], bb2[2]);
        double x2 = Math.min(bb1[3], bb2[3]);
        double y2 = Math.min(bb1[4], bb2[4]);
        double z2 = Math.min(bb1[5], bb2[5]);

        if (x2 <= x1 || y2 <= y1 || z2 <= z1) {
            return 0.0; // No intersection
        }

        double intersectionVolume = (x2 - x1) * (y2 - y1) * (z2 - z1);

        // Calculate union volume
        double vol1 = (bb1[3] - bb1[0]) * (bb1[4] - bb1[1]) * (bb1[5] - bb1[2]);
        double vol2 = (bb2[3] - bb2[0]) * (bb2[4] - bb2[1]) * (bb2[5] - bb2[2]);
        double unionVolume = vol1 + vol2 - intersectionVolume;

        return unionVolume > 0 ? intersectionVolume / unionVolume : 0.0;
    }

    /**
     * Get bounding box of an object [xmin, ymin, zmin, xmax, ymax, zmax]
     * @param obj MicroObject
     * @return bounding box array
     */
    private double[] getBoundingBox(MicroObject obj) {
        // Try to get from stored properties first
        Object bbox = obj.getProperty("boundingBox");
        if (bbox instanceof double[]) {
            return (double[]) bbox;
        }

        // Fallback: estimate from centroid and volume
        double[] centroid = obj.getCentroidXYZ_AsDbl();
        if (centroid == null || centroid.length < 3) {
            return null;
        }

        // Rough estimate: assume spherical object
        double volume = obj.getVoxelCount();
        double radius = Math.cbrt(3.0 * volume / (4.0 * Math.PI));

        return new double[]{
            centroid[0] - radius, centroid[1] - radius, centroid[2] - radius,
            centroid[0] + radius, centroid[1] + radius, centroid[2] + radius
        };
    }

    /**
     * Calculate intensity correlation between two objects
     * @param obj1 first object
     * @param obj2 second object
     * @return correlation coefficient (0.0 to 1.0)
     */
    private double calculateIntensityCorrelation(MicroObject obj1, MicroObject obj2) {
        // Simple comparison using mean intensities
        // Could be enhanced with actual intensity distribution correlation

        double[][] m1 = obj1.getMeasurements();
        double[][] m2 = obj2.getMeasurements();

        if (m1 == null || m2 == null || m1.length == 0 || m2.length == 0) {
            return 1.0; // Can't compare, assume similar
        }

        // Compare mean intensities across channels
        double sumDiff = 0.0;
        double sumMean = 0.0;
        int count = 0;

        for (int c = 0; c < Math.min(m1.length, m2.length); c++) {
            if (m1[c].length > 1 && m2[c].length > 1) {
                double mean1 = m1[c][1]; // Mean intensity
                double mean2 = m2[c][1];
                double avgMean = (mean1 + mean2) / 2.0;

                if (avgMean > 0) {
                    sumDiff += Math.abs(mean1 - mean2);
                    sumMean += avgMean;
                    count++;
                }
            }
        }

        if (count == 0 || sumMean == 0) {
            return 1.0;
        }

        // Normalized difference (0 = identical, 1 = completely different)
        double normalizedDiff = sumDiff / sumMean;

        // Convert to similarity (1 = identical, 0 = completely different)
        return Math.max(0.0, 1.0 - normalizedDiff);
    }

    /**
     * Merge a group of objects into a single object
     * @param objects list of objects to merge
     * @return merged object
     */
    private MicroObject mergeObjectGroup(List<MicroObject> objects) {
        if (objects.isEmpty()) {
            return null;
        }

        if (objects.size() == 1) {
            return objects.get(0);
        }

        // Start with the first object as base
        MicroObject merged = objects.get(0);

        // Accumulate voxel counts
        int totalVoxels = 0;
        double[] weightedCentroid = new double[3];
        double[][] sumMeasurements = null;

        for (MicroObject obj : objects) {
            int voxels = obj.getVoxelCount();
            totalVoxels += voxels;

            // Weight centroid by voxel count
            double[] centroid = obj.getCentroidXYZ_AsDbl();
            if (centroid != null && centroid.length >= 3) {
                weightedCentroid[0] += centroid[0] * voxels;
                weightedCentroid[1] += centroid[1] * voxels;
                weightedCentroid[2] += centroid[2] * voxels;
            }

            // Accumulate measurements
            double[][] measurements = obj.getMeasurements();
            if (measurements != null) {
                if (sumMeasurements == null) {
                    sumMeasurements = new double[measurements.length][];
                    for (int i = 0; i < measurements.length; i++) {
                        sumMeasurements[i] = new double[measurements[i].length];
                    }
                }

                for (int c = 0; c < measurements.length; c++) {
                    for (int m = 0; m < measurements[c].length; m++) {
                        sumMeasurements[c][m] += measurements[c][m] * voxels;
                    }
                }
            }
        }

        // Average centroid
        if (totalVoxels > 0) {
            weightedCentroid[0] /= totalVoxels;
            weightedCentroid[1] /= totalVoxels;
            weightedCentroid[2] /= totalVoxels;
            merged.setCentroidXYZ(weightedCentroid);
        }

        // Average measurements
        if (sumMeasurements != null && totalVoxels > 0) {
            for (int c = 0; c < sumMeasurements.length; c++) {
                for (int m = 0; m < sumMeasurements[c].length; m++) {
                    sumMeasurements[c][m] /= totalVoxels;
                }
            }
            merged.setMeasurements(sumMeasurements);
        }

        // Store merge info
        merged.setProperty("merged", true);
        merged.setProperty("mergeCount", objects.size());

        return merged;
    }

    /**
     * Get statistics about stitching operation
     * @param originalCount original object count
     * @param stitchedCount stitched object count
     * @return statistics string
     */
    public String getStitchingStats(int originalCount, int stitchedCount) {
        int merged = originalCount - stitchedCount;
        double mergeRate = originalCount > 0 ? (double) merged / originalCount * 100 : 0.0;

        return String.format("Stitching Stats:\n" +
                           "  Original objects: %d\n" +
                           "  Stitched objects: %d\n" +
                           "  Merged objects: %d (%.1f%%)\n" +
                           "  Overlap threshold: %.1f%%\n" +
                           "  Distance threshold: %.1f px",
                           originalCount, stitchedCount, merged, mergeRate,
                           overlapThreshold * 100, distanceThreshold);
    }
}
