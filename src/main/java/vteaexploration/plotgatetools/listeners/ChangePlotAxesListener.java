/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vteaexploration.plotgatetools.listeners;

/**
 *
 * @author vinfrais
 */
public interface ChangePlotAxesListener {

    public void onChangeAxes(int x, int y, int z, int size);
    
    public void onChangePointSize(int size);

}
