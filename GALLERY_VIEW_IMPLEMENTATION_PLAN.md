# Gallery View Implementation Plan

## Feature Overview

Add a gallery view feature that allows users to:
1. Right-click on a selected gate in MicroExplorer
2. Select "Gallery View..." from context menu
3. Open a new window displaying a gallery of gated cells
4. Each gallery cell shows a maximum projection of a subregion around the cell's centroid
5. Clicking a gallery image highlights the cell in:
   - The XY mapping (via XYChartPanel with imageGateOutline)
   - The MicroExplorer overlay (via GateLayer)

---

## Architecture Components

### New Components to Create

1. **GalleryViewWindow** - Main gallery display window
2. **GalleryThumbnailPanel** - Individual cell thumbnail display
3. **GallerySelectionListener** - Interface for gallery selection events
4. **GalleryImageProcessor** - Processes cell regions into thumbnail images

### Existing Components to Modify

1. **GateLayer** - Add "Gallery View..." to right-click context menu
2. **XYExplorationPanel** - Handle gallery selection events for highlighting
3. **MicroExplorer** - Coordinate between gallery and main views

---

## Detailed Implementation Plan

### Phase 1: Core Gallery Components

#### 1.1 Create GalleryImageProcessor

**File:** `src/main/java/vtea/exploration/gallery/GalleryImageProcessor.java`

**Purpose:** Process MicroObject 3D regions into 2D maximum projection thumbnails

**Key Methods:**
```java
public class GalleryImageProcessor {

    /**
     * Extract and process a single cell region
     * @param cell The MicroObject to extract
     * @param imageStacks Multi-channel image data
     * @param regionSize Size of region to extract (e.g., [64, 64, 20])
     * @param channels Channels to include in composite
     * @return BufferedImage of maximum projection
     */
    public static BufferedImage createThumbnail(
        MicroObject cell,
        ImageStack[] imageStacks,
        int[] regionSize,
        int[] channels,
        int thumbnailSize
    )

    /**
     * Create maximum projection from 3D stack
     * @param stack 3D ImageStack
     * @return 2D maximum projection as ImagePlus
     */
    private static ImagePlus makeMaxProjection(ImageStack stack)

    /**
     * Convert ImagePlus to BufferedImage for Swing display
     * @param imp ImagePlus to convert
     * @param targetSize Target thumbnail dimension (square)
     * @return BufferedImage scaled to targetSize x targetSize
     */
    private static BufferedImage toBufferedImage(ImagePlus imp, int targetSize)

    /**
     * Batch process multiple cells
     * @param cells List of cells to process
     * @param imageStacks Image data
     * @param regionSize Region extraction size
     * @param channels Channels to composite
     * @param thumbnailSize Output thumbnail size
     * @return Map of cell -> thumbnail image
     */
    public static Map<MicroObject, BufferedImage> createThumbnailBatch(
        List<MicroObject> cells,
        ImageStack[] imageStacks,
        int[] regionSize,
        int[] channels,
        int thumbnailSize
    )
}
```

**Implementation Details:**
- Use `CellRegionExtractor.extractRegion()` to get 3D regions
- Use ImageJ's `ZProjector` for maximum projection
- Use `PaddingType.REPLICATE` for edge cells
- Default region size: [64, 64, full-Z-range]
- Default thumbnail size: 128x128 pixels
- Support multi-channel composite with configurable channel selection

**Dependencies:**
- `vtea.deeplearning.data.CellRegionExtractor`
- `ij.ImagePlus`, `ij.ImageStack`
- `ij.plugin.ZProjector`
- `java.awt.image.BufferedImage`

---

#### 1.2 Create GalleryThumbnailPanel

**File:** `src/main/java/vtea/exploration/gallery/GalleryThumbnailPanel.java`

**Purpose:** Display individual cell thumbnail with selection state

