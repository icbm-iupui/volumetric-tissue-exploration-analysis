package vtea.exploration.gallery;

import vteaobjects.MicroObject;

/**
 * Listener interface for gallery cell selection events.
 * Notifies when a cell is selected or deselected in the gallery view.
 */
public interface GallerySelectionListener {
    /**
     * Called when a cell is selected in the gallery view.
     * @param selectedCell The selected MicroObject, or null if deselected
     */
    void gallerySelectionChanged(MicroObject selectedCell);
}
