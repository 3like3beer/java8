package ch.gma.image;

import java.awt.image.BufferedImage;
import java.util.List;

import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;


public class TestImage {

//	static{ System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME); }
	
	@Test
	public void testProcessImage() throws Exception {
		nu.pattern.OpenCV.loadLocally();
		ProcessPdf process = new ProcessPdf();
		byte[] input = process.readBytesFromFile("C:\\Users\\ruizj\\PycharmProjects\\scan_diff\\epo-6145658-1.pdf");
		List<BufferedImage> images = process.toJpg(input);
		Mat online = process.readImageIntoMat(images.get(0),"online1.jpg");
		
		
		input = process.readBytesFromFile("C:\\Users\\ruizj\\PycharmProjects\\scan_diff\\papier-6145658-1.pdf");
		images = process.toJpg(input);
		Mat papier = process.readImageIntoMat(images.get(0),"paper1.jpg");
		
		Mat aligned = process.align(online,papier);
		System.out.println(process.getSsimMat(online, aligned));
		
		Mat diff = process.getDiff(online, aligned);
		Imgcodecs.imwrite("C:\\Users\\ruizj\\PycharmProjects\\scan_diff\\diff.png", diff);
		
		
		
		
	}
}