**Key Features:**
```java
public class GalleryThumbnailPanel extends JPanel {
    private MicroObject cell;
    private BufferedImage thumbnail;
    private boolean selected;
    private static final int THUMBNAIL_SIZE = 128;
    private static final Color SELECTED_BORDER = Color.RED;
    private static final int BORDER_WIDTH = 3;

    /**
     * Constructor
     */
    public GalleryThumbnailPanel(MicroObject cell, BufferedImage thumbnail) {
        this.cell = cell;
        this.thumbnail = thumbnail;
        this.selected = false;

        setupUI();
        setupListeners();
    }

    private void setupUI() {
        setPreferredSize(new Dimension(THUMBNAIL_SIZE + 10, THUMBNAIL_SIZE + 30));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setBackground(Color.WHITE);
    }

    private void setupListeners() {
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                notifySelectionListeners(cell);
            }

            public void mouseEntered(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw thumbnail image
        if (thumbnail != null) {
            int x = (getWidth() - THUMBNAIL_SIZE) / 2;
            int y = 5;
            g2.drawImage(thumbnail, x, y, THUMBNAIL_SIZE, THUMBNAIL_SIZE, null);

            // Draw selection border if selected
            if (selected) {
                g2.setColor(SELECTED_BORDER);
                g2.setStroke(new BasicStroke(BORDER_WIDTH,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    0, new float[]{5, 5}, 0)); // Dashed line
                g2.drawRect(x - BORDER_WIDTH/2, y - BORDER_WIDTH/2,
                           THUMBNAIL_SIZE + BORDER_WIDTH,
                           THUMBNAIL_SIZE + BORDER_WIDTH);
            }

            // Draw cell ID label
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            String label = "Cell " + cell.getSerialID();
            FontMetrics fm = g2.getFontMetrics();
            int labelX = (getWidth() - fm.stringWidth(label)) / 2;
            g2.drawString(label, labelX, getHeight() - 5);
        }
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    public boolean isSelected() {
        return selected;
    }

    public MicroObject getCell() {
        return cell;
    }

    // Listener management
    private ArrayList<GallerySelectionListener> listeners = new ArrayList<>();

    public void addGallerySelectionListener(GallerySelectionListener listener) {
        listeners.add(listener);
    }

    private void notifySelectionListeners(MicroObject cell) {
        for (GallerySelectionListener listener : listeners) {
            listener.gallerySelectionChanged(cell);
        }
    }
}
```

---

#### 1.3 Create GallerySelectionListener Interface

**File:** `src/main/java/vtea/exploration/gallery/GallerySelectionListener.java`

**Purpose:** Event notification for gallery selection changes

```java
package vtea.exploration.gallery;

import vteaobjects.MicroObject;

/**
 * Listener interface for gallery cell selection events
 */
public interface GallerySelectionListener {
    /**
     * Called when a cell is selected in the gallery view
     * @param selectedCell The selected MicroObject, or null if deselected
     */
    void gallerySelectionChanged(MicroObject selectedCell);
}
```

---

#### 1.4 Create GalleryViewWindow

**File:** `src/main/java/vtea/exploration/gallery/GalleryViewWindow.java`

**Purpose:** Main gallery display window with grid layout of thumbnails

**Key Features:**
```java
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

    // Listeners for external notifications
    private ArrayList<GallerySelectionListener> externalListeners = new ArrayList<>();

    /**
     * Constructor
     * @param gateName Name of gate (for window title)
     * @param cells List of gated MicroObjects
     * @param imageStacks Multi-channel image data
     * @param channels Channels to display
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

        initializeUI();
        loadThumbnails();
    }

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

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(vtea._vtea.BACKGROUND);

        // Cell count label
        JLabel countLabel = new JLabel("Cells: " + cells.size());
        toolbar.add(countLabel);

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
            thumbnailSize = (Integer) sizeSelector.getSelectedItem();
            reloadThumbnails();
        });
        toolbar.add(sizeSelector);

        toolbar.add(new JSeparator(SwingConstants.VERTICAL));

        // Clear selection button
        JButton clearButton = new JButton("Clear Selection");
        clearButton.addActionListener(e -> clearSelection());
        toolbar.add(clearButton);

        return toolbar;
    }

    private void loadThumbnails() {
        // Show progress dialog for large cell counts
        boolean showProgress = cells.size() > 20;
        JProgressBar progressBar = null;
        JDialog progressDialog = null;

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
            protected void process(java.util.List<Integer> chunks) {
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

    private JDialog createProgressDialog() {
        JDialog dialog = new JDialog(this, "Loading Gallery...", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Processing cells...");
        panel.add(label, BorderLayout.NORTH);

        JProgressBar progressBar = new JProgressBar(0, cells.size());
        progressBar.setStringPainted(true);
        panel.add(progressBar, BorderLayout.CENTER);

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        return dialog;
    }

    private void updateGridLayout() {
        galleryPanel.setLayout(new GridLayout(0, columnsPerRow, 10, 10));
        galleryPanel.revalidate();
        galleryPanel.repaint();
    }

    private void reloadThumbnails() {
        galleryPanel.removeAll();
        thumbnailPanels.clear();
        currentlySelected = null;
        loadThumbnails();
    }

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

        // Select new cell
        if (selectedCell != null) {
            GalleryThumbnailPanel newPanel = thumbnailPanels.get(selectedCell);
            if (newPanel != null) {
                newPanel.setSelected(true);
            }
            currentlySelected = selectedCell;
        } else {
            currentlySelected = null;
        }

        // Notify external listeners (MicroExplorer, etc.)
        notifyExternalListeners(selectedCell);
    }

    public void addGallerySelectionListener(GallerySelectionListener listener) {
        externalListeners.add(listener);
    }

    private void notifyExternalListeners(MicroObject cell) {
        for (GallerySelectionListener listener : externalListeners) {
            listener.gallerySelectionChanged(cell);
        }
    }

    public void setRegionSize(int[] regionSize) {
        this.regionSize = regionSize;
    }

    public void setChannels(int[] channels) {
        this.channels = channels;
    }
}
```

