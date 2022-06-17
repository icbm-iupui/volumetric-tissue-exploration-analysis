/*
 * Copyright (C) 2022 SciJava
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

/*Derived from Jfreechart XY plot demo*/

package vtea.clustering.kestimation;
        

import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JPanel;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 import org.jfree.ui.ApplicationFrame;


/**
 *
 * @author Seth
 */
public class XYPlotkEstimation extends JFrame {

/**
 * Plotting for k-estimation.
 *
 * @param title  the frame title.
     * @param Xaxis
     * @param Yaxis
     * @param series
 */
public XYPlotkEstimation(final String title, String Xaxis, String Yaxis, XYSeries series) {

    super(title);
    final XYSeriesCollection data = new XYSeriesCollection(series);
    final JFreeChart chart = ChartFactory.createXYLineChart(
        title,
        Xaxis, 
        Yaxis, 
        data,
        PlotOrientation.VERTICAL,
        true,
        true,
        false
    );
    
    XYPlot plot = chart.getXYPlot();
    
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainMinorGridlinePaint(Color.GRAY);
        plot.setDomainGridlinePaint(Color.BLACK);
        plot.setRangeMinorGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.BLACK);


    final ChartPanel chartPanel = new ChartPanel(chart);
    chartPanel.setPreferredSize(new java.awt.Dimension(500, 300));
    setContentPane(chartPanel);
}
}