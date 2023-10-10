package vtea.utilities;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Scaler;
import ij.process.*;
import loci.formats.MetadataTools;
import loci.formats.meta.IMetadata;
import loci.formats.meta.IPyramidStore;
import loci.formats.out.PyramidOMETiffWriter;
import loci.formats.out.OMETiffWriter;
import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.units.unit.Unit;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.Color;
import ome.xml.model.primitives.PositiveInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

//reworked from: https://forum.image.sc/t/from-imageplus-to-pyramid-ome-tiff-metadata-whats-the-best-way/60374/3

public class ImagePlusToPyramidOMETiff {

//    private static Logger logger = LoggerFactory.getLogger(ImagePlusToOMETiff.class);

    // Inspired from https://github.com/dgault/bio-formats-examples/blob/6cdb11e8c64566611b18f384b3a257dab5037e90/src/main/macros/jython/PyramidConversion.py
    // And https://github.com/qupath/qupath/blob/430d6212e641f677dc9a411cf8033fbbe4da2fd6/qupath-extension-bioformats/src/main/java/qupath/lib/images/writers/ome/OMEPyramidWriter.java
    public static void writeToOMETiff(ImagePlus image, File outFile, int resolutions, int scale) throws Exception{

        // Copy metadata from ImagePlus:
        IMetadata omeMeta = MetadataTools.createOMEXMLMetadata();

        boolean isLittleEndian = false;
        boolean isRGB = false;
        boolean isInterleaved = false;
        int nChannels = image.getNChannels();

        int series = 0;
        omeMeta.setImageID("Image:"+series, series);
        omeMeta.setPixelsID("Pixels:"+series, series);
        if (image.getTitle() != null)
            omeMeta.setImageName(image.getTitle(), series);

        //meta.setPixelsBigEndian(ByteOrder.BIG_ENDIAN.equals(endian), series);

        omeMeta.setPixelsDimensionOrder(DimensionOrder.XYCZT, series);
        switch (image.getType()) {
            case ImagePlus.GRAY8:
                omeMeta.setPixelsType(PixelType.UINT8, series);
                break;
            case ImagePlus.GRAY16:
                isLittleEndian = false;
                omeMeta.setPixelsType(PixelType.UINT16, series);
                break;
            case ImagePlus.GRAY32:
                isLittleEndian = false;
                omeMeta.setPixelsType(PixelType.FLOAT, series);
                break;
//            case ImagePlus.COLOR_RGB:
//                nChannels = 3;
//                isInterleaved = true;
//                isRGB = true;
//                omeMeta.setPixelsType(PixelType.UINT8, series);
//                throw new UnsupportedOperationException("Unhandled bit depth: "+image.getBitDepth());
//                //break;
            default:
                throw new UnsupportedOperationException("Cannot convert image of type " + image.getType() + " into a valid OME PixelType");
        }

        omeMeta.setPixelsBigEndian(!isLittleEndian, 0);

        int width = image.getWidth();
        int height = image.getHeight();

        int sizeZ = image.getNSlices();
        int sizeT = image.getNFrames();
        int sizeC = image.getNChannels();

        omeMeta.setPixelsSizeZ(new PositiveInteger(sizeZ), series);
        omeMeta.setPixelsSizeT(new PositiveInteger(sizeT), series);

        // Set channel colors
        omeMeta.setPixelsSizeC(new PositiveInteger(nChannels), series);
        if (isRGB) {
            omeMeta.setChannelID("Channel:0", series, 0);
            omeMeta.setPixelsInterleaved(isInterleaved, series);
            omeMeta.setChannelSamplesPerPixel(new PositiveInteger(3), series, 0); //nSamples = 3; // TODO : check!
        } else {
            omeMeta.setChannelSamplesPerPixel(new PositiveInteger(1), series, 0);
            omeMeta.setPixelsInterleaved(isInterleaved, series);
            for (int c = 0; c < nChannels; c++) {
                omeMeta.setChannelID("Channel:0:" + c, series, c);
                omeMeta.setChannelSamplesPerPixel(new PositiveInteger(1), series, c);
                LUT channelLUT = image.getLuts()[c];
                int colorRed = channelLUT.getRed(255);
                int colorGreen = channelLUT.getGreen(255);
                int colorBlue = channelLUT.getBlue(255);
                int colorAlpha = channelLUT.getAlpha(255);
                omeMeta.setChannelColor(new Color(colorRed, colorGreen, colorBlue, colorAlpha), series, c);
                omeMeta.setChannelName("Channel_"+c, series, c);
            }
        }

        // Set physical units, if we have them

        if (image.getCalibration()!=null) {
            Calibration cal = image.getCalibration();
            Unit<Length> unit = getUnitFromCalibration(cal);
            omeMeta.setPixelsPhysicalSizeX(new Length(cal.pixelWidth, unit), series);
            omeMeta.setPixelsPhysicalSizeY(new Length(cal.pixelHeight, unit), series);
            omeMeta.setPixelsPhysicalSizeZ(new Length(cal.pixelDepth, unit), series);
            // set Origin in XYZ
            // TODO : check if enough or other planes need to be set ?
            omeMeta.setPlanePositionX(new Length(cal.xOrigin*cal.pixelWidth, unit),0,0);
            omeMeta.setPlanePositionY(new Length(cal.yOrigin*cal.pixelHeight, unit),0,0);
            omeMeta.setPlanePositionZ(new Length(cal.zOrigin*cal.pixelDepth, unit),0,0);
        }

        // Set resolutions
        omeMeta.setPixelsSizeX(new PositiveInteger(width), series);
        omeMeta.setPixelsSizeY(new PositiveInteger(height), series);

        ((IPyramidStore)omeMeta).setResolutionSizeX(new PositiveInteger(width),series, 0);
        ((IPyramidStore)omeMeta).setResolutionSizeY(new PositiveInteger(height),series, 0);

        // setup resolutions
        for (int i= 0;i<resolutions-1;i++) {
            double divScale = Math.pow(scale, i + 1);
            ((IPyramidStore)omeMeta).setResolutionSizeX(new PositiveInteger((int)(width / divScale)),series, i + 1);
            ((IPyramidStore)omeMeta).setResolutionSizeY(new PositiveInteger((int)(height / divScale)),series, i + 1);
        }

        // setup writer
        PyramidOMETiffWriter writer = new PyramidOMETiffWriter();
        writer.setWriteSequentially(true); // Setting this to false can be problematic!

        writer.setMetadataRetrieve(omeMeta);
        writer.setId(outFile.getAbsolutePath());

        // create ImageScaler for downsampling

        // generate downsampled resolutions and write to output
        for (int r = 0; r < resolutions; r++) {
//            logger.debug("Saving resolution size " + r);
//            IJ.log("Saving resolution size " + r);
            writer.setResolution(r);

            for (int t=0;t<image.getNFrames();t++) {
                for (int z=0;z<image.getNSlices();z++) {
                    for (int c=0;c<image.getNChannels();c++) {
                        IJ.log("(" + r+","+t+","+z+","+c+")");

                        ImageProcessor processor;

                        if (image.getStack()==null) {
                            processor = image.getProcessor();
                        } else {
                            processor = image.getStack().getProcessor(image.getStackIndex(c+1, z+1, t+1));
                        }

                        if (r!=0) {
                            Integer x = ((IPyramidStore)omeMeta).getResolutionSizeX(0, r).getValue();
                            Integer y = ((IPyramidStore)omeMeta).getResolutionSizeY(0, r).getValue();
                            processor.setInterpolationMethod(ImageProcessor.BILINEAR);
                            processor = processor.resize(x,y);
                        }

                        int plane = t * sizeZ * sizeC + z * sizeC + c;
                        writer.saveBytes(plane, processorToBytes(processor, processor.getWidth()*processor.getHeight()));

                    }
                }
            }
        }
        writer.close();
//        logger.debug("File saved");

    }