---

### Phase 2: Integration with Existing Components

#### 2.1 Modify GateLayer - Add Context Menu Item

**File:** `src/main/java/vtea/exploration/plotgatetools/gates/GateLayer.java`

**Changes:**

1. **Add new menu item in `createPopUpMenu()` method** (after line ~702):

```java
private void createPopUpMenu(JXLayer layer) {
    this.menu = new JPopupMenu();

    // ... existing menu items ...

    // ADD NEW: Gallery View menu item
    menu.add(new JSeparator());
    JMenuItem galleryMenuItem = new JMenuItem("Gallery View...");
    galleryMenuItem.addActionListener(this);
    menu.add(galleryMenuItem);

    // ... rest of existing items ...
}
```

2. **Update `actionPerformed()` to handle gallery action** (around line ~830):

```java
@Override
public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();

    // ... existing action handlers ...

    // ADD NEW: Handle Gallery View action
    if ("Gallery View...".equals(command)) {
        openGalleryView();
        return;
    }

    // ... rest of existing handlers ...
}
```

3. **Add new fields and methods**:

```java
// Add field to hold reference to data source
private GalleryViewDataProvider dataProvider;

/**
 * Set data provider for gallery view
 */
public void setGalleryViewDataProvider(GalleryViewDataProvider provider) {
    this.dataProvider = provider;
}

/**
 * Open gallery view for selected gate
 */
private void openGalleryView() {
    if (selectedGate == null) {
        JOptionPane.showMessageDialog(
            chart,
            "Please select a gate first.",
            "No Gate Selected",
            JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    if (dataProvider == null) {
        System.err.println("GalleryViewDataProvider not set!");
        return;
    }

    // Request gallery view from data provider
    dataProvider.openGalleryView(selectedGate);
}
```

4. **Create GalleryViewDataProvider interface**:

**File:** `src/main/java/vtea/exploration/gallery/GalleryViewDataProvider.java`

```java
package vtea.exploration.gallery;

import vtea.exploration.plotgatetools.gates.PolygonGate;

/**
 * Interface for components that can provide data for gallery view
 */
public interface GalleryViewDataProvider {
    /**
     * Open gallery view for the specified gate
     * @param gate The gate to display
     */
    void openGalleryView(PolygonGate gate);
}
```

---

#### 2.2 Modify XYExplorationPanel - Implement Data Provider

**File:** `src/main/java/vtea/exploration/plottools/panels/XYExplorationPanel.java`

**Changes:**

1. **Implement GalleryViewDataProvider interface**:

```java
public class XYExplorationPanel extends AbstractExplorationPanel
    implements ExplorationCenter, GalleryViewDataProvider {

    // ... existing code ...
}
```

2. **Add field to track gallery windows**:

```java
// Track open gallery windows
private HashMap<String, GalleryViewWindow> galleryWindows = new HashMap<>();
```

3. **Implement openGalleryView() method**:

