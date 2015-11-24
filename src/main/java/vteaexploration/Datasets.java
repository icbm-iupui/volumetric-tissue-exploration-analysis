/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vteaexploration;

import java.util.List;

/**
 *
 * @author vinfrais
 */
public interface Datasets {

    public int getObjectCount();

    public int getColumnCount();

    public String[] getColumnTitles();

    public List getObjects();
}