    public static Unit<Length> getUnitFromCalibration(Calibration cal) {
        switch (cal.getUnit()) {
            case "um":
            case "\u03BCm":
            case "\u03B5m":
            case "µm":
            case "micrometer":
                return UNITS.MICROMETER;
            case "mm":
            case "millimeter":
                return UNITS.MILLIMETER;
            case "cm":
            case "centimeter":
                return UNITS.CENTIMETER;
            case "m":
            case "meter":
                return UNITS.METRE;
            default:
                return UNITS.REFERENCEFRAME;
        }
    }

    private static byte[] processorToBytes(ImageProcessor processor, int nPixels) {
        ByteBuffer byteBuf;
        switch (processor.getBitDepth()) {
            case 8:
                return (byte[])processor.getPixels();
            case 16:
                //https://stackoverflow.com/questions/10804852/how-to-convert-short-array-to-byte-array
                // Slow...
                byteBuf = ByteBuffer.allocate(2*nPixels);
                short[] pixels_short = (short[]) processor.getPixels();
                for (short v : pixels_short) byteBuf.putShort(v);
                return byteBuf.array();
            case 32:
                byteBuf = ByteBuffer.allocate(4*nPixels);
                float[] pixels_float = (float[]) processor.getPixels();
                for (float v : pixels_float) byteBuf.putFloat(v);
                return byteBuf.array();
//            case 24:
//                /*byteBuf = ByteBuffer.allocate(4*nPixels);
//                ColorProcessor
//                float[] pixels_float = (float[]) processor.getPixels();
//                for (float v : pixels_float) byteBuf.putFloat(v);
//                return byteBuf.array();
//                break;*/
            default:
                throw new UnsupportedOperationException("Unhandled bit depth: "+processor.getBitDepth());
        }
    }

}