```java
@Override
public void openGalleryView(PolygonGate gate) {
    // Get cells in this gate
    ArrayList<MicroObject> gatedCells = getObjectsInGate(gate);

    if (gatedCells.isEmpty()) {
        JOptionPane.showMessageDialog(
            this,
            "No cells found in gate \"" + gate.getName() + "\"",
            "Empty Gate",
            JOptionPane.INFORMATION_MESSAGE
        );
        return;
    }

    // Get image stacks (from parent MicroExplorer or stored reference)
    ImageStack[] imageStacks = getImageStacks();
    if (imageStacks == null) {
        JOptionPane.showMessageDialog(
            this,
            "Image data not available for gallery view.",
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    // Determine which channels to display
    int[] channels = getDefaultChannels();

    // Create or reuse gallery window
    String gateKey = gate.getName();
    GalleryViewWindow galleryWindow = galleryWindows.get(gateKey);

    if (galleryWindow == null || !galleryWindow.isVisible()) {
        galleryWindow = new GalleryViewWindow(
            gate.getName(),
            gatedCells,
            imageStacks,
            channels
        );

        // Listen for gallery selections
        galleryWindow.addGallerySelectionListener(new GallerySelectionListener() {
            @Override
            public void gallerySelectionChanged(MicroObject selectedCell) {
                handleGallerySelection(selectedCell);
            }
        });

        galleryWindows.put(gateKey, galleryWindow);
        galleryWindow.setVisible(true);
    } else {
        // Bring existing window to front
        galleryWindow.toFront();
        galleryWindow.requestFocus();
    }
}

/**
 * Get all objects within a specific gate
 */
private ArrayList<MicroObject> getObjectsInGate(PolygonGate gate) {
    ArrayList<MicroObject> result = new ArrayList<>();

    // Get gate bounds in data coordinates
    Path2D.Float path = gate.makePathFloat(
        getChart(),
        getChart().getXYPlot().getRangeAxis(),
        getChart().getXYPlot().getDomainAxis()
    );

    // Test each object
    for (MicroObject obj : objects) {
        double xValue = obj.getChannelTagFloat(xAxis);
        double yValue = obj.getChannelTagFloat(yAxis);

        if (path.contains(xValue, yValue)) {
            result.add(obj);
        }
    }

    return result;
}

/**
 * Get image stacks - implementation depends on data architecture
 */
private ImageStack[] getImageStacks() {
    // Option 1: Get from parent MicroExplorer
    // Option 2: Store reference when panel is created
    // Option 3: Access via static/singleton image manager

    // For now, return stored reference (add field and setter)
    return this.imageStacks;
}

/**
 * Get default channels for display
 */
private int[] getDefaultChannels() {
    // Return all available channels or user-selected channels
    // For simplicity, return first 3 channels if available
    if (imageStacks != null) {
        int numChannels = Math.min(3, imageStacks.length);
        int[] channels = new int[numChannels];
        for (int i = 0; i < numChannels; i++) {
            channels[i] = i;
        }
        return channels;
    }
    return new int[]{0};
}

/**
 * Handle selection from gallery view
 */
private void handleGallerySelection(MicroObject selectedCell) {
    if (selectedCell == null) {
        // Clear highlighting
        clearGalleryHighlight();
        return;
    }

    // Highlight cell on XY chart
    highlightCellOnChart(selectedCell);

    // Highlight cell on image overlay via GateLayer
    highlightCellOnImage(selectedCell);
}
```

4. **Add methods for highlighting**:

```java
private MicroObject currentGallerySelection;

/**
 * Highlight a specific cell on the XY chart
 */
private void highlightCellOnChart(MicroObject cell) {
    currentGallerySelection = cell;

    // Create temporary image gate overlay for this single cell
    ArrayList<Number> imagegateOverlay = new ArrayList<>();
    imagegateOverlay.add(cell.getSerialID());

    // Update chart with highlighted cell
    // This requires modifying XYChartPanel to support single-cell highlighting
    // See section 2.3 below

    if (xyChartPanel != null) {
        xyChartPanel.highlightCell(cell);
    }
}

/**
 * Highlight cell on image overlay
 */
private void highlightCellOnImage(MicroObject cell) {
    // Notify GateLayer to highlight this cell
    // This requires adding highlight method to GateLayer
    // See section 2.4 below

    if (gateLayer != null) {
        gateLayer.highlightCell(cell);
    }
}

/**
 * Clear gallery highlight
 */
private void clearGalleryHighlight() {
    currentGallerySelection = null;

    if (xyChartPanel != null) {
        xyChartPanel.clearCellHighlight();
    }

    if (gateLayer != null) {
        gateLayer.clearCellHighlight();
    }
}
```

