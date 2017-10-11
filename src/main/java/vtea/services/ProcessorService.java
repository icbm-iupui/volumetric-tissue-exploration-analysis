/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vtea.services;

import org.scijava.Context;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;
import vtea.processor.Processor;



/**
 *
 * @author sethwinfree
 */
@Plugin(type = Service.class)
public class ProcessorService extends AbstractService< Processor > {

    public ProcessorService(Context context) {
        super(Processor.class, context);
    }
}
