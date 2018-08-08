/* 
 * Copyright (C) 2016-2018 Indiana University
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
package vtea.exploration.plottools.panels;


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
        HeaderPanel.setBackground(vtea._vtea.BACKGROUND);
        FooterPanel.setBackground(vtea._vtea.BACKGROUND);
        LeftPanel.setBackground(vtea._vtea.BACKGROUND);
        RightPanel.setBackground(vtea._vtea.BACKGROUND);
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