5. **Add fields to store references**:

```java
private ImageStack[] imageStacks;
private XYChartPanel xyChartPanel;
private GateLayer gateLayer;

// Add setters
public void setImageStacks(ImageStack[] imageStacks) {
    this.imageStacks = imageStacks;
}

public void setXYChartPanel(XYChartPanel panel) {
    this.xyChartPanel = panel;
}

public void setGateLayer(GateLayer layer) {
    this.gateLayer = layer;
    // Register as data provider
    layer.setGalleryViewDataProvider(this);
}
```

---

#### 2.3 Modify XYChartPanel - Add Cell Highlighting

**File:** `src/main/java/vtea/exploration/plottools/panels/XYChartPanel.java`

**Changes:**

1. **Add fields for gallery highlighting**:

```java
private MicroObject galleryHighlightedCell;
private Color galleryHighlightColor = Color.RED;
private Stroke galleryHighlightStroke = new BasicStroke(
    2.0f,
    BasicStroke.CAP_ROUND,
    BasicStroke.JOIN_ROUND,
    10.0f,
    new float[]{5.0f, 5.0f},  // Dashed pattern
    0.0f
);
```

2. **Add highlighting methods**:

```java
/**
 * Highlight a specific cell with red dashed outline
 */
public void highlightCell(MicroObject cell) {
    this.galleryHighlightedCell = cell;
    repaint();
}

/**
 * Clear cell highlight
 */
public void clearCellHighlight() {
    this.galleryHighlightedCell = null;
    repaint();
}

/**
 * Get currently highlighted cell
 */
public MicroObject getHighlightedCell() {
    return galleryHighlightedCell;
}
```

3. **Modify rendering to draw highlight** (in dataset creation or custom renderer):

This requires either:
- **Option A**: Modify the `createDataset()` method to mark highlighted cells
- **Option B**: Add custom renderer overlay

**Option B (Recommended)**: Add annotation to chart for highlighted cell:

```java
private void updateHighlightAnnotation() {
    // Remove existing highlight annotation
    XYPlot plot = chart.getXYPlot();
    plot.clearAnnotations();

    if (galleryHighlightedCell == null) {
        return;
    }

    // Get cell coordinates
    double x = galleryHighlightedCell.getChannelTagFloat(xAxisKey);
    double y = galleryHighlightedCell.getChannelTagFloat(yAxisKey);

    // Create circle annotation for highlight
    XYShapeAnnotation highlight = new XYShapeAnnotation(
        new Ellipse2D.Double(x - 0.02, y - 0.02, 0.04, 0.04),  // Size in data units
        galleryHighlightStroke,
        galleryHighlightColor,
        null  // No fill, just outline
    );

    plot.addAnnotation(highlight);
}
```

Call `updateHighlightAnnotation()` in `highlightCell()` and `clearCellHighlight()`.

**Note**: Coordinate scaling may need adjustment based on data range.

---

#### 2.4 Modify GateLayer - Add Cell Highlighting on Image

**File:** `src/main/java/vtea/exploration/plotgatetools/gates/GateLayer.java`

**Changes:**

1. **Add fields**:

```java
private MicroObject highlightedCell;
private Color highlightColor = Color.RED;
private Stroke highlightStroke = new BasicStroke(
    2.0f,
    BasicStroke.CAP_ROUND,
    BasicStroke.JOIN_ROUND,
    10.0f,
    new float[]{5.0f, 5.0f},
    0.0f
);
```

2. **Add highlighting methods**:

```java
/**
 * Highlight a specific cell on the image overlay
 */
public void highlightCell(MicroObject cell) {
    this.highlightedCell = cell;
    // Trigger repaint
    layer.repaint();
}

/**
 * Clear highlighted cell
 */
public void clearCellHighlight() {
    this.highlightedCell = null;
    layer.repaint();
}
```

3. **Modify `paintLayer()` to draw highlight**:

In the custom `LayerUI.paintLayer()` method, add after gate rendering:

