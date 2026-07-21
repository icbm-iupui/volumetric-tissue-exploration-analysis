package vtea.exploration.gallery;

import ij.ImageStack;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import vteaobjects.MicroObject;

/**
 * Main gallery display window showing grid of cell thumbnails.
 * Allows user to view and select individual cells from a gate.
 */
public class GalleryViewWindow extends JFrame implements GallerySelectionListener {

    private ArrayList<MicroObject> cells;
    private Map<MicroObject, GalleryThumbnailPanel> thumbnailPanels;
    private MicroObject currentlySelected;
    private JPanel galleryPanel;
    private JScrollPane scrollPane;
    private ImageStack[] imageStacks;

    // Configuration
    private int thumbnailSize = 128;
    private int[] regionSize = {64, 64, -1}; // -1 = full Z
    private int[] channels;
    private int columnsPerRow = 5;

    // UI Components
    private JLabel cellCountLabel;

    // Listeners for external notifications
    private ArrayList<GallerySelectionListener> externalListeners = new ArrayList<>();

    /**
     * Constructor.
     * @param gateName Name of gate (for window title)
     * @param cells List of gated MicroObjects
     * @param imageStacks Multi-channel image data
     * @param channels Channels to display (null for default)
     */
    public GalleryViewWindow(String gateName,
                              ArrayList<MicroObject> cells,
                              ImageStack[] imageStacks,
                              int[] channels) {
        super("Gallery View - " + gateName);

        this.cells = cells;
        this.imageStacks = imageStacks;
        this.channels = channels;
        this.thumbnailPanels = new HashMap<>();

        // Determine Z depth from image stacks
        if (imageStacks != null && imageStacks.length > 0) {
            regionSize[2] = imageStacks[0].getSize(); // Full Z depth
        }

        initializeUI();
        loadThumbnails();
    }

    /**
     * Initialize the user interface.
     */
    private void initializeUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBackground(vtea._vtea.BACKGROUND);

        // Create main panel with grid layout
        galleryPanel = new JPanel();
        galleryPanel.setLayout(new GridLayout(0, columnsPerRow, 10, 10));
        galleryPanel.setBackground(Color.WHITE);
        galleryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Wrap in scroll pane
        scrollPane = new JScrollPane(galleryPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Add toolbar
        JPanel toolbarPanel = createToolbar();

        // Layout
        setLayout(new BorderLayout());
        add(toolbarPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Size window
        int width = Math.min(columnsPerRow * (thumbnailSize + 30), 900);
        int rows = (int) Math.ceil((double) cells.size() / columnsPerRow);
        int height = Math.min(rows * (thumbnailSize + 50) + 100, 700);
        setSize(width, height);

        setLocationRelativeTo(null);
    }

    /**
     * Create the toolbar with controls.
     * @return JPanel containing toolbar
     */
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(vtea._vtea.BACKGROUND);

        // Cell count label
        cellCountLabel = new JLabel("Cells: " + cells.size());
        toolbar.add(cellCountLabel);

        toolbar.add(new JSeparator(SwingConstants.VERTICAL));

        // Grid size selector
        JLabel gridLabel = new JLabel("Columns:");
        toolbar.add(gridLabel);

        Integer[] columnOptions = {3, 4, 5, 6, 8, 10};
        JComboBox<Integer> columnSelector = new JComboBox<>(columnOptions);
        columnSelector.setSelectedItem(columnsPerRow);
        columnSelector.addActionListener(e -> {
            columnsPerRow = (Integer) columnSelector.getSelectedItem();
            updateGridLayout();
        });
        toolbar.add(columnSelector);

        toolbar.add(new JSeparator(SwingConstants.VERTICAL));

        // Thumbnail size selector
        JLabel sizeLabel = new JLabel("Size:");
        toolbar.add(sizeLabel);

        Integer[] sizeOptions = {64, 96, 128, 160, 192, 256};
        JComboBox<Integer> sizeSelector = new JComboBox<>(sizeOptions);
        sizeSelector.setSelectedItem(thumbnailSize);
        sizeSelector.addActionListener(e -> {
            int newSize = (Integer) sizeSelector.getSelectedItem();
            if (newSize != thumbnailSize) {
                thumbnailSize = newSize;
                reloadThumbnails();
            }
        });
        toolbar.add(sizeSelector);

        toolbar.add(new JSeparator(SwingConstants.VERTICAL));

        // Clear selection button
        JButton clearButton = new JButton("Clear Selection");
        clearButton.addActionListener(e -> clearSelection());
        toolbar.add(clearButton);

        return toolbar;
    }

