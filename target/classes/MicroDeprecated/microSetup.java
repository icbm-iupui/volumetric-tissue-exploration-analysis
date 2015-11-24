/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package MicroDeprecated;

import MicroProtocol.listeners.AnalysisStartListener;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.util.ArrayList;
import javax.swing.DefaultCellEditor;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 *
 * @author vinfrais
 */
@Deprecated

public class microSetup extends javax.swing.JFrame implements TableModelListener {

    static final int INTERLEAVED = 0;
    static final int HYPERSTACK = 1;

    //static final int CHANNEL = 2;
    public static final int MASK = 0;
    public static final int EXPERIMENTAL = 1;
    public static final int IGNORED = 2;

    public static final int GROW = 1;  //use subtype to determine how much
    public static final int FILL = 2;
//public static final int MASK = 0;

    public static final int SIZE = 0;
    public static final int MEAN = 1;
    public static final int INT_DEN = 2;
    public static final int MIN = 3;
    public static final int MAX = 4;
    public static final int STDEV = 5;
    public static final int AR = 6;
    public static final int MIN_FERET = 7;
    public static final int MAX_FERET = 8;

    private javax.swing.table.DefaultTableModel tmChannels;
    private Object[][] ChannelValues = {
        {null, null, null, null, null},
        {null, null, null, null, null},
        {null, null, null, null, null},
        {null, null, null, null, null}
    };
    private String[] ChannelTitles = {"Channel", "Name", "Type", "Analysis", "MOD2"};
    private String[] ChannelTypes = {"Mask", "Experimental"};
    private boolean[] canEditChannels = new boolean[]{false, true, true, true, true};

    @Override
    public void tableChanged(TableModelEvent tme) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private class channelType extends javax.swing.JComboBox {

        public channelType() {
            this.setModel(new javax.swing.DefaultComboBoxModel(ChannelTypes));
        }
    ;
    };
   
   private channelType TypeComboBox = new channelType();

    private class analysisType extends javax.swing.JComboBox {

        public analysisType() {
            this.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Mask", "Grow", "Fill"}));
        }
    ;

    };
   
   private analysisType AnalysisComboBox = new analysisType();

    private int column1Width;
    private int column2Width;
    private int column3Width;
    private int column4Width;
    private int column5Width;

    private TableColumn typeColumn;
    private TableColumn analysisColumn;

    private DefaultCellEditor typeEditor = new DefaultCellEditor(TypeComboBox);
    private DefaultCellEditor analysisEditor = new DefaultCellEditor(AnalysisComboBox);

    private ImageStack[] Stacks;

    private float[] minConstants = new float[4];
    // 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold

    private int[] channelType = new int[4];
    // 0: Mask, 1: Experimental, 2: Ignore

    private int[] analysisType = new int[4];
    // 0: MASK, 1: GROW, 2: FILL

    private int[] modificationConstant = new int[4];
    //for modification constant

    private String[] channelTitles = new String[4];

    //private ImageStack[] Experimentals;
    //private int countExperimentals;
    private int position;

    private int channelCount;

    private int x_dim;
    private int y_dim;
    private int height;
    private int interleavedHeight;

    private ImagePlus imp;

    private ArrayList<AnalysisStartListener> listeners = new ArrayList<AnalysisStartListener>();

    byte[] blank; //required by RGB plugin functions

    /**
     * Creates new form microSetup
     */
    public microSetup(ImagePlus localimp) {

        this.imp = localimp;
        GuiSetup();
        initComponents();

        typeColumn = Channels.getColumnModel().getColumn(2);
        analysisColumn = Channels.getColumnModel().getColumn(3);

//        typeColumn.sizeWidthToFit();
//        analysisColumn.sizeWidthToFit();
        typeColumn.setCellEditor(typeEditor);
        analysisColumn.setCellEditor(analysisEditor);

        minimumTextField.setText(String.valueOf(minSlider.getValue()));
        maximumTextField.setText(String.valueOf(maxSlider.getValue()));
        overlapTextField.setText("" + overlapSlider.getValue());

        Channels.getModel().addTableModelListener(this);

        pack();

        this.show();
    }

