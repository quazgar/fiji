package fiji.plugin.trackmate.features.spot;

import static org.junit.Assert.assertEquals;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.region.localneighborhood.EllipseNeighborhood;
import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import org.junit.Before;
import org.junit.Test;

import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotImp;

public class BlobDescriptiveStatisticsTest {

	private static final int TEST_VAL = 1000;
	private static final double RADIUS = 2; // physical units
	private static final double[] CENTER = new double[] { 10, 10, 20 }; // physical units
	private static final double[] CALIBRATION = new double[] { 0.2, 0.2, 1 };
	private ImgPlus<UnsignedShortType> img2D;
	private Spot spot;

	/**
	 * Create a 2D image 
	 */
	@Before
	public void setUp() throws Exception {
		ArrayImgFactory<UnsignedShortType> factory = new ArrayImgFactory<UnsignedShortType>();
		long[] dims = new long[] { (long) (2 * CENTER[0] / CALIBRATION[0]), (long) (2 * CENTER[1] / CALIBRATION[1]) } ;
		Img<UnsignedShortType> img = factory.create(dims , new UnsignedShortType());
		img2D = new ImgPlus<UnsignedShortType>(img, 
				"2D", 
				new AxisType[] { Axes.X,  Axes.Y }, 
				new double[] { CALIBRATION[0] , CALIBRATION[1] });
		
		// We paint MANUALLY a square in the middle of the image
		RandomAccess<UnsignedShortType> ra = img.randomAccess();
		for (int j = (int) ( (CENTER[1]-RADIUS)/CALIBRATION[1] ); j < (CENTER[1]+RADIUS)/CALIBRATION[1]+1; j++) {
			ra.setPosition(j, 1);
			for (int i = (int) ( (CENTER[0]-RADIUS)/CALIBRATION[0] ); i < (CENTER[0]+RADIUS)/CALIBRATION[0]+1; i++) {
				ra.setPosition(i, 0);
				ra.get().set(TEST_VAL);
				
			}
			
		}
		
		spot = new SpotImp(new double[] { CENTER[0], CENTER[1], CENTER[2] }, "1");
		spot.putFeature(Spot.RADIUS, RADIUS);
		
	}

	@Test
	public void testProcessSpot2D() {
		 BlobDescriptiveStatistics<UnsignedShortType> analyzer = new BlobDescriptiveStatistics<UnsignedShortType>();
		 analyzer.setTarget(img2D);
		 analyzer.process(spot);
		 
		 assertEquals(TEST_VAL, spot.getFeature(BlobDescriptiveStatistics.MEAN_INTENSITY), Double.MIN_VALUE);
		 assertEquals(TEST_VAL, spot.getFeature(BlobDescriptiveStatistics.MAX_INTENSITY), Double.MIN_VALUE);
		 assertEquals(TEST_VAL, spot.getFeature(BlobDescriptiveStatistics.MIN_INTENSITY), Double.MIN_VALUE);
	}
	
	
	/**
	 * Interactive test.
	 */
	public static void main(String[] args) throws Exception {
		BlobDescriptiveStatisticsTest test = new BlobDescriptiveStatisticsTest();
		test.setUp();
		
		EllipseNeighborhood<UnsignedShortType, ImgPlus<UnsignedShortType>> disc = 
				new EllipseNeighborhood<UnsignedShortType, ImgPlus<UnsignedShortType>>(
						test.img2D, 
						new long[] { Math.round(CENTER[0]), Math.round(CENTER[1]) }, 
						new long[] { Math.round(RADIUS), Math.round(RADIUS) });
		for(UnsignedShortType pixel : disc) 
			pixel.set(1500);
		
		ij.ImageJ.main(args);
		net.imglib2.img.display.imagej.ImageJFunctions.show(test.img2D);
		
	}

}
