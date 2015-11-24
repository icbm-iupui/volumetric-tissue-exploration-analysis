/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vteaexploration.plottools.panels;

/**
 *
 * @author vinfrais
 */
public class ChartPanelDoesNotExistException extends Exception {

    /**
     * Creates a new instance of <code>ChartPanelDoesNotExistException</code>
     * without detail message.
     */
    public ChartPanelDoesNotExistException() {
    }

    /**
     * Constructs an instance of <code>ChartPanelDoesNotExistException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public ChartPanelDoesNotExistException(String msg) {
        super(msg);
    }
}
