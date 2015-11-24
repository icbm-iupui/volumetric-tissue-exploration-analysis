/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vteaexploration.plotgatetools.listeners;

import vteaexploration.plotgatetools.gates.Gate;
import java.awt.Color;
import java.util.ArrayList;

/**
 *
 * @author vinfrais
 */
public interface MakeImageOverlayListener {

    void makeOverlayImage(ArrayList gates, int xAxis, int yAxis);
}