    private void makeChannelsTable(int nChannels) {

        //Object [][] tempChannelValues = new Object[4][5];
        switch (nChannels) {
            case 1:
                ChannelValues[0][0] = "1";
                ChannelValues[0][1] = "Channel_1";
                ChannelValues[1][0] = "";
                ChannelValues[1][1] = "";
                ChannelValues[2][0] = "";
                ChannelValues[2][1] = "";
                ChannelValues[3][0] = "";
                ChannelValues[3][1] = "";

                break;
            case 2:
                ChannelValues[0][0] = "1";
                ChannelValues[0][1] = "Channel_1";
                ChannelValues[1][0] = "2";
                ChannelValues[1][1] = "Channel_2";
                ChannelValues[2][0] = "";
                ChannelValues[2][1] = "";
                ChannelValues[3][0] = "";
                ChannelValues[3][1] = "";
                break;
            case 3:
                ChannelValues[0][0] = "1";
                ChannelValues[0][1] = "Channel_1";
                ChannelValues[1][0] = "2";
                ChannelValues[1][1] = "Channel_2";
                ChannelValues[2][0] = "3";
                ChannelValues[2][1] = "Channel_3";
                ChannelValues[3][0] = "";
                ChannelValues[3][1] = "";
                break;
            case 4:
                ChannelValues[0][0] = "1";
                ChannelValues[0][1] = "Channel_1";
                ChannelValues[1][0] = "2";
                ChannelValues[1][1] = "Channel_2";
                ChannelValues[2][0] = "3";
                ChannelValues[2][1] = "Channel_3";
                ChannelValues[3][0] = "4";
                ChannelValues[3][1] = "Channel_4";
                break;
            default:
                break;
        }
    }