    /**
     * Load thumbnails for all cells (with progress indication).
     */
    private void loadThumbnails() {
        // Show progress dialog for large cell counts
        boolean showProgress = cells.size() > 20;
        JDialog progressDialog = null;
        JProgressBar progressBar = null;

        if (showProgress) {
            progressDialog = createProgressDialog();
            progressBar = (JProgressBar) ((JPanel) progressDialog.getContentPane()
                    .getComponent(0)).getComponent(1);
            progressDialog.setVisible(true);
        }

        // Create thumbnails in background thread
        final JDialog finalProgressDialog = progressDialog;
        final JProgressBar finalProgressBar = progressBar;

        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                Map<MicroObject, BufferedImage> thumbnails =
                        GalleryImageProcessor.createThumbnailBatch(
                                cells, imageStacks, regionSize, channels, thumbnailSize);

                int count = 0;
                for (MicroObject cell : cells) {
                    BufferedImage thumbnail = thumbnails.get(cell);

                    GalleryThumbnailPanel panel = new GalleryThumbnailPanel(cell, thumbnail);
                    panel.addGallerySelectionListener(GalleryViewWindow.this);

                    thumbnailPanels.put(cell, panel);

                    // Update on EDT
                    final GalleryThumbnailPanel finalPanel = panel;
                    SwingUtilities.invokeLater(() -> galleryPanel.add(finalPanel));

                    count++;
                    if (showProgress) {
                        publish(count);
                    }
                }

                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                if (finalProgressBar != null && !chunks.isEmpty()) {
                    finalProgressBar.setValue(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                if (finalProgressDialog != null) {
                    finalProgressDialog.dispose();
                }
                galleryPanel.revalidate();
                galleryPanel.repaint();
            }
        };

        worker.execute();
    }

    /**
     * Create progress dialog for thumbnail loading.
     * @return JDialog with progress bar
     */
    private JDialog createProgressDialog() {
        JDialog dialog = new JDialog(this, "Loading Gallery...", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Processing " + cells.size() + " cells...");
        panel.add(label, BorderLayout.NORTH);

        JProgressBar progressBar = new JProgressBar(0, cells.size());
        progressBar.setStringPainted(true);
        panel.add(progressBar, BorderLayout.CENTER);

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        return dialog;
    }

    /**
     * Update the grid layout with new column count.
     */
    private void updateGridLayout() {
        galleryPanel.setLayout(new GridLayout(0, columnsPerRow, 10, 10));
        galleryPanel.revalidate();
        galleryPanel.repaint();

        // Resize window to fit new layout
        int width = Math.min(columnsPerRow * (thumbnailSize + 30), 900);
        setSize(width, getHeight());
    }

    /**
     * Reload all thumbnails with new size.
     */
    private void reloadThumbnails() {
        galleryPanel.removeAll();
        thumbnailPanels.clear();
        currentlySelected = null;
        loadThumbnails();
    }

    /**
     * Clear the current selection.
     */
    private void clearSelection() {
        if (currentlySelected != null) {
            GalleryThumbnailPanel panel = thumbnailPanels.get(currentlySelected);
            if (panel != null) {
                panel.setSelected(false);
            }
            currentlySelected = null;
            notifyExternalListeners(null);
        }
    }

    @Override
    public void gallerySelectionChanged(MicroObject selectedCell) {
        // Deselect previously selected
        if (currentlySelected != null && currentlySelected != selectedCell) {
            GalleryThumbnailPanel prevPanel = thumbnailPanels.get(currentlySelected);
            if (prevPanel != null) {
                prevPanel.setSelected(false);
            }
        }

        // Select new cell (or toggle off if clicking same cell)
        if (selectedCell != null) {
            if (selectedCell.equals(currentlySelected)) {
                // Clicking same cell - deselect
                GalleryThumbnailPanel panel = thumbnailPanels.get(selectedCell);
                if (panel != null) {
                    panel.setSelected(false);
                }
                currentlySelected = null;
                notifyExternalListeners(null);
            } else {
                // Select new cell
                GalleryThumbnailPanel newPanel = thumbnailPanels.get(selectedCell);
                if (newPanel != null) {
                    newPanel.setSelected(true);
                }
                currentlySelected = selectedCell;
                notifyExternalListeners(selectedCell);
            }
        } else {
            currentlySelected = null;
            notifyExternalListeners(null);
        }
    }

    /**
     * Add a listener for gallery selection events.
     * @param listener The listener to add
     */
    public void addGallerySelectionListener(GallerySelectionListener listener) {
        if (listener != null && !externalListeners.contains(listener)) {
            externalListeners.add(listener);
        }
    }

    /**
     * Remove a gallery selection listener.
     * @param listener The listener to remove
     */
    public void removeGallerySelectionListener(GallerySelectionListener listener) {
        externalListeners.remove(listener);
    }

    /**
     * Notify external listeners of selection change.
     * @param cell The selected cell (or null if deselected)
     */
    private void notifyExternalListeners(MicroObject cell) {
        for (GallerySelectionListener listener : externalListeners) {
            listener.gallerySelectionChanged(cell);
        }
    }

    /**
     * Set the region size for extraction.
     * @param regionSize Array of [width, height, depth]
     */
    public void setRegionSize(int[] regionSize) {
        this.regionSize = regionSize;
    }

    /**
     * Set the channels to display.
     * @param channels Array of channel indices
     */
    public void setChannels(int[] channels) {
        this.channels = channels;
    }

    /**
     * Get the currently selected cell.
     * @return Currently selected MicroObject, or null
     */
    public MicroObject getCurrentlySelected() {
        return currentlySelected;
    }
}
