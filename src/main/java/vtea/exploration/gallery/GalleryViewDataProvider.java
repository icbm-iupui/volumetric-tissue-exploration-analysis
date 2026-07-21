package vtea.exploration.gallery;

import vtea.exploration.plotgatetools.gates.PolygonGate;

/**
 * Interface for components that can provide data for gallery view.
 * Implemented by panels that manage gates and objects.
 */
public interface GalleryViewDataProvider {
    /**
     * Open gallery view for the specified gate.
     * @param gate The gate to display in gallery view
     */
    void openGalleryView(PolygonGate gate);
}