    private void setupSliders() {
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFormattedTextField1 = new javax.swing.JFormattedTextField();
        labelTitle = new javax.swing.JLabel();
        labelFormat = new javax.swing.JLabel();
        imageFormat = new javax.swing.JComboBox();
        nChannels = new javax.swing.JComboBox();
        labelChannels = new javax.swing.JLabel();
        labelChannelsTable = new javax.swing.JLabel();
        paneChannels = new javax.swing.JScrollPane();
        Channels = new javax.swing.JTable();
        seperatorImage = new javax.swing.JSeparator();
        ThresholdMethod = new javax.swing.JComboBox();
        jlabelThresholdingMethod = new javax.swing.JLabel();
        minSlider = new javax.swing.JSlider();
        maxSlider = new javax.swing.JSlider();
        seperatorObject = new javax.swing.JSeparator();
        overlapSlider = new javax.swing.JSlider();
        Overlap = new javax.swing.JLabel();
        MaximumSize = new javax.swing.JLabel();
        MinimumSize = new javax.swing.JLabel();
        thresholdTextField = new javax.swing.JTextField();
        startButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        seperatorObject1 = new javax.swing.JSeparator();
        maximumTextField = new javax.swing.JTextField();
        minimumTextField = new javax.swing.JTextField();
        overlapTextField = new javax.swing.JTextField();

        jFormattedTextField1.setText("jFormattedTextField1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("VTC-Setup");
        setResizable(false);

        labelTitle.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        labelTitle.setText("Analysis Setup");

        labelFormat.setText("Format");

        imageFormat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Interleaved", "Hyperstack" }));
        imageFormat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imageFormatActionPerformed(evt);
            }
        });

        nChannels.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4" }));
        nChannels.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nChannelsActionPerformed(evt);
            }
        });

        labelChannels.setText("Channels");

        labelChannelsTable.setText("Channels");

        Channels.setModel(new javax.swing.table.DefaultTableModel(
            ChannelValues,
            ChannelTitles
        ) {
            boolean[] canEdit = canEditChannels;

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        Channels.setCellSelectionEnabled(true);
        paneChannels.setViewportView(Channels);

        ThresholdMethod.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "minimum intensity", " " }));
        ThresholdMethod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ThresholdMethodActionPerformed(evt);
            }
        });

        jlabelThresholdingMethod.setText("Thresholding method");

        minSlider.setMajorTickSpacing(1000);
        minSlider.setMaximum(10000);
        minSlider.setMinorTickSpacing(100);
        minSlider.setValue(10);
        minSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                minSliderStateChanged(evt);
            }
        });

        maxSlider.setMajorTickSpacing(1000);
        maxSlider.setMaximum(10000);
        maxSlider.setMinorTickSpacing(100);
        maxSlider.setValue(10000);
        maxSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxSliderStateChanged(evt);
            }
        });

        overlapSlider.setMajorTickSpacing(1);
        overlapSlider.setMaximum(20);
        overlapSlider.setMinorTickSpacing(1);
        overlapSlider.setValue(5);
        overlapSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                overlapSliderStateChanged(evt);
            }
        });
        overlapSlider.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                overlapSliderCaretPositionChanged(evt);
            }
        });

        Overlap.setText("Overlap");

        MaximumSize.setText("Maximum size");

        MinimumSize.setText("Minimum size");

        thresholdTextField.setMinimumSize(new java.awt.Dimension(80, 28));
        thresholdTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                thresholdTextFieldActionPerformed(evt);
            }
        });

        startButton.setText("Start");

        cancelButton.setText("Cancel");

        saveButton.setText("Save");

        maximumTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maximumTextFieldActionPerformed(evt);
            }
        });

        minimumTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minimumTextFieldActionPerformed(evt);
            }
        });

        overlapTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                overlapTextFieldActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(seperatorObject1)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(paneChannels, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 427, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(layout.createSequentialGroup()
                            .add(jlabelThresholdingMethod)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(ThresholdMethod, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                            .add(thresholdTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 99, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .add(10, 10, 10))
            .add(layout.createSequentialGroup()
                .add(5, 5, 5)
                .add(seperatorImage)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(seperatorObject)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(layout.createSequentialGroup()
                            .add(saveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 89, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(cancelButton)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(startButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 88, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, labelChannelsTable, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(layout.createSequentialGroup()
                                            .add(12, 12, 12)
                                            .add(MinimumSize)
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                            .add(minimumTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 51, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .add(org.jdesktop.layout.GroupLayout.TRAILING, minSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 132, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(18, 18, 18)
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(layout.createSequentialGroup()
                                            .add(MaximumSize)
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                            .add(maximumTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 51, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .add(maxSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 132, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(24, 24, 24)
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                            .add(Overlap)
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                            .add(overlapTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 51, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(12, 12, 12))
                                        .add(overlapSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 132, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                    .add(labelFormat)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                    .add(imageFormat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 225, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(labelChannels)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(nChannels, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(2, 2, 2)))
                    .add(labelTitle))
                .add(10, 10, 10))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(labelTitle)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(imageFormat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(labelFormat)
                    .add(nChannels, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(labelChannels))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(seperatorImage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(5, 5, 5)
                .add(labelChannelsTable)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(paneChannels, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 95, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ThresholdMethod, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(thresholdTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jlabelThresholdingMethod))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(seperatorObject1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(minSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(maxSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(overlapSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(MinimumSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(MaximumSize)
                    .add(Overlap)
                    .add(maximumTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(minimumTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(overlapTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(4, 4, 4)
                .add(seperatorObject, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(startButton)
                    .add(cancelButton)
                    .add(saveButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void nChannelsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nChannelsActionPerformed

        makeChannelsTable(nChannels.getSelectedIndex() + 1);

        Channels.setModel(new javax.swing.table.DefaultTableModel(ChannelValues, ChannelTitles) {

            boolean[] canEdit = canEditChannels;

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        typeColumn = Channels.getColumnModel().getColumn(2);
        analysisColumn = Channels.getColumnModel().getColumn(3);

        typeColumn.setCellEditor(typeEditor);
        analysisColumn.setCellEditor(analysisEditor);

        pack();
    }//GEN-LAST:event_nChannelsActionPerformed

    private void imageFormatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imageFormatActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_imageFormatActionPerformed

    private void ThresholdMethodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ThresholdMethodActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ThresholdMethodActionPerformed

    private void thresholdTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_thresholdTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_thresholdTextFieldActionPerformed

    private void overlapSliderCaretPositionChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_overlapSliderCaretPositionChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_overlapSliderCaretPositionChanged

    private void minSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_minSliderStateChanged
        minimumTextField.setText(String.valueOf(minSlider.getValue()));

    }//GEN-LAST:event_minSliderStateChanged

    private void maxSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxSliderStateChanged
        maximumTextField.setText(String.valueOf(maxSlider.getValue()));
        // IJ.log("              maxSliderValue changed " + maxSlider.getValue());
    }//GEN-LAST:event_maxSliderStateChanged

    private void overlapSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_overlapSliderStateChanged
        overlapTextField.setText(String.valueOf(overlapSlider.getValue()));
        // IJ.log("              overlapSliderValue changed " + overlapSlider.getValue());
    }//GEN-LAST:event_overlapSliderStateChanged

    private void minimumTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minimumTextFieldActionPerformed
        minSlider.setValue(Integer.parseInt(minimumTextField.getText()));
    }//GEN-LAST:event_minimumTextFieldActionPerformed

    private void maximumTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maximumTextFieldActionPerformed
        maxSlider.setValue(Integer.parseInt(maximumTextField.getText()));
    }//GEN-LAST:event_maximumTextFieldActionPerformed

    private void overlapTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_overlapTextFieldActionPerformed
        overlapSlider.setValue(Integer.parseInt(overlapTextField.getText()));
    }//GEN-LAST:event_overlapTextFieldActionPerformed

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        startAnalysis();
        notifyListeners(1);
    }//GEN-LAST:event_startButtonActionPerformed

    private void OptimizeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OptimizeCheckBoxActionPerformed

    }//GEN-LAST:event_OptimizeCheckBoxActionPerformed

    public void startAnalysis() {

        if (Channels.isEditing()) {
            Channels.getCellEditor().stopCellEditing();
        }

        TableModel ChannelTableValues = Channels.getModel();

        y_dim = imp.getHeight();
        x_dim = imp.getWidth();
        height = imp.getStackSize();

        channelCount = Integer.parseInt(String.valueOf(nChannels.getSelectedItem()));
        interleavedHeight = imp.getImageStackSize() / channelCount;

        Stacks = getInterleavedStacks(imp);

//        IJ.log("microSetup...                parsing minConstants");
//        IJ.log("microSetup...                min: " + Float.parseFloat(minimumTextField.getText()));
//        IJ.log("microSetup...                max: " + Float.parseFloat(maximumTextField.getText()));
//        IJ.log("microSetup...                overlap: " + Float.parseFloat(overlapTextField.getText()));
//        IJ.log("microSetup...                threshold: " + Float.parseFloat(thresholdTextField.getText()));

        //set min constants
        // 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold
        minConstants[0] = Float.parseFloat(minimumTextField.getText());
        minConstants[1] = Float.parseFloat(maximumTextField.getText());
        minConstants[2] = Float.parseFloat(overlapTextField.getText());
        minConstants[3] = Float.parseFloat(thresholdTextField.getText());

//        private ImageStack[] Stacks;
//    
//   private int[] channelType = new int[4];
//        // 0: Mask, 1: Experimental, 2: Ignore
       // IJ.log("microSetup...                getting Channel type");
        channelType[0] = getTypeInt(0, ChannelTableValues);//IJ.log("microSetup...                Channel 1 Type: " + getTypeInt(0, ChannelTableValues));
        channelType[1] = getTypeInt(1, ChannelTableValues);//IJ.log("microSetup...                Channel 2 Type: " + getTypeInt(1, ChannelTableValues));
        channelType[2] = getTypeInt(2, ChannelTableValues);//IJ.log("microSetup...                Channel 3 Type: " + getTypeInt(2, ChannelTableValues));
        channelType[3] = getTypeInt(3, ChannelTableValues);//IJ.log("microSetup...                Channel 4 Type: " + getTypeInt(3, ChannelTableValues));

//   private int[] analysisType = new int[4];
//        // 0: MASK, 1: GROW, 2: FILL
       // IJ.log("microSetup...                getting Analysis type");
        analysisType[0] = getAnalysisTypeInt(0, ChannelTableValues);// IJ.log("microSetup...                Analysis 1: " + getAnalysisTypeInt(0, ChannelTableValues));
        analysisType[1] = getAnalysisTypeInt(1, ChannelTableValues);// IJ.log("microSetup...                Analysis 2: " + getAnalysisTypeInt(1, ChannelTableValues));
        analysisType[2] = getAnalysisTypeInt(2, ChannelTableValues);// IJ.log("microSetup...                Analysis 3: " + getAnalysisTypeInt(2, ChannelTableValues));
        analysisType[3] = getAnalysisTypeInt(3, ChannelTableValues);// IJ.log("microSetup...                Analysis 4: " + getAnalysisTypeInt(3, ChannelTableValues));

//        //for modification constant
       // IJ.log("microSetup...                getting Sub type");

        if (ChannelTableValues.getValueAt(0, 4) != null) {
            modificationConstant[0] = Integer.valueOf(ChannelTableValues.getValueAt(0, 4).toString());
        }
        if (ChannelTableValues.getValueAt(1, 4) != null) {
            modificationConstant[1] = Integer.valueOf(ChannelTableValues.getValueAt(1, 4).toString());
        }
        if (ChannelTableValues.getValueAt(2, 4) != null) {
            modificationConstant[2] = Integer.valueOf(ChannelTableValues.getValueAt(2, 4).toString());
        }
        if (ChannelTableValues.getValueAt(3, 4) != null) {
            modificationConstant[3] = Integer.valueOf(ChannelTableValues.getValueAt(3, 4).toString());
        }

       // IJ.log("microSetup...                getting titles");

        if (ChannelTableValues.getValueAt(0, 1) != null) {
            channelTitles[0] = ChannelTableValues.getValueAt(0, 1).toString();
        }//IJ.log("microSetup...                " + ChannelTableValues.getValueAt(0,1).toString());}
        if (ChannelTableValues.getValueAt(1, 1) != null) {
            channelTitles[1] = ChannelTableValues.getValueAt(1, 1).toString();
        }//IJ.log("microSetup...                " + ChannelTableValues.getValueAt(1,1).toString());}
        if (ChannelTableValues.getValueAt(2, 1) != null) {
            channelTitles[2] = ChannelTableValues.getValueAt(2, 1).toString();
        }//IJ.log("microSetup...                " + ChannelTableValues.getValueAt(2,1).toString());}
        if (ChannelTableValues.getValueAt(3, 1) != null) {
            channelTitles[3] = ChannelTableValues.getValueAt(3, 1).toString();
        }//IJ.log("microSetup...                " + ChannelTableValues.getValueAt(3,1).toString());}

       // IJ.log("microSetup...                ...analysis starting...");

        this.setVisible(false);
    }

    private int getTypeInt(int row, TableModel ChannelTableValues) {

        //IJ.log("microSetup...TypeInt                " + (ChannelValues[row][2]));
        if (ChannelTableValues.getValueAt(row, 2) == null) {
            return 2;
        }

        String comp = ChannelTableValues.getValueAt(row, 2).toString();

        if (comp == null) {
            return 2;
        }
        if (comp.equals("Mask")) {
            return 0;
        }
        if (comp.equals("Experimental")) {
            return 1;
        }
        if (comp.equals("Ignore")) {
            return 2;
        } else {
            return 2;
        }

    }

    private int getAnalysisTypeInt(int row, TableModel ChannelTableValues) {

        if (ChannelTableValues.getValueAt(row, 3) == null) {
            return 2;
        }

        String comp = ChannelTableValues.getValueAt(row, 3).toString();

        if (comp == null) {
            return 2;
        }
        if (comp.equals("Mask")) {
            return 0;
        }
        if (comp.equals("Grow")) {
            return 1;
        }
        if (comp.equals("Fill")) {
            return 2;
        } else {
            return 0;
        }
    }

    public ImageStack[] getInterleavedStacks(ImagePlus imp) {
        ImageStack[] stacks = new ImageStack[channelCount];
        ImageStack stack = imp.getImageStack();

        for (int m = 0; m <= channelCount - 1; m++) {
            stacks[m] = new ImageStack(x_dim, y_dim);
            for (int n = m; n <= imp.getStackSize() - 1; n += channelCount) {
                stacks[m].addSlice(stack.getProcessor(n + 1));
            }
        }
//        IJ.log("microSetup::getInterleavedStacks           Generated stack array.");
//        IJ.log("        ImagePlus height:  " + imp.getStackSize());
//        IJ.log("        Interleaved height:  " + interleavedHeight);
        //IJ.log("        Channel count:  " + channelCount);
        //IJ.log("        Stack height:  " + stacks[0].getSize());

        return stacks;
    }

    //taken from RGB merge plugin, http://rsbweb.nih.gov/ij/plugins/rgb-merge.html
    private ImageStack mergeStacks(int w, int h, int d, ImageStack red, ImageStack green, ImageStack blue, boolean keep) {
        ImageStack rgb = new ImageStack(w, h);
        int inc = d / 10;
        if (inc < 1) {
            inc = 1;
        }
        ColorProcessor cp;
        int slice = 1;
        blank = new byte[w * h];
        byte[] redPixels, greenPixels, bluePixels;
        boolean invertedRed = red != null ? red.getProcessor(1).isInvertedLut() : false;
        boolean invertedGreen = green != null ? green.getProcessor(1).isInvertedLut() : false;
        boolean invertedBlue = blue != null ? blue.getProcessor(1).isInvertedLut() : false;
        try {
            for (int i = 1; i <= d; i++) {
                cp = new ColorProcessor(w, h);
                redPixels = getPixels(red, slice, 0);
                greenPixels = getPixels(green, slice, 1);
                bluePixels = getPixels(blue, slice, 2);
                if (invertedRed) {
                    redPixels = invert(redPixels);
                }
                if (invertedGreen) {
                    greenPixels = invert(greenPixels);
                }
                if (invertedBlue) {
                    bluePixels = invert(bluePixels);
                }
                cp.setRGB(redPixels, greenPixels, bluePixels);
                if (keep) {
                    slice++;
                } else {
                    if (red != null) {
                        red.deleteSlice(1);
                    }
                    if (green != null && green != red) {
                        green.deleteSlice(1);
                    }
                    if (blue != null && blue != red && blue != green) {
                        blue.deleteSlice(1);
                    }
                }
                rgb.addSlice(null, cp);
                if ((i % inc) == 0) {
                    IJ.showProgress((double) i / d);
                }
            }
            IJ.showProgress(1.0);
        } catch (OutOfMemoryError o) {
            IJ.outOfMemory("Merge Stacks");
            IJ.showProgress(1.0);
        }
        return rgb;
    }

    private byte[] getPixels(ImageStack stack, int slice, int color) {
        if (stack == null) {
            return blank;
        }
        Object pixels = stack.getPixels(slice);
        if (!(pixels instanceof int[])) {
            if (pixels instanceof byte[]) {
                return (byte[]) pixels;
            } else {
                ImageProcessor ip = stack.getProcessor(slice);
                ip = ip.convertToByte(true);
                return (byte[]) ip.getPixels();
            }
        } else { //RGB
            byte[] r, g, b;
            int size = stack.getWidth() * stack.getHeight();
            r = new byte[size];
            g = new byte[size];
            b = new byte[size];
            ColorProcessor cp = (ColorProcessor) stack.getProcessor(slice);
            cp.getRGB(r, g, b);
            switch (color) {
                case 0:
                    return r;
                case 1:
                    return g;
                case 2:
                    return b;
            }
        }
        return null;
    }

    private byte[] invert(byte[] pixels) {
        byte[] pixels2 = new byte[pixels.length];
        System.arraycopy(pixels, 0, pixels2, 0, pixels.length);
        for (int i = 0; i < pixels2.length; i++) {
            pixels2[i] = (byte) (255 - pixels2[i] & 255);
        }
        return pixels2;
    }

    /**
     * @param args the command line arguments
     */
    public ImageStack[] getStacks() {
        //if(OptimizeCheckBox.isSelected()) {return this.getInterleavedStacks(this.imp);}
        return Stacks;
    }

    public float[] getMinConstants() {
        return minConstants;
    }

    public String[] getChannelTitles() {
        return channelTitles;
    }

    public String getImageName() {
        return imp.getTitle();
    }

    ;
    public int getChannelCount() {
        return channelCount;
    }

    public int getStackHeight() {
        return interleavedHeight;
    }

    public int[] getChannelTypes() {
        return this.channelType;
    }
    //public boolean getOptimizationFlag() {return OptimizeCheckBox.isSelected();}

    public int[][] getDerivedRegionTypes() {
        int[][] localDerivedRegionType = new int[channelCount][2];
        //[Channel][RegionDerivation, ModValue]

        for (int i = 0; i <= channelCount - 1; i++) {
            localDerivedRegionType[i][0] = analysisType[i];
            localDerivedRegionType[i][1] = modificationConstant[i];
            //IJ.log("microSetup...         For channel: " + i + " Derived region type:" + localDerivedRegionType[i][0] + " with mod: " + localDerivedRegionType[i][1]);
        }
        return localDerivedRegionType;
    }

    public ImageStack getRGBStack() {
        ImageStack is = mergeStacks(this.x_dim, this.y_dim, this.height, Stacks[0], Stacks[1], Stacks[2], true);
        return is;
    }

//    public ImageStack[] getExperimentalStacks() {return Experimentals;}
//    public int getExperimentalCount() {return countExperimentals;}
    private void GuiSetup() {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(microSetup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(microSetup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(microSetup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(microSetup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                //new microWindowManager().setVisible(true);
            }
        });

    }

    public void addListener(AnalysisStartListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners(int i) {
        for (AnalysisStartListener listener : listeners) {

            listener.onStartButton(i);

        }
    }

//    public static void main(String args[]) {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(microSetup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(microSetup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(microSetup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(microSetup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//              
//            }
//        });
//    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable Channels;
    private javax.swing.JLabel MaximumSize;
    private javax.swing.JLabel MinimumSize;
    private javax.swing.JLabel Overlap;
    private javax.swing.JComboBox ThresholdMethod;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox imageFormat;
    private javax.swing.JFormattedTextField jFormattedTextField1;
    private javax.swing.JLabel jlabelThresholdingMethod;
    private javax.swing.JLabel labelChannels;
    private javax.swing.JLabel labelChannelsTable;
    private javax.swing.JLabel labelFormat;
    private javax.swing.JLabel labelTitle;
    private javax.swing.JSlider maxSlider;
    private javax.swing.JTextField maximumTextField;
    private javax.swing.JSlider minSlider;
    private javax.swing.JTextField minimumTextField;
    private javax.swing.JComboBox nChannels;
    private javax.swing.JSlider overlapSlider;
    private javax.swing.JTextField overlapTextField;
    private javax.swing.JScrollPane paneChannels;
    private javax.swing.JButton saveButton;
    private javax.swing.JSeparator seperatorImage;
    private javax.swing.JSeparator seperatorObject;
    private javax.swing.JSeparator seperatorObject1;
    private javax.swing.JButton startButton;
    public javax.swing.JTextField thresholdTextField;
    // End of variables declaration//GEN-END:variables
}