```java
@Override
public void paintLayer(Graphics2D g2, JXLayer l) {
    // ... existing gate rendering code ...

    // Draw highlighted cell from gallery
    if (highlightedCell != null) {
        drawHighlightedCell(g2, highlightedCell);
    }
}

/**
 * Draw red dashed outline around highlighted cell
 */
private void drawHighlightedCell(Graphics2D g2, MicroObject cell) {
    // Get cell coordinates in chart space
    double xValue = cell.getChannelTagFloat(xAxis);
    double yValue = cell.getChannelTagFloat(yAxis);

    // Convert to screen coordinates
    Rectangle2D dataArea = chart.getChartRenderingInfo()
        .getPlotInfo()
        .getDataArea();

    XYPlot plot = chart.getXYPlot();
    ValueAxis domainAxis = plot.getDomainAxis();
    ValueAxis rangeAxis = plot.getRangeAxis();

    double screenX = domainAxis.valueToJava2D(
        xValue, dataArea, plot.getDomainAxisEdge());
    double screenY = rangeAxis.valueToJava2D(
        yValue, dataArea, plot.getRangeAxisEdge());

    // Draw circle outline
    g2.setColor(highlightColor);
    g2.setStroke(highlightStroke);

    int radius = 8;  // Pixels
    g2.drawOval(
        (int)(screenX - radius),
        (int)(screenY - radius),
        radius * 2,
        radius * 2
    );
}
```

---

#### 2.5 Modify MicroExplorer - Coordinate Components

**File:** `src/main/java/vteaexploration/MicroExplorer.java`

**Changes:**

1. **Pass image stacks to XYExplorationPanel**:

In `process()` method, after creating exploration panel:

```java
// After line where panel is created
explorationPanel.setImageStacks(imageStacks);
explorationPanel.setGateLayer(gateLayer);  // If accessible
explorationPanel.setXYChartPanel(chartPanel);  // If accessible
```

2. **Store image stacks if not already stored**:

```java
private ImageStack[] imageStacks;

// In process() method, store image stacks
this.imageStacks = extractImageStacks(impoverlay);

/**
 * Extract ImageStack array from VTEAImagePlus
 */
private ImageStack[] extractImageStacks(VTEAImagePlus imp) {
    int nChannels = imp.getNChannels();
    ImageStack[] stacks = new ImageStack[nChannels];

    for (int c = 0; c < nChannels; c++) {
        ImageStack stack = new ImageStack(imp.getWidth(), imp.getHeight());
        for (int z = 1; z <= imp.getNSlices(); z++) {
            imp.setPosition(c + 1, z, 1);
            stack.addSlice(imp.getProcessor().duplicate());
        }
        stacks[c] = stack;
    }

    return stacks;
}
```

---

### Phase 3: Testing & Polish

#### 3.1 Unit Testing

Create test classes:

1. **GalleryImageProcessorTest**
   - Test thumbnail generation from MicroObjects
   - Test maximum projection
   - Test batch processing
   - Test edge cases (cells at image boundaries)

2. **GalleryViewWindowTest**
   - Test window creation
   - Test thumbnail grid layout
   - Test selection handling
   - Test listener notifications

3. **Integration test**
   - Full workflow: gate selection → gallery view → cell selection → highlighting

#### 3.2 Performance Optimization

1. **Lazy loading**: Don't generate all thumbnails upfront for large gates (>100 cells)
2. **Caching**: Cache extracted regions to avoid re-extraction
3. **Background processing**: Use SwingWorker for thumbnail generation
4. **Memory management**: Use WeakHashMap for thumbnail cache

#### 3.3 UI Polish

1. **Progress indication** for thumbnail generation
2. **Tooltip** showing cell ID and measurements on hover
3. **Double-click** to show full-size view
4. **Export** button to save gallery as image
5. **Filter/sort** controls (by size, intensity, etc.)
6. **Keyboard navigation** (arrow keys to change selection)

---

## Implementation Sequence

### Step 1: Core Gallery Components (1-2 days)
1. Create `GalleryImageProcessor` with basic thumbnail generation
2. Create `GalleryThumbnailPanel` for single cell display
3. Create `GallerySelectionListener` interface
4. Create basic `GalleryViewWindow` with grid layout

### Step 2: Integration Points (1 day)
1. Add "Gallery View..." to GateLayer context menu
2. Create `GalleryViewDataProvider` interface
3. Implement `openGalleryView()` in XYExplorationPanel
4. Connect GateLayer menu to panel method

