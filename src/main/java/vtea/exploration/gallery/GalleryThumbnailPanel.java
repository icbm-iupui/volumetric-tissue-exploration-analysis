package vtea.exploration.gallery;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import vteaobjects.MicroObject;

/**
 * Panel displaying an individual cell thumbnail with selection state.
 * Notifies listeners when clicked.
 */
public class GalleryThumbnailPanel extends JPanel {

    private MicroObject cell;
    private BufferedImage thumbnail;
    private boolean selected;

    private static final int THUMBNAIL_SIZE = 128;
    private static final Color SELECTED_BORDER = Color.RED;
    private static final int BORDER_WIDTH = 3;
    private static final Color HOVER_BACKGROUND = new Color(240, 240, 240);

    private boolean hovered = false;

    // Listeners
    private ArrayList<GallerySelectionListener> listeners = new ArrayList<>();

    /**
     * Constructor.
     * @param cell The MicroObject this panel represents
     * @param thumbnail The thumbnail image to display
     */
    public GalleryThumbnailPanel(MicroObject cell, BufferedImage thumbnail) {
        this.cell = cell;
        this.thumbnail = thumbnail;
        this.selected = false;

        setupUI();
        setupListeners();
    }

    /**
     * Initialize UI components and styling.
     */
    private void setupUI() {
        setPreferredSize(new Dimension(THUMBNAIL_SIZE + 10, THUMBNAIL_SIZE + 30));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setBackground(Color.WHITE);
        setOpaque(true);
    }

    /**
     * Setup mouse listeners for interaction.
     */
    private void setupListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                notifySelectionListeners(cell);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                setCursor(Cursor.getDefaultCursor());
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw hover background
        if (hovered && !selected) {
            g2.setColor(HOVER_BACKGROUND);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        // Draw thumbnail image
        if (thumbnail != null) {
            int x = (getWidth() - THUMBNAIL_SIZE) / 2;
            int y = 5;
            g2.drawImage(thumbnail, x, y, THUMBNAIL_SIZE, THUMBNAIL_SIZE, null);

            // Draw selection border if selected
            if (selected) {
                g2.setColor(SELECTED_BORDER);
                g2.setStroke(new BasicStroke(
                        BORDER_WIDTH,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND,
                        0,
                        new float[]{5, 5},  // Dashed line pattern
                        0
                ));
                g2.drawRect(
                        x - BORDER_WIDTH / 2,
                        y - BORDER_WIDTH / 2,
                        THUMBNAIL_SIZE + BORDER_WIDTH,
                        THUMBNAIL_SIZE + BORDER_WIDTH
                );
            }

            // Draw cell ID label
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            String label = "Cell " + cell.getSerialID();
            FontMetrics fm = g2.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            int labelX = (getWidth() - labelWidth) / 2;
            int labelY = getHeight() - 5;
            g2.drawString(label, labelX, labelY);
        } else {
            // Draw placeholder if no thumbnail
            g2.setColor(Color.LIGHT_GRAY);
            int x = (getWidth() - THUMBNAIL_SIZE) / 2;
            int y = 5;
            g2.fillRect(x, y, THUMBNAIL_SIZE, THUMBNAIL_SIZE);

            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            String msg = "No Image";
            FontMetrics fm = g2.getFontMetrics();
            int msgWidth = fm.stringWidth(msg);
            int msgX = x + (THUMBNAIL_SIZE - msgWidth) / 2;
            int msgY = y + THUMBNAIL_SIZE / 2;
            g2.drawString(msg, msgX, msgY);
        }
    }

    /**
     * Set the selection state of this panel.
     * @param selected True if selected
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    /**
     * Get the selection state.
     * @return True if selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Get the cell associated with this panel.
     * @return The MicroObject
     */
    public MicroObject getCell() {
        return cell;
    }

    /**
     * Set the thumbnail image.
     * @param thumbnail New thumbnail image
     */
    public void setThumbnail(BufferedImage thumbnail) {
        this.thumbnail = thumbnail;
        repaint();
    }

    /**
     * Add a listener for selection events.
     * @param listener The listener to add
     */
    public void addGallerySelectionListener(GallerySelectionListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a selection listener.
     * @param listener The listener to remove
     */
    public void removeGallerySelectionListener(GallerySelectionListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners of selection change.
     * @param cell The selected cell
     */
    private void notifySelectionListeners(MicroObject cell) {
        for (GallerySelectionListener listener : listeners) {
            listener.gallerySelectionChanged(cell);
        }
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (cell != null) {
            return String.format("Cell %d - Click to highlight", cell.getSerialID());
        }
        return null;
    }

    {
        // Enable tooltips
        setToolTipText("");
    }
}
