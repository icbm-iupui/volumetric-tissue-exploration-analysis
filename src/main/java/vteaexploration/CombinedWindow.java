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
package vteaexploration;

import ij.IJ;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import vtea.exploration.listeners.NameUpdateListener;
import vtea.exploration.listeners.colorUpdateListener;
import vtea.exploration.listeners.remapOverlayListener;
import vtea.exploration.listeners.GateManagerActionListener;
import vtea.exploration.plotgatetools.gates.PolygonGate;
import vtea.exploration.listeners.GatePlotListener;
import vtea.gui.ColorRenderer;
import vtea.gui.ColorEditor;

/**
 *
 * @author vinfrais
 */
public class CombinedWindow extends javax.swing.JFrame implements TableModelListener {

    protected Object[][] DataTableArray = new Object[4][15];

    protected ArrayList<NameUpdateListener> nameUpdateListeners = new ArrayList<>();
    protected ArrayList<remapOverlayListener> remapOverlayListeners = new ArrayList<>();
    protected ArrayList<colorUpdateListener> UpdateColorListeners = new ArrayList<>();
    
    protected ArrayList<GatePlotListener> gatePlotListeners = new ArrayList<>();

    protected ArrayList<GateManagerActionListener> gateManagerListeners = new ArrayList<>();

    protected ArrayList<PolygonGate> gateList = new ArrayList();

    /**
     * Creates new form gatePercentages
     */
    public CombinedWindow(String name) {
        initComponents();
        GateDataTable.getModel().addTableModelListener(this);
        setTitle(getTitle() + ": " + name);


  
    }
    
    
    public void setVisible() {
        this.setVisible(false);
    }

    public int getNumberOfGates() {
        return gateList.size();
    }

    protected void notifyUpdateNameListeners(String name, int row) {
        for (NameUpdateListener listener : nameUpdateListeners) {
            listener.onUpdateName(name, row);
        }
    }

    public void addUpdateNameListener(NameUpdateListener listener) {
        nameUpdateListeners.add(listener);
    }

    protected void notifyRemapOverlayListeners(boolean b, int row) {
        for (remapOverlayListener listener : remapOverlayListeners) {
            listener.onRemapOverlay(b, row);
        }
    }
    
    public void addGatePlotListener(GatePlotListener listener) {
        gatePlotListeners.add(listener);
    }

    protected void notifyGatePlotListeners(String x, String y) {
        for (GatePlotListener listener : gatePlotListeners) {
            listener.onGatePlot(x, y);
        }
    }

    public void addRemapOverlayListener(remapOverlayListener listener) {
        remapOverlayListeners.add(listener);
    }

    protected void notifyUpdateColorListeners(Color color, int row) {
        for (colorUpdateListener listener : UpdateColorListeners) {
            listener.onColorUpdate(color, row);
        }
    }

    public void addGateActionListener(GateManagerActionListener listener) {
        gateManagerListeners.add(listener);
    }

    protected void notifyGateActionListeners(String st) {
        for (GateManagerActionListener listener : gateManagerListeners) {
            listener.doGates(st);
        }
    }