### Step 3: Highlighting System (1 day)
1. Add `highlightCell()` to XYChartPanel
2. Add `highlightCell()` to GateLayer
3. Implement selection callback in XYExplorationPanel
4. Test end-to-end highlighting

### Step 4: Data Wiring (1 day)
1. Pass ImageStacks from MicroExplorer to XYExplorationPanel
2. Store GateLayer and XYChartPanel references
3. Test with real data

### Step 5: Testing & Polish (1-2 days)
1. Test with various gate sizes
2. Add progress indicators
3. Add error handling
4. Optimize performance
5. Add UI enhancements (tooltips, keyboard nav, etc.)

---

## Technical Considerations

### Memory Usage
- Each thumbnail ~50KB (128x128 RGB)
- 100 cells = ~5MB
- 1000 cells = ~50MB
- **Mitigation**: Implement lazy loading and image caching

### Performance
- CellRegionExtractor is fast (~10ms per cell)
- Maximum projection adds ~5ms per cell
- **Bottleneck**: Rendering large grids (>200 cells)
- **Mitigation**: Virtual scrolling for very large galleries

### Thread Safety
- Thumbnail generation in background thread (SwingWorker)
- UI updates on EDT only
- Synchronize access to shared state

### Error Handling
- Handle missing image data gracefully
- Handle cells at image boundaries (use padding)
- Handle empty gates
- Handle gallery window already open

### Future Enhancements
1. Multi-selection in gallery
2. Export selected cells
3. Re-gate from gallery selection
4. Show measurement overlays on thumbnails
5. 3D view option instead of max projection
6. Channel selection controls
7. Brightness/contrast adjustment
8. Cell sorting and filtering

---

## Files Summary

### New Files to Create
1. `src/main/java/vtea/exploration/gallery/GalleryImageProcessor.java`
2. `src/main/java/vtea/exploration/gallery/GalleryThumbnailPanel.java`
3. `src/main/java/vtea/exploration/gallery/GallerySelectionListener.java`
4. `src/main/java/vtea/exploration/gallery/GalleryViewDataProvider.java`
5. `src/main/java/vtea/exploration/gallery/GalleryViewWindow.java`

### Existing Files to Modify
1. `src/main/java/vtea/exploration/plotgatetools/gates/GateLayer.java`
2. `src/main/java/vtea/exploration/plottools/panels/XYExplorationPanel.java`
3. `src/main/java/vtea/exploration/plottools/panels/XYChartPanel.java`
4. `src/main/java/vteaexploration/MicroExplorer.java`

---

## Dependencies

All required dependencies are already present:
- ImageJ (ij.jar) - for ImageStack, ZProjector
- JFreeChart - for chart annotations
- Swing/AWT - for UI components
- Existing VTEA classes - for MicroObject, CellRegionExtractor

No new external dependencies needed.

---

## Risk Assessment

### Low Risk
- Core gallery components (isolated, new code)
- GalleryImageProcessor (uses stable CellRegionExtractor)
- UI layout and rendering

### Medium Risk
- Integration with GateLayer (complex event handling)
- ImageStack management (memory, threading)
- Highlighting coordination across components

### High Risk
- Performance with large cell counts (>500 cells)
- Memory usage with many galleries open
- Thread safety in concurrent thumbnail generation

### Mitigation Strategies
- Start with small test cases
- Implement progress indicators early
- Add memory monitoring and warnings
- Thorough testing of edge cases
- Code review before merging

---

## Success Criteria

1. ✓ Right-click on gate shows "Gallery View..." option
2. ✓ Gallery window opens with grid of cell thumbnails
3. ✓ Each thumbnail shows max projection of cell region
4. ✓ Clicking thumbnail highlights cell in chart (red dashed)
5. ✓ Clicking thumbnail highlights cell on image (red dashed)
6. ✓ Performance acceptable for 100+ cells
7. ✓ No crashes or memory leaks
8. ✓ UI is responsive during thumbnail generation

---

## Next Steps

1. **Review this plan** with team/stakeholders
2. **Create feature branch** for development
3. **Set up test data** (sample gates with 10, 50, 100, 500 cells)
4. **Begin implementation** following sequence above
5. **Iterative testing** after each phase
6. **Code review** before merging
7. **User testing** with real data
8. **Documentation** update

---

*End of Implementation Plan*
