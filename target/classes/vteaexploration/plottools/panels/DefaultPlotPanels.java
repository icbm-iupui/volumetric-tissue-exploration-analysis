/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vteaexploration.plottools.panels;


import javax.swing.JPanel;

/**
 *
 * @author vinfrais
 */
public class DefaultPlotPanels implements PlotAxesPanels {

    protected JPanel HeaderPanel = new JPanel(true);
    protected JPanel FooterPanel = new JPanel(true);
    protected JPanel LeftPanel = new JPanel(true);
    protected JPanel RightPanel = new JPanel(true);

    public DefaultPlotPanels() {
        HeaderPanel.setBackground(VTC._VTC.BACKGROUND);
        FooterPanel.setBackground(VTC._VTC.BACKGROUND);
        LeftPanel.setBackground(VTC._VTC.BACKGROUND);
        RightPanel.setBackground(VTC._VTC.BACKGROUND);
    }
    

    @Override
    public JPanel getBorderPanelHeader() {
        return HeaderPanel;
    }

    @Override
    public JPanel getBorderPanelFooter() {
        return FooterPanel;
    }

    @Override
    public JPanel getBorderPanelLeft() {
        return LeftPanel;
    }

    @Override
    public JPanel getBorderPanelRight() {
        return RightPanel;
    }

}