    public void addUpdateColorListener(colorUpdateListener listener) {
        UpdateColorListeners.add(listener);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jRadioButton1 = new javax.swing.JRadioButton();
        jToolBar1 = new javax.swing.JToolBar();
        currentMeasure = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        LoadGates = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jScrollPane1 = new javax.swing.JScrollPane();
        GateDataTable = new javax.swing.JTable();

        jRadioButton1.setText("jRadioButton1");

        setTitle("Gate Management");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setFocusTraversalPolicyProvider(true);
        setMinimumSize(new java.awt.Dimension(725, 240));
        setSize(new java.awt.Dimension(725, 280));
        setType(java.awt.Window.Type.UTILITY);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setBorderPainted(false);
        jToolBar1.setMaximumSize(new java.awt.Dimension(700, 35));
        jToolBar1.setMinimumSize(new java.awt.Dimension(700, 35));
        jToolBar1.setPreferredSize(new java.awt.Dimension(700, 35));

        currentMeasure.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        currentMeasure.setText("VTEA datasets for combined analysis...");
        currentMeasure.setToolTipText(currentMeasure.getText());
        currentMeasure.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        currentMeasure.setMaximumSize(new java.awt.Dimension(670, 20));
        currentMeasure.setMinimumSize(new java.awt.Dimension(520, 20));
        currentMeasure.setPreferredSize(new java.awt.Dimension(580, 20));
        jToolBar1.add(currentMeasure);
        jToolBar1.add(jSeparator2);

        LoadGates.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/document-open-folder_24.png"))); // NOI18N
        LoadGates.setToolTipText("Load gates...");
        LoadGates.setFocusable(false);
        LoadGates.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        LoadGates.setMaximumSize(new java.awt.Dimension(35, 40));
        LoadGates.setMinimumSize(new java.awt.Dimension(35, 40));
        LoadGates.setName(""); // NOI18N
        LoadGates.setPreferredSize(new java.awt.Dimension(35, 40));
        LoadGates.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        LoadGates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LoadGatesActionPerformed(evt);
            }
        });
        jToolBar1.add(LoadGates);
        jToolBar1.add(jSeparator1);

        getContentPane().add(jToolBar1, java.awt.BorderLayout.NORTH);

        jScrollPane1.setMaximumSize(new java.awt.Dimension(725, 200));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(725, 200));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(725, 200));

        GateDataTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Gate", "Name", "X axis", "Y Axis", "Gated", "Total", "%", "", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        GateDataTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        GateDataTable.setMaximumSize(new java.awt.Dimension(665, 200));
        GateDataTable.setMinimumSize(new java.awt.Dimension(665, 200));
        GateDataTable.setPreferredSize(new java.awt.Dimension(665, 200));
        GateDataTable.setSize(new java.awt.Dimension(665, 200));
        jScrollPane1.setViewportView(GateDataTable);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.LINE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void LoadGatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoadGatesActionPerformed
        notifyGateActionListeners("import");
    }//GEN-LAST:event_LoadGatesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable GateDataTable;
    private javax.swing.JButton LoadGates;
    public javax.swing.JLabel currentMeasure;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables

    public void setMeasurementsText(String st) {
        if (st.length() > 60) {
            this.currentMeasure.setFont(new java.awt.Font("Lucida Grande", 0, 14));
        } else if (st.length() < 60 && st.length() > 30) {
            this.currentMeasure.setFont(new java.awt.Font("Lucida Grande", 0, 16));
        } else {
            this.currentMeasure.setFont(new java.awt.Font("Lucida Grande", 0, 18));
        }
        this.currentMeasure.setToolTipText(st);
        this.currentMeasure.setText(st);
    }

    public void addGateToTable(PolygonGate g) {
        //if (gateList.size() > 0) {
        if (g.getGateAsPoints().size() > 2) {
            ((DefaultTableModel) GateDataTable.getModel()).addRow(
                    new Object[]{g.getSelected(),
                        g.getColor(),
                        g.getName(),
                        g.getXAxis(),
                        g.getYAxis(),
                        g.getObjectsInGate(),
                        g.getTotalObjects(),
                        (float) 100 * ((int) g.getObjectsInGate()) / ((int) g.getTotalObjects())
                    });

        }
        pack();
        repaint();

    }

    public void updateGateSelection(ArrayList<PolygonGate> gates) {

        ListIterator<PolygonGate> itr = gates.listIterator();
        int i = 0;
        while (itr.hasNext()) {
            PolygonGate pg = (PolygonGate) itr.next();
            boolean selected = pg.getSelected();
            GateDataTable.getModel().setValueAt(selected, i, 0);
            i++;
        }
        pack();
        repaint();

    }

    protected ArrayList<PolygonGate> cleanGateList(ArrayList<PolygonGate> gates) {
        ListIterator<PolygonGate> itr = gates.listIterator();

        ArrayList<PolygonGate> result = new ArrayList<>();

        int i = 0;
        while (itr.hasNext()) {
            PolygonGate g = itr.next();
            if (g.getGateAsPoints().size() > 2) {
                result.add(g);
            }
        }
        return result;
    }

    public void updateTable(ArrayList<PolygonGate> gates, boolean view) {

        gateList = null;
        gateList = cleanGateList(gates);

        if (gateList.size() > 0) {

            ListIterator<PolygonGate> itr = gateList.listIterator();

            Object[][] gatesData = new Object[gateList.size()][9];
            int i = 0;
            while (itr.hasNext()) {
                Object[] gateData = new Object[9];

                PolygonGate pg = (PolygonGate) itr.next();

                gateData[0] = pg.getSelected();
                gateData[1] = pg.getColor();
                gateData[2] = pg.getName();
                gateData[3] = pg.getXAxis();
                gateData[4] = pg.getYAxis();
                gateData[5] = pg.getObjectsInGate();
                gateData[6] = pg.getTotalObjects();
                if (pg.getTotalObjects() > 0) {
                    gateData[7] = (float) 100 * ((int) pg.getObjectsInGate()) / ((int) pg.getTotalObjects());
                }
                gatesData[i] = gateData;
                i++;

            }

            String[] columnNames = {"Name",
                "File",
                "Name",
                "XAxis",
                "YAxis",
                "Gated",
                "Total",
                "Normalization"
            };

            this.GateDataTable = new JTable(gatesData, columnNames) {
                @Override
                public TableCellRenderer getCellRenderer(int row, int column) {
                    for (int i = 0; i < gatesData.length; i++) {
                        if ((row == i) && (column == 1)) {
                            return new ColorRenderer(true, (Color) gatesData[i][1]);
                        }
                    }

                    return super.getCellRenderer(row, column);
                }
            };

            GateDataTable.setModel(new javax.swing.table.DefaultTableModel(
                    gatesData,
                    columnNames
            ) {

                @Override
                public Class getColumnClass(int c) {

                    return getValueAt(0, c).getClass();

                }

                @Override
                public boolean isCellEditable(int row, int col) {

                    if (col == 2 || col == 0 && view) {
                        return true;
                    } else {
                        return false;
                    }
                }

            });
            
                    GateDataTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                try{
                int row = GateDataTable.rowAtPoint(e.getPoint());
                int col = GateDataTable.columnAtPoint(e.getPoint());

                String stringX = GateDataTable.getValueAt(row, 3).toString();
                String stringY = GateDataTable.getValueAt(row, 4).toString();
                
                //System.out.println("PROFILING: " + stringX + ", " + stringY);
                
                notifyGatePlotListeners(stringX, stringY);
                
                }catch(Exception ex){}
            }
        }
            
        );

            GateDataTable.setDefaultRenderer(Color.class,
                    new ColorRenderer(true, new Color(255, 0, 0)));
            GateDataTable.setDefaultEditor(Color.class,
                    new ColorEditor());

            GateDataTable.getModel().addTableModelListener(this);

            GateDataTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
            GateDataTable.setMaximumSize(new java.awt.Dimension(680, 1400));
            GateDataTable.setMinimumSize(new java.awt.Dimension(630, 1400));
            GateDataTable.setPreferredSize(new java.awt.Dimension(680, 1400));
            GateDataTable.setShowGrid(true);

            TableColumn column = null;
            column = GateDataTable.getColumnModel().getColumn(0);
            column.setPreferredWidth(40);
            column = GateDataTable.getColumnModel().getColumn(1);
            column.setPreferredWidth(60);
            column = GateDataTable.getColumnModel().getColumn(2);
            column.setPreferredWidth(120);
            column = GateDataTable.getColumnModel().getColumn(3);
            column.setPreferredWidth(115);
            column = GateDataTable.getColumnModel().getColumn(4);
            column.setPreferredWidth(115);
            column = GateDataTable.getColumnModel().getColumn(5);
            column.setPreferredWidth(90);
            column = GateDataTable.getColumnModel().getColumn(6);
            column.setPreferredWidth(90);
            column = GateDataTable.getColumnModel().getColumn(7);
            column.setPreferredWidth(55);
            GateDataTable.doLayout();
            GateDataTable.repaint();

            jScrollPane1.setViewportView(GateDataTable);
        } else {
            GateDataTable = new JTable();
            GateDataTable.setModel(new javax.swing.table.DefaultTableModel(
                    new Object[][]{
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null}
                    },
                    new String[]{
                        "View",
                        "Color",
                        "Name",
                        "XAxis",
                        "YAxis",
                        "Gated",
                        "Total",
                        "%"

                    }
            ) {
                Class[] types = new Class[]{
                    java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class
                };
                boolean[] canEdit = new boolean[]{
                    false, true, false, false, false, false, false, false, false
                };

                @Override
                public Class getColumnClass(int columnIndex) {
                    return types[columnIndex];
                }

                @Override
                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });

            jScrollPane1.setViewportView(GateDataTable);
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int column = e.getColumn();
        TableModel model = (TableModel) e.getSource();
        String columnName = model.getColumnName(column);
        Object data = model.getValueAt(row, column);
        if (column == 2) {
            notifyUpdateNameListeners((String) data, row);
        }
        if (column == 0) {
            notifyRemapOverlayListeners((Boolean) data, row);
        }
        if (column == 1) {
            notifyUpdateColorListeners((Color) data, row);
        }
    }

}


