/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vteaexploration.listeners;

/**
 *
 * @author vinfrais
 * 
 */
public interface PlotRequestListener {

    public void onPlotButton(int[] Axes);
    //pass the X and Y to plot, per the table:
    //[channel][0, count, 1, mean, 2, integrated density, 3, min, 4, max, 5 standard deviation]
}
