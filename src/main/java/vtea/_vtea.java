/* 
 * Copyright (C) 2020 Indiana University
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
package vtea;

import ij.IJ;
import ij.ImageJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.io.LogStream;
import ij.plugin.PlugIn;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;
import javax.swing.UIManager;
import org.scijava.Context;
import org.scijava.Prioritized;
import org.scijava.Priority;
import org.scijava.log.LogService;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.plugin.RichPlugin;
import org.scijava.ui.UIService;
import vtea.protocol.ProtocolManagerMulti;
import vtea.renjin.TestRenjin;
import vtea.services.FeatureService;
import vtea.services.FileTypeService;
import vtea.services.ImageProcessingService;
import vtea.services.LUTService;
import vtea.services.MorphologicalFilterService;
import vtea.services.NeighborhoodMeasurementService;
import vtea.services.ObjectMeasurementService;
import vtea.services.ProcessorService;
import vtea.services.SegmentationService;
import vtea.services.WorkflowService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import javax.swing.JPanel;
import org.apache.commons.io.FileUtils;
import vtea.jdbc.H2DatabaseEngine;
import vtea.services.GateMathService;
import vtea.services.PlotMakerService;



//@Plugin(type= RichPlugin.class, priority=Priority.HIGH_PRIORITY, menuPath = "Plugins>IU_Tools>VTEA")
public class _vtea implements PlugIn, RichPlugin, ImageListener, ActionListener {

    public static String VERSION = new String("1.1.5");
    //public static Connection connection = H2DatabaseEngine.getDBConnection();
    public ProtocolManagerMulti protocolWindow;

    public static Context context;
    public double priority;
    
    public String mode;

    public static Color BACKGROUND = new Color(204, 204, 204);
    public static Color BUTTONBACKGROUND = new Color(200, 200, 200);
    public static Color ACTIONPANELBACKGROUND = new Color(240, 240, 240);
    public static Color INACTIVETEXT = new Color(153, 153, 153);
    public static Color ACTIVETEXT = new Color(0, 0, 0);
    public static Dimension SMALLBUTTONSIZE = new Dimension(32, 32);
    public static Dimension BLOCKSETUP = new Dimension(370, 350);
    public static Dimension BLOCKSETUPPANEL = new Dimension(340, 100);
    
    public static int COUNTRANDOM = 0;

    public static String TEMP_DIRECTORY = new String(ij.Prefs.getImageJDir()
            + System.getProperty("file.separator") + "VTEA"
            + System.getProperty("file.separator") + "tmp");
    

    public static String H2_DATABASE = new String("VTEADB");
    public static String H2_MEASUREMENTS_TABLE = new String("MEASUREMENTS");
    public static String H2_OBJECT_TABLE = new String("OBJECTS");
    



    public static String MEASUREMENTS_TEMP = new String("MEASUREMENTS_TEMP");
    public static String OBJECTS_TEMP = new String("OBJECTS_TEMP");

    public static String DATABASE_DIRECTORY = ij.Prefs.getImageJDir() + "/" + "VTEA"
            + "/" + "tmp";
    public static boolean DATABASE_IN_RAM = true;
    
    public static String PLOT_DIRECTORY = new String(ij.Prefs.getImageJDir() + "/" + "VTEA"
             + "/" + "plots");

    public static String PLOT_TMP_DIRECTORY = new String(ij.Prefs.getImageJDir() + "/" + "VTEA"
             + "/" + "plots" + "/" + "tmp");
        
    public static String LASTDIRECTORY = new String(System.getProperty("user.home") 
             + "Desktop");

    public static String[] FEATURETYPE = {"Cluster", "Reduction", "Other"};

    public static String[] MEASUREMENTTYPE = {"Intensity", "Shape",
        "Texture", "Relationship"};

    public static String[] SEGMENTATIONOPTIONS;
    public static String[] PROCESSINGOPTIONS;
    public static String[] WORKFLOWOPTIONS;
    public static String[] PROCESSOROPTIONS;
    public static String[] FILETYPEOPTIONS;
    public static String[] OBJECTMEASUREMENTOPTIONS;
    public static String[] NEIGHBORHOODEASUREMENTOPTIONS;
    public static String[] MORPHOLOGICALOPTIONS;
    public static String[] FEATUREOPTIONS;
    public static String[] LUTOPTIONS;
    public static String[] PLOTMAKEROPTIONS;
    public static String[] GATEMATHOPTIONS;

    public static ConcurrentHashMap<String, String> PROCESSINGMAP;
    public static ConcurrentHashMap<String, String> SEGMENTATIONMAP;
    public static ConcurrentHashMap<String, String> WORKFLOWMAP;
    public static ConcurrentHashMap<String, String> PROCESSORMAP;
    public static ConcurrentHashMap<String, String> FILETYPEMAP;
    public static ConcurrentHashMap<String, String> OBJECTMEASUREMENTMAP;
    public static ConcurrentHashMap<String, String> NEIGHBORHOODMEASUREMENTMAP;
    public static ConcurrentHashMap<String, String> MORPHOLOGICALMAP;
    public static ConcurrentHashMap<String, String> FEATUREMAP;
    public static ConcurrentHashMap<String, String> PLOTMAKERMAP;
    public static ConcurrentHashMap<String, String> LUTMAP;
    public static ConcurrentHashMap<String, String> GATEMATHMAP;

    public static Date STARTUPTIME;

    public static void main(String[] args) {

        //set the plugins.dir property to make the plugin appear in the Plugins menu
        Class<?> clazz = _vtea.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
        System.setProperty("plugins.dir", pluginsDir);

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {


                new ImageJ();
            }
        });
    }

    //private Server sonicServer;
    public static void setJLF() {

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
        }

    }

    public static void setNLF() {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

    }

    public static long getPossibleThreads(double stackSize) {

        long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long freeMemory = Runtime.getRuntime().maxMemory() - usedMemory;

        double availMemory = freeMemory - (freeMemory * (0.25));

        if (Math.round(availMemory / stackSize) > Runtime.getRuntime().availableProcessors()) {
            //System.out.println("PROFILING:  Possible threads per dataset size: " + Runtime.getRuntime().availableProcessors());
            return Runtime.getRuntime().availableProcessors();
        } else {
            //System.out.println("PROFILING:  Possible threads per dataset size: " + Math.round(availMemory/stackSize));
            return Math.round(availMemory / stackSize);
        }
    }

    public static long getAvailableMemory() {

        long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long freeMemory = Runtime.getRuntime().maxMemory() - usedMemory;

        return freeMemory;

    }

    public static ImageStack[] getInterleavedStacks(ImagePlus imp) {
        ImageStack[] stacks = new ImageStack[imp.getNChannels()];
        ImageStack stack = imp.getImageStack();
        for (int m = 0; m <= imp.getNChannels() - 1; m++) {
            stacks[m] = new ImageStack(imp.getWidth(), imp.getHeight());
            for (int n = m; n <= imp.getStackSize() - 1; n += imp.getNChannels()) {
                stacks[m].addSlice(stack.getProcessor(n + 1));
            }
        }
        return stacks;
    }

    @Override
    public void run(String str) {
        
        mode = str;
        
        IJ.showStatus("Starting up VTEA...");

        //getUIValues();
        //context = new Context(LogService.class, PluginService.class, UIService.class);
        context = (Context)IJ.runPlugIn("org.scijava.Context", "");
        
        priority = Priority.HIGH;

        STARTUPTIME = new Date(System.currentTimeMillis());

        IJ.log("Activated Log: " + STARTUPTIME.toString());

//        LogStream.redirectSystemOut("");
//        LogStream.redirectSystemErr("");
        Frame log = WindowManager.getFrame("Log");
        log.setSize(new Dimension(760, 350));
        log.setLocation(0, 560);

        System.out.println("Starting up VTEA... ");
        System.out.println("-------------------------------- ");
        System.out.println("Available memory: " + getAvailableMemory() / (1000000000) + " GB");
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("-------------------------------- ");
        System.out.println("Seting JVM configurations...");

        System.setProperty("java.util.Arrays.sort", "true");

        System.out.println("-------------------------------- ");
        System.out.println("Setting ImageJ configurations...");

        IJ.run("Options...", "iterations=1 count=1");
        ImagePlus.addImageListener(this);

        System.out.println("-------------------------------- ");

        System.out.println("Setting-up VTEA folders...");
//        System.out.println("    VTEA folder:      " + DATABASE_DIRECTORY);
//        System.out.println("    plot folder:      " + PLOT_DIRECTORY);
//        System.out.println("    plot temp folder: " + PLOT_TMP_DIRECTORY);

        if (!Files.exists(Paths.get(DATABASE_DIRECTORY))) {

            try {

                Files.createDirectories(Paths.get(DATABASE_DIRECTORY));

            } catch (IOException e) {

                System.err.println("ERROR: VTEA directories could not be created...");

            }

        }
        
        if (!Files.exists(Paths.get(PLOT_DIRECTORY))) {

            try {

                Files.createDirectories(Paths.get(PLOT_DIRECTORY));

            } catch (IOException e) {

                System.err.println("ERROR: VTEA plot directories could not be created");

            }

        }
        if (!Files.exists(Paths.get(PLOT_TMP_DIRECTORY))) {

            try {

                Files.createDirectories(Paths.get(PLOT_TMP_DIRECTORY));

            } catch (IOException e) {

                System.err.println("ERROR: VTEA plot temp directory could not be created");

            }

        }

        System.out.println("-------------------------------- ");
        //System.out.println("Source: " + str);
        //System.out.println("-------------------------------- ");



        PROCESSINGMAP = new ConcurrentHashMap<String, String>();
        SEGMENTATIONMAP = new ConcurrentHashMap<String, String>();
        FILETYPEMAP = new ConcurrentHashMap<String, String>();
        WORKFLOWMAP = new ConcurrentHashMap<String, String>();
        PLOTMAKERMAP = new ConcurrentHashMap<String, String>();
        PROCESSORMAP = new ConcurrentHashMap<String, String>();
        OBJECTMEASUREMENTMAP = new ConcurrentHashMap<String, String>();
        NEIGHBORHOODMEASUREMENTMAP = new ConcurrentHashMap<String, String>();
        MORPHOLOGICALMAP = new ConcurrentHashMap<String, String>();
        FEATUREMAP = new ConcurrentHashMap<String, String>();
        LUTMAP = new ConcurrentHashMap<String, String>();
        GATEMATHMAP = new ConcurrentHashMap<String, String>(); 

        FileTypeService fts = new FileTypeService(context);

        FeatureService fs = new FeatureService(context);

        WorkflowService ws = new WorkflowService(context);

        ProcessorService ps = new ProcessorService(context);

        SegmentationService ss = new SegmentationService(context);

        ImageProcessingService ips = new ImageProcessingService(context);

        ObjectMeasurementService oms = new ObjectMeasurementService(context);

        NeighborhoodMeasurementService nms = new NeighborhoodMeasurementService(context);

        MorphologicalFilterService mfs = new MorphologicalFilterService(context);

        LUTService lfs = new LUTService(context);
        
        PlotMakerService pms = new PlotMakerService(context);
        
        GateMathService gms = new GateMathService(context);

        //ObjectAnalysisService oas = new ObjectAnalysisService();             
        //GroupAnalysisService gas = new GroupAnalysisService();
        //VisualizationService vs = new VisualizationService();
        List<String> fts_names = fts.getNames();
        List<String> fts_qualifiedNames = fts.getQualifiedName();

        List<String> ws_names = ws.getNames();
        List<String> ws_qualifiedNames = ws.getQualifiedName();

        List<String> ps_names = ps.getNames();
        List<String> ps_qualifiedNames = ps.getQualifiedName();

        List<String> ips_names = ips.getNames();
        List<String> ips_qualifiedNames = ips.getQualifiedName();

        List<String> oms_names = oms.getNames();
        List<String> oms_qualifiedNames = oms.getQualifiedName();

        List<String> nms_names = nms.getNames();
        List<String> nms_qualifiedNames = nms.getQualifiedName();

        List<String> ss_names = ss.getNames();
        List<String> ss_qualifiedNames = ss.getQualifiedName();

        List<String> mfs_names = mfs.getNames();
        List<String> mfs_qualifiedNames = mfs.getQualifiedName();

        List<String> fs_names = fs.getNames();
        List<String> fs_qualifiedNames = fs.getQualifiedName();

        List<String> lfs_names = lfs.getNames();
        List<String> lfs_qualifiedNames = lfs.getQualifiedName();
        
        List<String> pms_names = pms.getNames();
        List<String> pms_qualifiedNames = pms.getQualifiedName();
        
        List<String> gms_names = gms.getNames();
        List<String> gms_qualifiedNames = gms.getQualifiedName();

        //List<String> oas_names = oas.getNames();
        //List<String> oas_qualifiedNames = oas.getQualifiedName();
        //List<String> oas_names = oas.getNames();
        //List<String> oas_qualifiedNames = oas.getQualifiedName();    
        //List<String> ss_names = ss.getNames();
        //List<String> ss_namesames = ss.getQualifiedName();
        //List<String> ss_names = ss.getNames();
        //List<String> ss_namesames = ss.getQualifiedName();
        //List<String> ss_names = ss.getNames();
        //List<String> ss_namesames = ss.getQualifiedName();
        //List<String> ss_names = ss.getNames();
        //List<String> ss_namesames = ss.getQualifiedName();
        System.out.println("Loading LUT Plugins: ");
        //Logger.getAnonymousLogger().log(Level.INFO, "Loading Segmentation Plugins: ");

        LUTOPTIONS = lfs_names.toArray(new String[lfs_names.size()]);

        for (int i = 0; i < lfs_names.size(); i++) {
            try {
                Object o = Class.forName(lfs_qualifiedNames.get(i)).getDeclaredConstructor().newInstance();
                System.out.println("Loaded: " + o.getClass().getName());
                //Logger.getLogger(VTEAService.class.getName()).log(Level.INFO, "Loaded: " + o.getClass().getName());
                LUTMAP.put(LUTOPTIONS[i], o.getClass().getName());
            } catch (ClassNotFoundException | InstantiationException | 
                    IllegalAccessException | NoSuchMethodException |
                    SecurityException  | IllegalArgumentException |
                    InvocationTargetException ex) {
                Logger.getLogger(_vtea.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
        
                System.out.println("Loading PlotMaker Plugins: ");
        //Logger.getAnonymousLogger().log(Level.INFO, "Loading Segmentation Plugins: ");

        PLOTMAKEROPTIONS = pms_names.toArray(new String[pms_names.size()]);

        for (int i = 0; i < pms_names.size(); i++) {
            try {
                Object o = Class.forName(pms_qualifiedNames.get(i)).getDeclaredConstructor().newInstance();
                System.out.println("Loaded: " + o.getClass().getName());
                //Logger.getLogger(VTEAService.class.getName()).log(Level.INFO, "Loaded: " + o.getClass().getName());
                PLOTMAKERMAP.put(PLOTMAKEROPTIONS[i], o.getClass().getName());
            } catch (ClassNotFoundException | InstantiationException | 
                    IllegalAccessException | NoSuchMethodException |
                    SecurityException  | IllegalArgumentException |
                    InvocationTargetException ex) {
                Logger.getLogger(_vtea.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }

        System.out.println("Loading FileType Plugins: ");
        //Logger.getAnonymousLogger().log(Level.INFO, "Loading Segmentation Plugins: ");

        FILETYPEOPTIONS = fts_names.toArray(new String[ss_names.size()]);

        for (int i = 0; i < fts_names.size(); i++) {
            try {
                Object o = Class.forName(fts_qualifiedNames.get(i)).getDeclaredConstructor().newInstance();
                System.out.println("Loaded: " + o.getClass().getName());
                //Logger.getLogger(VTEAService.class.getName()).log(Level.INFO, "Loaded: " + o.getClass().getName());
                FILETYPEMAP.put(FILETYPEOPTIONS[i], o.getClass().getName());
            } catch (ClassNotFoundException | InstantiationException | 
                    IllegalAccessException | NoSuchMethodException |
                    SecurityException  | IllegalArgumentException |
                    InvocationTargetException ex) {
                Logger.getLogger(_vtea.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }

        System.out.println("Loading Workflow Plugins: ");
        //Logger.getAnonymousLogger().log(Level.INFO, "Loading Segmentation Plugins: ");

        WORKFLOWOPTIONS = ws_names.toArray(new String[ws_names.size()]);

        for (int i = 0; i < ws_names.size(); i++) {
            try {
                Object o = Class.forName(ws_qualifiedNames.get(i)).getDeclaredConstructor().newInstance();
                System.out.println("Loaded: " + o.getClass().getName());
                //Logger.getLogger(VTEAService.class.getName()).log(Level.INFO, "Loaded: " + o.getClass().getName());
                WORKFLOWMAP.put(WORKFLOWOPTIONS[i], o.getClass().getName());
            } catch (ClassNotFoundException | InstantiationException | 
                    IllegalAccessException | NoSuchMethodException |
                    SecurityException  | IllegalArgumentException |
                    InvocationTargetException ex) {
                Logger.getLogger(_vtea.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }

        System.out.println("Loading Processor Plugins: ");
        //Logger.getAnonymousLogger().log(Level.INFO, "Loading Image Processing Plugins: ");

        PROCESSOROPTIONS = ps_names.toArray(new String[ps_names.size()]);

        for (int i = 0; i < ps_names.size(); i++) {
            try {
                Object o = Class.forName(ps_qualifiedNames.get(i)).getDeclaredConstructor().newInstance();
                System.out.println("Loaded: " + o.getClass().getName());
                //Logger.getLogger(VTEAService.class.getName()).log(Level.INFO, "Loaded: " + o.getClass().getName());
                PROCESSORMAP.put(PROCESSOROPTIONS[i], o.getClass().getName());
            } catch (ClassNotFoundException | InstantiationException | 
                    IllegalAccessException | NoSuchMethodException |
                    SecurityException  | IllegalArgumentException |
                    InvocationTargetException ex) {
                Logger.getLogger(_vtea.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }

        System.out.println("Loading Image Processing Plugins: ");
        //Logger.getAnonymousLogger().log(Level.INFO, "Loading Image Processing Plugins: ");

        PROCESSINGOPTIONS = ips_names.toArray(new String[ips_names.size()]);

        for (int i = 0; i < ips_names.size(); i++) {
            try {
                Object o = Class.forName(ips_qualifiedNames.get(i)).getDeclaredConstructor().newInstance();
                System.out.println("Loaded: " + o.getClass().getName());
                //Logger.getLogger(VTEAService.class.getName()).log(Level.INFO, "Loaded: " + o.getClass().getName());
                PROCESSINGMAP.put(PROCESSINGOPTIONS[i], o.getClass().getName());
            } catch (ClassNotFoundException | InstantiationException | 
                    IllegalAccessException | NoSuchMethodException |
                    SecurityException  | IllegalArgumentException |
                    InvocationTargetException ex) {
                Logger.getLogger(_vtea.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }

        System.out.println("Loading Measurement Plugins: ");
        //Logger.getAnonymousLogger().log(Level.INFO, "Loading Segmentation Plugins: ");

        OBJECTMEASUREMENTOPTIONS = oms_names.toArray(new String[oms_names.size()]);

        for (int i = 0; i < oms_names.size(); i++) {
            try {
                Object o = Class.forName(oms_qualifiedNames.get(i)).getDeclaredConstructor().newInstance();
                System.out.println("Loaded: " + o.getClass().getName());
                //Logger.getLogger(VTEAService.class.getName()).log(Level.INFO, "Loaded: " + o.getClass().getName());
                OBJECTMEASUREMENTMAP.put(OBJECTMEASUREMENTOPTIONS[i], o.getClass().getName());
            } catch (ClassNotFoundException | InstantiationException | 
                    IllegalAccessException | NoSuchMethodException |
                    SecurityException  | IllegalArgumentException |
                    InvocationTargetException ex) {
                Logger.getLogger(_vtea.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }

        System.out.println("Loading Neighborhood Measurement Plugins: ");
        //Logger.getAnonymousLogger().log(Level.INFO, "Loading Segmentation Plugins: ");

        NEIGHBORHOODEASUREMENTOPTIONS = nms_names.toArray(new String[nms_names.size()]);

        for (int i = 0; i < nms_names.size(); i++) {
            try {
                Object o = Class.forName(nms_qualifiedNames.get(i)).getDeclaredConstructor().newInstance();
                System.out.println("Loaded: " + o.getClass().getName());
                //Logger.getLogger(VTEAService.class.getName()).log(Level.INFO, "Loaded: " + o.getClass().getName());
                NEIGHBORHOODMEASUREMENTMAP.put(NEIGHBORHOODEASUREMENTOPTIONS[i], o.getClass().getName());
            } catch (ClassNotFoundException | InstantiationException | 
                    IllegalAccessException | NoSuchMethodException |
                    SecurityException  | IllegalArgumentException |
                    InvocationTargetException ex) {
                Logger.getLogger(_vtea.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }

        System.out.println("Loading Segmentation Plugins: ");
        //Logger.getAnonymousLogger().log(Level.INFO, "Loading Segmentation Plugins: ");

        SEGMENTATIONOPTIONS = ss_names.toArray(new String[ss_names.size()]);

        for (int i = 0; i < ss_names.size(); i++) {
            try {
                Object o = Class.forName(ss_qualifiedNames.get(i)).getDeclaredConstructor().newInstance();
                System.out.println("Loaded: " + o.getClass().getName());
                //Logger.getLogger(VTEAService.class.getName()).log(Level.INFO, "Loaded: " + o.getClass().getName());
                SEGMENTATIONMAP.put(SEGMENTATIONOPTIONS[i], o.getClass().getName());
            } catch (ClassNotFoundException | InstantiationException | 
                    IllegalAccessException | NoSuchMethodException |
                    SecurityException  | IllegalArgumentException |
                    InvocationTargetException ex) {
                Logger.getLogger(_vtea.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }

        System.out.println("Loading Morphological Plugins: ");
        //Logger.getAnonymousLogger().log(Level.INFO, "Loading Segmentation Plugins: ");

        MORPHOLOGICALOPTIONS = mfs_names.toArray(new String[mfs_names.size()]);

        for (int i = 0; i < mfs_names.size(); i++) {
            try {
                Object o = Class.forName(mfs_qualifiedNames.get(i)).getDeclaredConstructor().newInstance();
                System.out.println("Loaded: " + o.getClass().getName());
                //Logger.getLogger(VTEAService.class.getName()).log(Level.INFO, "Loaded: " + o.getClass().getName());
                MORPHOLOGICALMAP.put(MORPHOLOGICALOPTIONS[i], o.getClass().getName());
            } catch (ClassNotFoundException | InstantiationException | 
                    IllegalAccessException | NoSuchMethodException |
                    SecurityException  | IllegalArgumentException |
                    InvocationTargetException ex) {
                Logger.getLogger(_vtea.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }

        System.out.println("Loading Feature Plugins: ");
        //Logger.getAnonymousLogger().log(Level.INFO, "Loading Segmentation Plugins: ");

        FEATUREOPTIONS = fs_names.toArray(new String[fs_names.size()]);

        for (int i = 0; i < fs_names.size(); i++) {
            try {
                Object o = Class.forName(fs_qualifiedNames.get(i)).getDeclaredConstructor().newInstance();
                System.out.println("Loaded: " + o.getClass().getName());
                //Logger.getLogger(VTEAService.class.getName()).log(Level.INFO, "Loaded: " + o.getClass().getName());
                FEATUREMAP.put(FEATUREOPTIONS[i], o.getClass().getName());
            } catch (ClassNotFoundException | InstantiationException | 
                    IllegalAccessException | NoSuchMethodException |
                    SecurityException  | IllegalArgumentException |
                    InvocationTargetException ex) {
                Logger.getLogger(_vtea.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
        
        System.out.println("Loading Gate Math Plugins: ");
        
        GATEMATHOPTIONS = gms_names.toArray(new String[gms_names.size()]);

        for (int i = 0; i < gms_names.size(); i++) {
            try {
                Object o = Class.forName(gms_qualifiedNames.get(i)).getDeclaredConstructor().newInstance();
                System.out.println("Loaded: " + o.getClass().getName());
                //Logger.getLogger(VTEAService.class.getName()).log(Level.INFO, "Loaded: " + o.getClass().getName());
                GATEMATHMAP.put(GATEMATHOPTIONS[i], o.getClass().getName());
            } catch (ClassNotFoundException | InstantiationException | 
                    IllegalAccessException | NoSuchMethodException |
                    SecurityException  | IllegalArgumentException |
                    InvocationTargetException ex) {
                Logger.getLogger(_vtea.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }

        System.out.println("-------------------------------- ");
        
        if(str.equals("load")){
                    new Thread(() -> {
            try {
                OpenObxFormat io = new OpenObxFormat();
                io.importObjects(new JPanel());
            } catch (Exception e) {
               
            }
        }).start();
        }else{
        protocolWindow = new ProtocolManagerMulti();
        protocolWindow.setVisible(true);
        }
        
//        try {
//            TestRenjin tr = new TestRenjin();
//            boolean success = tr.process();
//        } catch (Exception ex) {
//            Logger.getLogger(_vtea.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
//        System.out.println("-------------------------------- ");
        
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent evt) {

    }

    @Override
    public void imageOpened(ImagePlus imp) {
                if(mode.equals("load")){
            
        } else {
        protocolWindow.UpdateImageList();
        }
    }

    @Override
    public void imageClosed(ImagePlus imp) {
                if(mode.equals("load")){
            
        } else {
        protocolWindow.UpdateImageList();
        }
    }

    @Override
    public void imageUpdated(ImagePlus imp) {
        if(mode.equals("load")){
            
        } else {
        protocolWindow.UpdateImageList();
        }
    }

    @Override
    public Context context() {
        return context;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void setContext(Context cntxt) {
        context = cntxt;
    }

    @Override
    public double getPriority() {
        return priority;
    }

    @Override
    public void setPriority(double d) {
        priority = d;
    }

    @Override
    public int compareTo(Prioritized o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PluginInfo<?> getInfo() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setInfo(PluginInfo<?> pi) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static void setLastDirectory(String str) {
       if(str != null){
        _vtea.LASTDIRECTORY = str;
        ij.Prefs.set("VTEALastDirectory", str);
       }
    }

    public static String getLastDirectory() {
        return ij.Prefs.getString("VTEALastDirectory");
    }

    public static boolean isDatabaseInRam() {
        return DATABASE_IN_RAM;
    }

    public static void setDatabaseInRam(boolean b) {
        DATABASE_IN_RAM = b;
    }
    
    public static void clearVTEADirectory(){
        try{
        FileUtils.cleanDirectory(new File(_vtea.DATABASE_DIRECTORY)); 
        FileUtils.cleanDirectory(new File(_vtea.PLOT_DIRECTORY));
        } catch(IOException e){}
    }

}
