package explore;

import loci.common.image.IImageScaler;
import loci.common.image.SimpleImageScaler;
import loci.common.services.ServiceFactory;
import loci.formats.FormatTools;
import loci.formats.IFormatWriter;
import loci.formats.ImageReader;
import loci.formats.ImageWriter;
import loci.formats.ome.OMEPyramidStore;
import loci.formats.services.OMEXMLService;
import ome.xml.model.primitives.PositiveInteger;

public class WriteMultiResolutionTiff
{
	public static void main(String[] args) throws Exception {

		String in = "/Users/tischer/Documents/fiji-plugin-plateViewer/src/test/resources/MD-P2-S0-C2-T1/180730-Nup93-mEGFP-clone79-imaging-pipeline_A01_w2.tif";
		String out = "/Users/tischer/Documents/fiji-plugin-plateViewer/src/test/resources/MultiResolutionTiff/test01.tif";

		int scale = 2;
		int resolutions = 4;

		ImageReader reader = new ImageReader();
		ServiceFactory factory = new ServiceFactory();
		OMEXMLService service = factory.getInstance(OMEXMLService.class);
		OMEPyramidStore meta = ( OMEPyramidStore ) service.createOMEXMLMetadata();
		reader.setMetadataStore(meta);

		reader.setId(in);

		for (int i=1; i<resolutions; i++) {
			int divScale = (int) Math.pow(scale, i);
			meta.setResolutionSizeX(new PositiveInteger(reader.getSizeX() / divScale), 0, i);
			meta.setResolutionSizeY(new PositiveInteger(reader.getSizeY() / divScale), 0, i);
		}

		IImageScaler scaler = new SimpleImageScaler();
		byte[] img = reader.openBytes(0);

		// write image plane to disk
		System.out.println("Writing image to '" + out + "'...");
		IFormatWriter writer = new ImageWriter();
		writer.setMetadataRetrieve(meta);
		writer.setId(out);
		writer.saveBytes(0, img);
		int type = reader.getPixelType();
		for (int i=1; i<resolutions; i++) {
			writer.setResolution(i);
			int x = meta.getResolutionSizeX(0, i).getValue();
			int y = meta.getResolutionSizeY(0, i).getValue();
			byte[] downsample = scaler.downsample(img, reader.getSizeX(),
					reader.getSizeY(), Math.pow(scale, i),
					FormatTools.getBytesPerPixel(type), reader.isLittleEndian(),
					FormatTools.isFloatingPoint(type), reader.getRGBChannelCount(),
					reader.isInterleaved());
			writer.saveBytes(0, downsample);
		}
		writer.close();
		reader.close();

		System.out.println("Done.");
	}
}
